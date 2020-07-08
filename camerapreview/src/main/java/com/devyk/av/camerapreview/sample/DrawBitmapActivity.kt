package com.devyk.av.camerapreview.sample

import com.devyk.av.camerapreview.R
import com.devyk.ikavedit.base.BaseActivity

/**
 * <pre>
 *     author  : devyk on 2020-07-06 10:23
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is DrawBitmapActivity 绘制一张图片
 * </pre>
 */
public class DrawBitmapActivity : BaseActivity<Int>() {
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

    override fun getLayoutId(): Int = R.layout.activity_bitmap

}