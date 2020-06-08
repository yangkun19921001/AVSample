package com.devyk.fdkaac_audio_encode_decode

/**
 * <pre>
 *     author  : devyk on 2020-06-02 21:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IEncoder
 * </pre>
 */
public interface IEncoder {

    /**
     * @param bitRate 比特率
     * @param channel 采样通道
     * @param sampleRate 采样率
     */
    fun init(bitRate: Int, channel: Int, sampleRate: Int): Int;

    /**
     * 编码
     */
    fun encode(byteArray: ByteArray, bufferSize: Int);


    /**
     * 释放编码器
     */
    fun destory();
}