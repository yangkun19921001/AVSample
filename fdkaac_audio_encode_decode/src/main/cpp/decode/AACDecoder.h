//
// Created by 阳坤 on 2020-06-02.
//

#ifndef AVSAMPLE_AACDECODER_H
#define AVSAMPLE_AACDECODER_H


#define LOG_TAG decode_

#include <aacdecoder_lib.h>
#include "../common.h"


/**
 * 编解码注意细节:https://blog.csdn.net/mo4776/article/details/104054049?utm_medium=distribute.pc_relevant.none-task-blog-OPENSEARCH-1.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-OPENSEARCH-1.nonecase
 *
 *
 * 编码参考:
 *
 * @see https://github.com/jgfntu/libav/blob/master/libavcodec/libfdk-aacdec.c
 * @see https://github.com/Arcen/fdk-aac/blob/master/libAACdec/include/aacdecoder_lib.h
 *
 *
 * 在编码前，需要告诉编码器使用的封装格式，关于封装格式在前面的文件有介绍。libfdk-acc提供如上所示的枚举类型与几种封装格式对应。
 *
 *    TT_MP4_RAW 就是表示裸 AAC 码流，没有任何方式的封装
 *    TT_MP4_ADIF 和 TT_MP4_ADTS 分别是 ADIF 和 ADTS 格式
 *    TT_MP4_LATM_MCP1和TT_MP4_LATM_MCP0 是 LATM 封装格式，一个是带内传输 StreamMuxConfig，一个是带外传输 TT_MP4_LOAS 就是 LOAS
 *    在流媒体应用中使用的格式是ADTS，LATM:
 *    ADTS 对应的RTP封装标准为RFC3640
 *    LATM 对的标准为RFC6461
 *
 *    封装格式:
 *    ADIF(用于文件存储)
 *    ADTS(流媒体或文件存储)
 *    LOAS(流媒体)
 *    LATM(流媒体) - TT_MP4_LATM_MCP1
 *
 *    判断是否是  adts 格式
 *    if (packet[0] != 0xff || (packet[1] & 0xf0) != 0xf0)
*    {
 *   //不为ADTS格式
 *   }
 *   adts 帧长度
 *   packet_size = ((packet[3] & 0x03) << 11) | (packet[4] << 3) | (packet[5] >> 5);
 *
 *   判断是否是 loas 格式
 *   LOASFlag = packet[0]<<3|((packet[1]&0xe0)>>5);
*    if (LOASFlag != 0x2b7)
*    {
*    //不为LOAS格式
*    }
 *    packet_size = (packet[1]&0x1f)<<8 |packet[2];
 *
 */
#define FDK_MAX_AUDIO_FRAME_SIZE    88200      //1 second of 44.1khz 16bit audio
class AACDecoder {

private:
    /**
     * AAC 解码
     */
    HANDLE_AACDECODER mDecoder;
    /**
     * pcm 包大小
     */
    int mPckSize = 0;

    byte *mSpecInfo = 0;

    UINT mSpecInfoSize;


public:
    AACDecoder();

    ~AACDecoder();

    /**
     *  Audio_Specific_Config 参考
     *  @see https://wiki.multimedia.cx/index.php/MPEG-4_Audio#Audio_Specific_Config
     *  @see https://www.cnblogs.com/lidabo/p/7234671.html
     *
     * 如果使用该模式
     * TT_MP4_RAW 就是表示裸 AAC 码流，没有任何方式的封装
     * int audioSpecInfoSize = 2;
     * byte audioSpecInfo[2];
     * memcpy(audioSpecInfo, "\x12\x10", 2);
     *
     *
     *
     * 数据内容由高位到低位依次为：aacObjectType（5bits）,sampleRateIdx(4bits),numChannels(4bits)
     *         例如：音频编码参数为：
     *         aacObjectType:AAC_LC,对应值为2，用5bit二进制表示为00010;
     *         sampleRate:44100KHz, 对应的IDX值为4, 用4bit二进制表示为0100;
     *        numChannels:2，对应的值为2，用4bit二进制表示为0010；
     *         将它们由高位到低位串起来：0001,0010,0001,0000，
     *         则，对应的十六进制值为:0x1210
     */
    int initWithRawFormat(byte *specInfo, UINT mSpecInfoSize);

    /**
     * TT_MP4_ADTS
     * @return
     */
    int initWithADTSFormat();


    int decode(byte *pck, int len, byte **outBuffer);


    void destory();

private:
    void printAACInfo();
    int fdkDecodeAudio(INT_PCM *outBuffer,int *outSize,byte *buffer,int size);
    void initFrameSize();
};


#endif //AVSAMPLE_AACDECODER_H
