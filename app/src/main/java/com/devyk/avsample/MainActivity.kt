package com.devyk.avsample

import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.GsonUtils
import com.devyk.common.FindFiles
import com.devyk.fdkaac_audio_encode_decode.sample.AudioRecordActivty
import com.devyk.fdkaac_audio_encode_decode.sample.FDKAACDecodeActivity
import com.devyk.fdkaac_audio_encode_decode.sample.FDKAACEncodeActivty
import com.devyk.ffmpeg_audio_encode.sample.FFmpegAACDecodeActivity
import com.devyk.ffmpeg_audio_encode.sample.FFmpegAACEncodeActivity
import com.devyk.ffmpeg_video_encode.FFmpegVideoEncoderActivity
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.mediacodec_audio_encode.sample.AudioMediaCodecActivity
import com.devyk.mediacodec_video_encode.sample.H264MediaCodecEDActivity
import com.devyk.x264_video_encode.sample.x264EncodeActivity

class MainActivity : BaseActivity() {
    override fun initListener() {
    }

    override fun initData() {
    }

    override fun init() {
        checkPermission()
    }

    override fun getLayoutId(): Int = R.layout.activity_main


    /**
     * 音频采集
     */
    fun AudioCapture(view: View) {
        startActivity(Intent(this, AudioRecordActivty::class.java))
    }

    /**
     * FDK-AAC 音频编码
     */
    fun fdkaac_encode(view: View) {
        startActivity(Intent(this, FDKAACEncodeActivty::class.java))

    }

    /**
     * FDK-AAC 音频解码
     */
    fun fdkaac_decode(view: View) {
        startActivity(Intent(this, FDKAACDecodeActivity::class.java))

    }

    /**
     * libx264 编码
     */
    fun x264_encode(view: View) {
        startActivity(Intent(this, x264EncodeActivity::class.java))

    }

    /**
     * AAC 硬编码
     */
    fun mediacode_audio_ed(view: View) {
        startActivity(Intent(this, AudioMediaCodecActivity::class.java))

    }

    /**
     * h264 硬编码
     */
    fun mediacode_video_ed(view: View) {
        startActivity(Intent(this, H264MediaCodecEDActivity::class.java))
    }

    /**
     * ffmpeg 音频编码 AAC
     */
    fun ffmpeg_audio_encode(view: View) {
        startActivity(Intent(this, FFmpegAACEncodeActivity::class.java))
    }

    /**
     * ffmpeg 音频解码 AAC-PCM
     */
    fun ffmpeg_audio_decode(view: View) {
        startActivity(Intent(this, FFmpegAACDecodeActivity::class.java))
    }

    /**
     * ffmpeg 音频解码 AAC-PCM
     */
    fun ffmpeg_video_encode(view: View) {
        startActivity(Intent(this, FFmpegVideoEncoderActivity::class.java))
    }
}
