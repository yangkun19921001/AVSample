//
// Created by 阳坤 on 2020-08-23.
//

#include <jni.h>
#include <cstddef>
#include "AVPacketPool.h"
#include "mp4_muxer.h"


Mp4Muxer *mp4Muxer = 0;

void Android_JNI_init(JNIEnv *env, jobject obj,
                      jstring joutputPath) {

    const char *outputPath = env->GetStringUTFChars(joutputPath, NULL);

    mp4Muxer = new Mp4Muxer();
    std::string tag_name("AVTools");
    int ret = mp4Muxer->Init(outputPath, 1280, 720, 25, 2000 * 1000, 44100, 1, 64 * 1000, tag_name);
    if (ret >= 0) {
        while (1) {
            ret = mp4Muxer->Encode();
            if (ret < 0) {
                break;
            }
        }
        LOGE("MP4--->合并完成 %d  %s", ret,outputPath);
    } else {
        if (nullptr != mp4Muxer) {
            mp4Muxer->Stop();
            delete mp4Muxer;
            mp4Muxer = nullptr;
        }
    }

    env->ReleaseStringUTFChars(joutputPath, outputPath);
}

void Android_JNI_enqueue(JNIEnv *env, jobject obj, jbyteArray data, jint isAudio, jlong pts) {
    jint size = env->GetArrayLength(data);
    jbyte *datas = env->GetByteArrayElements(data, 0);

    if (mp4Muxer)
        mp4Muxer->enqueue(reinterpret_cast<uint8_t *>(datas), isAudio, size, pts);


    env->ReleaseByteArrayElements(data, datas, 0);

}

void Android_JNI_close(JNIEnv *env, jobject obj) {


    if (mp4Muxer) {
        if (nullptr != mp4Muxer) {
            mp4Muxer->Stop();
            delete mp4Muxer;
            mp4Muxer = nullptr;
        }
    }

}

void Android_JNI_start(JNIEnv *env, jobject obj) {

}

/**
 * 动态注册
 */
JNINativeMethod methods[] = {
        {"init",    "(Ljava/lang/String;)V", (void *) Android_JNI_init},
        {"enqueue", "([BIJ)V",               (void *) Android_JNI_enqueue},
//        {"start",   "()V",               (void *) Android_JNI_start},
        {"close",   "()V",                   (void *) Android_JNI_close}
};


/**
 * 动态注册
 * @param env
 * @return
 */
jint registerNativeMethod(JNIEnv *env) {
    jclass cl = env->FindClass("com/devyk/av/camera_recorder/muxer/NativeMuxer");
    if ((env->RegisterNatives(cl, methods, sizeof(methods) / sizeof(methods[0]))) < 0) {
        return JNI_ERR;
    }
    return JNI_OK;
}


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    //注册方法
    if (registerNativeMethod(env) != JNI_OK) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}