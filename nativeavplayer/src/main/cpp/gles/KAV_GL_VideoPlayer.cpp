//
// Created by 阳坤 on 2020-05-22.
//

#include <AVData.h>
#include "KAV_GL_VideoPlayer.h"

void KAV_GL_VideoPlayer::setRender(void *window) {
    this->pNativeWindow = window;

}

void KAV_GL_VideoPlayer::render(AVData data) {
    if (!pNativeWindow)return;
    if (!texture)
    {
        texture = KAVTxture::create();
        texture ->init(pNativeWindow,(AVTextureType)data.format);
    }
    texture->draw(data.datas,data.width,data.height);
}

void KAV_GL_VideoPlayer::close() {
    mux.lock();
    if (texture) {
        texture->drop();
        texture = 0;
    }
    mux.unlock();

}
