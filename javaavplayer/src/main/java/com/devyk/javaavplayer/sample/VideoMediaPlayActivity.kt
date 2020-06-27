package com.devyk.javaavplayer.sample

import android.media.AudioManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.javaavplayer.R
import com.devyk.javaavplayer.mediaplay.VideoMediaPlayer
import com.devyk.javaavplayer.mediaplay.BasePlayer

/**
 * <pre>
 *     author  : devyk on 2020-06-27 11:50
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoMediaPlayActivity
 * </pre>
 */
public class VideoMediaPlayActivity : BaseActivity(), BasePlayer.IPlayerListener {

    private lateinit var chronometer: Chronometer

    private val VIDEO_PATH = "sdcard/zhangjie_mo.mp4"


    override fun onPrepared() {
        startTime(chronometer)


    }

    override fun onCompletion() {
        cleanTime(chronometer)
        mVideoMediaPlayer.release()
    }

    override fun onError() {

    }

    protected lateinit var mVideoMediaPlayer: VideoMediaPlayer


    override fun initListener() {
    }

    override fun initData() {
    }

    override fun init() {
        var btn_start = findViewById<Button>(R.id.btn_start);
        var btn_stop = findViewById<Button>(R.id.btn_stop);
        chronometer = findViewById<Chronometer>(R.id.timer);
        var surface = findViewById<SurfaceView>(R.id.surface);
        mVideoMediaPlayer = VideoMediaPlayer()
        mVideoMediaPlayer.addPlayerListener(this)
        btn_start.setOnClickListener {
            mVideoMediaPlayer.prepare(VIDEO_PATH, AudioManager.STREAM_MUSIC, surface.holder)
            mVideoMediaPlayer.start()

        }
        btn_stop.setOnClickListener {
            cleanTime(chronometer)
            mVideoMediaPlayer.release()
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_video_mediaplayer


    override fun onDestroy() {
        super.onDestroy()
        mVideoMediaPlayer?.release()

    }
}