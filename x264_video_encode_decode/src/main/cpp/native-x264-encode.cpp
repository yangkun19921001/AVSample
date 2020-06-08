//
// Created by 阳坤 on 2020-06-08.

#include "jni.h"


#define ENCODEC_NATIVE_CLASS ""

static void Android_JNI_init() {}

static void Android_JNI_encode() {}

static void Android_JNI_destory() {}


/**
 * 编码 native 动态函数
 */
static JNINativeMethod mEncodeNativeMethods[] = {
        "init", "", (void *) Android_JNI_init,
        "encode", "", (void *) Android_JNI_encode,
        "destory", "", (void *) Android_JNI_destory
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




