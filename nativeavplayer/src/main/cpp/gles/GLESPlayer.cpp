//
// Created by 阳坤 on 2020-06-29.
//

#include "GLESPlayer.h"
#include "KAV_GL_VideoPlayer.h"

void GLESPlayer::release() {
    if (videoView)
        videoView->close();
}

int GLESPlayer::initView(void *nativeWindow) {
    videoView = new KAV_GL_VideoPlayer();
    if (videoView) {
        videoView->close();
        videoView->setRender(nativeWindow);
    }
    return 1;
}

void GLESPlayer::enqueue(AVData data) {
    if (videoView) {
        videoView->update(data);
    }
}




