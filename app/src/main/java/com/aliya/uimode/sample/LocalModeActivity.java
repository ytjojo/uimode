package com.aliya.uimode.sample;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.aliya.uimode.UiModeManager;
import com.aliya.uimode.sample.base.BaseActivity;

public class LocalModeActivity extends BaseActivity{
    ImageView ivWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_simple);


    }
}
