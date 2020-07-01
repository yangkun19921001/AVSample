//
// Created by 阳坤 on 2020-06-29.
//



#include <unistd.h>
#include <malloc.h>
#include "SLESPlayer.h"
#include <thread>
#include <AVData.h>

using namespace std;
FILE *file = 0;


AVData SLESPlayer::getPCMData() {
    AVData data;
    while (isInit) {
        if (frames.empty()) {
            usleep(1000 * 10);
            continue;
        }

        data = frames.front();
        frames.pop_front();

        this->out_pcm_buffer_size = data.size;
        return data;
    }
    return data;
};

void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void *pVoid) {
    SLESPlayer *audioPlayer = static_cast<SLESPlayer *>(pVoid);
    AVData data = audioPlayer->getPCMData();
    if (data.size <= 0) {
        LOGE("GetData() size is 0");
        return;
    }
    memcpy(audioPlayer->out_pcm_buffer, data.data, data.size);
    if ((*audioPlayer->pcmBufferQueue) && audioPlayer->pcmBufferQueue) {
        (*audioPlayer->pcmBufferQueue)->Enqueue(audioPlayer->pcmBufferQueue, audioPlayer->out_pcm_buffer, data.size);
    }

    data.drop();
}


/**
 * 相当于初始化 准备播放
 * @param sampleRate
 * @param channels
 * @param sampleFormat
 * @return
 */
int SLESPlayer::prepare(int sampleRate, int channels, int sampleFormat) {
    this->sampleFormat = sampleFormat;
    this->mSampleRate = sampleRate;
    this->mChannels = channels;
    this->out_pcm_buffer = static_cast<uint8_t *>(malloc(sampleRate * 2 * 2));
    //1. 创建 SLES 播放引擎
    if (checkCreate(slCreateEngine(&engineObject, 0, 0, 0, 0, 0)) != SUCCESSED) {
        LOGE("slCreateEngine error!\n");
        return ERROR;
    }
    //1.1 初始化引擎接口
    if (checkRealize((*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE)) != SUCCESSED) {
        LOGE("engineObject Realize error!\n");
        return ERROR;
    }
    //1.2 拿到引擎对象接口
    if (checkCreate((*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine)) != SUCCESSED) {
        LOGE("engineEngine GetInterface error!\n");
        return ERROR;
    }

    //2. 创建混音器
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    if (checkCreate((*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq)) !=
        SUCCESSED) {
        LOGE("CreateOutputMix error!\n");
        return ERROR;
    }

    //2.1 初始化混音器
    if (checkRealize((*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE)) != SUCCESSED) {
        LOGE("outputMixObject Realize error!\n");
        return ERROR;
    }

    //2.2 获取混音器接口   expected expression
    if (checkCreate((*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                                     &outputMixEnvironmentalReverb)) == SUCCESSED) {
        //设置混音环境的属性
        (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, 0};

    //3. 配置 PCM 信息
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};


    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            static_cast<SLuint32>(mChannels),//2个声道（立体声）
            getSamplesPerSec(this->mSampleRate),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
//            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_SPEAKER_FRONT_LEFT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };

    SLDataSource slDataSource = {&android_queue, &pcm};


    //4. TODO 创建播放器 2 个声道的时候选择
//    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_MUTESOLO};
//    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    const SLInterfaceID ids[] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[] = {SL_BOOLEAN_TRUE};
    if (checkCreate((*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject, &slDataSource, &audioSnk,
                                                       sizeof(ids) / sizeof(ids[0]), ids, req)) != SUCCESSED) {
        LOGE("CreateAudioPlayer  error!\n");
        return ERROR;
    }

    //4.1 初始化播放器
    if (checkRealize((*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE)) != SUCCESSED) {
        LOGE("pcmPlayerObject Realize error!\n");
        return ERROR;
    }
    //4.2 得到播放器接口
    if (checkCreate((*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay)) !=
        SUCCESSED) {
        LOGE("pcmPlayerPlay  GetInterface  error!\n");
        return ERROR;
    }
    //4.3 得到设置音量的接口
    if (checkCreate((*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmVolumePlay)) ==
        SUCCESSED) {
        LOGE("pcmVolumePlay  GetInterface  error!\n");
        setVolume(this->DEFAULT_VOLUME);
    }
    //4.4 得到设置音频通道接口
    if (checkCreate((*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_MUTESOLO, &pcmChannelModePlay)) !=
        SUCCESSED) {
        LOGE("pcmChannelModePlay  GetInterface  error!\n");
    }

    //4.5 得到播放缓冲队列接口
    if (checkCreate((*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue)) ==
        SUCCESSED) {
        LOGE("pcmBufferQueue  GetInterface  success!\n");
        //4.5.1 注册缓冲队列回调 一帧播放完了 就会回调
        (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    }

    LOGI("OpenSL ES init is success!\n");

    file = fopen("/sdcard/avsample/test_.pcm", "wb");

    isInit = 1;
    return SUCCESSED;
}

/**
 * 开始播放
 */
void SLESPlayer::play() {
    //6. 设置播放状态
    setPlayState(SL_PLAYSTATE_PLAYING);
//    //主动调用回调接口，开始播放
    pcmBufferCallBack(pcmBufferQueue, this);


}

/**
 * 暂停播放
 */
void SLESPlayer::pause() {
    setPlayState(SL_PLAYSTATE_PAUSED);
}


/**
 * 送入 PCM 数据
 * @param data
 * @return
 */
int SLESPlayer::enqueue(uint8_t *data, int len) {
    AVData avData;
    avData.data = static_cast<unsigned char *>(malloc(len));
    memcpy(avData.data, data, len);
    avData.size = len;
    avData.type = AVPACKET_TYPE;
    frames.push_back(avData);
    return 1;
}


/*
 * 释放
 */
void SLESPlayer::release() {
    isInit = false;
    while (!frames.empty()) {
        if (frames.front().data)
            frames.front().drop();
        frames.pop_front();
    }


    //清理播放队列
    if (pcmBufferQueue && (*pcmBufferQueue)) {
        (*pcmBufferQueue)->Clear(pcmBufferQueue);
    }
    //销毁player对象
    if (pcmPlayerObject && (*pcmPlayerObject)) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
    }
    //销毁混音器
    if (outputMixObject && (*outputMixObject)) {
        (*outputMixObject)->Destroy(outputMixObject);
    }

    //销毁播放引擎
    if (engineObject && (*engineObject)) {
        (*engineObject)->Destroy(engineObject);
    }

    engineObject = NULL;
    engineEngine = NULL;
    outputMixObject = NULL;
    pcmPlayerObject = NULL;
    pcmPlayerPlay = NULL;
    pcmBufferQueue = NULL;

    if (this->out_pcm_buffer) {
        free(this->out_pcm_buffer);
        this->out_pcm_buffer = 0;
    }


}


//设置音量
void SLESPlayer::setVolume(int percent) {
    if (pcmVolumePlay != NULL) {
        if (percent > 30) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -20);
        } else if (percent > 25) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -22);
        } else if (percent > 20) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -25);
        } else if (percent > 15) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -28);
        } else if (percent > 10) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -30);
        } else if (percent > 5) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -34);
        } else if (percent > 3) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -37);
        } else if (percent > 0) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -40);
        } else {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -100);
        }
    }

}

/**
 * 设置播放的声道
 * @param channelMode
 */
void SLESPlayer::setChannelMode(int channelMode) {
    if (pcmChannelModePlay != NULL) {
        if (channelMode == 0)//right
        {
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 1, false);
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 0, true);
        } else if (channelMode == 1)//left
        {
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 1, true);
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 0, false);
        } else if (channelMode == 2)//center
        {
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 1, false);
            (*pcmChannelModePlay)->SetChannelMute(pcmChannelModePlay, 0, false);
        }


    }

}


/**
 * 对应于 SL ES 中采样率宏
 * @param sample_rate
 * @return
 */
uint32_t SLESPlayer::getSamplesPerSec(int sample_rate) {
    uint32_t rate = 0;
    switch (sample_rate) {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate = SL_SAMPLINGRATE_44_1;
    }
    return rate;
}

int SLESPlayer::checkCreate(u_int32_t result) {
    return result == SL_RESULT_SUCCESS;
}

uint32_t SLESPlayer::checkRealize(u_int32_t result) {
    return result == SL_BOOLEAN_FALSE;
}

void SLESPlayer::setPlayState(SLuint32 state) {
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, state);
}




