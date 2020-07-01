//
// Created by 阳坤 on 2020-05-22.
//

#include <AVData.h>
#include "IVideoPlayer.h"

void IVideoPlayer::update(AVData data) {
    //渲染数据
    this->render(data);
}
