package com.powyin.test;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by powyin on 2016/8/8.
 */


class ListViewAdapter extends BaseAdapter {

    private Activity mActivity;
    private int mCount = 10;

    public ListViewAdapter(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.adapter_test_item, parent, false);
        }
        TextView info = (TextView) convertView.findViewById(R.id.my_text);
        info.setText("postion : " + position);
        return convertView;
    }

    public void addItem() {
        mCount++;
        notifyDataSetChanged();
    }

    public void reMoveItem() {
        if (mCount > 0) {
            mCount--;
            notifyDataSetChanged();
        }
    }


}


