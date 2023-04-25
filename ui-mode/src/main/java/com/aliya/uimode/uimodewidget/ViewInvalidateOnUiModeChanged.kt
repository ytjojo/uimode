package com.aliya.uimode.uimodewidget

import android.view.View
import com.aliya.uimode.factory.OnViewUiModeChanged

class ViewInvalidateOnUiModeChanged:OnViewUiModeChanged<View> {
    override fun onChanged(view: View) {
        view.invalidate()
    }
}