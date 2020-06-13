package com.devyk.mediacodec_audio_encode.controller

import android.annotation.TargetApi
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.devyk.avedit.audio.AudioUtils
import com.devyk.mediacodec_audio_encode.AudioConfiguration
import com.devyk.mediacodec_audio_encode.AudioProcessor
import com.devyk.mediacodec_audio_encode.mediacodec.OnAudioEncodeListener

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:32
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioControllerImpl
 * </pre>
 */
class AudioControllerImpl : IAudioController {
    private var mListener: OnAudioEncodeListener? = null
    private var mAudioRecord: AudioRecord? = null
    private var mAudioProcessor: AudioProcessor? = null
    private var mMute: Boolean = false
    private var mAudioConfiguration: AudioConfiguration? = null
    private var TAG = this.javaClass.simpleName

    override val sessionId: Int
        @TargetApi(16)
        get() = if (mAudioRecord != null) {
            mAudioRecord!!.audioSessionId
        } else {
            -1
        }

    init {
        mAudioConfiguration = AudioConfiguration.createDefault()
    }

    override fun setAudioConfiguration(audioConfiguration: AudioConfiguration) {
        mAudioConfiguration = audioConfiguration
    }

    override fun setAudioEncodeListener(listener: OnAudioEncodeListener?) {
        mListener = listener
    }

    override fun start() {
        Log.d(TAG, "Audio Recording start")
        AudioUtils.initAudioRecord(MediaRecorder.AudioSource.MIC,mAudioConfiguration!!.frequency,mAudioConfiguration!!.channelCount,mAudioConfiguration!!.encoding)
        mAudioRecord = AudioUtils.getAudioRecord()
        try {
            mAudioRecord?.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mAudioProcessor = AudioProcessor(mAudioRecord, mAudioConfiguration)
        mAudioProcessor?.setAudioHEncodeListener(mListener)
        mAudioProcessor?.start()
        mAudioProcessor?.setMute(mMute)
    }

    override fun stop() {
        Log.d(TAG, "Audio Recording stop")
        if (mAudioProcessor != null) {
            mAudioProcessor!!.stopEncode()
        }
        if (mAudioRecord != null) {
            try {
                mAudioRecord!!.stop()
                mAudioRecord!!.release()
                mAudioRecord = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun pause() {
        Log.d(TAG, "Audio Recording pause")
        if (mAudioRecord != null) {
            mAudioRecord!!.stop()
        }
        if (mAudioProcessor != null) {
            mAudioProcessor!!.pauseEncode(true)
        }
    }

    override fun resume() {
        Log.d(TAG, "Audio Recording resume")
        if (mAudioRecord != null) {
            mAudioRecord!!.startRecording()
        }
        if (mAudioProcessor != null) {
            mAudioProcessor!!.pauseEncode(false)
        }
    }

    override fun mute(mute: Boolean) {
        Log.d(TAG, "Audio Recording mute: $mute")
        mMute = mute
        if (mAudioProcessor != null) {
            mAudioProcessor!!.setMute(mMute)
        }
    }
}