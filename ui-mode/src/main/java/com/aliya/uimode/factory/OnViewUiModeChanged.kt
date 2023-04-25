package com.aliya.uimode.factory

import android.view.View

interface OnViewUiModeChanged<T : View> {

    fun onChanged(view:T)
}