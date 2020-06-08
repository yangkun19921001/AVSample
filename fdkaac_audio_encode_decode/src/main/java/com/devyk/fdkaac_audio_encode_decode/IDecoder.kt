package com.devyk.fdkaac_audio_encode_decode

/**
 * <pre>
 *     author  : devyk on 2020-06-07 15:52
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IDecoder
 * </pre>
 */

public interface IDecoder {
    /**
     * 初始化 ADTS 形式的 AAC
     */
    fun initWithADTformat():Int

    /**
     * 初始化 RAW 格式形式的 AAC
     */
    fun initWithRAWformat(specInfo: ByteArray, size: ByteArray):Int

    /**
     * 解码
     */
    fun decode(byteArray: ByteArray, len: Int):ByteArray

    /**
     * 销毁
     */
    fun destory()
}