package com.devyk.mediacodec_audio_encode

import android.media.AudioRecord
import com.devyk.avedit.audio.AudioUtils
import com.devyk.common.config.AudioConfiguration
import com.devyk.common.mediacodec.AACEncoder
import com.devyk.common.callback.OnAudioEncodeListener
import java.util.*

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:18
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioProcessor
 * </pre>
 */
class AudioProcessor(private val mAudioRecord: AudioRecord?, audioConfiguration: AudioConfiguration?) : Thread() {
    @Volatile
    private var mPauseFlag: Boolean = false
    @Volatile
    private var mStopFlag: Boolean = false
    @Volatile
    private var mMute: Boolean = false
    private var mAudioEncoder: AACEncoder? = null
    private val mRecordBuffer: ByteArray
    private val mRecordBufferSize: Int

    /**
     * 初始化
     */
    init {
        mRecordBufferSize = AudioUtils.getMinBufferSize(audioConfiguration!!.frequency, audioConfiguration.channelCount)
        mRecordBuffer = ByteArray(mRecordBufferSize)
        mAudioEncoder = AACEncoder(audioConfiguration)
        mAudioEncoder!!.prepareCoder()
    }


    /**
     * 设置音频硬编码监听
     */
    fun setAudioHEncodeListener(listener: OnAudioEncodeListener?) {
        mAudioEncoder!!.setOnAudioEncodeListener(listener)
    }

    /**
     * 停止
     */
    fun stopEncode() {
        mStopFlag = true
        if (mAudioEncoder != null) {
            mAudioEncoder!!.stop()
            mAudioEncoder = null
        }
    }

    /**
     * 暂停
     */
    fun pauseEncode(pause: Boolean) {
        mPauseFlag = pause
    }

    /**
     * 静音
     */
    fun setMute(mute: Boolean) {
        mMute = mute
    }

    override fun run() {
        while (!mStopFlag) {
            while (mPauseFlag) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            val readLen = mAudioRecord?.read(mRecordBuffer, 0, mRecordBufferSize)
            if (readLen!! > 0) {
                if (mMute) {
                    val clearM: Byte = 0
                    //内部全部是 0 bit
                    Arrays.fill(mRecordBuffer, clearM)
                }
                mAudioEncoder?.enqueueCodec(mRecordBuffer)
            }
        }
    }
}