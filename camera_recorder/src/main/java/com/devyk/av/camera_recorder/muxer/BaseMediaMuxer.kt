package com.devyk.av.camera_recorder.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
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


    protected var mMeidaMuxer: MediaMuxer? = null

    private var TAG = javaClass.simpleName

    private var isStart = false

    constructor(path: String, outType: Int) {
        init(path, outType)
    }

    override fun init(path: String, outType: Int) {
        checkNotNull(path)
        mMeidaMuxer = MediaMuxer(path, outType)
        isStart = false
    }

    override fun start() {
        mMeidaMuxer?.start()
        isStart = true
    }

    override fun addTrack(format: MediaFormat?): Int? = mMeidaMuxer?.addTrack(format!!)

    override fun writeSampleData(trackIndex: Int, byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        mMeidaMuxer?.writeSampleData(trackIndex, byteBuf, bufferInfo)
    }

    public fun isStart(): Boolean = isStart

    override fun release() {
        if (!isStart()) return
        mMeidaMuxer?.stop()
        mMeidaMuxer?.release()
        isStart = false
    }
}