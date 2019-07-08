//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HCALLJAVA_H
#define HONGPLAYER_HCALLJAVA_H

#include <jni.h>
#include "HType.h"

class HCallJava {
    jmethodID jmid_calledOnPrepared;
    jmethodID jmid_calledOnError;
public:
    JavaVM *javaVM;
    JNIEnv *jniEnv;
    jobject jobj;
public:
    HCallJava(JavaVM *vm,JNIEnv *env,jobject obj);
    ~HCallJava();
    void onPrepared(int type);
    void onError(int type,char *errorMsg);
};


#endif //HONGPLAYER_HCALLJAVA_H
