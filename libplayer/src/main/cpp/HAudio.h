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
#include "HLog.h"

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
    int in_sample_rate = 44100;//输入采样率，单位时间从连续信号中提取离散信号的个数，单位Hz
    int out_sample_rate;//重采样之后的输出采样率(这里没有过变化，因为没用filter库)
    uint8_t *out_buffer = NULL;
    void *out_buffer_point = NULL;
    int out_nb_samples = 0;// 重采样之后输出的采样个数
    int pcm_data_size;//pcm重采样之后的数据大小
    bool isReadPacketFinished = true;

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
    //播放循环是否结束的标志
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
    void freeAVPacket(AVPacket *packet);
    void freeAVFrame(AVFrame *frame);
    void pause();
    void resume();
    void setClock(int seconds);
    void release();
};


#endif //HONGPLAYER_HAUDIO_H
