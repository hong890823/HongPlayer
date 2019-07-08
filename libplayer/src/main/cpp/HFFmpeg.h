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

extern "C"{
#include <libavformat/avformat.h>
#include <libavutil/log.h>
};


class HFFmpeg {
public:
    const char *url;

    pthread_t decodeThread;
    pthread_mutex_t initMutex;
    AVFormatContext *avformatContext;

    HCallJava *callJava = NULL;
    HAudio *audio = NULL;
    HStatus *status = NULL;
public:
    HFFmpeg(HCallJava *callJava,const char *url);
    ~HFFmpeg();
    void prepareFFmpeg();
    void decodeFFmpeg();
    void getDecodeContext(AVCodecParameters *codecpar,HBaseAV *av);//获取解码器上下文
    void startPlay();
};


#endif //HONGPLAYER_HFFMPEG_H
