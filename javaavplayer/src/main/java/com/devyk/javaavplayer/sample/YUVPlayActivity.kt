package com.devyk.javaavplayer.sample

import android.annotation.SuppressLint
import android.view.SurfaceView
import android.widget.Button
import android.widget.Chronometer
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.javaavplayer.R
import com.devyk.javaavplayer.gles.GLESMaanager
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_yuv_play.*
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * <pre>
 *     author  : devyk on 2020-06-27 23:38
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is YUVPlayActivity
 * </pre>
 */
public class YUVPlayActivity : BaseActivity() {


    private lateinit var chronometer: Chronometer
    private var glesMaanager: GLESMaanager? = null


    private var mPreviewWidth = 352
    private var mPreviewHeight = 288
    private var isPlayer = false
    private lateinit var mFileInputStream: FileInputStream
    private var buffer = ByteArray(mPreviewHeight * mPreviewWidth * 3 / 2)

    private var YUV_PATH = "sdcard/avsample/352_288_i420.yuv"


    override fun initListener() {

    }

    override fun initData() {
        glesMaanager?.onDestory()
        glesMaanager = GLESMaanager.PlayManagerBuilder().
            /**
             * 预览的宽,高
             */
            withPreviewWidth(mPreviewWidth, mPreviewHeight)
            /**
             * 必须传入一个 ViewGroup 框架会自动绑定播放控件
             */
            .bindPlayControl(surface)
            /**
             * 是否开始播放，现在没有传入 byte 数据，最好设置为 false
             */
            .withRequestRender(false)
            /**
             * 通过 builder 构建播放管理类
             */
            .build(applicationContext)

        /**
         * init 播放器
         */
        glesMaanager?.initPlayControl()
    }

    override fun init() {
        var btn_start = findViewById<Button>(R.id.btn_start);
        btn_start.text = resources.getString(R.string.start_player)
        var btn_stop = findViewById<Button>(R.id.btn_stop);
        btn_stop.text = resources.getString(R.string.stop_player)
        chronometer = findViewById<Chronometer>(R.id.timer);

        btn_start.setOnClickListener {
            initData()
            isPlayer = true
            mFileInputStream = FileInputStream(YUV_PATH)
            startTime(chronometer)
            readYUV()
        }
        btn_stop.setOnClickListener {
            isPlayer = false
            cleanTime(chronometer)
            glesMaanager?.onDestory()
            mFileInputStream.close()
        }


    }

    @SuppressLint("CheckResult")
    private fun readYUV() {
        Observable.create<ByteArray> {
            while (isPlayer) {
                Arrays.fill(buffer, 0)
                val len = mFileInputStream.read(buffer)
                if (len <= 0) {
                    isPlayer = false
                    it.onComplete()
                    break
                }
                it.onNext(buffer)
                Thread.sleep(1000 / 25)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ByteArray> {
                override fun onComplete() {
                    isPlayer = false
                    cleanTime(chronometer)
                    glesMaanager?.onDestory()
                    mFileInputStream.close()

                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: ByteArray) {
                    glesMaanager?.enqueue(t)
                }

                override fun onError(e: Throwable) {
                }
            })


    }

    override fun getLayoutId(): Int = R.layout.activity_yuv_play


    override fun onDestroy() {
        super.onDestroy()
        isPlayer = false
        glesMaanager?.onDestory()
        mFileInputStream.close()
    }
}