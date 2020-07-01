//
// Created by 阳坤 on 2020-06-29.
//

#ifndef AVSAMPLE_GLESPLAYER_H
#define AVSAMPLE_GLESPLAYER_H


#include <AVData.h>
#include "IVideoPlayer.h"

class GLESPlayer {

private:
    IVideoPlayer *videoView = 0;


public:
    int initView(void *nativeWindow);
    void enqueue(AVData data);
    void release();

};


#endif //AVSAMPLE_GLESPLAYER_H
