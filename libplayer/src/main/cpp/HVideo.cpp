//
// Created by Hong on 2019/2/28.
//

#include "HVideo.h"

HVideo::HVideo(HStatus *status, HCallJava *callJava) {
    this->status = status;
    this->callJava = callJava;
    this->queue = new HQueue(status);
}

HVideo::~HVideo() {

}

void *frameInQueue(void *data){
    auto *video = (HVideo *)data;
    while(!video->status->exit){
        //设置frame queue的缓冲区
        if(video->queue->getFrameQueueSize()>20){
            continue;
        }
        //如果packet queue为空则等待
        if(video->queue->getPacketQueueSize()==0){
            continue;
        }
        AVPacket *packet = av_packet_alloc();
        if(video->queue->getPacket(packet)!=0){
            video->freeAVPacket(packet);
            continue;
        }
        int result = avcodec_send_packet(video->avCodecContext,packet);
        if(result<0 && result!=AVERROR(EAGAIN) && result!=AVERROR_EOF){
            video->freeAVPacket(packet);
            continue;
        }
        AVFrame *frame = av_frame_alloc();
        result = avcodec_receive_frame(video->avCodecContext,frame);
        if(result<0 && result!=AVERROR_EOF){
            video->freeAVPacket(packet);
            video->freeAVFrame(frame);
            continue;
        }
        video->queue->putFrame(frame);
        video->freeAVPacket(packet);
    }
    pthread_exit(&video->frameThread);
}

void *decodeVideoT(void *data){
    auto *video = (HVideo*)data;
    video->decodeVideo();
    pthread_exit(&video->videoThread);
}

void HVideo::decodeVideo() {
    while(status!= nullptr && !status->exit){
        if(queue->getPacketQueueSize()==0){
            continue;
        }

    }
}

/**
 * 0软解 1硬解
 * */
void HVideo::playVideo(int codecType) {
    this->codecType = codecType;
    if(codecType==0){
        pthread_create(&frameThread, nullptr,frameInQueue,this);
    }
    pthread_create(&videoThread, nullptr,decodeVideoT,this);

}

void HVideo::freeAVPacket(AVPacket *packet) {
    av_packet_free(&packet);
    av_free(packet);
    packet = nullptr;
}

void HVideo::freeAVFrame(AVFrame *frame) {
    av_frame_free(&frame);
    av_free(frame);
    frame = nullptr;
}

