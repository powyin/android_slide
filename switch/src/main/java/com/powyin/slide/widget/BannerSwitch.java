package com.powyin.slide.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListAdapter;

import com.powyin.slide.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/8/5.
 */
public class BannerSwitch extends ViewGroup {

    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;

    private float mLastMotionX;
    private float mLastMotionY;

    private float mInitialMotionX;
    private float mInitialMotionY;

    private int mActivePointerId = -1;

    private OnItemClickListener mOnItemClickListener;
    private OnScrollListener mOnScrollListener;
    private ValueAnimator mSwitchAnimator;
    private AnimationRun autoProgress;
    private int mSwitchFixedItem;

    private boolean mTouchScrollEnable;
    private boolean mSwitchEdge;

    private int mSwitchPagePeriod;
    private int mSwitchAnimationPeriod;
    private float mSelectIndex;             // 横幅滚动轴；
    private float scrollDirection;


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
        boolean delay = false;

        @Override
        public void run() {
            // ------------------------------------------------ 控制容量不够 与 展示边界情况---------------------------------------------------//
            if (isCancel || mSwitchFixedItem > getChildCount() || mSwitchEdge) {
                return;
            }
            // ------------------------------------------------------------------------------------------------------------------------------//


            if (getVisibility() == VISIBLE && !delay) {
                startInternalPageFly(-1, true);
            }

            if (delay) {
                postDelayed(this, mSwitchPagePeriod / 2);
            } else {
                postDelayed(this, mSwitchPagePeriod);
            }

            delay = false;
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
        mTouchSlop = configuration.getScaledPagingTouchSlop();


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
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (autoProgress != null) {
            autoProgress.delay = true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                mIsUnableToDrag = false;
                mIsBeingDragged = false;
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mTouchScrollEnable || mIsUnableToDrag) {
            return false;
        }
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(0);
                mIsUnableToDrag = false;
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();

                if (mSwitchAnimator != null && mSwitchAnimator.isStarted() && mSwitchAnimator.isRunning()) {
                    mSwitchAnimator.cancel();
                    mSwitchAnimator = null;
                    mIsBeingDragged = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollingCacheEnabled();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = ev.findPointerIndex(mActivePointerId);
                float x = ev.getX(pointerIndex);
                float dx = x - mLastMotionX;
                float xDiff = Math.abs(dx);
                float y = ev.getY(pointerIndex);
                float yDiff = Math.abs(y - mLastMotionY);

                if (dx != 0 && canScroll(this, false, (int) dx, (int) x, (int) y)) {
                    mIsUnableToDrag = true;
                    return false;
                }

                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    mIsBeingDragged = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollingCacheEnabled();
                } else if (yDiff > mTouchSlop) {
                    mIsUnableToDrag = true;
                }

                if (mIsBeingDragged) {
                    offsetScrollX(x);
                }
                break;
            }

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                int pointerIndex = MotionEventCompat.getActionIndex(ev);
                int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastMotionX = ev.getX(newPointerIndex);
                    mLastMotionY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mIsUnableToDrag = false;
                mActivePointerId = -1;
                return false;
        }
        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        if (!mTouchScrollEnable) {
            return false;
        }

        final int action = ev.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (pointerIndex < 0) return false;

                    float x = ev.getX(pointerIndex);
                    float y = ev.getY(pointerIndex);

                    final float xDiff = Math.abs(x - mLastMotionX);


                    if (xDiff != 0 &&
                            canScroll(this, false, (int) xDiff, (int) x, (int) y)) {
                        mIsUnableToDrag = true;
                        return false;
                    }

                    if (xDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionX = x;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        setScrollingCacheEnabled();
                    }
                } else {
                    offsetScrollX(ev.getX(ev.findPointerIndex(mActivePointerId)));
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastMotionX = ev.getX(newPointerIndex);
                    mLastMotionY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    startInternalTouchFly();
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                mActivePointerId = -1;
                mIsBeingDragged = false;
                break;

        }

        return true;
    }

    // 自动轮播控制器
    private void tryAddAutoBanner() {
        if (autoProgress != null) {
            autoProgress.isCancel = true;
        }

        // 不显示边界 && 子View 有足够容量
        if (!mSwitchEdge && getChildCount() >= mSwitchFixedItem) {
            autoProgress = new AnimationRun();
            postDelayed(autoProgress, (int) (mSwitchPagePeriod / 1.5f));
        }
    }

    // 自动轮播取消
    private void cancelBanner() {
        if (autoProgress != null) {
            autoProgress.isCancel = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        tryAddAutoBanner();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelBanner();
    }


    @Override
    public void setOnClickListener( OnClickListener l) {
        throw new RuntimeException("not support onClickListener");
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


    // 界面位移
    private void offsetScrollX(float x) {
        float deltaX = mLastMotionX - x;
        scrollDirection = deltaX != 0 ? deltaX : scrollDirection;
        mLastMotionX = x;
        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;
        mLastMotionX += scrollX - (int) scrollX;
        float pace = Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        if (pace <= 0) return;

        mSelectIndex -= deltaX / pace;
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
    private void startInternalTouchFly() {
        double diff = mSelectIndex - Math.rint(mSelectIndex);

        if (mSwitchAnimator != null) {
            mSwitchAnimator.cancel();
            mSwitchAnimator = null;
        }

        int left = (int) mSelectIndex + (mSelectIndex % 1 > 0 ? 1 : 0);
        int righ = (int) mSelectIndex + (mSelectIndex % 1 > 0 ? 0 : -1);

        final int mTarget;
        if (Math.abs(diff) < 0.08) {
            mTarget = (int) Math.rint(mSelectIndex);
        } else if (scrollDirection < 0) {
            mTarget = left;
        } else {
            mTarget = righ;
        }


        if (mSelectIndex - Math.rint(mTarget) == 0) return;

        final ValueAnimator current = ValueAnimator.ofFloat(mSelectIndex, mTarget);
        mSwitchAnimator = current;
        mSwitchAnimator.setDuration(Math.max(50, (int) Math.abs((mSelectIndex - mTarget) * mSwitchAnimationPeriod) / mSwitchFixedItem));
        mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mSwitchAnimator != current) return;
                mSelectIndex = (float) animation.getAnimatedValue();
                ensureTranslationOrder();
            }
        });
        mSwitchAnimator.start();
        if (autoProgress != null) {
            autoProgress.delay = true;
        }

    }

    // 自动轮播动画
    private void startInternalPageFly(float step, boolean animation) {
        if (mSwitchAnimator != null) {
            mSwitchAnimator.cancel();
            mSwitchAnimator = null;
        }
        float mTarget = (int) (Math.rint(mSelectIndex + step));
        if (animation) {
            final ValueAnimator current = ValueAnimator.ofFloat(mSelectIndex, mTarget);
            mSwitchAnimator = current;
            mSwitchAnimator.setDuration(Math.max(150, (int) Math.abs((mSelectIndex - mTarget) * mSwitchAnimationPeriod) / mSwitchFixedItem));
            mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mSwitchAnimator != current) return;
                    mSelectIndex = (float) animation.getAnimatedValue();
                    ensureTranslationOrder();
                }
            });
            mSwitchAnimator.start();
        } else {
            mSelectIndex = mTarget;
            ensureTranslationOrder();
        }
    }

    // 实现viewItem滚动总入口
    private void ensureTranslationOrder() {

        int count = getChildCount();
        float pace = Math.max(getWidth() - getPaddingLeft() - getPaddingRight(), 0) / mSwitchFixedItem;
        if (count < 2 || pace <= 0) {
            return;
        }

        if (mOnScrollListener != null) {
            float[] target = getSelectCenter();
            mOnScrollListener.onPageScrolled((int) target[0], target[1]);
        }

        int scrollWidth = (int) (Math.max(count, mSwitchFixedItem) * pace);

        for (int i = 0; i < count; i++) {
            int x = (int) ((i + mSelectIndex) * pace);
            int transX = x % scrollWidth;

            if (transX >= (int) (mSwitchFixedItem * pace)) {
                // 防止左边空白
                getChildAt(i).setTranslationX((transX - scrollWidth));
            } else if (transX <= (int) ((-pace))) {
                // 防止右边空白
                getChildAt(i).setTranslationX((transX + scrollWidth));
            } else {
                getChildAt(i).setTranslationX(transX);
            }
        }
    }

    // 刷新Adapter
    private void refreshAdapter() {
        removeAllViews();

        for (int i = 0; i < mTypeToView.size(); i++) {
            mTypeToView.valueAt(i).currentUsedPosition = 0;
        }

        int count = mListAdapter != null ? mListAdapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            Integer type = mListAdapter.getItemViewType(i);

            if (mTypeToView.indexOfKey(type) < 0) {
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
        cancelBanner();
        if (mListAdapter != null) {
            mListAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mListAdapter = adapter;
        if (adapter != null) {
            adapter.registerDataSetObserver(mDataSetObserver);
        }
        computeAdapter();
        tryAddAutoBanner();
    }


    private float[] getSelectCenter() {

        float[] ret = new float[3];

        int count = getChildCount();
        float mCurrentIndex = (mSwitchFixedItem / 2 - mSelectIndex) % count;
        mCurrentIndex = (mCurrentIndex + count) % count;

        int mCurrentCenter = (int) Math.rint(mCurrentIndex);
        float radio = mCurrentIndex - mCurrentCenter;

        mCurrentCenter = mCurrentCenter == count ? 0 : mCurrentCenter;

        ret[0] = mCurrentCenter;
        ret[1] = radio;
        ret[2] = mCurrentIndex;

        return ret;
    }

    public int getSelectPage() {
        float[] page = getSelectCenter();
        return (int) page[0];
    }


    public void setSelectPage(int index, boolean animation) {

        if (mSwitchAnimator != null) {
            mSwitchAnimator.cancel();
            mSwitchAnimator = null;
        }

        index = index % getChildCount();
        index = (index + getChildCount()) % getChildCount();
        int center = (int) getSelectCenter()[0];

        if (index == center) {
            return;
        }

        float step1 = center - index;
        float step2 = Math.max(mSwitchFixedItem, getChildCount()) + step1;
        float step3 = step1 - Math.max(mSwitchFixedItem, getChildCount());
        float step = step1;
        step = Math.abs(step) < Math.abs(step2) ? step : step2;
        step = Math.abs(step) < Math.abs(step3) ? step : step3;

        startInternalPageFly((int) Math.rint(mSelectIndex + step) - mSelectIndex, animation);


        if (autoProgress != null) {
            autoProgress.delay = true;
        }

    }


    @Override
    public boolean performClick() {
        if (mOnItemClickListener != null) {
            for (int i = 0; i < getChildCount(); i++) {
                Rect globeRect = new Rect();
                View view = getChildAt(i);
                globeRect.top = view.getTop();
                globeRect.left = view.getLeft() + (int) view.getTranslationX();
                globeRect.right = view.getRight() + (int) view.getTranslationX();
                globeRect.bottom = view.getBottom();

                if (globeRect.contains((int) mInitialMotionX, (int) mInitialMotionY)) {
                    mOnItemClickListener.onItemClicked(i, view);
                    return true;
                }
            }
        }
        return true;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        setClickable(mOnItemClickListener != null);
    }

    public void setEnableTouchScroll(boolean isEnable) {
        this.mTouchScrollEnable = isEnable;
        setSelectPage(getSelectPage(), false);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.mOnScrollListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClicked(int position, View view);
    }


}


















