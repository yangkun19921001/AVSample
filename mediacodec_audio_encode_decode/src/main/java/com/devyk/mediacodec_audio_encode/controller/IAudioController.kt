package com.devyk.mediacodec_audio_encode.controller

import com.devyk.common.config.AudioConfiguration
import com.devyk.common.callback.OnAudioEncodeListener
/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:31
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IAudioController
 * </pre>
 */
interface IAudioController {
    val sessionId: Int
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun mute(mute: Boolean)
    fun setAudioConfiguration(audioConfiguration: AudioConfiguration)
    fun setAudioEncodeListener(listener: OnAudioEncodeListener?)
}