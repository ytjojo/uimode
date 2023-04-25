package com.aliya.uimode.uimodewidget

import com.aliya.uimode.R

open class ProgressBarWidget: TextViewWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.ProgressBarHelper)
    }
}