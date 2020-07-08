package com.devyk.av.camerapreview.widget.base

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.devyk.av.camerapreview.callback.IRenderer
import java.lang.ref.WeakReference
import com.devyk.av.camerapreview.config.RendererConfiguration
import com.devyk.av.camerapreview.egl.EglHelper
import android.view.Surface
import com.devyk.common.LogHelper
import java.lang.Exception
import javax.microedition.khronos.egl.EGLContext


/**
 * <pre>
 *     author  : devyk on 2020-07-04 15:36
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is GLSurfaceView
 * </pre>
 *
 *
 * 步骤：
 * 1、继承 SurfaceView 并实现 CallBack 回调
 * 2、自定义 GLThread 线程类，主要用于 OpenGL 绘制工作
 * 3、添加设置 Surface 和 EglContext 的方法
 * 4、提供和系统 GLSurfaceView 相同的调用方法
 *
 */
public open class GLSurfaceView : SurfaceView, SurfaceHolder.Callback {

    public val TAG = javaClass.simpleName


    /**
     * 渲染模式-默认自动模式
     */
    private var mRendererMode = RENDERERMODE_CONTINUOUSLY


    private lateinit var mRendererConfiguration: RendererConfiguration

    private lateinit var mEglThread: GLESThread


    private var mSurface: Surface? = null

    private var mEGLContext: EGLContext? = null


    companion object {
        /**
         * 手动调用渲染
         */
        const val RENDERERMODE_WHEN_DIRTY = 0
        /**
         * 自动渲染
         */
        const val RENDERERMODE_CONTINUOUSLY = 1
    }

    init {
        mRendererConfiguration = RendererConfiguration.createDefault()
    }

    /**
     * 渲染器
     */
    public var mRenderer: IRenderer? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        holder.addCallback(this)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mSurface == null) {
            mSurface = holder.surface
        }
        this.mEglThread = GLESThread(
            WeakReference<GLSurfaceView>(this)
        )
        this.mEglThread.isCreate = true
        this.mEglThread.start()
    }

    public fun getSurface(): Surface? = mSurface

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mEglThread?.let { eglThread ->
            eglThread.setRendererSize(width, height)
            eglThread.isChange = true
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mEglThread?.let {
            mEglThread.onDestory()
        }
    }

    /**
     * 配置渲染属性
     */
    public fun configure(rendererConfiguration: RendererConfiguration) {
        this.mRendererConfiguration = rendererConfiguration
        this.mRenderer = mRendererConfiguration.renderer;
        this.mRendererMode = mRendererConfiguration.rendererMode;
        mRendererConfiguration.eglContext?.let {
            this.mEGLContext = it
        }
    }

    /**
     * 拿到 EGL 上下文
     */
    public fun getEGLContext(): EGLContext? = mEglThread?.getEGLContext()

    /**
     * 外部请求渲染刷新
     */
    public fun requestRenderer() = mEglThread?.requestRenderer()

    /**
     * 自定义GLThread线程类，主要用于OpenGL的绘制操作
     */
    public class GLESThread(weakReference: WeakReference<GLSurfaceView>) : Thread() {

        private var TAG = this.javaClass.simpleName

        /**
         * 管理 GLSurfaceView 避免内存泄漏
         */
        private lateinit var mWeakRerence: WeakReference<GLSurfaceView>

        /**
         * EGL 环境搭建帮助类
         */
        private lateinit var mEGLHelper: EglHelper
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
         * 刷新帧率
         */
        private var mDrawFpsRate = 60L

        /**
         * 渲染的 size
         */
        private var mWidth = 0
        private var mHeight = 0;

        init {
            mWeakRerence = weakReference

        }


        override fun run() {
            super.run()
            //实例化 EGL 环境搭建的帮组类
            mEGLHelper = EglHelper()
            //初始化 EGL
            mWeakRerence?.get()?.let { surfaceView ->
                mEGLHelper.initEgl(surfaceView.getSurface(), surfaceView.mEGLContext)

                while (true) {
                    if (isExit) {
                        release()
                        break
                    }

                    if (isStart) {
                        //判断是手动刷新还是自动 刷新
                        if (surfaceView.mRendererMode == RENDERERMODE_WHEN_DIRTY) {
                            synchronized(mLock) {
                                try {
                                    mLock.wait()
                                } catch (error: InterruptedException) {
                                    LogHelper.e(TAG, error.message)
                                }
                            }

                        } else if (surfaceView.mRendererMode == RENDERERMODE_CONTINUOUSLY) {
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
            if (!isCreate)return
            mWeakRerence?.get()?.let { view ->
                this.isCreate = false
                view.mRenderer?.onSurfaceCreate(width, height)
            }
        }

        /**
         * 渲染器需要改变窗口大小
         */
        private fun onChange(width: Int, height: Int) {
            if (!isChange)return
            mWeakRerence?.get()?.let { view ->
                this.isChange = false
                view.mRenderer?.onSurfaceChange(width, height)
            }
        }


        /**
         * 渲染器可以开始绘制了
         */
        private fun onDraw() {
            mWeakRerence?.get()?.let { view ->
                view.mRenderer?.onDraw()
                if (!isStart)
                    view.mRenderer?.onDraw()

                this.mEGLHelper.swapBuffers()
            }
        }

        /**
         * 手动请求刷新
         */
        public fun requestRenderer() {
            mLock?.let {
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
            mEGLHelper?.let { eglHelper ->
                eglHelper.destoryEgl()
            }
            mWeakRerence.clear()
        }

        /**
         * 获取 EGL 上下文环境
         */
        public fun getEGLContext(): EGLContext? = mEGLHelper?.getEglContext()


    }


}


