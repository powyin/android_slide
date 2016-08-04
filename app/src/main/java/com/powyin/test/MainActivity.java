package com.powyin.test;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.powyin.slide.widget.SlideSwitch;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.click_to_single:
                startActivity(new Intent(this,SimpleSlideSwitch.class));
                break;
            case R.id.click_to_multiple:
                startActivity(new Intent(this,SimpleSlideSwitchMultiple.class));
                break;
        }


    }

}
