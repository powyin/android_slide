package com.powyin.slide.widget;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.Scroller;

import com.powyin.slide.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/8/2.
 */
public class SlideSwitch extends ViewGroup {
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private int mMaximumVelocity;
    private ValueAnimator valueAnimator;
    private int mMaxWid;
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
    private OnPageChangeListener mOnPageChangeListener;

    public SlideSwitch(Context context) {
        this(context, null, 0);
    }

    public SlideSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitch);
        mSelectDrawable = a.getDrawable(R.styleable.SlideSwitch_pow_checked_drawable);
        if (mSelectDrawable == null) {
            mSelectDrawable = context.getResources().getDrawable(R.drawable.powyin_switch_slide_switch_select);
        }
        mSelectDrawableBac = a.getDrawable(R.styleable.SlideSwitch_pow_checked_bac);
        if (mSelectDrawableBac == null) {
            mSelectDrawableBac = context.getResources().getDrawable(R.drawable.powyin_switch_slide_switch_select_bac);
        }
        mSelectHei = a.getInt(R.styleable.SlideSwitch_pow_checked_hei, 8);
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
        if (mSelectMaxItem<=0) {
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
            maxWidth += getPaddingLeft() + getPaddingRight();
            maxHeight += getPaddingTop() + getPaddingBottom();

            maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight() - mSelectHei);
            maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

            // 设置测量大小
            setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(maxHeight + mSelectHei, heightMeasureSpec, 0));

            for (int i = 0; i < mMatchParentChildren.size(); i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight() +
                                lp.leftMargin + lp.rightMargin,
                        lp.width);
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.max(0, getMeasuredHeight() - mSelectHei
                                - getPaddingTop() - getPaddingBottom()
                                - lp.topMargin - lp.bottomMargin), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }

        } else {
            int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
            float pace = (1f / (mSelectMaxItem) * (widthMeasure - getPaddingLeft() - getPaddingRight())) ;
            int speWidthMeasure =  MeasureSpec.makeMeasureSpec((int)pace, MeasureSpec.EXACTLY) ;
            int usedHei = getPaddingTop() + getPaddingBottom() + mSelectHei;
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
            maxHeight += getPaddingTop() + getPaddingBottom();
            maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight() - mSelectHei);

            // 设置测量大小
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    resolveSizeAndState(maxHeight + mSelectHei, heightMeasureSpec, 0));

            for (int i = 0; i < mMatchParentChildren.size(); i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.max(0, getMeasuredHeight()
                                - getPaddingTop() - getPaddingBottom()
                                - lp.topMargin - lp.bottomMargin - mSelectHei), MeasureSpec.EXACTLY);
                child.measure(speWidthMeasure, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = getPaddingTop();
        int childLeft = getPaddingLeft();
        final int count = getChildCount();
        if (mSelectMaxItem<=0) {
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
            mMaxWid = childLeft - getPaddingLeft();
        } else {
            float pace = 1f / (mSelectMaxItem) * (r - l - getPaddingLeft() - getPaddingRight());
            int paddingLeft = getPaddingLeft();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp =
                        (LayoutParams) child.getLayoutParams();
                child.layout(paddingLeft + (int)(i * pace) + lp.leftMargin, childTop, paddingLeft + (int)((i+1) * pace) - lp.rightMargin, childHeight + childTop);
            }
            mMaxWid =Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), (int)(pace*(count)));
        }
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_POINTER;
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
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

                if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
                    mScroller.abortAnimation();
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
                    setScrollingCacheEnabled(true);
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

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

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
                        setScrollingCacheEnabled(true);

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
                    startFly();
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                mActivePointerId = INVALID_POINTER;
                mVelocityTracker.clear();
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


    private boolean performItemClick() {
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
                    if (mOnPageChangeListener != null) {
                        mOnPageChangeListener.onPageSelected(i);
                    }
                }
                break;
            }
        }

        return true;
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
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void offsetScrollX(float x) {
        float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;
        mLastMotionX += scrollX - (int) scrollX;

        int maxScroll = mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth();
        if (!mSelectShowOverScroll) {
            scrollX = Math.min(Math.max(scrollX, 0), maxScroll);
        }else if (scrollX < 0) {
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

    private void startFly() {
        mVelocityTracker.computeCurrentVelocity(500, mMaximumVelocity);
        int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                mVelocityTracker, mActivePointerId);
        int scrollX = getScrollX();
        int maxScrollX = mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth();
        mScroller.abortAnimation();
        if (scrollX < 0) {
            mScroller.startScroll(scrollX, 0, 0 - scrollX, 0, 450);
        } else if (scrollX > maxScrollX) {
            mScroller.startScroll(scrollX, 0, maxScrollX - scrollX, 0, 450);
        } else {
            mScroller.fling(getScrollX(), 0, -initialVelocity, 0, 0, maxScrollX, 0, 0);
        }
    }


    private void preformItemSelectAnimationClick(int targetIndex) {
        if (targetIndex < 0 || targetIndex >= getChildCount()) return;

        if (targetIndex != mSelectIndex && Math.abs(targetIndex - mSelectIndex) > 0.1f) {
            if (valueAnimator != null) valueAnimator.cancel();

            View targetView = getChildAt((targetIndex));
            int center = targetView.getLeft() / 2 + targetView.getRight() / 2;
            int temTargetX = -getWidth() / 2 - getPaddingLeft() / 2 + getPaddingRight() / 2 + center;

            int startScrollX = getScrollX();
            int endScrollX = Math.min(Math.max(temTargetX, 0), mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth());

            PropertyValuesHolder valuesHolderScrollX = PropertyValuesHolder.ofInt("scrollX",startScrollX,endScrollX);
            PropertyValuesHolder valuesHolderSelectIndex = PropertyValuesHolder.ofFloat("scrollRadio",mSelectIndex,targetIndex);

            valueAnimator = ValueAnimator.ofPropertyValuesHolder(valuesHolderScrollX, valuesHolderSelectIndex);
            valueAnimator.setDuration(150 + (int) (250 * Math.abs((targetIndex - mSelectIndex) * 1f / getChildCount())));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    int left = mSelectDrawableRect.left;
                    int right = mSelectDrawableRect.right;

                    int mScrollX = (int)  animation.getAnimatedValue("scrollX");
                    mSelectIndex = (float) animation.getAnimatedValue("scrollRadio");

                    if (getScrollX() != mScrollX) {
                        scrollTo(mScrollX, 0);
                    }

                    calculationRect(false);
                    if (left != mSelectDrawableRect.left || right != mSelectDrawableRect.right) {
                        if (mOnPageChangeListener != null) {
                            mOnPageChangeListener.onPageScroll(mSelectIndex);
                        }
                        ViewCompat.postInvalidateOnAnimation(SlideSwitch.this);
                    }

                }
            });
            valueAnimator.start();
        }
    }

    // 动态计算位置；
    private void calculationRect(boolean reSizeBound) {
        if (mSelectDrawable != null && mSelectIndex >= 0 && mSelectIndex + 1 <= getChildCount()) {
            View originView = getChildAt((int) mSelectIndex);
            View targetView = getChildAt((int) (mSelectIndex + 1));

            int c_left = targetView != null ? targetView.getLeft() - originView.getLeft() : 0;
            int c_right = targetView != null ? targetView.getRight() - originView.getRight() : 0;

            float radio = mSelectIndex - (int) mSelectIndex;

            //根据选择的位置实例化一个View，然后画
            mSelectDrawableRect.top = getHeight() - getPaddingBottom() - mSelectHei;
            mSelectDrawableRect.bottom = mSelectDrawableRect.top + mSelectHei;
            mSelectDrawableRect.left = (int) ((originView.getLeft() + c_left * radio));
            mSelectDrawableRect.right = (int) ((originView.getRight()) + c_right * radio);
        }
        if (reSizeBound) {
            mSelectDrawablePath.reset();
            mSelectDrawablePath.addRect(getPaddingLeft(), getPaddingTop(), mMaxWid + getPaddingLeft(), getHeight() - getPaddingBottom(), Path.Direction.CCW);
            mSelectDrawablePath.close();
            mSelectDrawableRectBac.top = getHeight() - getPaddingBottom() - mSelectHei;
            mSelectDrawableRectBac.bottom = mSelectDrawableRectBac.top + mSelectHei;
            mSelectDrawableRectBac.left = getPaddingLeft();
            mSelectDrawableRectBac.right = mMaxWid + getPaddingLeft();
        }
    }


    private ViewPager.OnPageChangeListener mViewPageChangeListener = new ViewPager.OnPageChangeListener() {
        boolean isDragTouch = false;                                                                          //是否手指触摸滚动
        float mFixedPosition = 0;                                                                             //辅助记录手指触摸点信息；
        int mFixedScrollX = 0;                                                                                //辅助记录手指触摸点信息；
        boolean mIsJustBegin = true;                                                                          //辅助记录手指触摸点信息；

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isDragTouch) return;
            if (mScroller != null) mScroller.abortAnimation();
            if (valueAnimator != null) valueAnimator.cancel();
            if (mIsJustBegin) {
                mFixedPosition = position + positionOffset;
                mIsJustBegin = false;
            }

            int left = mSelectDrawableRect.left;
            int right = mSelectDrawableRect.right;
            mSelectIndex = position + positionOffset;
            calculationRect(false);
            if (left != mSelectDrawableRect.left || right != mSelectDrawableRect.right) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScroll(mSelectIndex);
                }
                ViewCompat.postInvalidateOnAnimation(SlideSwitch.this);
            }

            int targetScrollX = getScrollXByFloatWei(mSelectIndex);
            float diff = Math.abs(mSelectIndex - mFixedPosition) > 0.5f ? 1 : Math.min(Math.abs((mSelectIndex - mFixedPosition) * 2), 1);
            targetScrollX = (int) (diff * targetScrollX + (1 - diff) * mFixedScrollX);

            if (targetScrollX != getScrollX()) {
                scrollTo(targetScrollX, 0);
            }
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:     // 开始用户拖拉
                    isDragTouch = true;
                    mFixedScrollX = getScrollX();
                    mIsJustBegin = true;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:     // 结束用户拖拉

                    break;
                case ViewPager.SCROLL_STATE_IDLE:         // 结束所有位移
                    isDragTouch = false;
                    break;
            }
        }
    };

    // 得到权重值对应的滑动ScrollX
    private int getScrollXByFloatWei(float mIndex) {
        int position = (int) mIndex;
        float positionOffset = mIndex - (int) mIndex;
        int center;
        int temTargetX;
        int startScrollX;
        int endScrollX;

        if (position < 0) {
            startScrollX = 0;
        } else if (position >= getChildCount()) {
            startScrollX = mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth();
        } else {
            View startView = getChildAt(position);
            center = startView.getLeft() / 2 + startView.getRight() / 2;
            temTargetX = -getWidth() / 2 - getPaddingLeft() / 2 + getPaddingRight() / 2 + center;
            startScrollX = Math.min(Math.max(temTargetX, 0), mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth());
        }

        int targetIndex = (int) (position + positionOffset + 1);
        if (targetIndex < 0) {
            endScrollX = 0;
        } else if (targetIndex >= getChildCount()) {
            endScrollX = mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth();
        } else {
            View targetView = getChildAt((targetIndex));
            center = targetView.getLeft() / 2 + targetView.getRight() / 2;
            endScrollX = -getWidth() / 2 - getPaddingLeft() / 2 + getPaddingRight() / 2 + center;
            endScrollX = Math.min(Math.max(endScrollX, 0), mMaxWid + getPaddingLeft() + getPaddingRight() - getWidth());
        }

        return (int) ((endScrollX - startScrollX) * positionOffset + startScrollX);
    }

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


    //------------------------------------------------------------------setting-----------------------------------------------------------//


    public ViewPager.OnPageChangeListener getSupportOnPageChangeListener() {
        return mViewPageChangeListener;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public interface OnPageChangeListener {
        void onPageSelected(int position);

        void onPageScroll(float mScroll);
    }
}

