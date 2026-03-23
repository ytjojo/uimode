package com.aliya.uimode.sample

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.aliya.uimode.R
import com.aliya.uimode.debug.WidgetDebugTool
import com.aliya.viewtreedebug.ViewTreeDebugTool
import com.aliya.viewtreedebug.ViewTreeDebugTool.DetailInfoProvider

/**
 * Application
 *
 * @author a_liYa
 * @date 2018/1/23 10:05.
 */
class App : Application() {
    private val PATH_VIVO_UI_MODE = "vivo_nightmode_used"

    override fun onCreate() {
        super.onCreate()

        Log.e("TAG", "App - defaultNightMode: " + AppCompatDelegate.getDefaultNightMode())

        AppUiMode.init(this@App)
        registerUiModeObserver()

        WidgetDebugTool.isDebugEnabled = true
        ViewTreeDebugTool.registerDetailInfoProvider(object : DetailInfoProvider {
            override fun provide(view: View): String? {
                return view.getTag(R.id.tag_ui_mode_assemble_info) as? String?
            }
        })
        ViewTreeDebugTool.registerDetailInfoProvider(object : DetailInfoProvider {
            override fun provide(view: View): String? {
                return view.getTag(R.id.tag_ui_mode_apply_info) as? String?
            }
        })
    }

    override fun attachBaseContext(base: Context?) {
        AppUiMode.disableAutoInfect()
        super.attachBaseContext(base)
    }

    private fun registerUiModeObserver() {
        getContentResolver().registerContentObserver(
            Settings.System.getUriFor(PATH_VIVO_UI_MODE), false,
            object : ContentObserver(Handler()) {
                override fun onChange(selfChange: Boolean) {
                    val uiMode = Settings.System.getInt(getContentResolver(), PATH_VIVO_UI_MODE, -2)
                    // uiMode = 1 时代表深色模式开启
                    Log.e("TAG", "onChange: " + selfChange + " - " + uiMode)
                }
            })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppUiMode.onSystemConfigurationChanged()
    }

    companion object {


        fun uiModeToString(uiMode: Int): String {
            val configUiMode = uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (configUiMode) {
                Configuration.UI_MODE_NIGHT_UNDEFINED -> return "NIGHT_AUTO"
                Configuration.UI_MODE_NIGHT_YES -> return "夜间"
                Configuration.UI_MODE_NIGHT_NO -> return "白间"
                else -> return "未知 - " + configUiMode
            }
        }
    }
}
