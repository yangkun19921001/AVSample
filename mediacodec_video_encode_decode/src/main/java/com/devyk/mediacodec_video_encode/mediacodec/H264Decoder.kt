package com.devyk.mediacodec_video_encode.mediacodec

import android.media.MediaCodec
import com.devyk.common.config.VideoConfiguration
import com.devyk.common.mediacodec.BaseVideoDecoder
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-16 20:06
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is H264Decoder
 * </pre>
 */
public class H264Decoder : BaseVideoDecoder() {

    override fun onVideoEncode(bb: ByteBuffer?, mBufferInfo: MediaCodec.BufferInfo) {
        super.onVideoEncode(bb, mBufferInfo)
    }


    override fun configure(videoConfiguration: VideoConfiguration?) {
        super.configure(videoConfiguration)
    }

    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }

}