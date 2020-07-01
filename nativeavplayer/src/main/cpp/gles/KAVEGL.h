//
// Created by 阳坤 on 2020-05-23.
//

#ifndef IKAVEDIT_KAVEGL_H
#define IKAVEDIT_KAVEGL_H


#include "IEGL.h"
#include <android/native_window.h>
#include <EGL/egl.h>
#include "common.h"
#include <mutex>

class KAVEGL : public IEGL {
public:
public:
    EGLDisplay display = EGL_NO_DISPLAY;
    EGLSurface surface = EGL_NO_SURFACE;
    EGLContext context = EGL_NO_CONTEXT;
    std::mutex mux;
public:
    virtual bool init(void *win);

    virtual void close();

    virtual void draw();

     static IEGL *get();

};


#endif //IKAVEDIT_KAVEGL_H
