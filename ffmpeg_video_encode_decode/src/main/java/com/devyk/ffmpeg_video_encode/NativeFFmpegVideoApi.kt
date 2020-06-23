package com.devyk.ffmpeg_video_encode

/**
 * <pre>
 *     author  : devyk on 2020-06-22 22:14
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeFFmpegVideoApi
 * </pre>
 */
public class NativeFFmpegVideoApi {
    companion object {
        init {
            System.loadLibrary("native-ffmpeg-video-api")
        }
    }

    /**
     * 编码初始化
     */
    public external fun init(
        inYUV420spPath: String,
        outH264Path: String,
        width: Int,
        height: Int,
        fps: Int,
        videoRate: Int
    ): Int;

    /**
     * 解码初始化
     */
    public external fun init(
        inYUV420spPath: String,
        outH264Path: String,
        width: Int,
        height: Int,
        videoRate: Int
    ): Int;

    public external fun start()
    public external fun release()
}