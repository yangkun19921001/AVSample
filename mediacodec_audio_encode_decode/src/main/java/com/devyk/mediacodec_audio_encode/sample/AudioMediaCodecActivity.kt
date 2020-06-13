package com.devyk.mediacodec_audio_encode.sample

import android.media.MediaCodec
import android.util.Log
import android.view.View
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.mediacodec_audio_encode.R
import com.devyk.mediacodec_audio_encode.controller.AudioControllerImpl
import com.devyk.mediacodec_audio_encode.controller.IMediaCodecListener
import com.devyk.mediacodec_audio_encode.controller.StreamController
import com.devyk.mediacodec_audio_encode.utils.ADTSUtils
import kotlinx.android.synthetic.main.activity_audio_mediacodec.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * <pre>
 *     author  : devyk on 2020-06-13 17:09
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AudioMediaCodecActivity
 * </pre>
 */
public class AudioMediaCodecActivity : BaseActivity(), IMediaCodecListener {


    private var mStreamController: StreamController? = null

    private var mFileOutputStream: FileOutputStream? = null

    private val AAC_DIR = "sdcard/AVSample/"

    private val AAC_PATH = "${AAC_DIR}mediacodec_44100_1_16.bit.aac"

    private var mAudio: ByteArray? = null

    override fun start() {
        runOnUiThread {
            startTime(timer)

        }

    }

    override fun stop() {
        runOnUiThread {
            cleanTime(timer)
        }
    }

    override fun initListener() {

    }

    override fun initData() {
    }

    override fun init() {
        mStreamController = StreamController(AudioControllerImpl());
        mStreamController?.setListener(this)
        if (!File(AAC_DIR).exists()) {
            File(AAC_DIR).mkdir()
        }

        if (File(AAC_PATH).exists()) {
            File(AAC_PATH).delete()
        }
        File(AAC_PATH).createNewFile()
        mFileOutputStream = FileOutputStream(AAC_PATH, true)


    }

    override fun getLayoutId(): Int = R.layout.activity_audio_mediacodec

    fun startEncode(view: View) {
        init()
        mStreamController?.start()

    }

    fun stopEncode(view: View) {
        mStreamController?.stop()
        mFileOutputStream?.close()
    }


    /**
     * 编码完成的数据
     */
    override fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        super.onAudioData(bb, bi)
        mAudio = ByteArray(bi.size + 7 )
        //添加 adts 头
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)
        bb.get(mAudio, 7, bi.size)
        ADTSUtils.addADTStoPacket(mAudio!!, mAudio!!.size, 2, 44100,1)
        //打印 ADTS
        Log.e(TAG,"mAudio[0]=${mAudio!![0]} mAudio[1]=${mAudio!![1]} mAudio[2]=${mAudio!![2]} mAudio[3]=${mAudio!![3]} mAudio[4]=${mAudio!![4]} mAudio[5]=${mAudio!![5]} mAudio[6]=${mAudio!![6]}")
        mFileOutputStream?.write(mAudio, 0, mAudio!!.size)
    }


    override fun onDestroy() {
        super.onDestroy()
        mStreamController?.stop()
        mFileOutputStream?.close()
    }

}