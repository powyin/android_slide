package com.powyin.slide.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

import com.powyin.slide.R;

/**
 * Created by powyin on 2016/8/4.
 */
public class PowSwitch extends View {

    private boolean mIsBeingDragged;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private ValueAnimator valueAnimator;
    private Drawable mSwitchBacOn;
    private Drawable mSwitchBacOff;
    private Drawable mSwitchIconOn;
    private Drawable mSwitchIconOff;
    private int mSwitchPadding;
    private int mSwitchSuggestWei;
    private int mSwitchSuggestHei;
    Rect iconRect = new Rect();
    Rect iconFixedRect = new Rect();
    Rect bacRect = new Rect();
    int targetMax;
    int targetCurrent;


    public PowSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;
        mSwitchSuggestWei = (int)(36*density);
        mSwitchSuggestHei = (int)(16*density);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PowSwitch);

        mSwitchBacOff = a.getDrawable(R.styleable.PowSwitch_pow_switch_bac_off);
        if(mSwitchBacOff==null){
            mSwitchBacOff = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_bac_off);
        }
        mSwitchBacOn = a.getDrawable(R.styleable.PowSwitch_pow_switch_bac_on);
        if(mSwitchBacOn==null){
            mSwitchBacOn = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_bac_on);
        }
        mSwitchIconOff = a.getDrawable(R.styleable.PowSwitch_pow_switch_icon_off);
        if(mSwitchIconOff==null){
            mSwitchIconOff = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_icon_off);
        }
        mSwitchIconOn = a.getDrawable(R.styleable.PowSwitch_pow_switch_icon_on);
        if(mSwitchIconOn==null){
            mSwitchIconOn = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_icon_on);
        }

        mSwitchPadding = (int)(a.getDimension(R.styleable.PowSwitch_pow_switch_padding,(int)(2*density))+0.5f);

        a.recycle();


        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context))/2;

    }

    public PowSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowSwitch(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int maxWei = 0;
        maxWei = Math.max(maxWei,mSwitchBacOff.getIntrinsicWidth());
        maxWei = Math.max(maxWei,mSwitchBacOn.getIntrinsicWidth());
        maxWei = Math.max(maxWei,mSwitchIconOff.getIntrinsicWidth()+2*mSwitchPadding);
        maxWei = Math.max(maxWei,mSwitchIconOn.getIntrinsicWidth()+2*mSwitchPadding);
        maxWei+=getPaddingLeft()+getPaddingRight();

        int maxHei = 0;
        maxHei = Math.max(maxHei,mSwitchBacOff.getIntrinsicHeight());
        maxHei = Math.max(maxHei,mSwitchBacOn.getIntrinsicHeight());
        maxHei = Math.max(maxHei,mSwitchIconOff.getIntrinsicHeight()+2*mSwitchPadding);
        maxHei = Math.max(maxHei,mSwitchIconOn.getIntrinsicHeight()+2*mSwitchPadding);
        maxHei += getPaddingTop()+getPaddingBottom();


        int minWei = 2*mSwitchPadding+getPaddingTop()+getPaddingBottom();
        int minHei = 2*mSwitchPadding+getPaddingLeft()+getPaddingRight();

        if(maxWei<=minHei) maxWei = mSwitchSuggestWei + minWei;
        if(maxHei<=minHei) maxHei = mSwitchSuggestHei + minHei;

        setMeasuredDimension(resolveSize(maxWei,widthMeasureSpec),resolveSize(maxHei,heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize();
    }

    private void initSize(){
        bacRect.left = getPaddingLeft();
        bacRect.right = getWidth()-getPaddingRight();
        bacRect.top = getPaddingTop();
        bacRect.bottom = getHeight()-getPaddingBottom();

        iconRect.top = iconFixedRect.top=getPaddingTop()+mSwitchPadding;
        iconRect.bottom = iconFixedRect.bottom = getHeight()-getPaddingBottom()-mSwitchPadding;

        int maxWid = Math.max(0,Math.max(mSwitchIconOff.getIntrinsicWidth(),mSwitchIconOn.getIntrinsicWidth()));
        int maxHei = Math.max(0,Math.max(mSwitchIconOff.getIntrinsicHeight(),mSwitchIconOn.getIntrinsicHeight()));

        iconFixedRect.left = getPaddingLeft()+mSwitchPadding ;
        if(maxHei!=0&&maxWid!=0){
            iconFixedRect.right=iconFixedRect.left+(int)(1f*maxWid/maxHei*(iconFixedRect.bottom-iconFixedRect.top));
        }else {
            iconFixedRect.right = iconFixedRect.left+iconFixedRect.bottom-iconFixedRect.top;
        }

        targetMax = getWidth()-getPaddingRight()-getPaddingLeft()-2*mSwitchPadding-iconFixedRect.right+iconFixedRect.left;
        targetMax = Math.max(targetMax,0);

        targetCurrent = 0;
        targetCurrent = 0;

        mSwitchBacOff.setBounds(bacRect);
        mSwitchBacOn.setBounds(bacRect);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float radio = targetMax>0 ? 1f*targetCurrent/targetMax : 0;
        radio = Math.min(1,radio);
        radio = Math.max(0,radio);

        mSwitchBacOff.setAlpha((int)(255*(1-radio)));
        mSwitchBacOff.draw(canvas);

        mSwitchBacOn.setAlpha((int)(255*radio));
        mSwitchBacOn.draw(canvas);

        iconRect.left = iconFixedRect.left+targetCurrent;
        iconRect.right = iconFixedRect.right+targetCurrent;

        mSwitchIconOff.setAlpha((int)(255*(1-radio)));
        mSwitchIconOff.setBounds(iconRect);
        mSwitchIconOff.draw(canvas);

        mSwitchIconOn.setAlpha((int)(255*radio));
        mSwitchIconOn.setBounds(iconRect);
        mSwitchIconOn.draw(canvas);
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
                    if (mActivePointerId == -1) {
                        return false;
                    }
                    int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    if (pointerIndex < 0) {
                        return false;
                    }
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    if (xDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                                mInitialMotionX - mTouchSlop;
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                } else {
                    if (mActivePointerId == -1) {
                        return false;
                    }
                    int activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId);
                    if (activePointerIndex < 0) {
                        return false;
                    }
                    offsetSwitch(MotionEventCompat.getX(ev, activePointerIndex));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsBeingDragged && Math.abs(ev.getY() - mInitialMotionY) <= mTouchSlop / 1.5f) {
                    ensureTargetClick();
                }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    ensureTarget(false);
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

        if(mIsBeingDragged){
            if(getParent()!=null){
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }

        return true;
    }

    private void offsetSwitch(float x) {
        float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        targetCurrent-=deltaX;
        targetCurrent = Math.min(targetMax,targetCurrent);
        targetCurrent = Math.max(0,targetCurrent);
        invalidate();
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


    public void ensureTarget( boolean forceReverse) {
        final int animationTarget;

        if(forceReverse){
            if(targetCurrent>targetMax/2){
                animationTarget = 0;
            }else {
                animationTarget = targetMax;
            }
        }else {
            if(targetCurrent>targetMax/2){
                animationTarget = targetMax;
            }else {
                animationTarget = 0;
            }
        }


        if(animationTarget==targetCurrent || targetMax==0) return;

        if(valueAnimator!=null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofInt(targetCurrent,animationTarget);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                targetCurrent = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.setDuration((int)Math.abs(450f*(animationTarget-targetCurrent)/targetMax));
        valueAnimator.start();
    }


    public void ensureTargetClick(){
        if(mInitialMotionX<getWidth()/2 && targetCurrent> targetMax/2){
            ensureTarget(true);
        }else if(mInitialMotionX>getWidth()/2 && targetCurrent<targetMax/2){
            ensureTarget(true);
        }

    }


}
