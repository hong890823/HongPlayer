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
    pthread_mutex_destroy(&mutexPacket);
    pthread_mutex_destroy(&mutexFrame);
    pthread_cond_destroy(&condPacket);
    pthread_cond_destroy(&condFrame);
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

 int HQueue::getPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
//    while(status!= nullptr && !status->exit){
    if(status!= nullptr && !status->exit){
        if(!queuePacket.empty()){
            AVPacket *avPacket = queuePacket.front();
            //把从队列头部拿出的AVPacket信息拷贝给参数AVPacket
            if(av_packet_ref(packet,avPacket)==0){
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = nullptr;
//            break;
        }else{
            if(!status->exit)pthread_cond_wait(&condPacket,&mutexPacket);
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int HQueue::getPacketQueueSize() {
    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);
    return size;
}

int HQueue::getFrameQueueSize() {
    return 0;
}

int HQueue::clearPacketQueue() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);
    while(!queuePacket.empty()){
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = nullptr;

    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int HQueue::clearFrameQueue() {
    return 0;
}

