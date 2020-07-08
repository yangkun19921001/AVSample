package com.devyk.lame_audio_encode_decode.sample

import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.lame_audio_encode_decode.FFmpegNativeApi
import com.devyk.lame_audio_encode_decode.R

/**
 * <pre>
 *     author  : devyk on 2020-06-25 19:01
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegMp3DecoderActivity
 *
 *
 *     play pcm
 *     ffplay -ar 44100 -channels 2 -f s16le -i ceshi.pcm
 *
 * </pre>
 */

public class FFmpegMp3DecoderActivity : BaseActivity<Int>() {

    lateinit var chronometer: Chronometer
    lateinit var start: Button
    lateinit var stop: Button
    lateinit var mFFmpegNativeApi: FFmpegNativeApi

    private var inMp3Path = "sdcard/avsample/lame_encode_pcm_2_mp3.mp3"
    private var outPCMPath = "sdcard/avsample/ffmpeg_decode_mp3_2_pcm.pcm"


    private var isInit = 0;


    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        initView()
    }

    private fun initView() {
        chronometer = findViewById(R.id.timer)
        start = findViewById(R.id.btn_start)
        start.text = getString(R.string.start_decoder)
        stop = findViewById(R.id.btn_stop)
        stop.text = getString(R.string.stop_decoder)
        mFFmpegNativeApi = FFmpegNativeApi()

        start.setOnClickListener {
            isInit = mFFmpegNativeApi.init(inMp3Path, outPCMPath, 44100, 1, 128)
            if (isInit == 1) {
                startTime(chronometer)
                mFFmpegNativeApi.startDecoder()
            }
        }


        stop.setOnClickListener {
            mFFmpegNativeApi.release()
            cleanTime(chronometer)

        }

    }

    override fun getLayoutId(): Int = R.layout.activity_ffmpeg_mp3_decoder


}