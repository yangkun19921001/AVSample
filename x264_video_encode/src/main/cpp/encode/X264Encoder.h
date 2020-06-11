//
// Created by 阳坤 on 2020-06-08.
//

#ifndef AVSAMPLE_X264ENCODER_H
#define AVSAMPLE_X264ENCODER_H


#include <stdint.h>
#include <x264.h>
#include <malloc.h>
#include <string.h>
#include "../x264Common.h"


static enum {
    YUV420p = 1,
    YUV420sp = 2,
};

typedef struct {
    uint8_t *data = 0;
    uint32_t type = YUV420p;
} AVPacket;



/**
 * 资料参考
 * @see https://wangpengcheng.github.io/2019/04/19/libx264_learn_note/
 * @see 雷神 x264编码： https://blog.csdn.net/leixiaohua1020/article/details/42078645
 */
class X264Encoder {
protected:
    int mFps, mWidth, mHeight, mVideoBitRate, mYSize, mUVSize, mCsp/*YUV 数据格式一般是 X264_CSP_I420*/;
    /**
     * x264 编码器
     */
    x264_t *pX264Codec = 0;
    /**
     * 存储压缩编码前的像素数据。
     */
    x264_picture_t *pIc_in = 0;



public:
    X264Encoder();

    ~X264Encoder();

public:
    /**
     *
     * @param outH264Path  编码完成输出的文件
     * @param width 视频宽
     * @param height 视频高
     * @param videoBitRate 码率 可以参考：https://docs.agora.io/cn/Video/video_profile_android?platform=Android
     * @param frameRate
     */
    void init(const char *outH264Path, int width, int height, int videoBitRate, int frameRate);


    /**
     * 编码的 YUV 数据。
     * @param packet
     */
    void encode(AVPacket *packet);

    /**
     *
     * @param sps 编码的第一帧
     * @param pps 编码的第二帧
     * @param sps_len  sps 长度
     * @param pps_len  pps 长度
     */
    void sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len);


    /**
     *
     * @param type  type == NAL_SLICE_IDR 是否是关键帧
     * @param payload 编码的帧
     * @param i_payload
     * @param timestamp
     */
    void sendFrame(int type, uint8_t *payload, int i_payload, long timestamp);

    /**
     * 销毁编码器
     */
    void destory();

};


#endif //AVSAMPLE_X264ENCODER_H
