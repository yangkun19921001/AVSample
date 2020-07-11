package com.devyk.av.camera_recorder.sample

import android.content.res.Configuration
import com.devyk.av.camera_recorder.R
import com.devyk.ikavedit.base.BaseActivity
import kotlinx.android.synthetic.main.activity_camera.*

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