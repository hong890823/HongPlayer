//
// Created by Hong on 2019/3/7.
//

#ifndef HONGPLAYER_HBASEAV_H
#define HONGPLAYER_HBASEAV_H

extern "C"
{
#include <libavcodec/avcodec.h>
};

class HBaseAV {
public:
    AVCodecContext *avCodecContext = NULL;
    int streamIndex;
    AVRational time_base;
    int duration;
};


#endif //HONGPLAYER_HBASEAV_H
