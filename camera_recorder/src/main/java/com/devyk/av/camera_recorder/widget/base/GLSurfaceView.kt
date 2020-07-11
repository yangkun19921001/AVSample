package com.devyk.av.camera_recorder.widget.base

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.devyk.av.camera_recorder.callback.IRenderer
import java.lang.ref.WeakReference
import com.devyk.av.camera_recorder.config.RendererConfiguration
import android.view.Surface
import com.devyk.av.camera_recorder.callback.IGLThreadConfig
import com.devyk.av.camera_recorder.egl.thread.GLThread
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
public open class GLSurfaceView : SurfaceView, SurfaceHolder.Callback, IGLThreadConfig {





    public val TAG = javaClass.simpleName


    /**
     * 渲染模式-默认自动模式
     */
    private var mRendererMode = RENDERERMODE_CONTINUOUSLY

    /**
     * 渲染配置
     */
    private lateinit var mRendererConfiguration: RendererConfiguration

    /**
     * GLES 渲染线程
     */
    private lateinit var mEglThread: GLSurfaceThread


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
        this.mEglThread = GLSurfaceThread(
            WeakReference<IGLThreadConfig>(this)
        )
        this.mEglThread.isCreate = true
        this.mEglThread.start()
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mEglThread.let { eglThread ->
            eglThread.setRendererSize(width, height)
            eglThread.isChange = true
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mEglThread.let {
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
    public override fun getEGLContext(): EGLContext? {
        if (mEGLContext == null)
            return mEglThread.getEGLContext()
        return mEGLContext
    }

    /**
     * 外部请求渲染刷新
     */
    public fun requestRenderer() = mEglThread.requestRenderer()

    /**
     * 得到渲染器
     */
    override fun getRenderer(): IRenderer? = mRenderer

    /**
     * 得到渲染模式
     */
    override fun getRendererMode(): Int = mRendererMode

    /**
     * 得到渲染 Surface
     */
    override fun getSurface(): Surface? = mSurface

    /**
     * 自定义GLThread线程类，主要用于OpenGL的绘制操作
     */
    public class GLSurfaceThread(weakReference: WeakReference<IGLThreadConfig>) : GLThread(weakReference) {

        /**
         * 获取 EGL 上下文环境
         */
        public fun getEGLContext(): EGLContext? = mEGLHelper?.getEglContext()

    }
}


