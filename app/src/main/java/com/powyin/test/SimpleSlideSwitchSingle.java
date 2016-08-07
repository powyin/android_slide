package com.powyin.test;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.powyin.slide.widget.SlideSwitch;

/**
 * Created by powyin on 2016/8/4.
 */
public class SimpleSlideSwitchSingle extends AppCompatActivity {

    SlideSwitch slideSwitch;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_simple_slide_switch);
        slideSwitch = (SlideSwitch)findViewById(R.id.test_switch);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));

        viewPager.addOnPageChangeListener(slideSwitch.getSupportOnPageChangeListener());
        viewPager.setOffscreenPageLimit(1);

        slideSwitch.setOnItemClickListener(new SlideSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                viewPager.setCurrentItem(position);
            }
        });

        slideSwitch.setOnButtonLineScrollListener(new SlideSwitch.OnButtonLineScrollListener() {
            @Override
            public void onButtonLineScroll(float mScroll , int viewCount) {

            }
        });


    }

    public void onClick(View view){

    }


}
