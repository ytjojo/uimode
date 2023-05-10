package com.aliya.uimode.utils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.aliya.uimode.HideLog;
import com.aliya.uimode.ResourcesFlusher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by leonhover on 16-9-26.
 */

public class AppResourceUtils {

    public static final boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean IS_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    public static final boolean IS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final boolean IS_M = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private static TypedValue sTypedValue;


    public static TypedValue getTypedValue() {
        if (sTypedValue == null) {
            sTypedValue = new TypedValue();
        }
        return sTypedValue;
    }

    /**
     * 判断是否是以“?attr/**”引用的资源
     *
     * @param attrValue AttributeValue
     * @return true or false
     */
    public static boolean isAttrReference(String attrValue) {
        if (!TextUtils.isEmpty(attrValue) && attrValue.startsWith("?")) {
            return true;
        }

        return false;
    }

    /**
     * 获取Attr的资源ID
     *
     * @param attrValue AttributeValue
     * @return resid
     */
    public static int getAttrResId(String attrValue) {
        if (TextUtils.isEmpty(attrValue)) {
            return -1;
        }

        String resIdStr = attrValue.substring(1);
        return Integer.valueOf(resIdStr);
    }

    /**
     * 获取attrResId指向的颜色Color
     *
     * @param context
     * @param attrResId attr资源id
     * @return Color
     */
    public static int getColorWithAttr(Context context, @AttrRes int attrResId) {
        if (context == null) {
            return -1;
        }

        TypedValue typedValue = getTypedValue();
        context.getTheme().resolveAttribute(attrResId, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
    }


    /**
     * 获取colorResId指向的颜色Color
     *
     * @param context
     * @param colorResId 颜色资源id
     * @return Color
     */
    public static int getColorWithResId(Context context, @ColorRes int colorResId) {
        if (context == null) {
            return -1;
        }
        return ContextCompat.getColor(context, colorResId);
    }


    /**
     * 获取attrResId指向的颜色ColorStateList
     *
     * @param context
     * @param attrResId attr资源id
     * @return ColorStateList
     */
    public static ColorStateList getColorStateListWithAttr(Context context, @AttrRes int attrResId) {
        if (context == null) {
            return null;
        }
        TypedValue typedValue = getTypedValue();
        context.getTheme().resolveAttribute(attrResId, typedValue, true);

        HideLog.d("ThemeUtils", "ColorStateList type:" + typedValue.toString());
        return ContextCompat.getColorStateList(context, typedValue.resourceId);
    }


    /**
     * 获取ColorStateListResId指向的颜色ColorStateList
     *
     * @param context
     * @param colorStateListResId 资源id
     * @return ColorStateList
     */
    public static ColorStateList getColorStateListWithResId(Context context, @ColorRes int colorStateListResId) {
        if (context == null) {
            return null;
        }
        return ContextCompat.getColorStateList(context, colorStateListResId);
    }


    /**
     * 获取attrResId指向的Drawable
     *
     * @param context
     * @param attrResId attr资源id
     * @return Drawable
     */
    public static Drawable getDrawableWithAttr(Context context, @AttrRes int attrResId) {
        if (context == null) {
            return null;
        }
        TypedValue typedValue = getTypedValue();
        context.getTheme().resolveAttribute(attrResId, typedValue, true);
        ;
        HideLog.d("getDrawable", "drawable type:" + typedValue.toString());

        Drawable drawable = DrawableCompatUtil.getDrawable(context, typedValue.resourceId);
        return drawable;

    }


    /**
     * 获取DrawableResId指向的Drawable
     *
     * @param context
     * @param drawableResId Drawable资源id
     * @return Drawable
     */
    public static Drawable getDrawableWithResId(Context context, @DrawableRes int drawableResId) {
        if (context == null) {
            return null;
        }

        return DrawableCompatUtil.getDrawable(context, drawableResId);
    }


    public static <T> T getViewTag(View view, @IdRes int tagKey) throws ClassCastException, NullPointerException {
        try {
            return (T) view.getTag(tagKey);
        } catch (ClassCastException e) {
            throw e;
        } catch (NullPointerException e) {
            throw e;
        }
    }

    /**
     * 获取StatusBar高度
     *
     * @param context
     * @return
     */
    @SuppressLint("PrivateApi")
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static Object invokeMethod(Object obj, String method, Object... parameters) {
        if (obj == null || TextUtils.isEmpty(method)) {
            return null;
        }

        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }

        try {
            Method methodWanted = obj.getClass().getDeclaredMethod(method, parameterTypes);
            methodWanted.setAccessible(true);
            return methodWanted.invoke(obj, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断是否是暗黑模式开启状态
     *
     * @param context Context
     * @return true or false
     */
    public static boolean isSystemDarkMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean result = mode == Configuration.UI_MODE_NIGHT_YES;
        HideLog.d(HideLog.TAG, "isSystemDarkMode result:" + result);
        return result;
    }

    /**
     * 夜间模式切换时是否执行了 {@link Activity#recreate()}
     *
     * @param context .
     * @return true : 表示执行了
     */
    public static boolean isRecreateOnUiModeChange(Context context) {
        if (context instanceof Activity) {
            final PackageManager pm = context.getPackageManager();
            try {
                final ActivityInfo info = pm.getActivityInfo(
                        new ComponentName(context, context.getClass()), 0);
                // We should return true (to recreate) if configChanges does not want to handle
                // uiMode
                return (info.configChanges & ActivityInfo.CONFIG_UI_MODE) == 0;
            } catch (PackageManager.NameNotFoundException e) {
                return true;
            }
        }
        return false;
    }


    public static int getManifestActivityTheme(Activity activity) {
        try {
            return activity.getPackageManager().getActivityInfo(new ComponentName(activity,
                    activity.getClass()), PackageManager.MATCH_DEFAULT_ONLY).theme;
        } catch (Exception e) {
            // no-op
        }
        return 0;
    }

    public static int getManifestApplicationTheme(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_SHARED_LIBRARY_FILES).theme;
        } catch (Exception e) {
            // no-op
        }
        return 0;
    }

    /**
     * “android"
     *
     * @param context
     * @param identifier
     * @param type
     * @return
     */
    public static int getIdentifier(Context context, String identifier, String type) {

        return context.getResources().getIdentifier(identifier, "drawable", context.getPackageName());
    }

    public static int getAttrId(Context context, String identifier) {
        int id = context.getResources().getIdentifier(identifier, "attr", context.getPackageName());
        if (id != 0) {
            return id;
        }
        return context.getResources().getIdentifier(identifier, "attr", "android");
    }

    /**
     * update UiMode
     *
     * @param mode    ui mode type
     * @param context context
     * @return true : 刷新成功
     * @see androidx.appcompat.app.AppCompatDelegateImpl#updateForNightMode(int, boolean)
     */
    public static boolean updateUiModeForApplication(Context context,
                                                     @AppCompatDelegate.NightMode int mode) {
        final Resources res = context.getApplicationContext().getResources();
        final Configuration conf = res.getConfiguration();
        final int currentNightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        final int newNightMode;
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                newNightMode = Configuration.UI_MODE_NIGHT_YES;
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                newNightMode = Configuration.UI_MODE_NIGHT_NO;
                break;
            default:
                newNightMode = Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                break;
        }

        if (currentNightMode != newNightMode) {
            final Configuration config = new Configuration(conf);
            final DisplayMetrics metrics = res.getDisplayMetrics();

            // Update the UI Mode to reflect the new night mode
            config.uiMode = newNightMode | (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
            res.updateConfiguration(config, metrics);

            // We may need to flush the Resources' drawable cache due to framework bugs..
            ResourcesFlusher.flush(res);
        }

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            ResourcesFlusher.flush(sAppContext.getResources());
//        }
        return currentNightMode != newNightMode;
    }


    @AppCompatDelegate.NightMode
    public static int calculateNightMode(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            return appCompatActivity.getDelegate().getLocalNightMode() != AppCompatDelegate.MODE_NIGHT_UNSPECIFIED ? appCompatActivity.getDelegate().getLocalNightMode() : AppCompatDelegate.getDefaultNightMode();
        }
        return AppCompatDelegate.getDefaultNightMode();
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
     * 纠正 {@link Configuration#uiMode} 的值.
     * 在xml中遇到WeView时会被改成 {@link Configuration#UI_MODE_NIGHT_NO}, 导致后续View出现问题.
     *
     * @param context .
     */
    public static void correctConfigUiMode(Context context) {
        /**
         * 参考自 {@link androidx.appcompat.app.AppCompatDelegateImplV14#updateForNightMode(int)}
         */
        final Resources res = context.getResources();
        final Configuration conf = res.getConfiguration();
        final int uiMode = (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_YES)
                ? Configuration.UI_MODE_NIGHT_YES
                : Configuration.UI_MODE_NIGHT_NO;
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
