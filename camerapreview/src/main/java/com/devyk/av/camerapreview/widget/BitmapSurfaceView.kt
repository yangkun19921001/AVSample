package com.devyk.av.camerapreview.widget

import android.content.Context
import android.util.AttributeSet
import com.devyk.av.camerapreview.config.RendererConfiguration
import com.devyk.av.camerapreview.egl.renderer.BitmapRenderer
import com.devyk.av.camerapreview.widget.base.GLSurfaceView


/**
 * <pre>
 *     author  : devyk on 2020-07-06 22:13
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BitmapSurface 绘制一张图片
 * </pre>
 */
public class BitmapSurfaceView : GLSurfaceView {

    private var mBitmapRenderer: BitmapRenderer? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mBitmapRenderer = BitmapRenderer(context!!)
        configure(RendererConfiguration.Builder().setRenderer(mBitmapRenderer!!).setRendererMode(RENDERERMODE_WHEN_DIRTY).build())
    }

    public override fun getRenderer(): BitmapRenderer? = mBitmapRenderer
}