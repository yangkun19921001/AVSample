package com.devyk.ffmpeg_audio_encode.sample

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.devyk.ffmpeg_audio_encode.FFmpeg_Native_Methods
import com.devyk.ffmpeg_audio_encode.R
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import kotlinx.android.synthetic.main.activity_ffmpeg_aac_encode.*

/**
 * <pre>
 *     author  : devyk on 2020-06-18 21:08
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegAACEncodeActivity
 * </pre>
 */
public class FFmpegAACEncodeActivity : BaseActivity() {


    var mNativeMethods: FFmpeg_Native_Methods? = null

    private var mAACPath = "sdcard/avsample/ffmpeg_aac_441.aac";
    @Volatile
    private var isInit = -1;

    override fun initListener() {
        AudioCapture.addRecordListener(object : AudioCapture.OnRecordListener {
            override fun onStart() {
                runOnUiThread {
                    timeClean()//计时器清零
                    setTimeFormat()
                    timer.start();
                    isInit = mNativeMethods?.init(mAACPath, 64*1000, 1, 44100)!!
                }
            }

            override fun onStop() {
                mNativeMethods?.release()
                runOnUiThread{
                    timeClean()//计时器清零
                    timer.stop();
                }

            }

            override fun onData(byteArray: ByteArray) {
                Log.i(TAG, byteArray.size.toString())
                if (isInit == 0) {
                    AudioCapture.stopRecording()
                    onStop()
                    isInit = -1;
                    return
                }
                if (isInit == 1)
                    mNativeMethods?.encode(byteArray)
            }
        })
    }

    override fun initData() {

    }

    override fun init() {
        //初始化音频采集
        AudioCapture.init()

        mNativeMethods = FFmpeg_Native_Methods()
    }

    override fun getLayoutId(): Int = R.layout.activity_ffmpeg_aac_encode

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
        mNativeMethods?.release()
        mNativeMethods = null

    }
}