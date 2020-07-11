package com.devyk.av.camera_recorder.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * <pre>
 *     author  : devyk on 2020-07-09 21:38
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BitmapUtils
 * </pre>
 */
public object BitmapUtils {
    /**
     * 将文字 生成 文字图片 生成显示编码的Bitmap,目前这个方法是可用的
     * 
     * @param contents
     * @param context
     * @return
     */
    fun creatBitmap(contents: String, context: Context, testSize: Int, testColor: String, bg: String): Bitmap {
        var scale = context.getResources().getDisplayMetrics().scaledDensity;
        var tv = TextView(context);
        var layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(scale * testSize);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.parseColor(testColor));
        tv.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        tv.setBackgroundColor(Color.parseColor(bg));
        tv.buildDrawingCache();
        return tv.getDrawingCache();
    }

}