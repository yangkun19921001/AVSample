package com.devyk.avedit.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.lang.RuntimeException
import java.nio.ByteBuffer

/**
 * <pre> 音频管理类
 *     author  : devyk on 2020-05-28 20:09
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioUtils
 * </pre>
 *
 *
 */

/**
 * 调用 startRecording 开始录音，调用 stopRecord 停止录音
 * 利用adb pull 导出PCM文件
 * 	adb pull record .
 * 利用ffplay播放声音
 * 		ffplay -f s16le  -sample_rate 44100  -channels 1 -i /record.pcm
 * 利用ffmpeg将PCM文件转换为WAV文件
 * 		ffmpeg -f s16le  -sample_rate 44100  -channels 1 -i record.pcm -acodec pcm_s16le record.wav
 */
public class AudioUtils {

    private var TAG = javaClass.simpleName;


    /**
     * 录音对象
     * @see AudioRecord
     */
    private var mAudioRecord: AudioRecord? = null;

    /**
     * 声音通道
     * 默认 单声道 个
     * @see AudioFormat.CHANNEL_IN_MONO 单声道
     * @see AudioFormat.CHANNEL_IN_STEREO 立体声
     */
    private var AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO

    /**
     * 采样率 如果 AudioRecord 初始化失败，那么可以降低为 16000 ，或者检查权限是否开启
     * 默认 44100
     */
    private var SAMPLE_RATE_IN_HZ = 44100

    /**
     * 采样格式
     * 默认 16bit 存储
     *
     * @see AudioFormat.ENCODING_PCM_16BIT 兼容大部分手机
     */
    private var AUDIO_FROMAT = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 录音源
     * @see MediaRecorder.AudioSource.MIC 手机麦克风
     * @see MediaRecorder.AudioSource.VOICE_RECOGNITION 用于语音识别，等同于默认
     * @see MediaRecorder.AudioSource.VOICE_COMMUNICATION 用于 VOIP 应用
     */
    private var AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /**
     * 配置内部音频缓冲区的大小，由于不同厂商会有不同的实现。那么我们可以通过一个静态函数来 getMinBufferSize 来定义
     * @see AudioRecord.getMinBufferSize
     */
    private var mBufferSizeInBytes = 0;


    /**
     * 获取音频缓冲区大小
     */
    public fun getMinBufferSize(
        sampleRateInHz: Int = SAMPLE_RATE_IN_HZ,
        channelConfig: Int = AUDIO_CHANNEL_CONFIG,
        audioFormat: Int = AUDIO_FROMAT
    ): Int = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

    /**
     * 拿到 AudioRecord 对象
     */
    public fun initAudioRecord(
        audioSource: Int = AUDIO_SOURCE,
        sampleRateInHz: Int = SAMPLE_RATE_IN_HZ,
        channelConfig: Int = AUDIO_CHANNEL_CONFIG,
        audioFormat: Int = AUDIO_FROMAT
    ): Boolean {
        //如果 AudioRecord 不为 null  那么直接销毁
        mAudioRecord?.run {
            release();
        }
        try {
            //得到录音缓冲大小
            mBufferSizeInBytes = getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            mAudioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mBufferSizeInBytes)
        } catch (error: Exception) {
            Log.e(TAG, "AudioRecord init error :${error.message}")
            return false
        }

        //如果初始化失败那么降低采样率
        if (mAudioRecord == null || mAudioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw RuntimeException("检查音频源是否为占用，或者是否打开录音权限？")
        }
        return true
    }

    /**
     * 开始录制
     */
    public fun startRecord() {
        mAudioRecord?.run {
            if (state == AudioRecord.STATE_INITIALIZED)
                startRecording()
        }

    }

    /**
     * 开始录制
     */
    public fun stopRecord() {
        mAudioRecord?.run {
            if (mAudioRecord?.state == AudioRecord.STATE_INITIALIZED)
                stop()
        }
    }

    /**
     * 释放资源
     */
    public fun releaseRecord() {
        mAudioRecord?.run {
            release()
            null
        }
    }

    /**
     * 读取音频数据
     */
    public fun read(bufferSize: Int = mBufferSizeInBytes, offsetInBytes: Int = 0, byte: ByteArray): Int {
        var ret = 0;
        mAudioRecord?.run {
            ret = read(byte, offsetInBytes, bufferSize)
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public fun read(bufferSize: Int = mBufferSizeInBytes, offsetInBytes: Int = 0, short: ShortArray): Int {
        var ret = 0;
        mAudioRecord?.run {
            ret = read(short, offsetInBytes, bufferSize)
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public fun read(bufferSize: Int = mBufferSizeInBytes, buffer: ByteBuffer): Int {
        var ret = 0;
        mAudioRecord?.run {
            ret = read(buffer, bufferSize)
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public fun read(bufferSize: Int = mBufferSizeInBytes, buffer: ByteArray): Int {
        var ret = 0;
        mAudioRecord?.run {
            ret = read(buffer, 0, bufferSize)
        }
        return ret;
    }


    /**
     * 拿到缓冲大小
     */
    public fun getBufferSize(): Int = mBufferSizeInBytes


}