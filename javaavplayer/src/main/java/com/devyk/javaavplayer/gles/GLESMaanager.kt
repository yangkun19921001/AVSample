package com.devyk.javaavplayer.gles

import android.content.Context
import java.nio.ByteBuffer.allocateDirect
import android.view.ViewGroup
import java.nio.ByteBuffer


/**
 * <pre>
 *     author  : devyk on 2020-06-27 23:30
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is GLESMaanager
 * </pre>
 */
class GLESMaanager private constructor() {
    /**
     * 预览宽
     */
    private var previewWidth: Int = 0
    /**
     * 预览高
     */
    private var previewHeight: Int = 0
    /**
     * 视频预览的控件
     */
    private var bindPlayControl: ViewGroup? = null
    /**
     * 播放控件
     */
    private var videoConsumerGLPreview: VideoConsumerGLPreview? = null
    /**
     * 是否开始渲染
     */
    private var isRequestRender: Boolean = false
    /**
     * 上下文
     */
    private var context: Context? = null
    private var mByteBuffer: ByteBuffer? = null

    class PlayManagerBuilder {
        private var previewWidth = -1
        private var previewHeight = -1
        private var isRequestRender: Boolean = false
        private var bindPlayControl: ViewGroup? = null

        fun withPreviewWidth(previewWidth: Int,previewHeight: Int): PlayManagerBuilder {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            return this
        }

        fun withRequestRender(requestRender: Boolean): PlayManagerBuilder {
            this.isRequestRender = requestRender
            return this
        }


        fun bindPlayControl(bindPlayControl: ViewGroup): PlayManagerBuilder {
            this.bindPlayControl = bindPlayControl
            return this
        }


        fun build(context: Context): GLESMaanager {
            val playYUVUtils = GLESMaanager()
            playYUVUtils.previewWidth = this.previewWidth
            playYUVUtils.previewHeight = this.previewHeight
            playYUVUtils.isRequestRender = this.isRequestRender
            playYUVUtils.bindPlayControl = this.bindPlayControl
            playYUVUtils.context = context.getApplicationContext()

            return playYUVUtils
        }

        companion object {

            fun aPlayManager(): PlayManagerBuilder {
                return PlayManagerBuilder()
            }
        }
    }

    fun initPlayControl() {
        checkControl()
        if (previewWidth == -1 || previewHeight == -1)
            throw RuntimeException("previewWidth or previewHeight is init ?")
        if (videoConsumerGLPreview != null) return
        videoConsumerGLPreview = VideoConsumerGLPreview(context!!, true, null, previewWidth, previewHeight)
        bindPlayControl!!.addView(videoConsumerGLPreview)
        mByteBuffer = ByteBuffer.allocateDirect(previewWidth * previewHeight * 3 / 2)
    }

    private fun checkControl() {
        if (bindPlayControl == null || context == null)
            throw NullPointerException("Context or bindPlayControl is null ？")
    }

    /**
     * 开始播放
     *
     * @param i420
     */
    fun enqueue(i420: ByteArray) {
        checkControl()
        mByteBuffer!!.rewind()
        mByteBuffer!!.put(i420)
        videoConsumerGLPreview!!.setBuffer(mByteBuffer!!, previewWidth, previewHeight)
        videoConsumerGLPreview!!.setIsRequestRender(true)
        videoConsumerGLPreview!!.requestRender()
    }

    /**
     * 删除播放 YUV 的控件
     */
    fun removePlayControl() {
        checkControl()
        bindPlayControl!!.removeView(videoConsumerGLPreview)
    }

    /**
     * 销毁
     */
    fun onDestory() {
        removePlayControl()
        videoConsumerGLPreview!!.setIsRequestRender(false)
    }


}
