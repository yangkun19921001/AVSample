//
// Created by 阳坤 on 2020-08-25.
//

#ifndef AVSAMPLE_AVPACKETPOOL_H
#define AVSAMPLE_AVPACKETPOOL_H


#include <cstdint>
#include "logger.h"
#include "h264_util.h"
#include "video_packet_queue.h"
#include "audio_packet_queue.h"


#define VIDEO_PACKET_QUEUE_THRRESHOLD                                        60

/**
 * 维护音频视频送入 H264, AAC 数据
 */
class AVPacketPool {


protected:
    VideoPacketQueue *recordingVideoQueue = 0;
    AudioPacketQueue *recordingAudioQueue = 0;

    void recordDropVideoFrame(int discardVideoPacketDuration);


    /**
     * 将视频数据入队列
     * @param videoPacket
     * @return
     */
    bool pushRecordingVideoPacketToQueue(VideoPacket *videoPacket);

    bool detectDiscardVideoPacket();


private:
    AVPacketPool(); //注意:构造方法私有
    ~AVPacketPool(); //注意:构造方法私有

    /**
     * 是否写入 SPS 也就是第一帧
     */
    bool isSPSUnWriteFlag;

    /** 为了计算每一帧的时间长度 **/
    VideoPacket *tempVideoPacket = 0;
    int tempVideoPacketRefCnt;

    /** 为了丢帧策略所做的实例变量 **/
    int totalDiscardVideoPacketDuration;
    pthread_rwlock_t mRwlock;

    void initRecordVideoQueue();

    void initRecordAudioQueue();


public:

    static AVPacketPool *GetInstance() //工厂方法(用来获得实例)
    {
        static AVPacketPool *packetPool = new AVPacketPool();
        return packetPool;
    }

    /**
     * 外部传入的视频数据
     * @param videoData
     * @param size
     * @param pts
     * @return
     */
    int enqueueVideo(uint8_t *videoData, int size, int pts);


    /**
     * 外部传入的音频数据
     * @param audioData
     * @param size
     * @param pts
     * @return
     */
    int enqueueAudio(uint8_t *audioData, int size, int pts);

    int getAudioPacket(AudioPacket **audioPacket);

    int getVideoPacket(VideoPacket **pVideoPacket);

    void destoryRecordingVideoPacketQueue();

    void destoryAudioPacketQueue();

    int getRecordingVideoPacketQueueSize();

    void pushRecordingAudioPacketToQueue(AudioPacket *pPacket);

    int getRecordingAudioPacketQueueSize();


    void initAVQueue();


    AudioPacketQueue * getAudioPacketQueue(){
        return recordingAudioQueue;
    }
    VideoPacketQueue * getVideoPacketQueue(){
        return recordingVideoQueue;
    }

    void closeAVPacketPool();
};


#endif //AVSAMPLE_AVPACKETPOOL_H
