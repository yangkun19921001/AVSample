//
// Created by 阳坤 on 2020-05-21.
//

#ifndef IKAVEDIT_AVDATA_H
#define IKAVEDIT_AVDATA_H

#include <malloc.h>

#include <cstring>

enum XDataType {
    AVPACKET_TYPE = 0,//音视频包类型
    UCHAR_TYPE = 1
};

struct AVData {
    int type = 0;
    long long pts = 0;
    unsigned char *data = 0;
    unsigned char *datas[8] = {0};
    int size = 0;
    int isAudio = false;
    int width = 0;
    int height = 0;
    int format = 0;

    bool alloc(int size, const char *data = 0);

    void drop();
};


#endif //IKAVEDIT_AVDATA_H
