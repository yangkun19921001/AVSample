package com.devyk.avsample

import android.content.Intent
import android.view.View
import com.devyk.av.camerapreview.sample.CameraPreviewActivity
import com.devyk.av.camerapreview.sample.GLSampleActivity
import com.devyk.fdkaac_audio_encode_decode.sample.AudioRecordActivty
import com.devyk.fdkaac_audio_encode_decode.sample.FDKAACDecodeActivity
import com.devyk.fdkaac_audio_encode_decode.sample.FDKAACEncodeActivty
import com.devyk.ffmpeg_audio_encode.sample.FFmpegAACDecodeActivity
import com.devyk.ffmpeg_audio_encode.sample.FFmpegAACEncodeActivity
import com.devyk.ffmpeg_video_encode.sample.FFmpegVideoDecoderActivity
import com.devyk.ffmpeg_video_encode.sample.FFmpegVideoEncoderActivity
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.javaavplayer.sample.AudioMediaPlayActivity
import com.devyk.javaavplayer.sample.AudioTrackPlayerActivity
import com.devyk.javaavplayer.sample.VideoMediaPlayActivity
import com.devyk.javaavplayer.sample.YUVPlayActivity
import com.devyk.lame_audio_encode_decode.sample.FFmpegMp3DecoderActivity
import com.devyk.lame_audio_encode_decode.sample.LameEncoderActivity
import com.devyk.mediacodec_audio_encode.sample.AudioMediaCodecActivity
import com.devyk.mediacodec_video_encode.sample.H264MediaCodecEDActivity
import com.devyk.nativeavplayer.sample.NativeAudioPlayer
import com.devyk.nativeavplayer.sample.NativeVideoPlayer
import com.devyk.x264_video_encode.sample.x264EncodeActivity

class MainActivity : BaseActivity<Int>() {
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
     * ffmpeg 视频编码 YUV420P(I420) - > H264
     */
    fun ffmpeg_video_encode(view: View) {
        startActivity(Intent(this, FFmpegVideoEncoderActivity::class.java))
    }

    /**
     * ffmpeg 视频编码  H264 - > YUV420P(I420)
     */
    fun ffmpeg_video_decode(view: View) {
        startActivity(Intent(this, FFmpegVideoDecoderActivity::class.java))
    }

    /**
     * ffmpeg Mp3编码  pcm - > mp3
     */
    fun lame_mp3_encode(view: View) {
        startActivity(Intent(this, LameEncoderActivity::class.java))
    }

    /**
     * ffmpeg Mp3编码  mp3 - > pcm
     */
    fun ffmpeg_mp3_decode(view: View) {
        startActivity(Intent(this, FFmpegMp3DecoderActivity::class.java))
    }

    /**
     * Android mediaplayer API 实现音频播放
     */
    fun mediaplayer_audio(view: View) {
        startActivity(Intent(this, AudioMediaPlayActivity::class.java))
    }

    /**
     * Android mediaplayer API 实现视频播放
     */
    fun mediaplayer_video(view: View) {
        startActivity(Intent(this, VideoMediaPlayActivity::class.java))
    }

    /**
     * Android AudioTrack API 实现音频播放
     */
    fun audiotrack_play(view: View) {
        startActivity(Intent(this, AudioTrackPlayerActivity::class.java))
    }

    /**
     * Android AudioTrack API 实现音频播放
     */
    fun gles_yuv_play(view: View) {
        startActivity(Intent(this, YUVPlayActivity::class.java))
    }

    /**
     * native OpenSL ES 实现 PCM 播放
     */
    fun native_audio_play(view: View){
        startActivity(Intent(this,NativeAudioPlayer::class.java))
    }

    /**
     * native OpenGL ES 实现 YUV 播放
     */
    fun native_video_play(view: View){
//        ToastUtils.showLong("参考：https://github.com/yangkun19921001/AVEditer/tree/master/avedit/src/main/cpp/avplay/video")
        startActivity(Intent(this, NativeVideoPlayer::class.java))
    }

    /**
     * 实战项目：OpenGL ES 实现 Camera 预览
     */
    fun camera_preview(view: View){
        startActivity(Intent(this, GLSampleActivity::class.java))
    }
}
