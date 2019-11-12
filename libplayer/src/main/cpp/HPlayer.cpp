#include <jni.h>
#include "HFFmpeg.h"
#include "HCallJava.h"

JavaVM *javaVM = nullptr;
HFFmpeg *hFFmpeg = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1prepare(JNIEnv *env, jobject instance, jstring url_,
                                                       jboolean isOnlyMusic) {
    jboolean *isCopy = JNI_FALSE;
    const char *url = env->GetStringUTFChars(url_,isCopy);
    auto *callJava = new HCallJava(javaVM, env, instance);
    hFFmpeg = new HFFmpeg(callJava,url);
    hFFmpeg->prepareFFmpeg();
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm,void *reserved){
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if(vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK){
        if(LOG_SHOW)LOGE("javaVM GetEnv failed");
        return result;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1start(JNIEnv *env, jobject instance) {
    if(hFFmpeg!= nullptr)hFFmpeg->startPlay();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1stop(JNIEnv *env, jobject instance) {
    if(hFFmpeg!= nullptr){
        hFFmpeg->release();
        delete(hFFmpeg);
    }

}