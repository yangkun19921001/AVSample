//
// Created by 阳坤 on 2020-06-02.
//
#include <jni.h>
#include <AACDecoder.h>

#define ENCODE_NATIVE_CLASS "com/devyk/fdkaac_audio_encode_decode/FDKAACEncode"
#define DECODE_NATIVE_CLASS "com/devyk/fdkaac_audio_encode_decode/FDKAACDecode"
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))


#include "encode/AACEncoder.h"

AACEncoder *mAACEncode = 0;
AACDecoder *mAACDecode = 0;

//static FILE *write = fopen("/sdcard/avsample/fdkaac_decode_2.pcm", "wb");

//-------------------------------编码--------------------------------

static jint Android_JNI_init(JNIEnv *jniEnv, jobject jobje, jint bitRate, jint channels, jint sampleRate) {
    if (!mAACEncode)
        mAACEncode = new AACEncoder();


    AACProfile profile = LC_AAC;
    mAACEncode->init(profile, sampleRate, channels, bitRate);

    return 1;

}

static void Android_JNI_encode(JNIEnv *jniEnv, jobject jobje, jbyteArray byteArray, jint size) {
    jbyte *pcm = jniEnv->GetByteArrayElements(byteArray, 0);
    int ret = 0;
    char *outBuffer = 0;
    if (mAACEncode)
        ret =  mAACEncode->encode((Byte *) (pcm), size, &outBuffer);
    jniEnv->ReleaseByteArrayElements(byteArray, pcm, 0);

    return ;
}



//-------------------------------解码--------------------------------

static int Android_JNI_initWithADTformat(JNIEnv *jniEnv, jobject jobje) {
    int ret = 0;
    if (!mAACDecode)
        mAACDecode = new AACDecoder();
    ret = mAACDecode->initWithADTSFormat();
    return ret;

}

static int Android_JNI_initWithRAWformat(JNIEnv *jniEnv, jobject jobje, jbyteArray specArray, jint size) {
    int ret = 0;
    if (!mAACDecode)
        mAACDecode = new AACDecoder();
    jbyte *spec = jniEnv->GetByteArrayElements(specArray, 0);
    ret = mAACDecode->initWithRawFormat((byte *) (spec), size);
    jniEnv->ReleaseByteArrayElements(specArray, spec, 0);
    return ret;
}

static jbyteArray Android_JNI_decode(JNIEnv *jniEnv, jobject jobje, jbyteArray aacByte, jint byteSize) {
    int ret = 0;
    jbyte *aac = jniEnv->GetByteArrayElements(aacByte, 0);
    byte *outFrame = NULL;
    jbyteArray pcmByte = NULL;
    if (mAACDecode) {
        ret = mAACDecode->decode((byte *) (aac), byteSize, &outFrame);
        if (outFrame) {
//            fwrite(outFrame, ret, 1, write);
            pcmByte = jniEnv->NewByteArray(ret);
            jniEnv->SetByteArrayRegion(pcmByte, 0, ret, reinterpret_cast<const jbyte *>(outFrame));
            delete[] outFrame;
        }
    }
    jniEnv->ReleaseByteArrayElements(aacByte, aac, 0);
    return pcmByte;
}



//-------------------------------解码/编码销毁--------------------------------

static void Android_JNI_destory() {
    if (mAACEncode) {
        mAACEncode->destory();
        delete mAACEncode;
        mAACEncode = 0;
    }

    if (mAACDecode) {
        mAACDecode->destory();
        delete mAACDecode;
        mAACDecode = 0;
    }

}


/**
 * 编码对应的 native 函数，用于动态注册
 */
static JNINativeMethod ENCODER_NATIVE_METHOD[] = {
        //native 函数-------签名-------对象的函数
        {"init",    "(III)I", (void *) Android_JNI_init},
        {"encode",  "([BI)V", (void *) Android_JNI_encode},
        {"destory", "()V",    (void *) Android_JNI_destory}
};

/**
 * 解码码对应的 native 函数，用于动态注册
 */
static JNINativeMethod DECODER_NATIVE_METHOD[] = {
        //native 函数-------签名-------对象的函数
        {"initWithADTformat", "()I",     (void *) Android_JNI_initWithADTformat},
        {"initWithRAWformat", "([B[B)I", (void *) Android_JNI_initWithRAWformat},
        {"decode",            "([BI)[B", (void *) Android_JNI_decode},
        {"destory",           "()V",     (void *) Android_JNI_destory}
};

/**
 * System.loadLibrary 会执行
 * @param javaVM
 * @param pVoid
 * @return
 */
jint JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass enClass = jniEnv->FindClass(ENCODE_NATIVE_CLASS);
    jniEnv->RegisterNatives(enClass, ENCODER_NATIVE_METHOD, NELEM(ENCODER_NATIVE_METHOD));
    jniEnv->DeleteLocalRef(enClass);

    jclass deClass = jniEnv->FindClass(DECODE_NATIVE_CLASS);
    jniEnv->RegisterNatives(deClass, DECODER_NATIVE_METHOD, NELEM(DECODER_NATIVE_METHOD));
    jniEnv->DeleteLocalRef(deClass);
    return JNI_VERSION_1_6;
}
