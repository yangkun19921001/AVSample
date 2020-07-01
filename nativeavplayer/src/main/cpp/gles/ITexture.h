//
// Created by 阳坤 on 2020-05-23.
//

#ifndef IKAVEDIT_KAVTEXTURE_H
#define IKAVEDIT_KAVTEXTURE_H


enum AVTextureType {
    AVTEXTURE_YUV420P = 0,  // Y 4  u 1 v 1
    AVTEXTURE_NV12 = 25,    // Y4   uv1
    AVTEXTURE_NV21 = 26     // Y4   vu1
};

class ITexture {
protected:
    ITexture() {};
public:
    ~ITexture() {};

public:
    /**
     * 静态创建
     * @return
     */
//    virtual static ITexture *create() = 0;

    /**
     * init
     */
    virtual int init(void *window, AVTextureType type = AVTEXTURE_YUV420P) = 0;

    /**
     * 绘制
     */
    virtual void draw(unsigned char *data[], int width, int height) = 0;

    /**
     * 关闭
     */
    virtual void drop() = 0;


};


#endif //IKAVEDIT_KAVTEXTURE_H
