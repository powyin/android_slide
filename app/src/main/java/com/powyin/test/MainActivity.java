package com.powyin.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click_to_slide_switch_single:
                startActivity(new Intent(this, SimpleSlideSwitchSingle.class));
                break;
            case R.id.click_to_slide_switch_multiple:
                startActivity(new Intent(this, SimpleSlideSwitchMultiple.class));
                break;
            case R.id.click_to_banner_1:
                startActivity(new Intent(this, SimpleBannerSwitch_Banner_1.class));
                break;
            case R.id.click_to_banner_3:
                startActivity(new Intent(this, SimpleBannerSwitch_Banner_3.class));
                break;
            case R.id.click_to_banner_no:
                startActivity(new Intent(this, SimpleBannerSwitch_No_Auto.class));
                break;
            case R.id.click_to_pow_switch:
                startActivity(new Intent(this, SimplePowSwitch.class));
                break;
        }


    }

}
