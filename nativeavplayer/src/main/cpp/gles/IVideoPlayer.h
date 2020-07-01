//
// Created by 阳坤 on 2020-05-22.
//

#ifndef IKAVEDIT_IVIDEOPLAYER_H
#define IKAVEDIT_IVIDEOPLAYER_H



/**
 * 抽象视频播放
 */
class IVideoPlayer  {


public:
    /**
     * 设置 render
     */
    virtual void setRender(void *surface) = 0;

    /**
     * 渲染的数据
     */
     virtual void render(AVData data) = 0;

     /**
      * 有新的数据更新
      */
      virtual void update(AVData data);

      /**
       * 关闭
       */
       virtual void close() = 0;

};


#endif //IKAVEDIT_IVIDEOPLAYER_H
