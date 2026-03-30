package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.core.ResourceNightModeChecker
import java.util.Arrays

open class TextViewWidget : AbstractWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.TextViewHelper)

    }

    override fun onAssemble(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        typedValue: TypedValue
    ): Boolean {
        return false
    }


    private fun applyTextAppearance(textView: TextView, typedArray: CachedTypedValueArray) {
        val indexCount = typedArray.length()
        if (indexCount > 0) {
            val typedValue =
                typedArray.peekValue(R.styleable.TextViewHelper_android_textAppearance)
            if (typedValue != null) {
                val style = TypedValueUtils.getStyle(textView, typedValue)
                if (style != 0) {
                    textView.setTextAppearance(style)
                }
            }
        }

    }

    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        val textView = v as TextView
        if (Arrays.equals(styleable, R.styleable.TextViewHelper)) {
            val indexCount = typedArray.length()


            var drawableLeft: Drawable? = null
            var drawableTop: Drawable? = null
            var drawableRight: Drawable? = null
            var drawableBottom: Drawable? = null
            var tintColorStateList: ColorStateList? = null
            for (i in 0 until indexCount) {
                val indeInAttrArray = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indeInAttrArray)
                if (typedValue != null) {
                    when (indeInAttrArray) {

                        R.styleable.TextViewHelper_android_textAppearance -> {


                        }

                        R.styleable.TextViewHelper_android_textColor -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                textView.setTextColor(it)
                            }

                        }

                        R.styleable.TextViewHelper_android_textColorHighlight -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                textView.highlightColor = it.defaultColor
                            }
                        }

                        R.styleable.TextViewHelper_android_textColorHint -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                textView.setHintTextColor(it)
                            }
                        }

                        R.styleable.TextViewHelper_android_textColorLink -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                textView.setLinkTextColor(it)
                            }
                        }

                        R.styleable.TextViewHelper_android_textCursorDrawable -> {
                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    textView.setTextCursorDrawable(it)
                                }
                            }
                        }


                        R.styleable.TextViewHelper_android_drawableLeft, R.styleable.TextViewHelper_drawableLeftCompat, R.styleable.TextViewHelper_android_drawableStart -> {
                            drawableLeft = drawableLeft ?: TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )
                        }

                        R.styleable.TextViewHelper_android_drawableTop, R.styleable.TextViewHelper_drawableTopCompat -> {
                            drawableTop = drawableTop ?: TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )
                        }

                        R.styleable.TextViewHelper_android_drawableRight, R.styleable.TextViewHelper_drawableRightCompat, R.styleable.TextViewHelper_android_drawableEnd -> {
                            drawableRight = drawableRight ?: TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )
                        }

                        R.styleable.TextViewHelper_android_drawableBottom, R.styleable.TextViewHelper_drawableBottomCompat -> {
                            drawableBottom = drawableBottom ?: TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )
                        }


                        R.styleable.TextViewHelper_drawableTint -> {
                            tintColorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )
                        }


                    }
                }
            }

            val compoundDrawablesRelative = textView.compoundDrawablesRelative
            val compoundDrawables = textView.compoundDrawables
            if (drawableLeft == null) {
                drawableLeft = compoundDrawables[0] ?: compoundDrawablesRelative[0]
            }
            if (drawableTop == null) {
                drawableTop = compoundDrawables[1] ?: compoundDrawablesRelative[1]
            }
            if (drawableRight == null) {
                drawableRight = compoundDrawables[2] ?: compoundDrawablesRelative[2]
            }
            if (drawableBottom == null) {
                drawableBottom = compoundDrawables[3] ?: compoundDrawablesRelative[3]
            }
            if (drawableLeft != null || drawableTop != null || drawableRight != null || drawableBottom != null) {
                textView.setCompoundDrawablesWithIntrinsicBounds(
                    drawableLeft,
                    drawableTop,
                    drawableRight,
                    drawableBottom
                )

            }

            if (tintColorStateList != null) {
                TextViewCompat.setCompoundDrawableTintList(textView, tintColorStateList)
            }
            return true
        }

        return false
    }

    override fun onInterceptPutCacheTypeValue(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        rawTypedValue: TypedValue,
        rawTypedArray: TypedArray,
        cachedTypedValueArray: CachedTypedValueArray
    ): Boolean {
        if (Arrays.equals(
                styleable,
                R.styleable.TextViewHelper
            )
        ) {
            when (indexInStyleable) {
                R.styleable.TextViewHelper_android_textAppearance -> {
                    return true
                }

                R.styleable.TextViewHelper_android_textColor -> {
                    if (isHexColorResourceType(rawTypedValue) || !ResourceNightModeChecker.hasNightModeResource(
                            view.context,
                            rawTypedValue.resourceId
                        )
                    ) {
                        return true
                    }
                }

                R.styleable.TextViewHelper_android_textColorHighlight -> {
                    if (isHexColorResourceType(rawTypedValue) || !ResourceNightModeChecker.hasNightModeResource(
                            view.context,
                            rawTypedValue.resourceId
                        )
                    ) {
                        return true
                    }
                }

                R.styleable.TextViewHelper_android_textColorHint -> {
                    if (isHexColorResourceType(rawTypedValue) || !ResourceNightModeChecker.hasNightModeResource(
                            view.context,
                            rawTypedValue.resourceId
                        )
                    ) {
                        return true
                    }
                }

                R.styleable.TextViewHelper_android_textColorLink -> {
                    if (isHexColorResourceType(rawTypedValue) || !ResourceNightModeChecker.hasNightModeResource(
                            view.context,
                            rawTypedValue.resourceId
                        )
                    ) {
                        return true
                    }
                }

                R.styleable.TextViewHelper_android_textCursorDrawable -> {
                    if (view !is EditText) {
                        return true
                    }
                }

            }

        }
        return super.onInterceptPutCacheTypeValue(
            view,
            styleable,
            indexInStyleable,
            rawTypedValue,
            rawTypedArray,
            cachedTypedValueArray
        )
    }

}