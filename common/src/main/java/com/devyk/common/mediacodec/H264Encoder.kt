package com.devyk.common.mediacodec

import android.media.MediaCodec
import android.media.MediaFormat
import com.devyk.common.callback.OnVideoEncodeListener
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-07-09 22:57
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is H264Encoder
 * </pre>
 */
public open class H264Encoder : BaseVideoEncoder() {


    override fun onVideoOutformat(outputFormat: MediaFormat?) {
        mListener?.onVideoOutformat(outputFormat)
    }


    private var mListener: OnVideoEncodeListener? = null

    /**
     * 视频编码完成的回调
     */
    override fun onVideoEncode(bb: ByteBuffer?, bi: MediaCodec.BufferInfo) {
        mListener?.onVideoEncode(bb!!, bi)
    }

    /**
     * 设置编码回调
     */
    fun setOnVideoEncodeListener(listener: OnVideoEncodeListener) {
        mListener = listener
    }


}