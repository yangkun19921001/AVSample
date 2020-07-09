package com.devyk.av.camerapreview.encode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.devyk.av.camerapreview.callback.IGLThreadConfig
import com.devyk.av.camerapreview.callback.IRenderer
import com.devyk.av.camerapreview.egl.EglHelper
import com.devyk.av.camerapreview.egl.renderer.EncodeRenderer
import com.devyk.av.camerapreview.egl.thread.GLThread
import com.devyk.av.camerapreview.muxer.MakeMP4
import com.devyk.av.camerapreview.widget.base.GLSurfaceView
import com.devyk.av.camerapreview.widget.base.GLSurfaceView.Companion.RENDERERMODE_CONTINUOUSLY
import com.devyk.common.LogHelper
import com.devyk.common.config.VideoConfiguration
import com.devyk.common.mediacodec.BaseVideoEncoder
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLContext
import kotlin.experimental.and

/**
 * <pre>
 *     author  : devyk on 2020-07-08 18:16
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is H264Encoder
 * </pre>
 */
public class MP4Encoder(path: String, textureId: Int, glcontext: EGLContext?, context: Context?) : BaseVideoEncoder(),
    IGLThreadConfig {

    protected lateinit var mRenderer: EncodeRenderer
    protected var mEGLContext: EGLContext?
    protected var mRendererMode = RENDERERMODE_CONTINUOUSLY
    protected  var mGLThread: EncodeRendererThread?=null
    protected lateinit var mMakeMP4: MakeMP4
    protected var mTrackIndex = -1;
    protected var mSurface: Surface? = null


    init {
        this.mEGLContext = glcontext
        LogHelper.e(TAG, "TextureId:${textureId}")
        this.mRenderer = EncodeRenderer(context, textureId)
        this.mMakeMP4 = MakeMP4(path)
    }


    override fun prepare(videoConfiguration: VideoConfiguration?) {
        super.prepare(videoConfiguration)

    }


    override fun onSurfaceCreate(surface: Surface?) {
        super.onSurfaceCreate(surface)
        mSurface =surface
        mGLThread = EncodeRendererThread(WeakReference(this))
        mGLThread?.run {
            setRendererSize(mConfiguration!!.width, mConfiguration!!.height)
            isCreate = true
            isChange = true
            start()
        }
    }


    override fun start() {
        super.start()

    }

    override fun stop() {
        super.stop()
        mGLThread?.onDestory()
        mMakeMP4.release()
    }

    override fun getSurface(): Surface? {
        if (mSurface != null)
            return mSurface
        return super.getSurface()
    }

    override fun getRenderer(): IRenderer? = mRenderer
    override fun getEGLContext(): EGLContext? = mEGLContext
    override fun getRendererMode(): Int = mRendererMode


    class EncodeRendererThread(weakReference: WeakReference<IGLThreadConfig>):GLThread(weakReference){

    }

    /**
     * 编码完成的 H264 数据
     *   00 00 00 01 06:  SEI信息
     *   00 00 00 01 67:  0x67&0x1f = 0x07 :SPS
     *   00 00 00 01 68:  0x68&0x1f = 0x08 :PPS
     *   00 00 00 01 65:  0x65&0x1f = 0x05: IDR Slice
     */
    override fun onVideoEncode(bb: ByteBuffer?, bi: MediaCodec.BufferInfo) {
        var h264Arrays = ByteArray(bi.size)
        bb?.position(bi.offset)
        bb?.limit(bi.offset + bi.size)
        bb?.get(h264Arrays)
        val tag = h264Arrays[4].and(0x1f).toInt()
        if (tag == 0x07) {//sps
            LogHelper.e(TAG, " SPS " + h264Arrays.size)
            mTrackIndex = mMakeMP4?.start(getOutputFormat())!!
        } else if (tag == 0x08) {//pps
            LogHelper.e(TAG, " PPS ")
        } else if (tag == 0x05) {//关键字帧
            LogHelper.e(TAG, " 关键帧 " + h264Arrays.size)
        } else {
            //普通帧
            LogHelper.e(TAG, " 普通帧 " + h264Arrays.size)
        }
        if (mTrackIndex != -1)
            mMakeMP4.writeSampleData(mTrackIndex, bb!!, bi)

    }






}