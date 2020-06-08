package com.devyk.avsample

import android.app.Application
import android.util.Log
import com.blankj.utilcode.util.CrashUtils

/**
 * <pre>
 *     author  : devyk on 2020-06-03 21:37
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is App
 * </pre>
 */
public class App : Application() {


    override fun onCreate() {
        super.onCreate()

        CrashUtils.init { crashInfo, e ->

            Log.e("OnCrashListener",crashInfo)
        }
    }
}