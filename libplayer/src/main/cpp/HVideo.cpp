//
// Created by Hong on 2019/2/28.
//

#include "HVideo.h"

HVideo::HVideo(HStatus *status, HCallJava *callJava,HAudio *audio) {
    streamIndex = -1;
    clock = 0;
    this->status = status;
    this->callJava = callJava;
    this->audio = audio;
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
            video->freeAVFrame(frame);
            video->freeAVPacket(packet);
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
    ///这里释放内存不要用freeAVPacket和freeAVFrame里面的方法，会立马报错退出程序
    while(status!= nullptr && !status->exit){
        if(queue->getPacketQueueSize()==0){
            continue;
        }
        ///硬解操作的是AVPacket
        if(codecType==1){
            AVPacket *packet = av_packet_alloc();
            if(queue->getPacket(packet)!=0){
                av_free(packet->data);
                av_free(packet->buf);
                av_free(packet->side_data);
                continue;
            }
            ///音视频同步以及丢帧策略
            //当前packet的展示时间
            double time = packet->pts * av_q2d(time_base);
            //当前packet的解码时间
            if(time<0)time = packet->dts * av_q2d(time_base);
            if(time<clock)time = clock;
            clock = time;
            double diff = 0;
            if(audio!= nullptr)diff = audio->clock-clock;
            playCount++;
            if(playCount>500)playCount = 0;
            if(diff>=0.5){//如果视频比音频慢
                if(frameRateBig){
                    if(playCount%3==0 && packet->flags!=AV_PKT_FLAG_KEY){
                        av_free(packet->data);
                        av_free(packet->buf);
                        av_free(packet->side_data);
                        continue;
                    }
                }else{
                    av_free(packet->data);
                    av_free(packet->buf);
                    av_free(packet->side_data);
                    continue;
                }
            }
            delayTime = getDelayTime(diff);
            av_usleep(static_cast<unsigned int>(delayTime * 1000));
//            LOGE("硬解视频队列中还有%d帧数据",queue->getPacketQueueSize());
            //todo 视频播放进度以及硬解码开始
            callJava->onVideoInfo(H_THREAD_CHILD, static_cast<int>(clock), duration);
            callJava->onDecMediaCodec(H_THREAD_CHILD,packet->size,packet->data,0);
            av_free(packet->data);
            av_free(packet->buf);
            av_free(packet->side_data);
            ///软解操作的是AVFrame
        }else if(codecType==0){
            AVFrame *frame = av_frame_alloc();
            if(queue->getFrame(frame)!=0){
                av_frame_free(&frame);
                av_free(frame);
                frame = nullptr;
                continue;
            }
            if((framePts = av_frame_get_best_effort_timestamp(frame))==AV_NOPTS_VALUE){
                framePts = 0;
            }
            framePts *= av_q2d(time_base);
            clock = synchronize(frame,framePts);
            double diff = 0;
            if(audio!= nullptr)diff = audio->clock-clock;
            delayTime = getDelayTime(diff);
            if(diff>=0.8){
                freeAVFrame(frame);
                continue;
            }
            playCount++;
            if(playCount>500)playCount = 0;
            if(diff>=0.5){
                if(frameRateBig){
                    if(playCount%3 == 0){
                        freeAVFrame(frame);
                        queue->clearToKeyPacket();
                        continue;
                    }
                }else{
                    freeAVFrame(frame);
                    queue->clearToKeyPacket();
                    continue;
                }
            }
            av_usleep(static_cast<unsigned int>(delayTime * 1000));
            if(frame->format==AV_PIX_FMT_YUV420P){
                callJava->onGlRenderYuv(H_THREAD_CHILD, frame->linesize[0], frame->height, frame->data[0], frame->data[1], frame->data[2]);
                  //这两种方法都可以
//                callJava->onGlRenderYuv(H_THREAD_CHILD, avCodecContext->width, avCodecContext->height, frame->data[0], frame->data[1], frame->data[2]);
            }else{
                ///非YUV420转到YUV420
                AVFrame *frameYUV420 = av_frame_alloc();
                int num = av_image_get_buffer_size(AV_PIX_FMT_YUV420P,avCodecContext->width,avCodecContext->height,1);
                auto *buffer = static_cast<uint8_t *>(av_malloc(num * sizeof(uint8_t)));
                av_image_fill_arrays(frameYUV420->data,frameYUV420->linesize,buffer,AV_PIX_FMT_YUV420P,avCodecContext->width,avCodecContext->height,1);
                SwsContext *sws_ctx = sws_getContext(avCodecContext->width,avCodecContext->height,avCodecContext->pix_fmt,avCodecContext->width,avCodecContext->height,AV_PIX_FMT_YUV420P,SWS_BICUBIC,
                               nullptr, nullptr, nullptr);
                if(!sws_ctx){
                    av_frame_free(&frameYUV420);
                    av_free(frameYUV420);
                    av_free(buffer);
                    continue;
                }
                //实际转换YUV420P
                sws_scale(sws_ctx,frame->data,frame->linesize,0,frame->height,frameYUV420->data,frameYUV420->linesize);
                //渲染
                callJava->onGlRenderYuv(H_THREAD_CHILD, avCodecContext->width, avCodecContext->height, frameYUV420->data[0], frameYUV420->data[1], frameYUV420->data[2]);
            }
            freeAVFrame(frame);
        }
    }
}

double HVideo::getDelayTime(double diff) {
    if(diff>0.003){
        delayTime = delayTime/3*2;
        if(delayTime<rate/2){
            delayTime = rate/3*2;
        }else if(delayTime>rate*2){
            delayTime = rate*2;
        }
    }
    else if(diff<-0.003){
        delayTime = delayTime*3/2;
        if(delayTime<rate/2){
            delayTime = rate/3*2;
        }else if(delayTime>rate/2){
            delayTime = rate*2;
        }
    }
    else if(diff==0){
        delayTime = rate;
    }
    if(diff>1.0){
        delayTime = 0;
    }
    if(diff<-1.0){
        delayTime = rate*2;
    }
    if(fabs(diff)>10){
        delayTime = rate;
    }
    return delayTime;
}

double HVideo::synchronize(AVFrame *srcFrame,double pts) {
    double frame_delay;
    if(pts!=0){
        video_clock = pts;// Get pts,then set video clock to it
    }else{
        pts = video_clock;// Don't get pts,set it to video clock
    }
    frame_delay = av_q2d(time_base);
    frame_delay += srcFrame->repeat_pict * (frame_delay * 0.5);

    video_clock += frame_delay;
    return pts;
}

/**
 * 0软解 1硬解
 * */
void HVideo::playVideo(int codecType) {
    this->codecType = codecType;
    if(codecType==0){
        //经过测试这种先把AFrame放入队列再重队列中取出的方式，再从队列中取出的时候会报错，暂不知原因
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






