package com.powyin.test;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.powyin.test.wt.FragmentImage;

/**
 * Created by powyin on 2016/8/3.
 */
public class PageAdapter extends FragmentPagerAdapter {

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentImage.getNewInstance(position);

    }

    @Override
    public int getCount() {
        return 6;
    }
}
