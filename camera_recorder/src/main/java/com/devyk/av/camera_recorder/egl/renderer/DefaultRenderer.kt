package com.devyk.av.camera_recorder.egl.renderer

import android.opengl.GLES20
import com.devyk.av.camera_recorder.callback.IRenderer

/**
 * <pre>
 *     author  : devyk on 2020-07-06 11:57
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is DefaultRenderer 渲染
 * </pre>
 */
public class DefaultRenderer : IRenderer {
    override fun onSurfaceCreate(width: Int, height: Int) {
    }

    override fun onSurfaceChange(width: Int, height: Int) {
        //清屏
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 1f, 0f, 1f)
    }
}