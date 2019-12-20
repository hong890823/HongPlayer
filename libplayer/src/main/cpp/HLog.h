//
// Created by Hong on 2019/2/27.
//

#ifndef HONGPLAYER_HLOG_H
#define HONGPLAYER_HLOG_H

#include <android/log.h>

#define LOG_SHOW true

#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"Hong",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"Hong",FORMAT,##__VA_ARGS__);

#endif //HONGPLAYER_HLOG_H
