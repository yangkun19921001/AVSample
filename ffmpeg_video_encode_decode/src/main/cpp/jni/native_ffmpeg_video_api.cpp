//
// Created by 阳坤 on 2020-06-21.
//

#include <jni.h>
#include "YUV2H264Encoder.h"
#include "H264_2_YUVDecoder.h"

#define JNI_PATH "com/devyk/ffmpeg_video_encode/NativeFFmpegVideoApi"


YUV2H264Encoder *mH264Encoder = 0;
H264_2_YUVDecoder *mH264Decoder = 0;

static int
Android_jni_encode_init(JNIEnv *jniEnv, jobject jobject1, jstring in, jstring out, int w, int h, int fps,
                        int videoRate) {
    int ret = 0;

    const char *inYuvPath = jniEnv->GetStringUTFChars(in, 0);
    const char *outH264Path = jniEnv->GetStringUTFChars(out, 0);


    if (mH264Encoder) {
        mH264Encoder->release();
    }

    if (!mH264Encoder) {
        mH264Encoder = new YUV2H264Encoder();
        ret = mH264Encoder->init(inYuvPath, outH264Path, w, h, fps, videoRate);
    }

    jniEnv->ReleaseStringUTFChars(in, inYuvPath);
    jniEnv->ReleaseStringUTFChars(out, outH264Path);

    return ret;
}

static int
Android_jni_decode_init(JNIEnv *jniEnv, jobject jobject1, jstring in, jstring out, int w, int h, int videoRate) {
    int ret = 0;

    const char *inH264Path = jniEnv->GetStringUTFChars(in, 0);
    const char *outYUVPath = jniEnv->GetStringUTFChars(out, 0);


    if (mH264Decoder) {
        mH264Decoder->release();
    }

    if (!mH264Decoder) {
        mH264Decoder = new H264_2_YUVDecoder();
        ret = mH264Decoder->init(inH264Path, outYUVPath, w, h, videoRate);
    }

    jniEnv->ReleaseStringUTFChars(in, inH264Path);
    jniEnv->ReleaseStringUTFChars(out, outYUVPath);

    return ret;
}

static void Android_jni_start(JNIEnv *jniEnv, jobject jobject1) {
    if (mH264Encoder)
        mH264Encoder->start();

    if (mH264Decoder)
        mH264Decoder->start();

}

static void Android_jni_release(JNIEnv *jniEnv, jobject jobject1) {
    if (mH264Encoder)
        mH264Encoder->release();

    if (mH264Decoder)
        mH264Decoder->release();


}

JNINativeMethod mNativeMethods[] = {
        {"init",    "(Ljava/lang/String;Ljava/lang/String;IIII)I", (void *) Android_jni_encode_init},
        {"init",    "(Ljava/lang/String;Ljava/lang/String;III)I",  (void *) Android_jni_decode_init},
        {"start",   "()V",                                         (void *) Android_jni_start},
        {"release", "()V",                                         (void *) Android_jni_release}
};


int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass jclass1 = jniEnv->FindClass(JNI_PATH);
    jniEnv->RegisterNatives(jclass1, mNativeMethods, sizeof(mNativeMethods) / sizeof(mNativeMethods[0]));
    jniEnv->DeleteLocalRef(jclass1);
    return JNI_VERSION_1_6;
}