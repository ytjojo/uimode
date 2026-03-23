package com.aliya.uimode.debug
import android.util.AttributeSet
import android.view.View
import com.aliya.uimode.HideLog
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.uimodewidget.AbstractWidget
import com.aliya.uimode.utils.AppUtil

/**
 * AbstractWidget 的 Debug 工具类
 * 用于在 assemble 和 apply 方法中进行断言和断点调试输出
 */
object WidgetDebugTool {

    /**
     * Debug 模式开关
     * true: 开启详细日志和断言检查
     * false: 关闭所有 Debug 功能
     */
    var isDebugEnabled = false

    /**
     * 是否启用断言检查
     */
    var isAssertionEnabled = false

    /**
     * 是否记录性能数据
     */
    var isPerformanceMonitoringEnabled = false

    /**
     * 需要调试的 View 类名列表 (支持模糊匹配)
     * 例如：["TextView", "ImageView", "Button"]
     * 为空时表示调试所有 View
     */
    var debugViewClasses: MutableSet<String> = mutableSetOf()


    /**
     * 需要调试的 View所在Activity 类名列表 (支持模糊匹配)
     * 例如：["HomeActivity", "UserInfoActivity"]
     * 为空时表示调试所有 Activity
     */
    var debugActivityClassNamees: MutableSet<String> = mutableSetOf()


    /**
     * 需要调试的 View ID 列表
     * 例如：[R.id.button, R.id.text_view]
     * 0 表示不指定 ID 过滤
     */
    var debugViewIds: MutableSet<Int> = mutableSetOf()

    /**
     * 断言回调接口
     */
    interface AssertionCallback {
        /**
         * 在 assemble 阶段调用
         * @return true 表示断言通过，false 表示断言失败
         */
        fun onAssembleAssertion(
            view: View,
            attributeSet: AttributeSet,
            widget: AbstractWidget
        ): Boolean

        /**
         * 在 apply 阶段调用
         * @return true 表示断言通过，false 表示断言失败
         */
        fun onApplyAssertion(
            view: View,
            styleable: IntArray,
            typedArray: CachedTypedValueArray,
            widget: AbstractWidget
        ): Boolean
    }

    private val assertionCallbacks = mutableListOf<AssertionCallback>()

    /**
     * 注册断言回调
     */
    fun registerAssertionCallback(callback: AssertionCallback) {
        assertionCallbacks.add(callback)
    }

    /**
     * 移除断言回调
     */
    fun unregisterAssertionCallback(callback: AssertionCallback) {
        assertionCallbacks.remove(callback)
    }

    /**
     * 清除所有断言回调
     */
    fun clearAssertionCallbacks() {
        assertionCallbacks.clear()
    }

    /**
     * 判断是否应该对当前 View 进行 Debug
     */
    fun shouldDebugView(view: View): Boolean {
        if (!isDebugEnabled) return false

        if(!debugActivityClassNamees.isEmpty()){
           val activity =  AppUtil.findActivity(view.context)
            if (activity == null || !debugActivityClassNamees.any {
                activity.javaClass.simpleName.contains(it, ignoreCase = true)
            }) {
                return false
            }
        }


        // 如果指定了调试的 View 类名，检查是否匹配
        if (debugViewClasses.isNotEmpty()) {
            val viewClassName = view.javaClass.simpleName
            val isMatch = debugViewClasses.any {
                viewClassName.contains(it, ignoreCase = true)
            }
            if (!isMatch) return false
        }

        // 如果指定了调试的 View ID，检查是否匹配
        if (debugViewIds.isNotEmpty()) {
            val viewId = view.id
            if (viewId == View.NO_ID || !debugViewIds.contains(viewId)) {
                return false
            }
        }

        return true
    }

    /**
     * Assemble 阶段的 Debug 入口
     * @return true 表示继续执行，false 表示中断执行
     */
    fun onAssembleDebug(
        view: View,
        attributeSet: AttributeSet,
        widget: AbstractWidget,
    ): Boolean {
        if (!shouldDebugView(view)) return true

        val viewInfo = buildViewInfo(view, attributeSet)

        HideLog.d("WidgetDebug", "========== Assemble Start ==========")
        HideLog.d("WidgetDebug", "Widget: ${widget.javaClass.simpleName}")
        HideLog.d("WidgetDebug", viewInfo)

        // 执行断言检查
        if (isAssertionEnabled) {
            for (callback in assertionCallbacks) {
                try {
                    val result = callback.onAssembleAssertion(view, attributeSet, widget)
                    if (!result) {
                        HideLog.e("WidgetDebug", "❌ Assertion failed in onAssemble")
                        return false
                    }
                } catch (e: Exception) {
                    HideLog.e("WidgetDebug", "❌ Assertion exception: ${e.message}")
                    e.printStackTrace()
                    return false
                }
            }
        }

        return true
    }


    /**
     * Apply 阶段的 Debug 入口
     * @return true 表示继续执行，false 表示中断执行
     */
    fun onApplyDebug(
        view: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray,
        widget: AbstractWidget,
    ): Boolean {
        if (!shouldDebugView(view)) return true

        val attrInfo = buildAttributeInfo(view, styleable, typedArray)

        HideLog.d("WidgetDebug", "========== Apply Start ==========")
        HideLog.d("WidgetDebug", "Widget: ${widget.javaClass.simpleName}")
        HideLog.d("WidgetDebug", attrInfo)

        // 执行断言检查
        if (isAssertionEnabled) {
            for (callback in assertionCallbacks) {
                try {
                    val result = callback.onApplyAssertion(view, styleable, typedArray, widget)
                    if (!result) {
                        HideLog.e("WidgetDebug", "❌ Assertion failed in onApply")
                        return false
                    }
                } catch (e: Exception) {
                    HideLog.e("WidgetDebug", "❌ Assertion exception: ${e.message}")
                    e.printStackTrace()
                    return false
                }
            }
        }

        return true
    }


    /**
     * 构建 View 信息字符串
     */
    private fun buildViewInfo(view: View, attributeSet: AttributeSet): String {
        val builder = StringBuilder()
        builder.appendLine("View Info:")
        builder.appendLine("  Class: ${view.javaClass.name}")
        val idName = if (view.id == View.NO_ID) "" else view.resources?.getResourceName(view.id)
        builder.appendLine("  Id: ${if (view.id == View.NO_ID) "NO_ID" else "${idName} = 0x${view.id.toString(16)}"}")

        if (attributeSet != null) {
            builder.appendLine("  Attribute Count: ${attributeSet.attributeCount}")
            builder.appendLine("  Style Attribute: 0x${attributeSet.styleAttribute.toString(16)}")

            // 输出关键属性
            for (i in 0 until attributeSet.attributeCount) {
                val attrName = attributeSet.getAttributeName(i)
                val attrValue = attributeSet.getAttributeValue(i)
                builder.appendLine("    [$attrName] = $attrValue")
            }
        }

        return builder.toString()
    }

    /**
     * 构建属性信息字符串
     */
    private fun buildAttributeInfo(
        view: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray
    ): String {
        val builder = StringBuilder()
        builder.appendLine("Apply Info:")
        builder.appendLine("  Styleable Length: ${styleable.size}")
        builder.appendLine("  TypedArray Length: ${typedArray.length()}")

        val indexCount = typedArray.length()
        if (indexCount > 0) {
            builder.appendLine("  Attributes:")
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)

                if (typedValue != null) {
                    val attrName = try {
                        view.context.resources.getResourceName(styleable[indexInStyleable])
                    } catch (e: Exception) {
                        "unknown"
                    }

                    builder.appendLine("    [$indexInStyleable] $attrName")
                    builder.appendLine("      TypeValue: ${typedValue.toString()}")
                    if (typedValue.resourceId != 0) {
                        try {
                            val resourceName = view.context.resources.getResourceName(typedValue.resourceId)
                            builder.appendLine("      ResourceName: $resourceName")
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }

        return builder.toString()
    }


    /**
     * 重置所有配置
     */
    fun reset() {
        isDebugEnabled = false
        isAssertionEnabled = false
        isPerformanceMonitoringEnabled = false
        debugViewClasses.clear()
        debugViewIds.clear()
        clearAssertionCallbacks()
    }
}