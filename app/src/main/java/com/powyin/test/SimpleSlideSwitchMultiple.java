package com.powyin.test;

import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.powyin.slide.widget.OnItemClickListener;
import com.powyin.slide.widget.OnScrollListener;
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

        slideSwitch.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, View... unselect) {
                viewPager.setCurrentItem(position);
            }

        });

        slideSwitch.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, View select, View... unSelect) {
//                if (leftView != null) {
//                    if (leftView instanceof TextView) {
//                        TextView tem = (TextView) leftView;
//                        tem.setTextColor(ColorUtil.calculationColor(0xff656565, 0xff009dff, leftNearWei));
//                        tem.setTextSize(14 * (7 + leftNearWei) / 7);
//                    }
//
//                }
//                if (rightView != null) {
//                    if (rightView instanceof TextView) {
//                        TextView tem = (TextView) rightView;
//                        tem.setTextColor(ColorUtil.calculationColor(0xff656565, 0xff009dff, rightNearWei));
//                        tem.setTextSize(14 * (7 + rightNearWei) / 7);
//                    }
//                }
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
