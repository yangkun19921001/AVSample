package com.devyk.nativeavplayer

/**
 * <pre>
 *     author  : devyk on 2020-06-30 21:43
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeAPI
 * </pre>
 */
public class NativeAPI {
    companion object {
        init {
            System.loadLibrary("native-player")
        }
    }


/*
    JNINativeMethod mNATIVE_METHODS[] = {
        {"prepare", "(III)I", (void *) Android_JNI_prepare},
        {"play",    "()V",    (void *) Android_JNI_play},
        {"pause",   "()V",    (void *) Android_JNI_pause},
        {"enqueue", "",       (void *) Android_JNI_enqueue},
        {"release", "()V",    (void *) Android_JNI_release},
    };*/


    public external fun prepare(sampleRate: Int, channel: Int, sampleFormat: Int): Int
    public external fun play()
    public external fun pause()
    public external fun enqueue(data: ByteArray)
    public external fun release()


    public external fun initNativeWindow(surface: Any)
    public external fun enqueue(data: ByteArray, w: Int, h: Int)
}