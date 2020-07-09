package com.devyk.common.config

import android.media.AudioFormat
import android.media.MediaCodecInfo

/**
 * <pre>
 *     author  : devyk on 2020-06-13 15:26
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioConfiguration
 * </pre>
 */
class AudioConfiguration private constructor(builder: Builder) {

    val minBps: Int
    val maxBps: Int
    val frequency: Int
    val encoding: Int
    val codeType: CodeType
    val channelCount: Int
    val adts: Int
    val aacProfile: Int
    val mime: String
    val aec: Boolean
    val mediaCodec: Boolean

    init {
        minBps = builder.minBps
        maxBps = builder.maxBps
        frequency = builder.frequency
        encoding = builder.encoding
        codeType = builder.codeType
        channelCount = builder.channelCount
        adts = builder.adts
        mime = builder.mime
        aacProfile = builder.aacProfile
        aec = builder.aec
        mediaCodec = builder.mediaCodec
    }

    class Builder {
        public var mediaCodec = DEFAULT_MEDIA_CODEC
        public var minBps = DEFAULT_MIN_BPS
        public var maxBps = DEFAULT_MAX_BPS
        public var frequency = DEFAULT_FREQUENCY
        public var encoding = DEFAULT_AUDIO_ENCODING
        public var channelCount = DEFAULT_CHANNEL_COUNT
        public var adts = DEFAULT_ADTS
        public var mime = DEFAULT_MIME
        public var codeType = DEFAULT_CODE_TYPE
        public var aacProfile = DEFAULT_AAC_PROFILE
        public var aec = DEFAULT_AEC

        fun setBps(minBps: Int, maxBps: Int): Builder {
            this.minBps = minBps
            this.maxBps = maxBps
            return this
        }

        fun setFrequency(frequency: Int): Builder {
            this.frequency = frequency
            return this
        }

        fun setEncoding(encoding: Int): Builder {
            this.encoding = encoding
            return this
        }

        fun setChannelCount(channelCount: Int): Builder {
            this.channelCount = channelCount
            return this
        }

        fun setAdts(adts: Int): Builder {
            this.adts = adts
            return this
        }

        fun setAacProfile(aacProfile: Int): Builder {
            this.aacProfile = aacProfile
            return this
        }

        fun setMime(mime: String): Builder {
            this.mime = mime
            return this
        }

        fun setAec(aec: Boolean): Builder {
            this.aec = aec
            return this
        }

        fun setMediaCodec(meidaCodec: Boolean): AudioConfiguration.Builder {
            this.mediaCodec = meidaCodec
            return this
        }

        fun setCodecType(codeType: CodeType): Builder {
            this.codeType = codeType
            return this
        }

        fun build(): AudioConfiguration {
            return AudioConfiguration(this)
        }
    }

    companion object {
        val DEFAULT_FREQUENCY = 44100
        val DEFAULT_MAX_BPS = 64
        val DEFAULT_MIN_BPS = 32
        val DEFAULT_ADTS = 0
        val DEFAULT_CODE_TYPE = CodeType.ENCODE
        val DEFAULT_MIME = "audio/mp4a-latm"
        val DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        val DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC
        val DEFAULT_CHANNEL_COUNT = 1
        val DEFAULT_AEC = false

        val DEFAULT_MEDIA_CODEC = true

        fun createDefault(): AudioConfiguration {
            return Builder().build()
        }
    }


    public enum class CodeType(codeType: Int) {
        ENCODE(1),
        DECODE(2),
    }
}
