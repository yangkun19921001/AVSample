package com.devyk.av.ffmpeg_muxer

import android.view.View
import com.devyk.ikavedit.base.BaseActivity

/**
 * <pre>
 *     author  : devyk on 2020-08-23 21:09
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegMuxerActivity
 * </pre>
 */
public class FFmpegMuxerActivity : BaseActivity<Int>() {
    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
    }

    override fun getLayoutId(): Int = R.layout.activity_ffmpeg_muxer


    /**
     * 注意： aac 文件必须添加 ADTS 头信息
     */
    public fun merge_mp4(view: View) {
        Thread {
            NativeMuxer.muxer("sdcard/aveditor/123.h264", "sdcard/aveditor/123.aac", "sdcard/aveditor/ffmpeg_muxer.mp4")
        }.start()
    }
}