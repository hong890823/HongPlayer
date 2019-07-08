//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HVIDEO_H
#define HONGPLAYER_HVIDEO_H

#include "HStatus.h"
#include "HCallJava.h"
#include "HBaseAV.h"

class HVideo :public HBaseAV{
public:
    HStatus *status;
    HCallJava *callJava;

public:
    HVideo(HStatus *status,HCallJava *callJava);
    ~HVideo();
};


#endif //HONGPLAYER_HVIDEO_H
