//
// Created by Hong on 2019/3/8.
//

#include "HQueue.h"

HQueue::HQueue(HStatus *status) {
    this->status = status;
    pthread_mutex_init(&mutexPacket,nullptr);
    pthread_mutex_init(&mutexFrame,nullptr);
    pthread_cond_init(&condPacket,nullptr);
    pthread_cond_init(&condFrame,nullptr);
}

HQueue::~HQueue() {
    this->status = nullptr;
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
    while(status!= nullptr && !status->exit){
        if(!queuePacket.empty()){
            AVPacket *avPacket = queuePacket.front();
            /*把从队列头部拿出的AVPacket信息拷贝给参数AVPacket
             * (经过测试直接拿队列头部的AVPacket传递出去也可以，
             * 但是很可能需要面临内存释放的问题，因为传递出去的
             * AVPacket没有办法在这里释放占用的内存哦
             * */
            if(av_packet_ref(packet,avPacket)==0){
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = nullptr;
            break;
        }else{
            if(!status->exit)pthread_cond_wait(&condPacket,&mutexPacket);
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

void HQueue::putFrame(AVFrame *frame) {
    pthread_mutex_lock(&mutexFrame);
    queueFrame.push(frame);
    pthread_cond_signal(&condFrame);
    pthread_mutex_unlock(&mutexFrame);
}

int HQueue::getFrame(AVFrame *frame) {
    pthread_mutex_lock(&mutexFrame);
    while(status!= nullptr && !status->exit){
        if(!queueFrame.empty()){
            AVFrame *avFrame = queueFrame.front();
            if(av_frame_ref(frame,avFrame)==0){
                queueFrame.pop();
            }
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = nullptr;
            break;
        }else{
            if(status!= nullptr && !status->exit)
                pthread_cond_wait(&condFrame,&mutexFrame);
        }
    }
    pthread_mutex_unlock(&mutexFrame);
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
    int size = 0;
    pthread_mutex_lock(&mutexFrame);
    size = queueFrame.size();
    pthread_mutex_unlock(&mutexFrame);
    return size;
}

int HQueue::clearPacketQueue() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);
    while(!queuePacket.empty()){
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_free(packet->data);
        av_free(packet->buf);
        av_free(packet->side_data);
        packet = nullptr;

    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

/**
 * clearPacketQueue和clearFrameQueue的释放方式不一样
 * 试过一样的，结果在video release的时候会报错
 * */
int HQueue::clearFrameQueue() {
    pthread_cond_signal(&condFrame);
    pthread_mutex_lock(&mutexFrame);
    while (!queueFrame.empty()){
        AVFrame *frame = queueFrame.front();
        queueFrame.pop();
        av_frame_free(&frame);
        av_free(frame);
        frame = nullptr;
    }
    pthread_mutex_unlock(&mutexFrame);
    return 0;
}

int HQueue::clearToKeyPacket() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);
    while (!queuePacket.empty()){
        AVPacket *avPacket = queuePacket.front();
        if(avPacket->flags!=AV_PKT_FLAG_KEY){
            queuePacket.pop();
            av_free(avPacket->data);
            av_free(avPacket->buf);
            av_free(avPacket->side_data);
        }else{
            break;
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int HQueue::noticeThread() {
    pthread_cond_signal(&condFrame);
    pthread_cond_signal(&condPacket);
    return 0;
}

void HQueue::release() {
    noticeThread();
    clearPacketQueue();
    clearFrameQueue();
}


