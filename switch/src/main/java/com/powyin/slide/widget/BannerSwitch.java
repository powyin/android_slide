package com.powyin.slide.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
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
 * Created by powyin on 2016/8/5.
 */
public class BannerSwitch extends ViewGroup {

    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private boolean mIsMultipleFinger;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    private OnItemClickListener mOnItemClickListener;
    private OnButtonLineScrollListener mOnButtonLineScrollListener;
    private ValueAnimator mSwitchAnimator;
    private AnimationRun autoProgress;
    private int mSwitchFixedItem;
    private boolean mTouchScrollEnable;
    private boolean mSwitchEdge;
    private int mSwitchPagePeriod;
    private int mSwitchAnimationPeriod;
    private boolean mIsTouched;
    private long mSwitchEndTime;
    private float mSelectIndex;             // 横幅滚动轴；
    private ListAdapter mListAdapter;

    SparseArray<TypeViewInfo> mTypeToView = new SparseArray<TypeViewInfo>();

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshAdapter();
        }

    };

    private class TypeViewInfo {
        Integer mType;
        List<View> holdViews = new ArrayList<>();
        int currentUsedPosition;

        TypeViewInfo(Integer type) {
            this.mType = type;
        }
    }


    private class AnimationRun implements Runnable {
        boolean isCancel;

        @Override
        public void run() {
            if (isCancel) return;
            long pre = (System.currentTimeMillis() - mSwitchEndTime);
            if (getVisibility() == VISIBLE && !mIsTouched && pre >= mSwitchPagePeriod / 3) {
                startInternalFlySwipePage(-1);
            }

            if (pre >= mSwitchPagePeriod / 3) {
                postDelayed(this, mSwitchPagePeriod);
            } else {
                postDelayed(this, mSwitchPagePeriod / 10);
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

        mSwitchFixedItem = a.getInt(R.styleable.BannerSwitch_pow_switch_fixed_item, 1);
        mTouchScrollEnable = a.getBoolean(R.styleable.BannerSwitch_pow_switch_touch_scroll, true);
        mSwitchEdge = a.getBoolean(R.styleable.BannerSwitch_pow_switch_fixed_edge, false);


        mSwitchPagePeriod = a.getInt(R.styleable.BannerSwitch_pow_switch_period, 2550);
        mSwitchPagePeriod = Math.max(1500, mSwitchPagePeriod);
        mSwitchPagePeriod = Math.min(10000, mSwitchPagePeriod);


        mSwitchAnimationPeriod = a.getInt(R.styleable.BannerSwitch_pow_switch_animation_period, 550);
        mSwitchAnimationPeriod = Math.max(100, mSwitchAnimationPeriod);
        mSwitchAnimationPeriod = Math.min(1500, mSwitchAnimationPeriod);

        a.recycle();
        setScrollingCacheEnabled();

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        float pace = Math.max(MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        int speWidthMeasure = count > 0 ? MeasureSpec.makeMeasureSpec(
                (int) pace, MeasureSpec.EXACTLY) : 0;
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
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

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
        final int count = getChildCount();
        int pace = Math.max(r - l - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            child.layout(childLeft, childTop, childLeft + pace, childHeight + childTop);
        }
        ensureTranslationOrder();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mTouchScrollEnable) {
            return super.dispatchTouchEvent(ev);
        }

        mIsTouched = true;

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mIsTouched = false;
            mIsMultipleFinger = false;
        }

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            mIsMultipleFinger = true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mIsUnableToDrag = false;
            mIsBeingDragged = false;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mTouchScrollEnable) {
            return super.onInterceptTouchEvent(ev);
        }

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

                if (mSwitchAnimator != null && mSwitchAnimator.isStarted() && mSwitchAnimator.isRunning()) {
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


                if (dx != 0 &&
                        canScroll(this, false, (int) dx, (int) x, (int) y)) {
                    mIsUnableToDrag = true;
                    return false;
                }


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
        if (!mTouchScrollEnable) {
            return super.onTouchEvent(ev);
        }

        final int action = ev.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                mLastMotionX = mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    if (mActivePointerId == -1) return false;
                    int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    if (pointerIndex < 0) return false;

                    float x = MotionEventCompat.getX(ev, pointerIndex);
                    float y = MotionEventCompat.getY(ev, pointerIndex);

                    final float xDiff = Math.abs(x - mLastMotionX);


                    if (xDiff != 0 &&
                            canScroll(this, false, (int) xDiff, (int) x, (int) y)) {
                        mIsUnableToDrag = true;
                        return false;
                    }

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
                if (!mIsBeingDragged && Math.abs(ev.getY() - mInitialMotionY) <= mTouchSlop && !mIsMultipleFinger) {
                    performItemClick();
                }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    startInternalFly();
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
        if (autoProgress != null) {
            autoProgress.isCancel = true;
        }
        if (!mSwitchEdge) {
            autoProgress = new AnimationRun();
            postDelayed(autoProgress, (int) (mSwitchPagePeriod / 1.5f));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (autoProgress != null) {
            autoProgress.isCancel = true;
        }
    }


    private boolean performItemClick() {
        for (int i = 0; i < getChildCount(); i++) {
            Rect globeRect = new Rect();
            View view = getChildAt(i);
            globeRect.top = view.getTop();
            globeRect.left = view.getLeft() + (int) view.getTranslationX();
            globeRect.right = view.getRight() + (int) view.getTranslationX();
            globeRect.bottom = view.getBottom();

            if (globeRect.contains((int) mInitialMotionX, (int) mInitialMotionY)) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(i, view);
                }
            }
        }
        return true;
    }

    // 检查子元素 是否存在滑动可能
    private boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && ViewCompat.canScrollHorizontally(v, -dx);
    }

    // 重写是否支持滑动
    @Override
    public boolean canScrollHorizontally(int direction) {
        if (!mSwitchEdge) {
            return true;
        }
        return getChildCount() != 0 && ((direction < 0 && mSelectIndex <= -0.05f) || (direction > 0 && mSelectIndex >= -getChildCount() + 1 + 0.05f));
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
        float pace = Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        if (pace <= 0) return;
        mSelectIndex -= deltaX / pace;

        if (mSwitchEdge) {
            mSelectIndex = Math.min(0, mSelectIndex);
            mSelectIndex = Math.max(-getChildCount() + 1, mSelectIndex);
        }

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

    // 抬手后状态恢复动画
    private void startInternalFly() {

        double diff = mSelectIndex - Math.rint(mSelectIndex);

        if (diff == 0) return;

        if (mSwitchAnimator != null) mSwitchAnimator.cancel();
        final float mTarget;

        if (Math.abs(diff) < 0.05) {
            mTarget = (int) Math.rint(mSelectIndex);
        } else if (mLastMotionX - mInitialMotionX > 0) {
            if (Math.abs((int) mSelectIndex - mSelectIndex) > 0.5 && mSelectIndex < 0 || Math.abs((int) mSelectIndex - mSelectIndex) <= 0.5 && mSelectIndex > 0) {
                mTarget = (int) (Math.rint(mSelectIndex) + 1);
            } else {
                mTarget = (int) (Math.rint(mSelectIndex));
            }
        } else {
            if (Math.abs((int) mSelectIndex - mSelectIndex) > 0.5 && mSelectIndex < 0 || Math.abs((int) mSelectIndex - mSelectIndex) <= 0.5 && mSelectIndex > 0) {
                mTarget = (int) (Math.rint(mSelectIndex));
            } else {
                mTarget = (int) (Math.rint(mSelectIndex) - 1);
            }
        }


        mSwitchAnimator = ValueAnimator.ofFloat(mSelectIndex, mTarget);
        mSwitchAnimator.setDuration(Math.max(50, (int) Math.abs((mSelectIndex - mTarget) * mSwitchAnimationPeriod) / mSwitchFixedItem));
        mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSelectIndex = (float) animation.getAnimatedValue();

                if (mSwitchEdge) {
                    mSelectIndex = Math.min(0, mSelectIndex);
                    mSelectIndex = Math.max(-getChildCount() + 1, mSelectIndex);
                }

                ensureTranslationOrder();
            }
        });
        mSwitchAnimator.start();
        mSwitchEndTime = System.currentTimeMillis() + mSwitchAnimator.getDuration() + 100;

    }


    // 自动轮播动画
    private void startInternalFlySwipePage(int step) {
        if (mSwitchAnimator != null && mSwitchAnimator.isStarted() && mSwitchAnimator.isRunning()) {
            return;
        }

        float mTarget = (int) (Math.rint(mSelectIndex) + step);
        mSwitchAnimator = ValueAnimator.ofFloat(mSelectIndex, mTarget);
        mSwitchAnimator.setDuration(Math.max(50, (int) Math.abs((mSelectIndex - mTarget) * mSwitchAnimationPeriod) / mSwitchFixedItem));
        mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSelectIndex = (float) animation.getAnimatedValue();

                if (mSwitchEdge) {
                    mSelectIndex = Math.min(0, mSelectIndex);
                    mSelectIndex = Math.max(-getChildCount() + 1, mSelectIndex);
                }

                ensureTranslationOrder();
            }
        });
        mSwitchAnimator.start();
        mSwitchEndTime = System.currentTimeMillis() + mSwitchAnimator.getDuration() + 50;
    }

    // 实现viewItem滚动总入口
    private void ensureTranslationOrder() {

        int count = getChildCount();
        float pace = Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        if (count < 2 || pace <= 0) return;

        if (mOnButtonLineScrollListener != null) {
            float index = (mSwitchFixedItem / 2 - mSelectIndex) % count;
            if (index < 0) index += count;

            int locLeft = (int) index;
            int locRight = locLeft + 1 >= count ? 0 : locLeft + 1;
            float diff = index - locLeft;

            mOnButtonLineScrollListener.onButtonLineScroll(
                    count, locLeft, locRight, getChildAt(locLeft), getChildAt(locRight), 1 - diff, diff);
        }


        for (int i = 0; i < count; i++) {
            int x = (int) ((i + mSelectIndex) * pace);
            int transX = x % (int) (count * pace);
            if (transX >= (int) (mSwitchFixedItem * pace)) {
                // 防止右边空白
                getChildAt(i).setTranslationX((int) (transX - count * pace));
            } else if (transX <= (int) ((-pace))) {
                // 防止左边空白
                getChildAt(i).setTranslationX((int) (transX + count * pace));
            } else {
                getChildAt(i).setTranslationX(transX);
            }
        }
    }

    // 刷新Adapter
    private void refreshAdapter() {
        removeAllViews();

        for(int i=0 ;i <mTypeToView.size();i++){
            mTypeToView.valueAt(i).currentUsedPosition = 0;
        }

        int count = mListAdapter != null ? mListAdapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            Integer type = mListAdapter.getItemViewType(i);

            if(mTypeToView.indexOfKey(type) <0){
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
    }

    // 载入Adapter
    private void computeAdapter() {
        mTypeToView.clear();
        removeAllViews();
        if (mListAdapter == null || mListAdapter.getCount() == 0) return;
        int count = mListAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Integer type = mListAdapter.getItemViewType(i);
            if (mTypeToView.indexOfKey(type) < 0) {
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
    }

    //------------------------------------------------------------------setting-----------------------------------------------------------//

    // 设置适配器
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

    /**
     * @param index           选中页面
     * @param animation       是否滑动过渡
     */
    public void setSelectPage(int index, boolean animation){
        if (mSwitchAnimator != null) {
            mSwitchAnimator.cancel();
            mSwitchAnimator = null;
        }

        if(index == mSelectIndex){
            return;
        }

        if(animation){
            mSwitchAnimator = ValueAnimator.ofFloat(mSelectIndex, index);
            mSwitchAnimator.setDuration(Math.max(50, (int) Math.abs((mSelectIndex - index) * mSwitchAnimationPeriod) / mSwitchFixedItem));
            mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSelectIndex = (float) animation.getAnimatedValue();

                    if (mSwitchEdge) {
                        mSelectIndex = Math.min(0, mSelectIndex);
                        mSelectIndex = Math.max(-getChildCount() + 1, mSelectIndex);
                    }

                    ensureTranslationOrder();
                }
            });
            mSwitchAnimator.start();
            mSwitchEndTime = System.currentTimeMillis() + mSwitchAnimator.getDuration() + 50;
        }else {
            mSelectIndex = index;
            if (mSwitchEdge) {
                mSelectIndex = Math.min(0, mSelectIndex);
                mSelectIndex = Math.max(-getChildCount() + 1, mSelectIndex);
            }
            ensureTranslationOrder();
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnButtonLineScrollListener(OnButtonLineScrollListener listener) {
        this.mOnButtonLineScrollListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClicked(int position, View view);
    }

    public interface OnButtonLineScrollListener {
        /**
         * @param viewCount    当前View数量
         * @param leftIndex    这边View 位置
         * @param rightIndex   右边View 位置
         * @param leftView     这边View
         * @param rightView    右边View
         * @param leftNearWei  中央位置接近 右边View 的尺度  0 表示远离； 1 表示重合
         * @param rightNearWei 中央位置接近 右边View 的尺度  0 表示远离； 1 表示重合
         */
        void onButtonLineScroll(int viewCount, int leftIndex, int rightIndex, View leftView, View rightView, float leftNearWei, float rightNearWei);
    }

}


















