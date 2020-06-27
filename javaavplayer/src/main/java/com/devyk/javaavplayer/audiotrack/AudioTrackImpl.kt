package com.devyk.javaavplayer.audiotrack

import android.media.AudioFormat
import android.media.AudioTrack
import com.devyk.common.LogHelper

/**
 * <pre>
 *     author  : devyk on 2020-06-27 19:32
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioTrackImpl
 * </pre>
 */

public class AudioTrackImpl : IAudioTrack {


    private lateinit var mAudioTrack: AudioTrack

    private var isPlayer = false;


    override fun prepare(
        streamType: Int,
        sampleRate: Int,
        channels: Int,
        audioFormat: Int
    ): Boolean {
        var bufSize = getMinBufferSize(sampleRate, channels, audioFormat)
        mAudioTrack = AudioTrack(
            streamType,
            sampleRate,
            channels,
            audioFormat,
            bufSize,
            AudioTrack.MODE_STREAM
        )
        //初始化成功
        if (mAudioTrack.state != AudioTrack.STATE_INITIALIZED) {
            LogHelper.e("AudioTrack", "不能播放，当前播放器未处于初始化状态..");
            return false
        }
        return true
    }


    override fun start() {
        mAudioTrack.play()
        isPlayer = true;
    }

    override fun encode(byteArray: ByteArray) {
        mAudioTrack.write(byteArray, 0, byteArray.size)
    }

    override fun stop() {
        mAudioTrack.stop()
        isPlayer = false;
    }

    override fun release() {
        mAudioTrack?.release()
        mAudioTrack == null;
        isPlayer = false;
    }

    fun getStatus(): Boolean = isPlayer
}