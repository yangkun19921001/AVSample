package com.devyk.av.camerapreview.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import java.io.File
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-07-08 17:55
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BaseMediaMuxer
 * </pre>
 */
public open class BaseMediaMuxer : IMuxer {

    protected  var mMeidaMuxer: MediaMuxer?=null

    constructor(path: String, outType: Int) {
        init(path, outType)
    }

    override fun init(path: String, outType: Int) {
        checkNotNull(path)
        mMeidaMuxer = MediaMuxer(path, outType)
    }

    override fun start(mediaFormat: MediaFormat?): Int? {
        mediaFormat?.let { format ->
            val addTrack = mMeidaMuxer?.addTrack(format)
            mMeidaMuxer?.start()
            return addTrack
        }

        return -1;
    }

    override fun writeSampleData(trackIndex: Int, byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        mMeidaMuxer?.writeSampleData(trackIndex, byteBuf, bufferInfo)
    }

    override fun release() {
        mMeidaMuxer?.stop()
        mMeidaMuxer?.release()
    }
}