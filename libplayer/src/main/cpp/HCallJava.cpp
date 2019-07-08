//
// Created by Hong on 2019/2/28.
//

#include "HCallJava.h"

HCallJava::HCallJava(JavaVM *vm,JNIEnv *env,jobject obj) {
    this->javaVM = vm;
    this->jniEnv = env;
    this->jobj = obj;
    //全局引用才可以跨线程
    jobj = env->NewGlobalRef(jobj);
    jclass jcl = jniEnv->GetObjectClass(jobj);

    jmid_calledOnPrepared = jniEnv->GetMethodID(jcl,"calledOnPrepared","()V");
    jmid_calledOnError = jniEnv->GetMethodID(jcl,"calledOnError","(Ljava/lang/String;)V");
}

HCallJava::~HCallJava() {
    jniEnv->DeleteLocalRef(jobj);
}

void HCallJava::onPrepared(int type) {
    if(H_THREAD_MAIN==type){
        jniEnv->CallVoidMethod(jobj,jmid_calledOnPrepared);
    }else if(H_THREAD_CHILD==type){
        JNIEnv *env;
        javaVM->AttachCurrentThread(&env,0);
        env->CallVoidMethod(jobj,jmid_calledOnPrepared);
        javaVM->DetachCurrentThread();
    }
}

void HCallJava::onError(int type, char *errorMsg_) {
    if(H_THREAD_MAIN==type){
        jstring errorMsg = jniEnv->NewStringUTF(errorMsg_);
        jniEnv->CallVoidMethod(jobj,jmid_calledOnPrepared,errorMsg);
    }else if(H_THREAD_CHILD==type){
        JNIEnv *env;
        javaVM->AttachCurrentThread(&env,0);
        jstring errorMsg = env->NewStringUTF(errorMsg_);
        env->CallVoidMethod(jobj,jmid_calledOnPrepared,errorMsg);
        javaVM->DetachCurrentThread();
    }
}



