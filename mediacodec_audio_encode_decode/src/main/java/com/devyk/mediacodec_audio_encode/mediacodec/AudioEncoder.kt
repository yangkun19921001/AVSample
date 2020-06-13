package com.devyk.mediacodec_audio_encode.mediacodec

import android.media.MediaCodec
import android.util.Log
import com.devyk.mediacodec_audio_encode.AudioConfiguration
import java.util.concurrent.LinkedBlockingQueue

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:08
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioEncoder
 * </pre>
 */

class AudioEncoder(private val mAudioConfiguration: AudioConfiguration?) {
    private var mMediaCodec: MediaCodec? = null
    private var mListener: OnAudioEncodeListener? = null
    internal var mBufferInfo = MediaCodec.BufferInfo()



    fun setOnAudioEncodeListener(listener: OnAudioEncodeListener?) {
        mListener = listener
    }

    internal fun prepareEncoder() {
        mMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration!!)
        mMediaCodec!!.start()
        Log.e("encode","--start")
    }

    @Synchronized
    fun stop() {
        if (mMediaCodec != null) {
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
            mMediaCodec = null
        }
    }


    private fun encode(input: ByteArray?) {
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
            if (mListener != null) {
                mListener!!.onAudioEncode(outputBuffer, mBufferInfo)
            }
            mMediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 0)
        }
    }

    @Synchronized
    internal fun offerEncoder(input: ByteArray) {
        encode(input);
    }
}
