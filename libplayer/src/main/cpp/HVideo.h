//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HVIDEO_H
#define HONGPLAYER_HVIDEO_H

#include "HStatus.h"
#include "HCallJava.h"
#include "HQueue.h"
#include "HBaseAV.h"
#include "HAudio.h"

extern "C"
{
#include <libavutil/time.h>
};

class HVideo :public HBaseAV{
public:
    pthread_t frameThread;
    pthread_t videoThread;

    HStatus *status;
    HCallJava *callJava;
    HQueue *queue;
    HAudio *audio;

    int codecType;//解码类型 0软解 1硬解

    int rate;
    bool frameRateBig = false;
    int playCount = -1;
    double delayTime = 0;
    //和软件处理AVFrame相关的音视频同步以及丢帧
    double framePts = 0;
    double video_clock = 0;

public:
    HVideo(HStatus *status,HCallJava *callJava,HAudio *audio);
    ~HVideo();
    //codecType 0软解 1硬解
    void playVideo(int codecType);
    void decodeVideo();
    double getDelayTime(double diff);
    double synchronize(AVFrame *srcFrame,double pts);
    void freeAVPacket(AVPacket *packet);
    void freeAVFrame(AVFrame *frame);
};


#endif //HONGPLAYER_HVIDEO_H
