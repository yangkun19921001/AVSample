package com.devyk.av.camera_recorder.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import com.devyk.av.camera_recorder.config.RendererConfiguration
import com.devyk.av.camera_recorder.egl.renderer.CameraRenderer
import com.devyk.av.camera_recorder.widget.base.GLSurfaceView
import android.view.WindowManager
import com.devyk.av.camera_recorder.callback.ICameraOpenListener
import com.devyk.common.LogHelper
import com.devyk.common.camera.CameraHolder
import com.devyk.common.config.CameraConfiguration


/**
 * <pre>
 *     author  : devyk on 20-07-07 22:47
 *     blog    : https://juejin.im/user/578259398ac24700613a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is CameraView
 * </pre>
 */
public open class CameraView : GLSurfaceView, SurfaceTexture.OnFrameAvailableListener {


    /**
     * Camera 渲染器
     */
    protected lateinit var renderer: CameraRenderer


    /**
     * 相机预览的纹理 ID
     */
    protected var mTextureId = -1;

    protected var mCameraOpenListener:ICameraOpenListener? = null

    /**
     * 默认后置摄像头
     */
    private var cameraId = CameraConfiguration.Facing.BACK

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        renderer = CameraRenderer(context!!)
        configure(RendererConfiguration.Builder().setRenderer(renderer).setRendererMode(RENDERERMODE_CONTINUOUSLY).build())
        //第一次需要初始化预览角度
        previewAngle(context)
        addRendererListener()
    }


    private fun addRendererListener() {
        renderer.setOnRendererListener(object : CameraRenderer.OnRendererListener {
            override fun onCreate(cameraTextureId: Int, textureID: Int) {
                mTextureId = textureID
                val cameraConfiguration =
                    CameraConfiguration.Builder().setFacing(cameraId).build()
                CameraHolder.instance().setConfiguration(cameraConfiguration)
                CameraHolder.instance().openCamera()
                CameraHolder.instance().setSurfaceTexture(cameraTextureId, this@CameraView);
                CameraHolder.instance().startPreview();
                LogHelper.e(TAG,"TextureId:${mTextureId}")
                mCameraOpenListener?.onCameraOpen()
            }

            override fun onDraw() {
                CameraHolder.instance().updateTexImage()
            }
        })
    }

    /**
     * 释放 Camera 资源的时候调用
     */
    public open fun releaseCamera() {
        CameraHolder.instance().stopPreview()
        CameraHolder.instance().releaseCamera()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
    }

    fun previewAngle(context: Context) {
        val rotation =
            (context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        LogHelper.d(TAG, "旋转角度：" + rotation)
        renderer.resetMatrix()
        when (rotation) {

            Surface.ROTATION_0 -> {
                if (cameraId == CameraConfiguration.Facing.BACK) {
                    renderer.setAngle(90, 0, 0, 1);
                    renderer.setAngle(180, 1, 0, 0);
                } else {
                    renderer.setAngle(90, 0, 0, 1);
                }
            }

            Surface.ROTATION_90 -> {
                if (cameraId == CameraConfiguration.Facing.BACK) {
                    renderer.setAngle(180, 0, 0, 1);
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(90, 0, 0, 1);
                }
            }

            Surface.ROTATION_180 -> {
                if (cameraId == CameraConfiguration.Facing.BACK) {
                    renderer.setAngle(90, 0, 0, 1);
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(-90, 0, 0, 1);
                }
            }

            Surface.ROTATION_270 -> {
                if (cameraId == CameraConfiguration.Facing.BACK) {
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(0, 0, 0, 1);
                }
            }
        }
    }

    /**
     * 拿到纹理 ID
     */
    public fun getTextureId(): Int = mTextureId


    public fun addCameraOpenCallback(listener:ICameraOpenListener){
        mCameraOpenListener = listener
    }

}