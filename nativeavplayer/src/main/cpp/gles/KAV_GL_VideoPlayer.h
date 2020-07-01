//
// Created by 阳坤 on 2020-05-22.
//

#ifndef IKAVEDIT_KAV_GL_VIDEOPLAYER_H
#define IKAVEDIT_KAV_GL_VIDEOPLAYER_H


#include "IVideoPlayer.h"
#include "ITexture.h"
#include "KAVTxture.h"

/**
 * 具体视频模块播放
 */
class KAV_GL_VideoPlayer : public IVideoPlayer {

protected:
    /**
     * native window
     */
    void *pNativeWindow;

    ITexture *texture;

    /**
     * 互斥锁
     */
    std::mutex mux;
public:
    /**
     * 设置渲染的 window
     * @param window
     */
    virtual void setRender(void *window);

    /**
     * 渲染数据
     */
     virtual void render(AVData data);

     /**
      * 关闭
      */
      virtual void close();

};


#endif //IKAVEDIT_KAV_GL_VIDEOPLAYER_H
