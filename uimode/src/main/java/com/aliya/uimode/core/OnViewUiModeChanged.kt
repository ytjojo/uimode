package com.aliya.uimode.core

import android.view.View

interface OnViewUiModeChanged<T : View> {

    fun onChanged(view:T)
}

interface OnViewCreateUiModeChanged<T : View>: OnViewUiModeChanged<T> {

    fun onCreate(view:T)
}