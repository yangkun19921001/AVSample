package com.devyk.av.camera_recorder.controller.video

import android.content.Context
import android.media.MediaFormat
import com.devyk.av.camera_recorder.controller.IBaseController
import com.devyk.common.callback.OnVideoEncodeListener
import com.devyk.common.config.VideoConfiguration
import javax.microedition.khronos.egl.EGLContext

/**
 * <pre>
 *     author  : devyk on 2020-07-11 14:44
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoController 对视频流的控制
 * </pre>
 */
public class CameraVideoController(context: Context, textureId: Int, eglContext: EGLContext) : IBaseController {


    private var mCameraVideoController: CameraRecorder? = null

    init {
        mCameraVideoController = CameraRecorder(context, textureId, eglContext)
    }


    fun setVideoConfiguration(videoConfiguration: VideoConfiguration = VideoConfiguration.createDefault()) {
        mCameraVideoController?.prepare(videoConfiguration)
    }

    override fun start() {
        mCameraVideoController?.start()
    }

    override fun stop() {
        mCameraVideoController?.stop()
    }

    override fun pause() {
        mCameraVideoController?.pause()
    }

    override fun resume() {
        mCameraVideoController?.resume()
    }


    public fun getOutputFormat():MediaFormat? = mCameraVideoController?.getOutputFormat()


    /**
     * 设置编码回调
     */
    fun setOnVideoEncodeListener(listener: OnVideoEncodeListener) {
        mCameraVideoController?.setOnVideoEncodeListener(listener)
    }

    fun setVideoBps(bps: Int) {
        mCameraVideoController?.setEncodeBps(bps)
    }

}