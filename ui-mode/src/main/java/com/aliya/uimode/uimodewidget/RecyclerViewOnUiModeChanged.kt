package com.aliya.uimode.uimodewidget

import androidx.recyclerview.widget.RecyclerView
import com.aliya.uimode.factory.OnViewUiModeChanged

class RecyclerViewOnUiModeChanged:OnViewUiModeChanged<RecyclerView> {

    override fun onChanged(view: RecyclerView) {
        view.adapter?.notifyDataSetChanged()
    }
}