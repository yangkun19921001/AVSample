package com.devyk.mediacodec_audio_encode.mediacodec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import com.devyk.avedit.audio.AudioUtils
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.mediacodec_audio_encode.AudioConfiguration

/**
 * <pre>
 *     author  : devyk on 2020-06-13 15:28
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioMediaCodec
 * </pre>
 */
public class AudioMediaCodec {
    companion object {

        fun selectCodec(mimeType: String): MediaCodecInfo? {
            val numCodecs = MediaCodecList.getCodecCount()
            for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {
                    continue
                }
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (types[j].equals(mimeType, ignoreCase = true)) {
                        return codecInfo
                    }
                }
            }
            return null
        }


        fun getAudioMediaCodec(configuration: AudioConfiguration): MediaCodec? {
            val format =
                MediaFormat.createAudioFormat(configuration.mime, configuration.frequency, configuration.channelCount)
            if (configuration.mime.equals(AudioConfiguration.DEFAULT_MIME)) {
                format.setInteger(MediaFormat.KEY_AAC_PROFILE, configuration.aacProfile)
            }
            format.setInteger(MediaFormat.KEY_BIT_RATE, configuration.maxBps * 1024)
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, configuration.frequency)
            val maxInputSize =
                AudioUtils.getMinBufferSize(configuration.frequency, configuration.channelCount, configuration.encoding)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize)
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, configuration.channelCount)
            var mediaCodec: MediaCodec? = null
            try {
                mediaCodec = MediaCodec.createEncoderByType(configuration.mime)
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            } catch (e: Exception) {
                e.printStackTrace()
                if (mediaCodec != null) {
                    mediaCodec.stop()
                    mediaCodec.release()
                    mediaCodec = null
                }
            }

            return mediaCodec
        }
    }


}