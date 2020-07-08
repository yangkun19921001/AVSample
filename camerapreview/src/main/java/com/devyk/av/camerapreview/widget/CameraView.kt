package com.devyk.av.camerapreview.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import android.view.Surface
import com.devyk.av.camerapreview.config.RendererConfiguration
import com.devyk.av.camerapreview.egl.renderer.CameraRenderer
import com.devyk.av.camerapreview.widget.base.GLSurfaceView
import android.view.WindowManager
import com.devyk.common.LogHelper
import com.devyk.common.camera.CameraHolder


/**
 * <pre>
 *     author  : devyk on 20-07-07 22:47
 *     blog    : https://juejin.im/user/578259398ac24700613a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is CameraView
 * </pre>
 */
public class CameraView : GLSurfaceView, SurfaceTexture.OnFrameAvailableListener {


    protected lateinit var renderer: CameraRenderer


    private var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        renderer = CameraRenderer(context!!)
        configure(RendererConfiguration.Builder().setRenderer(renderer).setRendererMode(RENDERERMODE_CONTINUOUSLY).build())
        //第一次需要初始化预览角度
        if (CameraHolder.instance().isOpenBackFirst()) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
        }
        previewAngle(context)
        addRendererListener()
    }


    private fun addRendererListener() {
        renderer.setOnRendererListener(object : CameraRenderer.OnRendererListener {
            override fun onCreate(textureId: Int) {
                CameraHolder.instance().openCamera()
                CameraHolder.instance().setSurfaceTexture(textureId, this@CameraView);
                CameraHolder.instance().startPreview();


            }

            override fun onDraw() {
                CameraHolder.instance().updateTexImage()
            }
        })
    }


    /**
     * 释放 Camera 资源的时候调用
     */
    public fun releaseCamera() {
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
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    renderer.setAngle(90, 0, 0, 1);
                    renderer.setAngle(180, 1, 0, 0);
                } else {
                    renderer.setAngle(90, 0, 0, 1);
                }
            }
            Surface.ROTATION_90 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    renderer.setAngle(180, 0, 0, 1);
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(90, 0, 0, 1);
                }
            }
            Surface.ROTATION_180 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    renderer.setAngle(90, 0, 0, 1);
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(-90, 0, 0, 1);
                }
            }
            Surface.ROTATION_270 -> {
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    renderer.setAngle(180, 0, 1, 0);
                } else {
                    renderer.setAngle(0, 0, 0, 1);
                }

            }
        }

    }
}