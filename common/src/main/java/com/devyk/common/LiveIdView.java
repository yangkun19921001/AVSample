package com.devyk.common;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * <pre>
 *     author  : devyk on 2020-06-30 11:12
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is LiveIdView
 * </pre>
 */
@SuppressLint("AppCompatCustomView")
public class LiveIdView extends View {

    private String mName = "DevYK 1214123";

    private Paint mPaint = null;


    private float x = 0;


    private float view_width = 0;
    private float view_height = 0;

    private int w =0;

    private Bitmap mBitMap = null;


    private ArrayList<Point> mLists = null;


    public LiveIdView(Context context) {
        super(context);
        init();
    }

    public LiveIdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(40);

//       mBitMap =  BitmapFactory.decodeResource(getResources(),R.drawable.cloudy);


//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
//        valueAnimator.setDuration(25000);
//        valueAnimator.setRepeatCount(-1);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                Log.i("onAnimationUpdate ", "onAnimationUpdate: " + animation.getAnimatedValue());
//                x = (float) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
//        valueAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //测量姓名 ID 的名称
        float TEXT_LEN = mPaint.measureText(mName);

        //控件的宽
        this.w = w;

        //宽 - 减去一个字体宽
        view_width = w - TEXT_LEN;
        //控件高
        view_height = h;

        //模拟位置
        mLists = new ArrayList();
        mLists.clear();
        mLists.add(new Point(new Random().nextInt((int) view_width), new Random().nextInt((int) view_height)));
        mLists.add(new Point(new Random().nextInt((int) view_width), new Random().nextInt((int) view_height)));
        mLists.add(new Point(new Random().nextInt((int) view_width), new Random().nextInt((int) view_height)));
        mLists.add(new Point(new Random().nextInt((int) view_width), new Random().nextInt((int) view_height)));

        //重绘
        invalidate();
    }

    public LiveIdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public LiveIdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        for (Point point : mLists) {
            canvas.rotate(45,point.w, point.h);
            canvas.drawText(mName, point.w, point.h, mPaint);
            canvas.rotate(-45,point.w, point.h);
        }


        mPaint.setTextSize(60);
        mPaint.setColor(Color.RED);
        float len = mPaint.measureText("LOGO");
        canvas.drawText("LOGO",w - len - 20 ,view_height -20,mPaint);
        canvas.restore();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    class Point {
        private int w;
        private int h;

        public int getW() {
            return w;
        }

        public void setW(int w) {
            this.w = w;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public Point(int w, int h) {
            this.w = w;
            this.h = h;
        }
    }

}
