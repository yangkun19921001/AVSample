package com.devyk.mediacodec_audio_encode.sample

import android.media.MediaCodec
import android.util.Log
import android.view.View
import com.devyk.common.config.AudioConfiguration
import com.devyk.common.mediacodec.AACDecoder
import com.devyk.common.callback.OnAudioDecodeListener
import com.devyk.ikavedit.base.BaseActivity
import com.devyk.mediacodec_audio_encode.R
import com.devyk.mediacodec_audio_encode.controller.AudioControllerImpl
import com.devyk.mediacodec_audio_encode.controller.IMediaCodecListener
import com.devyk.mediacodec_audio_encode.controller.AudioStreamController
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
public class AudioMediaCodecActivity : BaseActivity<Int>(), IMediaCodecListener,
    OnAudioDecodeListener {


    private var mStreamController: AudioStreamController? = null

    private var mFileOutputStream: FileOutputStream? = null
    private var mFileOutputStream_PCM: FileOutputStream? = null

    var mDecode: AACDecoder? = null


    private val AAC_DIR = "sdcard/AVSample/"

    private val AAC_PATH = "${AAC_DIR}mediacodec_44100_1_16_bit.aac"

    private val PCM_PATH = "${AAC_DIR}mediacodec_44100_1_16_bit.pcm"

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
        var audioConfiguration = AudioConfiguration.Builder()
            .setCodecType(AudioConfiguration.CodeType.DECODE)
            .setAdts(1)//有 ADTS 数据
            .build();
        mDecode = AACDecoder(audioConfiguration)
        mDecode?.prepareCoder()
        mDecode?.setOnAudioEncodeListener(this)

    }

    override fun init() {
        mStreamController = AudioStreamController(AudioControllerImpl());
        mStreamController?.setListener(this)
        if (!File(AAC_DIR).exists()) {
            File(AAC_DIR).mkdir()
        }

        if (File(AAC_PATH).exists()) {
            File(AAC_PATH).delete()
        }
        File(AAC_PATH).createNewFile()


        if (File(PCM_PATH).exists()) {
            File(PCM_PATH).delete()
        }
        File(PCM_PATH).createNewFile()
        mFileOutputStream = FileOutputStream(AAC_PATH, true)
        mFileOutputStream_PCM = FileOutputStream(PCM_PATH, true)


    }

    override fun getLayoutId(): Int = R.layout.activity_audio_mediacodec

    fun startEncode(view: View) {
        init()
        mStreamController?.start()

    }

    fun stopEncode(view: View) {
        //内部是控制编码的
        mStreamController?.stop()
        mDecode?.stop()
        mFileOutputStream?.close()
        mFileOutputStream_PCM?.close()
    }

    /**
     * AAC - PCM 解码完成的回调
     */
    override fun onAudioPCMData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        var mAudio = ByteArray(bi.size)
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)
        bb.get(mAudio)
        mFileOutputStream_PCM?.write(mAudio)
    }


    /**
     * PCM - AAC 编码完成的回调
     */
    override fun onAudioAACData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        super.onAudioAACData(bb, bi)
        mAudio = ByteArray(bi.size + 7)
        //添加 adts 头
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)
        bb.get(mAudio, 7, bi.size)
        ADTSUtils.addADTStoPacket(mAudio!!, mAudio!!.size, 2, 44100, 1)
        //打印 ADTS
        Log.e(
            TAG,
            "mAudio[0]=${mAudio!![0]} mAudio[1]=${mAudio!![1]} mAudio[2]=${mAudio!![2]} mAudio[3]=${mAudio!![3]} mAudio[4]=${mAudio!![4]} mAudio[5]=${mAudio!![5]} mAudio[6]=${mAudio!![6]}"
        )
        mDecode?.enqueueCodec(mAudio!!)
        mFileOutputStream?.write(mAudio, 0, mAudio!!.size)
    }


    override fun onDestroy() {
        super.onDestroy()
        mStreamController?.stop()
        mFileOutputStream?.close()
        mFileOutputStream_PCM?.close()
        mDecode?.stop()
    }

}