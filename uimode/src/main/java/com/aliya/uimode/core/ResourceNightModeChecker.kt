package com.aliya.uimode.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.aliya.uimode.utils.AppResourceUtils

object ResourceNightModeChecker {
    
    /**
     * 检查资源是否存在夜间模式的定义
     */
    fun hasNightModeResource(context: Context, @AnyRes resourceId: Int): Boolean {
        if (resourceId == 0) return false
        
        val resources = context.resources
        val currentConfig = resources.configuration
        
        // 获取资源名称
        val resourceName = resources.getResourceEntryName(resourceId)
        val resourceType = resources.getResourceTypeName(resourceId)



        
        // 创建夜间模式配置
        val nightConfig = Configuration(currentConfig).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        }
        val nightContext = context.createConfigurationContext(nightConfig)
        val nightResources = nightContext.resources


        
        // 创建日间模式配置
        val dayConfig = Configuration(currentConfig).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
        }
        val dayContext = context.createConfigurationContext(dayConfig)
        val dayResources = dayContext.resources
        

        // 在夜间模式下获取资源 ID
        val nightResourceId = nightResources.getIdentifier(resourceName, resourceType, context.packageName)
        
        // 在日间模式下获取资源 ID
        val dayResourceId = dayResources.getIdentifier(resourceName, resourceType, context.packageName)
        
        // 如果 ID 不同，说明有独立的夜间模式定义
        return nightResourceId != dayResourceId
    }
    
    /**
     * 检查 drawable 资源是否有夜间模式版本
     */
    fun hasNightModeDrawable(context: Context, @DrawableRes drawableRes: Int): Boolean {
        return hasNightModeResource(context, drawableRes)
    }
    
    /**
     * 检查 color 资源是否有夜间模式版本
     */
    fun hasNightModeColor(context: Context, @ColorRes colorRes: Int): Boolean {
        return hasNightModeResource(context, colorRes)
    }

    private fun isSameResourcePath(dayResources: Resources, nightResources: Resources, dayResId: Int, nightResId: Int): Boolean{
        val dayPath = getResourcePath(dayResources, dayResId)
        val nightPath = getResourcePath(nightResources, nightResId)
        return dayPath.isNotEmpty() &&  dayPath == nightPath
    }

    private fun getResourcePath(resources: Resources, resId: Int): String {
        return try {
            val implField = Resources::class.java.getDeclaredField("mResourcesImpl")
            implField.isAccessible = true
            val impl = implField.get(resources)

            val getResourcePathMethod = impl.javaClass.getDeclaredMethod(
                "getResourcePath",
                Int::class.java
            )
            getResourcePathMethod.isAccessible = true
            getResourcePathMethod.invoke(impl, resId) as String
        } catch (e: Exception) {
            ""
        }
    }



    // 缓存检查结果，避免重复计算耗时
    private val cache = mutableMapOf<Int, Boolean>()

    fun hasNightModeResourceByTypedValue(context: Context, @AnyRes resId: Int): Boolean {
        if (resId == 0) return false

        // 1. 检查缓存
        cache[resId]?.let { return it }
        val uiModeMask = Configuration.UI_MODE_NIGHT_MASK
        val nightConfig = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and uiModeMask.inv()) or Configuration.UI_MODE_NIGHT_YES
        }
        val resources = context.createConfigurationContext(nightConfig).resources
        val value = AppResourceUtils.getTypedValue()

        // 2. 获取资源元数据 (true 表示追踪引用)
        resources.getValue(resId, value, true)

        // 3. 判定逻辑
        val hasNight = when {
            // 如果是文件类资源 (Drawable, Layout, res/color 目录下的 XML)
            value.type == TypedValue.TYPE_STRING -> {
                val path = value.string.toString()
                // 检查路径中是否包含 -night 限定符
                path.contains("-night")
            }
            else -> false
        }

        cache[resId] = hasNight
        return hasNight
    }

}
