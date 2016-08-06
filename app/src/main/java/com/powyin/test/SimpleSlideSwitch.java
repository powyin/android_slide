package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.powyin.slide.widget.SlideSwitch;

import java.lang.reflect.AccessibleObject;

/**
 * Created by powyin on 2016/8/4.
 */
public class SimpleSlideSwitch extends AppCompatActivity {

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

        slideSwitch.setOnPageChangeListener(new SlideSwitch.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScroll(float mScroll) {
            //    System.out.println("-------zzz"+mScroll);
            }
        });



    }

    public void onClick(View view){

    }


}
