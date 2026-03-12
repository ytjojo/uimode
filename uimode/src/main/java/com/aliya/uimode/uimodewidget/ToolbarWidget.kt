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
        val logoDrawable = TypedValueUtils.getDrawable(toolBar,typedValue,this)
        toolBar.logo = logoDrawable
    }

    open fun setNavigationIcon(toolBar: Toolbar?,typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val iconDrawable: Drawable? = TypedValueUtils.getDrawable(toolBar,typedValue,this)
        toolBar.navigationIcon = iconDrawable
    }

    open fun setTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val color = TypedValueUtils.getColorStateList(toolBar,typedValue,this)
        color?.apply {
            toolBar.setTitleTextColor(color)
        }

    }

    open fun setSubTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val color = TypedValueUtils.getColorStateList(toolBar,typedValue,this)
        color?.apply {
            toolBar.setSubtitleTextColor(color)
        }

    }
}