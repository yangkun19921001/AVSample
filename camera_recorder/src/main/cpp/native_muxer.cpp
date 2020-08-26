//
// Created by 阳坤 on 2020-08-23.
//

#include <jni.h>
#include <cstddef>
#include "FFmpegMuxer.h"
#include "AVPacketPool.h"
#include "recording_h264_publisher.h"
//#include "FFmpegMuxer.h"


FFmpegMuxer *fFmpegMuxer = 0;
RecordingPublisher *recordingPublisher = 0;


int isStart = false;

void Android_JNI_init(JNIEnv *env, jobject obj,
                      jstring joutputPath) {

    const char *outputPath = env->GetStringUTFChars(joutputPath, NULL);

//    fFmpegMuxer = new FFmpegMuxer();

//    fFmpegMuxer->Transform(outputPath);


    recordingPublisher = new RecordingH264Publisher();
    AVPacketPool::GetInstance()->destoryAudioPacketQueue();
    AVPacketPool::GetInstance()->destoryRecordingVideoPacketQueue();
    AVPacketPool::GetInstance()->initAVQueue();

    int ret = recordingPublisher->init(const_cast<char *>(outputPath), 1280, 720, 25, 2000 * 1000, 44100, 1, 64 * 1000);
    if (ret >= 0)
        recordingPublisher->start();


    env->ReleaseStringUTFChars(joutputPath, outputPath);
}

void Android_JNI_enqueue(JNIEnv *env, jobject obj, jbyteArray data, jint isAudio, jlong pts) {
    jint size = env->GetArrayLength(data);
    jbyte *datas = env->GetByteArrayElements(data, 0);
    if (recordingPublisher) {
        if (isAudio) {
            AVPacketPool::GetInstance()->enqueueAudio(reinterpret_cast<uint8_t *>(datas), size, pts);
        } else {
            AVPacketPool::GetInstance()->enqueueVideo(reinterpret_cast<uint8_t *>(datas), size, pts);
        }
//
//        AVData avData;
//        avData.alloc(size, reinterpret_cast<const char *>(datas));
//        avData.isAudio = isAudio;
//        avData.pts = pts;
//        fFmpegMuxer->writeAVPacket(avData);
    }
//
//    if (!isStart) {
//        recordingPublisher->start();
//        isStart = true;
//
//    }


    env->ReleaseByteArrayElements(data, datas, 0);

}

void Android_JNI_close(JNIEnv *env, jobject obj) {
//    if (fFmpegMuxer) {
//        fFmpegMuxer->close();
//        delete fFmpegMuxer;
//        fFmpegMuxer = 0;
//    }
    isStart = false;

    AVPacketPool::GetInstance()->getAudioPacketQueue()->abort();
    AVPacketPool::GetInstance()->getVideoPacketQueue()->abort();
    recordingPublisher->interruptPublisherPipe();
    if (recordingPublisher) {
        recordingPublisher->stop();
        delete recordingPublisher;
        recordingPublisher = nullptr;
        AVPacketPool::GetInstance()->destoryRecordingVideoPacketQueue();
        AVPacketPool::GetInstance()->destoryAudioPacketQueue();
    }

}

void Android_JNI_start(JNIEnv *env, jobject obj) {
    if (recordingPublisher) {
        recordingPublisher->start();
    }

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