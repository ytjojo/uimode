package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.view.View
import com.aliya.uimode.R

open class SeekbarWidget:AbstractWidget() {
    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.SeekBarHelper)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: TypedArray): Boolean {

        return false
    }
}