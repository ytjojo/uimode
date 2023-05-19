package com.aliya.uimode

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.res.CachedTypedArray
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.LayoutInflater.Factory2
import android.view.View
import androidx.annotation.AnyRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.core.view.LayoutInflaterCompat
import com.aliya.uimode.factory.FactoryMerger
import com.aliya.uimode.factory.UiModeChangeListener
import com.aliya.uimode.factory.UiModeDelegate
import com.aliya.uimode.factory.UiModeDelegate.onUiModeChanged
import com.aliya.uimode.factory.UiModeInflaterFactory
import com.aliya.uimode.factory.ViewStore.applyUiMode
import com.aliya.uimode.factory.ViewStore.dispatchApplyUiMode
import com.aliya.uimode.factory.ViewStore.removeUselessViews
import com.aliya.uimode.factory.ViewStore.saveView
import com.aliya.uimode.utils.AppResourceUtils
import java.lang.ref.SoftReference

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
    private var mIgnoreActivitySet = HashSet<Class<Activity>>()


    @NightMode
    var appUiMode = AppCompatDelegate.MODE_NIGHT_NO

    var appConfigurationUiMode = Configuration.UI_MODE_NIGHT_NO

    fun addIgnoreActivity(clazz: Class<Activity>) {
        mIgnoreActivitySet.add(clazz)
    }

    fun removeIgnoreActivity(clazz: Class<Activity>) {
        mIgnoreActivitySet.remove(clazz)
    }

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


    private val lifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                AppStack.pushActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                AppStack.removeActivity(activity)
                removeUselessViews(activity)
            }
        }

    /**
     * 初始化： 持有ApplicationContext引用，保存支持的Attr
     *
     * @param context Context
     */
    @JvmStatic
    fun init(context: Context, factory2: Factory2?) {
        sAppContext = context.applicationContext
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
            if (inflater.factory2 == null) {
                LayoutInflaterCompat.setFactory2(inflater, obtainInflaterFactory())
            }
        }
    }

    @JvmStatic
    fun setDefaultUiMode(@NightMode mode: Int): Boolean {

        appUiMode = mode
        if (appUiMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            appConfigurationUiMode = getConfigurationUiMode(Resources.getSystem().configuration)
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

    @JvmStatic
    fun setUiMode(@NightMode mode: Int) {
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
        dispatchApplyUiMode()
    }

    @JvmStatic
    fun applyUiModeViews(activity: Activity?) {
        applyUiMode(activity!!)
    }

    /**
     *
     *  设置 UiMode 相关的 Factory2
     *
     *
     * 如果还需要设置自己的 Factory, [com.aliya.uimode.factory.FactoryMerger], 参考以下代码
     *
     * <pre>
     * LayoutInflater.Factory2 before = UiModeManager.obtainInflaterFactory();
     * LayoutInflater.Factory2 after; // 赋值自己的Factory
     * LayoutInflaterCompat.setFactory2(inflater, new FactoryMerger(before, after))
    </pre> *
     *
     * @param inflater [android.app.Activity.getLayoutInflater]
     */
    @JvmStatic
    fun setInflaterFactor(inflater: LayoutInflater?, before: LayoutInflater.Factory2? = null) {
        if (sAppContext != null) {
            if (before != null) {
                LayoutInflaterCompat.setFactory2(
                    inflater!!,
                    FactoryMerger(before, obtainInflaterFactory())
                )
            } else {
                LayoutInflaterCompat.setFactory2(inflater!!, obtainInflaterFactory())
            }

        } else {
            HideLog.e(TAG, "Using the ui mode, you need to initialize")
        }
    }

    fun cancelLocalNightMode(activity: AppCompatActivity) {
        activity.delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        setLocalNightMode(activity, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
    }

    fun setLocalNightMode(activity: AppCompatActivity, @NightMode uiMode: Int) {
//        var currentMode = AppResourceUtils.calculateNightMode(activity)
        var currentMode = getUiModeFromConfiguration(activity.resources.configuration)

        activity.delegate.localNightMode = uiMode
        var newMode = AppResourceUtils.calculateNightMode(activity)
        if (newMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            newMode = getSystemUiMode()
        }
        if (newMode != currentMode) {
            //todo
        }
        applyUiModeViews(activity)

    }


    @NightMode
    fun getSystemUiMode(): Int {
        return getUiModeFromConfiguration(Resources.getSystem().configuration)
    }

    fun getConfigurationUiMode(config:Configuration): Int {
        return  config.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }

    /**
     * 获取 Inflater Factory 实例
     *
     * @return LayoutInflaterFactory
     * @see .setInflaterFactor
     */
    fun obtainInflaterFactory(): Factory2 {
        return LayoutInflaterFactory.get()
    }

    /**
     * 设置日志 debug模式状态
     *
     * @param isDebug : false 强制关闭日志
     */
    fun setLogDebug(isDebug: Boolean) {
        HideLog.setIsDebug(isDebug)
    }

    internal object LayoutInflaterFactory {
        /**
         * 通过软引用单例来优化内存
         */
        var sSoftInstance: SoftReference<UiModeInflaterFactory>? = null
        fun get(): UiModeInflaterFactory {
            var factory: UiModeInflaterFactory? = sSoftInstance?.get()
            if (sSoftInstance == null) {
                factory = UiModeInflaterFactory(sFactory2)
                sSoftInstance = SoftReference(factory)
            }
            return factory!!
        }
    }

    fun saveViewValue(
        v: View,
        styleableRes: IntArray,
        @StyleableRes index: Int,
        @AnyRes resourceId: Int
    ) {
        val tag = v.getTag(R.id.tag_ui_mode_type_array_map)
        var map: HashMap<IntArray, TypedArray?>? = null
        if (tag != null) {
            map = tag as HashMap<IntArray, TypedArray?>
        } else {
            map = HashMap()
            v.setTag(R.id.tag_ui_mode_type_array_map, map)
        }
        var cachedTypeArray = map[styleableRes] as CachedTypedArray?
        if (cachedTypeArray == null) {
            cachedTypeArray = CachedTypedArray(v.resources, v.context)
            map!![styleableRes] = cachedTypeArray
            cachedTypeArray.putIndexAttr(0, index)
            map[styleableRes] = cachedTypeArray
        }
        val typedValue = TypedValue()
        typedValue.type = TypedValue.TYPE_STRING
        typedValue.resourceId = resourceId
        cachedTypeArray.putTypeValue(index, typedValue)
        saveView(v.context, v)
        onUiModeChanged(v)
    }


    fun applyStyle(v: View,@StyleRes style:Int) {
        UiModeDelegate.applyStyle(v,style)
    }


    fun onSystemConfigurationChanged() {

        if (appUiMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppResourceUtils.updateUiModeForApplication(sAppContext, appUiMode)
            return
        }

        var configurationUiMode: Int = getConfigurationUiMode(Resources.getSystem().configuration)
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

    @NightMode
    private fun getUiModeFromConfiguration(newConfig: Configuration): Int {
        var configurationUiMode: Int = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val mode = when (configurationUiMode) {
            Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        return mode
    }

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


}