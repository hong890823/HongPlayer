#include <jni.h>
#include "HFFmpeg.h"
#include "HCallJava.h"

JavaVM *javaVM = NULL;
HFFmpeg *hfFmpeg = NULL;

extern "C"
JNIEXPORT void JNICALL
Java_com_hong_libplayer_player_HPlayer_native_1prepare(JNIEnv *env, jobject instance, jstring url_,
                                                       jboolean isOnlyMusic) {
    jboolean *isCopy = JNI_FALSE;
    const char *url = env->GetStringUTFChars(url_,isCopy);
    HCallJava *callJava = new HCallJava(javaVM, env, instance);
    hfFmpeg = new HFFmpeg(callJava,url);
    hfFmpeg->prepareFFmpeg();
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
    hfFmpeg->startPlay();
}