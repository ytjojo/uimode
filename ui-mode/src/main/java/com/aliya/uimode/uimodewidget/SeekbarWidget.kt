package com.aliya.uimode.uimodewidget

import com.aliya.uimode.R

open class SeekbarWidget:ViewWidget() {
    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.SeekBarHelper)
    }
}