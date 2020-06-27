package com.devyk.javaavplayer.mediaplay

import android.media.MediaPlayer
import android.view.Surface
import android.view.SurfaceHolder
import com.devyk.common.LogHelper

/**
 * <pre>
 *     author  : devyk on 2020-06-27 11:30
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioMediaPlayer
 * </pre>
 */
public class AudioMediaPlayer : BasePlayer() {

    override fun onPrepared(mp: MediaPlayer?) {
        LogHelper.e(TAG, "prepare")
        listener?.onPrepared()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        LogHelper.e(TAG, "onCompletion")
        listener?.onCompletion()
        release();
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        LogHelper.e(TAG, "onError:what=${what} extra:${extra}")
        listener?.onError()
        return false;
    }


    override fun prepare(url: String, stream_type: Int, surface: SurfaceHolder?) {
        super.prepare(url, stream_type, surface)

    }


    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }

    override fun release() {
        super.release()
    }


    public fun addPlayerListener(listener: IPlayerListener) {
        super.setOnPlayerListener(listener)
    }


}