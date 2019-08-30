//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HAUDIO_H
#define HONGPLAYER_HAUDIO_H

#include "HStatus.h"
#include "HCallJava.h"
#include "HBaseAV.h"
#include "HQueue.h"
#include "pthread.h"

extern "C"{
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include <libavcodec/avcodec.h>
#include <libswresample/swresample.h>
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};

class HAudio :public HBaseAV{
public:
    pthread_t audioThread;
public:
    int in_sample_rate;//输入采样率，单位时间从连续信号中提取离散信号的个数，单位Hz
    int out_sample_rate;//重采样之后的输出采样率
    int pcm_data_size;//pcm重采样之后的数据大小
    uint8_t *out_buffer = NULL;
    //因为一个packet中会包含多个frame，所以该变量用来判断一个packet中的frame是否已经读取完毕，用来决定有没有必要avcodec_send_packet
    bool isReadPacketFinished = true;
    void *out_buffer_point = NULL;

    HStatus *status;
    HCallJava *callJava;
    HQueue *queue;

    //引擎
    SLObjectItf slObjectItf;
    SLEngineItf slEngineItf;
    //混音器
    SLObjectItf outputMixObjectItf;//创建混音器的接口对象
    SLEnvironmentalReverbItf slEnvironmentalReverbItf;//混音器实例对象
    SLEnvironmentalReverbSettings slEnvironmentalReverbSettings;
    //pcm播放器
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLVolumeItf pcmPlayerVolume = NULL;
    SLMuteSoloItf  pcmPlayerMuteSolo = NULL;
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

    bool isExit = false;
    bool hasVideo = false;

public:
    HAudio(HStatus *status,HCallJava *callJava);
    HAudio(HStatus *status,HCallJava *callJava,int sample_rate);
    ~HAudio();
    void playAudio();
    void initOpenSL();
    int getCurrentSampleRateForOpenSLES(int sample_rate);
    int getPcmData(void **out_buffer_point );
};


#endif //HONGPLAYER_HAUDIO_H
