//
// Created by Hong on 2019/2/26.
//

#include "HFFmpeg.h"

HFFmpeg::HFFmpeg(HCallJava *callJava,const char *url, bool isOnlyMusic) {
    this->callJava = callJava;
    this->url = url;
    this->isOnlyMusic = isOnlyMusic;
    this->status = new HStatus();
    pthread_mutex_init(&initMutex,NULL);
    pthread_mutex_init(&seekMutex, nullptr);
}

HFFmpeg::~HFFmpeg() {
    pthread_mutex_destroy(&initMutex);
    pthread_mutex_destroy(&seekMutex);
}

void *decode(void *data){
    auto *ffmpeg = (HFFmpeg*)data;
    ffmpeg->decodeFFmpeg();
    pthread_exit(&ffmpeg->decodeThread);
}

void HFFmpeg::prepareFFmpeg() {
    pthread_create(&decodeThread,NULL,decode,this);
}

int avformat_interrupt_cb(void *ctx)
{
    HFFmpeg *hfFmpeg = (HFFmpeg *) ctx;
    if(hfFmpeg->status->exit)
    {
        if(LOG_SHOW)
        {
            LOGE("avformat_interrupt_cb return 1")
        }
        return AVERROR_EOF;
    }
    if(LOG_SHOW)
    {
        LOGE("avformat_interrupt_cb return 0")
    }
    return 0;
}

/**
 * 准备FFmpeg的相关信息
 * 从注册开始一直到获取到解码的上下文结束
 * */
void HFFmpeg::decodeFFmpeg() {
    pthread_mutex_init(&initMutex,nullptr);
    exit = false;
    isAvi = false;
    av_register_all();
    avformat_network_init();
    avFormatContext = avformat_alloc_context();
    if(avFormatContext== nullptr){
        if(LOG_SHOW)LOGE("context is null");
        callError(H_ERROR_CODE,const_cast<char *>("context is null"));
        pthread_mutex_unlock(&initMutex);
    }
    /*现在的avformat_open_input方法无法支持https协议，如果支持需要重新编译库。
     *现下不是重点，先放一边。
     */
    if(avformat_open_input(&avFormatContext,url,nullptr,nullptr)!=0){
        if(LOG_SHOW)LOGE("open input url error:%s",url);
        callError(H_ERROR_CODE,const_cast<char *>("open input url error:%s",url));
        pthread_mutex_unlock(&initMutex);
    }
    //总时长
    duration = static_cast<int>(avFormatContext->duration / 1000000);
    avFormatContext->interrupt_callback.callback = avformat_interrupt_cb;
    avFormatContext->interrupt_callback.opaque = this;
    if(avformat_find_stream_info(avFormatContext,nullptr)<0){
        if(LOG_SHOW)LOGE("find stream info error");
        callError(H_ERROR_CODE,const_cast<char *>("find stream info error"));
        pthread_mutex_unlock(&initMutex);
    }

    for(int i=0;i<avFormatContext->nb_streams;i++){
        AVStream *avStream = avFormatContext->streams[i];
        AVCodecParameters *avCodecParameters = avStream->codecpar;
        if(AVMEDIA_TYPE_AUDIO==avCodecParameters->codec_type){//音频
            auto *audioChannel = new HChannel(i,avStream->time_base);
            audioChannels.push_front(audioChannel);
        }
        if(AVMEDIA_TYPE_VIDEO==avCodecParameters->codec_type){//视频
            if(!isOnlyMusic){
                int num = avStream->avg_frame_rate.num;
                int den = avStream->avg_frame_rate.den;
                if(num!=0 && den!=0){
                    ///总的帧数/总的时长
                    int fps = num/den;
                    auto *videoChannel = new HChannel(i,avStream->time_base,fps);
                    videoChannels.push_front(videoChannel);
                }
            }
        }
    }

    if(!audioChannels.empty()){
        audio = new HAudio(status,callJava);
        setAudioChannel(0);
        if(audio->streamIndex>=0 && audio->streamIndex<avFormatContext->nb_streams){
            if(getDecodeContext(avFormatContext->streams[audio->streamIndex]->codecpar,audio)){
                callError(H_ERROR_CODE, const_cast<char *>("getDecodeContext audio error"));
                pthread_mutex_unlock(&initMutex);
            }
        }
    }
    if(!videoChannels.empty()){
        video = new HVideo(status,callJava,audio);
        setVideoChannel(0);
        if(video->streamIndex>=0 && video->streamIndex<avFormatContext->nb_streams){
            if(getDecodeContext(avFormatContext->streams[video->streamIndex]->codecpar,video)){
                callError(H_ERROR_CODE, const_cast<char *>("getDecodeContext video error"));
                pthread_mutex_unlock(&initMutex);
            }
        }
    }
    if(audio== nullptr && video== nullptr){
        callError(H_ERROR_CODE, const_cast<char *>("audio and video is null"));
        pthread_mutex_unlock(&initMutex);
    }
    if(audio!= nullptr){
        audio->duration = static_cast<int>(avFormatContext->duration / 1000000);
        audio->out_sample_rate = audio->in_sample_rate = audio->avCodecContext->sample_rate;
        if(video!= nullptr){
            audio->hasVideo = true;
        }
    }
    if(video!= nullptr){
        if(!callJava->isOnlySoft(H_THREAD_CHILD)){
            mimeType = getMimeType(video->avCodecContext->codec->name);
        }else{
            mimeType = -1;
        }
        if(mimeType!=-1){
            //初始化硬件解码，硬件解码的参数基本来源于解码器上下文
            callJava->onInitMediaCodec(H_THREAD_CHILD,mimeType,video->avCodecContext->width,video->avCodecContext->height
            ,video->avCodecContext->extradata_size,video->avCodecContext->extradata_size
            ,video->avCodecContext->extradata,video->avCodecContext->extradata);
        }
        video->duration = static_cast<int>(avFormatContext->duration / 1000000);
    }
    callJava->onPrepared(H_THREAD_CHILD);

    exit = true;
    pthread_mutex_unlock(&initMutex);
}

/**
 * 获取解码器上下文
 * @param codecParameters 编解码的相关参数结构体
 * */
int HFFmpeg::getDecodeContext(AVCodecParameters *codecParameters,HBaseAV *av) {
    //找到编码器对应的解码器
    AVCodec *deCodec = avcodec_find_decoder(codecParameters->codec_id);
    if(!deCodec){
        callError(H_ERROR_CODE,const_cast<char *>("avcodec_find_decoder error"));
        return -1;
    }
    //拿到解码器的上下文
    av->avCodecContext = avcodec_alloc_context3(deCodec);
    if(!av->avCodecContext){
        callError(H_ERROR_CODE,const_cast<char *>("avcodec_alloc_context3 error"));
        return -1;
    }
    //把编解码的相关参数信息复制给解码器的上下文
    if(avcodec_parameters_to_context( av->avCodecContext,codecParameters)!=0){
        callError(H_ERROR_CODE,const_cast<char *>("avcodec_parameters_to_context error"));
        return -1;
    }
    //初始化解码器的上下文
    if(avcodec_open2( av->avCodecContext,deCodec,NULL)){
        callError(H_ERROR_CODE,const_cast<char *>("avcodec_open2 error"));
        return -1;
    };
    return 0;
}

/**
 * 播放的时候是生产/消费者模式的集中体现
 * 多线程操作队列（从队列中取出播放和入队分别在不同的线程中进行）
 * */
void HFFmpeg::startPlay() {
    int count = 0;
    if(audio!= nullptr)audio->playAudio();
    if(video!= nullptr){
        if(mimeType==-1){//执行软解
            video->playVideo(0);
        }else{//执行硬解
            video->playVideo(1);
        }
    }

    ///经过测试硬解的时候添加SPS和PPS是可行的，软解的话不行
    AVBitStreamFilterContext *filterContext = nullptr;
    if(mimeType!=-1)filterContext = getH264(mimeType);

    while(!status->exit){
        if(audio!= nullptr && audio->queue->getPacketQueueSize()>100){
            av_usleep(1000*100);
            continue;
        }
        if(video!= nullptr && video->queue->getPacketQueueSize()>100){
            av_usleep(1000*100);
            continue;
        }
        AVPacket *packet = av_packet_alloc();
        //入队列和seek进度条互锁
        pthread_mutex_lock(&seekMutex);
        ///这步很关键，得把frame的信息存到packet中再放入队列中
        int result = av_read_frame(avFormatContext,packet);
        pthread_mutex_unlock(&seekMutex);
        if(result==0){
            //stream_index=streamIndex=循环取流中该流所处的索引位置也是channel的channelId
            if(audio!=nullptr && packet->stream_index==audio->streamIndex){
                count++;
//                LOGD("放入第%d个Packet进队列",count);
                audio->queue->putPacket(packet);
            }else if(video!= nullptr && packet->stream_index==video->streamIndex){
                if(filterContext!= nullptr)addSPSAndPPS(filterContext,packet);
                video->queue->putPacket(packet);
            }else{
                av_packet_free(&packet);
                av_free(packet);
                packet = nullptr;
                if((video != NULL && video->queue->getFrameQueueSize() == 0) || (audio != NULL && audio->queue->getPacketQueueSize() == 0))
                {
                    status->exit = true;
                    break;
                }
            }
        }else{
            av_packet_free(&packet);
            av_free(packet);
            packet = nullptr;
        }
    }
    if(filterContext != nullptr) {
            av_bitstream_filter_close(filterContext);
    }
    if(!exitByUser && callJava != nullptr){
        callJava->onComplete(H_THREAD_CHILD);
    }
    exit = true;
}

void HFFmpeg::setAudioChannel(int index) {
    if(audio!= nullptr){
        int channelSize = audioChannels.size();
        if(index<channelSize){
            for(int i=0;i<channelSize;i++){
                if(i==index){
                    HChannel *audioChannel = audioChannels.at(static_cast<unsigned int>(i));
                    audio->time_base = audioChannel->time_base;
                    audio->streamIndex = audioChannel->channelId;
                }
            }
        }
    }
}

void HFFmpeg::setVideoChannel(int index) {
    if(video!= nullptr){
        HChannel *videoChannel = videoChannels.at(static_cast<unsigned int>(index));
        video->streamIndex = videoChannel ->channelId;
        video->time_base = videoChannel->time_base;
        video->rate = 1000/videoChannel->fps;//fps 帧率
        video->frameRateBig = videoChannel->fps >= 60;
    }
}

int HFFmpeg::getMimeType(const char* codecName){
    if(strcmp(codecName,"h264")==0){
        return H_MIMETYPE_H264;
    }
    if(strcmp(codecName,"hevc")==0){
        return H_MIMETYPE_HEVC;
    }
    if(strcmp(codecName,"mpeg4")==0){
        isAvi = true;
        return H_MIMETYPE_MPEG4;
    }
    if(strcmp(codecName,"wmv3")==0){
        isAvi = true;
        return H_MIMETYPE_WMV3;
    }
    return -1;
}

///分离某些封装格式中的H.264
AVBitStreamFilterContext *HFFmpeg::getH264(int mimeType) {
    AVBitStreamFilterContext* filterContext = nullptr;
    if(mimeType == 1){
        filterContext =  av_bitstream_filter_init("h264_mp4toannexb");
    }
    else if(mimeType == 2){
        filterContext =  av_bitstream_filter_init("hevc_mp4toannexb");
    }
    else if(mimeType == 3){
        filterContext =  av_bitstream_filter_init("h264_mp4toannexb");
    }
    else if(mimeType == 4){
        filterContext =  av_bitstream_filter_init("h264_mp4toannexb");
    }
    return filterContext;
}

/**
 * 每个AVPacket的data添加了H.264的NALU的起始码{0,0,0,1}
 * 每个IDR帧数据前面添加了SPS和PPS
 * */
void HFFmpeg::addSPSAndPPS(AVBitStreamFilterContext *filterContext,AVPacket *packet) {
    if(filterContext != nullptr && !isAvi){
        uint8_t *data;
        av_bitstream_filter_filter(filterContext, avFormatContext->streams[video->streamIndex]->codec, nullptr, &data, &packet->size, packet->data, packet->size, 0);
        uint8_t *tdata = nullptr;
        tdata = packet->data;
        packet->data = data;
        if(tdata != nullptr){
            av_free(tdata);
        }
    }
}

int HFFmpeg::getDuration() {
    return duration;
}

void HFFmpeg::callError(int errorCode, char *errorMsg) {
    callJava->onError(H_THREAD_CHILD, errorCode, errorMsg);
    exit = true;
}

void HFFmpeg::release() {
    status->exit = true;
    pthread_mutex_lock(&initMutex);
    int sleepCount = 0;
    while(!exit)
    {
        if(sleepCount > 1000)//十秒钟还没有退出就自动强制退出
        {
            exit = true;
        }
        if(LOG_SHOW)
        {
            LOGE("wait ffmpeg  exit %d", sleepCount);
        }

        sleepCount++;
        av_usleep(1000 * 10);//暂停10毫秒
    }
    if(audio!= nullptr){
        audio->release();
        delete(audio);
        audio= nullptr;
    }
    if(video!= nullptr){
        video->release();
        delete(video);
        video = nullptr;
    }
    if(avFormatContext!= nullptr){
        avformat_close_input(&avFormatContext);
        avformat_free_context(avFormatContext);
        avFormatContext= nullptr;
    }
    if(callJava!= nullptr){
        callJava = nullptr;
    }
    pthread_mutex_unlock(&initMutex);
}

void HFFmpeg::seek(int64_t seconds) {

    if(seconds>=duration)return;
    //loading状态下无法seek
    if(status->isLoading)return;
    if(avFormatContext!= nullptr){
        status->isSeeking = true;
        pthread_mutex_lock(&seekMutex);
        int64_t rel = seconds * AV_TIME_BASE;
        avformat_seek_file(avFormatContext, -1, INT64_MIN, rel, INT64_MAX, 0);

        if(audio!= nullptr){
            audio->queue->clearPacketQueue();
            audio->setClock(0);
        }

        if(video!= nullptr){
            video->queue->clearFrameQueue();
            video->queue->clearPacketQueue();
            video->setClock(0);
        }

        status->isSeeking = false;
        pthread_mutex_unlock(&seekMutex);
    }
}

#pragma clang diagnostic pop