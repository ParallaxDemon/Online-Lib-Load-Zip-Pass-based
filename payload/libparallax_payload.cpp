#include <jni.h>
#include <android/log.h>

#define LOG_TAG "ParallaxPayload"

extern "C" jint JNI_OnLoad(JavaVM *vm, void *) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,
                        "JNI_OnLoad OK - libParallaxPayload.so loaded from downloaded ZIP");
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}

extern "C" jstring Java_com_Parallax_Papa_Payload_probe(JNIEnv *env, jclass) {
    return env->NewStringUTF("ParallaxPayload OK");
}

extern "C" __attribute__((constructor)) void parallax_payload_ctor() {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "ELF constructor ran");
}
