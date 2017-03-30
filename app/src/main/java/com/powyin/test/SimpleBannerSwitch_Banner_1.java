package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powyin.slide.widget.BannerSwitch;
import com.powyin.slide.widget.BannerUpperView;

import java.util.Random;

/**
 * Created by powyin on 2016/8/7.
 */
public class SimpleBannerSwitch_Banner_1 extends Activity {


    BannerSwitch bannerSwitch;
    BannerUpperView bannerUpperView;
    ListViewAdapter adapter;


    Random random = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_banner_switch_banner_1);
        bannerSwitch = (BannerSwitch) findViewById(R.id.my_banner);
        bannerUpperView = (BannerUpperView) findViewById(R.id.my_banner_upper_view);

//        viewPager.setAdapter(new PagerAdapter() {
//            @Override
//            public int getCount() {
//                return 5;
//            }
//
//            @Override
//            public boolean isViewFromObject(View view, Object object) {
//                return view == object;
//            }
//
//            @Override
//            public Object instantiateItem(ViewGroup container, int position) {
//
//                System.out.println(container.getChildCount()+":::::::::::::::::::::");
//
//                View view = getLayoutInflater().inflate(R.layout.fr,container,false);
//                container.addView(view);
//                TextView textView = (TextView)view.findViewById(R.id.text);
//                textView.setText(String.valueOf(random.nextInt(200000)));
//
//                return view;
//            }
//
//            @Override
//            public void destroyItem(ViewGroup container, int position, Object object) {
//
//                container.removeView((View)object);
//
//            }
//        });

        bannerSwitch.setOnItemClickListener(new BannerSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
            //    System.out.println("------------------------>>" + position);
            }
        });

        bannerSwitch.setOnButtonLineScrollListener(new BannerSwitch.OnButtonLineScrollListener() {
            @Override
            public void onButtonLineScroll(int viewCount, int leftIndex, int rightIndex, View leftView, View rightView, float leftNearWei, float rightNearWei) {
             //   System.out.println(":::" + leftIndex + "::" + rightIndex + ":::" + leftNearWei + ":::" + rightNearWei);
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
