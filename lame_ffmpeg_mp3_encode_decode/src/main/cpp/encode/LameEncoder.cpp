//
// Created by 阳坤 on 2020-05-06.
//


#include "LameEncoder.h"

#define DEBUG_WRITE 0


/**
 * 初始化
 * @param mp3SavePath
 * @param sampleRate
 * @param channels
 * @param bit
 */
int LameEncoder::init(const char *mp3SavePath, int sampleRate, int channels, uint64_t bitRate) {
    this->mp3File = fopen(mp3SavePath, "wb");
    //初始化
    lameClient = lame_init();
    //设置采样率
    lame_set_in_samplerate(lameClient, sampleRate);
    lame_set_out_samplerate(lameClient, sampleRate);

    //设置通道
    lame_set_num_channels(lameClient, channels);

    //设置码率
    lame_set_brate(lameClient, bitRate);
    //第二个参数是质量设置，0~9 0质量最好
    lame_set_quality(lameClient, 2);

    //初始化参数
    lame_init_params(lameClient);

    return 1;


}

/**
 * 开始将 PCM 编码为 MP3
 * @param pcm
 */
int LameEncoder::encode(uint8_t *pcm, int bufferSize) {
    if (!pcm || bufferSize <= 0)return -1;
    int ret = 0;

    uint8_t *outmp3 = static_cast<uint8_t *>(malloc(bufferSize * 1.25 + 7200));

    if (lame_get_num_channels(lameClient) == 2) {
        ret = lame_encode_buffer_interleaved(lameClient, reinterpret_cast<short *>(pcm), bufferSize / 4,
                                             outmp3,
                                             (int) (bufferSize * 1.25) + 7500);
    } else {
        ret = lame_encode_buffer(lameClient, reinterpret_cast<const short *>(pcm), NULL, bufferSize / 2,
                                 outmp3,
                                 (int) (bufferSize * 1.25) + 7500);
    }

    fwrite(outmp3, 1, ret, mp3File);
    free(outmp3);
    return ret;
}

/**
 * 编码结束
 */
void LameEncoder::release() {
    if (mp3File) {
        fclose(mp3File);
        mp3File = NULL;
    }
    if (lameClient) {
        lame_close(lameClient);
    }



}


