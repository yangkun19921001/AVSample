package com.devyk.av.camera_recorder.egl

import android.opengl.EGL14
import android.view.Surface
import javax.microedition.khronos.egl.*

/**
 * <pre>
 *     author  : devyk on 2020-07-04 14:49
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is EglHelper EGL 环境
 * </pre>
 */


class EglHelper {

    private var mEgl: EGL10? = null
    private var mEglDisplay: EGLDisplay? = null
    private var mEglContext: EGLContext? = null
    private var mEglSurface: EGLSurface? = null

    /**
     * 初始化 EGL
     * @param surface
     * @param eglContext
     */
    fun initEgl(surface: Surface?, eglContext: EGLContext?) {

        //1、得到 EGL 实例
        mEgl = EGLContext.getEGL() as EGL10

        //2、得到默认的显示设备
        mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed")
        }

        //3、初始化默认的显示设备
        val version = IntArray(2)
        if (!mEgl!!.eglInitialize(mEglDisplay, version)) {
            throw RuntimeException("eglInitialize failed")
        }

        //4、设置显示设备的属性
        val attrbutes = intArrayOf(
            EGL10.EGL_RED_SIZE,
            8,
            EGL10.EGL_GREEN_SIZE,
            8,
            EGL10.EGL_BLUE_SIZE,
            8,
            EGL10.EGL_ALPHA_SIZE,
            8,
            EGL10.EGL_DEPTH_SIZE,
            8,
            EGL10.EGL_STENCIL_SIZE,
            8,
            EGL10.EGL_RENDERABLE_TYPE,
            4,
            EGL10.EGL_NONE
        )


        val num_config = IntArray(1)
        require(mEgl!!.eglChooseConfig(mEglDisplay, attrbutes, null, 1, num_config)) { "eglChooseConfig failed" }

        val numConfigs = num_config[0]
        require(numConfigs > 0) { "No configs match configSpec" }

        //5、重系统从获取对应属性的配置
        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        require(
            mEgl!!.eglChooseConfig(
                mEglDisplay, attrbutes, configs, numConfigs,
                num_config
            )
        ) { "eglChooseConfig#2 failed" }

        //6、创建爱你 EGL 上下文环境

        val attrib_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)

        if (eglContext != null) {
            mEglContext = mEgl!!.eglCreateContext(mEglDisplay, configs[0], eglContext, attrib_list)
        } else {
            mEglContext = mEgl!!.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attrib_list)
        }

        //7、创建渲染的 Surface
        mEglSurface = mEgl!!.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null)

        //8、绑定 EglContext 和 Surface 到显示设备中
        if (!mEgl!!.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw RuntimeException("eglMakeCurrent fail")
        }
    }

    fun swapBuffers() {
        //9. 刷新数据，显示渲染场景
        mEgl?.eglSwapBuffers(mEglDisplay, mEglSurface)
    }

    fun getEglContext(): EGLContext? {
        return mEglContext
    }

    /**
     * 销毁
     */
    fun destoryEgl() {
        if (mEgl != null) {
            mEgl!!.eglMakeCurrent(
                mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT
            )

            mEgl!!.eglDestroySurface(mEglDisplay, mEglSurface)
            mEglSurface = null

            mEgl!!.eglDestroyContext(mEglDisplay, mEglContext)
            mEglContext = null

            mEgl!!.eglTerminate(mEglDisplay)
            mEglDisplay = null
            mEgl = null
        }
    }

}
