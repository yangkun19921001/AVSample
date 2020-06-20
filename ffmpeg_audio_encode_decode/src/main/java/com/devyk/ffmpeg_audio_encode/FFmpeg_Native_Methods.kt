package com.devyk.ffmpeg_audio_encode

/**
 * <pre>
 *     author  : devyk on 2020-06-18 21:01
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpeg_Native_Methods
 * </pre>
 */
public class FFmpeg_Native_Methods {
    companion object {
        init {
            System.loadLibrary("native-ffmpeg-audio")
        }
    }

    public external fun init( outAACPath:String,bitRate:Int, channels:Int,sampleRate:Int):Int;
    public external fun encode(byteArray: ByteArray):Int;
    public external fun release();
}