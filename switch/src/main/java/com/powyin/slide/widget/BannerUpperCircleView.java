package com.powyin.slide.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by powyin on 2017/8/3.
 */

public class BannerUpperCircleView extends View {


    public BannerUpperCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerUpperCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRectUnSelect = new Paint();
        mRectUnSelect.setStyle(Paint.Style.FILL);
        mRectUnSelect.setColor(0x66000000);

        mRectSelect = new Paint();
        mRectSelect.setStyle(Paint.Style.FILL);
        mRectSelect.setColor(0xffffffff);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ensureConfig();

    }

    // 内外圆画笔； 用于定位当前页面的index；

    private Paint mRectUnSelect;
    private Paint mRectSelect;

    private int mViewCount = 0;
    private float mDiver;
    private float mLeft;
    private float mCircle;
    private int mIndex;
    private int mY;

    private void ensureConfig() {
        int w = getWidth();
        int h = getHeight();

        if (mViewCount >= 7) {
            mDiver = w * 0.7f / mViewCount;
        } else {
            mDiver = 0.05f * w;
        }

        mLeft = ((w - mDiver * mViewCount) / 2);
        mCircle = 0.015f * h;
        mY = (int) (0.93 * h);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mViewCount; i++) {
            if (i == mIndex) {
                canvas.drawCircle(mLeft + i * mDiver + mDiver / 2, mY, mCircle, mRectSelect);
            } else {
                canvas.drawCircle(mLeft + i * mDiver + mDiver / 2, mY, mCircle, mRectUnSelect);
            }
        }
    }

    //---------------------------------setting-------------------------------------//


    public void onButtonLineScroll(int viewCount, int centerIndex, float off) {

        if (this.mViewCount != viewCount) {
            this.mViewCount = viewCount;
            ensureConfig();
        }

        if (mIndex != centerIndex) {
            mIndex = centerIndex;
            invalidate();
        }
    }
}
