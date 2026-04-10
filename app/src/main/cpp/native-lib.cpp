#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_hips_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_hips_Steganography_embedJpegMessage(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_,
        jstring outputPath_,
        jstring message_) {

    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);
    const char* outputPath = env->GetStringUTFChars(outputPath_, nullptr);
    const char* message = env->GetStringUTFChars(message_, nullptr);

    // Temporary stub so JNI resolves and the app builds.
    // to Replace this with real JPEG embedding logic next.
    bool success = false;

    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
    env->ReleaseStringUTFChars(message_, message);

    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_hips_Steganography_getEmbedCapacityBytes(
        JNIEnv* env,
        jobject /* thiz */,
        jstring inputPath_) {

    const char* inputPath = env->GetStringUTFChars(inputPath_, nullptr);

    // Temporary stub so JNI resolves and the app builds.
    int capacityBytes = 0;

    env->ReleaseStringUTFChars(inputPath_, inputPath);

    return capacityBytes;
}