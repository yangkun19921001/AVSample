//
// Created by 阳坤 on 2020-08-23.
//

#include "FFmpegMuxer.h"
#include "logger.h"


FILE *AUDIO_FILE = 0;
FILE *VIDEO_FILE = 0;

// < 0 = error
// 0 = I-Frame
// 1 = P-Frame
// 2 = B-Frame
// 3 = S-Frame
int getVopType(const void *p, int len) {
    if (!p || 6 >= len)
        return -1;

    unsigned char *b = (unsigned char *) p;

    // Verify NAL marker
    if (b[0] || b[1] || 0x01 != b[2]) {
        b++;
        if (b[0] || b[1] || 0x01 != b[2])
            return -1;
    } // end if

    b += 3;

    // Verify VOP id
    if (0xb6 == *b) {
        b++;
        return (*b & 0xc0) >> 6;
    } // end if

    switch (*b) {
        case 0x65 :
            return 0;
        case 0x61 :
            return 1;
        case 0x01 :
            return 2;
    } // end switch

    return -1;
}

int get_nal_type(void *p, int len) {
    if (!p || 5 >= len)
        return -1;

    unsigned char *b = (unsigned char *) p;

    // Verify NAL marker
    if (b[0] || b[1] || 0x01 != b[2]) {
        b++;
        if (b[0] || b[1] || 0x01 != b[2])
            return -1;
    } // end if

    b += 3;

    return *b;
}


double FFmpegMuxer::GetVideoStreamTimeInSecs() {
    return last_video_presentation_time_ms_ / 1000.0f;
}


double FFmpegMuxer::GetAudioStreamTimeInSecs() {
    return last_audio_packet_presentation_time_mills_ / 1000.0f;
}


void FFmpegMuxer::WritePTS(AVPacket *avPacket, AVStream *inputStream) {
    if (avPacket->pts == AV_NOPTS_VALUE) {
        //Write PTS
        AVRational time_base = inputStream->time_base;
        //计算两帧的时间
        int64_t calc_duration =
                (double) AV_TIME_BASE / av_q2d(inputStream->r_frame_rate);
        //Parameters
        avPacket->pts = (double) (frameIndex * calc_duration) /
                        (double) (av_q2d(time_base) * AV_TIME_BASE);
        avPacket->dts = avPacket->pts;
        avPacket->duration = (double) calc_duration /
                             (double) (av_q2d(time_base) * AV_TIME_BASE);
        frameIndex++;
    }
}

static inline long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, nullptr);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

int FFmpegMuxer::Transform(const char *outputPath) {
    //1.注册所有组件
    av_register_all();
    //2.打开输出上下文
    avformat_alloc_output_context2(&outFormatCtx, NULL, NULL, outputPath);
    if (NULL == outFormatCtx) {
        LOGE("Could't create output context");
        return 0;
    }



    //6.打开输出文件
    if (!(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&outFormatCtx->pb, outputPath, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could't open output %s", outputPath);
            return 0;
        }
    }


    if (BuildAudioStream() < 0) {
        LOGE("BuildAudioStream error");
        return 0;
    }
    if (BuildVideoStream() < 0) {
        LOGE("BuildVideoStream error");
        return 0;
    }

    //7.写文件头
    int ret = avformat_write_header(outFormatCtx, NULL);
    if (ret < 0) {
        LOGE("Could't write header %s", av_err2str(ret));
        return 0;
    }


    while (true) {
        if (isExit) {
            break;
        }
        av_usleep(1000 * 1000 / 25);

        double video_time = GetVideoStreamTimeInSecs();
        double audio_time = GetAudioStreamTimeInSecs();
        int ret = 0;
        /* write interleaved audio and video frames */
        if (!video_stream_ || (video_stream_ && audio_stream_ && audio_time < video_time)) {
            if (alist.empty())continue;
            ret = WriteAudioFrame(outFormatCtx, audio_stream_);
        } else if (video_stream_) {
            if (vlist.empty())continue;
            ret = WriteVideoFrame(outFormatCtx, video_stream_);
        }
    }
//    close();

    return 0;
}

int FFmpegMuxer::close() {
    isExit = true;
    if (!outFormatCtx)
        return


    //9.写文件尾
    av_write_trailer(outFormatCtx);

    //关闭操作
    if (outFormatCtx && !(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_close(outFormatCtx->pb);
    }
    avformat_free_context(outFormatCtx);
    outFormatCtx = 0;
    LOGE("merge mp4 success! outPath:");
    return 0;
}


void FFmpegMuxer::writeAVPacket(AVData data) {
    data.pts = getCurrentTime();
    if (data.isAudio)
        alist.push_back(data);
    else
        vlist.push_back(data);


}

int FFmpegMuxer::BuildVideoStream() {
    AVCodec *codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (nullptr == codec) {
        LOGE("h264 encode codec is null");
        return -1;
    }
    video_stream_ = avformat_new_stream(outFormatCtx, codec);
    if (nullptr == video_stream_) {
        return -2;
    }
    video_codec_context_ = avcodec_alloc_context3(nullptr);
    if (nullptr == video_codec_context_) {
        return -3;
    }
    video_codec_context_->codec_type = AVMEDIA_TYPE_VIDEO;
    video_codec_context_->codec_id = AV_CODEC_ID_H264;
    video_codec_context_->pix_fmt = AV_PIX_FMT_YUV420P;
    video_codec_context_->bit_rate = 20000;
    video_codec_context_->width = 1280;
    video_codec_context_->height = 720;
    AVRational video_time_base = {1, 25};
    video_codec_context_->time_base = video_time_base;
    video_stream_->avg_frame_rate = video_time_base;
//    video_stream_->time_base = video_time_base;
    video_stream_->time_base.den = 25;
    video_stream_->time_base.num = 1;
    video_codec_context_->gop_size = static_cast<int>(25);
    video_codec_context_->qmin = 10;
    video_codec_context_->qmax = 30;
    // 新增语句，设置为编码延迟
    av_opt_set(video_codec_context_->priv_data, "preset", "ultrafast", 0);
    // 实时编码关键看这句，上面那条无所谓
    av_opt_set(video_codec_context_->priv_data, "tune", "zerolatency", 0);
    if (outFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        video_codec_context_->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    avcodec_parameters_from_context(video_stream_->codecpar, video_codec_context_);
    return 0;
}


int GetSampleRateIndex(int sampling_frequency) {
    switch (sampling_frequency) {
        case 96000:
            return 0;
        case 88200:
            return 1;
        case 64000:
            return 2;
        case 48000:
            return 3;
        case 44100:
            return 4;
        case 32000:
            return 5;
        case 24000:
            return 6;
        case 22050:
            return 7;
        case 16000:
            return 8;
        case 12000:
            return 9;
        case 11025:
            return 10;
        case 8000:
            return 11;
        case 7350:
            return 12;
        default:
            return 0;
    }
}

int FFmpegMuxer::BuildAudioStream() {
    LOGI("enter: %s", __func__);
    AVCodec *codec = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if (nullptr == codec) {
        return -1;
    }
    audio_stream_ = avformat_new_stream(outFormatCtx, codec);
    if (nullptr == audio_stream_) {
        LOGE("new audio stream error");
        return -2;
    }
    audio_codec_context_ = avcodec_alloc_context3(nullptr);
    audio_codec_context_->codec_type = AVMEDIA_TYPE_AUDIO;
    audio_codec_context_->sample_fmt = AV_SAMPLE_FMT_S16;
    audio_codec_context_->codec_id = AV_CODEC_ID_AAC;
    audio_codec_context_->sample_rate = 44100;
    audio_codec_context_->bit_rate = 64;
    audio_codec_context_->channel_layout = 1 == 1 ? AV_CH_LAYOUT_MONO : AV_CH_LAYOUT_STEREO;
    audio_codec_context_->channels = av_get_channel_layout_nb_channels(audio_codec_context_->channel_layout);
    audio_codec_context_->flags |= CODEC_FLAG_GLOBAL_HEADER;
    audio_codec_context_->extradata = reinterpret_cast<uint8_t *>(av_malloc(2));
    audio_codec_context_->extradata_size = 2;
    // AAC LC by default
    unsigned int object_type = 2;
    uint8_t dsi[2];
    dsi[0] = static_cast<uint8_t>((object_type << 3) |
                                  (GetSampleRateIndex(audio_codec_context_->sample_rate) >> 1));
    dsi[1] = static_cast<uint8_t>(((GetSampleRateIndex(audio_codec_context_->sample_rate) & 1) << 7) |
                                  (audio_codec_context_->channels << 3));
    memcpy(audio_codec_context_->extradata, dsi, 2);
    avcodec_parameters_from_context(audio_stream_->codecpar, audio_codec_context_);
    const AVBitStreamFilter *bit_stream_filter_ = av_bsf_get_by_name("aac_adtstoasc");
    if (nullptr == bit_stream_filter_) {
        LOGE("aac adts filter is null.");
        return -1;
    }
    int ret = av_bsf_alloc(bit_stream_filter_, &bsf_context_);
    if (ret < 0) {
        LOGE("av_bsf_alloc error: %s", av_err2str(ret));
        return ret;
    }
    ret = avcodec_parameters_copy(bsf_context_->par_in, audio_stream_->codecpar);
    if (ret < 0) {
        LOGE("copy parameters to par_in error: %s", av_err2str(ret));
        return ret;
    }
    ret = av_bsf_init(bsf_context_);
    if (ret < 0) {
        LOGE("av_bsf_init error: %s", av_err2str(ret));
        return ret;
    }
    LOGI("leave: %s", __func__);

    return 0;
}

int FFmpegMuxer::WriteAudioFrame(AVFormatContext *pContext, AVStream *pStream) {
    AVPacket *pkt = av_packet_alloc();
    AVData data = alist.front();
    alist.pop_front();
    pkt->data = static_cast<uint8_t *>(malloc(sizeof(uint8_t*) * data.size));
    memcpy(pkt->data,data.data,data.size);
    pkt->size = data.size;
    pkt->dts = pkt->pts = (int64_t) (last_audio_packet_presentation_time_mills_ / 1000.0f / av_q2d(pStream->time_base));
    last_audio_packet_presentation_time_mills_ = data.pts;
    pkt->stream_index = pStream->index;
//    WritePTS(pkt, pStream);


    //8.转换PTS/DTS
//    avPacket.pts = av_rescale_q_rnd(avPacket.pts, pStream->time_base, pStream->time_base,
//                                    (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
//    avPacket.dts = av_rescale_q_rnd(avPacket.dts, pStream->time_base, pStream->time_base,
//                                    (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
//    avPacket.duration = av_rescale_q(avPacket.duration, pStream->time_base,
//                                     pStream->time_base);
//    avPacket.pos = -1;
//    avPacket.stream_index = 0;

    int ret = av_interleaved_write_frame(pContext, pkt);
    if (ret != 0) {
        LOGE("write audio frame error: %s", av_err2str(ret));
    }

    av_packet_free(&pkt);
    LOGE("write audio frame success: duration:%lld  pts:%lld  dts:%lld", avPacket.duration, avPacket.pts, avPacket.dts);
    return 0;
}

int waitkey = 1;

int FFmpegMuxer::WriteVideoFrame(AVFormatContext *pContext, AVStream *pStream) {
    AVPacket *pkt = av_packet_alloc();
    AVData data = vlist.front();
    vlist.pop_front();
    pkt->data = static_cast<uint8_t *>(malloc(sizeof(uint8_t*) * data.size));
    memcpy(pkt->data,data.data,data.size);
    pkt->size = data.size;
//    pkt->dts = pkt->pts = (int64_t) (last_audio_packet_presentation_time_mills_ / 1000.0f / av_q2d(pStream->time_base));
    last_video_presentation_time_ms_ = data.pts;
    pkt->flags |= (0 >= getVopType(data.data, data.size)) ? AV_PKT_FLAG_KEY : 0;
    pkt->stream_index = pStream->index;

    if (waitkey)
        if (0 == (pkt->flags & AV_PKT_FLAG_KEY))
            return 0;
        else
            waitkey = 0;

    pkt->pts = (frameIndex++) * (90000 / 25);
//    WritePTS(pkt, pStream);
    //8.转换PTS/DTS
//    avPacket.pts = av_rescale_q_rnd(avPacket.pts, pStream->time_base, pStream->time_base,
//                                    (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
//    avPacket.dts = av_rescale_q_rnd(avPacket.dts, pStream->time_base, pStream->time_base,
//                                    (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
//    avPacket.duration = av_rescale_q(avPacket.duration, pStream->time_base,
//                                     pStream->time_base);
//

    int ret = av_interleaved_write_frame(pContext, pkt);
    if (ret != 0) {
        LOGE("write video frame error: %s", av_err2str(ret));
    }
    av_packet_free(&pkt);

    free(data.data);
    LOGE("write video frame success: duration:%lld  pts:%lld  dts:%lld", avPacket.duration, avPacket.pts, avPacket.dts);
    return 0;
}


