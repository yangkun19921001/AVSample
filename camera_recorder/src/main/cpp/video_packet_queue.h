#ifndef LIVE_VIDEO_PACKETQUEUE_H
#define LIVE_VIDEO_PACKETQUEUE_H

#include <pthread.h>
#include "logger.h"

#define H264_NALU_TYPE_NON_IDR_PICTURE                                  1
#define H264_NALU_TYPE_IDR_PICTURE                                      5
#define H264_NALU_TYPE_SEQUENCE_PARAMETER_SET                           7
#define H264_NALU_TYPE_PICTURE_PARAMETER_SET							8
#define H264_NALU_TYPE_SEI                                          	6


#define NON_DROP_FRAME_FLAG                                             -1.0f
#define DTS_PARAM_UN_SETTIED_FLAG										-1
#define DTS_PARAM_NOT_A_NUM_FLAG										-2
#define PTS_PARAM_UN_SETTIED_FLAG										-1

typedef unsigned char byte;

typedef struct VideoPacket {
    byte * buffer;
    int size;
    int timeMills;
    int duration;
    int64_t pts;
    int64_t dts;
    VideoPacket() {
        buffer = NULL;
        size = 0;
        pts = PTS_PARAM_UN_SETTIED_FLAG;
        dts = DTS_PARAM_UN_SETTIED_FLAG;
    }
    ~VideoPacket() {
        if (NULL != buffer) {
            delete[] buffer;
            buffer = NULL;
        }
    }
    int getNALUType() {
        int nalu_type = H264_NALU_TYPE_NON_IDR_PICTURE;
        if(NULL != buffer){
            nalu_type = (buffer[4] & 0x1F);
        }
        return nalu_type;
    }
    bool isIDRFrame(){
        bool ret = false;
        if(getNALUType() == H264_NALU_TYPE_IDR_PICTURE){
            ret = true;
        }
        return ret;
    }
    VideoPacket* clone(){
        VideoPacket* result = new VideoPacket();
        result->buffer = new byte[size];
        memcpy(result->buffer, buffer, size);
        result->size = size;
        result->timeMills = timeMills;
        return result;
    }
    
} VideoPacket;

typedef struct VideoPacketList {
    VideoPacket *pkt;
    struct VideoPacketList *next;
    VideoPacketList(){
        pkt = NULL;
        next = NULL;
    }
} VideoPacketList;

class VideoPacketQueue {
public:
    VideoPacketQueue();
    VideoPacketQueue(const char* queueNameParam);
    ~VideoPacketQueue();

    void init();
    void flush();
    int put(VideoPacket* videoPacket);
    /* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
    int get(VideoPacket **videoPacket, bool block);
    int discardGOP(int* discardVideoFrameCnt);
    int size();
    void abort();

private:
    VideoPacketList* mFirst;
    VideoPacketList* mLast;
    int mNbPackets;
    bool mAbortRequest;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    const char* queueName = 0;
    
    float currentTimeMills;
};

#endif // LIVE_VIDEO_PACKETQUEUE_H
