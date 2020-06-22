package com.devyk.ffmpeg_video_encode

import android.annotation.SuppressLint
import android.view.Choreographer
import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.base.BaseActivity

/**
 * <pre>
 *     author  : devyk on 2020-06-22 22:27
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegVideoEncoderActivity
 * </pre>
 *
 * YUV play: ffplay -f rawvideo -video_size 352x288 352_288_i420.yuv
 * H264 play: ffplay 352_288_i420.h264
 */
public class FFmpegVideoEncoderActivity : BaseActivity() {

    private var IN_YUV_PATH = "sdcard/avsample/352_288_i420.yuv"
    private var OUT_H264_PATH = "sdcard/avsample/352_288_i420.h264"


    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        val nativeFFmpegVideoApi = NativeFFmpegVideoApi()
        val btn_start = findViewById<Button>(R.id.btn_start)
        btn_start.text = "开始编码"
        val btn_stop = findViewById<Button>(R.id.btn_stop)
        btn_stop.text = "停止编码"
        val timer_ = findViewById<Chronometer>(R.id.timer)


        //LOG 过滤 h264 encode complete! 表示编码完成
        btn_start.setOnClickListener {
            var isSuccess = nativeFFmpegVideoApi.init(IN_YUV_PATH, OUT_H264_PATH, 352, 288, 15, 200);
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

    override fun getLayoutId(): Int = R.layout.activity_video_encoder


    override fun onDestroy() {
        super.onDestroy()
    }
}