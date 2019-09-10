//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HVIDEO_H
#define HONGPLAYER_HVIDEO_H

#include "HStatus.h"
#include "HCallJava.h"
#include "HQueue.h"
#include "HBaseAV.h"

class HVideo :public HBaseAV{
public:
    pthread_t frameThread;
    pthread_t videoThread;

    HStatus *status;
    HCallJava *callJava;
    HQueue *queue;

    int codecType;//解码类型 0软解 1硬解

    int rate;
    bool frameRateBig = false;

public:
    HVideo(HStatus *status,HCallJava *callJava);
    ~HVideo();
    //codecType 0软解 1硬解
    void playVideo(int codecType);
    void decodeVideo();
    void freeAVPacket(AVPacket *packet);
    void freeAVFrame(AVFrame *frame);
};


#endif //HONGPLAYER_HVIDEO_H
