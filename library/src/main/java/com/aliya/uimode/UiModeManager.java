package com.aliya.uimode;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.aliya.uimode.apply.ApplyAlpha;
import com.aliya.uimode.apply.ApplyBackground;
import com.aliya.uimode.apply.ApplyDivider;
import com.aliya.uimode.apply.ApplyForeground;
import com.aliya.uimode.apply.ApplyNavIcon;
import com.aliya.uimode.apply.ApplySrc;
import com.aliya.uimode.apply.ApplyTextColor;
import com.aliya.uimode.factory.UiModeInflaterFactory;
import com.aliya.uimode.intef.ApplyPolicy;
import com.aliya.uimode.intef.InflaterSupport;
import com.aliya.uimode.intef.UiApply;
import com.aliya.uimode.mode.UiMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * UiMode管理类
 *
 * @author a_liYa
 * @date 2017/6/23 10:51.
 */
public class UiModeManager implements ApplyPolicy, InflaterSupport {

    private static volatile UiModeManager sInstance;

    private static Context sContext;

    private static Set<Integer> sSupportAttrIds;
    private static Map<String, UiApply> sSupportApplies = new HashMap<>();
    public static String NAME_ATTR_MASK_COLOR;

    static {
        sSupportApplies.put("background", new ApplyBackground());
        sSupportApplies.put("foreground", new ApplyForeground());
        sSupportApplies.put("alpha", new ApplyAlpha());
        sSupportApplies.put("textColor", new ApplyTextColor());
        sSupportApplies.put("divider", new ApplyDivider());
        sSupportApplies.put("src", new ApplySrc());
        sSupportApplies.put("navigationIcon", new ApplyNavIcon());
    }

    private UiModeManager() {
    }

    public static UiModeManager get() {
        if (sInstance == null) {
            synchronized (UiModeManager.class) {
                if (sInstance == null) {
                    sInstance = new UiModeManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化： 持有ApplicationContext引用，保存支持的Attr
     *
     * @param context Context
     * @param attrs   支持夜间模式的属性数组
     */
    public static final void init(Context context, int[] attrs) {

        sContext = context.getApplicationContext();

        addSupportAttrIds(attrs);

        if (TextUtils.isEmpty(NAME_ATTR_MASK_COLOR)) {
            NAME_ATTR_MASK_COLOR = context.getResources().getResourceEntryName(R.attr.iv_maskColor);
        }

        if (sContext instanceof Application) {

            ((Application) sContext).unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
            ((Application) sContext).registerActivityLifecycleCallbacks(lifecycleCallbacks);

            LayoutInflater inflater = LayoutInflater.from(sContext);
            if (LayoutInflaterCompat.getFactory(inflater) == null) {
                LayoutInflaterCompat.setFactory(inflater, obtainInflaterFactory());
            }
        }

    }

    public static void setTheme(int resId) {
        // 设置所有Activity主题
        Stack<Activity> appStack = AppStack.getAppStack();
        if (appStack != null) {
            Iterator<Activity> iterator = appStack.iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                if (next != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (next.isDestroyed()) continue;
                    }
                    next.setTheme(resId);
                }
            }
        }

        // 执行View到对应主题
        UiMode.applyUiMode(resId, get());

    }

    private static final Application.ActivityLifecycleCallbacks lifecycleCallbacks =
            new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    AppStack.pushActivity(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    AppStack.removeActivity(activity);
                    UiMode.removeUselessViews(activity);
                }
            };


    @Override
    public UiApply obtainApplyPolicy(String key) {
        return sSupportApplies.get(key);
    }

    @Override
    public boolean isSupportApply(String key) {
        return sSupportApplies.containsKey(key);
    }

    @Override
    public boolean isSupportAttrId(Integer attrId) {
        return sSupportAttrIds != null && sSupportAttrIds.contains(attrId);
    }

    public static void addSupportAttrIds(int[] attrs) {
        if (attrs == null) return;
        // 不存在，创建
        if (sSupportAttrIds == null) {
            synchronized (UiModeManager.class) {
                if (sSupportAttrIds == null)
                    // 创建时指定初始容量，更节省内存
                    sSupportAttrIds = new HashSet<>(attrs.length);
            }
        }
        // 添加全部
        for (int attrId : attrs) {
            sSupportAttrIds.add(attrId);
        }
    }

    public static LayoutInflaterFactory obtainInflaterFactory() {
        return UiModeInflaterFactory.get(get());
    }

}