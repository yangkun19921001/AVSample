package com.devyk.mediacodec_audio_encode.utils

import android.os.Build
import android.os.Looper

/**
 * <pre>
 *     author  : devyk on 2020-06-13 16:54
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is EncodeCastUtils
 * </pre>
 */
object EncodeCastUtils {

    val isOverLOLLIPOP: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    interface INotUIProcessor {
        fun process()
    }

    fun processNotUI(processor: INotUIProcessor) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread(Runnable { processor.process() }).start()
        } else {
            processor.process()
        }
    }
}