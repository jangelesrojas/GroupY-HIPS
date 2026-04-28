#include <jni.h>
#include <string>
#include <vector>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <setjmp.h>
#include <cctype>

extern "C" {
#include <jpeglib.h>
}

// This file holds the native JPEG steganography logic.
// Kotlin calls into this through JNI from the Steganography object.

// Repeats each header bit 20 times.
// The header is extra important because it stores the "HIPS" marker and message length.
// If the header breaks, extraction will not know that a message exists.
static constexpr int HEADER_BIT_REPETITION = 20;

// Repeats each message bit 5 times.
// This gives the hidden message some protection if a few JPEG coefficients change.
static constexpr int PAYLOAD_BIT_REPETITION = 5;

// Header format:
// 4 bytes = "HIPS"
// 4 bytes = message length
static constexpr int HEADER_BYTES = 8;

// Project limit for hidden message size.
// This keeps the app more reliable because longer messages need more JPEG coefficients.
static constexpr int MAX_MESSAGE_BYTES = 100;

// Custom libjpeg error wrapper.
// libjpeg uses longjmp for errors, so this lets us exit safely instead of crashing.
struct JpegErrorManager {
    jpeg_error_mgr pub;
    jmp_buf setjmpBuffer;
};

// This is called by libjpeg if a JPEG read/write error happens.
// It jumps back to the setjmp block so the app can return false instead of crashing.
METHODDEF(void) hipsErrorExit(j_common_ptr cinfo) {
    auto* err = reinterpret_cast<JpegErrorManager*>(cinfo->err);
    longjmp(err->setjmpBuffer, 1);
}

// Builds the byte payload that will actually be embedded into the JPEG.
// The payload is header first, then the message bytes.
static std::vector<uint8_t> buildPayload(const std::string& message) {
    std::vector<uint8_t> payload;

    // Magic bytes let extraction recognize our hidden message format.
    payload.push_back('H');
    payload.push_back('I');
    payload.push_back('P');
    payload.push_back('S');

    // Store the message length as 4 bytes in big-endian order.
    // This lets extraction know exactly how many message bytes to read.
    uint32_t len = static_cast<uint32_t>(message.size());
    payload.push_back((len >> 24) & 0xFF);
    payload.push_back((len >> 16) & 0xFF);
    payload.push_back((len >> 8) & 0xFF);
    payload.push_back(len & 0xFF);

    // Add the real message after the header.
    payload.insert(payload.end(), message.begin(), message.end());
    return payload;
}

// Reads one logical bit from the payload byte array.
// Bits are read from left to right inside each byte.
static inline int getPayloadBit(const std::vector<uint8_t>& payload, size_t bitIndex) {
    size_t byteIndex = bitIndex / 8;
    int bitOffset = 7 - static_cast<int>(bitIndex % 8);
    return (payload[byteIndex] >> bitOffset) & 1;
}

// Decides how many times a logical bit should be repeated.
// Header bits get repeated more than message bits.
static inline int getRepeatCountForLogicalBit(size_t logicalBitIndex) {
    // Header = "HIPS" + length field.
    if (logicalBitIndex < HEADER_BYTES * 8) {
        return HEADER_BIT_REPETITION;
    }

    // Message body uses normal payload repetition.
    return PAYLOAD_BIT_REPETITION;
}

// Counts how many actual coefficient writes are needed for a given number of logical bits.
static size_t countPhysicalBitsNeeded(size_t logicalBits) {
    size_t total = 0;

    for (size_t i = 0; i < logicalBits; i++) {
        total += getRepeatCountForLogicalBit(i);
    }

    return total;
}

// Uses majority vote to recover one repeated bit during extraction.
// For example, with 5 repeats, three or more 1s means the bit is read as 1.
static int majorityVoteRepeated(const std::vector<int>& bits, size_t start, int repeatCount) {
    int ones = 0;

    for (int i = 0; i < repeatCount; i++) {
        if (bits[start + i] == 1) {
            ones++;
        }
    }

    return (ones > repeatCount / 2) ? 1 : 0;
}

// Checks if a DCT coefficient is safe enough to use for embedding.
// The goal is to avoid coefficients that are too visible or too unstable.
static inline bool isUsableMediumCoeff(JCOEF coeff, int zigzagIndex) {
    // Never touch the DC coefficient because that controls the block's main brightness.
    if (zigzagIndex == 0) return false;

    // Use only middle-frequency coefficients.
    // Low frequencies affect image appearance more.
    // Very high frequencies are more likely to get removed by JPEG compression.
    if (zigzagIndex < 10 || zigzagIndex > 40) return false;

    int mag = std::abs(static_cast<int>(coeff));

    // Skip tiny values because small coefficients are easier to break.
    // Skip very large values to avoid making noticeable changes.
    if (mag < 2 || mag > 20) return false;

    return true;
}

// Generic helper that walks through every usable DCT coefficient.
// The passed-in function decides what to do with each coefficient.
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

                    // If func returns false, stop early.
                    if (!func(block[k])) {
                        return;
                    }
                }
            }
        }
    }
}

// Counts how many coefficients are available for storing bits.
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

// Computes the number of message bytes this JPEG can support.
// It subtracts the repeated header first, then calculates message space.
static int computeCapacityBytes(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays
) {
    size_t usableBits = countUsableCoefficients(dinfo, coefArrays);

    // Header is required for every embedded message.
    size_t headerPhysicalBits = (HEADER_BYTES * 8) * HEADER_BIT_REPETITION;

    if (usableBits < headerPhysicalBits) {
        return 0;
    }

    // Remaining space can hold repeated message bits.
    size_t remainingPhysicalBits = usableBits - headerPhysicalBits;
    size_t messageLogicalBits = remainingPhysicalBits / PAYLOAD_BIT_REPETITION;

    int bytes = static_cast<int>(messageLogicalBits / 8);

    if (bytes < 0) {
        bytes = 0;
    }

    // Never report more than the app's project limit.
    if (bytes > MAX_MESSAGE_BYTES) {
        bytes = MAX_MESSAGE_BYTES;
    }

    return bytes;
}

// Writes one bit into a coefficient by changing the magnitude parity.
// Odd magnitude = 1.
// Even magnitude = 0.
static void writeBitToCoeff(JCOEF& coeff, int bit) {
    int value = static_cast<int>(coeff);
    int sign = (value < 0) ? -1 : 1;
    int mag = std::abs(value);

    // Only adjust the coefficient if its current parity does not match the bit.
    if ((mag & 1) != bit) {
        if (bit == 1) {
            if ((mag % 2) == 0) mag += 1;
        } else {
            if ((mag % 2) == 1) {
                // Avoid dropping a coefficient into a tiny unstable value.
                if (mag == 3) mag = 4;
                else mag -= 1;
            }
        }
    }

    // Keep the magnitude from becoming too small.
    if (mag < 2) mag = 2;
    coeff = static_cast<JCOEF>(sign * mag);
}

// Embeds the full payload into the selected DCT coefficients.
// It writes repeated physical bits for each logical bit.
static bool embedPayloadIntoCoefficients(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        const std::vector<uint8_t>& payload
) {
    const size_t logicalBits = payload.size() * 8;
    const size_t totalPhysicalBitsNeeded = countPhysicalBitsNeeded(logicalBits);

    size_t logicalBitIndex = 0;
    int repeatIndex = 0;
    size_t physicalBitsWritten = 0;

    forEachUsableCoeff(dinfo, coefArrays, [&](JCOEF& coeff) {
        if (physicalBitsWritten >= totalPhysicalBitsNeeded || logicalBitIndex >= logicalBits) {
            return false;
        }

        int bit = getPayloadBit(payload, logicalBitIndex);
        writeBitToCoeff(coeff, bit);

        physicalBitsWritten++;
        repeatIndex++;

        int repeatCount = getRepeatCountForLogicalBit(logicalBitIndex);

        // Once this logical bit has been repeated enough, move to the next bit.
        if (repeatIndex >= repeatCount) {
            repeatIndex = 0;
            logicalBitIndex++;
        }

        return true;
    });

    return physicalBitsWritten == totalPhysicalBitsNeeded;
}

// Reads one bit from a coefficient using the same parity rule as embedding.
static inline int readBitFromCoeff(JCOEF coeff) {
    return std::abs(static_cast<int>(coeff)) & 1;
}

// Main internal embed function.
// It reads the input JPEG, edits DCT coefficients, and writes the output JPEG.
static bool embedJpegInternal(
        const char* inputPath,
        const char* outputPath,
        const std::string& message
) {
    // Reject messages that are too large for the project limit.
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

    // Attach our custom error handlers for decompression and compression.
    dinfo.err = jpeg_std_error(&jerrDecomp.pub);
    jerrDecomp.pub.error_exit = hipsErrorExit;

    cinfo.err = jpeg_std_error(&jerrComp.pub);
    jerrComp.pub.error_exit = hipsErrorExit;

    // If libjpeg fails while reading, clean up and return false.
    if (setjmp(jerrDecomp.setjmpBuffer)) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    // If libjpeg fails while writing, clean up and return false.
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
    // This helps preserve EXIF data like orientation.
    for (int marker = 0xE0; marker <= 0xEF; ++marker) {
        jpeg_save_markers(&dinfo, marker, 0xFFFF);
    }
    jpeg_save_markers(&dinfo, JPEG_COM, 0xFFFF);

    jpeg_read_header(&dinfo, TRUE);

    // Read DCT coefficients directly instead of converting the JPEG to pixels.
    jvirt_barray_ptr* coefArrays = jpeg_read_coefficients(&dinfo);

    // Check capacity before trying to embed.
    int capacityBytes = computeCapacityBytes(&dinfo, coefArrays);
    if (capacityBytes <= 0 || static_cast<int>(message.size()) > capacityBytes) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    std::vector<uint8_t> payload = buildPayload(message);

    // Write the payload into the JPEG coefficients.
    if (!embedPayloadIntoCoefficients(&dinfo, coefArrays, payload)) {
        jpeg_destroy_decompress(&dinfo);
        jpeg_destroy_compress(&cinfo);
        std::fclose(inFile);
        std::fclose(outFile);
        return false;
    }

    jpeg_create_compress(&cinfo);
    jpeg_stdio_dest(&cinfo, outFile);

    // Copy the original JPEG structure so the output stays compatible.
    jpeg_copy_critical_parameters(&dinfo, &cinfo);
    jpeg_write_coefficients(&cinfo, coefArrays);

    // Write saved metadata markers back into the output JPEG.
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

// Internal capacity check.
// Kotlin uses this to show the user how many bytes can fit in the selected JPEG.
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

// Extracts a requested number of logical bits using repeated physical bits.
// The output is rebuilt into bytes after majority voting.
static bool extractLogicalBits(
        j_decompress_ptr dinfo,
        jvirt_barray_ptr* coefArrays,
        size_t logicalBitsNeeded,
        std::vector<uint8_t>& outBytes
) {
    const size_t totalPhysicalBitsNeeded = countPhysicalBitsNeeded(logicalBitsNeeded);

    std::vector<int> physicalBits;
    physicalBits.reserve(totalPhysicalBitsNeeded);

    // Read the physical repeated bits from usable coefficients.
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

    size_t physicalIndex = 0;

    // Convert repeated physical bits back into logical bits.
    for (size_t logicalBitIndex = 0; logicalBitIndex < logicalBitsNeeded; ++logicalBitIndex) {
        int repeatCount = getRepeatCountForLogicalBit(logicalBitIndex);

        int votedBit = majorityVoteRepeated(
                physicalBits,
                physicalIndex,
                repeatCount
        );

        physicalIndex += repeatCount;

        size_t byteIndex = logicalBitIndex / 8;
        int bitOffset = 7 - static_cast<int>(logicalBitIndex % 8);

        outBytes[byteIndex] |= static_cast<uint8_t>(votedBit << bitOffset);
    }

    return true;
}

// Main internal extraction function.
// It checks the header first, then reads the message length and message bytes.
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

    // First extract only the 8-byte header.
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

    // Check the HIPS marker.
    // If it is not present, this image is treated as not containing a message.
    if (!(headerBytes[0] == 'H' &&
          headerBytes[1] == 'I' &&
          headerBytes[2] == 'P' &&
          headerBytes[3] == 'S')) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    // Rebuild the message length from the 4 length bytes.
    uint32_t msgLen =
            (static_cast<uint32_t>(headerBytes[4]) << 24) |
            (static_cast<uint32_t>(headerBytes[5]) << 16) |
            (static_cast<uint32_t>(headerBytes[6]) << 8)  |
            (static_cast<uint32_t>(headerBytes[7]));

    // Reject impossible or unsafe lengths.
    if (msgLen > MAX_MESSAGE_BYTES) {
        jpeg_finish_decompress(&dinfo);
        jpeg_destroy_decompress(&dinfo);
        std::fclose(inFile);
        return "";
    }

    // Now extract the whole payload: header plus message.
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

    // Return only the actual message bytes, not the header.
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

// Cleans the extracted string before sending it back to Kotlin.
// JNI NewStringUTF can crash if it receives invalid UTF-8, so this keeps it safe.
static std::string cleanExtractedMessageForJava(const std::string& rawMessage) {
    std::string cleaned;

    for (unsigned char c : rawMessage) {
        // Stop if we hit a null byte.
        if (c == '\0') {
            break;
        }

        // Keep normal readable characters.
        // This matches the type of messages the app is meant to demo.
        if (c == '\n' || c == '\r' || c == '\t' || (c >= 32 && c <= 126)) {
            cleaned.push_back(static_cast<char>(c));
        } else {
            // Stop if garbage characters appear.
            break;
        }

        // Safety cap so the app never returns a huge broken string.
        if (cleaned.size() >= 100) {
            break;
        }
    }

    return cleaned;
}

// JNI function called by Kotlin to embed a message into a JPEG.
// Maps to Steganography.embedJpegMessage().
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_hips_Steganography_embedJpegMessage(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_,
        jstring outputPath_,
        jstring message_
) {
    // Convert Java strings into C strings for native processing.
    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);
    const char* outputPath = env->GetStringUTFChars(outputPath_, nullptr);
    const char* messageChars = env->GetStringUTFChars(message_, nullptr);

    bool success = embedJpegInternal(
            inputPath,
            outputPath,
            std::string(messageChars)
    );

    // Always release JNI strings after using them.
    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
    env->ReleaseStringUTFChars(message_, messageChars);

    return success ? JNI_TRUE : JNI_FALSE;
}

// JNI function called by Kotlin to check embed capacity.
// Maps to Steganography.getEmbedCapacityBytes().
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

// JNI function called by Kotlin to extract a message from a JPEG.
// Maps to Steganography.extractJpegMessage().
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

    // If extraction failed, Kotlin receives null.
    if (!ok) {
        return nullptr;
    }

    std::string cleanedMessage = cleanExtractedMessageForJava(message);

    // Empty cleaned messages are treated as failed extraction.
    if (cleanedMessage.empty()) {
        return nullptr;
    }

    return env->NewStringUTF(cleanedMessage.c_str());
}