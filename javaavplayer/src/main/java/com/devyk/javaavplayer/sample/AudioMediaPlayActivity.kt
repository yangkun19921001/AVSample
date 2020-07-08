package com.devyk.javaavplayer.sample

import android.media.AudioManager
import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.javaavplayer.R
import com.devyk.javaavplayer.mediaplay.AudioMediaPlayer
import com.devyk.javaavplayer.mediaplay.BasePlayer

/**
 * <pre>
 *     author  : devyk on 2020-06-27 11:49
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioMediaPlayActivity
 * </pre>
 */
public class AudioMediaPlayActivity : BaseActivity<Int>(), BasePlayer.IPlayerListener {

    private lateinit var chronometer: Chronometer


    override fun onPrepared() {
        startTime(chronometer)

    }

    override fun onCompletion() {
        cleanTime(chronometer)
        mAudioMediaPlayer.release()
    }

    override fun onError() {

    }

    protected lateinit var mAudioMediaPlayer: AudioMediaPlayer


    private val AUDIO_PATH = "sdcard/avsample/lame_encode.mp3"


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
        mAudioMediaPlayer = AudioMediaPlayer()
        mAudioMediaPlayer.addPlayerListener(this)
        btn_start.setOnClickListener {
            mAudioMediaPlayer.prepare(AUDIO_PATH, AudioManager.STREAM_MUSIC, null)
            mAudioMediaPlayer.start()

        }
        btn_stop.setOnClickListener {
            cleanTime(chronometer)
            mAudioMediaPlayer.release()
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_audio_mediaplayer


    override fun onDestroy() {
        super.onDestroy()
        mAudioMediaPlayer?.release()

    }
}