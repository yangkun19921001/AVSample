//
// Created by 阳坤 on 2020-08-23.
//

#include <jni.h>
#include "FFmpegMuxer.h"



void muxer(JNIEnv *env, jobject obj, jstring jvideoPath, jstring jaudioPath,
                     jstring joutputPath) {

    const char *videoPath = env->GetStringUTFChars(jvideoPath, NULL);
    const char *audioPath = env->GetStringUTFChars(jaudioPath, NULL);
    const char *outputPath = env->GetStringUTFChars(joutputPath, NULL);

    FFmpegMuxer *fFmpegMuxer = new FFmpegMuxer();

    fFmpegMuxer->Transform(videoPath, audioPath, outputPath);

    delete fFmpegMuxer;

    env->ReleaseStringUTFChars(jvideoPath, videoPath);
    env->ReleaseStringUTFChars(jaudioPath, audioPath);
    env->ReleaseStringUTFChars(joutputPath, outputPath);
}


/**
 * 动态注册
 */
JNINativeMethod methods[] = {
        {"muxer", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void *) muxer}
};




/**
 * 动态注册
 * @param env
 * @return
 */
jint registerNativeMethod(JNIEnv *env) {
    jclass cl = env->FindClass("com/devyk/av/ffmpeg_muxer/NativeMuxer");
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