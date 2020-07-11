package com.devyk.av.camera_recorder.egl.thread

import com.devyk.av.camera_recorder.callback.IGLThreadConfig
import com.devyk.av.camera_recorder.egl.EglHelper
import com.devyk.av.camera_recorder.widget.base.GLSurfaceView
import com.devyk.common.LogHelper
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * <pre>
 *     author  : devyk on 2020-07-08 21:03
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is GLThread 自定义 GLThread 线程类，主要用于 OpenGL 的绘制操作
 * </pre>
 */
public open class GLThread(weakReference: WeakReference<IGLThreadConfig>) : Thread() {

    private var TAG = this.javaClass.simpleName

    /**
     * 避免内存泄漏
     */
    private lateinit var mWeakRerence: WeakReference<IGLThreadConfig>

    /**
     * EGL 环境搭建帮助类
     */
    protected lateinit var mEGLHelper: EglHelper
    /**
     * 对象锁
     */
    private val mLock = java.lang.Object()

    /**
     * 是否退出线程
     */
    private var isExit = false;

    /**
     * 是否创建线程
     */
    internal var isCreate = false

    /**
     * 窗口是否改变
     */
    internal var isChange = false

    /**
     * 是否开始渲染
     */
    private var isStart = false

    /**
     * 是否停止
     */
    private var isPause = false

    /**
     * 刷新帧率
     */
    private var mDrawFpsRate = 60L

    /**
     * 渲染的 size
     */
    private var mWidth = 1080
    private var mHeight = 1920;

    init {
        mWeakRerence = weakReference
    }


    override fun run() {
        super.run()
        //实例化 EGL 环境搭建的帮组类
        mEGLHelper = EglHelper()
        //初始化 EGL
        mWeakRerence?.get()?.let { thread ->
            mEGLHelper.initEgl(thread.getSurface(), thread.getEGLContext())

            while (true) {
                if (isExit) {
                    release()
                    break
                }

                if (isPause) {
                    try {
                        sleep(500)
                        continue
                    } catch (error: InterruptedException) {
                        LogHelper.e(TAG, error.message)
                    }
                }
                if (isStart) {
                    //判断是手动刷新还是自动 刷新
                    if (thread.getRendererMode() == GLSurfaceView.RENDERERMODE_WHEN_DIRTY) {
                        synchronized(mLock) {
                            try {
                                mLock.wait()
                            } catch (error: InterruptedException) {
                                LogHelper.e(TAG, error.message)
                            }
                        }

                    } else if (thread.getRendererMode() == GLSurfaceView.RENDERERMODE_CONTINUOUSLY) {
                        try {
                            sleep(1000 / mDrawFpsRate)
                        } catch (error: InterruptedException) {
                            LogHelper.e(TAG, error.message)
                        }
                    } else {
                        throw RuntimeException("mRendererMode is wrong value");
                    }
                }
                //开始创建
                onCreate(mWidth, mHeight)
                //改变窗口
                onChange(mWidth, mHeight)
                //开始绘制
                onDraw()
                this.isStart = true
            }
        }
    }


    /**
     * 渲染窗口的大小
     */
    public fun setRendererSize(width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
    }

    /**
     * 渲染器可以创建了
     */
    private fun onCreate(width: Int, height: Int) {
        if (!isCreate) return
        mWeakRerence.get()?.let { view ->
            this.isCreate = false
            view.getRenderer()?.onSurfaceCreate(width, height)
        }
    }

    /**
     * 渲染器需要改变窗口大小
     */
    private fun onChange(width: Int, height: Int) {
        if (!isChange) return
        mWeakRerence.get()?.let { view ->
            this.isChange = false
            view.getRenderer()?.onSurfaceChange(width, height)
        }
    }


    /**
     * 停止渲染
     */
    public fun setPause() {
        isPause = true
    }

    /**
     * 恢复渲染
     */
    public fun setResume() {
        isPause = false
    }


    /**
     * 渲染器可以开始绘制了
     */
    private fun onDraw() {
        mWeakRerence.get()?.let { view ->
            view.getRenderer()?.onDraw()
            if (!isStart)
                view.getRenderer()?.onDraw()

            this.mEGLHelper.swapBuffers()
        }
    }

    /**
     * 手动请求刷新
     */
    public fun requestRenderer() {
        mLock.let {
            synchronized(mLock) {
                try {
                    mLock.notifyAll()
                } catch (error: Exception) {
                    LogHelper.e(TAG, error.message)
                }
            }
        }
    }


    /**
     * 销毁的时候调用
     */
    public fun onDestory() {
        this.isExit = true
        //避免线程睡眠这里重新刷新一次
        requestRenderer()
    }


    /**
     * 释放资源
     */
    private fun release() {
        mEGLHelper.let { eglHelper ->
            eglHelper.destoryEgl()
        }
        mWeakRerence.clear()
    }

}