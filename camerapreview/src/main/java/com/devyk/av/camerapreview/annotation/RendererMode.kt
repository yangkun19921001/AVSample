package com.devyk.av.camerapreview.annotation

import androidx.annotation.IntDef
import com.devyk.av.camerapreview.widget.base.GLSurfaceView
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * <pre>
 *     author  : devyk on 2020-07-06 11:35
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is RendererMode 用于 OpenGL ES 渲染模式  @link com.devyk.av.camerapreview.widget.base.GLSurfaceView
 * </pre>
 */
@IntDef(GLSurfaceView.RENDERERMODE_WHEN_DIRTY, GLSurfaceView.RENDERERMODE_CONTINUOUSLY)
@Target(AnnotationTarget.VALUE_PARAMETER) //用于参数上
@Retention(RetentionPolicy.SOURCE) //编译器
annotation class RendererMode {}