package com.devyk.av.camerapreview.widget

import android.content.Context
import android.util.AttributeSet
import com.devyk.av.camerapreview.encode.MP4Encoder
import com.devyk.common.config.VideoConfiguration

/**
 * <pre>
 *     author  : devyk on 2020-07-08 16:22
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is RecordView
 * </pre>
 */
public class RecordView :CameraView{

    private  var mMP4Encoder: MP4Encoder?=null

    protected  var mContext: Context? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        mContext = context
    }

    public fun start() {
        mMP4Encoder?.start()
    }


    override fun releaseCamera() {
        super.releaseCamera()
        mMP4Encoder?.stop()
    }

    fun setDataSource(path: String) {
        mMP4Encoder = MP4Encoder(path, getTextureId(), getEGLContext(), context?.applicationContext)
        mMP4Encoder?.prepare(VideoConfiguration.createDefault())
    }

}