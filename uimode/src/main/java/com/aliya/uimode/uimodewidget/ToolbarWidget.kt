package com.aliya.uimode.uimodewidget

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

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


    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        if (Arrays.equals(styleable, R.styleable.ToolbarHelper)) {
            val indexCount = typedArray.length()
            val toolbar = v as? Toolbar ?: return false
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.ToolbarHelper_android_navigationIcon -> {

                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                toolbar.navigationIcon = it
                            }

                        }

                        R.styleable.ToolbarHelper_android_logo -> {
                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                toolbar.logo = it
                            }

                        }

                        R.styleable.ToolbarHelper_android_titleTextColor -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                toolbar.setTitleTextColor(it)
                            }

                        }

                        R.styleable.ToolbarHelper_android_subtitleTextColor -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                toolbar.setSubtitleTextColor(it)
                            }

                        }
                    }
                }
            }
            return true
        }
        return false
    }


    open fun setLogo(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val logoDrawable = TypedValueUtils.getDrawable(toolBar, typedValue)
        toolBar.logo = logoDrawable
    }

    open fun setNavigationIcon(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val iconDrawable: Drawable? = TypedValueUtils.getDrawable(toolBar, typedValue)
        toolBar.navigationIcon = iconDrawable
    }

    open fun setTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val color = TypedValueUtils.getColorStateList(toolBar, typedValue)
        color?.apply {
            toolBar.setTitleTextColor(color)
        }

    }

    open fun setSubTitleTextColor(toolBar: Toolbar?, typedValue: TypedValue) {
        if (toolBar == null) {
            return
        }
        val color = TypedValueUtils.getColorStateList(toolBar, typedValue)
        color?.apply {
            toolBar.setSubtitleTextColor(color)
        }

    }
}