package com.devyk.common.mediacodec

import android.media.MediaCodec
import android.view.Surface
import com.devyk.common.config.VideoConfiguration
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-15 21:42
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is ICodec
 * </pre>
 */

public interface ICodec {



    /**
     * 初始化编码器
     */
    fun prepare(videoConfiguration: VideoConfiguration?){};

    /**
     * start 编码
     */
    fun start();

    /**
     * 停止编码
     */
    fun stop();

    /**
     * 返回编码好的 H264 数据
     */
    abstract fun onVideoEncode(bb: ByteBuffer?, mBufferInfo: MediaCodec.BufferInfo)


}