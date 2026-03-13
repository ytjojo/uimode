package com.aliya.uimode.uimodewidget

import androidx.recyclerview.widget.RecyclerView
import com.aliya.uimode.core.OnViewUiModeChanged
import com.aliya.uimode.core.WidgetRegister

class RecyclerViewOnUiModeChanged:OnViewUiModeChanged<RecyclerView> {

    override fun onChanged(view: RecyclerView) {
        view.adapter?.notifyDataSetChanged()
        view.invalidate()
    }

    fun registerViewUiModeChanged(){
        WidgetRegister.registerViewUiModeChanged(RecyclerView::class.java,this)
    }
}