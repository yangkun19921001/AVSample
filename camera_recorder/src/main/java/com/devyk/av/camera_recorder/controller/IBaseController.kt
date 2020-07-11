package com.devyk.av.camera_recorder.controller

/**
 * <pre>
 *     author  : devyk on 2020-07-11 14:36
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IBaseController 对音视频统一接口封装
 * </pre>
 */
public interface IBaseController {
    fun start()
    fun stop()
    fun pause()
    fun resume()
}