package com.devyk.x264_video_encode

interface IEncoder {
    fun init(h264Path: String, width: Int, height: Int, videoBitRate: Int, frameRate: Int);

    fun encode(byteArray: ByteArray, type: Int = YUVType.YUV420p.type);

    fun destory();

}
