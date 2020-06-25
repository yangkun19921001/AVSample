//
// Created by 阳坤 on 2020-06-23.
//

#include <jni.h>
#include "Mp3Decoder.h"
#include "LameEncoder.h"

#define NATIVE_PATH "com/devyk/lame_audio_encode_decode/FFmpegNativeApi"


LameEncoder *mp3Encoder = 0;
Mp3Decoder *mp3Decoder = 0;

static int
Android_JNI_init_encode(JNIEnv *jniEnv, jobject jobject1, jstring out, jint sampleRate, jint channels, jint bitrate) {
    if (!mp3Encoder) {
        mp3Encoder = new LameEncoder();
    }
    const char *url = jniEnv->GetStringUTFChars(out, 0);
    int ret = 0;
    ret = mp3Encoder->init(url, sampleRate, channels, bitrate);

    jniEnv->ReleaseStringUTFChars(out, url);
    return ret;
}

static int
Android_JNI_init_decode(JNIEnv *jniEnv, jobject jobject1, jstring in, jstring out, jint sampleRate, jint channels,
                        jint bitrate) {
    if (!mp3Decoder) {
        mp3Decoder = new Mp3Decoder();
    }
    const char *in_url = jniEnv->GetStringUTFChars(in, 0);
    const char *out_url = jniEnv->GetStringUTFChars(out, 0);
    int ret = 0;
    ret = mp3Decoder->init(in_url, out_url, "libmp3lame");

    jniEnv->ReleaseStringUTFChars(in, in_url);
    jniEnv->ReleaseStringUTFChars(out, out_url);
    return ret;
}

static jint Android_JNI_encode(JNIEnv *jniEnv, jobject jobject1, jbyteArray pcm) {
    int ret = 0;
    jbyte *pcmArray = jniEnv->GetByteArrayElements(pcm, 0);
    if (mp3Encoder) {
        ret = mp3Encoder->encode((uint8_t *) pcmArray, jniEnv->GetArrayLength(pcm));
    }
    jniEnv->ReleaseByteArrayElements(pcm, pcmArray, 0);
    return ret;

}

static void Android_JNI_start_decoder(JNIEnv *jniEnv, jobject jobject1, jbyteArray pcm) {
    if (mp3Decoder) {
        mp3Decoder->start();
    }
}

static void Android_JNI_release(JNIEnv *jniEnv, jobject jobject1) {
    if (mp3Encoder)
        mp3Encoder->release();

    if (mp3Decoder)
        mp3Decoder->release();
}

JNINativeMethod NATIVE_METHODS[] = {
        {"init",          "(Ljava/lang/String;III)I",                   (void *) Android_JNI_init_encode},
        {"init",          "(Ljava/lang/String;Ljava/lang/String;III)I", (void *) Android_JNI_init_decode},
        {"encode",        "([B)V",                                      (void *) Android_JNI_encode},
        {"startDecoder", "()V",                                        (void *) Android_JNI_start_decoder},
        {"release",       "()V",                                        (void *) Android_JNI_release}
};

int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jclass jclass1 = jniEnv->FindClass(NATIVE_PATH);

    jniEnv->RegisterNatives(jclass1, NATIVE_METHODS, sizeof(NATIVE_METHODS) / sizeof(NATIVE_METHODS[0]));
    jniEnv->DeleteLocalRef(jclass1);

    return JNI_VERSION_1_6;


}