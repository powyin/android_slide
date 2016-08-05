package com.powyin.slide.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.powyin.slide.R;

/**
 * Created by powyin on 2016/8/5.
 */
public class BannerSwitch extends ViewGroup {


    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;


    private OnPageChangeListener mOnPageChangeListener;

    private ValueAnimator mSwitchAnimator;
    private AnimationRun autoProgress;
    private int mSwitchPagePeriod ;
    private int mSwitchAnimationPeriod ;
    private boolean mIsTouched = false;
    private long mTouchedEndTime;

    // 横幅滚动轴；
    float mSelectIndex;

    private class AnimationRun implements Runnable{
        boolean isCancel;
        @Override
        public void run() {
            if(isCancel) return;
            long pre = (System.currentTimeMillis()-mTouchedEndTime);
            if(getVisibility() == VISIBLE && !mIsTouched && pre>=mSwitchPagePeriod/3) {
                startFly(true);
            }

            if(pre>=mSwitchPagePeriod/3){
                postDelayed(this,mSwitchPagePeriod);
            }else {
                postDelayed(this,mSwitchPagePeriod/10);
            }


        }
    }

    public BannerSwitch(Context context) {
        this(context, null, 0);
    }

    public BannerSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerSwitch);
        mSwitchPagePeriod = a.getInt(R.styleable.BannerSwitch_pow_banner_switch_period,2450);

        mSwitchPagePeriod = Math.max(1500,mSwitchPagePeriod);
        mSwitchPagePeriod = Math.min(10000,mSwitchPagePeriod);

        mSwitchAnimationPeriod = a.getInt(R.styleable.BannerSwitch_pow_banner_switch_animation_period,450);
        mSwitchAnimationPeriod = Math.max(100,mSwitchAnimationPeriod);
        mSwitchAnimationPeriod = Math.max(1000,mSwitchAnimationPeriod);

        a.recycle();
        setScrollingCacheEnabled();
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int speWidthMeasure = count > 0 ? MeasureSpec.makeMeasureSpec(
                Math.max(MeasureSpec.getSize(widthMeasureSpec) -getPaddingLeft() - getPaddingRight(), 0), MeasureSpec.EXACTLY) : 0;
        int usedHei = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            int speHeiMeasure = getChildMeasureSpec(heightMeasureSpec,
                    usedHei, LayoutParams.WRAP_CONTENT);

            child.measure(speWidthMeasure, speHeiMeasure);
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());

        }

        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        // 设置测量大小
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                resolveSizeAndState(maxHeight , heightMeasureSpec, 0));

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, getMeasuredHeight()
                            - getPaddingTop() - getPaddingBottom()), MeasureSpec.EXACTLY);
            child.measure(speWidthMeasure, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = getPaddingTop();
        int childLeft = getPaddingLeft();
        int childRight = getPaddingRight();
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            child.layout(childLeft, childTop, r-l-childLeft - childRight, childHeight + childTop);
        }
        ensureTranslationOrder();
    }

    //OnDraw是画自己，dispatchDraw画自己的孩子，这里的就是画两个滑块
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mIsTouched = true;
        if(ev.getAction()==MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL){
            mIsTouched = false;
            mTouchedEndTime = System.currentTimeMillis();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_POINTER;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                return true;
            }
            if (mIsUnableToDrag) {
                return false;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsUnableToDrag = false;

                if (mSwitchAnimator !=null && mSwitchAnimator.isStarted() && mSwitchAnimator.isRunning()) {
                    mSwitchAnimator.cancel();
                    mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                } else {
                    mIsBeingDragged = false;
                }

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == -1) {
                    return false;
                }

                int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                float x = MotionEventCompat.getX(ev, pointerIndex);
                float dx = x - mLastMotionX;
                float xDiff = Math.abs(dx);
                float y = MotionEventCompat.getY(ev, pointerIndex);
                float yDiff = Math.abs(y - mInitialMotionY);

                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    setScrollingCacheEnabled();
                } else if (yDiff > mTouchSlop) {
                    mIsUnableToDrag = true;
                }

                if (mIsBeingDragged) {
                    offsetScrollX(x);
                }

                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }


        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                mLastMotionX = mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {                                                                              // just in case
                    if (mActivePointerId == -1) return false;
                    int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    if (pointerIndex < 0) return false;
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    if (xDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                                mInitialMotionX - mTouchSlop;
                        setScrollingCacheEnabled();

                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                } else {
                    if (mActivePointerId == -1) return false;
                    int activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId);
                    if (activePointerIndex < 0) return false;
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    offsetScrollX(MotionEventCompat.getX(ev, activePointerIndex));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsBeingDragged && Math.abs(ev.getY() - mInitialMotionY) <= mTouchSlop / 1.5f) {
                    performItemClick();
                }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    startFly(false);
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);

                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = MotionEventCompat.getX(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(autoProgress!=null){
            autoProgress.isCancel = true;
        }
        autoProgress = new AnimationRun();
        postDelayed(autoProgress,mSwitchPagePeriod);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(autoProgress!=null){
            autoProgress.isCancel = true;
        }
    }


    private boolean performItemClick() {
        for (int i = 0; i < getChildCount(); i++) {
            Rect globeRect = new Rect();
            View view = getChildAt(i);
            int currentScrollX = getScrollX();
            globeRect.top = view.getTop();
            globeRect.left = view.getLeft() - currentScrollX;
            globeRect.right = view.getRight() - currentScrollX;
            globeRect.bottom = view.getBottom();
        }
        return true;
    }



    // 取消父类打断触摸事件传递
    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    // 多指介入
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    // 界面位移
    private void offsetScrollX(float x) {
        float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;
        mLastMotionX += scrollX - (int) scrollX;
        mSelectIndex -= deltaX/getWidth();
        ensureTranslationOrder();
    }

    // View开启Draw缓存
    private void setScrollingCacheEnabled() {
        final int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
                child.setDrawingCacheEnabled(true);
        }
    }

    // 状态恢复
    private void startFly( boolean force) {
        double diff = mSelectIndex - Math.rint(mSelectIndex);
        if(diff ==0 && !force) return;

        if(mSwitchAnimator !=null) mSwitchAnimator.cancel();

        final float mTarget;
        final float mStart = mSelectIndex;
        if(force){
            mTarget = (int) (Math.rint(mSelectIndex) -1);
        }else if(Math.abs(diff)<0.1){
            mTarget = (int) Math.rint(mSelectIndex);
        }else if(diff>0){
            mTarget = (int) (Math.rint(mSelectIndex) +1);
        }else {
            mTarget = (int) (Math.rint(mSelectIndex) -1);
        }


        mSwitchAnimator = ValueAnimator.ofFloat(mStart, mTarget);
        mSwitchAnimator.setDuration(Math.max(100,(int) Math.abs((mStart - mTarget) * mSwitchAnimationPeriod)));
        mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSelectIndex = (float) animation.getAnimatedValue();
                ensureTranslationOrder();
            }
        });
        mSwitchAnimator.start();

    }

    // 实现viewItem滚动总入口
    private void ensureTranslationOrder() {
        int count = getChildCount();
        if (count < 2 || getWidth() == 0) return;
        for (int i = 0; i < count; i++) {
            int x = (int) ((i + mSelectIndex) * getWidth());
            int transX = x % (count * getWidth());
            if (transX >= (count - 1) * getWidth()) {
                // 防止右边空白
                getChildAt(i).setTranslationX(transX - count * getWidth());
            } else if (transX <= (1 - count) * getWidth()) {
                // 防止左边空白
                getChildAt(i).setTranslationX(transX + count * getWidth());
            } else {
                getChildAt(i).setTranslationX(transX);
            }
        }

    }

    //------------------------------------------------------------------setting-----------------------------------------------------------//


    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public interface OnPageChangeListener {
        void onPageSelected(int position);
        void onPageScroll(float mScroll);
    }

}
