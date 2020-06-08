package com.devyk.fdkaac_audio_encode_decode.sample

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.devyk.fdkaac_audio_encode_decode.R
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.FileOutputStream

/**
 * <pre>
 *     author  : devyk on 2020-05-28 21:30
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioRecordActivty
 * </pre>
 */

public class AudioRecordActivty : BaseActivity() {

    var mFileOutputStream: FileOutputStream? = null;

    private var mSavePCMPath = "sdcard/record_pcm_441.pcm";

    override fun initListener() {
        AudioCapture.addRecordListener(object : AudioCapture.OnRecordListener {
            override fun onStart() {
                Handler().post {
                    mFileOutputStream = FileOutputStream(mSavePCMPath, true);
                    timeClean()//计时器清零
                    setTimeFormat()
                    timer.start();
                }

            }

            override fun onStop() {
                Handler().post {
                    timeClean()//计时器清零
                    timer.stop();
                    mFileOutputStream?.flush()
                    mFileOutputStream?.close()
                    mFileOutputStream = null
                }
            }

            override fun onData(byteArray: ByteArray) {
                Log.i(TAG, byteArray.size.toString())
                mFileOutputStream?.write(byteArray, 0, byteArray.size)
            }
        })
    }

    override fun initData() {

    }

    override fun init() {
        //初始化音频采集
        AudioCapture.init()
    }

    override fun getLayoutId(): Int = R.layout.activity_audio_record


    fun startRecord(view: View) {
        AudioCapture.startRecording()


    }

    private fun setTimeFormat() {
        var hour = ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60).toInt();
        timer.setFormat("0${hour}:%s");
    }

    fun stopRecord(view: View) {
        AudioCapture.stopRecording()

    }

    private fun timeClean() {
        timer.setBase(SystemClock.elapsedRealtime());
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioCapture.stopRecording()

    }
}


