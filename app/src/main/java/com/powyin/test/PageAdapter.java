package com.powyin.test;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.powyin.test.wt.FragmentImage;

/**
 * Created by powyin on 2016/8/3.
 */
public class PageAdapter extends FragmentPagerAdapter {
    private int count;

    public PageAdapter(FragmentManager fm, int count) {
        super(fm);
        this.count = count;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentImage.getNewInstance(position);

    }

    @Override
    public int getCount() {
        return count;
    }
}
