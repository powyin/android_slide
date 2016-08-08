package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.powyin.slide.widget.BannerSwitch;
import com.powyin.slide.widget.BannerUpperView;

/**
 * Created by powyin on 2016/8/7.
 */
public class SimpleBannerSwitch_Banner_3 extends Activity {


    BannerSwitch bannerSwitch;
    BannerUpperView bannerUpperView;

    ListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_banner_switch_banner_3);
        bannerSwitch = (BannerSwitch) findViewById(R.id.my_banner);
        bannerUpperView = (BannerUpperView) findViewById(R.id.my_banner_upper_view);

        bannerSwitch.setOnItemClickListener(new BannerSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
                System.out.println("------------------------>>" + position);
            }
        });

        bannerSwitch.setOnButtonLineScrollListener(new BannerSwitch.OnButtonLineScrollListener() {
            @Override
            public void onButtonLineScroll(int viewCount, int leftIndex, int rightIndex, View leftView, View rightView, float leftNearWei, float rightNearWei) {
                bannerUpperView.onButtonLineScroll(viewCount, leftIndex, rightIndex, leftView, rightView, leftNearWei, rightNearWei);
            }
        });


    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click_reset_adapter:
                adapter = new ListViewAdapter(this);
                bannerSwitch.setAdapter(adapter);
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



