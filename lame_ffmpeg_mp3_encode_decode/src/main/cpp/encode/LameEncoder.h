//
// Created by 阳坤 on 2020-05-06.
//

#ifndef AUDIOPLAYER_PCM_2_MP3_DECODE_H
#define AUDIOPLAYER_PCM_2_MP3_DECODE_H
#include <cstring>
#include <stdio.h>
#include <malloc.h>

extern "C" {

#include "lame.h"
}

class LameEncoder {
public:


public:

    int init(const char *mp3SavePath, int sampleRate, int channels, uint64_t bitRate);

    int encode(uint8_t *pcm,int size);

    void release();

public:
    FILE *mp3File = NULL;
    lame_t lameClient;
    int sampleRate;
    int channel;
    int bit;

};


#endif //AUDIOPLAYER_PCM_2_MP3_DECODE_H
