package com.devyk.av.camera_recorder.widget

import android.content.Context
import android.util.AttributeSet
import com.devyk.av.camera_recorder.config.RendererConfiguration
import com.devyk.av.camera_recorder.egl.renderer.MultiRenderer
import com.devyk.av.camera_recorder.widget.base.GLSurfaceView
import javax.microedition.khronos.egl.EGLContext


/**
 * <pre>
 *     author  : devyk on 2020-07-06 22:13
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BitmapSurface 绘制一张图片
 * </pre>
 */
public class MultiSurfaceView : GLSurfaceView {

    private lateinit var renderer: MultiRenderer


    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    public fun config(eglContext: EGLContext?) {
        renderer = MultiRenderer(context!!)
        configure(
            RendererConfiguration.Builder().setRenderer(renderer).setEGLContext(eglContext).setRendererMode(
                RENDERERMODE_WHEN_DIRTY
            ).build()
        )
    }

    /**
     * 根据纹理来绘制
     */
    public fun setTextureId(id: Int, index: Int) {
        renderer?.setTextureId(id, index)
    }
}