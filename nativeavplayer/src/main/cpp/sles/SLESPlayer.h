//
// Created by 阳坤 on 2020-06-29.
//

#ifndef AVSAMPLE_SLESPLAYER_H
#define AVSAMPLE_SLESPLAYER_H


#include <SLES/OpenSLES_Android.h>
#include <stdint.h>
#include <cstring>
#include "common.h"
#include "AVData.h"


#include <list>





class SLESPlayer {

private:
    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    //混音器
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;

    //音频通道
    SLMuteSoloItf pcmChannelModePlay = NULL;

    //音量
    SLVolumeItf pcmVolumePlay = NULL;


    //采样率
    int mSampleRate;
    //通道
    int mChannels;
    //采样格式
    int sampleFormat;
    int DEFAULT_VOLUME = 100;


    int isInit = 0;


private:
    uint32_t getSamplesPerSec(int sampleRate);


public:
    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

    std::list<AVData> frames;

    /**
     * 大小
     */
    SLuint32
            out_pcm_buffer_size;

    /**
    * pcm 缓冲数据
    */
    uint8_t *out_pcm_buffer = NULL;
public:
    /**
     * 准备初始化
     * @param sampleRate
     * @param channels
     * @param sampleFormat
     * @return
     */
    int prepare(int sampleRate, int channels, int sampleFormat);


    void play();

    void pause();

    /**
     * PCM 数据
     * @param data
     * @return
     */
    int enqueue(uint8_t *data, int len);

    /**
     * 释放
     */
    void release();

    /**
     * 设置音量
     * @param values
     */
    void setVolume(int values);

    /**
     * 设置音频通道模式
     * @param mode
     */
    void setChannelMode(int mode);

    int checkCreate(u_int32_t result);

    uint32_t checkRealize(u_int32_t ret);


    void setPlayState(SLuint32 state);


    AVData getPCMData();
};


#endif //AVSAMPLE_SLESPLAYER_H
