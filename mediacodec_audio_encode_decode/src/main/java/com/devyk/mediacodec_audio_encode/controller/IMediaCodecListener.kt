package com.devyk.mediacodec_audio_encode.controller

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-13 17:04
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IMediaCodecListener
 * </pre>
 */
public interface IMediaCodecListener {
    //处理音频硬编编码器输出的数据
    fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {}

    //开始打包，一般进行打包的预处理
    abstract fun start()

    //结束打包，一般进行打包器的状态恢复
    abstract fun stop()
}