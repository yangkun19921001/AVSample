package com.devyk.ffmpeg_video_encode.sample

import android.widget.Button
import android.widget.Chronometer
import com.devyk.ffmpeg_video_encode.NativeFFmpegVideoApi
import com.devyk.ffmpeg_video_encode.R
import com.devyk.ikavedit.base.BaseActivity

/**
 * <pre>
 *     author  : devyk on 2020-06-23 14:31
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegVideoDecoderActivity
 * </pre>
 */

public class FFmpegVideoDecoderActivity : BaseActivity<Int>() {

    private var OUT_YUV_PATH = "sdcard/avsample/352_288_i420_h264_yuv.yuv"
    private var IN_H264_PATH = "sdcard/avsample/352_288_i420.h264"


    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        val nativeFFmpegVideoApi = NativeFFmpegVideoApi()
        val btn_start = findViewById<Button>(R.id.btn_start)
        btn_start.text = "开始解码"
        val btn_stop = findViewById<Button>(R.id.btn_stop)
        btn_stop.text = "停止解码"
        val timer_ = findViewById<Chronometer>(R.id.timer)

        //LOG ffmpeg H264->YUV decoder completed!  代表解码完成！
        btn_start.setOnClickListener {
            var isSuccess = nativeFFmpegVideoApi.init(IN_H264_PATH, OUT_YUV_PATH, 352, 288, 200);
            if (isSuccess == 1) {
                startTime(timer_)
                nativeFFmpegVideoApi.start()
            }
        }

        btn_stop.setOnClickListener {
            cleanTime(timer_)
            nativeFFmpegVideoApi.release()
        }

    }

    override fun getLayoutId(): Int = R.layout.activity_video_decoder


    override fun onDestroy() {
        super.onDestroy()
    }
}