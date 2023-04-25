package com.aliya.uimode.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.aliya.uimode.UiModeManager;
import com.aliya.uimode.intef.UiApply;
import com.aliya.uimode.mode.Attr;
import com.aliya.uimode.mode.ResourceEntry;
import com.aliya.uimode.mode.UiMode;

import java.util.Map;

/**
 * 帮助解析资源保存资源 - 工具类
 *
 * @author a_liYa
 * @date 2018/12/17 21:44.
 */
public class UiModes {


    /**
     * 应用属性，并加入到UiMode列表
     *
     * @param v        a view
     * @param attrName attr name {@link Attr}
     * @param resId    resource reference, such as R.color.x or R.mipmap.x or R.drawable.x
     *                 or R.string.x or R.attr.x or R.style.x or R.dimen.x, etc.
     */
    public static void applySave(View v, String attrName, @AnyRes int resId) {
        if (v == null) return;

        UiApply uiApply = UiModeManager.get().obtainApplyPolicy(attrName);
        if (uiApply != null) {
            ResourceEntry entry = new ResourceEntry(resId, v.getContext());
            if (uiApply.isSupportType(entry.getType())) {
                if (uiApply.onApply(v, entry)) {
                    UiMode.saveViewAndAttrs(
                            v.getContext(), v, Attr.builder().add(attrName, entry).build());
                }
            }
        }
    }

    /**
     * 供外部使用，添加 通过new创建具有UiMode属性的View
     *
     * @param v     a view
     * @param attrs 建议通过 {@link Attr#builder()} 创建
     */
    public static void saveViewUiMode(View v, Map<String, ResourceEntry> attrs) {
        if (v == null) return;
        UiMode.saveViewAndAttrs(v.getContext(), v, attrs);
    }

    /**
     * 纠正 {@link Configuration#uiMode} 的值.
     * 在xml中遇到WeView时会被改成 {@link Configuration#UI_MODE_NIGHT_NO}, 导致后续View出现问题.
     *
     * @param context .
     */
    public static void correctConfigUiMode(Context context, Activity activity) {
        /**
         * 参考自 {@link androidx.appcompat.app.AppCompatDelegateImpl#updateForNightMode()}
         */
        final Resources res = context.getResources();
        final Configuration conf = res.getConfiguration();
        int uiMode;
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                uiMode = Configuration.UI_MODE_NIGHT_YES;
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                uiMode = Configuration.UI_MODE_NIGHT_NO;
                break;
            default:
                uiMode = Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                break;
        }

        final Resources applicationRes = context.getApplicationContext().getResources();
        final Configuration applicationConfiguration = applicationRes.getConfiguration();
        if ((applicationConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK) != uiMode) {
            final Configuration config = new Configuration(applicationConfiguration);
            final DisplayMetrics metrics = applicationRes.getDisplayMetrics();

            // Update the UI Mode to reflect the new night mode
            applicationConfiguration.uiMode = uiMode | (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
            applicationRes.updateConfiguration(config, metrics);
        }

        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            switch (appCompatActivity.getDelegate().getLocalNightMode()) {
                case AppCompatDelegate.MODE_NIGHT_YES:
                    uiMode = Configuration.UI_MODE_NIGHT_YES;
                    break;
                case AppCompatDelegate.MODE_NIGHT_NO:
                    uiMode = Configuration.UI_MODE_NIGHT_NO;
                    break;
                default:
                    break;
            }
        }
        if ((conf.uiMode & Configuration.UI_MODE_NIGHT_MASK) != uiMode) {
            final Configuration config = new Configuration(conf);
            final DisplayMetrics metrics = res.getDisplayMetrics();

            // Update the UI Mode to reflect the new night mode
            config.uiMode = uiMode | (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
            res.updateConfiguration(config, metrics);
        }
    }

    /**
     * 判断当前 context 是否为夜间模式.
     *
     * @param context The current context.
     * @return true : 表示为夜间模式.
     */
    public static boolean isUiModeNight(Context context) {
        final Configuration config = context.getResources().getConfiguration();
        final int currentUiMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return Configuration.UI_MODE_NIGHT_YES == currentUiMode;
    }

}
