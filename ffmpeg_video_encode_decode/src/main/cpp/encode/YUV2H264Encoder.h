//
// Created by 阳坤 on 2020-06-21.
//

#ifndef AVSAMPLE_YUV2H264ENCODER_H
#define AVSAMPLE_YUV2H264ENCODER_H


#define X264 "libx264"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
};

#include "../../../../../nativeavplayer/src/main/cpp/common.h"
#include "pthread.h"

/**
 * 参考：
 * @see 最简单的基于FFMPEG的视频编码器（YUV编码为H.264）：https://blog.csdn.net/leixiaohua1020/article/details/25430425
 *
 * av_register_all()：注册FFmpeg所有编解码器。
 * avformat_alloc_output_context2()：初始化输出码流的AVFormatContext。
 * avio_open()：打开输出文件。
 * av_new_stream()：创建输出码流的AVStream。
 * avcodec_find_encoder()：查找编码器。
 * avcodec_open2()：打开编码器。
 * avformat_write_header()：写文件头（对于某些没有文件头的封装格式，不需要此函数。比如说MPEG2TS）。
 * avcodec_encode_video2()：编码一帧视频。即将AVFrame（存储YUV像素数据）编码为AVPacket（存储H.264等格式的码流数据）。
 * av_write_frame()：将编码后的视频码流写入文件。
 * flush_encoder()：输入的像素数据读取完成后调用此函数。用于输出编码器中剩余的AVPacket。
 * av_write_trailer()：写文件尾（对于某些没有文件头的封装格式，不需要此函数。比如说MPEG2TS）。
 */
class YUV2H264Encoder {


private:
    /**
     * 封装格式上下文
     */
    AVFormatContext *pFormatCtx = 0;
    /**
     * 输出的封装格式
     */
    AVOutputFormat *pOFmt = 0;
    /**
     * 视频流
     */
    AVStream *pVStream = 0;
    /**
     * 视频的编码器
     */
    AVCodec *pCodec = 0;
    /**
     * 编码之后的数据包
     */
    AVPacket *pPacket = 0;
    /**
     * 编解码器上下文
     */
    AVCodecContext *pCodecCtx = 0;
    /**
     * 原始 YUV 数据包
     */
    AVFrame *pFrame = 0;
    /**
     * 视频宽
     */
    int mWidht = 0;
    /**
     * 视频高
     */
    int mHeight = 0;
    /**
     * 码率
     */
    int mVideoBitRate = 0;
    /**
     * 帧率
     */
    int fps = 0;
    /**
     * Y 大小
     */
    int mY_size = 0;
    /**
     * Uv 大小
     */
    int mUV_size = 0;
    /**
     * 读 YUV 文件
     */
    FILE *pReadYUV = 0;
    /**
     * 写 H264 文件
     */
    FILE *pWriteH264 = 0;
    /**
     * 装 YUV 缓冲区
     */
    int mPictureSize;
    /**
     * 编码的 buf
     */
    uint8_t *pPicBuf = 0;
    /**
     * 编码线程 ID
     */
    pthread_t mEncode_thread_id;



private:

    int allo_video_stream();

    int allo_video_frame();


public:
    /**
     * 初始化一些数据和编码器
     * @param inYUV420sp
     * @param outH264
     * @param width
     * @param height
     * @param fps
     * @param videRitRate
     * @return
     */
    int init(const char *inYUV420sp, const char *outH264, int width, int height, int fps, int videRitRate);

    /*
     * 开始读取帧
     */
    void start();

    /**
     *将输入送入编码器
     */
    int encode(AVFrame *frame);

    /**
     * 销毁
     */
    void release();


    void readFrame();


};


#endif //AVSAMPLE_YUV2H264ENCODER_H
