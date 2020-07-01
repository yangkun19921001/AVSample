//
// Created by 阳坤 on 2020-06-07.
//

#ifndef AVSAMPLE_COMMON_H
#define AVSAMPLE_COMMON_H


#include <android/log.h>
#include "stdio.h"
#include <sys/time.h>

#define LOG_TAG "NATIVE_PLAYER "
#define SUCCESSED 1
#define ERROR 0

#ifdef __ANDROID__
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#elif defined(__APPLE__)	// IOS or OSX
#define LOGI(...)  printf("  ");printf(__VA_ARGS__); printf("\t -  <%s> \n", LOG_TAG);
#define LOGE(...)  printf(" Error: ");printf(__VA_ARGS__); printf("\t -  <%s> \n", LOG_TAG);
#endif
typedef unsigned char byte;

static inline long long currentTimeMills(){
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (long long)tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

#endif //AVSAMPLE_COMMON_H
