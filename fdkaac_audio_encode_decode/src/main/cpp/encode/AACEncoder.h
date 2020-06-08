//
// Created by 阳坤 on 2020-06-02.
//

#ifndef AVSAMPLE_AACENCODER_H
#define AVSAMPLE_AACENCODER_H


#include <aacenc_lib.h>
#include <zconf.h>


#define LOG_TAG encode_
#include "../common.h"


/**
 * 定义 AAC 编码格式
 * LC-AAC:应用于中高码率场景的编码 （>= 80Kbit/s）
 * HE-AAC:主要应用于中低码率场景的编码 (<= 80kit/s)
 * HE-V2-AAC: 主要应用于低码率场景的编码 (<= 48Kbit/s)
 */
typedef enum {
    LC_AAC = 2,
    HE_AAC = 5,
    LC_v2_AAC = 29,
} AACProfile;


class AACEncoder {
private:
    HANDLE_AACENCODER mAacencoder;
    AACENC_InfoStruct mEncInfo = {0};
    uint8_t *mInBuffer = 0;
    int mInBufferCursor;
    int mInputSizeFixed;
    uint8_t mAacOutbuf[20480];
    /**
     * 设置编码 Header
     */
    bool isFlagGlobalHeader = false;

    FILE *aacFile = 0;

public:
    AACEncoder();

    ~AACEncoder();

    int init(AACProfile profile, int sampleRate, int channels, int bitRate);

    int encode(Byte *pData, int dataByteSize, char **outBuffer);

    void destory();

    int fdkEncodeAudio();


    void addADTS2Packet(uint8_t *packet,int packetLen);

    void writeAACPacketToFile(uint8_t *string, int i);
};


#endif //AVSAMPLE_AACENCODER_H
