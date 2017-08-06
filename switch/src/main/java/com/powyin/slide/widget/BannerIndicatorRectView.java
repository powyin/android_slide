package com.powyin.slide.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by powyin on 2016/8/8.
 */
public class BannerIndicatorRectView extends View {


    public BannerIndicatorRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerIndicatorRectView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ensureConfig();

    }

    // 内外圆画笔； 用于定位当前页面的index；

    private int[] split = new int[1000];
    private Paint mRectUnSelect;
    private Paint mRectSelect;

    private int mViewCount = 0;
    private float mDiver;
    private float mDiverSpace;
    private float mDiverContent;

    private int mTop;
    private int mBot;

    private int mIndex;
    private float offset;

    private void ensureConfig() {
        int w = getWidth();
        int h = getHeight();

        if (mViewCount >= 7) {
            mDiver = w * 0.7f / mViewCount;
        } else {
            mDiver = 0.07f * w;
        }


        mDiverSpace = mDiver * 0.1f;
        mDiverContent = mDiver - 2 * mDiverSpace;
        float left = ((w - mDiver * mViewCount) / 2);

        for (int i = 0; i < mViewCount; i++) {
            split[4 * i] = (int) (left + mDiver * i + mDiverSpace);
            split[4 * i + 3] = (int) (left + mDiver * (i + 1) - mDiverSpace);
        }

        mTop = (int) (0.898f * h);
        mBot = (int) (0.91f * h);

    }

    private void ensureProgerss() {
        for (int i = 0; i < mViewCount; i++) {
            split[4 * i + 1] = split[4 * i];
            split[4 * i + 2] = split[4 * i + 3];
        }

        float endPoint = mIndex + offset;
;

        if (endPoint < 0) {
            split[1] = (int) ((1 + endPoint) * mDiverContent) + split[0];
            split[mViewCount * 4 - 4 + 2] = (int) (split[mViewCount * 4 - 4 + 3] + endPoint * mDiverContent);
        } else if (endPoint > (mViewCount - 1 + 0.5f)) {
            split[1] = (int) (offset * mDiverContent) + split[0];
            split[mViewCount * 4 - 4 + 2] = (int) (split[mViewCount * 4 - 4 + 3] - offset * mDiverContent);
        } else {
            int left = offset < 0 ? mIndex - 1 : mIndex;
            int right = offset < 0 ? mIndex : mIndex + 1;
            split[4 * left + 2] = (int) (split[4 * left] + (endPoint - left) * mDiverContent);
            split[4 * right + 1] = (int) (split[4 * right + 3] - (right - endPoint) * mDiverContent);
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        for (int i = 0; i < mViewCount; i++) {
            int index0 = split[4 * i];
            int index1 = split[4 * i + 1];
            int index2 = split[4 * i + 2];
            int index3 = split[4 * i + 3];

            if (index0 != index1) {
                canvas.drawRect(index0, mTop, index1, mBot, mRectSelect);
            }

            if (index1 != index2) {
                canvas.drawRect(index1, mTop, index2, mBot, mRectUnSelect);
            }

            if (index2 != index3) {
                canvas.drawRect(index2, mTop, index3, mBot, mRectSelect);
            }
        }


    }

    //---------------------------------setting-------------------------------------//


    public void onButtonLineScroll(int viewCount, int centerIndex, float off) {

        if (this.mViewCount != viewCount) {
            this.mViewCount = viewCount;
            this.mViewCount = viewCount;
            ensureConfig();
        }

        mIndex = centerIndex;
        offset = off;

        ensureProgerss();
        invalidate();
    }


}
