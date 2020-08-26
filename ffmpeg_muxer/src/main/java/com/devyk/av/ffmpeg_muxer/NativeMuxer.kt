package com.devyk.av.ffmpeg_muxer

/**
 * <pre>
 *     author  : devyk on 2020-08-23 20:50
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeMuxer
 * </pre>
 */
object NativeMuxer {
    init {
        System.loadLibrary("muxer")
    }

    external fun muxer(h264Path: String, audioPath: String, outputPath: String)

}