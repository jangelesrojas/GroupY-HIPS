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

// Repeats each payload bit 5 times to make the hidden message more robust
// if a few coefficients get changed later. (can switch it to 3 for more bytes but 5
// is more robust especially if we are limiting the message size)
static constexpr int BIT_REPETITION = 5;

// Header format:
// 4 bytes = "HIPS"
// 4 bytes = message length
static constexpr int HEADER_BYTES = 8;

// Project limit for hidden message size.
// We are keeping this small on purpose for better reliability.
static constexpr int MAX_MESSAGE_BYTES = 100;

// Custom libjpeg error wrapper so we can safely jump out on read/write failures.
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

    // Magic bytes let us identify that this JPEG contains our format.
    payload.push_back('H');
    payload.push_back('I');
    payload.push_back('P');
    payload.push_back('S');

    // Store message length in 4 bytes so extraction knows how much to read.
    uint32_t len = static_cast<uint32_t>(message.size());
    payload.push_back((len >> 24) & 0xFF);
    payload.push_back((len >> 16) & 0xFF);
    payload.push_back((len >> 8) & 0xFF);
    payload.push_back(len & 0xFF);

    // After the header, append the actual message bytes.
    payload.insert(payload.end(), message.begin(), message.end());
    return payload;
}

static inline int getPayloadBit(const std::vector<uint8_t>& payload, size_t bitIndex) {
    size_t byteIndex = bitIndex / 8;
    int bitOffset = 7 - static_cast<int>(bitIndex % 8);
    return (payload[byteIndex] >> bitOffset) & 1;
}

static inline bool isUsableMediumCoeff(JCOEF coeff, int zigzagIndex) {
    // Never touch the DC coefficient because that affects the block too much.
    if (zigzagIndex == 0) return false;

    // Only use a middle-frequency range.
    // Low frequencies are too visually important.
    // Very high frequencies are less stable after compression.
    if (zigzagIndex < 10 || zigzagIndex > 40) return false;

    int mag = std::abs(static_cast<int>(coeff));

    // Skip tiny values because they are easier to break.
    // Skip very large values to avoid making obvious changes.
    if (mag < 2 || mag > 20) return false;

    return true;
}

template <typename Func>
static void forEachUsableCoeff(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        Func func
) {
    // Walk through every JPEG component and every 8x8 DCT block.
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

    // Because of repetition, one real payload bit uses multiple coefficients.
    size_t effectivePayloadBits = usableBits / BIT_REPETITION;

    // Need enough room for at least the fixed header.
    if (effectivePayloadBits < HEADER_BYTES * 8) {
        return 0;
    }

    int bytes = static_cast<int>(effectivePayloadBits / 8) - HEADER_BYTES;
    if (bytes < 0) bytes = 0;

    // Keep capacity within the project message limit.
    if (bytes > MAX_MESSAGE_BYTES) {
        bytes = MAX_MESSAGE_BYTES;
    }

    return bytes;
}

static void writeBitToCoeff(JCOEF& coeff, int bit) {
    int value = static_cast<int>(coeff);
    int sign = (value < 0) ? -1 : 1;
    int mag = std::abs(value);

    // We encode the bit using the parity of the coefficient magnitude.
    // Odd = 1, even = 0.
    if ((mag & 1) != bit) {
        if (bit == 1) {
            if ((mag % 2) == 0) mag += 1;
        } else {
            if ((mag % 2) == 1) {
                // Avoid collapsing into unstable tiny values.
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

        // Every real payload bit is written multiple times.
        size_t logicalBitIndex = embeddedBitIndex / BIT_REPETITION;
        int bit = getPayloadBit(payload, logicalBitIndex);

        writeBitToCoeff(coeff, bit);
        ++embeddedBitIndex;
        return true;
    });

    return embeddedBitIndex == totalEmbeddedBits;
}

// These helpers are for extraction later.
// We are not using them yet in embed, but they match the rep-5 format.
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

    // Save APP markers and comment markers from the original JPEG.
    // This helps preserve EXIF data like orientation, which fixes the
    // rotated-image issue after embedding.
    for (int marker = 0xE0; marker <= 0xEF; ++marker) {
        jpeg_save_markers(&dinfo, marker, 0xFFFF);
    }
    jpeg_save_markers(&dinfo, JPEG_COM, 0xFFFF);

    jpeg_read_header(&dinfo, TRUE);

    // Read the DCT coefficients directly instead of decoding to pixels.
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

    // Copy JPEG settings so the output stays compatible with the input structure.
    jpeg_copy_critical_parameters(&dinfo, &cinfo);
    jpeg_write_coefficients(&cinfo, coefArrays);

    // Write the saved metadata markers back into the output JPEG.
    // This preserves EXIF and other useful JPEG metadata.
    for (jpeg_saved_marker_ptr marker = dinfo.marker_list; marker != nullptr; marker = marker->next) {
        jpeg_write_marker(&cinfo, marker->marker, marker->data, marker->data_length);
    }

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

static bool extractLogicalBits(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        size_t logicalBitsNeeded,
        std::vector<uint8_t>& outBytes
) {
    const size_t totalPhysicalBitsNeeded = logicalBitsNeeded * BIT_REPETITION;
    std::vector<int> physicalBits;
    physicalBits.reserve(totalPhysicalBitsNeeded);

    forEachUsableCoeff(dinfo, coefArrays, [&](JCOEF& coeff) {
        if (physicalBits.size() >= totalPhysicalBitsNeeded) {
            return false;
        }
        physicalBits.push_back(readBitFromCoeff(coeff));
        return true;
    });

    if (physicalBits.size() < totalPhysicalBitsNeeded) {
        return false;
    }

    outBytes.assign((logicalBitsNeeded + 7) / 8, 0);

    for (size_t logicalBitIndex = 0; logicalBitIndex < logicalBitsNeeded; ++logicalBitIndex) {
        size_t base = logicalBitIndex * BIT_REPETITION;
        int votedBit = majorityVote5(
                physicalBits[base + 0],
                physicalBits[base + 1],
                physicalBits[base + 2],
                physicalBits[base + 3],
                physicalBits[base + 4]
        );

        size_t byteIndex = logicalBitIndex / 8;
        int bitOffset = 7 - static_cast<int>(logicalBitIndex % 8);
        outBytes[byteIndex] |= static_cast<uint8_t>(votedBit << bitOffset);
    }

    return true;
}

static std::string extractJpegInternal(const char* inputPath, bool& ok) {
    ok = false;

    FILE* inFile = std::fopen(inputPath, "rb");
    if (!inFile) return "";

    jpeg_decompress_struct dinfo{};
    JpegErrorManager jerr{};
    dinfo.err = jpeg_std_error(&jerr.pub);
    jerr.pub.error_exit = hipsErrorExit;

    if (setjmp(jerr.setjmpBuffer)) {
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    jpeg_create_decompress(&dinfo);
    jpeg_stdio_src(&dinfo, inFile);
    jpeg_read_header(&dinfo, TRUE);
    jvirt_barray_ptr* coefArrays = jpeg_read_coefficients(&dinfo);

    // First extract the 8-byte header
    std::vector<uint8_t> headerBytes;
    if (!extractLogicalBits(&dinfo, coefArrays, HEADER_BYTES * 8, headerBytes)) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    if (headerBytes.size() < HEADER_BYTES) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    if (!(headerBytes[0] == 'H' &&
          headerBytes[1] == 'I' &&
          headerBytes[2] == 'P' &&
          headerBytes[3] == 'S')) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    uint32_t msgLen =
            (static_cast<uint32_t>(headerBytes[4]) << 24) |
            (static_cast<uint32_t>(headerBytes[5]) << 16) |
            (static_cast<uint32_t>(headerBytes[6]) << 8)  |
            (static_cast<uint32_t>(headerBytes[7]));

    if (msgLen > MAX_MESSAGE_BYTES) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    const size_t totalBytes = HEADER_BYTES + msgLen;
    std::vector<uint8_t> payloadBytes;
    if (!extractLogicalBits(&dinfo, coefArrays, totalBytes * 8, payloadBytes)) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    if (payloadBytes.size() < totalBytes) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    std::string message(
            payloadBytes.begin() + HEADER_BYTES,
            payloadBytes.begin() + HEADER_BYTES + msgLen
    );

    jpeg_finish_decompress(&dinfo);
    jpeg_destroy_decompress(&dinfo);
    std::fclose(inFile);

    ok = true;
    return message;
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

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_hips_Steganography_extractJpegMessage(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_
) {
    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);

    bool ok = false;
    std::string message = extractJpegInternal(inputPath, ok);

    env->ReleaseStringUTFChars(inputPath_, inputPath);

    if (!ok) {
        return nullptr;
    }

    return env->NewStringUTF(message.c_str());
}