package com.wogoo.backgroud

import android.content.res.TypedArray
import android.os.Build
import android.view.View
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.uimodewidget.AbstractWidget
import com.aliya.uimode.uimodewidget.ViewWidget

class BackgroundViewWidget : AbstractWidget() {


    /**
     * 自定义属性
     * background
     * background_button_drawable
     * background_multi_selector
     * background_multi_selector_text
     * background_press
     * background_selector
     * bl_anim
     * bl_other
     * bl_text
     * text_selector
     *
     */
    override fun onRegisterStyleable() {
        registerCustomAttrArray(com.noober.background.R.styleable.background);
        registerCustomAttrArray(com.noober.background.R.styleable.background_press);
        registerCustomAttrArray(com.noober.background.R.styleable.background_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.text_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.background_button_drawable);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_other);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_anim);
        registerCustomAttrArray(com.noober.background.R.styleable.background_multi_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.background_multi_selector_text);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_text);
    }

    override fun onApply(
        v: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray
    ): Boolean {
        return false
    }

    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, CachedTypedValueArray>) {
        super.onApplyCustom(v, typedArrayMap)
        BackgroundTypedArrayDelegate.setViewBackground(
            v.context,
            typedArrayMap as MutableMap<IntArray, CachedTypedValueArray>,
            v
        )

    }
}