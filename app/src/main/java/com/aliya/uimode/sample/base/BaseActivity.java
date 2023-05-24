package com.aliya.uimode.sample.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.aliya.uimode.UiModeManager;
import com.aliya.uimode.core.FactoryMerger;
import com.aliya.uimode.core.UiModeChangeListener;
import com.aliya.uimode.sample.AppUiMode;
import com.aliya.uimode.utils.AppResourceUtils;
import com.noober.background.BackgroundFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.LayoutInflaterCompat;

/**
 * Base Activity
 *
 * @author a_liYa
 * @date 2018/2/1 15:05.
 */
public class BaseActivity extends AppCompatActivity implements UiModeChangeListener {

    private int nightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        LayoutInflater.Factory2 before = UiModeManager.INSTANCE.obtainInflaterFactory();
        LayoutInflater.Factory2 after = new BackgroundFactory(); // 赋值自己的Factory
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new FactoryMerger(before, after));
        super.onCreate(savedInstanceState);
        nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        super.onConfigurationChanged(newConfig);
        final int newNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (AppUiMode.getUiMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM && nightMode != newNightMode) {
//            UiModeManager.applyUiModeViews(this);
            nightMode = newNightMode;
        }
        Toast.makeText(this,"系统uiMode = " + UiModeManager.INSTANCE.getSystemUiMode() + "当前uiMode = " + AppResourceUtils.calculateNightMode(this),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUiModeChange() {
        //
    }

}
