//
// Created by 阳坤 on 2020-08-23.
//

#include "FFmpegMuxer.h"
#include "logger.h"


FILE*AUDIO_FILE = 0;
FILE*VIDEO_FILE = 0;

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

int
FFmpegMuxer::NewStream(AVFormatContext *avFormatContext, int &inputIndex, AVMediaType mediaType) {
    int outputStreamIndex = -1;
    for (int index = 0; index < avFormatContext->nb_streams; index++) {
        //根据输入流创建一个输出流
        if (avFormatContext->streams[index]->codecpar->codec_type == mediaType) {
            AVStream *in_stream = avFormatContext->streams[index];
            AVCodecContext *codec_ctx = avcodec_alloc_context3(NULL);
            avcodec_parameters_to_context(codec_ctx, in_stream->codecpar);
            AVStream *out_stream = avformat_new_stream(outFormatCtx, codec_ctx->codec);
            inputIndex = index;
            if (NULL == out_stream) {
                LOGE("Could't allocating output stream");
                return outputStreamIndex;
            }
            outputStreamIndex = out_stream->index;
            if (avcodec_parameters_from_context(out_stream->codecpar, codec_ctx) < 0) {
                LOGE("Could't to copy context from input to output stream codec context");
                return outputStreamIndex;
            }
            codec_ctx->codec_tag = 0;
            if (outFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
                codec_ctx->flags |= CODEC_FLAG_GLOBAL_HEADER;
            }
            break;
        }
    }
    return outputStreamIndex;
}

static inline long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, nullptr);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

int FFmpegMuxer::Transform(const char *videoPath, const char *audioPath, const char *outputPath) {
    //1.注册所有组件
    av_register_all();
    //2.打开输出上下文
    avformat_alloc_output_context2(&outFormatCtx, NULL, NULL, outputPath);
    if (NULL == outFormatCtx) {
        LOGE("Could't create output context");
        return 0;
    }

    //3.打开视频输入文件上下文
    if (avformat_open_input(&videoFormatCtx, videoPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(videoFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;
    }
    //4.打开音频输入文件上下文
    if (avformat_open_input(&audioFormatCtx, audioPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(audioFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;

    }

    //5.新建视频和音频输出流
    videoStreamIndex = NewStream(videoFormatCtx, videoIndex, AVMEDIA_TYPE_VIDEO);
    audioStreamIndex = NewStream(audioFormatCtx, audioIndex, AVMEDIA_TYPE_AUDIO);

    //6.打开输出文件
    if (!(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&outFormatCtx->pb, outputPath, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could't open output %s", outputPath);
            return 0;
        }
    }
    //7.写文件头
   int ret =  avformat_write_header(outFormatCtx, NULL);
    if (ret < 0) {
        LOGE("Could't write header %s",av_err2str(ret));
        return 0;
    }

    int audioExit = 0;
    int videoExit = 0;
    while (true) {
        int streamIndex = 0;
        AVStream *inStream, *outStream;


        if (av_compare_ts(cur_pts_v, videoFormatCtx->streams[videoIndex]->time_base, cur_pts_a,
                          audioFormatCtx->streams[audioIndex]->time_base) <= 0) {
            streamIndex = videoStreamIndex;
            while (!videoExit) {
                int ret = av_read_frame(videoFormatCtx, &avPacket);
                if (ret < 0) {
                    videoExit = true;
                    break;
                }
                inStream = videoFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == videoIndex) {
                    //没有PTS的需要写入 Raw H.264
                    WritePTS(&avPacket, inStream);
                    cur_pts_v = avPacket.pts;
                    break;
                }
            }

        } else {
            streamIndex = audioStreamIndex;
            while (!audioExit) {

                int ret = av_read_frame(audioFormatCtx, &avPacket);
                if (ret < 0) {
                    audioExit = true;
                    break;
                }
                inStream = audioFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == audioIndex) {
                    WritePTS(&avPacket, inStream);
                    cur_pts_a = avPacket.pts;
                    break;
                }
            }
        }
        if (audioExit ){
            LOGE("准备退出！");
            break;
        }

        //8.转换PTS/DTS
        avPacket.pts = av_rescale_q_rnd(avPacket.pts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.dts = av_rescale_q_rnd(avPacket.dts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.duration = av_rescale_q(avPacket.duration, inStream->time_base,
                                         outStream->time_base);
        avPacket.pos = -1;
        avPacket.stream_index = streamIndex;

        //写数据
        if (av_interleaved_write_frame(outFormatCtx, &avPacket) < 0) {
            LOGE("Could't write frame");
            break;
        }
        av_packet_unref(&avPacket);
    }
    //9.写文件尾
    av_write_trailer(outFormatCtx);

    avformat_close_input(&videoFormatCtx);
    avformat_close_input(&audioFormatCtx);
    //关闭操作
    if (outFormatCtx && !(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_close(outFormatCtx->pb);
    }
    avformat_free_context(outFormatCtx);
    LOGE("merge mp4 success! outPath:%s", outputPath);
    return 0;
}


