package com.devyk.av.camera_recorder.controller

import android.content.Context
import android.media.MediaFormat
import com.devyk.av.camera_recorder.controller.audio.AudioController
import com.devyk.av.camera_recorder.controller.video.CameraVideoController
import com.devyk.common.callback.OnAudioEncodeListener
import com.devyk.common.callback.OnVideoEncodeListener
import com.devyk.common.config.AudioConfiguration
import com.devyk.common.config.VideoConfiguration
import javax.microedition.khronos.egl.EGLContext

/**
 * <pre>
 *     author  : devyk on 2020-07-09 23:24
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is StreamController 音视频编码完成的流控制
 * </pre>
 */
public open class StreamController(context: Context, textureId: Int, eglContext: EGLContext) : IBaseController {


    /**
     * 音频流管理
     */
    private var mAudioController: AudioController? = null

    /**
     * 视频流管理
     */
    private var mVideoController: CameraVideoController? = null


    init {
        mAudioController = AudioController()
        mVideoController = CameraVideoController(context, textureId, eglContext)
    }

    override fun start() {
        setVideoConfiguration()
        mAudioController?.start()
        mVideoController?.start()
    }

    override fun stop() {
        mAudioController?.stop()
        mVideoController?.stop()
    }

    override fun pause() {
        mAudioController?.pause()
        mVideoController?.pause()
    }

    override fun resume() {
        mAudioController?.resume()
        mVideoController?.resume()
    }


    /**
     * 设置视频配置参数
     */
    fun setVideoConfiguration(videoConfiguration: VideoConfiguration = VideoConfiguration.createDefault()) {
        mVideoController?.setVideoConfiguration(videoConfiguration)
    }

    /**
     * 获取音视频编码输出格式
     */
    public fun getVideoOutputFormat(): MediaFormat? = mVideoController?.getOutputFormat()
    public fun getAudioOutputFormat(): MediaFormat? = mAudioController?.getOutputFormat()

    /**
     * 设置音频配置参数
     */
    fun setAudioConfiguration(audioConfiguration: AudioConfiguration = AudioConfiguration.createDefault()) {
        mAudioController?.setAudioConfiguration(audioConfiguration)
    }

    /**
     * 设置音频数据流的监听
     */
    fun setAudioEncodeListener(listener: OnAudioEncodeListener?) {
        mAudioController?.setAudioEncodeListener(listener)
    }

    fun setOnVideoEncodeListener(listener: OnVideoEncodeListener) {
        mVideoController?.setOnVideoEncodeListener(listener)
    }



}
