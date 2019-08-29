//
// Created by Hong on 2019-08-26.
//

#ifndef HONGPLAYER_HCHANNEL_H
#define HONGPLAYER_HCHANNEL_H

extern "C"{
#include <libavutil/rational.h>
};

class HChannel {
public:
    int channelId = -1;
    AVRational time_base;
    int fps;
public:
    HChannel(int id,AVRational base);
    HChannel(int id,AVRational base,int fps);
};


#endif //HONGPLAYER_HCHANNEL_H
