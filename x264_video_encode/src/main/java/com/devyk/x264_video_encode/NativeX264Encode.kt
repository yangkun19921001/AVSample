package com.devyk.x264_video_encode

/**
 * <pre>
 *     author  : devyk on 2020-06-10 22:41
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeX264Encode
 * </pre>
 */

public class NativeX264Encode :IEncoder{
    companion object{
        init {

            System.loadLibrary("native-x264")
        }
    }

    override external fun init(h264Path: String, width: Int, height: Int, videoBitRate: Int, frameRate: Int) ;

    override external fun encode(byteArray: ByteArray,yuvType:Int) ;

    override external fun destory() ;



}