//
// Created by 阳坤 on 2020-05-23.
//

#include "KAVEGL.h"

IEGL *KAVEGL::get() {
    static KAVEGL elg;
    return &elg;
}

void KAVEGL::draw() {
    mux.lock();
    if(display == EGL_NO_DISPLAY || surface == EGL_NO_SURFACE)
    {
        mux.unlock();
        return;
    }
    eglSwapBuffers(display,surface);
    mux.unlock();

}

void KAVEGL::close() {
    mux.lock();
    if(display == EGL_NO_DISPLAY)
    {
        mux.unlock();
        return;
    }
    eglMakeCurrent(display,EGL_NO_SURFACE,EGL_NO_SURFACE,EGL_NO_CONTEXT);

    if(surface != EGL_NO_SURFACE)
        eglDestroySurface(display,surface);
    if(context != EGL_NO_CONTEXT)
        eglDestroyContext(display,context);

    eglTerminate(display);

    display = EGL_NO_DISPLAY;
    surface = EGL_NO_SURFACE;
    context = EGL_NO_CONTEXT;
    mux.unlock();
}

bool KAVEGL::init(void *window) {
    ANativeWindow *nativeWindow = static_cast<ANativeWindow *>(window);
    close();
    mux.lock();
    //1 获取EGLDisplay对象 显示设备
    display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if(display == EGL_NO_DISPLAY)
    {
        mux.unlock();
        LOGE("eglGetDisplay failed!");
        return false;
    }
    LOGE("eglGetDisplay success!");
    //2 初始化 EGS Display
    if(EGL_TRUE != eglInitialize(display,0,0))
    {
        mux.unlock();
        LOGE("eglInitialize failed!");
        return false;
    }
    LOGE("eglInitialize success!");

    //3 获取配置并创建surface
    EGLint configSpec [] = {//可以理解为窗口的配置
            EGL_RED_SIZE,8, // 8bit R
            EGL_GREEN_SIZE,8,// 8bit G
            EGL_BLUE_SIZE,8,// 8bit B
            EGL_SURFACE_TYPE,EGL_WINDOW_BIT,
            EGL_NONE
    };
    EGLConfig config = 0;//输出配置
    EGLint numConfigs = 0;//
    if(EGL_TRUE != eglChooseConfig(display,configSpec,&config,1,&numConfigs))
    {
        mux.unlock();
        LOGE("eglChooseConfig failed!");
        return false;
    }
    LOGE("eglChooseConfig success!");
    //关联 NativeWindow
    surface = eglCreateWindowSurface(display,config,nativeWindow,NULL);


    //4 创建并打开 EGL 上下文
    const EGLint ctxAttr[] = { EGL_CONTEXT_CLIENT_VERSION ,2, EGL_NONE};
    context = eglCreateContext(display,config,EGL_NO_CONTEXT,ctxAttr);
    if(context == EGL_NO_CONTEXT)
    {
        mux.unlock();
        LOGE("eglCreateContext failed!");
        return false;
    }

    LOGE("eglCreateContext success!");
    //真正关联
    if(EGL_TRUE != eglMakeCurrent(display,surface,surface,context))
    {
        mux.unlock();
        LOGE("eglMakeCurrent failed!");
        return false;
    }
    LOGE("eglMakeCurrent success!");
    mux.unlock();
    
    return true;
}
