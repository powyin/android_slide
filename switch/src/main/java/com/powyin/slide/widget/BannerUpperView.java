package com.powyin.slide.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/8/8.
 */
public class BannerUpperView extends View {


    // 内外圆画笔； 用于定位当前页面的index；
    private Paint mRectUnSelect;
    private Paint mRectSelect;
    private RectF mRectF;
    private List<RectF> rectFListNo = new ArrayList<>();
    private List<RectF> rectFListYes = new ArrayList<>();


    private float mScroll = 2;
    private int mViewCount = 5;

    public BannerUpperView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BannerUpperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRectUnSelect = new Paint();
        mRectUnSelect.setStyle(Paint.Style.FILL);
        mRectUnSelect.setColor(0x66000000);


        mRectSelect = new Paint();
        mRectSelect.setStyle(Paint.Style.FILL);
        mRectSelect.setColor(0xffffffff);

        mRectF = new RectF();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float mDiver = 0.15f * w;
        mRectF.top = 0.93f * h;
        mRectF.bottom = mRectF.top + Math.max(3,h*0.01f);
        mRectF.left = mDiver;
        mRectF.right = w - mDiver;
        ensureUnSelectRect();
        ensureSelectRect();
    }

    private void ensureUnSelectRect(){
        rectFListNo.clear();
        if(mViewCount == 0) {
            return;
        }
        float divide = (mRectF.right - mRectF.left) / mViewCount ;
        for(int i=0;i<mViewCount;i++){
            RectF tem = new RectF();
            tem.left = mRectF.left +divide/12 + i*divide;
            tem.right = mRectF.left - divide/12 + i*divide + divide;
            tem.top = mRectF.top;
            tem.bottom = mRectF.bottom;
            rectFListNo.add(tem);
        }
    }

    private void ensureSelectRect(){
        rectFListYes.clear();
        if(rectFListNo.size() ==0 ){
            return;
        }
        int locLeft = (int) mScroll;
        int locRight = locLeft + 1 >= mViewCount ? 0 : locLeft+1;
        float diff = mScroll - locLeft;

        RectF temLeft =  rectFListNo.get(locLeft);
        RectF temRight = rectFListNo.get(locRight);

        RectF left  =  new RectF(temLeft);
        left.left = temLeft.left+ (temLeft.right-temLeft.left)* (diff);


        rectFListYes.add(left);

        RectF right  =  new RectF(temRight);
        right.right = temRight.left + (temRight.right-temRight.left)* (diff);

        rectFListYes.add(right);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewCount ==0) {
            return;
        }


        for(RectF tem : rectFListNo){
            canvas.drawRect(tem,mRectUnSelect);
            System.out.println("xxx::"+tem);
        }

        for(RectF tem : rectFListYes){
            canvas.drawRect(tem,mRectSelect);
            System.out.println("yyy::"+tem);
        }




    }

    //---------------------------------setting-------------------------------------//

    public void setScroll(float scroll, int viewCount){

        if(this.mViewCount != viewCount){
            this.mViewCount = viewCount;
            ensureUnSelectRect();
        }

        this.mScroll = scroll;
        ensureSelectRect();

        invalidate();
    }

}