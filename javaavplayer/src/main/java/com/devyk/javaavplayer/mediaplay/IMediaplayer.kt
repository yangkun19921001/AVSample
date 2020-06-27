package com.devyk.javaavplayer.mediaplay

import android.media.AudioManager
import android.media.MediaPlayer
import android.view.Surface
import android.view.SurfaceHolder

/**
 * <pre>
 *     author  : devyk on 2020-06-27 11:11
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IMediaplayer
 * </pre>
 *
 *
 *
 * @see https://developer.android.com/guide/topics/media/mediaplayer?hl=zh-cn
 */

public interface IMediaplayer {

    /**
     * 播放的准备工作
     */
   abstract fun prepare(url: String, stream_type: Int = AudioManager.STREAM_MUSIC, surface: SurfaceHolder?)

    abstract fun getMediaPlayer():MediaPlayer

    abstract fun start()

    abstract  fun stop()

    abstract  fun release()


}