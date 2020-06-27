package com.devyk.javaavplayer.audiotrack

import android.media.AudioFormat
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioManager
import android.media.AudioTrack


/**
 * <pre>
 *     author  : devyk on 2020-06-27 19:18
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IAudioTrack
 * </pre>
 */

public interface IAudioTrack {


    /**
     * @param streamType //播放的流类型
     * @param sampleRate //采样率
     * @param channels //音频通道数量
     * @param audioFormat //采样格式
     * @param bufferSizeInBytes //播放的缓冲区大小
     * @param mode //MODE_STATIC 一次性将所有数据都写入播放缓冲区中,用于铃声和提醒音频
     *             //MODE_STREAM 需要按照一定的时间间隔不间断的写入音频数据
     */
    fun prepare(
        streamType: Int,
        sampleRate: Int,//采样率
        channels: Int,//音频通道数量
        audioFormat: Int//采样格式
    ):Boolean


    /**
     * 获取一个最小的播放缓冲区大小
     */
    fun getMinBufferSize(sampleRate: Int, channels: Int, audioFormat: Int) =
        AudioTrack.getMinBufferSize(sampleRate, channels, audioFormat)


    /**
     * 将 PCM 数据送入缓冲区
     */
    fun encode(byteArray: ByteArray)


    /**
     * 开始
     */
    fun  start();

    /**
     * 停止播放
     */
    fun stop()

    /**
     * 销毁资源
     */
    fun release()


}