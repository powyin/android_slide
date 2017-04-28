package com.powyin.test;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.powyin.slide.tool.ColorUtil;
import com.powyin.slide.widget.SlideSwitch;

/**
 * Created by powyin on 2016/8/4.
 */
public class SimpleSlideSwitchMultiple extends AppCompatActivity {

    SlideSwitch slideSwitch;
    ViewPager viewPager;
    ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_simple_slide_switch_multiple);

        slideSwitch = (SlideSwitch) findViewById(R.id.test_switch);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager(), 10));

        viewPager.addOnPageChangeListener(slideSwitch.getSupportOnPageChangeListener());

        slideSwitch.setOnItemClickListener(new SlideSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                viewPager.setCurrentItem(position);
            }

        });

        slideSwitch.setOnButtonLineScrollListener(new SlideSwitch.OnButtonLineScrollListener() {
            @Override
            public void onButtonLineScroll(int viewCount, int leftIndex, int rightIndex, View leftView, View rightView, float leftNearWei, float rightNearWei) {
                if (leftView != null) {
                    if (leftView instanceof TextView) {
                        TextView tem = (TextView) leftView;
                        tem.setTextColor(ColorUtil.calculationColor(0xff656565, 0xff009dff, leftNearWei));
                        tem.setTextSize(14 * (7 + leftNearWei) / 7);
                    }

                }
                if (rightView != null) {
                    if (rightView instanceof TextView) {
                        TextView tem = (TextView) rightView;
                        tem.setTextColor(ColorUtil.calculationColor(0xff656565, 0xff009dff, rightNearWei));
                        tem.setTextSize(14 * (7 + rightNearWei) / 7);
                    }
                }
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
