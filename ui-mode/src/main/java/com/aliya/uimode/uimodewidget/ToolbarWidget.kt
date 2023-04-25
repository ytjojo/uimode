package com.aliya.uimode.uimodewidget

import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.appcompat.widget.Toolbar
import com.aliya.uimode.R

open class ToolbarWidget : ViewWidget() {


    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.ToolbarHelper)
//        registerAttrArray(
//            intArrayOf(
//                R.attr.logo,
//                R.attr.navigationIcon,
//                R.attr.subtitleTextColor,
//                R.attr.titleTextColor
//            )
//        )
    }




    open fun setLogo(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val logoDrawable: Drawable = getDrawable(toolBar, typedValue)
        toolBar.logo = logoDrawable
    }

    open fun setNavigationIcon(toolBar: Toolbar?,typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val iconDrawable: Drawable = getDrawable(toolBar, typedValue)
        toolBar.navigationIcon = iconDrawable
    }

    open fun setTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        toolBar.setTitleTextColor(getColor(toolBar, typedValue))
    }

    open fun setSubTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        toolBar.setSubtitleTextColor(getColor(toolBar, typedValue))
    }
}