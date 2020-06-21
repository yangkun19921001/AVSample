//
// Created by 阳坤 on 2020-06-17.
//

#ifndef AVSAMPLE_AUDIOENCODER_H
#define AVSAMPLE_AUDIOENCODER_H


#define PUBLISH_BITE_RATE 64000

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
};

#include "common.h"

typedef struct PCMPacket {
    int len;
    uint8_t *data = 0;
};


/**
 * ffmpeg API 进行音频编码
 *
 * 参考雷神代码
 * @see {https://blog.csdn.net/leixiaohua1020/article/details/25430449}
 *
 * 编码流程：
 * av_register_all()：注册FFmpeg所有编解码器。
 *
 * avformat_alloc_output_context2()：初始化输出码流的AVFormatContext。
 *
 * avio_open()：打开输出文件。
 *
 * av_new_stream()：创建输出码流的AVStream。
 *
 * avcodec_find_encoder()：查找编码器。
 *
 * avcodec_open2()：打开编码器。
 *
 * avformat_write_header()：写文件头（对于某些没有文件头的封装格式，不需要此函数。比如说MPEG2TS）。
 *
 * avcodec_encode_audio2()：编码音频。即将AVFrame（存储PCM采样数据）编码为AVPacket（存储AAC，MP3等格式的码流数据）。
 *
 * av_write_frame()：将编码后的视频码流写入文件。
 *
 * av_write_trailer()：写文件尾（对于某些没有文件头的封装格式，不需要此函数。比如说MPEG2TS）。
 *
 */
class AudioEncoder {

     typedef void (*EncodeCallback)(uint8_t *data, int len);

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
     * 输出的音频码流
     */
    AVStream *pAudioStream = 0;

    /**
     * 原始帧
     */
    AVFrame *pFrame = 0;


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
     * 填充音频的 buffer
     */
    uint8_t *mSamples = 0;

    /**
     * 缓冲区大小
     */
    int mSampleSizeBuffer = 0;

    /**
     * 编码的取数据的位置
     */
    int samplesCursor = 0;

    /**
     * 总得编码时间
     */
    int totalEncodeTimeMills = 0;

    /**
     * 编码所在的时长
     */
    double duration = 0;


    /**
     * 编码回调
     */
    EncodeCallback mCallback;

private:
    /**
     * 实例化一个音频流
     * @param code_name
     * @return
     */
    int alloc_audio_stream(const char *code_name);

    /**
     * 原始 PCM 帧
     * @return
     */
    int alloc_avframe();

    /**
     * 声明一个编码完成的 AVPacket
     * @return
     */
    AVPacket *alloc_avpacket();

    /**
     * 将  AAC 写入文件
     */
    void writeAACPakcetToFile(uint8_t *data, int datalen);

    /**
     * 为 AAC 添加  adts
     * @param packet
     * @param packetLen
     */
    void addADTStoPacket(uint8_t *packet, int packetLen);

public:
    int init(const char *outAACPath, int bitRate, int channels, int sampleRate, const char *codec_name);

    int encode(PCMPacket *packet);

    void release();


    void encodePacket();


    void addEncodeCallback(EncodeCallback);


};


#endif //AVSAMPLE_AUDIOENCODER_H
