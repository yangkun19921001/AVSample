//
// Created by 阳坤 on 2020-08-23.
//

#ifndef AVSAMPLE_FFMPEGMUXER_H
#define AVSAMPLE_FFMPEGMUXER_H

#include <list>
#include "AVData.h"



class FFmpegMuxer {

private:

    int frameIndex = 0;


    int64_t cur_pts_v = 0, cur_pts_a = 0;

    AVPacket avPacket;


    AVStream *video_stream_;
    AVCodecContext *video_codec_context_;
    AVStream *audio_stream_;
    AVCodecContext *audio_codec_context_;
    AVBSFContext *bsf_context_;
    double last_audio_packet_presentation_time_mills_;
    int last_video_presentation_time_ms_;

    std::list<AVData> alist;
    std::list<AVData> vlist;


    int isExit = false;

public:


/** 8、获取视频流的时间戳(秒为单位的double) **/
    virtual double GetVideoStreamTimeInSecs();

/** 9、获取音频流的时间戳(秒为单位的double) **/
    double GetAudioStreamTimeInSecs();

    AVFormatContext *outFormatCtx = NULL;

    int NewStream(AVFormatContext *avFormatContext, int &inputIndex, AVMediaType mediaType);

    void WritePTS(AVPacket *avPacket, AVStream *inputStream);


    int BuildVideoStream();

    int BuildAudioStream();

    int Transform(const char *outputPath);

    void writeAVPacket(AVData data);

    int close();


    int WriteAudioFrame(AVFormatContext *pContext, AVStream *pStream);

    int WriteVideoFrame(AVFormatContext *pContext, AVStream *pStream);
};


#endif //AVSAMPLE_FFMPEGMUXER_H
