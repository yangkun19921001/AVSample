//

#include "AudioEncoder.h"
//
// Created by 阳坤 on 2020-06-17.

FILE *outFile;


void AudioEncoder::addADTStoPacket(uint8_t *packet, int packetLen) {
    int profile = 2;//2 : LC; 5 : HE-AAC; 29: HEV2
    int freqIdx = 4; // 48KHz
    int chanCfg = 1; // Mono

    // fill in ADTS data
    packet[0] = (unsigned char) 0xFF;
    packet[1] = (unsigned char) 0xF1;
    packet[2] = (unsigned char) 0x5c;//(unsigned char) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
    packet[3] = (unsigned char) (((chanCfg & 3) << 6) + (packetLen >> 11));
    packet[4] = (unsigned char) ((packetLen & 0x7FF) >> 3);
    packet[5] = (unsigned char) (((packetLen & 7) << 5) + 0x1F);
    packet[6] = (unsigned char) 0xFC;
}

int AudioEncoder::alloc_audio_stream(const char *code_name) {
    //2 byte 为一个采样
    AVSampleFormat sampleFormat = AV_SAMPLE_FMT_S16;

    //创建一个新的流
    this->pAudioStream = avformat_new_stream(this->pFormatCtx, 0);
    this->pAudioStream->id = 1;
    this->pCodeccCtx = this->pAudioStream->codec;
    this->pCodeccCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    this->pCodeccCtx->sample_rate = this->mSampleRate;
    if (this->mBitRate > 0) {
        this->pCodeccCtx->bit_rate = this->mBitRate;
    } else {
        this->pCodeccCtx->bit_rate = PUBLISH_BITE_RATE;
    }
    this->pCodeccCtx->sample_fmt = sampleFormat;
    LOGI("audioChannels is %d", mChannels);
    this->pCodeccCtx->channel_layout = this->mChannels == 1 ? AV_CH_LAYOUT_MONO : AV_CH_LAYOUT_STEREO;
    this->pCodeccCtx->channels = av_get_channel_layout_nb_channels(this->pCodeccCtx->channel_layout);
    /** FF_PROFILE_AAC_LOW;FF_PROFILE_AAC_HE;FF_PROFILE_AAC_HE_V2 **/
    this->pCodeccCtx->profile = FF_PROFILE_AAC_HE;
    LOGI("avCodecContext->channels is %d", this->pCodeccCtx->channels);
    this->pCodeccCtx->flags |= CODEC_FLAG_GLOBAL_HEADER;


    //找到编码器
    //也可以通过如下 API 寻找
    //avcodec_find_encoder(AV_CODEC_ID_MP3);
    AVCodec *avCodec = avcodec_find_encoder_by_name(code_name);

    if (!avCodec) {
        LOGI("Couldn't find a valid audio codec");
        return 0;
    }
    this->pCodeccCtx->codec_id = avCodec->id;
    if (avCodec->sample_fmts) {
        /* check if the prefered sample format for this codec is supported.
         * this is because, depending on the version of libav, and with the whole ffmpeg/libav fork situation,
         * you have various implementations around. float samples in particular are not always supported.
         */
        const enum AVSampleFormat *p = avCodec->sample_fmts;
        for (; *p != -1; p++) {
            if (*p == this->pAudioStream->codec->sample_fmt)
                break;
        }
        if (*p == -1) {
            LOGI("sample format incompatible with codec. Defaulting to a format known to work.........");
            /* sample format incompatible with codec. Defaulting to a format known to work */
            this->pCodeccCtx->sample_fmt = avCodec->sample_fmts[0];
        }
    }

    if (avCodec->supported_samplerates) {
        const int *p = avCodec->supported_samplerates;
        int best = 0;
        int best_dist = INT_MAX;
        for (; *p; p++) {
            int dist = abs(this->pAudioStream->codec->sample_rate - *p);
            if (dist < best_dist) {
                best_dist = dist;
                best = *p;
            }
        }
        /* best is the closest supported sample rate (same as selected if best_dist == 0) */
        this->pCodeccCtx->sample_rate = best;
    }


    //打开编码器。
    if (avcodec_open2(this->pCodeccCtx, avCodec, NULL) < 0) {
        LOGI("Couldn't open codec");
        return 0;
    }

    this->pCodeccCtx->time_base.num = 1;
    this->pCodeccCtx->time_base.den = this->pCodeccCtx->sample_rate;
    this->pCodeccCtx->frame_size = 1024;
    return 1;

}


int AudioEncoder::alloc_avframe() {
    int ret = 0;
    AVSampleFormat sampleFormat = AV_SAMPLE_FMT_S16;

    pFrame = av_frame_alloc();
    //帧大小
    pFrame->nb_samples = this->pCodeccCtx->frame_size;
    //采样格式为 2 byte
    pFrame->format = sampleFormat;
    //通道布局
    pFrame->channel_layout = this->mChannels == 1 ? AV_CH_LAYOUT_MONO : AV_CH_LAYOUT_STEREO;
    //采样率
    pFrame->sample_rate = this->mSampleRate;

    //获取音频缓冲区大小
    ret = av_samples_get_buffer_size(NULL,
                                     av_get_channel_layout_nb_channels(pFrame->channel_layout),
                                     pFrame->nb_samples,
                                     sampleFormat, 0);
    if (ret <= 0) {
        LOGE("av_samples_get_buffer_size error！");
        return 0;
    }

    mSampleSizeBuffer = ret;

    mSamples = (uint8_t *) av_malloc(mSampleSizeBuffer);

    samplesCursor = 0;

    ret = avcodec_fill_audio_frame(pFrame,
                                   av_get_channel_layout_nb_channels(pFrame->channel_layout),
                                   sampleFormat,
                                   mSamples,
                                   mSampleSizeBuffer,
                                   0);

    return ret >= 0;
}


AVPacket *AudioEncoder::alloc_avpacket() {
    //用于解码之后的数据结构体
    AVPacket *pPacket = av_packet_alloc();
    pPacket->duration = (int) AV_NOPTS_VALUE;
    pPacket->pts = pPacket->dts = 0;
    pPacket->stream_index = 0;
    pPacket->size = mSampleSizeBuffer;
    pPacket->data = mSamples;
    return pPacket;
}


int AudioEncoder::init(const char *outAACPath, int publishBitRate, int audioChannels, int audioSampleRate,
                       const char *codec_name) {
    this->mChannels = audioChannels;
    this->mSampleRate = audioSampleRate;
    this->mBitRate = publishBitRate;
    this->samplesCursor = 0;
    this->mCallback = NULL;
    //1. 注册FFmpeg所有编解码器。
    avcodec_register_all();
    av_register_all();


    //2. 初始化输出码流的AVFormatContext。
    this->pFormatCtx = avformat_alloc_context();

    int ret = 0;
    if ((ret = avformat_alloc_output_context2(&this->pFormatCtx, NULL, NULL, outAACPath)) != 0) {
        LOGE("avFormatContext   alloc   failed : %s", av_err2str(ret));
        return 0;
    }

    //3. 打开输出文件。
    if (avio_open(&this->pFormatCtx->pb, outAACPath, AVIO_FLAG_WRITE) < 0) {
        LOGE("Failed to open outout file! :%s\n", outAACPath);
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


    outFile = fopen("sdcard/avsample/ffmpeg_audio_encode.aac", "wb");


    av_dump_format(pFormatCtx, 0, outAACPath, 1);

    //5. 写文件头（对于某些没有文件头的封装格式，不需要此函数。比如说MPEG2TS）。
    if (avformat_write_header(this->pFormatCtx, NULL) != 0) {
        LOGI("Could not write header\n");
        return -1;
    }

    //初始化 PCM 存储空间
    ret = this->alloc_avframe();


    LOGI("init success!");
    return ret;

}


void AudioEncoder::writeAACPakcetToFile(uint8_t *data, int datalen) {
    LOGI("After One Encode Size is : %d", datalen);
    uint8_t *buffer = new uint8_t[datalen + 7];
    memset(buffer, 0, datalen + 7);
    memcpy(buffer + 7, data, datalen);
    addADTStoPacket(buffer, datalen + 7);
    fwrite(buffer, sizeof(uint8_t), datalen + 7, outFile);
    delete[] buffer;
}

/**
 * 将按照编码的缓冲区的大小（@see mSampleSizeBuffer）来进行编码
 * @param packet
 * @return
 */
int AudioEncoder::encode(PCMPacket *packet) {
    if (packet == NULL) {
        LOGE("PCMPacket not init ?");
        return -1;
    }
    int bufferCursor = 0;
    int bufferSize = packet->len;
    while (bufferSize >= (mSampleSizeBuffer - samplesCursor)) {
        int cpySize = mSampleSizeBuffer - samplesCursor;
        memcpy(mSamples + samplesCursor, packet->data + bufferCursor, cpySize);
        bufferCursor += cpySize;
        bufferSize -= cpySize;
        uint64_t beginEncodeTimeMills = currentTimeMills();
        this->encodePacket();
        totalEncodeTimeMills += (currentTimeMills() - beginEncodeTimeMills);
        samplesCursor = 0;
    }
    if (bufferSize > 0) {
        memcpy(mSamples + samplesCursor, packet->data + bufferCursor, bufferSize);
        samplesCursor += bufferSize;
    }
    return 1;
}


/**
 * ps:内部自己手动也写入了文件目录："sdcard/avsample/ffmpeg_audio_encode.aac"
 */
void AudioEncoder::encodePacket() {
    AVPacket *pPacket = alloc_avpacket();
    AVFrame *encode_frame = this->pFrame;
    int ret = avcodec_send_frame(this->pCodeccCtx, encode_frame);
    if (ret < 0) {
        LOGE("Could't send frame");
        return;
    }

    //需要循环获取有可能一帧编码多个 packet
    while (avcodec_receive_packet(this->pCodeccCtx, pPacket) == 0) {
        writeAACPakcetToFile(pPacket->data, pPacket->size);

        //将编码完成的数据回调回去
        if (mCallback)
            mCallback(pPacket->data, pPacket->size);

        if (this->pCodeccCtx->coded_frame && this->pCodeccCtx->coded_frame->pts != AV_NOPTS_VALUE)
            pPacket->pts = av_rescale_q(this->pCodeccCtx->coded_frame->pts, this->pCodeccCtx->time_base,
                                        this->pAudioStream->time_base);
        pPacket->flags |= AV_PKT_FLAG_KEY;
        this->duration = pPacket->pts * av_q2d(this->pAudioStream->time_base);
        ret = av_interleaved_write_frame(pFormatCtx, pPacket);
    }
    av_packet_free(&pPacket);
}


void flush(AVFormatContext *avFormatContext) {
    int ret = avformat_flush(avFormatContext);
    if (ret < 0) {
        LOGE("Flushing encoder failed\n");
        return;
    }
    //Write Trailer
    av_write_trailer(avFormatContext);
}

void AudioEncoder::release() {
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

    if (pFrame != NULL) {
        av_free(pFrame);
        pFrame = 0;
    }

    if (this->mSamples != NULL) {
        free(this->mSamples);
    }

    if (this->pFrame != NULL) {
        av_frame_free(&this->pFrame);
    }

    fclose(outFile);

}

void AudioEncoder::addEncodeCallback(EncodeCallback encodeCallback) {
    this->mCallback = encodeCallback;
}





