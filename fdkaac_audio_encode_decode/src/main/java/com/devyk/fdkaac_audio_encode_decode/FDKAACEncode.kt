package com.devyk.fdkaac_audio_encode_decode

/**
 * <pre>
 *     author  : devyk on 2020-06-02 21:44
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FDKAACEncode FDKAAC 编码器
 * </pre>
 */
public class FDKAACEncode : IEncoder {


    companion object {
        init {
            System.loadLibrary("native-fdkaac")
        }
    }
    override external fun init(bitRate: Int, channel: Int, sampleRate: Int): Int;

    override external fun encode(byteArray: ByteArray, bufferSize: Int);

    override external fun destory();
}