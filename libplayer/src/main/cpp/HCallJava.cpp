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
    jmid_calledOnError = jniEnv->GetMethodID(jcl,"calledOnError","(ILjava/lang/String;)V");
    jmid_onlySoft = jniEnv->GetMethodID(jcl, "isOnlySoft", "()Z");
    jmid_onlyMusic = jniEnv->GetMethodID(jcl,"isOnlyMusic","()Z");
    jmid_init_mdeiaCodec = jniEnv->GetMethodID(jcl,"initMediaCodec","(III[B[B)V");
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

void HCallJava::onError(int type, int errorCode,char *errorMsg_) {
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

bool HCallJava::isOnlySoft(int type) {
    bool soft = false;
    if(H_THREAD_CHILD==type){
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv,0)!=JNI_OK){

        }
        soft = jniEnv->CallBooleanMethod(jobj,jmid_onlySoft);
        javaVM->DetachCurrentThread();
    }else{
        soft = jniEnv->CallBooleanMethod(jobj,jmid_onlySoft);
    }
    return soft;
}

/**
 * 硬解初始化，注意要把申请的byte数组的内存空间进行及时的释放
 * */
void HCallJava::onInitMediaCodec(int type, int mimeType, int width, int height, int csd_0_size,
                                 int csd_1_size, uint8_t *csd_0, uint8_t *csd_1) {
    if(H_THREAD_CHILD==type){
        JNIEnv *jniEnv;
        javaVM->AttachCurrentThread(&jniEnv,0);
        jbyteArray csd0 = jniEnv->NewByteArray(csd_0_size);
        jniEnv->SetByteArrayRegion(csd0, 0, csd_0_size, reinterpret_cast<const jbyte *>(csd0));
        jbyteArray csd1 = jniEnv->NewByteArray(csd_1_size);
        jniEnv->SetByteArrayRegion(csd1, 0, csd_1_size, reinterpret_cast<const jbyte *>(csd1));
        jniEnv->CallVoidMethod(jobj,jmid_init_mdeiaCodec,mimeType,width,height,csd0,csd1);
        jniEnv->DeleteLocalRef(csd0);
        jniEnv->DeleteLocalRef(csd1);
        javaVM->DetachCurrentThread();
    }else{
        jbyteArray csd0 = jniEnv->NewByteArray(csd_0_size);
        jniEnv->SetByteArrayRegion(csd0, 0, csd_0_size, reinterpret_cast<const jbyte *>(csd0));
        jbyteArray csd1 = jniEnv->NewByteArray(csd_1_size);
        jniEnv->SetByteArrayRegion(csd1, 0, csd_1_size, reinterpret_cast<const jbyte *>(csd1));
        jniEnv->CallVoidMethod(jobj,jmid_init_mdeiaCodec,mimeType,width,height,csd0,csd1);
        jniEnv->DeleteLocalRef(csd0);
        jniEnv->DeleteLocalRef(csd1);
    }
}



