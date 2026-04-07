#include <jni.h>
#include <string>
#include <vector>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_hips_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

// Helper to extract the n-th bit of a byte array
int getBit(const std::vector<uint8_t>& data, int bitIndex) {
    int byteIndex = bitIndex / 8;
    int bitOffset = bitIndex % 8;
    return (data[byteIndex] >> bitOffset) & 1;
}

// Helper to set the n-th bit of a byte array
void setBit(std::vector<uint8_t>& data, int bitIndex, int bitValue) {
    int byteIndex = bitIndex / 8;
    int bitOffset = bitIndex % 8;
    if (bitValue) {
        data[byteIndex] |= (1 << bitOffset);
    } else {
        data[byteIndex] &= ~(1 << bitOffset);
    }
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_hips_Steganography_encodeMessage(JNIEnv *env, jobject thiz, jintArray pixels,
                                                  jstring message) {
    jsize numPixels = env->GetArrayLength(pixels);
    jint *pixelData = env->GetIntArrayElements(pixels, nullptr);

    const char *msgChars = env->GetStringUTFChars(message, nullptr);
    jsize msgLen = env->GetStringUTFLength(message);

    // We need 32 bits for the length + msgLen * 8 bits for the message
    int totalBitsNeeded = 32 + msgLen * 8;

    if (totalBitsNeeded > numPixels) {
        // Not enough pixels to hide the message
        env->ReleaseStringUTFChars(message, msgChars);
        env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);
        return nullptr;
    }

    // Create a byte array of the message including its length prefix
    std::vector<uint8_t> dataToHide(4 + msgLen);
    dataToHide[0] = (msgLen >> 24) & 0xFF;
    dataToHide[1] = (msgLen >> 16) & 0xFF;
    dataToHide[2] = (msgLen >> 8) & 0xFF;
    dataToHide[3] = msgLen & 0xFF;
    for (int i = 0; i < msgLen; ++i) {
        dataToHide[4 + i] = msgChars[i];
    }

    // Create a new array for the encoded pixels
    jintArray result = env->NewIntArray(numPixels);
    jint *resultData = env->GetIntArrayElements(result, nullptr);

    for (int i = 0; i < numPixels; ++i) {
        jint pixel = pixelData[i];
        if (i < totalBitsNeeded) {
            int bit = getBit(dataToHide, i);
            // Embed the message bit into the pixel
            pixel = (pixel & 0xFFFFFFFE) | bit;
        }
        resultData[i] = pixel;
    }

    env->ReleaseStringUTFChars(message, msgChars);
    env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);
    env->ReleaseIntArrayElements(result, resultData, 0);

    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_hips_Steganography_decodeMessage(JNIEnv *env, jobject thiz, jintArray pixels) {
    jsize numPixels = env->GetArrayLength(pixels);
    jint *pixelData = env->GetIntArrayElements(pixels, nullptr);

    if (numPixels < 32) {
        env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);
        return nullptr;
    }

    // Read the first 32 bits to get the message length
    std::vector<uint8_t> lengthBytes(4, 0);
    for (int i = 0; i < 32; ++i) {
        int bit = pixelData[i] & 1;
        setBit(lengthBytes, i, bit);
    }

    int msgLen = (lengthBytes[0] << 24) | (lengthBytes[1] << 16) | (lengthBytes[2] << 8) | lengthBytes[3];

    // Sanity check for message length
    if (msgLen < 0 || msgLen > (numPixels - 32) / 8) {
        env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);
        return nullptr;
    }

    std::vector<uint8_t> msgBytes(msgLen, 0);
    for (int i = 0; i < msgLen * 8; ++i) {
        int bit = pixelData[32 + i] & 1;
        setBit(msgBytes, i, bit);
    }

    env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);

    // Convert to jstring
    std::string decodedStr(msgBytes.begin(), msgBytes.end());
    return env->NewStringUTF(decodedStr.c_str());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_hips_Steganography_encodeMessageDCT(JNIEnv *env, jobject thiz, jbyteArray jpeg_bytes, jstring message) {
    // TODO: Implement DCT embedding using libjpeg-turbo.
    // 1. Decompress JPEG headers and read DCT coefficients.
    // 2. Convert message to binary format.
    // 3. Implement pre-compression embedding logic (modify DCT coefficients).
    // 4. Compress back to JPEG bytes.
    
    // For now, return the original bytes as a placeholder.
    jsize len = env->GetArrayLength(jpeg_bytes);
    jbyteArray result = env->NewByteArray(len);
    jbyte *bytes = env->GetByteArrayElements(jpeg_bytes, nullptr);
    env->SetByteArrayRegion(result, 0, len, bytes);
    env->ReleaseByteArrayElements(jpeg_bytes, bytes, JNI_ABORT);
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_hips_Steganography_decodeMessageDCT(JNIEnv *env, jobject thiz, jbyteArray jpeg_bytes) {
    // TODO: Implement DCT extraction using libjpeg-turbo.
    // 1. Decompress JPEG headers and read DCT coefficients.
    // 2. Extract binary message from modified DCT coefficients.
    // 3. Convert binary back to string.
    
    // For now, return a placeholder string.
    std::string placeholder = "DCT extraction not yet implemented";
    return env->NewStringUTF(placeholder.c_str());
}
