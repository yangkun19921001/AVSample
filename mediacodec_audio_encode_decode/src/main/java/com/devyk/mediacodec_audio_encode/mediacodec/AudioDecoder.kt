package com.devyk.mediacodec_audio_encode.mediacodec

import android.media.MediaCodec
import com.devyk.mediacodec_audio_encode.AudioConfiguration
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-14 00:06
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioDecoder 音频硬解码器
 * </pre>
 */
public class AudioDecoder(private val audioConfiguration: AudioConfiguration) : BaseCodec(audioConfiguration) {

    public var mListener: OnAudioDecodeListener? = null

    fun setOnAudioEncodeListener(listener: OnAudioDecodeListener?) {
        mListener = listener
    }


    override fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        mListener?.onAudioPCMData(bb, bi)
    }


}