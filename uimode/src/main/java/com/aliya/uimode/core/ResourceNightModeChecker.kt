package com.aliya.uimode.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AnyRes
import com.aliya.uimode.utils.AppResourceUtils

object ResourceNightModeChecker {

    // 缓存检查结果，避免重复计算耗时
    private val cache = mutableMapOf<Int, Boolean>()

    fun hasNightModeResource(context: Context, @AnyRes resId: Int): Boolean {
        if (resId == 0) return false

        // 1. 检查缓存
        cache[resId]?.let { return it }

        val value = AppResourceUtils.getTypedValue()

        try {
            val uiModeMask = Configuration.UI_MODE_NIGHT_MASK
            val nightConfig = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_YES
            }
            val resources = context.createConfigurationContext(nightConfig).resources
            // 2. 获取资源元数据 (true 表示追踪引用)
            resources.getValue(resId, value, true)
            // 如果是文件类资源 (Drawable, Layout, res/color 目录下的 XML)
            if (value.type == TypedValue.TYPE_STRING) {
                val path = value.string?.toString()
                if (!path.isNullOrEmpty()) {
                    // 检查路径中是否包含 -night 限定符
                    val hasNight =  path.contains("-night")
                    cache[resId] = hasNight
                    return hasNight
                }
            }
        } catch (e: Exception) {
        }

        // 3. 判定逻辑
        val hasNight = when {

            // 如果是普通的颜色值 (values/colors.xml 中的 #FFFFFF)
            resIdIsColor(context.resources, resId) -> {
                checkColorDifference(context, resId)
            }

            else -> false
        }

        cache[resId] = hasNight
        return hasNight
    }

    private fun resIdIsColor(res: Resources, resId: Int): Boolean {
        return try {
            res.getResourceTypeName(resId) == "color"
        } catch (e: Exception) {
            false
        }
    }

    // 对于简单颜色值，对比日夜间模式下的具体数值
    private fun checkColorDifference(context: Context, resId: Int): Boolean {
        return try {
            val uiModeMask = Configuration.UI_MODE_NIGHT_MASK

            val dayConfig = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            val dayColor = context.createConfigurationContext(dayConfig).getColor(resId)

            val nightConfig = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_YES
            }
            val nightColor = context.createConfigurationContext(nightConfig).getColor(resId)

            dayColor != nightColor
        } catch (e: Exception) {
            false
        }
    }




}
