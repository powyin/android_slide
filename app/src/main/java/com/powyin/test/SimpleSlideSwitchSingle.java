package com.powyin.test;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.powyin.slide.tool.ColorUtil;
import com.powyin.slide.widget.OnItemClickListener;
import com.powyin.slide.widget.SlideSwitch;

/**
 * Created by powyin on 2016/8/4.
 */
public class SimpleSlideSwitchSingle extends AppCompatActivity {

    SlideSwitch slideSwitch;
    ViewPager viewPager;
    ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_simple_slide_switch);
        slideSwitch = (SlideSwitch) findViewById(R.id.test_switch);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager(), 4));

        viewPager.addOnPageChangeListener(slideSwitch.getSupportOnPageChangeListener());

        slideSwitch.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, View... unselect) {
                viewPager.setCurrentItem(position);
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click_reset_adapter:
                adapter = new ListViewAdapter(this);
                slideSwitch.setAdapter(adapter);
                break;
            case R.id.click_add_item:
                if (adapter != null) {
                    adapter.addItem();
                }
                break;
            case R.id.click_remove_item:
                if (adapter != null) {
                    adapter.reMoveItem();
                }
                break;
        }

    }


}
