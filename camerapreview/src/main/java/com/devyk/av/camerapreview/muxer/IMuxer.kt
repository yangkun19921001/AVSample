package com.devyk.av.camerapreview.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-07-08 17:44
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IMuxer
 * </pre>
 */
public interface IMuxer {

    /**
     * 初始化 复用器
     */
    fun init(path: String, outType: Int)

    /**
     * 开始
     */
    fun start(mediaFormat: MediaFormat?):Int?


    /**
     * 写入数据
     */
    fun writeSampleData(
        trackIndex: Int, byteBuf: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )


    /**
     * 释放复用器
     */
    fun release()


}