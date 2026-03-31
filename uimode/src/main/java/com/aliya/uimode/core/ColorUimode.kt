package com.aliya.uimode.core

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.ColorRes
import com.aliya.uimode.UiModeManager
import android.content.res.ColorStateList

class ColorUimode {

    companion object {
        var dayConfigurationContext: Context? = null
        var nightConfigurationContext: Context? = null


        fun initConfigurationContext(context: Context) {
            val uiModeMask = Configuration.UI_MODE_NIGHT_MASK

            val dayConfig = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            dayConfigurationContext = context.createConfigurationContext(dayConfig)

            val nightConfig = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_YES
            }
            nightConfigurationContext = context.createConfigurationContext(nightConfig)
        }

        fun getColor(@ColorRes resId: Int): Int {

            return if (UiModeManager.isNight()) {
                nightConfigurationContext?.getColor(resId) ?: Color.TRANSPARENT
            } else {
                dayConfigurationContext?.getColor(resId) ?: Color.TRANSPARENT
            }


        }
        fun getColorStateList(@ColorRes resId: Int): ColorStateList? {
            return if (UiModeManager.isNight()) {
                nightConfigurationContext?.getColorStateList(resId)
            } else {
                dayConfigurationContext?.getColorStateList(resId)
            }
        }
    }
}

fun Int.resColorInt(){
    ColorUimode.getColor(this)
}