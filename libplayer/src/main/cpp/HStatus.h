//
// Created by Hong on 2019/2/28.
//

#ifndef HONGPLAYER_HSTATUS_H
#define HONGPLAYER_HSTATUS_H


class HStatus {
public:
    bool exit;
    bool pause;
    bool load;
    bool seek;
public:
    HStatus();
    ~HStatus();
};


#endif //HONGPLAYER_HSTATUS_H
