package com.devyk.av.camerapreview.sample

import android.content.res.Configuration
import android.opengl.GLES20
import android.view.SurfaceHolder
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ScreenUtils
import com.devyk.av.camerapreview.R
import com.devyk.av.camerapreview.egl.EglHelper
import com.devyk.ikavedit.base.BaseActivity
import kotlinx.android.synthetic.main.activity_camera.*
import javax.microedition.khronos.egl.EGL
import javax.microedition.khronos.egl.EGL10

/**
 * <pre>
 *     author  : devyk on 2020-07-04 14:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is CameraPreviewActivity 相机预览
 * </pre>
 */

public class CameraPreviewActivity : BaseActivity<Int>() {


    override fun initListener() {
    }

    override fun initData() {
    }

    override fun init() {
    }

    override fun onContentViewBefore() {
        super.onContentViewBefore()
        setNotTitleBar()
    }

    override fun getLayoutId(): Int = R.layout.activity_camera


    override fun onDestroy() {
        super.onDestroy()
        camera_view.releaseCamera()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        camera_view.previewAngle(this)
    }
}