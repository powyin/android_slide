package com.powyin.slide.widget;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.powyin.slide.R;

/**
 * Created by MT3020 on 2016/2/19.
 * 滑动页卡
 */
public class SlideSwitch extends LinearLayout {

    public SlideSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitch);

        selectDrawable = a.getDrawable(R.styleable.SlideSwitch_checked_drawable);
        animationSelectIndex = a.getInt(R.styleable.SlideSwitch_checked_select,0);
        alwaysSelect = a.getBoolean(R.styleable.SlideSwitch_checked_always_select,false);

        a.recycle();
        setClickable(true);
    }

    // 是否出现点击就滑动
    boolean alwaysSelect = false;

    // 滑块 图片 通过styleable赋值实现
    Drawable selectDrawable;
    // 监听器，暴露给外部的方法
    OnItemSelectListener onItemSelectListener;
    // 动画是否运行中
    boolean isAnimating;

    // 选择位置
    private int animationSelectIndex = 0;                       //当前选中项
    private int animationTragerIndex = 0;                       //移动目标
    private float animationSelectRadio =1;                      //移动比例


    private Rect selectItemRect = new Rect();

    // 点击落点位置  用于判断当前点击Index；
    float downX;
    float downY;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //OnDraw是画自己，dispatchDraw画自己的孩子，这里的就是画两个滑块
    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (selectDrawable != null && animationSelectIndex >= 0 && animationSelectIndex < getChildCount()) {

            View originView = getChildAt(animationSelectIndex);
            View targetView = getChildAt(animationTragerIndex);

            int c_left = targetView.getLeft()-originView.getLeft();
            int c_right = targetView.getRight()-originView.getRight();
            int c_top = targetView.getTop()-originView.getTop();
            int c_bottom = targetView.getBottom()-originView.getBottom();

            //根据选择的位置实例化一个View，然后画
            selectItemRect.top = (int)(originView.getTop()+c_top*animationSelectRadio);
            selectItemRect.bottom = (int)(originView.getBottom()+c_bottom*animationSelectRadio);
            selectItemRect.left = (int) ((originView.getLeft() + c_left * animationSelectRadio));
            selectItemRect.right = (int) ((originView.getRight())+c_right*animationSelectRadio);

            //View组件的绘制会调用draw(Canvas canvas)方法，draw过程中主要是先画Drawable背景，
            // 对 drawable调用setBounds()然后是draw(Canvas c)方法
            selectDrawable.setBounds(selectItemRect);//画这个view的背景
            selectDrawable.draw(canvas);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            downX = event.getX();
            downY = event.getY();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        boolean inter = super.performClick();
        if (inter) return true;

        for (int i = 0; i < getChildCount(); i++) {
            Rect globeRect = new Rect();
            View view = getChildAt(i);
            globeRect.top = view.getTop();
            globeRect.left = view.getLeft();
            globeRect.right = view.getRight();
            globeRect.bottom = view.getBottom();
            if (globeRect.contains((int) downX, (int) downY)) {
                if (animationSelectIndex != i) {
                    preformItemSelectAnimationClick(i, 200);
                }else if(alwaysSelect){
                    //如果 点击就变换   （实现）
                    if(i+1<getChildCount()){
                        preformItemSelectAnimationClick(i+1,200);
                    }else if(i-1>=0){
                        preformItemSelectAnimationClick(i-1,200);
                    }
                }
                break;
            }
        }
        return true;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.onItemSelectListener = listener;
        preformItemSelectAnimationClick(animationSelectIndex,0);
    }

    public void setSelectIndex(int index) {
        if (index != animationSelectIndex && !isAnimating) {
            preformItemSelectAnimationClick(index, 0);
            invalidate();
        }
    }


    private void preformItemSelectAnimationClick( int targetIndex, int animationTime) {
        if (animationTime > 0) {
            if (isAnimating) return;
            isAnimating = true;
            animationTragerIndex = targetIndex;
            ValueAnimator valueAnimator =
                    ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(animationTime);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animationSelectRadio = (float) animation.getAnimatedValue();
                    invalidate();
                    invokeItemClickListener();
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationSelectIndex = animationTragerIndex;
                    isAnimating = false;
                    invokeItemClickListener();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            valueAnimator.start();
        } else {
            animationSelectIndex = targetIndex;
            animationTragerIndex = targetIndex;
            invokeItemClickListener();
        }
    }



    private void invokeItemClickListener() {
        if (this.onItemSelectListener != null) {
            this.onItemSelectListener.onItemSelectChange(animationSelectIndex,animationTragerIndex,animationSelectRadio,getChildAt(animationTragerIndex));
        }
    }


    public interface OnItemSelectListener {
        /*
            动画结束后执行
            selectView 选中视图
            index 选中视图位置
            unSelectViews 非选中视图集合
        */
        void onItemSelectChange(int startIndex, int targetIndex, float radio, View targetSelectView);
    }
}










