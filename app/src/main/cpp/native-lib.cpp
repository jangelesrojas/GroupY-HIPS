#include <jni.h>
#include <string>
#include <vector>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <setjmp.h>

extern "C" {
#include <jpeglib.h>
}

static constexpr int BIT_REPETITION = 5; //can swap to 3 but for 100 chars 5 offers a lot more robustness
static constexpr int HEADER_BYTES = 8;
static constexpr int MAX_MESSAGE_BYTES = 100;

struct JpegErrorManager {
    jpeg_error_mgr pub;
    jmp_buf setjmpBuffer;
};

METHODDEF(void) hipsErrorExit(j_common_ptr cinfo) {
    auto* err = reinterpret_cast<JpegErrorManager*>(cinfo->err);
    longjmp(err->setjmpBuffer, 1);
}

static std::vector<uint8_t> buildPayload(const std::string& message) {
    std::vector<uint8_t> payload;

    payload.push_back('H');
    payload.push_back('I');
    payload.push_back('P');
    payload.push_back('S');

    uint32_t len = static_cast<uint32_t>(message.size());
    payload.push_back((len >> 24) & 0xFF);
    payload.push_back((len >> 16) & 0xFF);
    payload.push_back((len >> 8) & 0xFF);
    payload.push_back(len & 0xFF);

    payload.insert(payload.end(), message.begin(), message.end());
    return payload;
}

static inline int getPayloadBit(const std::vector<uint8_t>& payload, size_t bitIndex) {
    size_t byteIndex = bitIndex / 8;
    int bitOffset = 7 - static_cast<int>(bitIndex % 8);
    return (payload[byteIndex] >> bitOffset) & 1;
}

static inline bool isUsableMediumCoeff(JCOEF coeff, int zigzagIndex) {
    if (zigzagIndex == 0) return false;

    // Medium-frequency band
    if (zigzagIndex < 10 || zigzagIndex > 40) return false;

    int mag = std::abs(static_cast<int>(coeff));

    // Skip fragile tiny values and very large values
    if (mag < 2 || mag > 20) return false;

    return true;
}

template <typename Func>
static void forEachUsableCoeff(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        Func func
) {
    for (int comp = 0; comp < dinfo->num_components; ++comp) {
        jpeg_component_info* compInfo = &dinfo->comp_info[comp];

        for (JDIMENSION row = 0; row < compInfo->height_in_blocks; ++row) {
            JBLOCKARRAY buffer = (dinfo->mem->access_virt_barray)(
                    reinterpret_cast<j_common_ptr>(dinfo),
                    coefArrays[comp],
                    row,
                    1,
                    TRUE
            );

            for (JDIMENSION col = 0; col < compInfo->width_in_blocks; ++col) {
                JCOEFPTR block = buffer[0][col];

                for (int k = 1; k < DCTSIZE2; ++k) {
                    if (!isUsableMediumCoeff(block[k], k)) continue;

                    if (!func(block[k])) {
                        return;
                    }
                }
            }
        }
    }
}

static size_t countUsableCoefficients(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays
) {
    size_t usableBits = 0;

    forEachUsableCoeff(dinfo, coefArrays, [&](JCOEF& coeff) {
        (void)coeff;
        ++usableBits;
        return true;
    });

    return usableBits;
}

static int computeCapacityBytes(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays
) {
    size_t usableBits = countUsableCoefficients(dinfo, coefArrays);

    // Each logical payload bit consumes BIT_REPETITION coefficients
    size_t effectivePayloadBits = usableBits / BIT_REPETITION;

    if (effectivePayloadBits < HEADER_BYTES * 8) {
        return 0;
    }

    int bytes = static_cast<int>(effectivePayloadBits / 8) - HEADER_BYTES;
    if (bytes < 0) bytes = 0;

    // hard cap for this project
    if (bytes > MAX_MESSAGE_BYTES) {
        bytes = MAX_MESSAGE_BYTES;
    }

    return bytes;
}

static void writeBitToCoeff(JCOEF& coeff, int bit) {
    int value = static_cast<int>(coeff);
    int sign = (value < 0) ? -1 : 1;
    int mag = std::abs(value);

    if ((mag & 1) != bit) {
        if (bit == 1) {
            if ((mag % 2) == 0) mag += 1;
        } else {
            if ((mag % 2) == 1) {
                if (mag == 3) mag = 4;
                else mag -= 1;
            }
        }
    }

    if (mag < 2) mag = 2;
    coeff = static_cast<JCOEF>(sign * mag);
}

static bool embedPayloadIntoCoefficients(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        const std::vector<uint8_t>& payload
) {
    const size_t logicalBits = payload.size() * 8;
    const size_t totalEmbeddedBits = logicalBits * BIT_REPETITION;

    size_t embeddedBitIndex = 0;

    forEachUsableCoeff(dinfo, coefArrays, [&](JCOEF& coeff) {
        if (embeddedBitIndex >= totalEmbeddedBits) {
            return false;
        }

        size_t logicalBitIndex = embeddedBitIndex / BIT_REPETITION;
        int bit = getPayloadBit(payload, logicalBitIndex);

        writeBitToCoeff(coeff, bit);
        ++embeddedBitIndex;
        return true;
    });

    return embeddedBitIndex == totalEmbeddedBits;
}

// Ready for later extraction work
static inline int readBitFromCoeff(JCOEF coeff) {
    return std::abs(static_cast<int>(coeff)) & 1;
}

static int majorityVote5(int a, int b, int c, int d, int e) {
    int sum = a + b + c + d + e;
    return (sum >= 3) ? 1 : 0;
}

static bool embedJpegInternal(
        const char* inputPath,
        const char* outputPath,
        const std::string& message
) {
    if (message.size() > MAX_MESSAGE_BYTES) {
        return false;
    }

    FILE* inFile = std::fopen(inputPath, "rb");
    if (!inFile) return false;

    FILE* outFile = std::fopen(outputPath, "wb");
    if (!outFile) {
        std::fclose(inFile);
        return false;
    }

    jpeg_decompress_struct dinfo{};
    jpeg_compress_struct cinfo{};
    JpegErrorManager jerrDecomp{};
    JpegErrorManager jerrComp{};

    dinfo.err = jpeg_std_error(&jerrDecomp.pub);
    jerrDecomp.pub.error_exit = hipsErrorExit;

    cinfo.err = jpeg_std_error(&jerrComp.pub);
    jerrComp.pub.error_exit = hipsErrorExit;

    if (setjmp(jerrDecomp.setjmpBuffer)) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    if (setjmp(jerrComp.setjmpBuffer)) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    jpeg_create_decompress(&dinfo);
    jpeg_stdio_src(&dinfo, inFile);
    jpeg_read_header(&dinfo, TRUE);

    jvirt_barray_ptr* coefArrays = jpeg_read_coefficients(&dinfo);

    int capacityBytes = computeCapacityBytes(&dinfo, coefArrays);
    if (capacityBytes <= 0 || static_cast<int>(message.size()) > capacityBytes) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    std::vector<uint8_t> payload = buildPayload(message);

    if (!embedPayloadIntoCoefficients(&dinfo, coefArrays, payload)) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    jpeg_create_compress(&cinfo);
    jpeg_stdio_dest(&cinfo, outFile);

    jpeg_copy_critical_parameters(&dinfo, &cinfo);
    jpeg_write_coefficients(&cinfo, coefArrays);

    jpeg_finish_compress(&cinfo);
    jpeg_finish_decompress(&dinfo);

    jpeg_destroy_compress(&cinfo);
    jpeg_destroy_decompress(&dinfo);

    std::fclose(inFile);
    std::fclose(outFile);

    return true;
}

static int getCapacityInternal(const char* inputPath) {
    FILE* inFile = std::fopen(inputPath, "rb");
    if (!inFile) return 0;

    jpeg_decompress_struct dinfo{};
    JpegErrorManager jerr{};

    dinfo.err = jpeg_std_error(&jerr.pub);
    jerr.pub.error_exit = hipsErrorExit;

    if (setjmp(jerr.setjmpBuffer)) {
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return 0;
    }

    jpeg_create_decompress(&dinfo);
    jpeg_stdio_src(&dinfo, inFile);
    jpeg_read_header(&dinfo, TRUE);
    jvirt_barray_ptr* coefArrays = jpeg_read_coefficients(&dinfo);

    int capacity = computeCapacityBytes(&dinfo, coefArrays);

    jpeg_finish_decompress(&dinfo);
    jpeg_destroy_decompress(&dinfo);
    std::fclose(inFile);

    return capacity;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_hips_Steganography_embedJpegMessage(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_,
        jstring outputPath_,
        jstring message_
) {
    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);
    const char* outputPath = env->GetStringUTFChars(outputPath_, nullptr);
    const char* messageChars = env->GetStringUTFChars(message_, nullptr);

    bool success = embedJpegInternal(
            inputPath,
            outputPath,
            std::string(messageChars)
    );

    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
    env->ReleaseStringUTFChars(message_, messageChars);

    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_hips_Steganography_getEmbedCapacityBytes(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_
) {
    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);
    int capacity = getCapacityInternal(inputPath);
    env->ReleaseStringUTFChars(inputPath_, inputPath);
    return capacity;
}