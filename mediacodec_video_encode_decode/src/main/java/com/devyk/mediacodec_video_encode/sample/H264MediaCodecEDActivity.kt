package com.devyk.mediacodec_video_encode.sample

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.SurfaceHolder
import android.view.View
import com.devyk.common.config.VideoConfiguration
import com.devyk.common.callback.OnVideoEncodeListener
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.mediacodec_video_encode.R
import com.devyk.mediacodec_video_encode.mediacodec.H264Decoder
import com.devyk.mediacodec_video_encode.mediacodec.WriteH264
import kotlinx.android.synthetic.main.activity_mediacodec_video_ed.*
import java.nio.ByteBuffer

/**
 * <pre>
 *
 *     目前只是 Canvas + MediaCodec
 *     等短视频 SDK  开发完成在完善此组件 Camera + Surface + MediaCodec + OpenGL ES预览
 *     author  : devyk on 2020-06-15 23:10
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is H264MediaCodecEDActivity
 * </pre>
 */
public class H264MediaCodecEDActivity : BaseActivity<Int>(), SurfaceHolder.Callback,
    OnVideoEncodeListener {
    override fun onVideoOutformat(outputFormat: MediaFormat?) {

    }


    private var mH264Encoder: WriteH264? = null
    private var mH264Decoder: H264Decoder? = null
    private var mH264Buffer: ByteArray? = null


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mH264Decoder?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mH264Decoder?.stop()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }


    override fun initListener() {

    }

    override fun initData() {

    }

    override fun init() {
        mH264Encoder = WriteH264()
        mH264Decoder = H264Decoder()
        surface.getHolder().addCallback(this)
        mH264Encoder?.prepare(applicationContext, VideoConfiguration.createDefault())
        mH264Encoder?.setOnEncodeListener(this)



    }

    override fun getLayoutId(): Int = R.layout.activity_mediacodec_video_ed


    public fun startEncode(view: View) {
        if (mH264Encoder == null) {
            init()
            mH264Decoder?.start()
        }
        startTime(timer)
        mH264Encoder?.start()
    }

    public fun stopEncode(view: View) {
        cleanTime(timer)
        mH264Encoder?.stop()
        mH264Decoder?.stop()
        mH264Encoder = null
        mH264Decoder = null
    }

    /**
     * 编码完成的回调，
     * 注意：内部有写入文件的操作
     */

    override fun onVideoEncode(data: ByteBuffer?, info: MediaCodec.BufferInfo?) {

        if (data == null || info == null)return

        if (mH264Buffer == null || mH264Buffer?.size!! < info.size) {
            mH264Buffer = ByteArray(info.size)
        }
        data.position(info.offset)
        data.limit(info.offset + info.size)
        data.get(mH264Buffer, 0, info.size)

        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {//SPS,PPS
            // this is the first and only config sample, which contains information about codec
            // like H.264, that let's configure the decoder
            mH264Decoder?.configure(
                VideoConfiguration.Builder()
                    .setSurface(surface.holder.surface)
                    .setSpsPpsBuffer(ByteBuffer.wrap(mH264Buffer, 0, info.size))
                    .setCodeType(VideoConfiguration.ICODEC.DECODE)
                    .build()
            )
        } else {
            // pass byte[] to decoder's queue to render asap
            mH264Decoder?.enqueue(
                mH264Buffer!!,
                info.presentationTimeUs,
                info.flags
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mH264Decoder?.stop()
        mH264Encoder?.stop()
        mH264Encoder = null
        mH264Decoder = null
    }
}