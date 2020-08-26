#ifndef RECORDING_PUBLISHER_H
#define RECORDING_PUBLISHER_H


#include <string>
#include "audio_packet_queue.h"
#include "video_packet_queue.h"
#include <map>

#define COLOR_FORMAT            AV_PIX_FMT_YUV420P
#ifndef PUBLISH_DATA_TIME_OUT
#define PUBLISH_DATA_TIME_OUT 15 * 1000
#endif

#define AUDIO_QUEUE_ABORT_ERR_CODE               -100200
#define VIDEO_QUEUE_ABORT_ERR_CODE               -100201

#ifndef PUBLISH_INVALID_FLAG
#define PUBLISH_INVALID_FLAG -1
#endif

#define MAX(a, b)  (((a) > (b)) ? (a) : (b))
#define MIN(a, b)  (((a) < (b)) ? (a) : (b))
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>
#include <libavutil/time.h>
};


#ifndef MAX
#define MAX(a, b)  (((a) > (b)) ? (a) : (b))
#endif

#ifndef MIN
#define MIN(a, b)  (((a) < (b)) ? (a) : (b))
#endif

namespace platform_4_live {
    static inline long getCurrentTimeMills() {
        struct timeval tv;
        gettimeofday(&tv, NULL);
        return tv.tv_sec * 1000 + tv.tv_usec / 1000;
    }

    static inline long getCurrentTimeSeconds() {
        struct timeval tv;
        gettimeofday(&tv, NULL);
        return tv.tv_sec;
    }

}

class RecordingPublisher {


private:


    pthread_mutex_t mutex;
    pthread_t id;

public:
    RecordingPublisher();

    virtual ~RecordingPublisher();

    static int interrupt_cb(void *ctx);

    int detectTimeout();

    virtual int init(char *videoOutputURI,
                     int videoWidth, int videoHeight, float videoFrameRate, int videoBitRate,
                     int audioSampleRate, int audioChannels, int audioBitRate
                     );

    virtual void registerFillAACPacketCallback(
            int (*fill_aac_packet)(AudioPacket **, void *context), void *context);

    virtual void registerFillVideoPacketCallback(
            int (*fill_packet_frame)(VideoPacket **, void *context), void *context);

    virtual void registerPublishTimeoutCallback(int (*on_publish_timeout_callback)(void *context),
                                                void *context);

    void start();

    int encode();

    virtual int stop();

    void interruptPublisherPipe() {
        this->publishTimeout = PUBLISH_INVALID_FLAG;
    };

    inline bool isInterrupted() {
        return this->publishTimeout == PUBLISH_INVALID_FLAG;
    };

    /** 声明填充一帧PCM音频的方法 **/
    typedef int (*fill_aac_packet_callback)(AudioPacket **, void *context);

    /** 声明填充一帧H264 packet的方法 **/
    typedef int (*fill_h264_packet_callback)(VideoPacket **, void *context);

    /** 当由于网络问题导致timeout的问题回调 **/
    typedef int (*on_publish_timeout_callback)(void *context);

protected:
    /** 1、为AVFormatContext增加指定的编码器 **/
    virtual AVStream *add_stream(AVFormatContext *oc, AVCodec **codec, enum AVCodecID codec_id,
                                 char *codec_name);

    /** 2、开启视频编码器 **/
    virtual int open_video(AVFormatContext *oc, AVCodec *codec, AVStream *st);

    /** 3、开启音频编码器 **/
    int open_audio(AVFormatContext *oc, AVCodec *codec, AVStream *st);

    /** 4、为视频流写入一帧数据 **/
    virtual int write_video_frame(AVFormatContext *oc, AVStream *st) = 0;

    /** 5、为音频流写入一帧数据 **/
    virtual int write_audio_frame(AVFormatContext *oc, AVStream *st);

    /** 6、关闭视频流 **/
    virtual void close_video(AVFormatContext *oc, AVStream *st);

    /** 7、关闭音频流 **/
    void close_audio(AVFormatContext *oc, AVStream *st);

    /** 8、获取视频流的时间戳(秒为单位的double) **/
    virtual double getVideoStreamTimeInSecs() = 0;

    /** 9、获取音频流的时间戳(秒为单位的double) **/
    double getAudioStreamTimeInSecs();

    int buildVideoStream();

    int buildAudioStream(char *audio_codec_name);

protected:
    // sps and pps data
    uint8_t *headerData = 0;
    int headerSize;
    int publishTimeout;

    int isRuning = true;

    //秒为单位
    int startSendTime = 0;


    int interleavedWriteFrame(AVFormatContext *s, AVPacket *pkt);

    /** 输出视频的上下文以及视频音频流 **/
    AVOutputFormat *fmt = 0;
    AVFormatContext *oc = 0;
    AVStream *video_st = 0;
    AVStream *audio_st=0;
    AVBitStreamFilterContext *bsfc=0;
    double duration;

    /** 音频流数据输出 **/
    double lastAudioPacketPresentationTimeMills;

    /** 音频与视频的编码参数 **/
    int videoWidth;
    int videoHeight;
    float videoFrameRate;
    int videoBitRate;
    int audioSampleRate;
    int audioChannels;
    int audioBitRate;

    /** 注册回调函数 **/
    fill_aac_packet_callback fillAACPacketCallback;
    void *fillAACPacketContext;
    fill_h264_packet_callback fillH264PacketCallback;
    void *fillH264PacketContext;
    /** 当我们发送超时的时候，会断开连接，这个时候通知客户端进行停止掉生产者 **/
    on_publish_timeout_callback onPublishTimeoutCallback;
    void *timeoutContext;

    /** 为了纪录发出的最后一帧的发送时间, 以便于判断超时 **/
    long sendLatestFrameTimemills;
    /** 纪录是否成功连接上了RTMPServer **/
    bool isConnected;
    bool isWriteHeaderSuccess = 0;
};

#endif
