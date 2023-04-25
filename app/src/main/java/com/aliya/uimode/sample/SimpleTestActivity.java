package com.aliya.uimode.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aliya.uimode.sample.base.BaseActivity;

public class SimpleTestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_test);
    }
}
