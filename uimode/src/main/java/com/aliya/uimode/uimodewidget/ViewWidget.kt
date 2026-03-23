package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

open class ViewWidget : AbstractWidget() {

    @CallSuper
    override fun onRegisterStyleable() {
        registerAttrArray(R.styleable.UiModeView)
        registerAttrArray(androidx.appcompat.R.styleable.ViewBackgroundHelper)
        registerAttrArray(R.styleable.UiModeViewEffectsStyle)
    }


    override fun assemble(view: View, attributeSet: AttributeSet): Boolean {
        return super.assemble(view, attributeSet)
    }

    override fun onAssemble(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        typedValue: TypedValue
    ): Boolean {
        if (Arrays.equals(
                styleable,
                R.styleable.UiModeView
            ) && indexInStyleable == R.styleable.UiModeView_android_theme
        ) {
            view.setTag(R.id.tag_ui_mode_theme_typed_value, typedValue)
            return true
        }
        return false
    }

    override fun onApply(
        view: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray
    ): Boolean {

        if (Arrays.equals(styleable, R.styleable.UiModeView)) {
            val indexCount = typedArray.length()
            for (i in 0 until indexCount) {
                val indexInAttrArray = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInAttrArray)
                if (typedValue != null) {
                    when (indexInAttrArray) {
                        R.styleable.UiModeView_android_foreground -> {
                            TypedValueUtils.getDrawable(
                                view,
                                typedValue,
                            )?.let {
                                view.foreground = it
                            }
                        }

                        R.styleable.UiModeView_view_invalidate -> {
                            view.invalidate()
                        }

                        R.styleable.UiModeView_android_theme -> {
                            val style = TypedValueUtils.getStyle(view, typedValue)
                            if (style != 0) {
                                view.getContext().getTheme()?.applyStyle(style, true)
                            }
                        }


                    }
                }
            }
            return true
        } else if (Arrays.equals(styleable, R.styleable.UiModeViewEffectsStyle)) {

            val indexCount = typedArray.length()
            var colorFilterColor: Int? = null
            var colorFilterMode: PorterDuff.Mode? = null
            for (i in 0 until indexCount) {
                val indexInAttrArray = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInAttrArray)
                if (typedValue != null) {
                    when (indexInAttrArray) {

                        R.styleable.UiModeViewEffectsStyle_drawable_colorFilter -> {
                            colorFilterColor = TypedValueUtils.getColor(view, typedValue)
                        }

                        R.styleable.UiModeViewEffectsStyle_mutate_drawable -> {
                            val isMutate = typedArray.getBoolean(indexInAttrArray, false)
                            if (isMutate) {
                                view.setTag(R.id.tag_mutate_drawable, true)
                                DrawableMutateHelper.mutateTargetDrawable(view)
                            }
                        }

                        R.styleable.UiModeViewEffectsStyle_drawable_colorFilterMode -> {
                            colorFilterMode =
                                TypedValueUtils.getPorterDuffMode(view, typedValue, this)
                        }

                        R.styleable.UiModeViewEffectsStyle_view_alpha -> {
                            view.alpha = typedArray.getFloat(indexInAttrArray, 1f)
                        }

                    }
                }
            }
            colorFilterColor?.let { color ->
                val mode = colorFilterMode ?: PorterDuff.Mode.SRC_IN
                if (view is ImageView) {
                    view.setImageDrawable(view.drawable.mutate())
                    view.setColorFilter(color, mode)
                } else if (view is TextView) {
                    var isHasCompoundDrawable = false

                    val mutates = view.getCompoundDrawablesRelative().map {
                        if (it != null) {
                            val mutateDrawable = it.mutate()
                            isHasCompoundDrawable = true
                            mutateDrawable.setColorFilter(color, mode)
                            mutateDrawable
                        }else{
                            null
                        }
                    }

                    if (isHasCompoundDrawable) {
                        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            mutates[0],
                            mutates[1],
                            mutates[2],
                            mutates[3]
                        )
                    }

                    if (!isHasCompoundDrawable) {
                        val mutates =  view.compoundDrawables.map {
                            if (it != null) {
                                val mutateDrawable = it.mutate()
                                isHasCompoundDrawable = true
                                mutateDrawable.setColorFilter(color, mode)
                                mutateDrawable
                            }else{
                                null
                            }
                        }
                        if (isHasCompoundDrawable) {
                            view.setCompoundDrawablesWithIntrinsicBounds(
                                mutates[0],
                                mutates[1],
                                mutates[2],
                                mutates[3]
                            )
                        }
                    }
                    if (!isHasCompoundDrawable) {
                        view.background = view.background?.mutate()
                        view.background?.setColorFilter(color, mode)
                        view.foreground = view.foreground?.mutate()
                        view.foreground?.setColorFilter(color, mode)
                    }

                } else {
                    view.background = view.background?.mutate()
                    view.background?.setColorFilter(color, mode)
                    view.foreground = view.foreground?.mutate()
                    view.foreground?.setColorFilter(color, mode)
                }
            }
            return true
        } else if (Arrays.equals(styleable, androidx.appcompat.R.styleable.ViewBackgroundHelper)) {
            val indexCount = typedArray.length()
            var background = view.background
            var colorStateList: ColorStateList? = null
            for (i in 0 until indexCount) {
                val attr = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(attr)
                if (typedValue != null) {
                    when (attr) {
                        androidx.appcompat.R.styleable.ViewBackgroundHelper_android_background -> {

                            background = TypedValueUtils.getDrawable(
                                view,
                                typedValue,
                            )
                        }

                        androidx.appcompat.R.styleable.ViewBackgroundHelper_backgroundTint -> {

                            colorStateList = TypedValueUtils.getColorStateList(
                                view,
                                typedValue,
                            )
                        }

                    }
                }
            }
            if (background != null && background != view.background) {
                view.background = background
            }
            colorStateList?.let {
                ViewCompat.setBackgroundTintList(view, it)
            }

            return true

        }
        return false
    }

    override fun onInterceptApplyWhenOnAssemble(
        view: View,
        styleable: IntArray,
        typedArray: CachedTypedValueArray
    ): Boolean {
        if (Arrays.equals(styleable, R.styleable.UiModeViewEffectsStyle)) {
            return false
        }
        return super.onInterceptApplyWhenOnAssemble(view, styleable, typedArray)
    }


}