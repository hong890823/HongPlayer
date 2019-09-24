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
    jmid_video_info = jniEnv->GetMethodID(jcl,"onVideoInfo","(II)V");
    jmid_dec_mediaCodec = jniEnv->GetMethodID(jcl,"decodeMediaCodec","([BII)V");
    jmid_gl_yuv = jniEnv->GetMethodID(jcl, "setFrameData", "(II[B[B[B)V");
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
        jniEnv->SetByteArrayRegion(csd0, 0, csd_0_size, reinterpret_cast<const jbyte *>(csd_0));
        jbyteArray csd1 = jniEnv->NewByteArray(csd_1_size);
        jniEnv->SetByteArrayRegion(csd1, 0, csd_1_size, reinterpret_cast<const jbyte *>(csd_1));
        jniEnv->CallVoidMethod(jobj,jmid_init_mdeiaCodec,mimeType,width,height,csd0,csd1);
        jniEnv->DeleteLocalRef(csd0);
        jniEnv->DeleteLocalRef(csd1);
        javaVM->DetachCurrentThread();
    }else{
        jbyteArray csd0 = jniEnv->NewByteArray(csd_0_size);
        jniEnv->SetByteArrayRegion(csd0, 0, csd_0_size, reinterpret_cast<const jbyte *>(csd_0));
        jbyteArray csd1 = jniEnv->NewByteArray(csd_1_size);
        jniEnv->SetByteArrayRegion(csd1, 0, csd_1_size, reinterpret_cast<const jbyte *>(csd_1));
        jniEnv->CallVoidMethod(jobj,jmid_init_mdeiaCodec,mimeType,width,height,csd0,csd1);
        jniEnv->DeleteLocalRef(csd0);
        jniEnv->DeleteLocalRef(csd1);
    }
}

/**
 * 调用硬解码方法解析视频
 * */
void HCallJava::onDecMediaCodec(int type, int size, uint8_t *packet_data, int pts) {
    if(H_THREAD_CHILD==type){
        JNIEnv *jniEnv;
        javaVM->AttachCurrentThread(&jniEnv,0);
        jbyteArray data = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(data, 0, size, reinterpret_cast<const jbyte *>(packet_data));
        jniEnv->CallVoidMethod(jobj,jmid_dec_mediaCodec,data,size,pts);
        jniEnv->DeleteLocalRef(data);
        javaVM->DetachCurrentThread();
    }else{
        jbyteArray data = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(data, 0, size, reinterpret_cast<const jbyte *>(packet_data));
        jniEnv->CallVoidMethod(jobj,jmid_dec_mediaCodec,data,size,pts);
        jniEnv->DeleteLocalRef(data);
    }
}

//YUV420 所以Y:U:V=4:1:1
void HCallJava::onGlRenderYuv(int type, int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {
    if(H_THREAD_CHILD==type){
        JNIEnv *jniEnv;
        javaVM->AttachCurrentThread(&jniEnv,0);

        jbyteArray y = jniEnv->NewByteArray(width*height);
        jniEnv->SetByteArrayRegion(y, 0,width*height, reinterpret_cast<const jbyte *>(fy));

        jbyteArray u = jniEnv->NewByteArray(width*height/4);
        jniEnv->SetByteArrayRegion(u, 0,width*height/4, reinterpret_cast<const jbyte *>(fu));

        jbyteArray v = jniEnv->NewByteArray(width*height/4);
        jniEnv->SetByteArrayRegion(v, 0,width*height/4, reinterpret_cast<const jbyte *>(fv));

        jniEnv->CallVoidMethod(jobj,jmid_gl_yuv,width,height,y,u,v);

        jniEnv->DeleteLocalRef(y);
        jniEnv->DeleteLocalRef(u);
        jniEnv->DeleteLocalRef(v);

        javaVM->DetachCurrentThread();
    }else{
        jbyteArray y = jniEnv->NewByteArray(width*height);
        jniEnv->SetByteArrayRegion(y, 0,width*height, reinterpret_cast<const jbyte *>(fy));

        jbyteArray u = jniEnv->NewByteArray(width*height/4);
        jniEnv->SetByteArrayRegion(u, 0,width*height/4, reinterpret_cast<const jbyte *>(fu));

        jbyteArray v = jniEnv->NewByteArray(width*height/4);
        jniEnv->SetByteArrayRegion(v, 0,width*height/4, reinterpret_cast<const jbyte *>(fv));

        jniEnv->CallVoidMethod(jobj,jmid_gl_yuv,width,height,y,u,v);

        jniEnv->DeleteLocalRef(y);
        jniEnv->DeleteLocalRef(u);
        jniEnv->DeleteLocalRef(v);
    }
}

void HCallJava::onVideoInfo(int type, int currentTime, int total) {
    if(H_THREAD_CHILD==type){
        JNIEnv *jniEnv;
        javaVM->AttachCurrentThread(&jniEnv,0);
        jniEnv->CallVoidMethod(jobj,jmid_video_info,currentTime,total);
        javaVM->DetachCurrentThread();
    }else{
        jniEnv->CallVoidMethod(jobj,jmid_video_info,currentTime,total);
    }
}






