//
// Created by 阳坤 on 2020-08-23.
//

#ifndef AVSAMPLE_FFMPEGMUXER_H
#define AVSAMPLE_FFMPEGMUXER_H


extern "C"{
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
};

class FFmpegMuxer {

private:

    int frameIndex = 0;

    int videoIndex = -1;
    int audioIndex = -1;

    int audioStreamIndex = -1;
    int videoStreamIndex = -1;

    int64_t cur_pts_v = 0, cur_pts_a = 0;

    AVPacket avPacket;

    AVFormatContext *videoFormatCtx = NULL;
    AVFormatContext *audioFormatCtx = NULL;

public:

    AVFormatContext *outFormatCtx = NULL;

    int NewStream(AVFormatContext *avFormatContext, int &inputIndex, AVMediaType mediaType);

    void WritePTS(AVPacket *avPacket, AVStream *inputStream);

    int Transform(const char *videoPath, const char *audioPath, const char *outputPath);

};


#endif //AVSAMPLE_FFMPEGMUXER_H
