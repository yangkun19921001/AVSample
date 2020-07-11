package com.devyk.av.camera_recorder.packer

/**
 * <pre>
 *     author  : devyk on 2020-07-11 17:28
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IPacker
 * </pre>
 */
public interface IPacker {

    fun start()
    fun stop()
    fun pause()
    fun resume()

}