//
// Created by 阳坤 on 2020-05-21.
//


#include "AVData.h"

bool AVData::alloc(int size, const char *data) {
    drop();
    type = UCHAR_TYPE;
    if (size <= 0)return false;
    this->data = new unsigned char[size];
    if (!this->data) return false;
    if (data) {
        memcpy(this->data, data, size);
    }
    this->size = size;
    return false;
}

void AVData::drop() {
    if(!data) return;
    if(type == AVPACKET_TYPE)
        free(data);
    else
        delete data;
    data = 0;
    size = 0;
}
