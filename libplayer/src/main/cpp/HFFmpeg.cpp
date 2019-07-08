//
// Created by Hong on 2019/2/26.
//

#include "HFFmpeg.h"

HFFmpeg::HFFmpeg(HCallJava *callJava,const char *url) {
    this->callJava = callJava;
    this->url = url;
    this->status = new HStatus();
    pthread_mutex_init(&initMutex,NULL);
}

HFFmpeg::~HFFmpeg() {

}

void *decode(void *data){
    HFFmpeg *ffmpeg = (HFFmpeg*)data;
    ffmpeg->decodeFFmpeg();
    pthread_exit(&ffmpeg->decodeThread);
}

void HFFmpeg::prepareFFmpeg() {
    pthread_create(&decodeThread,NULL,decode,this);
}

/**
 * 准备FFmpeg的相关信息
 * 从注册开始一直到获取到解码的上下文结束
 * */
void HFFmpeg::decodeFFmpeg() {
    pthread_mutex_init(&initMutex,NULL);
    av_register_all();
    avformat_network_init();
    avformatContext = avformat_alloc_context();
    if(avformatContext==NULL){
        if(LOG_SHOW)LOGE("context is null");
        goto error;
    }
    if(avformat_open_input(&avformatContext,url,NULL,NULL)!=0){
        if(LOG_SHOW)LOGE("open input url error:%s",url);
        goto error;
    }
    if(avformat_find_stream_info(avformatContext,NULL)<0){
        if(LOG_SHOW)LOGE("find stream info error");
    }

    for(int i=0;i<avformatContext->nb_streams;i++){
        AVCodecParameters *avCodecParameters = avformatContext->streams[i]->codecpar;
        if(AVMEDIA_TYPE_AUDIO==avCodecParameters->codec_type){//音频
            audio = new HAudio(status,callJava,avCodecParameters->sample_rate);
            getDecodeContext(avCodecParameters,audio);
        }
        if(AVMEDIA_TYPE_VIDEO==avCodecParameters->codec_type){//视频

        }
    }

    callJava->onPrepared(H_THREAD_CHILD);

    pthread_mutex_unlock(&initMutex);

    error:pthread_mutex_unlock(&initMutex);
}

/**
 * 获取解码器上下文
 * @param codecParameters 编解码的相关参数结构体
 * */
void HFFmpeg::getDecodeContext(AVCodecParameters *codecParameters,HBaseAV *av) {
    //找到编码器对应的解码器
    AVCodec *codec = avcodec_find_decoder(codecParameters->codec_id);
    //拿到解码器的上下文
    av->avCodecContext = avcodec_alloc_context3(codec);
    //把编解码的相关参数信息复制给解码器的上下文
    avcodec_parameters_to_context( av->avCodecContext,codecParameters);
    //初始化解码器的上下文
    avcodec_open2( av->avCodecContext,codec,NULL);
}

void HFFmpeg::startPlay() {
    audio->playAudio();

    while(!status->exit){
        AVPacket *packet = av_packet_alloc();
        //这步很关键，得把frame的信息存到packet中再放入队列中
        int result = av_read_frame(avformatContext,packet);
        if(result==0){
            audio->queue->putPacket(packet);
        }
    }

}



