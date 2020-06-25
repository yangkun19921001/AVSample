//
// Created by 阳坤 on 2020-06-23.
//

#ifndef AVSAMPLE_MP3DECODER_H
#define AVSAMPLE_MP3DECODER_H


#define PUBLISH_BITE_RATE 128000

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <pthread.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/time.h"
};

#include "common.h"

typedef struct AACPacket {
    AVPacket *packet = 0;
};

class Mp3Decoder {

private:
    /**
     * 封装格式上下文
     */
    AVFormatContext *pFormatCtx = 0;
    /**
     * 编码器上下文
     */
    AVCodecContext *pCodeccCtx = 0;

    /**
     * 采样率
     */
    int mSampleRate = 0;
    /**
     * 码率
     */
    int mBitRate = 0;
    /**
     * 通道
     */
    int mChannels = 0;

    /**
     * 是否开始
     */
    int isStart = 0;

    pthread_t id;

private:
    /**
     * 实例化一个音频流
     * @param code_name
     * @return
     */
    int alloc_audio_stream(const char *code_name);


    /**
     * 将  PCM 写入文件
     */
    void writePCMPakcetToFile(uint8_t *data, int datalen);



public:
    int init(const char *inAACPath, const char *outPCMPath,
             const char *codec_name);

    int encode(AACPacket *packet);

    void release();


    void start();

    void startDecoder();
};



#endif //AVSAMPLE_MP3DECODER_H
