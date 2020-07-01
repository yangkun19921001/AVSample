//
// Created by 阳坤 on 2020-05-23.
//

#ifndef IKAVEDIT_AVSHADER_H
#define IKAVEDIT_AVSHADER_H


#include <mutex>
#include <GLES2/gl2.h>
#include "common.h"

enum AVShaderType
{
    AVSHADER_YUV420P = 0,    //软解码和虚拟机
    AVSHADER_NV12 = 25,      //手机
    AVSHADER_NV21 = 26
};

class AVShader {

protected:
    unsigned int vsh = 0;
    unsigned int fsh = 0;
    unsigned int program = 0;
    unsigned int texts[100] = {0};
    std::mutex mux;

public:
    virtual bool init(AVShaderType type=AVSHADER_YUV420P);
    virtual void close();

    //获取材质并映射到内存
    virtual void getTexture(unsigned int index,int width,int height, unsigned char *buf,bool isa=false);
    virtual void draw();

    static GLuint initShader(const char *code,GLint type);
};


#endif //IKAVEDIT_AVSHADER_H
