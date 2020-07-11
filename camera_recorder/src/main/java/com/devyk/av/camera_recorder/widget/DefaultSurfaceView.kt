package com.devyk.av.camera_recorder.widget

import android.content.Context
import android.util.AttributeSet
import com.devyk.av.camera_recorder.config.RendererConfiguration
import com.devyk.av.camera_recorder.egl.renderer.DefaultRenderer
import com.devyk.av.camera_recorder.widget.base.GLSurfaceView

/**
 * <pre>
 *     author  : devyk on 2020-07-06 15:59
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is DefaultSurfaceView
 * </pre>
 */
public class DefaultSurfaceView : GLSurfaceView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        configure(RendererConfiguration.Builder().setRenderer(DefaultRenderer()).setRendererMode(RENDERERMODE_WHEN_DIRTY).build())
    }
}