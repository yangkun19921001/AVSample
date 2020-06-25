package com.devyk.lame_audio_encode_decode

/**
 * <pre>
 *     author  : devyk on 2020-06-25 14:31
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegNativeApi
 * </pre>
 */
class FFmpegNativeApi {
    companion object {
        init {
            System.loadLibrary("native-ffmpeg-mp3-api");
        }
    }


    public external fun init(outURL: String, sampleRate: Int, channels: Int, bitRate: Int):Int;

    public external fun init(inPath: String,outPath: String, sampleRate: Int, channels: Int, bitRate: Int):Int;

    public external fun  startDecoder();

    public external fun encode(byteArray: ByteArray);


    public external fun release();
}