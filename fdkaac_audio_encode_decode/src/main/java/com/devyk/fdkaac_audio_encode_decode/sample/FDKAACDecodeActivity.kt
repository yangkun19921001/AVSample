package com.devyk.fdkaac_audio_encode_decode.sample

import android.util.Log
import android.view.View
import com.devyk.fdkaac_audio_encode_decode.FDKAACDecode
import com.devyk.fdkaac_audio_encode_decode.R
import com.devyk.ikavedit.base.BaseActivity
import kotlinx.android.synthetic.main.activity_fdkaac_decode.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * <pre>
 *     author  : devyk on 2020-06-07 16:35
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FDKAACDecodeActivity
 * </pre>
 */

class FDKAACDecodeActivity : BaseActivity() {
    lateinit var fdkaacDecode: FDKAACDecode;
    lateinit var readBuffer: FileInputStream;
    lateinit var writeBuffer: FileOutputStream;
    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        fdkaacDecode = FDKAACDecode()
        fdkaacDecode.initWithADTformat()
    }

    override fun getLayoutId(): Int = R.layout.activity_fdkaac_decode


    fun startDecode(view: View) {
        startTime(timer)
        readBuffer = FileInputStream(File("/sdcard/avsample/fdkaac_encode.aac"));
        writeBuffer = FileOutputStream(File("/sdcard/avsample/fdkaac_decode.pcm"));
        decode()
    }

    fun decode() {
        var aacByteArray = ByteArray(1024 )
        Thread {
            do {
                var len = -1;
                len = readBuffer.read(aacByteArray, 0, aacByteArray.size)
                val decodeByteArray = fdkaacDecode.decode(aacByteArray, len)
                decodeByteArray?.run {
                    writeBuffer.write(this);
                }
            } while (len > 0);
            Log.e(TAG, "解码完毕!")
            runOnUiThread {
                cleanTime(timer)
            }
        }.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        fdkaacDecode?.run {
            fdkaacDecode.destory()
        }

        readBuffer?.run {
            readBuffer.close()
        }

        writeBuffer?.run {
            writeBuffer.close()
        }

        cleanTime(timer)
    }

}