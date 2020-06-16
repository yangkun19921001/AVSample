package com.devyk.mediacodec_video_encode.mediacodec

import android.media.MediaCodec
import android.os.Build
import android.util.Log
import com.devyk.mediacodec_video_encode.config.VideoConfiguration
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock

/**
 * <pre>
 *     author  : devyk on 2020-06-16 17:41
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BaseVideoDecoder
 * </pre>
 */
public open class BaseVideoDecoder : ICodec {


    public val TAG = this.javaClass.simpleName

    private var mWorker: Worker? = null


    /**
     * 解码的准备工作，需要配置 spspps mediacodec等一些信息
     */
   public open fun configure(videoConfiguration: VideoConfiguration?) {
        super.prepare(videoConfiguration)
        videoConfiguration?.run {
            mWorker?.configure(this)
        }
    }

    /**
     * 开始解码
     */
    override fun start() {
        mWorker = Worker(this);
        mWorker?.setRunning(true)
        mWorker?.start()
    }

    /**
     * 停止解码
     */
    override fun stop() {
        mWorker?.setRunning(false)
        mWorker = null
    }

    /**
     * 解码完成的数据
     */
    override fun onVideoEncode(bb: ByteBuffer?, mBufferInfo: MediaCodec.BufferInfo) {

    }


    /**
     * 待解码数据送入解码器
     */
    public fun enqueue(byteArray: ByteArray,timeoutUs: Long, flag: Int) {
        mWorker?.enqueue(byteArray, timeoutUs,flag)
    }


    private class Worker(decoder: BaseVideoDecoder) : Thread() {
        private val decodeLock = ReentrantLock()
        @Volatile
        private var isStarted: Boolean = false
        private var mMediaCodec: MediaCodec? = null
        @Volatile
        private var mConfigured: Boolean = false
        private var mConfiguration: VideoConfiguration? = null
        private var baseVideoEncoder: BaseVideoDecoder? = null

        private var TAG = this.javaClass.simpleName

        private var  mTimeoutUs = 10000L
        init {
            baseVideoEncoder = decoder
        }

        public fun configure(videoConfiguration: VideoConfiguration?) {
            videoConfiguration?.run {
                mConfiguration = this
                mMediaCodec = VideoMediaCodec.getVideoMediaCodec(this)
                mMediaCodec?.start()
                mConfigured = true
            }
        }

        fun setRunning(isRuning: Boolean) {
            isStarted = isRuning
        }

        /**
         * 放入编码器
         */
        fun enqueue(data: ByteArray,timeoutUs:Long ,flag: Int) {
            if (mConfigured && isStarted) {
                try {
                    decodeLock.lock()
                    val index = mMediaCodec?.dequeueInputBuffer(timeoutUs)
                    if (index!! >= 0) {
                        val buffer: ByteBuffer?
                        // since API 21 we have new API to use
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            buffer = mMediaCodec?.getInputBuffers()?.get(index)
                            buffer!!.clear()
                        } else {
                            buffer = mMediaCodec?.getInputBuffer(index)
                        }
                        if (buffer != null) {
                            buffer.put(data, 0, data.size)
                            mMediaCodec?.queueInputBuffer(index, 0, data.size, timeoutUs, flag)
                        }
                    }
                    decodeLock.unlock()
                } catch (error:Exception){
                    Log.e(TAG,error.message)
                }
            }
        }

        override fun start() {
            super.start()


        }

        override fun interrupt() {
            super.interrupt()
            isStarted = false
        }

        /**
         * 线程编码
         */
        override fun run() {
            try {
                val info = MediaCodec.BufferInfo()
//                val outBuffers = mMediaCodec?.getOutputBuffers()
                while (isStarted) {
                    if (mConfigured) {
                        val index = mMediaCodec?.dequeueOutputBuffer(info, mTimeoutUs)
                        if (index!! >= 0) {
//                            val byteBuffer = outBuffers!![index]
//                            byteBuffer.position(info.offset)
//                            byteBuffer.limit(info.offset + info.size)
//                            val byteArray = ByteArray(info.size)
//                            byteBuffer.get(byteArray)
//                            baseVideoEncoder?.onVideoEncode(byteBuffer, info)
//                             setting true is telling system to render frame onto Surface
                            mMediaCodec?.releaseOutputBuffer(index, true)
                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                break
                            }
                        }
                        Thread.sleep(10)
                    } else {
                        // just waiting to be configured, then decode and render
                        try {
                            Thread.sleep(10)
                        } catch (ignore: InterruptedException) {
                        }

                    }
                }
                release()
            } finally {
                release()
            }
        }

        public  fun release() {
            if (mConfigured && !isStarted) {
                mMediaCodec?.stop()
                mMediaCodec?.release()
                mConfigured = false
                Log.e(TAG,"释放编解码器")
            }
        }


    }
}