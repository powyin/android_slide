package com.powyin.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.powyin.slide.widget.PowSwitch;

/**
 * Created by powyin on 2016/8/5.
 */
public class SimplePowSwitch extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_pow_switch);


        PowSwitch powSwitch = (PowSwitch) findViewById(R.id.pow);

        powSwitch.setOpen(true);

    }

    public void onClick(View view) {

    }
}
