//
// Created by Hong on 2019/2/26.
//

#ifndef HONGPLAYER_HFFMPEG_H
#define HONGPLAYER_HFFMPEG_H

#include "pthread.h"
#include "HLog.h"
#include "HCallJava.h"
#include "HAudio.h"
#include "HStatus.h"
#include "HVideo.h"
#include "HChannel.h"
#include "HCommonCode.h"

extern "C"{
#include <libavformat/avformat.h>
#include <libavutil/log.h>
};


class HFFmpeg {
public:
    const char *url;

    pthread_t decodeThread;
    pthread_mutex_t initMutex;
    AVFormatContext *avFormatContext;

    HCallJava *callJava = NULL;
    HAudio *audio = NULL;
    HVideo *video = NULL;
    HStatus *status = NULL;

    bool isAvi = false;
    bool isOnlyMusic = false;
    bool exit = false;
    int mimeType = 1;

    std::deque<HChannel*> audioChannels;
    std::deque<HChannel*> videoChannels;


public:
    HFFmpeg(HCallJava *callJava,const char *url);
    ~HFFmpeg();
    void prepareFFmpeg();
    void decodeFFmpeg();
    //返回0成功，否则失败
    int getDecodeContext(AVCodecParameters *codecpar,HBaseAV *av);//获取解码器上下文
    void startPlay();
    void setAudioChannel(int index);
    void setVideoChannel(int index);
    void callError(int errorCode,char *errorMsg);
    int getMimeType(const char* codecName);
    AVBitStreamFilterContext* getH264(int mimeType);
    void addSPSAndPPS(AVBitStreamFilterContext* filterContext, AVPacket *packet);
};


#endif //HONGPLAYER_HFFMPEG_H
