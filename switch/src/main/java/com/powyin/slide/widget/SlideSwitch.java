package com.powyin.slide.widget;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.powyin.slide.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by powyin on 2016/8/2.
 */
public class SlideSwitch extends ViewGroup {

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private boolean mIsBeingDragged;

    float mInitialMotionX;
    float mInitialMotionY;

    private int mTouchSlop;
    private float mLastMotionX;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private int mMaximumVelocity;
    private ValueAnimator valueAnimator;
    private int mMaxWid;                                                  //最大布局宽度
    private float mSelectIndex = 0;
    private Drawable mSelectDrawable;                                     //选择区域显示
    private Drawable mSelectDrawableBac;                                  //选择区域背景
    private int mSelectHei;                                               //选择区域高度
    private int mSelectMaxItem;                                           //选择区域平分最大宽度
    private boolean mSelectShowOverScroll;                                //选择区域是否显示过度拉升
    private Rect mSelectDrawableRect = new Rect();                        //选择区域显示 边界
    private Rect mSelectDrawableRectBac = new Rect();                     //选择区域背景 边界
    private Path mSelectDrawablePath = new Path();                        //选择区域背景 绘制边界
    private List<View> mMatchParentChildren = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnScrollListener mOnScrollListener;

    Map<Integer, TypeViewInfo> mTypeToView = new HashMap<>();


    private class TypeViewInfo {
        Integer mType;
        List<View> holdViews = new ArrayList<>();
        int currentUsedPosition;

        TypeViewInfo(Integer type) {
            this.mType = type;
        }
    }

    public SlideSwitch(Context context) {
        this(context, null, 0);
    }

    public SlideSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitch);
        mSelectDrawable = a.getDrawable(R.styleable.SlideSwitch_pow_checked_drawable);
        if (mSelectDrawable == null) {
            mSelectDrawable = context.getResources().getDrawable(R.drawable.powyin_switch_slide_switch_select);
        }
        mSelectDrawableBac = a.getDrawable(R.styleable.SlideSwitch_pow_checked_bac);
        if (mSelectDrawableBac == null) {
            mSelectDrawableBac = context.getResources().getDrawable(R.drawable.powyin_switch_slide_switch_select_bac);
        }
        mSelectHei = (int) a.getDimension(R.styleable.SlideSwitch_pow_checked_hei, (int) (3.5 * density));
        mSelectMaxItem = a.getInt(R.styleable.SlideSwitch_pow_fixed_item, -1);
        mSelectShowOverScroll = a.getBoolean(R.styleable.SlideSwitch_pow_show_over_scroll, false);
        a.recycle();

        setScrollingCacheEnabled(true);
        mScroller = new Scroller(context);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        if (mSelectMaxItem <= 0) {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, mSelectHei);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                if (measureMatchParentChildren) {
                    if (lp.height == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.WRAP_CONTENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }

            maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight() - mSelectHei);
            maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

            // 设置测量大小
            setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(maxHeight + mSelectHei, heightMeasureSpec, 0));

            for (int i = 0; i < mMatchParentChildren.size(); i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        lp.leftMargin + lp.rightMargin,
                        lp.width);
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.max(0, getMeasuredHeight() - mSelectHei
                                - lp.topMargin - lp.bottomMargin), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }

        } else {
            int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
            float pace = (1f / (mSelectMaxItem) * (widthMeasure));
            int speWidthMeasure = MeasureSpec.makeMeasureSpec((int) pace, MeasureSpec.EXACTLY);
            int usedHei = mSelectHei;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                int speHeiMeasure = getChildMeasureSpec(heightMeasureSpec,
                        usedHei + lp.topMargin + lp.bottomMargin, lp.height);

                child.measure(speWidthMeasure, speHeiMeasure);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                if (lp.height == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.WRAP_CONTENT) {
                    mMatchParentChildren.add(child);
                }
            }

            maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight() - mSelectHei);

            // 设置测量大小
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    resolveSizeAndState(maxHeight + mSelectHei, heightMeasureSpec, 0));

            for (int i = 0; i < mMatchParentChildren.size(); i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.max(0, getMeasuredHeight()
                                - lp.topMargin - lp.bottomMargin - mSelectHei), MeasureSpec.EXACTLY);
                child.measure(speWidthMeasure, childHeightMeasureSpec);
            }
        }
    }

    int maxScrollX;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = 0;
        int childLeft = 0;
        final int count = getChildCount();
        if (mSelectMaxItem <= 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp =
                        (LayoutParams) child.getLayoutParams();
                childLeft += lp.leftMargin;
                child.layout(childLeft, childTop,
                        childWidth + childLeft, childHeight + childTop);
                childLeft += childWidth + lp.rightMargin;
            }
            mMaxWid = childLeft;
        } else {
            float pace = 1f / (mSelectMaxItem) * (r - l);
            int paddingLeft = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp =
                        (LayoutParams) child.getLayoutParams();
                child.layout(paddingLeft + (int) (i * pace) + lp.leftMargin, childTop, paddingLeft + (int) ((i + 1) * pace) - lp.rightMargin, childHeight + childTop);
            }
            mMaxWid = Math.max(getWidth(), (int) (pace * (count)));
        }
        maxScrollX = mMaxWid - getWidth();
        maxScrollX = maxScrollX > 0 ? maxScrollX : 0;
        calculationRect(true);
    }

    //OnDraw是画自己，dispatchDraw画自己的孩子，这里的就是画两个滑块
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mSelectHei > 0) {
            canvas.save();
            canvas.clipPath(mSelectDrawablePath);
            if (mSelectDrawableBac != null) {
                mSelectDrawableBac.setBounds(mSelectDrawableRectBac);
                mSelectDrawableBac.draw(canvas);
            }

            if (mSelectDrawable != null) {
                mSelectDrawable.setBounds(mSelectDrawableRect);                                                                //画这个view的背景
                mSelectDrawable.draw(canvas);
            }
            canvas.restore();
        }
        super.dispatchDraw(canvas);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mInitialMotionX = ev.getX();
            mInitialMotionY = ev.getY();
            mIsBeingDragged = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);

                if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
                    mScroller.abortAnimation();
                    mIsBeingDragged = true;

                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }

                    setScrollingCacheEnabled(true);
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    mIsBeingDragged = false;
                }
                break;
            }
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

                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }

                }
                break;
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = ev.findPointerIndex(mActivePointerId);
                float x = ev.getX(pointerIndex);
                float dx = x - mLastMotionX;
                float xDiff = Math.abs(dx);

                if (xDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    mLastMotionX = x;
                    setScrollingCacheEnabled(true);
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return false;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }


        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
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
                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    if (xDiff > mTouchSlop) {
                        mIsBeingDragged = true;

                        if (mVelocityTracker == null) {
                            mVelocityTracker = VelocityTracker.obtain();
                        } else {
                            mVelocityTracker.clear();
                        }

                        mLastMotionX = x;
                        setScrollingCacheEnabled(true);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                } else {
                    int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    offsetScrollX(ev.getX(activePointerIndex));
                    mLastMotionX = ev.getX(activePointerIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    startInternalFly();
                    ViewCompat.postInvalidateOnAnimation(this);
                }


                mActivePointerId = INVALID_POINTER;

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                return true;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }


        return true;
    }


    @Override
    public boolean performClick() {
        if (mIsBeingDragged) return true;

        for (int i = 0; i < getChildCount(); i++) {
            Rect globeRect = new Rect();
            View view = getChildAt(i);
            int currentScrollX = getScrollX();
            globeRect.top = view.getTop();
            globeRect.left = view.getLeft() - currentScrollX;
            globeRect.right = view.getRight() - currentScrollX;
            globeRect.bottom = view.getBottom();
            if (globeRect.contains((int) mInitialMotionX, (int) mInitialMotionY)) {
                if (mSelectIndex != i) {
                    preformItemSelectAnimationClick(i);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClicked(i, view);
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        throw new RuntimeException("not support");
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private void offsetScrollX(float x) {
        float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;

        int maxScroll = mMaxWid - getWidth();
        maxScroll = maxScroll > 0 ? maxScroll : 0;

        if (!mSelectShowOverScroll) {
            scrollX = scrollX > 0 ? scrollX : 0;
            scrollX = scrollX < maxScroll ? scrollX : 0;
        } else if (scrollX < 0) {
            //加入滑动阻尼系数
            int maxLen = getWidth() / 3;
            deltaX = (float) Math.pow(1f * Math.max(0, maxLen + scrollX) / maxLen, 3) * deltaX;
            scrollX = oldScrollX + deltaX;
        } else if (scrollX > maxScroll) {
            //加入滑动阻尼系数
            int maxLen = getWidth() / 3;
            deltaX = (float) Math.pow(1f * Math.max(0, maxLen - scrollX + maxScroll) / maxLen, 3) * deltaX;
            scrollX = oldScrollX + deltaX;
        }

        scrollTo((int) scrollX, getScrollY());
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        final int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.setDrawingCacheEnabled(enabled);
            }
        }
    }

    private void startInternalFly() {
        mScroller.abortAnimation();

        mVelocityTracker.computeCurrentVelocity(500, mMaximumVelocity);
        int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                mVelocityTracker, mActivePointerId);

        int scrollX = getScrollX();
        int maxScrollX = mMaxWid - getWidth();
        maxScrollX = maxScrollX > 0 ? maxScrollX : 0;

        if (scrollX < 0) {
            mScroller.startScroll(scrollX, 0, 0 - scrollX, 0, 350);
        } else if (scrollX > maxScrollX) {
            mScroller.startScroll(scrollX, 0, maxScrollX - scrollX, 0, 350);
        } else {
            mScroller.fling(getScrollX(), 0, -initialVelocity, 0, 0, maxScrollX, 0, 0);
        }
    }


    private void preformItemSelectAnimationClick(int targetIndex) {
        if (targetIndex < 0 || targetIndex >= getChildCount()) return;

        if (targetIndex != mSelectIndex) {
            if (valueAnimator != null) valueAnimator.cancel();

            View targetView = getChildAt((targetIndex));
            int center = targetView.getLeft() / 2 + targetView.getRight() / 2;
            int targetScrollX = -getWidth() / 2 + center;
            targetScrollX = targetScrollX > 0 ? targetScrollX : 0;

            int maxScroll = mMaxWid - getWidth();
            maxScroll = maxScroll > 0 ? maxScroll : 0;
            targetScrollX = targetScrollX < maxScroll ? targetScrollX : maxScroll;

            PropertyValuesHolder valuesHolderScrollX = PropertyValuesHolder.ofInt("scrollX", getScrollX(), targetScrollX);
            PropertyValuesHolder valuesHolderSelectIndex = PropertyValuesHolder.ofFloat("scrollRadio", mSelectIndex, targetIndex);

            valueAnimator = ValueAnimator.ofPropertyValuesHolder(valuesHolderScrollX, valuesHolderSelectIndex);
            valueAnimator.setDuration(150 + (int) (250 * Math.abs((targetIndex - mSelectIndex) * 1f / getChildCount())));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int mScrollX = (int) animation.getAnimatedValue("scrollX");
                    mSelectIndex = (float) animation.getAnimatedValue("scrollRadio");
                    if (getScrollX() != mScrollX) {
                        scrollTo(mScrollX, 0);
                    }
                    calculationRect(false);

                }
            });
            valueAnimator.start();
        }
    }

    // 动态计算位置；
    private void calculationRect(boolean reSizeBound) {
        if (mSelectDrawable != null && mSelectIndex >= 0 && mSelectIndex <= getChildCount() - 1) {

            int locLeft = (int) mSelectIndex;
            int locRight = locLeft + 1;

            View originView = getChildAt(locLeft);
            View targetView = locRight < getChildCount() ? getChildAt(locRight) : null;

            int c_left = targetView != null ? targetView.getLeft() - originView.getLeft() : 0;
            int c_right = targetView != null ? targetView.getRight() - originView.getRight() : 0;

            int oldLeft = mSelectDrawableRect.left;
            int oldRight = mSelectDrawableRect.right;

            mSelectDrawableRect.top = getHeight() - mSelectHei;
            mSelectDrawableRect.bottom = mSelectDrawableRect.top + mSelectHei;
            mSelectDrawableRect.left = (int) ((originView.getLeft() + c_left * (mSelectIndex - locLeft)));
            mSelectDrawableRect.right = (int) ((originView.getRight()) + c_right * (mSelectIndex - locLeft));

            if (oldLeft != mSelectDrawableRect.left || oldRight != mSelectDrawableRect.right) {
                if (mOnScrollListener != null) {
                    int center = (int) Math.rint(mSelectIndex);
                    mOnScrollListener.onPageScrolled(center, mSelectIndex - center);
                }
                ViewCompat.postInvalidateOnAnimation(SlideSwitch.this);
            }
            //-------------------
        }
        if (reSizeBound) {
            mSelectDrawablePath.reset();
            mSelectDrawablePath.addRect(0, 0, mMaxWid, getHeight(), Path.Direction.CCW);
            mSelectDrawablePath.close();
            mSelectDrawableRectBac.top = getHeight() - mSelectHei;
            mSelectDrawableRectBac.bottom = mSelectDrawableRectBac.top + mSelectHei;
            mSelectDrawableRectBac.left = 0;
            mSelectDrawableRectBac.right = mMaxWid;
        }
    }


    private ViewPager.OnPageChangeListener mViewPageChangeListener = new ViewPager.OnPageChangeListener() {
        boolean isDragTouch = false;                                                                          //是否手指触摸滚动
        boolean needGetPosition;
        float mFixedSelect;
        //        float mFixedPosition = 0;                                                                             //辅助记录手指触摸点信息；
        int mFixedScrollX = 0;                                                                                //辅助记录手指触摸点信息；

        @Override
        public void onPageScrolled(int position, float selectIndexOffset, int positionOffsetPixels) {
            if (!isDragTouch) return;
            if (needGetPosition) {
                mFixedSelect = (int) Math.rint(position + selectIndexOffset);
                needGetPosition = false;
            }

            float targetSelectIndex = position + selectIndexOffset;
            targetSelectIndex = targetSelectIndex < getChildCount() - 1 ? targetSelectIndex : getChildCount() - 1;

            int locLeft = (int) targetSelectIndex;
            int locRight = locLeft + 1;
            View originView = getChildAt(locLeft);
            View targetView = locRight < getChildCount() ? getChildAt(locRight) : originView;
            int scrollLeft = (originView.getLeft() + originView.getRight()) / 2;
            int scrollRight = (targetView.getLeft() + targetView.getRight()) / 2;
            int targetScrollX = (int) (scrollLeft + (targetSelectIndex - locLeft) * (scrollRight - scrollLeft));
            targetScrollX -= getWidth() / 2;

            targetScrollX = targetScrollX > 0 ? targetScrollX : 0;
            targetScrollX = targetScrollX < maxScrollX ? targetScrollX : maxScrollX;

            selectIndexOffset = targetSelectIndex - mFixedSelect;
            selectIndexOffset = selectIndexOffset < 0 ? -selectIndexOffset : selectIndexOffset;

            targetScrollX = (int) (mFixedScrollX + (targetScrollX - mFixedScrollX) * selectIndexOffset);

            targetScrollX = targetScrollX > 0 ? targetScrollX : 0;
            targetScrollX = targetScrollX < maxScrollX ? targetScrollX : maxScrollX;

            if (targetScrollX != getScrollX()) {
                scrollTo(targetScrollX, 0);
            }

            mSelectIndex = mFixedSelect + (targetSelectIndex - mFixedSelect) * selectIndexOffset;

            calculationRect(false);
        }

        @Override
        public void onPageSelected(int position) {


        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:     // 开始用户拖拉
                    if (mScroller != null) mScroller.abortAnimation();
                    if (valueAnimator != null) valueAnimator.cancel();
                    isDragTouch = true;
                    needGetPosition = true;
                    mFixedScrollX = getScrollX();
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:     // 结束用户拖拉

                    break;
                case ViewPager.SCROLL_STATE_IDLE:         // 结束所有位移
                    isDragTouch = false;
                    mSelectIndex = (int) Math.rint(mSelectIndex);
                    calculationRect(false);
                    break;
            }
        }
    };


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
        }
    }


    // 载入Adapter
    private void computeAdapter() {
        mTypeToView.clear();
        removeAllViews();
        if (mListAdapter == null || mListAdapter.getCount() == 0) return;
        int count = mListAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Integer type = mListAdapter.getItemViewType(i);
            if (!mTypeToView.containsKey(type)) {
                mTypeToView.put(type, new TypeViewInfo(type));
            }
            TypeViewInfo info = mTypeToView.get(type);

            View current = mListAdapter.getView(i, null, this);
            if (current == null)
                throw new RuntimeException("Adapter.getView(postion , convasView, viewParent) cannot be null ");
            info.holdViews.add(current);
            info.currentUsedPosition++;
            addView(current);
        }

        mSelectIndex = Math.min(mSelectIndex, count - 1);
        mSelectIndex = Math.max(0, mSelectIndex);
        calculationRect(true);
    }


    private ListAdapter mListAdapter;


    public void setAdapter(ListAdapter adapter) {
        if (mListAdapter == adapter) return;
        if (mListAdapter != null) {
            mListAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mListAdapter = adapter;
        if (adapter != null) {
            adapter.registerDataSetObserver(mDataSetObserver);
        }
        computeAdapter();
    }

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshAdapter();
        }

    };


    //刷新Adapter
    private void refreshAdapter() {
        removeAllViews();
        for (TypeViewInfo info : mTypeToView.values()) {
            info.currentUsedPosition = 0;
        }
        int count = mListAdapter != null ? mListAdapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            Integer type = mListAdapter.getItemViewType(i);
            if (!mTypeToView.containsKey(type)) {
                mTypeToView.put(type, new TypeViewInfo(type));
            }
            TypeViewInfo info = mTypeToView.get(type);
            View canvasView = info.currentUsedPosition < info.holdViews.size() ? info.holdViews.get(info.currentUsedPosition) : null;
            View current = mListAdapter.getView(i, canvasView, this);
            if (current == null)
                throw new RuntimeException("Adapter.getView(postion , convasView, viewParent) cannot be null ");
            if (canvasView != current) {
                if (canvasView == null) {
                    info.holdViews.add(current);
                } else {
                    info.holdViews.remove(canvasView);
                    info.holdViews.add(info.currentUsedPosition, current);
                }
            }
            info.currentUsedPosition++;
            addView(current);
        }

        mSelectIndex = Math.min(mSelectIndex, count - 1);
        mSelectIndex = Math.max(0, mSelectIndex);
        calculationRect(true);
    }


    //------------------------------------------------------------------setting-----------------------------------------------------------//


    public ViewPager.OnPageChangeListener getSupportOnPageChangeListener() {
        return mViewPageChangeListener;
    }


    /**
     * 设置选择项
     *
     * @param index
     */
    public void setSlectIndex(int index) {
        if (mSelectIndex != index) {
            mSelectIndex = index;
            mSelectIndex = Math.min(mSelectIndex, getChildCount() - 1);
            mSelectIndex = Math.max(0, mSelectIndex);
            calculationRect(false);
        }
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        setClickable(mOnItemClickListener != null);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }


}


