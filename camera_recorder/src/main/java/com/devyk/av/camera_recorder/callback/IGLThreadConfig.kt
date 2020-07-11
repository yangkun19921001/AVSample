package com.devyk.av.camera_recorder.callback

import android.view.Surface
import javax.microedition.khronos.egl.EGLContext

/**
 * <pre>
 *     author  : devyk on 2020-07-08 20:51
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IGLThreadConfig ELThread 需要的配置
 * </pre>
 */
public interface IGLThreadConfig {
    /**
     * 拿到渲染器
     */
    fun getRenderer(): IRenderer?

    /**
     * 拿到渲染的 Surface
     */
    fun getSurface(): Surface?

    /**
     * 拿到 EGL 环境的上下文
     */
    fun getEGLContext(): EGLContext?

    /**
     * 拿到渲染模式
     */
    fun getRendererMode(): Int


}