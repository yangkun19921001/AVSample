package com.devyk.av.camera_recorder.packer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import com.devyk.av.camera_recorder.controller.StreamController
import com.devyk.av.camera_recorder.muxer.MakeMP4
import com.devyk.av.camera_recorder.muxer.NativeMuxer
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
    protected  var mMakeMP4: MakeMP4?=null
    private lateinit var mController: StreamController

    private var mTrackAudioIndex = -1;
    private var mTrackVideoIndex = -1;

    private var mPath : String?=null

    init {
        mPath = path;
        mController = StreamController(context, textureId, eglContext)
//        mMakeMP4 = MakeMP4(path)


    }

    override fun start() {

        Thread {
            mPath?.let { NativeMuxer.init(it) };
        }.start()
        mController.setAudioEncodeListener(this)
        mController.setOnVideoEncodeListener(this)
        mController.start()


    }


    override fun stop() {
        mController.stop()
        mMakeMP4?.release()
        NativeMuxer.close()
    }

    override fun pause() {
        mController.pause()

    }

    override fun resume() {
        mController.resume()
    }


    override fun onVideoOutformat(outputFormat: MediaFormat?) {
//        mTrackVideoIndex = mMakeMP4.addTrack(outputFormat)!!
//        if (mTrackAudioIndex != -1)
//            mMakeMP4.start()
    }

    override fun onAudioOutformat(outputFormat: MediaFormat?) {
//        mTrackAudioIndex = mMakeMP4.addTrack(outputFormat)!!
//        if (mTrackVideoIndex != -1)
//            mMakeMP4.start()

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

            NativeMuxer.enqueue(h264Arrays, 0, bi.presentationTimeUs)


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
//            if (mTrackVideoIndex != -1 && mMakeMP4.isStart())
//                mMakeMP4.writeSampleData(mTrackVideoIndex, bb, bi)
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
        var data = ByteArray(bi.size)
        bb?.position(bi.offset)
        bb?.limit(bi.offset + bi.size)
        bb.get(data)
//        addADTStoPacket(data, data.size)
        NativeMuxer.enqueue(data, 1, bi.presentationTimeUs)


//            if (mTrackAudioIndex != -1 && mMakeMP4.isStart()) {
//            try {
//                mMakeMP4.writeSampleData(mTrackAudioIndex, bb, bi)
//            } catch (error: Exception) {
//                LogHelper.e(TAG, error.message)
//            }

//        }
    }

    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 // AAC LC
        val freqIdx = 0x4 // 16KHz
        val chanCfg = 1 // CPE

        // fill in ADTS data
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF1.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()

    }


}