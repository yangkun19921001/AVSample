//
// Created by 阳坤 on 2020-05-23.
//

#ifndef IKAVEDIT_KAVTXTURE_H
#define IKAVEDIT_KAVTXTURE_H


#include <mutex>
#include "ITexture.h"
#include "AVShader.h"
#include "KAVEGL.h"

class KAVTxture : public ITexture {

public:
    /**
     * 着色器
     */
    AVShader sh;
    /**
     * 渲染的格式类型
     */
    AVTextureType type;
    /**
     * 互斥锁
     */
    std::mutex mux;

    IEGL *iegl;

public:
    /**
     * 静态创建
     * @return
     */
     static ITexture *create();

    /**
     * init
     */
    virtual int init(void *window, AVTextureType type = AVTEXTURE_YUV420P);

    /**
     * 绘制
     */
    virtual void draw(unsigned char *data[], int width, int height);

    /**
     * 关闭
     */
    virtual void drop();


};



#endif //IKAVEDIT_KAVTXTURE_H
