package com.devyk.mediacodec_video_encode.renderer

import android.content.Context
import android.text.TextPaint
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.app.Activity
import android.graphics.*
import androidx.core.view.ViewCompat.getDisplay
import java.util.*


/**
 * <pre>
 *     author  : devyk on 2020-06-15 22:51
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is DevYKSurfaceRenderer
 * </pre>
 */
public class DevYKSurfaceRenderer : SurfaceRenderer {
    private var mPaint: TextPaint? = null
    private var mWidth = 0;
    private var mHeight = 0;

    private var mX = 0.0;

    private var TAG = "在线教育直播"

    constructor(context: Context, surface: Surface) : super(surface) {
        // setting some text paint
        if (mPaint == null) {
            mPaint = TextPaint()
            mPaint?.setAntiAlias(true)
            mPaint?.setColor(Color.GREEN)
            mPaint?.setTextSize(60f * context.getResources().getConfiguration().fontScale)
            mPaint?.setTextAlign(Paint.Align.CENTER)
        }
        mWidth = context.getScreenWidth()
        mHeight = context.getScreenHeight()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        if (mX >= 1) mX = 0.0
        mX +=0.004
        var matrix = Matrix()
        matrix.postScale(mX.toFloat(),mX.toFloat())
        canvas.setMatrix(matrix)
        mPaint?.let { canvas.drawText(TAG, (canvas.width.toFloat()/2), canvas.height.toFloat()/2, it) }
    }

    private fun Context.getDisplay(): Display? {
        val wm: WindowManager?
        if (this is Activity) {
            wm = this.windowManager
        } else {
            wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }


        return wm?.defaultDisplay
    }

    private fun Context.getScreenWidth(): Int {
        val display = this.getDisplay() ?: return 0
        val point = Point()
        display.getSize(point)
        return point.x
    }

    private fun Context.getScreenHeight(): Int {
        val display = this.getDisplay() ?: return 0
        val point = Point()
        display.getSize(point)
        return point.y
    }
}