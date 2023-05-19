package com.aliya.uimode.sample.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.aliya.uimode.UiModeManager;
import com.aliya.uimode.core.UiModeChangeListener;
import com.aliya.uimode.sample.R;
import com.aliya.uimode.sample.base.BaseActivity;
import com.aliya.uimode.utils.AppUtil;

public class LocalTopBar extends FrameLayout implements View.OnClickListener, UiModeChangeListener {

    TextView mTvTitle;
    TextView mBtnSwitch;

    TextView mBtnShowLocal;

    View mLocalView;

    public LocalTopBar(@NonNull Context context) {
        this(context, null);
    }

    public LocalTopBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LocalTopBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_top_bar, this, true);
        init();
    }

    private void init() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mBtnSwitch = (TextView) findViewById(R.id.btn_switch);
        mLocalView = (View) findViewById(R.id.select_local);
        mBtnShowLocal = findViewById(R.id.btn_home);
        mBtnShowLocal.setText("直接选择");

        Context context = AppUtil.findActivity(getContext());
        mTvTitle.setText("localUimode");
        mBtnSwitch.setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_home).setOnClickListener(this);
        findViewById(R.id.btn_day).setOnClickListener(this);
        findViewById(R.id.btn_night).setOnClickListener(this);
        findViewById(R.id.btn_cancel_local).setOnClickListener(this);
        BaseActivity activity = (BaseActivity) AppUtil.findActivity(getContext());
        UiModeManager.INSTANCE.setLocalNightMode(activity, AppCompatDelegate.MODE_NIGHT_NO);
        bindModeView();
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        BaseActivity activity = (BaseActivity) AppUtil.findActivity(getContext());
        switch (v.getId()) {
            case R.id.btn_switch:
                switchUiMode();
                break;
            case R.id.btn_back:
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
                break;
            case R.id.btn_home:
                mLocalView.setVisibility(View.VISIBLE);
                mBtnSwitch.setVisibility(View.GONE);
                mBtnShowLocal.setVisibility(View.GONE);
                break;
            case R.id.btn_day:

                int nextUiMode = AppCompatDelegate.MODE_NIGHT_NO;
                UiModeManager.INSTANCE.setLocalNightMode(activity, nextUiMode);
                bindModeView();
                break;
            case R.id.btn_night:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_YES;
                UiModeManager.INSTANCE.setLocalNightMode(activity, nextUiMode);
                bindModeView();
                break;
            case R.id.btn_cancel_local:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED;
                UiModeManager.INSTANCE.setLocalNightMode(activity, nextUiMode);
                bindModeView();
                mLocalView.setVisibility(View.GONE);
                mBtnSwitch.setVisibility(View.VISIBLE);
                mBtnShowLocal.setVisibility(View.VISIBLE);
                break;

        }
    }


    private void switchUiMode() {
        BaseActivity activity = (BaseActivity) AppUtil.findActivity(getContext());
        int nextUiMode;
        switch (activity.getDelegate().getLocalNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED;
                break;
            default:
                nextUiMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
        }
        UiModeManager.INSTANCE.setLocalNightMode(activity, nextUiMode);
        bindModeView();
    }

    private void bindModeView() {
        BaseActivity activity = (BaseActivity) AppUtil.findActivity(getContext());
        switch (activity.getDelegate().getLocalNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                mBtnSwitch.setText("白间");
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                mBtnSwitch.setText("黑暗");
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                mBtnSwitch.setText("跟随系统");
                break;
            default:
                mBtnSwitch.setText("统一设置");
                break;
        }
    }

    @Override
    public void onUiModeChange() {
        bindModeView();
    }
}