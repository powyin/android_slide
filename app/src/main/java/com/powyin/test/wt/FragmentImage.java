package com.powyin.test.wt;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.powyin.test.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by powyin on 2016/8/3.
 */
public class FragmentImage extends Fragment {

    View view;

    public static FragmentImage getNewInstance(int postion){
        Bundle bundle = new Bundle();
        bundle.putInt("key",postion);
        FragmentImage image = new FragmentImage();
        image.setArguments(bundle);
        return image;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_new, container, false);
        ((TextView)view.findViewById(R.id.text)).setText(String.valueOf(getArguments().getInt("key")));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }





















}
