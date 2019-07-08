//
// Created by Hong on 2019/3/8.
//

#ifndef HONGPLAYER_HQUEUE_H
#define HONGPLAYER_HQUEUE_H

#include "queue"
#include "HStatus.h"
extern "C"
{
#include <libavcodec/avcodec.h>
#include "pthread.h"
};

class HQueue {
public:
    std::queue<AVPacket*> queuePacket;
    std::queue<AVFrame*> queueFrame;
    pthread_mutex_t mutexPacket;
    pthread_mutex_t mutexFrame;
    pthread_cond_t condPacket;
    pthread_cond_t condFrame;
    HStatus *status;
public:
    HQueue(HStatus *status);
    ~HQueue();
    void putPacket(AVPacket *packet);
    AVPacket* getPacket();

};


#endif //HONGPLAYER_HQUEUE_H
