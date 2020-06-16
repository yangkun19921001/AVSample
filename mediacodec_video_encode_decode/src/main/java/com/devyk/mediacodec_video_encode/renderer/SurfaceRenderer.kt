package com.devyk.mediacodec_video_encode.renderer

import android.graphics.Canvas
import android.view.Surface

/**
 * <pre>
 *     author  : devyk on 2020-06-15 22:48
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is SurfaceRenderer 在 surface 绘制
 * </pre>
 */
abstract class SurfaceRenderer(internal var mSurface: Surface) {
    internal var mRenderer: Renderer? = null

    abstract  fun onDraw(canvas: Canvas);

    fun start() {
        if (mRenderer == null) {
            mRenderer = Renderer()
            mRenderer!!.setRunning(true)
            mRenderer!!.start()
        }
    }

    fun stopAndWait() {
        if (mRenderer != null) {
            mRenderer!!.setRunning(false)
            // we want to make sure complete drawing cycle, otherwise
            // unlockCanvasAndPost() will be the one who may or may not throw
            // IllegalStateException
            try {
                mRenderer!!.join()
            } catch (ignore: InterruptedException) {
            }

            mRenderer = null
        }
    }

    internal inner class Renderer : Thread() {

        @Volatile
        var mRunning: Boolean = false

        fun setRunning(running: Boolean) {
            mRunning = running
        }

        override fun run() {
            while (mRunning) {
                val canvas = mSurface.lockCanvas(null)
                try {
                    onDraw(canvas)
                } finally {
                    mSurface.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}