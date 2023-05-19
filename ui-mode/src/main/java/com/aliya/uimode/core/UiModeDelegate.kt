package com.aliya.uimode.core

import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.StyleRes
import com.aliya.uimode.R
import com.aliya.uimode.UiModeManager
import com.aliya.uimode.uimodewidget.TypedValueUtils
import com.aliya.uimode.utils.AppUtil

object UiModeDelegate {

    fun onViewCreated(v: View, attrs: AttributeSet) {
        val activity = AppUtil.findActivity(v.context)
        if(activity != null && UiModeManager.isContainsIgnoreActivity(activity::class.java)){
            return
        }
        val list = WidgetRegister.getListBySuperclass(v::class.java)
        var isSave = false
        list.forEach {
            val result = it.assemble(v, attrs)
            if (result) {
                isSave = result
            }
        }
        val viewUiModeChanged =  WidgetRegister.getViewUiModeChanged(v::class.java)
        if(viewUiModeChanged != null){
            isSave = true
            v.setTag(R.id.tag_ui_mode_on_ui_mode_changed,viewUiModeChanged)
        }
        if(isSave){
            ViewStore.saveView(v.context, v)
        }

    }


    fun onUiModeChanged(v: View) {
        val tagThemeTypedValue = v.getTag(R.id.tag_ui_mode_theme_typed_value) as? TypedValue?
        tagThemeTypedValue?.let {
            val theme = TypedValueUtils.getStyle(v, it, WidgetRegister.get(View::class.java)!!)
            if (theme != 0) {
                v.getContext().getTheme()?.applyStyle(theme, true)
            }
        }


        val list = WidgetRegister.getListBySuperclass(v::class.java)
        val tagStyle = v.getTag(R.id.tag_ui_mode_widget_style)
        if (tagStyle != null && tagStyle is Int) {
            val styleResId = tagStyle
            list.forEach {
                it.applyStyle(v, styleResId)
            }
        }

        val tag = v.getTag(R.id.tag_ui_mode_type_array_map)
        if (tag != null && tag is Map<*, *>) {
            val typeArrayMap = tag as Map<IntArray, TypedArray>

            list.forEach {
                typeArrayMap.forEach { entry ->
                    it.onApply(v, entry.key, entry.value)
                }
            }


        }

        val tagCustom = v.getTag(R.id.tag_ui_mode_custom_type_array_map)
        if (tagCustom != null && tagCustom is Map<*, *>) {
            val typedArrayMap = tagCustom as Map<IntArray, TypedArray>
            list.forEach {
                it.onApplyCustom(v, typedArrayMap)
            }
        }
        val tagOnUiModeChanged = v.getTag(R.id.tag_ui_mode_on_ui_mode_changed)
        if (tagOnUiModeChanged != null && tagOnUiModeChanged is OnViewUiModeChanged<*>) {
            val onUiModeChanged = tagOnUiModeChanged as OnViewUiModeChanged<View>
            onUiModeChanged.onChanged(v)
        }
        if (v is UiModeChangeListener) {
            v.onUiModeChange()
        }


    }


    fun applyStyle(v: View,@StyleRes style:Int) {
        val tagStyle = v.getTag(R.id.tag_ui_mode_widget_style)
        val list = WidgetRegister.getListBySuperclass(v::class.java)
        if(style != tagStyle){
            v.setTag(R.id.tag_ui_mode_widget_style,style)
            val theme = v.context.theme
            list.forEach {
                it.applyStyle(v, style)
            }
        }

        if(v.getTag(R.id.tag_ui_mode_is_save_store) != true){
            ViewStore.saveView(v.context,v)
        }
    }


}