package com.devyk.mediacodec_audio_encode.mediacodec

import android.media.MediaCodec
import android.util.Log
import com.devyk.mediacodec_audio_encode.AudioConfiguration
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-13 23:53
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BaseCoder
 * </pre>
 */
abstract class BaseCodec(private val mAudioConfiguration: AudioConfiguration?) : ICodec {
    private var mMediaCodec: MediaCodec? = null

    internal var mBufferInfo = MediaCodec.BufferInfo()

    /**
     * 编码完成的函数自己不处理，交由子类处理
     */
    abstract fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo);

    @Synchronized
    override fun prepareCoder() {
        mMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration!!)
        mMediaCodec!!.start()
        Log.e("encode", "--start")
    }

    /**
     * 将数据入队 java.lang.IllegalStateException
     */
    @Synchronized
    override fun enqueueCodec(input: ByteArray?) {
        if (mMediaCodec == null) {
            return
        }
        val inputBuffers = mMediaCodec!!.inputBuffers
        val outputBuffers = mMediaCodec!!.outputBuffers
        val inputBufferIndex = mMediaCodec!!.dequeueInputBuffer(12000)
        if (inputBufferIndex >= 0) {
            val inputBuffer = inputBuffers[inputBufferIndex]
            inputBuffer.clear()
            inputBuffer.put(input)
            mMediaCodec!!.queueInputBuffer(inputBufferIndex, 0, input!!.size, 0, 0)
        }

        var outputBufferIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 12000)
        while (outputBufferIndex >= 0) {
            val outputBuffer = outputBuffers[outputBufferIndex]
            onAudioData(outputBuffer, mBufferInfo)
            mMediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 0)
        }
    }

    @Synchronized
    override fun stop() {
        if (mMediaCodec != null) {
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
            mMediaCodec = null
        }
    }

}