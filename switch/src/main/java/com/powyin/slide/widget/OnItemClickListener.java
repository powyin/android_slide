package com.powyin.slide.widget;

import android.view.View;

/**
 * Created by powyin on 2017/8/5.
 */

public interface OnItemClickListener {
    void onItemClicked(View select, int position, View... unSelect);
}
