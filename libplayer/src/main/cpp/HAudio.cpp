//
// Created by Hong on 2019/2/28.
//



#include "HAudio.h"

HAudio::HAudio(HStatus *status, HCallJava *callJava) {
    this->status = status;
    this->callJava = callJava;
    out_buffer = (uint8_t *) malloc(sample_rate * 2 * 2 * 2 / 3);
    this->queue = new HQueue(status);
}

HAudio::HAudio(HStatus *status, HCallJava *callJava,int sample_rate) {
    this->status = status;
    this->callJava = callJava;
    this->in_sample_rate = sample_rate;
    out_buffer = (uint8_t *) malloc(sample_rate * 2 * 2 * 2 / 3);
    this->queue = new HQueue(status);
}

HAudio::~HAudio() {

}

void *decodeAudio(void *data){
    HAudio *audio = static_cast<HAudio *>(data);
    audio->initOpenSL();
    pthread_exit(&audio->audioThread);
}

void HAudio::playAudio() {
    pthread_create(&audioThread,NULL,decodeAudio,this);
}

//线程方法回调
void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf caller, void *pContext) {
    LOGD("音频播放准备完毕，开始回调数据");
    HAudio *audio = static_cast<HAudio *>(pContext);
    audio->out_buffer_point = NULL;
    audio->pcm_data_size = audio->getPcmData(&audio->out_buffer_point);
    if(audio->out_buffer_point && audio->pcm_data_size>0){
        (*audio->pcmBufferQueue)->Enqueue(audio->pcmBufferQueue, audio->out_buffer_point,
                                          static_cast<SLuint32>(audio->pcm_data_size));
    }

}

void HAudio::initOpenSL() {
    SLresult sLresult;
    //创建SLES引擎
    slCreateEngine(&slObjectItf,0,0,0,0,0);
    (*slObjectItf)->Realize(slObjectItf,SL_BOOLEAN_FALSE);//Realize之后可以调用资源
    (*slObjectItf)->GetInterface(slObjectItf,SL_IID_ENGINE,&slEngineItf);//基本上创建啥都是通过GetInterface完成的

    //创建混音器
    const SLInterfaceID mixIds[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mixRequires[1] = {SL_BOOLEAN_FALSE};
    sLresult = (*slEngineItf)->CreateOutputMix(slEngineItf,&outputMixObjectItf,1,mixIds,mixRequires);
    (void)sLresult;
    sLresult = (*outputMixObjectItf)->Realize(outputMixObjectItf,SL_BOOLEAN_FALSE);
    (void)sLresult;
    sLresult = (*outputMixObjectItf)->GetInterface(outputMixObjectItf,SL_IID_ENVIRONMENTALREVERB,&slEnvironmentalReverbItf);
    if(SL_RESULT_SUCCESS==sLresult){
        sLresult = (*slEnvironmentalReverbItf)->SetEnvironmentalReverbProperties(slEnvironmentalReverbItf,&slEnvironmentalReverbSettings);
        (void)sLresult;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,outputMixObjectItf};
    SLDataSink audioSink = {&outputMix,0};

    // 第三步，配置PCM格式信息
    SLDataLocator_AndroidSimpleBufferQueue android_queue={SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM pcm={
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            static_cast<SLuint32>(getCurrentSampleRateForOpenSLES(in_sample_rate)),
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };
    SLDataSource slDataSource = {&android_queue, &pcm};

    //切记这里面一定要把相应的功能都加上
    const SLInterfaceID ids[4] = {SL_IID_BUFFERQUEUE,SL_IID_VOLUME,SL_IID_PLAYBACKRATE,SL_IID_MUTESOLO};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE};
    (*slEngineItf)->CreateAudioPlayer(slEngineItf,&pcmPlayerObject,&slDataSource,&audioSink,4,ids,req);
    (*pcmPlayerObject)->Realize(pcmPlayerObject,SL_BOOLEAN_FALSE);
    //初始化播放接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);
    //初始化音量接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject,SL_IID_VOLUME,&pcmPlayerVolume);
    //初始化声道接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject,SL_IID_MUTESOLO,&pcmPlayerMuteSolo);
    //初始化缓冲队列接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);
    //缓冲接口回调
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    //获取播放状态接口
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    //回调方法中播放音频数据
    pcmBufferCallBack(pcmBufferQueue,this);
}

/**
 * 返回每一帧的pcm数据大小，并且在这个过程中把重采样的pcm数据存储起来
 * */
int HAudio::getPcmData(void **out_buffer_point) {
    LOGD("开始读取音频数据");
    int count = 0;
    while(!status->exit){
        int result;
        AVPacket *packet = nullptr;
        if(queue->getPacketQueueSize()==0){
            av_usleep(1000*100);
            continue;
        }
        LOGE("123456789");
        if(isReadPacketFinished){//此时需要把一个新的packet放入到解码器中
            isReadPacketFinished = false;
            LOGE("11111111");
            packet = av_packet_alloc();
            if(queue->getPacket(packet)!=0){
                av_packet_free(&packet);
                av_free(packet);
                packet = nullptr;
                isReadPacketFinished = true;
                continue;
            }
            count++;
            LOGE("从队列中取出第%d个Packet",count);
            //把packet放到解码器中
            result = avcodec_send_packet(avCodecContext,packet);
            if(result < 0 && result != AVERROR(EAGAIN) && result != AVERROR_EOF){
                av_packet_free(&packet);
                av_free(packet);
                packet = nullptr;
                isReadPacketFinished = true;
                continue;
            }
        }

        AVFrame *frame = av_frame_alloc();
        ////从解码器中接收packet解压缩到avFrame，一个avPacket中可能有多个avFrame
        result = avcodec_receive_frame(avCodecContext,frame);
        if(result==0){
            //更正frame中的声道和声道布局信息
            if(frame->channels>0 && frame->channel_layout==0){
                frame->channel_layout = static_cast<uint64_t>(av_get_default_channel_layout(frame->channels));
            }else if(frame->channels==0 && frame->channel_layout>0){
                frame->channels = av_get_channel_layout_nb_channels(frame->channel_layout);
            }
            //重采样初始化（不能改变采样率，要改变采样率需要FFmpegFilter才可以）
            int64_t out_ch_layout = AV_CH_LAYOUT_STEREO;//（立体声）
            AVSampleFormat out_sample_format = AV_SAMPLE_FMT_S16;
            SwrContext *swrContext = swr_alloc_set_opts(
                    NULL,
                    out_ch_layout,//输出声道布局
                    out_sample_format,//输出重采样的位数
                    frame->sample_rate,//输出采样率
                    frame->channel_layout,//输入声道布局
                    static_cast<AVSampleFormat>(frame->format),//输入采样位数
                    frame->sample_rate,//输入采样率
                    0, NULL
            );
            //说明该frame重采样失败
            if(!swrContext || swr_init(swrContext)<0){
                av_frame_free(&frame);
                av_free(frame);
                frame = NULL;
                swr_free(&swrContext);
                swrContext = NULL;
                continue;
            }
            //计算转换后的sample个数 a * b / c
            dst_nb_samples = av_rescale_rnd(
                    swr_get_delay(swrContext, frame->sample_rate) + frame->nb_samples,
                    frame->sample_rate, frame->sample_rate, AV_ROUND_INF);

            //out_sample_rate就是重采样后的采样率大小，实际数据存在了out_buffer中
            out_sample_rate = swr_convert(
                    swrContext,
                    &out_buffer,//buffer定成1秒需要的内存空间就足够，真实的重采样根本不到1秒
                    dst_nb_samples,//输出采样个数（
                    (const uint8_t **) frame->data,//输入的数据
                    frame->nb_samples//输入的采样个数
            );
            int out_channels = av_get_channel_layout_nb_channels(static_cast<uint64_t>(out_ch_layout));
            pcm_data_size = out_sample_rate*out_channels*av_get_bytes_per_sample(out_sample_format);//比如44100*2*2
            //已经正确的获取了该frame的pcm数据，可以退出循环了
            av_packet_free(&packet);
            av_free(packet);
            packet = NULL;
            av_frame_free(&frame);
            av_free(frame);
            frame = NULL;
            swr_free(&swrContext);
            swrContext = NULL;

            *out_buffer_point = out_buffer;
            break;
        }else{
            isReadPacketFinished = true;
            av_frame_free(&frame);
            av_free(frame);
            frame = NULL;
            av_packet_free(&packet);
            av_free(packet);
            packet = NULL;
            continue;
        }
    }
    return pcm_data_size;
}

int HAudio::getCurrentSampleRateForOpenSLES(int sample_rate) {
    int rate = 0;
    switch (sample_rate){
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate =  SL_SAMPLINGRATE_44_1;
    }
    return rate;
}







