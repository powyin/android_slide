package com.powyin.test.wt;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.powyin.test.R;

/**
 * Created by powyin on 2016/8/3.
 */
public class FragmentImage extends Fragment {

    View view;

    public static FragmentImage getNewInstance(int postion) {
        Bundle bundle = new Bundle();
        bundle.putInt("key", postion);
        FragmentImage image = new FragmentImage();
        image.setArguments(bundle);
        return image;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_new, container, false);
        ((TextView) view.findViewById(R.id.text)).setText(String.valueOf(getArguments().getInt("key")));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


}
