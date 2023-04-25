package com.aliya.uimode.factory

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import com.aliya.uimode.R

object UiModeDelegate {

    fun onViewCreated(view: View, attrs: AttributeSet) {
        val iApplyAttrResourceId = WidgetRegister.getBySuperclass(view::class.java)
        iApplyAttrResourceId?.apply {
            this.assemble(view, attrs)
        }
    }


    fun onUiModeChanged(view: View) {

        val tagStyle = view.getTag(R.id.tag_ui_mode_widget_style)
        if(tagStyle != null && tagStyle is Int){
            val styleResId = tagStyle
            val iApplyAttrResourceId = WidgetRegister.getBySuperclass(view::class.java)
            iApplyAttrResourceId?.apply {
                this.applyStyle(view, styleResId)
            }
        }

        val tag = view.getTag(R.id.tag_ui_mode_type_array_map)
        if(tag != null && tag is Map<*,*>){
            val typeArrayMap = tag as Map<IntArray,TypedArray>
            val iApplyAttrResourceId = WidgetRegister.getBySuperclass(view::class.java)
            iApplyAttrResourceId?.apply {
                typeArrayMap.forEach { entry ->
                    this.onApply(view, entry.key,entry.value)
                }
            }

        }

        val tagCustom = view.getTag(R.id.tag_ui_mode_custom_type_array_map)
        if(tagCustom != null && tagCustom is Map<*,*>){
            val typedArrayMap = tagCustom as Map<IntArray,TypedArray>
            val iApplyAttrResourceId = WidgetRegister.getBySuperclass(view::class.java)
            iApplyAttrResourceId?.apply {
                iApplyAttrResourceId.onApplyCustom(view, typedArrayMap)
            }
        }
        val tagOnUiModeChanged = view.getTag(R.id.tag_ui_mode_on_ui_mode_changed_list)
        if(tagOnUiModeChanged != null && tagOnUiModeChanged is ArrayList<*>){
            val onUiModeChangedList = tagOnUiModeChanged as ArrayList<OnViewUiModeChanged<View>>
            onUiModeChangedList.forEach {
                it.onChanged(view)
            }
        }


    }


}