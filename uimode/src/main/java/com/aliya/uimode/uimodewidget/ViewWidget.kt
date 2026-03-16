package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

open class ViewWidget : AbstractWidget() {

    @CallSuper
    override fun onRegisterStyleable() {
        registerAttrArray(androidx.appcompat.R.styleable.ViewBackgroundHelper)
        registerAttrArray(R.styleable.UiModeView)
    }


    override fun assemble(view: View, attributeSet: AttributeSet): Boolean {
        return super.assemble(view, attributeSet)
    }
    override fun onAssemble(view: View, styleable: IntArray, indexInStyleable: Int,typedValue: TypedValue): Boolean {
        if(Arrays.equals(styleable, R.styleable.UiModeView) && indexInStyleable == R.styleable.UiModeView_android_theme){
            view.setTag(R.id.tag_ui_mode_theme_typed_value,typedValue)
            return true
        }
        return false
    }
    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {

        if (Arrays.equals(styleable, R.styleable.UiModeView)) {
            val indexCount = typedArray.length()
            var colorFilterColor: Int? = null
            var colorFilterMode: PorterDuff.Mode? = null
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
                        R.styleable.UiModeView_view_colorFilter -> {
                            colorFilterColor = TypedValueUtils.getColor(v, typedValue, this)
                        }

                        R.styleable.UiModeView_view_colorFilterMode -> {
                            colorFilterMode = TypedValueUtils.getPorterDuffMode(v, typedValue, this)
                        }

                    }
                }
            }
            colorFilterColor?.let { color ->
                val mode = colorFilterMode ?: PorterDuff.Mode.SRC_IN
                if(v is ImageView){
                    v.setColorFilter(color, mode)
                }else {
                    v.background?.setColorFilter(color, mode)
                    v.foreground?.setColorFilter(color, mode)
                }

            }
            return true
        }else if (Arrays.equals(styleable, androidx.appcompat.R.styleable.ViewBackgroundHelper)) {
            val indexCount = typedArray.length()
            var background = v.background
            var colorStateList: ColorStateList? = null
            for (i in 0 until indexCount) {
                val attr = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(attr)
                if (typedValue != null) {
                    when (attr) {
                        androidx.appcompat.R.styleable.ViewBackgroundHelper_android_background -> {
                            background = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }
                        androidx.appcompat.R.styleable.ViewBackgroundHelper_backgroundTint -> {

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

        }
        return false
    }


}