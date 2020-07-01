//
// Created by 阳坤 on 2020-06-29.
//

#include <stdint.h>
#include <jni.h>
#include "SLESPlayer.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <ITexture.h>
#include <mutex>
#include "GLESPlayer.h"

#define NATIVE_PATH  "com/devyk/nativeavplayer/NativeAPI"

SLESPlayer *mAudioPlayer = 0;
GLESPlayer *mViewPlayer = 0;
/**
 * 互斥锁
 */
std::mutex mux;

/**
 * 准备初始化
 * @param sampleRate
 * @param channels
 * @param sampleFormat
 * @return
 */
int Android_JNI_prepare(JNIEnv *jniEnv, jobject jobject1, int sampleRate, int channels, int sampleFormat) {
    if (!mAudioPlayer)
        mAudioPlayer = new SLESPlayer();
    return mAudioPlayer->prepare(sampleRate, channels, sampleFormat);
};


void Android_JNI_play(JNIEnv *jniEnv, jobject jobject1) {
    if (mAudioPlayer)
        mAudioPlayer->play();
};

void Android_JNI_pause(JNIEnv *jniEnv, jobject jobject1) {
    if (mAudioPlayer)
        mAudioPlayer->pause();
};

/**
 * PCM 数据
 * @param data
 * @return
 */
int Android_JNI_enqueuePCM(JNIEnv *jniEnv, jobject jobject1, jbyteArray data) {
    jbyte *jbyte1 = jniEnv->GetByteArrayElements(data, 0);
    if (mAudioPlayer) {
        mAudioPlayer->enqueue(reinterpret_cast<uint8_t *>(jbyte1), jniEnv->GetArrayLength(data));
    }
    jniEnv->ReleaseByteArrayElements(data, jbyte1, 0);
    return 1;

};

/**
 * 释放
 */
void Android_JNI_release(JNIEnv *jniEnv, jobject jobject1) {
    if (mAudioPlayer)
        mAudioPlayer->release();

    if (mViewPlayer)
        mViewPlayer->release();

};


void Android_JNI_initNativeWindow(JNIEnv *jniEnv, jobject jobject1, jobject surface) {
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(jniEnv, surface);
    if (!mViewPlayer) {
        mViewPlayer = new GLESPlayer();
    }
    mViewPlayer->initView(nativeWindow);
}

void Android_JNI_enqueueYUV(JNIEnv *jniEnv, jobject jobject1, jbyteArray data, int w, int h) {
    AVData avData;
    jbyte *yuv420p = jniEnv->GetByteArrayElements(data, 0);
    if (mViewPlayer) {
        mux.lock();
        avData.width = w;
        avData.height = h;
        avData.isAudio = 0;
        avData.format = AVTEXTURE_YUV420P;
        avData.size = jniEnv->GetArrayLength(data);

        //copy Y 数据
//        memcpy(this->pIc_in->img.plane[0], packet->data, this->mYSize);
//        switch (packet->type){
//            case YUV420p:
//                //copy U 数据
//                memcpy(this->pIc_in->img.plane[1], packet->data + this->mYSize, this->mUVSize);
//                //copy V 数据 packet->data + this->mYSize + this->mUVSize == packet->data + this->mYSize*5/4
//                memcpy(this->pIc_in->img.plane[2], packet->data + this->mYSize + this->mUVSize, this->mUVSize);
        int ysize = w * h;
        int uvsize = ysize / 4;
        avData.datas[0] = static_cast<unsigned char *>(malloc(ysize));
        avData.datas[1] = static_cast<unsigned char *>(malloc(uvsize));
        avData.datas[2] = static_cast<unsigned char *>(malloc(uvsize));
        memcpy(avData.datas[0], yuv420p, ysize);
        memcpy(avData.datas[1], yuv420p + ysize, uvsize);
        memcpy(avData.datas[2], yuv420p + ysize * 5 / 4, uvsize);
        mViewPlayer->enqueue(avData);
        mux.unlock();
    }
    jniEnv->ReleaseByteArrayElements(data, yuv420p, 0);
}


JNINativeMethod mNATIVE_METHODS[] = {
        {"prepare",          "(III)I",                (void *) Android_JNI_prepare},
        {"play",             "()V",                   (void *) Android_JNI_play},
        {"pause",            "()V",                   (void *) Android_JNI_pause},
        {"enqueue",          "([B)V",                 (void *) Android_JNI_enqueuePCM},
        {"release",          "()V",                   (void *) Android_JNI_release},

        {"initNativeWindow", "(Ljava/lang/Object;)V", (void *) Android_JNI_initNativeWindow},
        {"enqueue",          "([BII)V",               (void *) Android_JNI_enqueueYUV}
};

int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass jclass1 = jniEnv->FindClass(NATIVE_PATH);
    jniEnv->RegisterNatives(jclass1, mNATIVE_METHODS, sizeof(mNATIVE_METHODS) / sizeof(mNATIVE_METHODS[0]));
    jniEnv->DeleteLocalRef(jclass1);
    return JNI_VERSION_1_6;
}