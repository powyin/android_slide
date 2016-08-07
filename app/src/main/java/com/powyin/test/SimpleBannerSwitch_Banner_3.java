package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.powyin.slide.widget.BannerSwitch;
import com.powyin.slide.widget.BannerUpperView;

/**
 * Created by powyin on 2016/8/7.
 */
public class SimpleBannerSwitch_Banner_3 extends Activity{


    BannerSwitch bannerSwitch;
    BannerUpperView bannerUpperView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_banner_switch_banner_3);
        bannerSwitch = (BannerSwitch)findViewById(R.id.my_banner);
        bannerUpperView = (BannerUpperView)findViewById(R.id.my_banner_upper_view);

        bannerSwitch.setOnItemClickListener(new BannerSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
                System.out.println("------------------------>>"+position);
            }
        });

        bannerSwitch.setOnButtonLineScrollListener(new BannerSwitch.OnButtonLineScrollListener() {
            @Override
            public void onButtonLineScroll(float mScroll, int viewCount) {
                bannerUpperView.setScroll(mScroll,viewCount);
                System.out.println("-------------:::"+mScroll);
            }
        });


    }


    public void onClick(View view){

    }
}
