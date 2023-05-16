package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import com.aliya.uimode.R
import java.util.*

open class ViewWidget : AbstractWidget() {

    @CallSuper
    override fun onRegisterStyleable() {
        registerAttrArray(R.styleable.ViewBackgroundHelper)
        registerAttrArray(R.styleable.UiModeView)
    }


    override fun onApply(v: View, styleable: IntArray, typedArray: TypedArray): Boolean {
        if (Arrays.equals(styleable, R.styleable.ViewBackgroundHelper)) {
            val indexCount = typedArray.indexCount
            var background = v.background
            var colorStateList: ColorStateList? = null
            for (i in 0 until indexCount) {
                val attr = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(attr)
                if (typedValue != null) {
                    when (attr) {
                        R.styleable.ViewBackgroundHelper_android_background -> {
                            background = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }
                        R.styleable.ViewBackgroundHelper_backgroundTint -> {

                            colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                        }

                    }
                }
            }
            if (background != v.background) {
                v.background = background
            }
            colorStateList?.let {
                ViewCompat.setBackgroundTintList(v, it)
            }

            return true

        } else if (Arrays.equals(styleable, R.styleable.UiModeView)) {
            val indexCount = typedArray.indexCount
            for (i in 0 until indexCount) {
                val indexInAttrArray = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInAttrArray)
                if (typedValue != null) {
                    when (indexInAttrArray) {
                        R.styleable.UiModeView_android_foreground -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                v.foreground = TypedValueUtils.getDrawable(
                                    v,
                                    typedValue,
                                    this
                                )
                            }
                        }
                        R.styleable.UiModeView_view_invalidate -> {
                            v.invalidate()
                        }

                        R.styleable.UiModeView_android_theme -> {
                            val style = TypedValueUtils.getStyle(v, typedValue, this)
                            if (style != 0) {
                                v.getContext().getTheme()?.applyStyle(style, true)
                            }
                        }

                    }
                }
            }
            return true
        }
        return false
    }


}