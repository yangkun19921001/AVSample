//
// Created by 阳坤 on 2020-05-21.
//

#ifndef IAVEDIT_AVDATA_H
#define IAVEDIT_AVDATA_H
#include <string.h>


extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>
#include <libavutil/time.h>
};

enum XDataType {
    AVPACKET_TYPE = 0,//音视频包类型
    UCHAR_TYPE = 1
};

struct AVData {
    int type = 0;
    long long pts = 0;
    /**
     * 音频数据
     */
    unsigned char *data = 0;
    /**
     * 视频数据
     */
    unsigned char *datas[8] = {0};
    int size = 0;
    int isAudio = false;
    int width = 0;
    int height = 0;
    int format = 0;

    bool alloc(int size, const char *data = 0);

    void drop();
};


#endif //IAVEDIT_AVDATA_H
