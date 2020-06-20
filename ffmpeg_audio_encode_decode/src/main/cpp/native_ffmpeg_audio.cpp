//
// Created by 阳坤 on 2020-06-17.
//
#include <jni.h>
#include "AudioEncoder.h"

#define NATIVE_PATH "com/devyk/ffmpeg_audio_encode/FFmpeg_Native_Methods"

AudioEncoder *mAudioEncoder = 0;


static int Android_JNI_init(JNIEnv *jniEnv, jobject job, jstring outAACPath, int bitRate, int channels, int sampleRate
) {
    if (mAudioEncoder){
        mAudioEncoder->release();
        mAudioEncoder = NULL;
    }
    int ret = 0;
    if (mAudioEncoder == NULL)
        mAudioEncoder = new AudioEncoder();


    const char *url = jniEnv->GetStringUTFChars(outAACPath, 0);


    ret = mAudioEncoder->init(url, bitRate, channels, sampleRate, "libfdk_aac");
    jniEnv->ReleaseStringUTFChars(outAACPath, url);


    return ret;
}

static int Android_JNI_encode(JNIEnv *jniEnv, jobject job, jbyteArray array) {
    int ret = 0;
    if (mAudioEncoder == NULL)
        return 0;
    jbyte *jb = jniEnv->GetByteArrayElements(array, 0);
    PCMPacket *packet = static_cast<PCMPacket *>(malloc(sizeof(PCMPacket)));
    packet->len = jniEnv->GetArrayLength(array);
    packet->data = reinterpret_cast<uint8_t *>(jb);
    ret = mAudioEncoder->encode(packet);
    free(packet);
    jniEnv->ReleaseByteArrayElements(array, jb, 0);
    return ret;
}

static void Android_JNI_release(JNIEnv *jniEnv, jobject job) {
    if (mAudioEncoder == NULL)
        return;
    mAudioEncoder->release();
    mAudioEncoder = NULL;
}


static JNINativeMethod nativeMethods[] = {
        {"init",    "(Ljava/lang/String;III)I", (void *) Android_JNI_init},
        {"encode",  "([B)I",                    (void *) Android_JNI_encode},
        {"release", "()V",                      (void *) Android_JNI_release}
};


int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass JAVA_CLASS = jniEnv->FindClass(NATIVE_PATH);
    jniEnv->RegisterNatives(JAVA_CLASS, nativeMethods, sizeof(nativeMethods) / sizeof(nativeMethods[0]));
    jniEnv->DeleteLocalRef(JAVA_CLASS);
    return JNI_VERSION_1_6;

}
