package com.devyk.nativeavplayer.sample

import android.media.AudioFormat
import android.media.MediaRecorder
import android.view.View
import com.devyk.avedit.audio.AudioUtils
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.nativeavplayer.NativeAPI
import com.devyk.nativeavplayer.R
import kotlinx.android.synthetic.main.activity_native_audio_play.*
import java.io.FileInputStream
import java.util.*

/**
 * <pre>
 *     author  : devyk on 2020-06-30 21:48
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeAudioPlayer
 * </pre>
 */

class NativeAudioPlayer : BaseActivity() {

    private lateinit var mNativeAPI: NativeAPI

    private var isInitSuccess = false;

    //44100 1个通道 16bit sampleFormat
    private val AUDIO_PATH = "sdcard/record_pcm_441.pcm";

    private lateinit var mFileInputStream: FileInputStream

    override fun initListener() {
        AudioCapture.addRecordListener(object : AudioCapture.OnRecordListener {
            override fun onStart(sampleRate: Int, channels: Int, sampleFormat: Int) {
                Thread {
                    if (mNativeAPI.prepare(sampleRate, 1, sampleFormat) == 1) {
                        isInitSuccess = true
                        mNativeAPI.play()
                        runOnUiThread { startTime(timer) }
                    }
                }.start()

            }

            override fun onStop() {
                isInitSuccess = false;
                runOnUiThread { cleanTime(timer) }
            }

            override fun onData(byteArray: ByteArray) {
                if (isInitSuccess) {
//                    Arrays.fill(byteArray, 0)
                    val read = mFileInputStream.read(byteArray)
                    mNativeAPI.enqueue(byteArray)
                }

            }
        })

    }

    override fun initData() {
    }

    override fun init() {
        AudioCapture.init()
        mNativeAPI = NativeAPI();
        mFileInputStream = FileInputStream(AUDIO_PATH)
    }

    override fun getLayoutId(): Int = R.layout.activity_native_audio_play


    public fun btn_play(view: View) {
        AudioCapture.startRecording()


    }

    public fun btn_pause(view: View) {
        isInitSuccess = false

    }

    public fun btn_restart(view: View) {
        isInitSuccess = true
    }

    public fun btn_stop(view: View) {
        AudioCapture.stopRecording()
        mNativeAPI.release()

    }
}