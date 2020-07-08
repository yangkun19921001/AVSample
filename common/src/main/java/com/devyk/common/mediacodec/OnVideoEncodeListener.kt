package com.devyk.common.mediacodec

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * 编码回调
 */
interface OnVideoEncodeListener {
    abstract fun onVideoEncode(bb: ByteBuffer, bi: MediaCodec.BufferInfo)
}
