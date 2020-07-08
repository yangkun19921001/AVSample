package com.devyk.x264_video_encode.sample

import android.view.View
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.x264_video_encode.NativeX264Encode
import com.devyk.x264_video_encode.R
import com.devyk.x264_video_encode.YUVType
import kotlinx.android.synthetic.main.activity_x264_encode.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer


/**
 *
 * YUV 文件在该处可以下载：http://trace.eas.asu.edu/yuv/index.html
 *
 * ffplay -stats -f h264 yuvtest.h264
 *
 * <pre>
 *     author  : devyk on 2020-06-10 22:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is x264EncodeActiviy
 * </pre>
 */
public class x264EncodeActivity : BaseActivity<Int>() {

    //    private var mYUV420SPPATH = "sdcard/waterfall_cif_352*288_i420.yuv"
    private var mYUV420SPPATH = "sdcard/record_1280_720_nv21.yuv"
    private var mH264PATH = "sdcard/record_1280_720.h264"

    private lateinit var mNativeX264Encode: NativeX264Encode

    private lateinit var mFileInputStream: FileInputStream

    /**
     * 视频宽
     */
    private val mWidth = 1280
    /**
     * 视频高
     */
    private val mHeight = 720
    /**
     * YUV类型
     * YUV420sp == nv21
     * YUV420p == i420
     */
    private val mYUVType = YUVType.YUV420sp.type

    private val mVideRate = 2000;

    private val mFrameRate = 25;

    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        mNativeX264Encode = NativeX264Encode()
        mNativeX264Encode.init(mH264PATH, mWidth, mHeight, mVideRate, mFrameRate)
        mFileInputStream = FileInputStream(mYUV420SPPATH)
    }

    override fun getLayoutId(): Int = R.layout.activity_x264_encode


    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release() {
        mNativeX264Encode?.destory()
        mFileInputStream?.close()
    }

    fun startEncode(view: View) {
        startTime(timer)
        Thread {
            do {
                var byte = ByteArray(mWidth * mHeight * 3 / 2);
                val len = mFileInputStream.read(byte)
                if (len > 0) {
                    mNativeX264Encode.encode(byte, mYUVType)
                }
            } while (len > 0)
            release()
            runOnUiThread {
                cleanTime(timer)
            }
        }.start()
    }


}