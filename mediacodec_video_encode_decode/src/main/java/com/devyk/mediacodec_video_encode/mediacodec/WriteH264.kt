package com.devyk.mediacodec_video_encode.mediacodec

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.devyk.common.LogHelper
import com.devyk.common.config.VideoConfiguration
import com.devyk.common.mediacodec.H264Encoder
import com.devyk.common.callback.OnVideoEncodeListener
import com.devyk.mediacodec_video_encode.renderer.DevYKSurfaceRenderer
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * <pre>
 *     author  : devyk on 2020-06-15 22:16
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is H264Encoder
 * </pre>
 */
public class WriteH264 : H264Encoder() {
    override fun onVideoOutformat(outputFormat: MediaFormat?) {

    }

    private var mRenderer: DevYKSurfaceRenderer? = null
    private var mContext: Context? = null

    private var mFileOutputStream: FileOutputStream? = null

    private var listener: OnVideoEncodeListener? = null

    override fun onSurfaceCreate(surface: Surface?) {
        super.onSurfaceCreate(surface)
        mRenderer = DevYKSurfaceRenderer(mContext!!, surface!!)
        mRenderer?.start()
    }


    override fun onSurfaceDestory(surface: Surface?) {
        super.onSurfaceDestory(surface)
        mRenderer?.stopAndWait()
    }


    public fun setOnEncodeListener(listener: OnVideoEncodeListener) {
        this.listener = listener
    }

    /**
     * 准备编码
     */
    fun prepare(context: Context, convideoConfiguration: VideoConfiguration) {
        mContext = context
        prepare(convideoConfiguration)


    }

    /**
     * 开始编码
     */
    override fun start() {
        super.start()
        mFileOutputStream = FileOutputStream("sdcard/avsample/mediacodec_video.h264")
    }

    /**
     * 停止编码
     */
    override fun stop() {
        super.stop()
        mFileOutputStream?.close()
    }

    /**
     * 编码完成的 H264 数据
     *   00 00 00 01 06:  SEI信息
     *   00 00 00 01 67:  0x67&0x1f = 0x07 :SPS
     *   00 00 00 01 68:  0x68&0x1f = 0x08 :PPS
     *   00 00 00 01 65:  0x65&0x1f = 0x05: IDR Slice
     */
    override fun onVideoEncode(bb: ByteBuffer?, bi: MediaCodec.BufferInfo) {
        Log.e(TAG, bi.size.toString())
        val h264Arrays = ByteArray(bi.size)
        bb?.position(bi.offset)
        bb?.limit(bi.offset + bi.size)
        bb?.get(h264Arrays)
        val tag = h264Arrays[4].and(0x1f).toInt()
        if (tag == 0x07) {//sps
            LogHelper.e(TAG, " SPS " + h264Arrays.size)
        } else if (tag == 0x08) {//pps
            LogHelper.e(TAG, " PPS ")
        } else if (tag == 0x05) {//关键字帧
            LogHelper.e(TAG, " 关键帧 " + h264Arrays.size)
        } else {
            //普通帧
            LogHelper.e(TAG, " 普通帧 " + h264Arrays.size)
        }
        listener?.onVideoEncode(bb!!,bi)
        mFileOutputStream?.write(h264Arrays)
    }


}