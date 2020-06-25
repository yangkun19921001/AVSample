//
// Created by 阳坤 on 2020-06-23.
//

#include "Mp3Decoder.h"


FILE *outFilePCM;
int count = 0;


int Mp3Decoder::alloc_audio_stream(const char *code_name) {
    int stream_index = av_find_best_stream(this->pFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    if (stream_index == -1) {
        LOGE("find AUDIO stream error:%s\n", av_err2str(stream_index));
        return 0;
    }
    AVCodec *avCodec = avcodec_find_decoder(this->pFormatCtx->streams[stream_index]->codecpar->codec_id);
//    if (code_name)
//        avCodec = avcodec_find_decoder_by_name(code_name);

    if (!avCodec) {
        LOGI("Couldn't find a valid audio codec");
        return 0;
    }
    pCodeccCtx = avcodec_alloc_context3(avCodec);
    if (!pCodeccCtx) {
        printf("avcodec_alloc_context3 error");
        return 0;
    }
    //2. 将解码器信息 copy 到解码器上下文中
    if (avcodec_parameters_to_context(pCodeccCtx, this->pFormatCtx->streams[stream_index]->codecpar) < 0) {
        printf("avcodec_parameters_to_context error");
        return 0;
    }

    this->mChannels = pCodeccCtx->channels;
    this->mSampleRate = pCodeccCtx->sample_rate;
    this->mBitRate = pCodeccCtx->bit_rate;

    LOGE("----------------------解码信息：--------------------------------\n");
    AVCodecID avCodecID = pCodeccCtx->codec_id;
    switch (avCodecID) {
        case AV_CODEC_ID_MP3:
            LOGE("编码格式: AV_CODEC_ID_MP3");
            break;
        case AV_CODEC_ID_AAC:
            LOGE("编码格式: AV_CODEC_ID_AAC");
            break;
        default:
            LOGE("编码格式: %d", avCodec);
            break;
    }

    LOGE("通道:%d\n", mChannels);
    LOGE("采样率:%d\n", mSampleRate);
    LOGE("码率:%d kbps/s\n", mBitRate / 1000);
    LOGE("----------------------解码信息：--------------------------------\n");


    //打开解码器。
    if (avcodec_open2(this->pCodeccCtx, avCodec, NULL) < 0) {
        LOGI("Couldn't open codec");
        return 0;
    }
    return 1;

}


int AVInterruptCallBack(void *p) {
    return 0;//0，正常，1，强制退出

}

int Mp3Decoder::init(const char *inAACPath, const char *outPCMPath,
                     const char *codec_name) {
    this->mChannels = 0;
    this->mSampleRate = 0;
    this->mBitRate = 0;
    //1. 注册FFmpeg所有编解码器。
    av_register_all();
    int ret = 0;

    //2. 初始化输出码流的AVFormatContext。
    this->pFormatCtx = avformat_alloc_context();

    pFormatCtx->interrupt_callback.callback = AVInterruptCallBack;
    pFormatCtx->interrupt_callback.opaque = this;

    if ((ret = avformat_open_input(&this->pFormatCtx, inAACPath, NULL, 0)) != 0) {
        LOGE("avformat_open_input   failed : %s\n \n", av_err2str(ret), inAACPath);
        return 0;
    }

    if ((ret = avformat_find_stream_info(pFormatCtx, 0) < 0)) {
        LOGE("find stream error:%s\n", av_err2str(ret));
        return 0;
    }

    if (codec_name == NULL) {
        LOGE("codec_name:  [%s] is null !", codec_name);
        return 0;
    }

    //4. 创建输出码流的AVStream。
    ret = this->alloc_audio_stream(codec_name);
    if (ret == 0) {
        LOGE("alloc_audio_stream error");
        return 0;
    }


    outFilePCM = fopen(outPCMPath, "wb");

    this->isStart = 1;
    LOGI("init success!");
    return ret;

}

/**
 * 写入文件
 * @param data
 * @param datalen
 */
void Mp3Decoder::writePCMPakcetToFile(uint8_t *data, int datalen) {
    LOGE("aac-pcm  decode : %d", datalen);
    fwrite(data, datalen, 1, outFilePCM);
}

/**
 * 将按照编码的缓冲区的大小（@see mSampleSizeBuffer）来进行编码
 * @param packet
 * @return
 */
int Mp3Decoder::encode(AACPacket *packet) {
    if (packet == NULL) {
        LOGE("PCMPacket not init ?");
        return -1;
    }
    AVPacket *pPacket = packet->packet;
    AVFrame *encode_frame = av_frame_alloc();
    int ret = avcodec_send_packet(this->pCodeccCtx, pPacket);
    if (ret < 0) {
        LOGE("Could't send frame");
        return 0;
    }

    //需要循环获取有可能一帧编码多个 packet
    while (avcodec_receive_frame(this->pCodeccCtx, encode_frame) == 0) {
        writePCMPakcetToFile(encode_frame->data[0], encode_frame->linesize[0]);
    }
    if (packet->packet)
        av_packet_free(&packet->packet);
    av_frame_free(&encode_frame);
    free(packet);
    return 1;
}


void flush_(AVFormatContext *avFormatContext) {
    int ret = avformat_flush(avFormatContext);
    if (ret < 0) {
        LOGE("Flushing encoder failed\n");
        return;
    }
    //Write Trailer
    av_write_trailer(avFormatContext);
}

void Mp3Decoder::release() {
    this->isStart = 0;
    if (this->pFormatCtx != NULL && this->pFormatCtx->pb != NULL) {
        avio_close(this->pFormatCtx->pb);
        pFormatCtx->pb = 0;
    }

    if (this->pCodeccCtx != NULL) {
        avcodec_close(this->pCodeccCtx);
        pCodeccCtx = 0;
    }

    if (this->pFormatCtx != NULL) {
        avformat_free_context(this->pFormatCtx);
        pFormatCtx = 0;
    }

    fclose(outFilePCM);

}


void *_start(void *pVoid) {
    Mp3Decoder *decode = static_cast<Mp3Decoder *>(pVoid);
    decode->startDecoder();
    return 0;
}

void Mp3Decoder::start() {
    pthread_create(&this->id, NULL, _start, this);

}


/**
 * av_read_frame 会导致阻塞，这里展示读取 42 帧
 */
void Mp3Decoder::startDecoder() {

    while (isStart) {
        count++;
        LOGE("decode startDecoder！%d %d\n", isStart, count);
        if (count == 42) {
//            break;
        }
        AVPacket *packet = av_packet_alloc();
        AACPacket *pck = new AACPacket();
        if (av_read_frame(this->pFormatCtx, packet) != 0) {
            isStart = false;
            av_packet_free(&packet);
            free(pck);
            LOGE("decode startDecoder！error %d \n", isStart);
            break;
        }
        pck->packet = packet;
        encode(pck);
        av_usleep(1000 * 10);
    }
    LOGE("decode compeleted！");
    release();

}

