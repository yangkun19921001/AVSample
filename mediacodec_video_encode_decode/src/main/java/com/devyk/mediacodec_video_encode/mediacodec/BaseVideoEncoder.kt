package com.devyk.mediacodec_video_encode.mediacodec

import android.annotation.TargetApi
import android.hardware.Camera
import android.media.MediaCodec
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.devyk.mediacodec_video_encode.config.VideoConfiguration
import java.nio.ByteBuffer
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
public abstract class BaseVideoEncoder : ICodec {

    private var mMediaCodec: MediaCodec? = null
    private var mPause: Boolean = false
    private var mHandlerThread: HandlerThread? = null
    private var mEncoderHandler: Handler? = null
    private var mConfiguration: VideoConfiguration? = null
    private var mBufferInfo: MediaCodec.BufferInfo? = null
    @Volatile
    private var isStarted: Boolean = false
    private val encodeLock = ReentrantLock()
    private var mSurface : Surface? = null
    public val TAG = this.javaClass.simpleName

    /**
     * 准备硬编码工作
     */
    override fun prepare(videoConfiguration: VideoConfiguration?) {
        videoConfiguration?.run {
            mConfiguration = videoConfiguration
            mMediaCodec = VideoMediaCodec.getVideoMediaCodec(videoConfiguration)
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
    public fun getSurface(): Surface? {
        return  mSurface
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
            mSurface =  mMediaCodec?.createInputSurface()
            mMediaCodec?.start()
            mEncoderHandler?.post(swapDataRunnable)
            isStarted = true
            //必须在  mMediaCodec?.start() 之后
            onSurfaceCreate(getSurface())
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
        isStarted = false
        mEncoderHandler?.removeCallbacks(null)
        mHandlerThread?.quit()
        encodeLock.lock()
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
}