package com.devyk.av.camera_recorder.packer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import com.devyk.av.camera_recorder.controller.StreamController
import com.devyk.av.camera_recorder.muxer.MakeMP4
import com.devyk.common.LogHelper
import com.devyk.common.callback.OnAudioEncodeListener
import com.devyk.common.callback.OnVideoEncodeListener
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLContext
import kotlin.experimental.and

/**
 * <pre>
 *     author  : devyk on 2020-07-11 17:07
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is MP4Packer
 * </pre>
 */
public class MP4Packer(context: Context, textureId: Int, eglContext: EGLContext, path: String) : IPacker,
    OnVideoEncodeListener, OnAudioEncodeListener {


    private var TAG = javaClass.simpleName
    protected lateinit var mMakeMP4: MakeMP4
    private lateinit var mController: StreamController

    private var mTrackAudioIndex = -1;
    private var mTrackVideoIndex = -1;

    init {
        mController = StreamController(context, textureId, eglContext)
        mMakeMP4 = MakeMP4(path)
    }

    override fun start() {
        mController.setAudioEncodeListener(this)
        mController.setOnVideoEncodeListener(this)
        mController.start()


    }


    override fun stop() {
        mController.stop()
        mMakeMP4?.release()
    }

    override fun pause() {
        mController.pause()

    }

    override fun resume() {
        mController.resume()
    }


    override fun onVideoOutformat(outputFormat: MediaFormat?) {
        mTrackVideoIndex = mMakeMP4.addTrack(outputFormat)!!
        if (mTrackAudioIndex != -1)
            mMakeMP4.start()
    }

    override fun onAudioOutformat(outputFormat: MediaFormat?) {
        mTrackAudioIndex = mMakeMP4.addTrack(outputFormat)!!
        if (mTrackVideoIndex != -1)
            mMakeMP4.start()

    }

    /**
     * 编码完成的 H264 数据
     *   00 00 00 01 06:  SEI信息
     *   00 00 00 01 67:  0x67&0x1f = 0x07 :SPS
     *   00 00 00 01 68:  0x68&0x1f = 0x08 :PPS
     *   00 00 00 01 65:  0x65&0x1f = 0x05: IDR Slice
     */
    override fun onVideoEncode(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {
        if (bi != null && bb != null) {
            var h264Arrays = ByteArray(bi.size)
            bb?.position(bi.offset)
            bb?.limit(bi.offset + bi.size)
            bb?.get(h264Arrays)
            val tag = h264Arrays[4].and(0x1f).toInt()
            if (tag == 0x07) {//sps
                LogHelper.e(TAG, " SPS " + h264Arrays.size)
            } else if (tag == 0x08) {//pps 这里需要在 第一帧里面截取才能拿到 pps
                LogHelper.e(TAG, " PPS ")
            } else if (tag == 0x05) {//关键帧
                LogHelper.e(TAG, " 关键帧 " + h264Arrays.size)
            } else {
                //普通帧
                LogHelper.e(TAG, " 普通帧 " + h264Arrays.size)
            }
            if (mTrackVideoIndex != -1 && mMakeMP4.isStart())
                mMakeMP4.writeSampleData(mTrackVideoIndex, bb, bi)
        }
    }


    /**
     * 报错：java.lang.IllegalStateException: writeSampleData returned an error
     *   at android.media.MediaMuxer.nativeWriteSampleData(Native Method)
     *   at android.media.MediaMuxer.writeSampleData(MediaMuxer.java:682)
     * 解决：
     *  音频 pts 时间戳问题 新的时间戳必须比旧的时间戳大
     */
    override fun onAudioEncode(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        if (mTrackAudioIndex != -1 && mMakeMP4.isStart()) {
            try {
                mMakeMP4.writeSampleData(mTrackAudioIndex, bb, bi)
            } catch (error: Exception) {
                LogHelper.e(TAG, error.message)
            }

        }
    }


}