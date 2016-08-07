package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.powyin.slide.widget.BannerSwitch;

/**
 * Created by powyin on 2016/8/7.
 */
public class SimpleBannerSwitch_Banner_1 extends Activity {

    BannerSwitch bannerSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_banner_switch_banner_1);
        bannerSwitch = (BannerSwitch)findViewById(R.id.my_banner);

        bannerSwitch.setOnItemClickListener(new BannerSwitch.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
                System.out.println("------------------------>>"+position);
            }
        });

    }


    public void onClick(View view){

    }
}
