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
    jmethodID jmid_onlySoft;
    jmethodID jmid_onlyMusic;
    jmethodID jmid_init_mdeiaCodec;
    jmethodID jmid_video_info;
    jmethodID jmid_dec_mediaCodec;
public:
    JavaVM *javaVM;
    JNIEnv *jniEnv;
    jobject jobj;
public:
    HCallJava(JavaVM *vm,JNIEnv *env,jobject obj);
    ~HCallJava();
    void onPrepared(int type);
    void onError(int type,int errorCode,char *errorMsg);
    bool isOnlySoft(int type);
    void onInitMediaCodec(int type, int mimeType, int width, int height, int csd_0_size, int csd_1_size, uint8_t *csd_0, uint8_t *csd_1);
    void onDecMediaCodec(int type,int size,uint8_t *packet_data, int pts);
    void onVideoInfo(int type,int currentTime,int totalTime);
};


#endif //HONGPLAYER_HCALLJAVA_H
