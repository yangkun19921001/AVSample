package com.devyk.javaavplayer.mediaplay

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import android.view.Surface
import android.view.SurfaceHolder

/**
 * <pre>
 *     author  : devyk on 2020-06-27 11:21
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BasePlayer
 * </pre>
 */
public abstract class BasePlayer : IMediaplayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {


    protected var TAG = this.javaClass.simpleName


    override fun getMediaPlayer(): MediaPlayer = mMediaPlayer

    private lateinit var mMediaPlayer: MediaPlayer
    protected lateinit var listener: IPlayerListener

    private var PLAY_STATUS = 0;


    override fun prepare(url: String, stream_type: Int, surface: SurfaceHolder?) {
        mMediaPlayer = MediaPlayer();
        mMediaPlayer.setDataSource(url)
        var attr = AudioAttributes.Builder()
            .setContentType(stream_type)
            .build()
        mMediaPlayer.setAudioAttributes(attr)
        getMediaPlayer().setOnPreparedListener(this)
        surface?.run {
            mMediaPlayer.setDisplay(surface)
        }
        mMediaPlayer.prepare()
        PLAY_STATUS = 1;

    }


    override fun start() {
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
        mMediaPlayer.start()
    }

    override fun stop() {
        mMediaPlayer.stop()
    }

    override fun release() {
        if (PLAY_STATUS == 0) return
        mMediaPlayer.release()
        mMediaPlayer == null
        listener == null
        PLAY_STATUS = 0;

    }


    public interface IPlayerListener {
        fun onPrepared();
        fun onCompletion();
        fun onError();
    }

    public fun setOnPlayerListener(listener: IPlayerListener) {
        this.listener = listener
    }
}