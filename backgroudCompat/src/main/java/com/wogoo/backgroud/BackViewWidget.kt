package com.wogoo.backgroud

import android.content.res.TypedArray
import android.os.Build
import android.view.View
import com.aliya.uimode.uimodewidget.ViewWidget

class BackViewWidget:ViewWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()

        registerCustomAttrArray(R.styleable.background);
        registerCustomAttrArray(R.styleable.background_press);
        registerCustomAttrArray(R.styleable.background_selector);
        registerCustomAttrArray( R.styleable.text_selector);
        registerCustomAttrArray( R.styleable.background_button_drawable);
        registerCustomAttrArray( R.styleable.bl_other);
        registerCustomAttrArray( R.styleable.bl_anim);
        registerCustomAttrArray(R.styleable.background_multi_selector);
        registerCustomAttrArray( R.styleable.background_multi_selector_text);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            registerCustomAttrArray(R.styleable.background_selector_pre_21);
        }
    }

    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, TypedArray>) {
        super.onApplyCustom(v, typedArrayMap)
        BackgroundFactory.setViewBackground(v.context,typedArrayMap as MutableMap<Array<Int>, TypedArray>,v)

    }
}