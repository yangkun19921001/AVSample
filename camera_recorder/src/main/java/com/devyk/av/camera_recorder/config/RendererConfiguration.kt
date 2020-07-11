package com.devyk.av.camera_recorder.config

import android.view.Surface
import com.devyk.av.camera_recorder.annotation.RendererMode
import com.devyk.av.camera_recorder.callback.IRenderer
import com.devyk.av.camera_recorder.egl.renderer.DefaultRenderer
import com.devyk.av.camera_recorder.widget.base.GLSurfaceView
import javax.microedition.khronos.egl.EGLContext

/**
 * <pre>
 *     author  : devyk on 2020-07-06 11:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is RendererConfiguration
 * </pre>
 */
public class RendererConfiguration private constructor(builder: Builder) {


    val renderer: IRenderer

    val rendererMode: Int

    val surface: Surface? = null

    var eglContext: EGLContext? = null

    val width: Int

    val height: Int


    init {
        renderer = builder.renderer
        rendererMode = builder.rendererMode
        width = builder.width
        height = builder.height
        builder.eglContext?.let {
            eglContext = it
        }
    }


    class Builder {
        var renderer: IRenderer = DEFAULT_RENDERER

        var rendererMode: Int = DEFAULT_RENDERERMODE

        var surface: Surface? = null

        var eglContext: EGLContext? = null

        var width: Int = DEFAULT_WIDTH

        var height: Int = DEFAULT_HEIGHT


        /**
         * 设置渲染器
         */
        fun setRenderer(renderer: IRenderer): Builder {
            this.renderer = renderer;
            return this
        }

        /**
         * 设置渲染模式
         */
        fun setRendererMode(@RendererMode mode: Int): Builder {
            this.rendererMode = mode
            return this
        }

        /**
         * 设置显示的 Surface
         */
        fun setSurface(surface: Surface): Builder {
            this.surface = surface
            return this
        }

        /**
         * 设置 EGL 上下文
         */
        fun setEGLContext(context: EGLContext?): Builder {
            this.eglContext = context
            return this;
        }

        /**
         * 设置窗口大小
         */
        fun setSize(width: Int, height: Int): Builder {
            this.width = width
            this.height = height
            return this
        }

        /**
         * 构建配置
         */
        fun build(): RendererConfiguration {
            return RendererConfiguration(this)
        }


    }


    companion object {
        val DEFAULT_RENDERER = DefaultRenderer()
        val DEFAULT_RENDERERMODE = GLSurfaceView.RENDERERMODE_CONTINUOUSLY
        val DEFAULT_WIDTH = 720
        val DEFAULT_HEIGHT = 1280


        fun createDefault(): RendererConfiguration {
            return Builder().build()
        }
    }

}