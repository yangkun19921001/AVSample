//
// Created by 阳坤 on 2020-06-02.
//

#include "AACDecoder.h"


AACDecoder::AACDecoder() : mSpecInfoSize(64), mDecoder(NULL), mPckSize(-1), mSpecInfo(NULL) {

}

AACDecoder::~AACDecoder() {

}

/**
 * 如果是 raw 格式的需要传递一个完整的 ES 包
 * @param specInfo 参考如下配置
 *
 * (1) aac eld 44.1kHz 双声道 conf为{ 0xF8, 0xE8, 0x50, 0x00 },转为二进制为:
 *
 *    11111 000111   0100 0010 1 0000 0000 0000
 *
 *
 *
 *    32     +  7              4        2        其他
 *
 *    aot为39,即eld; 4查表为44.1Hz, 2为双声道; 其他设置,参考源代码来解释,前3项正确,基本可以解码.
 *
 *    (2) aac ld 32kHz 单声道 conf为{ 0xBA, 0x88, 0x00, 0x00 },转为为二进制为:
 *
 *    10111 0101 0001 0000 0000 0000 0000 000
 *
 *    23           5         1        其他
 *
 *
 * @param mSpecInfoSize
 * @return
 */
int AACDecoder::initWithRawFormat(byte *specInfo, UINT mSpecInfoSize) {
    LOGI("initWithRawFormat");
    int nrOfLayers = 1;
    /**
     * 打开 AAC 解码器
     */
    mDecoder = aacDecoder_Open(TT_MP4_RAW, nrOfLayers);
    /**
     * 配置解码器信息
     */
    int ret = aacDecoder_ConfigRaw(mDecoder, &specInfo, &mSpecInfoSize);
    if (ret != AAC_DEC_OK) {
        LOGE("unable to set configraw\n");
        aacDecoder_Close(mDecoder);
        return 0;
    }
    initFrameSize();
    printAACInfo();
    return 1;
}

int AACDecoder::initWithADTSFormat() {
    LOGI("initWithRawFormat");
    int nrOfLayers = 1;
    /**
     * 打开 AAC 解码器
     */
    mDecoder = aacDecoder_Open(TT_MP4_ADTS, nrOfLayers);

    printAACInfo();
    return 1;
}

int AACDecoder::decode(byte *pck, int len, byte **outBuffer) {
    LOGI("Enter AACDecoder Decode");
    int threshold = (FDK_MAX_AUDIO_FRAME_SIZE * 3) / 2;
    uint8_t pcm_buf[threshold];
    int pcm_buf_index = 0;
    int pcm_buf_size = 0;
    while (len > 0) {
        int data_size = threshold;
        int len1 = this->fdkDecodeAudio((INT_PCM *) (pcm_buf + pcm_buf_index), &data_size,
                                        pck, len);
        if (len1 < 0) {
            /* if error, skip frame */
            len = 0;
            break;
        }
        pck += len1;
        len -= len1;
        if (data_size <= 0) {
            /* No data yet, get more frames */
            break;
        }
        pcm_buf_index += data_size;
        pcm_buf_size += data_size;
    }
    if (pcm_buf_size > 0) {
        *outBuffer = new byte[pcm_buf_size];
        memcpy(*outBuffer, pcm_buf, pcm_buf_size);
    }
    return pcm_buf_size;
}

void AACDecoder::destory() {
    LOGI("Enter AACDecoder Destroy");
    if (mDecoder) {
        aacDecoder_Close(mDecoder);
    }

}

void AACDecoder::printAACInfo() {
    CStreamInfo *aac_stream_info = aacDecoder_GetStreamInfo(mDecoder);
    if (aac_stream_info == NULL) {
        LOGI("aacDecoder_GetStreamInfo failed!\n");
        return;
    }
    LOGI("> stream info: channel = %d\tsample_rate = %d\tframe_size = %d\taot = %d\tbitrate = %d\n",
         aac_stream_info->channelConfig, aac_stream_info->aacSampleRate,
         aac_stream_info->aacSamplesPerFrame, aac_stream_info->aot, aac_stream_info->bitRate);

}

int AACDecoder::fdkDecodeAudio(INT_PCM *outBuffer, int *output_size, byte *pktBuffer, int pktSize) {
    printAACInfo();
    int ret = 0;
    UINT pkt_size = pktSize;
    UINT valid_size = pktSize;
    UCHAR *input_buf[1] = {pktBuffer};

    /* step 1 -> fill aac_data_buf to decoder's internal buf */
    ret = aacDecoder_Fill(mDecoder, input_buf, &pkt_size, &valid_size);
    if (ret != AAC_DEC_OK) {
        fprintf(stderr, "Fill failed: %x\n", ret);
        *output_size = 0;
        //AVERROR_INVALIDDATA
        return 0;
    }
    int buf_size = mPckSize;
    if (mPckSize <= 0) {
        buf_size = 10 * 1024;
    }
    /* step 2 -> call decoder function */
    int fdk_flags = 0;
    ret = aacDecoder_DecodeFrame(mDecoder, outBuffer, buf_size, fdk_flags);
    if (ret == AAC_DEC_NOT_ENOUGH_BITS) {
        fprintf(stderr, "not enough\n");
        *output_size = 0;
        return (pktSize - valid_size);
    }
    if (ret != AAC_DEC_OK) {
        fprintf(stderr, "aacDecoder_DecodeFrame : 0x%x\n", ret);
        *output_size = 0;
        //AVERROR_INVALIDDATA
        return 0;
    }
    if (mPckSize <= 0) {
        initFrameSize();
    }
    *output_size = mPckSize;
    /* return aac decode size */
    return (pktSize - valid_size);
}

void AACDecoder::initFrameSize() {
    CStreamInfo *aac_stream_info = aacDecoder_GetStreamInfo(mDecoder);
//    if (aac_stream_info) {
//        aac_stream_info->aacSamplesPerFrame = aac_stream_info->aacSamplesPerFrame * 2;
//        aac_stream_info->aacSampleRate = aac_stream_info->aacSampleRate * 2;
//        m_pcm_pkt_size = aac_stream_info->numChannels * 2 * aac_stream_info->frameSize;
        mPckSize = aac_stream_info->channelConfig * 2 * aac_stream_info->aacSamplesPerFrame;
//    }
}
