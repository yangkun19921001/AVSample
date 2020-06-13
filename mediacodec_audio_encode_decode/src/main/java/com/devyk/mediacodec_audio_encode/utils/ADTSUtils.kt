package com.devyk.mediacodec_audio_encode.utils

/**
 * <pre>
 *     author  : devyk on 2020-06-13 17:36
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is ADTSUtils
 * </pre>
 */
public object ADTSUtils {

    init {
        System.loadLibrary("native-jni")
    }

    /**
     * 0: 96000 Hz
    1: 88200 Hz
    2: 64000 Hz
    3: 48000 Hz
    4: 44100 Hz
    5: 32000 Hz
    6: 24000 Hz
    7: 22050 Hz
    8: 16000 Hz
    9: 12000 Hz
    10: 11025 Hz
    11: 8000 Hz
    12: 7350 Hz
    13: Reserved
    14: Reserved
    15: frequency is written explictly
     */
    public external fun addADTStoPacket(
        packet: ByteArray,
        packetLen: Int = 7,
        profile: Int = 2,
        freqIdx: Int = 4,
        chanCfgCounts: Int = 1
    );


}