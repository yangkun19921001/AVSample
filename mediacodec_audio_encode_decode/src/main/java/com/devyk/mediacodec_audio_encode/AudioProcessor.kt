package com.devyk.mediacodec_audio_encode

import android.media.AudioRecord
import com.devyk.avedit.audio.AudioUtils
import com.devyk.mediacodec_audio_encode.mediacodec.AudioEncoder
import com.devyk.mediacodec_audio_encode.mediacodec.OnAudioEncodeListener
import java.io.FileOutputStream
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
    private var mAudioEncoder: AudioEncoder? = null
    private val mRecordBuffer: ByteArray
    private val mRecordBufferSize: Int


    private var mFileOutputStream:FileOutputStream? = null

    init {
        mRecordBufferSize = AudioUtils.getMinBufferSize(audioConfiguration!!.frequency,audioConfiguration.channelCount)
        mRecordBuffer = ByteArray(mRecordBufferSize)
        mAudioEncoder = AudioEncoder(audioConfiguration)
        mAudioEncoder!!.prepareEncoder()
//        mFileOutputStream = FileOutputStream("/sdcard/avsample/test.pcm");
    }


    fun setAudioHEncodeListener(listener: OnAudioEncodeListener?) {
        mAudioEncoder!!.setOnAudioEncodeListener(listener)
    }

    fun stopEncode() {
        mStopFlag = true
        if (mAudioEncoder != null) {
            mAudioEncoder!!.stop()
            mAudioEncoder = null
        }
    }

    fun pauseEncode(pause: Boolean) {
        mPauseFlag = pause
    }

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
                    Arrays.fill(mRecordBuffer, clearM)
                }
                if (mAudioEncoder != null) {
//                    mFileOutputStream?.write(mRecordBuffer)
                    mAudioEncoder!!.offerEncoder(mRecordBuffer)
                }
            }
        }
    }
}