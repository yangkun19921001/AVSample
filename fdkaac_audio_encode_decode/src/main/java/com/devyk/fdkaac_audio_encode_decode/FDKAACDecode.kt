package com.devyk.fdkaac_audio_encode_decode

/**
 * <pre>
 *     author  : devyk on 2020-06-07 15:56
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FDKAACDecode
 * </pre>
 */
public class FDKAACDecode : IDecoder {

    companion object {
        init {
            System.loadLibrary("native-fdkaac")
        }
    }

    override external fun initWithADTformat(): Int

    override external fun initWithRAWformat(specInfo: ByteArray, size: ByteArray): Int

    override external fun decode(byteArray: ByteArray, len: Int): ByteArray

    override external fun destory()
}