package com.aliya.uimode.core

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.annotation.AnyRes
import java.util.zip.ZipFile

object NightResourceIndex {
    
    // 缓存：有夜间版本的资源ID集合
    private val nightResourceIds = mutableSetOf<Int>()
    private var isInitialized = false
    
    /**
     * 初始化：在 Application 或库初始化时调用一次
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        val startTime = SystemClock.elapsedRealtime()
        
        // 方案A：APK 解析（准确，推荐）
        parseApkResources(context)
        
        // 方案B：如果APK解析失败，降级到反射探测
        // fallbackToReflection(context)
        
        isInitialized = true
        
        Log.d("NightResourceIndex", "Initialized ${nightResourceIds.size} resources in " +
                "${SystemClock.elapsedRealtime() - startTime}ms")
    }
    
    /**
     * 快速判断：O(1) 查表
     */
    fun hasNightVariant(@AnyRes resId: Int): Boolean {
        return nightResourceIds.contains(resId)
    }
    
    /**
     * 核心：解析 APK 资源索引
     */
    private fun parseApkResources(context: Context) {
        try {
            val apkPath = context.applicationInfo.sourceDir
            ZipFile(apkPath).use { zip ->
                
                zip.entries().asSequence()
                    .filter { it.name.startsWith("res/") }
                    .forEach { entry ->
                        val path = entry.name // res/drawable-night-hdpi/ic_like.png
                        val parts = path.split("/")
                        
                        if (parts.size >= 3 && isNightResourcePath(parts[1])) {
                            // 提取资源名称和类型
                            val resType = parts[1] // drawable-night-hdpi
                            val fileName = parts[2]
                            val entryName = fileName.substringBeforeLast(".")
                            
                            // 转换为资源ID
                            val resId = getResourceId(context, entryName, extractBaseType(resType))
                            if (resId != 0) {
                                nightResourceIds.add(resId)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("NightResourceIndex", "Failed to parse APK", e)
        }
    }
    
    /**
     * 判断路径是否包含夜间限定符
     */
    private fun isNightResourcePath(dirName: String): Boolean {
        // 匹配 drawable-night, drawable-night-xhdpi, values-night 等
        return dirName.contains("-night") && 
               !dirName.contains("-night-") // 排除类似 -night-mode 的非标准限定符
    }
    
    /**
     * 提取基础资源类型（去掉限定符）
     */
    private fun extractBaseType(qualifiedType: String): String {
        return when {
            qualifiedType.startsWith("drawable") -> "drawable"
            qualifiedType.startsWith("mipmap") -> "mipmap"
            qualifiedType.startsWith("color") -> "color"
            qualifiedType.startsWith("values") -> when {
                qualifiedType.contains("colors") -> "color"
                qualifiedType.contains("drawables") -> "drawable"
                else -> "raw" // 其他 values 资源
            }
            else -> qualifiedType.substringBefore("-")
        }
    }
    
    /**
     * 通过名称获取资源ID
     */
    private fun getResourceId(context: Context, name: String, type: String): Int {
        return try {
            context.resources.getIdentifier(name, type, context.packageName)
        } catch (e: Exception) {
            0
        }
    }
}