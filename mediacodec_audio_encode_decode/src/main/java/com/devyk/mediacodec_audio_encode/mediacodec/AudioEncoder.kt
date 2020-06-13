package com.devyk.mediacodec_audio_encode.mediacodec

import android.media.MediaCodec
import android.util.Log
import com.devyk.mediacodec_audio_encode.AudioConfiguration
import java.nio.ByteBuffer
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

class AudioEncoder(private val mAudioConfiguration: AudioConfiguration?) : BaseCodec(mAudioConfiguration) {

    public var mListener: OnAudioEncodeListener? = null


    override fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        mListener?.onAudioEncode(bb, bi)
    }

    fun setOnAudioEncodeListener(listener: OnAudioEncodeListener?) {
        mListener = listener
    }

    override fun stop() {
        super.stop()
        mListener = null
    }
}
