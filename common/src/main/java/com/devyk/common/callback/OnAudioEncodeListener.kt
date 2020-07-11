package com.devyk.common.callback

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:09
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is OnAudioEncodeListener
 * </pre>
 */
public interface OnAudioEncodeListener {
    fun onAudioEncode(bb: ByteBuffer, bi: MediaCodec.BufferInfo)
    fun onAudioOutformat(outputFormat: MediaFormat?)
}