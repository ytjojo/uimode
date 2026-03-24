package com.aliya.uimode.debug
import android.util.AttributeSet
import android.view.View
import com.aliya.uimode.HideLog
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.core.OnViewUiModeChanged
import com.aliya.uimode.core.UiModeChangeListener
import com.aliya.uimode.core.WidgetRegister
import com.aliya.uimode.uimodewidget.AbstractWidget
import com.aliya.uimode.utils.AppUtil

/**
 * AbstractWidget 的 Debug 工具类
 * 用于在 assemble 和 apply 方法中进行断言和断点调试输出
 */
object UiModeWidgetDebugTool {

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


    fun onAssembleInfo(
        view: View,
        attributeSet: AttributeSet,
    ): String {
        val stringBuilder: StringBuilder = StringBuilder()
        val viewInfo = buildViewInfo(view, attributeSet)
        val list = WidgetRegister.getListBySuperclass(view::class.java)
        stringBuilder.append("\n ========== Assemble Start ==========")
        stringBuilder.append( viewInfo)
        list.forEach { widget ->
            stringBuilder.append("\nWidget: ${widget.javaClass.simpleName}")
        }
        val info = stringBuilder.toString()
        view.setTag(R.id.tag_ui_mode_assemble_info, info)
        return info

    }


    fun onApplyInfo(
        v: View,
        applyStyleCount: Int
    ) {
        val list: ArrayList<AbstractWidget> = WidgetRegister.getListBySuperclass(v::class.java)
        val sb = StringBuilder()
        val tag = v.getTag(R.id.tag_ui_mode_type_array_map)
        val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        sb.append("====== Apply Start @ time: ${currentTime}  =======")
        sb.appendLine("applyStyleCount: $applyStyleCount")
        val isNight = v.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        sb.appendLine("====  是否是夜间 : ${isNight}  =====")
        if (tag != null && tag is Map<*, *>) {
            val typeArrayMap = tag as Map<IntArray, CachedTypedValueArray>
            list.forEach {
                sb.appendLine("Widget: ${it.javaClass.simpleName}")
                typeArrayMap.forEach { entry ->
                    onApplyInfo(v, entry.key, entry.value,sb)

                }
            }
        }
        val tagCustom = v.getTag(R.id.tag_ui_mode_custom_type_array_map)
        if (tagCustom != null && tagCustom is Map<*, *>) {
            val typedArrayMap = tagCustom as Map<IntArray, CachedTypedValueArray>
            list.forEach {
                sb.appendLine("Widget: ${it.javaClass.simpleName}")
                typedArrayMap.forEach { entry ->
                    onApplyInfo(v, entry.key, entry.value,sb)
                }
            }
        }

        val tagOnUiModeChanged = v.getTag(R.id.tag_ui_mode_on_ui_mode_changed)
        if (tagOnUiModeChanged != null && tagOnUiModeChanged is OnViewUiModeChanged<*>) {
            sb.appendLine("OnViewUiModeChanged onChanged(view)")
        }
        if (v is UiModeChangeListener) {
            sb.appendLine("UiModeChangeListener onChanged")
        }

        val info = sb.toString()
        v.setTag(R.id.tag_ui_mode_apply_info, info)
    }
    fun onApplyInfo(
        view: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray,
        sb: StringBuilder
    ) {

        val attrInfo = buildAttributeInfo(view, styleable, typedArray)
        sb.append(attrInfo)
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
        builder.appendLine("Class: ${view.javaClass.name}")
        val idName = if (view.id == View.NO_ID) "" else view.resources?.getResourceName(view.id)
        builder.appendLine("Id: ${if (view.id == View.NO_ID) "NO_ID" else "${idName} = 0x${view.id.toString(16)}"}")

        if (attributeSet != null) {
            builder.appendLine("Attribute Count: ${attributeSet.attributeCount}")
            builder.appendLine("Style Attribute: 0x${attributeSet.styleAttribute.toString(16)}")
            // 输出关键属性
            for (i in 0 until attributeSet.attributeCount) {
                val attrName = attributeSet.getAttributeName(i)
                val attrValue = attributeSet.getAttributeValue(i)
                builder.appendLine("[$attrName] = $attrValue")
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
        builder.append("\nStyleable Length: ${styleable.size}")
        builder.append("\nTypedArray Length: ${typedArray.length()}")

        val indexCount = typedArray.length()
        if (indexCount > 0) {
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)

                if (typedValue != null) {
                    val attrName = try {
                        view.context.resources.getResourceName(styleable[indexInStyleable])
                    } catch (e: Exception) {
                        "unknown"
                    }
                    builder.append("\n[$indexInStyleable] $attrName")
                    builder.append("\nTypedValue: ${typedValue.toString()}")
                    if (typedValue.resourceId != 0) {
                        try {
                            val resourceName = view.context.resources.getResourceName(typedValue.resourceId)
                            builder.append("\n name: $resourceName")
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