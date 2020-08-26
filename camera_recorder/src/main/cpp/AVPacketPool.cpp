//
// Created by 阳坤 on 2020-08-25.
//

#include <vector>
#include "AVPacketPool.h"
#include "video_packet_queue.h"


static inline long getCurrentTimeMills()
{
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}
/**
 * 接收音频数据
 * @param audioData
 * @param size
 * @param pts
 * @return
 */
int AVPacketPool::enqueueAudio(uint8_t *audioData, int size, int pts) {
    int timeMills = (int)(pts/1000.0f);
    AudioPacket *audioPacket = new AudioPacket();
    audioPacket->data = new byte[size];
    memcpy(audioPacket->data, audioData, size);
    audioPacket->size = size;
    audioPacket->position = timeMills;
    pushRecordingAudioPacketToQueue(audioPacket);
    return 0;
}


/**
 * 接收视频数据
 * @param outputData
 * @param size
 * @param lastPresentationTimeUs
 * @return
 */
int AVPacketPool::enqueueVideo(uint8_t *outputData, int size, int lastPresentationTimeUs) {
    int timeMills = (int) (lastPresentationTimeUs / 1000.0f);
    // push to queue
    int nalu_type = (outputData[4] & 0x1F);
    LOGI("Final is in nalu_type is %d... size is %d", nalu_type, size);
    if (H264_NALU_TYPE_SEQUENCE_PARAMETER_SET == nalu_type) {
        std::vector<NALUnit *> *units = new vector<NALUnit *>();
        parseH264SpsPps(outputData, size, units);
        int unitSize = units->size();
        LOGI("unitSize is %d", unitSize);
        if (unitSize > 2) {
            //证明是sps和pps后边有I帧
            const char bytesHeader[] = "\x00\x00\x00\x01";
            size_t headerLength = 4; //string literals have implicit trailing '\0'
            NALUnit *idrUnit = units->at(2);
            int idrSize = idrUnit->naluSize + headerLength;
            VideoPacket *videoPacket = new VideoPacket();
            videoPacket->buffer = new byte[idrSize];
            memcpy(videoPacket->buffer, bytesHeader, headerLength);
            memcpy(videoPacket->buffer + headerLength, idrUnit->naluBody, idrUnit->naluSize);
            videoPacket->size = idrSize;
            videoPacket->timeMills = timeMills;
            if (videoPacket->size > 0)
                pushRecordingVideoPacketToQueue(videoPacket);
        }
        if (isSPSUnWriteFlag) {
            VideoPacket *videoPacket = new VideoPacket();
            videoPacket->buffer = new byte[size];
            memcpy(videoPacket->buffer, outputData, size);
            videoPacket->size = size;
            videoPacket->timeMills = timeMills;
            if (videoPacket->size > 0)
                pushRecordingVideoPacketToQueue(videoPacket);
            isSPSUnWriteFlag = false;
        }
    } else if (size > 0) {
        //为了兼容有一些设备的MediaCodec编码出来的每一帧有多个Slice的问题(华为荣耀6，华为P9)
        int frameBufferSize = 0;
        size_t headerLength = 4;
        byte *frameBuffer;
        const char bytesHeader[] = "\x00\x00\x00\x01";

        vector<NALUnit *> *units = new vector<NALUnit *>();

        parseH264SpsPps(outputData, size, units);
        vector<NALUnit *>::iterator i;
        for (i = units->begin(); i != units->end(); ++i) {
            NALUnit *unit = *i;
            int frameLen = unit->naluSize;
            frameBufferSize += headerLength;
            frameBufferSize += frameLen;
        }
        frameBuffer = new byte[frameBufferSize];
        int frameBufferCursor = 0;
        for (i = units->begin(); i != units->end(); ++i) {
            NALUnit *unit = *i;
            uint8_t *nonIDRFrame = unit->naluBody;
            int nonIDRFrameLen = unit->naluSize;
            memcpy(frameBuffer + frameBufferCursor, bytesHeader, headerLength);
            frameBufferCursor += headerLength;
            memcpy(frameBuffer + frameBufferCursor, nonIDRFrame, nonIDRFrameLen);
            frameBufferCursor += nonIDRFrameLen;
            frameBuffer[frameBufferCursor - nonIDRFrameLen - headerLength] = ((nonIDRFrameLen) >> 24) & 0x00ff;
            frameBuffer[frameBufferCursor - nonIDRFrameLen - headerLength + 1] = ((nonIDRFrameLen) >> 16) & 0x00ff;
            frameBuffer[frameBufferCursor - nonIDRFrameLen - headerLength + 2] = ((nonIDRFrameLen) >> 8) & 0x00ff;
            frameBuffer[frameBufferCursor - nonIDRFrameLen - headerLength + 3] = ((nonIDRFrameLen)) & 0x00ff;
            delete unit;
        }
        delete units;

        VideoPacket *videoPacket = new VideoPacket();
        videoPacket->buffer = frameBuffer;
        videoPacket->size = frameBufferSize;
        videoPacket->timeMills = timeMills;
        if (videoPacket->size > 0)
            pushRecordingVideoPacketToQueue(videoPacket);
    }

    return 0;
}


bool AVPacketPool::detectDiscardVideoPacket() {
    if (recordingVideoQueue)
        return recordingVideoQueue->size() > VIDEO_PACKET_QUEUE_THRRESHOLD;
    return false;
}

void AVPacketPool::recordDropVideoFrame(int discardVideoPacketDuration) {
    pthread_rwlock_wrlock(&mRwlock);
    totalDiscardVideoPacketDuration += discardVideoPacketDuration;
    pthread_rwlock_unlock(&mRwlock);
}

bool AVPacketPool::pushRecordingVideoPacketToQueue(VideoPacket *videoPacket) {
    if (!videoPacket) {
        LOGE("videoPacket 为空");
        return 0;
    }
    bool dropFrame = false;
    if (NULL != recordingVideoQueue) {
        while (detectDiscardVideoPacket()) {
            dropFrame = true;
            int discardVideoFrameCnt = 0;
            int discardVideoFrameDuration = recordingVideoQueue->discardGOP(&discardVideoFrameCnt);
            if (discardVideoFrameDuration < 0) {
                break;
            }
//            if(NULL != statistics){
//                statistics->discardVideoFrame(discardVideoFrameCnt);
//            }
            recordDropVideoFrame(discardVideoFrameDuration);
            //            LOGI("discard a GOP Video Packet And totalDiscardVideoPacketSize is %d", totalDiscardVideoPacketSize);
        }
        //为了计算当前帧的Duration, 所以延迟一帧放入Queue中
        if (NULL != tempVideoPacket) {
            int packetDuration = videoPacket->timeMills - tempVideoPacket->timeMills;
            tempVideoPacket->duration = packetDuration;
            recordingVideoQueue->put(tempVideoPacket);
            tempVideoPacketRefCnt = 0;
        }
        tempVideoPacket = videoPacket;
        tempVideoPacketRefCnt = 1;
//        if(NULL != statistics){
//            statistics->pushVideoFrame();
//        }
    }
    return dropFrame;
}





AVPacketPool::AVPacketPool() {
    isSPSUnWriteFlag = true;
    pthread_rwlock_init(&mRwlock, NULL);

}

int AVPacketPool::getRecordingVideoPacketQueueSize() {
    if (NULL != recordingVideoQueue) {
        return recordingVideoQueue->size();
    }
    return 0;
}

int AVPacketPool::getRecordingAudioPacketQueueSize() {
    if (NULL != recordingAudioQueue) {
        return recordingAudioQueue->size();
    }
    return 0;
}

void AVPacketPool::pushRecordingAudioPacketToQueue(AudioPacket *pPacket) {
    if (recordingAudioQueue)
        recordingAudioQueue->put(pPacket);
}



void AVPacketPool::initRecordVideoQueue() {
    recordingVideoQueue = new VideoPacketQueue("H264 QUEUE");
    totalDiscardVideoPacketDuration = 0;
}

void AVPacketPool::initRecordAudioQueue() {
    const char *name = "audioPacket AAC Data queue";
    recordingAudioQueue = new AudioPacketQueue(name);

}

void AVPacketPool::destoryRecordingVideoPacketQueue() {
    if (NULL != recordingVideoQueue) {
        delete recordingVideoQueue;
        recordingVideoQueue = NULL;
        if (tempVideoPacketRefCnt > 0) {
            delete tempVideoPacket;
            tempVideoPacket = NULL;
        }
    }

}

void AVPacketPool::destoryAudioPacketQueue() {
    if (NULL != recordingAudioQueue) {
        delete recordingAudioQueue;
        recordingAudioQueue = NULL;
    }
}

int AVPacketPool::getAudioPacket(AudioPacket **audioPacket) {
    if (recordingAudioQueue && recordingAudioQueue->get(audioPacket, true) < 0) {
        LOGI("aacPacketPool->getAudioPacket return negetive value...");
        return -1;
    }
    return 1;
}

int AVPacketPool::getVideoPacket(VideoPacket **pVideoPacket) {
    if (recordingVideoQueue && recordingVideoQueue->get(pVideoPacket, true) < 0) {
        LOGI("aacPacketPool->getAudioPacket return negetive value...");
        return -1;
    }
    return 1;
}



AVPacketPool::~AVPacketPool() {
    isSPSUnWriteFlag = false;
}

void AVPacketPool::initAVQueue() {
    isSPSUnWriteFlag = true;
    initRecordVideoQueue();
    initRecordAudioQueue();
}


