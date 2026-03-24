package com.aliya.uimode.uimodewidget

import androidx.recyclerview.widget.RecyclerView
import com.aliya.uimode.core.OnViewCreateUiModeChanged
import com.aliya.uimode.core.WidgetRegister

class RecyclerViewOnUiModeChanged: OnViewCreateUiModeChanged<RecyclerView> {

    override fun onChanged(view: RecyclerView) {
        view.adapter?.notifyDataSetChanged()
        view.invalidate()
    }

    fun registerViewUiModeChanged(){
        WidgetRegister.registerViewCreateUiModeChanged(RecyclerView::class.java,this)
    }

    override fun onCreate(view: RecyclerView) {
    }
}