package com.powyin.slide.widget;

import android.view.View;

/**
 * Created by powyin on 2017/8/5.
 */

public interface OnScrollListener {
    void onPageScrolled(int position, float positionOffset, View select, View... unSelect);
}
