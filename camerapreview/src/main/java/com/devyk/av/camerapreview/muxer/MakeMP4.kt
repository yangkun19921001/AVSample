package com.devyk.av.camerapreview.muxer

import android.media.MediaFormat
import android.media.MediaMuxer

/**
 * <pre>
 *     author  : devyk on 2020-07-08 18:04
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is MakeMP4
 * </pre>
 */
public class MakeMP4 :BaseMediaMuxer{

    constructor(path: String, outType: Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) : super(path, outType)
}