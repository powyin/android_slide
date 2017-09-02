package com.powyin.slide.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import com.powyin.slide.R;

import java.lang.reflect.Method;

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
    private int mSwitchSuggestWei;
    private int mSwitchSuggestHei;
    private Rect iconRect = new Rect();
    private Rect iconFixedRect = new Rect();
    private Rect bacRect = new Rect();
    private int targetMax;
    private int targetCurrent;
    private boolean mIsOpen;
    private OnToggleListener mOnToggleListener;


    public PowSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;
        mSwitchSuggestWei = (int) (36 * density);
        mSwitchSuggestHei = (int) (16 * density);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PowSwitch);

        mSwitchBacOff = a.getDrawable(R.styleable.PowSwitch_pow_switch_bac_off);
        if (mSwitchBacOff == null) {
            mSwitchBacOff = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_bac_off);
        }
        mSwitchBacOn = a.getDrawable(R.styleable.PowSwitch_pow_switch_bac_on);
        if (mSwitchBacOn == null) {
            mSwitchBacOn = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_bac_on);
        }
        mSwitchIconOff = a.getDrawable(R.styleable.PowSwitch_pow_switch_icon_off);
        if (mSwitchIconOff == null) {
            mSwitchIconOff = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_icon_off);
        }
        mSwitchIconOn = a.getDrawable(R.styleable.PowSwitch_pow_switch_icon_on);
        if (mSwitchIconOn == null) {
            mSwitchIconOn = context.getResources().getDrawable(R.drawable.powyin_switch_pow_switch_icon_on);
        }


        a.recycle();


        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context)) / 2;

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
        maxWei = Math.max(maxWei, mSwitchBacOff.getIntrinsicWidth());
        maxWei = Math.max(maxWei, mSwitchBacOn.getIntrinsicWidth());
        maxWei = Math.max(maxWei, mSwitchIconOff.getIntrinsicWidth());
        maxWei = Math.max(maxWei, mSwitchIconOn.getIntrinsicWidth());
        maxWei += getPaddingLeft() + getPaddingRight();

        int maxHei = 0;
        maxHei = Math.max(maxHei, mSwitchBacOff.getIntrinsicHeight());
        maxHei = Math.max(maxHei, mSwitchBacOn.getIntrinsicHeight());
        maxHei = Math.max(maxHei, mSwitchIconOff.getIntrinsicHeight());
        maxHei = Math.max(maxHei, mSwitchIconOn.getIntrinsicHeight());
        maxHei += getPaddingTop() + getPaddingBottom();


        int minWei = getPaddingTop() + getPaddingBottom();
        int minHei = getPaddingLeft() + getPaddingRight();

        if (maxWei <= minHei) maxWei = mSwitchSuggestWei + minWei;
        if (maxHei <= minHei) maxHei = mSwitchSuggestHei + minHei;

        setMeasuredDimension(resolveSize(maxWei, widthMeasureSpec), resolveSize(maxHei, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize();
    }

    private void initSize() {
        bacRect.left = getPaddingLeft();
        bacRect.right = getWidth() - getPaddingRight();
        bacRect.top = getPaddingTop();
        bacRect.bottom = getHeight() - getPaddingBottom();

        iconRect.top = iconFixedRect.top = getPaddingTop();
        iconRect.bottom = iconFixedRect.bottom = getHeight() - getPaddingBottom();

        int maxWid = Math.max(0, Math.max(mSwitchIconOff.getIntrinsicWidth(), mSwitchIconOn.getIntrinsicWidth()));
        int maxHei = Math.max(0, Math.max(mSwitchIconOff.getIntrinsicHeight(), mSwitchIconOn.getIntrinsicHeight()));

        iconFixedRect.left = getPaddingLeft();
        if (maxHei != 0 && maxWid != 0) {
            iconFixedRect.right = iconFixedRect.left + (int) (1f * maxWid / maxHei * (iconFixedRect.bottom - iconFixedRect.top));
        } else {
            iconFixedRect.right = iconFixedRect.left + iconFixedRect.bottom - iconFixedRect.top;
        }

        targetMax = getWidth() - getPaddingRight() - getPaddingLeft() - iconFixedRect.right + iconFixedRect.left;
        targetMax = Math.max(targetMax, 0);

        targetCurrent = 0;
        targetCurrent = 0;

        mSwitchBacOff.setBounds(bacRect);
        mSwitchBacOn.setBounds(bacRect);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            ensureTarget();
        }

    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        throw new RuntimeException("not support");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float radio = targetMax > 0 ? 1f * targetCurrent / targetMax : 0;
        radio = Math.min(1, radio);
        radio = Math.max(0, radio);

        mSwitchBacOff.setAlpha((int) (255 * (1 - radio)));
        mSwitchBacOff.draw(canvas);

        mSwitchBacOn.setAlpha((int) (255 * radio));
        mSwitchBacOn.draw(canvas);

        iconRect.left = iconFixedRect.left + targetCurrent;
        iconRect.right = iconFixedRect.right + targetCurrent;

        mSwitchIconOff.setAlpha((int) (255 * (1 - radio)));
        mSwitchIconOff.setBounds(iconRect);
        mSwitchIconOff.draw(canvas);

        mSwitchIconOn.setAlpha((int) (255 * radio));
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
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {                                                                              // just in case

                    int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (pointerIndex < 0) {
                        return false;
                    }
                    final float x = ev.getX(pointerIndex);
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
                    int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (activePointerIndex < 0) {
                        return false;
                    }
                    offsetSwitch(ev.getX(activePointerIndex));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsBeingDragged && Math.abs(ev.getY() - mInitialMotionY) <= mTouchSlop) {
                    if (mIsOpen && mInitialMotionX < getWidth() / 2) {
                        mIsOpen = false;
                        if (mOnToggleListener != null) {
                            mOnToggleListener.onToggle(false);
                        }
                        ensureTarget();
                    } else if (!mIsOpen && mInitialMotionX > getWidth() / 2) {
                        mIsOpen = true;
                        if (mOnToggleListener != null) {
                            mOnToggleListener.onToggle(true);
                        }
                        ensureTarget();
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    if (mIsOpen && targetCurrent < targetMax / 2) {
                        mIsOpen = false;
                        if (mOnToggleListener != null) {
                            mOnToggleListener.onToggle(mIsOpen);
                        }
                    } else if (!mIsOpen && targetCurrent > targetMax / 2) {
                        mIsOpen = true;
                        if (mOnToggleListener != null) {
                            mOnToggleListener.onToggle(mIsOpen);
                        }
                    }
                    ensureTarget();
                }
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = ev.getX(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int actionPointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = ev.getPointerId(actionPointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = actionPointerIndex == 0 ? 1 : 0;
                    mLastMotionX = ev.getX(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
        }

        return true;
    }

    private void offsetSwitch(float x) {
        float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        targetCurrent -= deltaX;
        targetCurrent = Math.min(targetMax, targetCurrent);
        targetCurrent = Math.max(0, targetCurrent);
        invalidate();
    }



    private void ensureTarget() {
        final int animationTarget;

        if (mIsOpen) {
            animationTarget = targetMax;
        } else {
            animationTarget = 0;
        }

        if (animationTarget == targetCurrent || targetMax == 0) return;

        if (valueAnimator != null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofInt(targetCurrent, animationTarget);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                targetCurrent = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.setDuration(Math.max(50, (int) Math.abs(450f * (animationTarget - targetCurrent) / targetMax)));
        valueAnimator.start();
    }


    //---------------------------------------------setting----------------------------------------------//

    // 设置开启
    public void setOpen(boolean isOpen) {
        if (mIsOpen != isOpen) {
            mIsOpen = isOpen;
            ensureTarget();
        }
    }

    // 是否开启
    public boolean isOpen() {
        return mIsOpen;
    }

    public void setOnToggleListener(OnToggleListener listener) {
        this.mOnToggleListener = listener;
    }


    public interface OnToggleListener {
        void onToggle(boolean isOpen);
    }


}
