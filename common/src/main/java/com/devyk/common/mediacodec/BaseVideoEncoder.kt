package com.devyk.common.mediacodec

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.devyk.common.LogHelper
import com.devyk.common.config.VideoConfiguration
import java.util.concurrent.locks.ReentrantLock

/**
 * <pre>
 *     author  : devyk on 2020-06-15 21:46
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoEncodec
 * </pre>
 */
public abstract class BaseVideoEncoder : IVideoCodec {

    private var mMediaCodec: MediaCodec? = null
    private var mPause: Boolean = false
    private var mHandlerThread: HandlerThread? = null
    private var mEncoderHandler: Handler? = null
    protected var mConfiguration: VideoConfiguration? = null
    private var mBufferInfo: MediaCodec.BufferInfo? = null
    @Volatile
    private var isStarted: Boolean = false
    private val encodeLock = ReentrantLock()
    private lateinit var mSurface: Surface
    public val TAG = this.javaClass.simpleName


    protected var mPts = 0L

    /**
     * 准备硬编码工作
     */
    override fun prepare(videoConfiguration: VideoConfiguration?) {
        videoConfiguration?.run {
            mConfiguration = videoConfiguration
            mMediaCodec = VideoMediaCodec.getVideoMediaCodec(videoConfiguration)
            LogHelper.e(TAG,"prepare success!")
        }
    }

    /**
     * 渲染画面销毁了 open 子类可以重写
     */
    protected open fun onSurfaceDestory(surface: Surface?) {
    }

    /**
     * 可以创建渲染画面了 open 子类可以重写
     */
    protected open fun onSurfaceCreate(surface: Surface?) {

    }


    /**
     * 创建一个输入型的 Surface
     */
    open fun getSurface(): Surface? {
        return mSurface
    }


    /**
     * 开始编码
     */
    override fun start() {
        mHandlerThread = HandlerThread("AVSample-Encode")

        mHandlerThread?.run {
            this.start()
            mEncoderHandler = Handler(getLooper())
            mBufferInfo = MediaCodec.BufferInfo()
            //必须在  mMediaCodec?.start() 之前
            mSurface = mMediaCodec!!.createInputSurface()
            mMediaCodec?.start()
            mEncoderHandler?.post(swapDataRunnable)
            isStarted = true
            //必须在  mMediaCodec?.start() 之后
            onSurfaceCreate(mSurface)
        }
    }

    /**
     * 编码的线程
     */
    private val swapDataRunnable = Runnable { drainEncoder() }


    /**
     * 停止编码
     */
    override fun stop() {
        if (!isStarted)return
        isStarted = false
        mEncoderHandler?.removeCallbacks(swapDataRunnable)
        mHandlerThread?.quit()
        encodeLock.lock()
        //提交一个空的缓冲区
        mMediaCodec?.signalEndOfInputStream()
        releaseEncoder()
        encodeLock.unlock()
    }

    /**
     * 释放编码器
     */
    private fun releaseEncoder() {
        onSurfaceDestory(getSurface())
        mMediaCodec?.stop()
        mMediaCodec?.release()
        mMediaCodec = null
    }


    /**
     * 动态码率设置
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun setRecorderBps(bps: Int) {
        if (mMediaCodec == null) {
            return
        }
        Log.d(TAG, "bps :" + bps * 1024)
        val bitrate = Bundle()
        bitrate.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bps * 1024)
        mMediaCodec?.setParameters(bitrate)
    }

    /**
     * 解码函数
     */
    private fun drainEncoder() {
        val outBuffers = mMediaCodec?.getOutputBuffers()
        if (!isStarted) {
            // if not running anymore, complete stream
            mMediaCodec?.signalEndOfInputStream()
        }
        while (isStarted) {
            encodeLock.lock()
            if (mMediaCodec != null) {
                val outBufferIndex = mMediaCodec?.dequeueOutputBuffer(mBufferInfo!!, 12000)
                if (outBufferIndex!! >= 0) {

                    val bb = outBuffers!![outBufferIndex]

                    if (mPts == 0L)
                        mPts = mBufferInfo!!.presentationTimeUs

                    mBufferInfo!!.presentationTimeUs -= mPts

                    if (!mPause) {
                        onVideoEncode(bb, mBufferInfo!!)
                    }
                    mMediaCodec?.releaseOutputBuffer(outBufferIndex, false)
                } else {
                    try {
                        // wait 10ms
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
                encodeLock.unlock()
            } else {
                encodeLock.unlock()
                break
            }
        }
    }

    /**
     * 获取输出的格式
     */
    public fun getOutputFormat(): MediaFormat? = mMediaCodec?.outputFormat
}