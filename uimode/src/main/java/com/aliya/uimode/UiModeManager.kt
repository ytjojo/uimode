package com.aliya.uimode

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.LayoutInflater.Factory2
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.core.view.LayoutInflaterCompat
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.core.FactoryMerger
import com.aliya.uimode.core.UiModeChangeListener
import com.aliya.uimode.core.UiModeDelegate
import com.aliya.uimode.core.UiModeDelegate.onUiModeChanged
import com.aliya.uimode.core.UiModeInflaterFactory
import com.aliya.uimode.core.ViewStore
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.LayoutInflaterHelper
import java.lang.ref.WeakReference

/**
 * UiMode管理类
 *
 * @author a_liYa
 * @date 2017/6/23 10:51.
 */
object UiModeManager {

    private const val TAG = "UiMode"
    private var sAppContext: Context? = null
    private var sFactory2: Factory2? = null
    private var mIgnoreActivitySet = HashSet<Class<out Activity>>()


    @ConfigAbleNightMode
    var appUiMode = AppCompatDelegate.MODE_NIGHT_NO

    @NightMode
    var systemUiMode = AppCompatDelegate.MODE_NIGHT_NO

    /**
     * 是否全局支持ImageView夜间遮罩
     * app内必须定义颜色maskColor
     */
    var isSupportImageViewMask = true

    /**
     * 是否全局支持TextView的Drawable夜间遮罩
     * 与drawableTint互斥
     * 与drawable_colorFilter互斥
     */
    var isSupportTextViewDrawableMask = false

    var appConfigurationUiMode = Configuration.UI_MODE_NIGHT_NO

    /**
     * 添加忽略日夜间切换的activity
     */
    fun addIgnoreActivity(clazz: Class<out Activity>) {
        mIgnoreActivitySet.add(clazz)
    }

    /**
     * 移除忽略日夜间切换的activity
     */
    fun removeIgnoreActivity(clazz: Class<out Activity>) {
        mIgnoreActivitySet.remove(clazz)
    }

    /**
     * 判断是否忽略日夜间切换的activity
     */
    fun isContainsIgnoreActivity(clazz: Class<out Activity>): Boolean {
        for (item in mIgnoreActivitySet) {
            if (item.equals(clazz)) {
                return true
            }
            if (item.isAssignableFrom(clazz)) {
                return true
            }
        }
        return false
    }

    private val defaultFactory by lazy {
        FactoryMerger(ArrayList(), UiModeInflaterFactory(sFactory2))
    }

    private val lifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                AppStack.pushActivity(activity)
                if (isEnableAutoInject) {
                    setFactory2(activity.layoutInflater)
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                AppStack.removeActivity(activity)
                ViewStore.removeUselessViews(activity)
            }
        }


    var isEnableAutoInject: Boolean = false


    /**
     * 初始化： 持有ApplicationContext引用，保存支持的Attr
     *
     * @param context Context
     */
    @JvmStatic
    fun init(context: Context, factory2: Factory2?) {
        sAppContext = context.applicationContext
        systemUiMode = getConfigurationUiMode(context.getResources().getConfiguration())
        HideLog.init(sAppContext)
        sFactory2 = factory2
        val appTheme = AppResourceUtils.getManifestApplicationTheme(sAppContext)
        if (appTheme != 0) {
            sAppContext!!.getTheme().applyStyle(appTheme, true)
        }
        if (sAppContext is Application) {
            (sAppContext as Application?)!!.unregisterActivityLifecycleCallbacks(
                lifecycleCallbacks
            )
            (sAppContext as Application?)!!.registerActivityLifecycleCallbacks(
                lifecycleCallbacks
            )
            val inflater = LayoutInflater.from(sAppContext)
            this.setFactory2(inflater)
        }
    }

    fun getAppContext(): Context? {
        return sAppContext
    }


    /**
     * 设置默认日夜间模式
     */
    @JvmStatic
    fun setDefaultUiMode(@ConfigAbleNightMode mode: Int): Boolean {

        appUiMode = mode
        if (appUiMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
//            appConfigurationUiMode = getConfigurationUiMode(Resources.getSystem().configuration)
            appConfigurationUiMode = convertFromAppDelegateToConfigurationMode(systemUiMode)
        } else {
            appConfigurationUiMode = convertFromAppDelegateToConfigurationMode(appUiMode)
        }
        /**
         * 1. 设置默认 uiMode
         * 2. 遍历 AppCompatDelegate 执行 applyDayNight
         * 2.1 更新 Configuration#uiMode
         * 2.2 Activity#recreate 或者 Activity.onConfigurationChanged(mode);
         */
        AppCompatDelegate.setDefaultNightMode(mode)
        // 更新 ApplicationContext
        return AppResourceUtils.updateUiModeForApplication(sAppContext, mode)
    }


    /**
     * 设置日夜间模式
     * 同时更新默认日夜间模式
     */
    @JvmStatic
    fun setUiMode(@ConfigAbleNightMode mode: Int) {
        if (sAppContext == null) {
            HideLog.e(TAG, "Using the ui mode, you need to initialize")
            return
        }
        val uiModeChange = setDefaultUiMode(mode)
        var appTheme = 0
        // 应用Application
        if (uiModeChange) {
            appTheme = AppResourceUtils.getManifestApplicationTheme(sAppContext)
            if (appTheme != 0) {
                sAppContext!!.theme.applyStyle(appTheme, true)
            }
        }

        // 遍历应用所有Activity
        val appStack = AppStack.getAppStack()
        if (appStack != null) {
            for (activity in appStack) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (activity.isDestroyed) continue
                }
                if (isContainsIgnoreActivity(activity::class.java)) {
                    continue
                }
                // 跟随系统时，以下方法仍需要调用
                if (activity is AppCompatActivity) {
                    activity.delegate.applyDayNight()
                }
                if (uiModeChange) {
                    val theme = AppResourceUtils.getManifestActivityTheme(activity)
                    if (theme != 0) {
                        activity.theme.applyStyle(theme, true)
                    } else if (appTheme != 0) {
                        activity.theme.applyStyle(appTheme, true)
                    }
                }
                if (activity is UiModeChangeListener) {
                    (activity as UiModeChangeListener).onUiModeChange()
                }
            }
        }
        ViewStore.dispatchApplyUiMode()
    }


    /**
     * 内部调用
     *遍历Activity中的View，应用日夜间模式
     */
    @JvmStatic
    fun applyUiModeViews(activity: Activity?) {
        ViewStore.applyUiMode(activity!!)
    }

    @JvmStatic
    fun setFactory2(inflater: LayoutInflater) {

        if (inflater.factory2 == null) {
            LayoutInflaterCompat.setFactory2(inflater, obtainInflaterFactory())
        } else {
            if (inflater.factory2 !is FactoryMerger) {
                val factoryMerger = obtainInflaterFactory()
                addFactory2(inflater.factory2)
                try {
                    LayoutInflaterCompat.setFactory2(inflater, factoryMerger)
                } catch (e: Exception) {
                    forceSetFactory2(inflater, factoryMerger)
                }


            }
        }
    }


    private fun forceSetFactory2(inflater: LayoutInflater, factory: Factory2) {
        val compatClass: Class<LayoutInflaterCompat> = LayoutInflaterCompat::class.java
        val inflaterClass: Class<LayoutInflater> = LayoutInflater::class.java
        try {
            val sCheckedField = compatClass.getDeclaredField("sCheckedField")
            sCheckedField.setAccessible(true)
            sCheckedField.setBoolean(compatClass, false)

            val mFactory2 = inflaterClass.getDeclaredField("mFactory2")
            mFactory2.setAccessible(true)
            mFactory2.set(inflater, factory)
//            val mFactory = inflaterClass.getDeclaredField("mFactory")
//            mFactory.setAccessible(true)
//            mFactory.set(inflater, factory)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun addFactory2(factory: LayoutInflater.Factory2) {
        obtainInflaterFactory().addBeforeFactory(factory)
    }


    /**
     * 取消指定当前Activity的夜间模式
     */
    fun cancelLocalNightMode(activity: AppCompatActivity) {
        activity.delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        setLocalNightMode(activity, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
    }


    /**
     * 恢复指定当前Activity的夜间模式到默认
     */
    fun recoveryToDefaultUiMode(activity: AppCompatActivity) {
        activity.delegate.localNightMode = this.appUiMode
        setLocalNightMode(activity, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
    }

    /**
     * 设置指定当前Activity的夜间模式
     */
    fun setLocalNightMode(activity: AppCompatActivity, @ConfigAbleNightMode uiMode: Int) {
//        var currentMode = AppResourceUtils.calculateNightMode(activity)
        var currentMode = getUiModeFromConfiguration(activity.resources.configuration)

        activity.delegate.localNightMode = uiMode
        var newMode = AppResourceUtils.calculateNightMode(activity)
        if (newMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            newMode = systemUiMode
        }
        if (newMode != currentMode) {
            //todo
        }
        applyUiModeViews(activity)

    }


    /**
     * 获取 Configuration 日夜间模式
     */
    fun getConfigurationUiMode(config: Configuration): Int {
        return config.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }

    /**
     * 获取 Inflater Factory 实例
     *
     * @return LayoutInflaterFactory
     * @see .setInflaterFactor
     */
    fun obtainInflaterFactory(): FactoryMerger {
        return defaultFactory
    }

    /**
     * 设置日志 debug模式状态
     *
     * @param isDebug : false 强制关闭日志
     */
    fun setLogDebug(isDebug: Boolean) {
        HideLog.setIsDebug(isDebug)
    }


    /**
     * 保存手动创建的View
     */
    fun saveView(context: Context, v: View) {
        ViewStore.saveView(context, v)
    }

    /**
     * 保存 View 的属性值
     *
     * androidx.appcompat.R.styleable.AppCompatImageView
     * com.aliya.uimode.R.styleable.ProgressBarHelper
     * com.aliya.uimode.R.styleable.SeekBarHelper
     * com.aliya.uimode.R.styleable.TextViewHelper
     * com.aliya.uimode.R.styleable.ToolbarHelper
     * com.aliya.uimode.R.styleable.UiModeView
     * androidx.appcompat.R.styleable.ViewBackgroundHelper
     */
    fun saveViewValue(
        v: View,
        styleableRes: IntArray,
        @StyleableRes index: Int,
        @AnyRes resourceId: Int
    ) {
        val tag = v.getTag(R.id.tag_ui_mode_type_array_map)
        var map: HashMap<IntArray, CachedTypedValueArray?>? = null
        if (tag != null) {
            map = tag as HashMap<IntArray, CachedTypedValueArray?>
        } else {
            map = HashMap()
            v.setTag(R.id.tag_ui_mode_type_array_map, map)
        }
        var cachedTypeArray = map[styleableRes] as? CachedTypedValueArray?
        if (cachedTypeArray == null) {
            cachedTypeArray = CachedTypedValueArray(v.resources, WeakReference(v.context))
            map[styleableRes] = cachedTypeArray
            cachedTypeArray.putIndexAttr( index)
            map[styleableRes] = cachedTypeArray
        } else {
            if (cachedTypeArray.peekValue(index) == null) {
                cachedTypeArray.putIndexAttr( index)
            }
        }
        val typedValue = TypedValue()
        typedValue.type = TypedValue.TYPE_STRING
        typedValue.resourceId = resourceId
        cachedTypeArray.putTypeValue(index, typedValue)
        saveView(v.context, v)
        onUiModeChanged(v)
    }


    /**
     * 移除 View 的属性值
     *
     * androidx.appcompat.R.styleable.AppCompatImageView
     * com.aliya.uimode.R.styleable.ProgressBarHelper
     * com.aliya.uimode.R.styleable.SeekBarHelper
     * com.aliya.uimode.R.styleable.TextViewHelper
     * com.aliya.uimode.R.styleable.ToolbarHelper
     * com.aliya.uimode.R.styleable.UiModeView
     * androidx.appcompat.R.styleable.ViewBackgroundHelper
     */
    fun removeViewValue(
        v: View,
        styleableRes: IntArray,
        @StyleableRes index: Int,
    ) {
        val tag = v.getTag(R.id.tag_ui_mode_type_array_map)
        var map: HashMap<IntArray, CachedTypedValueArray?>? = null
        if (tag != null) {
            map = tag as HashMap<IntArray, CachedTypedValueArray?>

            var cachedTypeArray = map[styleableRes] as? CachedTypedValueArray?
            if (cachedTypeArray != null) {
                if (cachedTypeArray.peekValue(index) != null) {
                    cachedTypeArray.removeValue(index)
                    if (cachedTypeArray.isEmpty()) {
                        cachedTypeArray.recycle()
                        map.remove(styleableRes)
                    }
                }
            }
        } else {
        }

    }


    /**
     *
     * 禁用 View 日夜间切换,无法恢复
     * 不移除 View日夜间属性值
     */
    fun disableViewUimode(view: View){
        ViewStore.removeView(view.context, view)
    }

    /**
     *
     * 禁用 View 日夜间切换,无法恢复
     * 不移除 View日夜间属性值
     */
    fun enableViewUimode(view: View){
       saveView(view.context, view)
    }

    /**
     * 移除 View 的所有日夜间属性值
     */
    fun removeViewAllValue(
        view: View,
    ) {
        val tag = view.getTag(R.id.tag_ui_mode_type_array_map)
        var map: HashMap<IntArray, CachedTypedValueArray?>? = null
        if (tag != null) {
            map = tag as HashMap<IntArray, CachedTypedValueArray?>
            map.forEach { (key, value) ->
                value?.recycle()
            }
            map.clear()
            view.setTag(R.id.tag_ui_mode_type_array_map, null)
        }
        ViewStore.removeView(view.context, view)

    }


    fun removeViewValueTextColor(view: TextView) {
        removeViewValue(
            view,
            R.styleable.TextViewHelper,
            R.styleable.TextViewHelper_android_textColor,
        )
    }

    fun removeViewValueBackground(view: View) {
        removeViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_android_background,
        )
    }

    fun removeViewValueBackgroundTint(view: View) {
        removeViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_backgroundTint,
        )
    }

    fun removeViewValueImageSrc(view: ImageView) {
        removeViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_android_src,
        )
    }

    fun removeViewValueImageTint(view: ImageView) {
        removeViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_tint,
        )
    }


    fun saveViewValueTextColor(view: TextView, @ColorRes color: Int) {
        saveViewValue(
            view,
            R.styleable.TextViewHelper,
            R.styleable.TextViewHelper_android_textColor,
            color
        )
    }

    fun saveViewValueBackground(view: View, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_android_background,
            res
        )
    }

    fun saveViewValueBackgroundTint(view: View, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_backgroundTint,
            res
        )
    }

    fun saveViewValueImageSrc(view: ImageView, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_android_src,
            res
        )
    }

    fun saveViewValueImageTint(view: ImageView, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_tint,
            res
        )
    }


    /**
     * 应用样式
     */
    fun applyStyle(v: View, @StyleRes style: Int) {
        UiModeDelegate.applyStyle(v, style)
    }


    /**
     *系统配置改变,在Application中调用
     */
    fun onSystemConfigurationChanged() {
        /**
         * 有可能其他进程调用, 这里只处理本进程
         */
        if (sAppContext == null) {
            return
        }
        systemUiMode = getConfigurationUiMode(Resources.getSystem().configuration)

        if (appUiMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppResourceUtils.updateUiModeForApplication(sAppContext, appUiMode)
            return
        }

        val configurationUiMode: Int = getConfigurationUiMode(Resources.getSystem().configuration)
        AppCompatDelegate.getDefaultNightMode()
        if (appConfigurationUiMode != configurationUiMode) {
            setUiMode(appUiMode)
        }


    }

    /**
     * 判断当前
     * 是否手动选择夜间模式， 或者跟随系统且系统为夜间模式
     */
    fun isNight(): Boolean {
        return appConfigurationUiMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 判断是否支持跟随系统
     */
    fun isFollowSystemAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isFollowSystem(): Boolean {
        return appUiMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }


    /**
     * 获取 Configuration 日夜间模式
     */
    @ConfigAbleNightMode
    private fun getUiModeFromConfiguration(newConfig: Configuration): Int {
        var configurationUiMode: Int = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val mode = when (configurationUiMode) {
            Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        return mode
    }


    /**
     * 将 AppCompatDelegate.MODE_NIGHT_XXX 转换成 Configuration.UI_MODE_NIGHT_XXX
     */
    private fun convertFromAppDelegateToConfigurationMode(mode: Int): Int {
        val configUiMode = when (mode) {
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                // If we're following the system, we just use the system default from the
                // application context
                val appConfig: Configuration =
                    sAppContext!!.getResources().getConfiguration()
                appConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
            }

            else -> {
                val appConfig: Configuration =
                    sAppContext!!.getResources().getConfiguration()
                appConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
            }
        }
        return configUiMode
    }

    @IntDef(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ConfigAbleNightMode
}