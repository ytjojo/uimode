package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.utils.AppResourceUtils

open class ViewWidget : AbstractWidget() {

    @CallSuper
    override fun onRegisterStyleable() {
//        intArrayOf(R.attr.background, R.attr.backgroundTint, R.attr.backgroundTintMode)
        registerAttrArray(R.styleable.ViewBackgroundHelper)
        registerAttrArray(R.styleable.UiModeView)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: TypedArray): Boolean {
        if (styleable == R.styleable.ViewBackgroundHelper) {
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

        }else if(styleable ==R.styleable.UiModeView){
            val indexCount = typedArray.indexCount
            for (i in 0 until indexCount) {
                val attr = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(attr)
                if (typedValue != null) {
                    when (attr) {
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

                        R.styleable.UiModeView_android_theme-> {
                           val style = TypedValueUtils.getStyle(v,typedValue,this)
                            if(style != 0){
                                v.getContext().getTheme()?.applyStyle(style, true)
                            }
                        }

                    }
                }
            }
        }

        return true
    }

    open fun getDrawable(v: View, typedValue: TypedValue): Drawable {
        if (typedValue.type == TypedValue.TYPE_ATTRIBUTE) {
            return AppResourceUtils.getDrawableWithAttr(v.context, typedValue.resourceId)
        } else if (typedValue.type == TypedValue.TYPE_REFERENCE) {
            return AppResourceUtils.getDrawableWithResId(v.context, typedValue.resourceId)
        } else {
            throw UnsupportedOperationException("Failed to resolve attribute typedValue ${typedValue.toString()}")
        }


    }

    open fun getColor(v: View, typedValue: TypedValue): Int {
        if (typedValue.type == TypedValue.TYPE_ATTRIBUTE) {
            return AppResourceUtils.getColorWithAttr(v.context, typedValue.resourceId)
        } else if (typedValue.type == TypedValue.TYPE_REFERENCE) {
            return AppResourceUtils.getColorWithResId(v.context, typedValue.resourceId)
        } else {
            throw UnsupportedOperationException("Failed to resolve attribute typedValue ${typedValue.toString()}")
        }
    }




}