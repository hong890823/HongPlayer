//
// Created by Hong on 2019-08-26.
//

#include "HChannel.h"

HChannel::HChannel(int id, AVRational base) {
    this->channelId = id;
    this->time_base = base;
}

HChannel::HChannel(int id, AVRational base, int fps) {
    this->channelId = id;
    this->time_base = base;
    this->fps = fps;
}
