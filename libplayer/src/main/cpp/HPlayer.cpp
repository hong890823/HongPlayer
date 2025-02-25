#include <jni.h>
#include "HFFmpeg.h"
#include "HCallJava.h"

JavaVM *javaVM = nullptr;
HFFmpeg *hFFmpeg = nullptr;
HCallJava *callJava = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1prepare(JNIEnv *env, jobject instance, jstring url_,
                                                       jboolean isOnlyMusic) {
    jboolean *isCopy = JNI_FALSE;
    const char *url = env->GetStringUTFChars(url_,isCopy);
    callJava = new HCallJava(javaVM, env, instance);
    hFFmpeg = new HFFmpeg(callJava,url,isOnlyMusic);
    callJava->onLoad(H_THREAD_MAIN,true);
    hFFmpeg->status->isLoading = true;
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
        hFFmpeg->exitByUser = true;
        hFFmpeg->release();
        delete(hFFmpeg);
        hFFmpeg = nullptr;
    }
    if(callJava!= nullptr)
    {
        callJava->release();
    }

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1duration(JNIEnv *env, jobject instance) {
    if(hFFmpeg!= nullptr)hFFmpeg->getDuration();
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1seek(JNIEnv *env, jobject instance, jint seconds) {
    if(hFFmpeg!= nullptr)hFFmpeg->seek(seconds);
}