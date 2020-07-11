package com.devyk.av.camera_recorder.controller.audio

import android.media.AudioRecord
import android.media.MediaFormat
import android.media.MediaRecorder
import android.util.Log
import com.devyk.av.camera_recorder.controller.IBaseController
import com.devyk.avedit.audio.AudioUtils
import com.devyk.common.LogHelper
import com.devyk.common.callback.OnAudioEncodeListener
import com.devyk.common.config.AudioConfiguration

/**
 * <pre>
 *     author  : devyk on 2020-07-11 14:46
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioController 对音频流的控制
 * </pre>
 */
public class AudioController : IBaseController {
    private var mListener: OnAudioEncodeListener? = null
    private var mAudioRecord: AudioRecord? = null
    private var mAudioProcessor: AudioRecordManager? = null
    private var mMute: Boolean = false
    private var mAudioConfiguration = AudioConfiguration.createDefault()
    private var TAG = this.javaClass.simpleName

    init {
        mAudioConfiguration = AudioConfiguration.createDefault()
    }

    fun setAudioConfiguration(audioConfiguration: AudioConfiguration) {
        mAudioConfiguration = audioConfiguration
    }

    fun setAudioEncodeListener(listener: OnAudioEncodeListener?) {
        mListener = listener
    }

    override fun start() {
        LogHelper.d(TAG, "Audio Recording start")
        mAudioConfiguration?.let { config ->
            AudioUtils.initAudioRecord(
                MediaRecorder.AudioSource.MIC,
                config.frequency,
                config.channelCount,
                config.encoding
            )
        }

        mAudioRecord = AudioUtils.getAudioRecord()
        try {
            mAudioRecord?.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mAudioProcessor = AudioRecordManager(mAudioRecord, mAudioConfiguration)
        mAudioProcessor?.run {
            setAudioHEncodeListener(mListener)
            start()
            setMute(mMute)
        }

    }

    override fun stop() {
        mAudioProcessor?.stopEncode()
        mAudioRecord?.run {
            stop()
            release()
        }
        mAudioRecord = null
    }

    override fun pause() {
        mAudioRecord?.stop()
        mAudioProcessor?.pauseEncode(true)
    }

    override fun resume() {
        mAudioRecord?.startRecording()
        mAudioProcessor?.pauseEncode(false)
    }

    fun mute(mute: Boolean) {
        Log.d(TAG, "Audio Recording mute: $mute")
        mMute = mute
        mAudioProcessor?.setMute(mMute)
    }

    fun getOutputFormat(): MediaFormat? = mAudioProcessor?.getOutputFormat()

}