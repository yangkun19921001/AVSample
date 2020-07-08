package com.devyk.ffmpeg_audio_encode.sample

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.devyk.ffmpeg_audio_encode.FFmpeg_Native_Methods
import com.devyk.ffmpeg_audio_encode.R
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
public class FFmpegAACDecodeActivity : BaseActivity<Int>() {


    var mNativeMethods: FFmpeg_Native_Methods? = null

//    private var mAACPath = "sdcard/avsample/ffmpeg_aac_441.aac";
//    private var mAACPath = "sdcard/avsample/test8k.mp3";
    private var mAACPath = "sdcard/avsample/mediacodec_video.h264";
//    private var mPCMPath = "sdcard/avsample/ffmpeg_aac_decode_441.pcm";
    private var mPCMPath = "sdcard/avsample/ffmpeg_decode_video.yuv";
    @Volatile
    private var isInit = -1;

    override fun initListener() {
    }

    override fun initData() {

    }

    override fun init() {
        //初始化音频采集
        mNativeMethods = FFmpeg_Native_Methods()
    }

    override fun getLayoutId(): Int = R.layout.activity_ffmpeg_aac_encode

    /**
     * play pcm:
     *  ffplay -ar 44100 -channels 2 -f s16le -i ffmpeg_aac_decode_441.pcm
     */
    fun startDecode(view: View) {
        timeClean()//计时器清零
        setTimeFormat()
        timer.start();
        isInit = mNativeMethods?.init(mAACPath, mPCMPath)!!


    }

    private fun setTimeFormat() {
        var hour = ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60).toInt();
        timer.setFormat("0${hour}:%s");
    }

    fun stopDecode(view: View) {
        mNativeMethods?.release()
        timeClean()//计时器清零
        timer.stop();

    }

    private fun timeClean() {
        timer.setBase(SystemClock.elapsedRealtime());
    }

    override fun onDestroy() {
        super.onDestroy()
        mNativeMethods?.release()
        mNativeMethods = null

    }
}