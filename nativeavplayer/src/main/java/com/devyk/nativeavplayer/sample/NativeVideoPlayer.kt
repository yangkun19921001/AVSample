package com.devyk.nativeavplayer.sample

import android.view.SurfaceHolder
import android.view.View
import com.devyk.ikavedit.audio.AudioCapture
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.nativeavplayer.NativeAPI
import com.devyk.nativeavplayer.R
import kotlinx.android.synthetic.main.activity_native_video_play.*
import kotlinx.android.synthetic.main.activity_native_video_play.view.*
import java.io.FileInputStream

/**
 * <pre>
 *     author  : devyk on 2020-06-30 21:48
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is NativeVideoPlayer
 * </pre>
 *
 * 参考代码：
 * https://github.com/yangkun19921001/AVEditer/tree/master/avedit/src/main/cpp/avplay/video
 *
 */

public class NativeVideoPlayer : BaseActivity<Int>() {

    private var mYUV420SPPATH = "sdcard/waterfall_cif_352*288_i420.yuv"
    private  var mFileInputStream:FileInputStream?=null
    private lateinit var mNativeAPI: NativeAPI


    private var mWidth = 352
    private var mHeight = 288

    private var isPlayer = false;

    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        mNativeAPI = NativeAPI()


        surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                mNativeAPI.initNativeWindow(holder!!.surface)

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }
        })
    }

    override fun getLayoutId(): Int = R.layout.activity_native_video_play


    public fun btn_play(view: View) {
        isPlayer = true;
        startTime(timer)
        mFileInputStream = FileInputStream(mYUV420SPPATH)
        Thread {
            do {
                var byte = ByteArray(mWidth * mHeight * 3 / 2);
                val len = mFileInputStream!!.read(byte)
                if (len > 0) {
                    mNativeAPI.enqueue(byte, mWidth, mHeight)
                }
            } while (len > 0 && isPlayer)
            mNativeAPI.release()
            mFileInputStream!!.close()
            runOnUiThread {
                cleanTime(timer)
            }
        }.start()


    }


    public fun btn_stop(view: View) {
        isPlayer = false;
    }

    override fun onDestroy() {
        super.onDestroy()
        isPlayer = false;
        mNativeAPI.release()
        mFileInputStream?.close()

    }
}