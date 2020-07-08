package com.devyk.javaavplayer.sample

import android.media.AudioFormat
import android.media.AudioManager
import android.widget.Button
import android.widget.Chronometer
import com.devyk.common.LogHelper
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.javaavplayer.R
import com.devyk.javaavplayer.audiotrack.AudioTrackImpl
import java.io.FileInputStream
import java.util.*

/**
 * <pre>
 *     author  : devyk on 2020-06-27 19:46
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioTrackPlayerActivity
 * </pre>
 */
public class AudioTrackPlayerActivity : BaseActivity<Int>() {

    private val sampleRate = 44100;
    private val channels = AudioFormat.CHANNEL_OUT_MONO;
    private val sampleFormat = AudioFormat.ENCODING_PCM_16BIT;

    private val bufferSize = 1024;

    private val buffer = ByteArray(bufferSize);


    private var isPlayer = true;

    private lateinit var chronometer: Chronometer

    private val AUDIO_PATH = "sdcard/avsample/test.pcm"

    private lateinit var mFileInputStream: FileInputStream

    private lateinit var mAudioTrackImpl: AudioTrackImpl
    override fun initListener() {
    }

    override fun initData() {
    }

    override fun init() {
        var btn_start = findViewById<Button>(R.id.btn_start);
        btn_start.text = resources.getString(R.string.start_player)
        var btn_stop = findViewById<Button>(R.id.btn_stop);
        btn_stop.text = resources.getString(R.string.stop_player)
        chronometer = findViewById<Chronometer>(R.id.timer);
        mAudioTrackImpl = AudioTrackImpl()
        btn_start.setOnClickListener {
            val isSuccess = mAudioTrackImpl.prepare(AudioManager.STREAM_MUSIC, sampleRate, channels, sampleFormat)
            if (isSuccess) {
                isPlayer = true
                mFileInputStream = FileInputStream(AUDIO_PATH)
                mAudioTrackImpl.start()
                startTime(chronometer)
                readPcm()
                LogHelper.e(TAG, "AudioTrack init success!")
            }
        }
        btn_stop.setOnClickListener {
            if (mAudioTrackImpl.getStatus()) {
                isPlayer = false
                mAudioTrackImpl.release()
                mFileInputStream.close()
            }
            cleanTime(chronometer)
        }
    }

    private fun readPcm() {
        Thread {
            var len = 0;
            do {
                Arrays.fill(buffer, 0)
                len = mFileInputStream.read(buffer)
                if (len > 0 && isPlayer)
                    mAudioTrackImpl.encode(buffer)
                Thread.sleep(5)
            } while (isPlayer && len > 0)
            release()

        }.start()

    }

    private fun release() {
        isPlayer = false
        mAudioTrackImpl?.release()
        mFileInputStream?.close()
        runOnUiThread {
            cleanTime(chronometer)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_audiotrack_player


    override fun onDestroy() {
        release()
        super.onDestroy()

    }
}