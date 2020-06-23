//
// Created by 阳坤 on 2020-06-21.
//

#include "H264_2_YUVDecoder.h"


int H264_2_YUVDecoder::allo_video_stream_info() {
    //找到视频流 id
    int ret = av_find_best_stream(this->pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, 0, 0);
    if (ret < 0) {
        LOGE("not find video stream!\n");
        return 0;
    }
    //拿到对应的流信息
    AVStream *stream = this->pFormatCtx->streams[ret];

    //找到解码器
    AVCodec *codec = avcodec_find_decoder(stream->codecpar->codec_id);
    if (!codec) {
        LOGE("not find decoder!");
        return 0;
    }
    //实例化一个编解码器上下文
    this->pCodecCtx = avcodec_alloc_context3(codec);

    //将解码参数 copy 到编解码器上下文中
    ret = avcodec_parameters_from_context(stream->codecpar, this->pCodecCtx);
    if (ret < 0) {
        LOGE("avcodec_parameters_from_context error:%s\n", av_err2str(ret));
        return 0;
    }

    this->pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

    //打印解码信息
    LOGE("-------------------解码信息-------------------\n");
    LOGE("width:%d \n", pCodecCtx->width);
    LOGE("height:%d \n", pCodecCtx->height);
    LOGE("rate:%d \n", pCodecCtx->bit_rate);
    LOGE("-------------------解码信息-------------------\n");


    //打开解码器
    if (avcodec_open2(this->pCodecCtx, codec, 0) != 0) {
        LOGE("failed open codec!");
        return 0;
    }


    return 1;
}

int H264_2_YUVDecoder::init(const char *inH264Path, const char *outYUVPath, int w, int h, int videoRate) {
    this->mWidth = w;
    this->mHeight = h;
    this->mVideoRate = videoRate;

    //1. 注册所有编解码器
    av_register_all();

    this->pFormatCtx = avformat_alloc_context();

    //2. 打开文件
    int ret = avformat_open_input(&this->pFormatCtx, inH264Path, 0, 0);
    if (ret != 0) {
        LOGE("failed open file !%s \n", av_err2str(ret));
        release();
        return 0;
    }
    //3. 看看是否有需要的流
    ret = avformat_find_stream_info(this->pFormatCtx, 0);
    if (ret < 0) {
        LOGE("not find  stream info %s\n", av_err2str(ret));
        release();
        return 0;
    }

    //4. 初始化一个流信息
    ret = allo_video_stream_info();
    if (ret == 0) {
        release();
        return 0;
    }

    //创建输出的文件
    this->pOutFile = fopen(outYUVPath, "wb");

    //可以开始解码了
    this->isStart = 1;


    LOGE("init decode success!");

    return 1;
}

int H264_2_YUVDecoder::decode(AVPacket *avPacket) {
    int ret = avcodec_send_packet(this->pCodecCtx, avPacket);
    do {
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(this->pCodecCtx, frame);
        if (ret < 0)
            return 0;
        int y_size = pCodecCtx->width * pCodecCtx->height;
        fwrite(frame->data[0], 1, y_size, this->pOutFile);    //Y
        fwrite(frame->data[1], 1, y_size / 4, this->pOutFile);  //U
        fwrite(frame->data[2], 1, y_size / 4, this->pOutFile);  //V
        av_frame_free(&frame);
    } while (ret == 0 && isStart);
    return 0;
}

void H264_2_YUVDecoder::release() {
    this->isStart = false;

    if (this->pCodecCtx) {
        avcodec_close(this->pCodecCtx);
        this->pCodecCtx = 0;
    }


    if (this->pFormatCtx) {
        avformat_free_context(this->pFormatCtx);
        this->pFormatCtx = 0;
    }
}

void *__start(void *pVoid) {
    H264_2_YUVDecoder *decoder = static_cast<H264_2_YUVDecoder *>(pVoid);
    decoder->startDecoder();
    return 0;
}


void H264_2_YUVDecoder::start() {
    pthread_create(&this->mTID, NULL, __start, this);
}

void H264_2_YUVDecoder::startDecoder() {
    int ret = 0;
    while (isStart) {
        AVPacket *pck = av_packet_alloc();
        ret = av_read_frame(this->pFormatCtx, pck);
        if (ret < 0) {
            av_packet_free(&pck);
            break;
        }
        decode(pck);
        av_packet_free(&pck);
        av_usleep(1000 * 10);
    }
    LOGE("ffmpeg H264->YUV decoder completed!");
    release();
}

