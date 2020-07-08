package com.devyk.av.camerapreview.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceActivity
import com.devyk.av.camerapreview.R

/**
 * <pre>
 *     author  : devyk on 2020-07-06 10:09
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is GLSampleActivity
 * </pre>
 */
public class GLSampleActivity : PreferenceActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.gl_sample)
    }
}