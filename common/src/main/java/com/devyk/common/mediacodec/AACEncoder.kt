package com.devyk.common.mediacodec

import android.media.MediaCodec
import android.media.MediaFormat
import com.devyk.common.callback.OnAudioEncodeListener
import com.devyk.common.config.AudioConfiguration
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:08
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioEncoder
 * </pre>
 */

class AACEncoder(private val mAudioConfiguration: AudioConfiguration?) : BaseAudioCodec(mAudioConfiguration) {
    override fun onAudioOutformat(outputFormat: MediaFormat?) {
        mListener?.onAudioOutformat(outputFormat)
    }

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
