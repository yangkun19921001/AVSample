//
// Created by 阳坤 on 2020-06-21.
//

#include "YUV2H264Encoder.h"

int
YUV2H264Encoder::init(const char *inYUV420sp, const char *outH264, int width, int height, int fps, int videRitRate) {
    this->mHeight = height;
    this->mWidht = width;
    this->fps = fps;
    this->mY_size = width * height;
    this->mUV_size = mY_size / 4;
    this->mVideoBitRate = videRitRate;

    //注册函数的返回值
    int ret = 0;
    //注册 FFmpeg 所有函数
    av_register_all();
    //初始化封装格式上下文
    this->pFormatCtx = avformat_alloc_context();
    //拿到输出的上下文
    this->pOFmt = av_guess_format(NULL, outH264, NULL);
    this->pFormatCtx->oformat = this->pOFmt;

    //打开输出文件
    if (avio_open(&this->pFormatCtx->pb, outH264, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("open file error:%s\n", outH264);
        release();
        return ret = 0;
    }
    //初始化一个新的视频流，和配置一些编码参数，最后打开编码器
    ret = allo_video_stream();
    if (!ret)
        return 0;
    av_dump_format(this->pFormatCtx, 0, outH264, 1);
    ret = allo_video_frame();
    if (!ret)
        return 0;
    ret = avformat_write_header(this->pFormatCtx, NULL);

    this->pReadYUV = fopen(inYUV420sp, "rb");
    this->pWriteH264 = fopen(outH264, "wb");
    if (!pReadYUV || !pWriteH264) {
        LOGE("check the input:[%s] and output:[%s] path is correct?\n", inYUV420sp, outH264);
        return 0;
    }

    return ret == AVSTREAM_INIT_IN_WRITE_HEADER;
}


int YUV2H264Encoder::encode(AVFrame *frame) {

    AVPacket *pck = av_packet_alloc();
    int ret = avcodec_send_frame(this->pCodecCtx, frame);
    if (ret != 0) {
        return 0;
    }
    while (avcodec_receive_packet(this->pCodecCtx, pck) == 0) {
        pck->stream_index = this->pVStream->index;
        av_write_frame(this->pFormatCtx, pck);
    }

    av_packet_free(&pck);
    return 1;
}

void YUV2H264Encoder::release() {
    if (this->pVStream) {
        avcodec_close(this->pVStream->codec);
        this->pVStream = 0;
    }

    if (this->pFrame) {
        av_frame_free(&this->pFrame);
        this->pFrame = 0;
    }

    if (this->pPicBuf) {
        free(this->pPicBuf);
        this->pPicBuf = 0;
    }

    if (this->pFormatCtx && this->pFormatCtx->pb) {
        avio_close(this->pFormatCtx->pb);
        avformat_free_context(this->pFormatCtx);
        this->pFormatCtx = 0;
    }


}

int YUV2H264Encoder::allo_video_stream() {
    //创建一个新的流
    this->pVStream = avformat_new_stream(this->pFormatCtx, 0);

    this->pCodecCtx = this->pVStream->codec;
    //设置视频编码参数
//    pCodecCtx->codec_id = this->pOFmt->video_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
    pCodecCtx->width = this->mWidht;
    pCodecCtx->height = this->mHeight;
    pCodecCtx->bit_rate = this->mVideoBitRate * 1024;
    pCodecCtx->gop_size = 250;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = fps; //相当于 fps
    pCodecCtx->max_b_frames = 3;


    //下面 2 个参数的含义 参考：https://stackoverflow.com/questions/18563764/why-low-qmax-value-improve-video-quality
    //官方建议使用默认值 参考：https://sites.google.com/site/linuxencoding/x264-ffmpeg-mapping
    pCodecCtx->qmin = 10;//qmin 0 -qmax 1提供最高质量
    pCodecCtx->qmax = 51;//qmin 50和qmax 51给出最低质量

    // Set Option
    AVDictionary *param = 0;
    //H.264
    if (pCodecCtx->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "preset", "slow", 0);
        av_dict_set(&param, "tune", "zerolatency", 0);
        //av_dict_set(¶m, "profile", "main", 0);
    }
    //H.265
    if (pCodecCtx->codec_id == AV_CODEC_ID_H265) {
        av_dict_set(&param, "preset", "ultrafast", 0);
        av_dict_set(&param, "tune", "zero-latency", 0);
    }
    //根据名称找到编码器
    this->pCodec = avcodec_find_encoder_by_name(X264);

    if (!this->pCodec) {
        LOGE("not find libx264 encoder!\n");
        release();
        return 0;
    }
    int ret = 0;
    //打开编码器
    if (ret = avcodec_open2(this->pCodecCtx, this->pCodec, &param) != 0) {
        LOGE("failed to open encoder! %s\n", av_err2str(ret));
        release();
        return 0;
    }
    LOGE(" open encoder success!\n");
    return 1;
}

int YUV2H264Encoder::allo_video_frame() {
    this->pFrame = av_frame_alloc();
    this->mPictureSize = avpicture_get_size(this->pCodecCtx->pix_fmt, this->pCodecCtx->width, this->pCodecCtx->height);
    this->pPicBuf = static_cast<uint8_t *>(malloc(mPictureSize));
    //填充 buf
    avpicture_fill(reinterpret_cast<AVPicture *>(this->pFrame), this->pPicBuf, this->pCodecCtx->pix_fmt,
                   this->pCodecCtx->width, this->pCodecCtx->height);
    return 1;
}


void *_start(void *pVoid) {
    YUV2H264Encoder *yuv2H264Encoder = static_cast<YUV2H264Encoder *>(pVoid);
    yuv2H264Encoder->readFrame();
    return 0;

}

/**
 * 开启读取 YUV 的线程
 */
void YUV2H264Encoder::start() {
    pthread_create(&this->mEncode_thread_id, NULL, _start, this);

}

void YUV2H264Encoder::readFrame() {
    size_t len = 0;
    int index = 1;
    while (true) {
        len = fread(this->pPicBuf, 1, this->mY_size * 3 / 2, this->pReadYUV);
        if (len <= 0 || feof(this->pReadYUV)) {//代表读完了
            release();
            LOGE("h264 encode complete!");
            return;
        }

        this->pFrame->data[0] = this->pPicBuf;                      //Y
        this->pFrame->data[1] = this->pPicBuf + this->mY_size;      //U
        this->pFrame->data[2] = this->pPicBuf + this->mY_size * 5 / 4; //V
        //PTS
        //pFrame->pts=i;
        pFrame->pts = index * (this->pVStream->time_base.den) / ((this->pVStream->time_base.num) * this->fps);

        encode(pFrame);


        index++;


    }


}
