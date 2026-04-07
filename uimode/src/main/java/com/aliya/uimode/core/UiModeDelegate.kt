package com.aliya.uimode.core

import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.StyleRes
import com.aliya.uimode.R
import com.aliya.uimode.UiModeManager
import com.aliya.uimode.debug.UiModeWidgetDebugTool
import com.aliya.uimode.uimodewidget.AbstractWidget
import com.aliya.uimode.uimodewidget.TypedValueUtils
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.AppUtil

object UiModeDelegate {
    const val TAG = "UiModeDelegate"

    fun onViewCreated(v: View, attrs: AttributeSet) {
        val activity = AppUtil.findActivity(v.context)
        if(activity == null && AppResourceUtils.isRecreateOnUiModeChange(activity)){
            return
        }
        if(activity != null && UiModeManager.isContainsIgnoreActivity(activity::class.java)){
            return
        }
        val list = WidgetRegister.getWidgetList(v)
        var isSave = false
        list?.forEach {
            val result = it.assemble(v, attrs)
            if (result) {
                isSave = result
            }
        }
        if(isSave && UiModeWidgetDebugTool.isDebugEnabled){
            UiModeWidgetDebugTool.onAssembleInfo(v, attrs)
        }
        val viewCreateUiModeChangedList = WidgetRegister.getViewCreateUiModeChanged(v::class.java) as? ArrayList<OnViewCreateUiModeChanged<View>>
        if(viewCreateUiModeChangedList != null && viewCreateUiModeChangedList.isNotEmpty() ){
            viewCreateUiModeChangedList.forEach { viewUiModeChanged ->
                viewUiModeChanged.onCreate(v)
            }
            isSave = true
            v.setTag(R.id.tag_ui_mode_view_ui_mode_changed_list,viewCreateUiModeChangedList)
        }
        if(isSave){
            ViewStore.saveView(v.context, v)
        }

    }


    fun onUiModeChanged(v: View) {
        val tagThemeTypedValue = v.getTag(R.id.tag_ui_mode_theme_typed_value) as? TypedValue?
        tagThemeTypedValue?.let {
            val theme = TypedValueUtils.getStyle(v, it)
            if (theme != 0) {
                v.getContext().getTheme()?.applyStyle(theme, true)
            }
        }


        val list: ArrayList<AbstractWidget>? = WidgetRegister.getWidgetList(v)

        val typedArrayMap = ViewStore.getCachedTypedArrayMap(v)
        if (typedArrayMap != null) {
            list?.forEach {
                typedArrayMap.forEach { entry ->
                    it.onApply(v, entry.key, entry.value)
                }
            }


        }

        val tagCustom = v.getTag(R.id.tag_ui_mode_custom_type_array_map)
        if (tagCustom != null && tagCustom is Map<*, *>) {
            val typedArrayMap = tagCustom as Map<IntArray, CachedTypedValueArray>
            list?.forEach {
                it.onApplyCustom(v, typedArrayMap)
            }
        }

        val tagOnUiModeChanged = v.getTag(R.id.tag_ui_mode_user_view_ui_mode_changed)
        if (tagOnUiModeChanged != null && tagOnUiModeChanged is OnViewUiModeChanged<*>) {
            val onUiModeChanged = tagOnUiModeChanged as OnViewUiModeChanged<View>
            onUiModeChanged.onChanged(v)
        }
        val viewCreateUiModeChangedList = v.getTag(R.id.tag_ui_mode_view_ui_mode_changed_list)
        if (viewCreateUiModeChangedList != null && viewCreateUiModeChangedList is ArrayList<*>) {
            val viewUiModeChangedList = viewCreateUiModeChangedList as ArrayList<OnViewCreateUiModeChanged<View>>
            viewUiModeChangedList.forEach {
                it.onChanged(v)
            }
        }
        if (v is UiModeChangeListener) {
            v.onUiModeChange()
        }
        if (UiModeWidgetDebugTool.isDebugEnabled) {
            UiModeWidgetDebugTool.onApplyInfo(v)
        }


    }


    fun applyStyleUimode(v: View, @StyleRes style:Int) {
        val tagStyle = ViewStore.getViewStyleTag(v)
        val list = WidgetRegister.getWidgetList(v)
        if(style != tagStyle){
            ViewStore.setViewStyleTag(v,style)
            val theme = v.context.theme
            list?.forEach {
                it.assembleStyle(v, style)
            }
        }

        if(v.getTag(R.id.tag_ui_mode_is_save_store) != true){
            ViewStore.saveView(v.context,v)
        }
        this.onUiModeChanged(v)
    }


}