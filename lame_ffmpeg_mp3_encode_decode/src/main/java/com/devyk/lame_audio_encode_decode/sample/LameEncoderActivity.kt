package com.devyk.lame_audio_encode_decode.sample

import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.lame_audio_encode_decode.FFmpegNativeApi
import com.devyk.lame_audio_encode_decode.R

/**
 * <pre>
 *     author  : devyk on 2020-06-25 14:34
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is LameEncoderActivity
 * </pre>
 */
public class LameEncoderActivity : BaseActivity() {

    lateinit var chronometer: Chronometer
    lateinit var start: Button
    lateinit var stop: Button
    lateinit var mFFmpegNativeApi: FFmpegNativeApi

    private var outMp3Path = "sdcard/avsample/lame_encode_pcm_2_mp3.mp3"


    private var isInit = 0;


    override fun initListener() {
        AudioCapture.addRecordListener(object : AudioCapture.OnRecordListener {
            override fun onStart() {

                runOnUiThread {
                    startTime(chronometer)
                    isInit = mFFmpegNativeApi.init(outMp3Path, 44100, 1, 128)
                }
            }

            override fun onStop() {
                runOnUiThread { cleanTime(chronometer) }
                mFFmpegNativeApi.release()
            }

            override fun onData(byteArray: ByteArray) {
                if (isInit == 1)
                    mFFmpegNativeApi.encode(byteArray)
            }
        })
    }

    override fun initData() {
    }

    override fun init() {
        initView()
    }

    private fun initView() {
        chronometer = findViewById(R.id.timer)
        start = findViewById(R.id.btn_start)
        start.text = getString(R.string.start_encoder)
        stop = findViewById(R.id.btn_stop)
        stop.text = getString(R.string.stop_encoder)
        mFFmpegNativeApi = FFmpegNativeApi()

        start.setOnClickListener {
            AudioCapture.init()
            AudioCapture.startRecording()
        }


        stop.setOnClickListener {
            AudioCapture.stopRecording()
        }

    }

    override fun getLayoutId(): Int = R.layout.activity_ffmpeg_mp3_encoder
}