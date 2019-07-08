//
// Created by Hong on 2019/3/8.
//

#include "HQueue.h"

HQueue::HQueue(HStatus *status) {
    this->status = status;
    pthread_mutex_init(&mutexPacket,NULL);
    pthread_mutex_init(&mutexFrame,NULL);
    pthread_cond_init(&condPacket,NULL);
    pthread_cond_init(&condFrame,NULL);
}

HQueue::~HQueue() {

}

/**
 * 把已经经过av_read_frame有了信息的packet放到队列中
 * */
void HQueue::putPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
}

AVPacket* HQueue::getPacket() {
    pthread_mutex_lock(&mutexPacket);
    AVPacket *packet = NULL;
    while(!status->exit){
        if(queuePacket.size()>0){
            packet = queuePacket.front();
            queuePacket.pop();
        }else{
            pthread_cond_wait(&condPacket,&mutexPacket);
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return packet;
}

