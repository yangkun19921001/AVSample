//
// Created by 阳坤 on 2020-06-08.

#include "jni.h"
#include "encode/X264Encoder.h"


#define ENCODEC_NATIVE_CLASS "com/devyk/x264_video_encode/NativeX264Encode"


X264Encoder *x264Encoder = 0;



static void
Android_JNI_init(JNIEnv *jniEnv, jobject jobject1, jstring outH264Path, jint width, jint height, jint videoBitRate,
                 jint frameRate) {

    if (!x264Encoder)
        x264Encoder = new X264Encoder();

    const char *h264Path = jniEnv->GetStringUTFChars(outH264Path, 0);

    x264Encoder->init(h264Path, width, height, videoBitRate, frameRate);

    jniEnv->ReleaseStringUTFChars(outH264Path, h264Path);

}

static void Android_JNI_encode(JNIEnv *jniEnv, jobject jobject1, jbyteArray jbyteArray1,jint yuvType) {
    jbyte *byte = jniEnv->GetByteArrayElements(jbyteArray1, 0);
    AVPacket *avPacket = static_cast<AVPacket *>(malloc(sizeof(AVPacket)));
    avPacket->data = reinterpret_cast<uint8_t *>(byte);
    avPacket->type = yuvType;
    if (x264Encoder)
        x264Encoder->encode(avPacket);
    free(avPacket);
    jniEnv->ReleaseByteArrayElements(jbyteArray1, byte, 0);
}

static void Android_JNI_destory(JNIEnv *jniEnv, jobject jobject1) {
    if (x264Encoder)
        x264Encoder->destory();
}


/**
 * 编码 native 动态函数
 */
static JNINativeMethod mEncodeNativeMethods[] = {
        "init", "(Ljava/lang/String;IIII)V", (void *) Android_JNI_init,
        "encode", "([BI)V", (void *) Android_JNI_encode,
        "destory", "()V", (void *) Android_JNI_destory
};


/**
 * system.load 会执行该函数，然后动态注册 native
 * @param javaVM
 * @param pVoid
 * @return
 */
int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jclass JVAV_CLASS = jniEnv->FindClass(ENCODEC_NATIVE_CLASS);
    jniEnv->RegisterNatives(JVAV_CLASS, mEncodeNativeMethods,
                            sizeof(mEncodeNativeMethods) / sizeof(mEncodeNativeMethods[0]));
    jniEnv->DeleteLocalRef(JVAV_CLASS);
    return JNI_VERSION_1_6;
}




