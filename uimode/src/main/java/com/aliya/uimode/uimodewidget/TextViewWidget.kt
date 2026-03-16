package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.*

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

    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        val textView = v as TextView
        if (Arrays.equals(styleable, R.styleable.TextViewHelper)) {
            val indexCount = typedArray.length()
            if(indexCount > 0){
                val typedValue = typedArray.peekValue(R.styleable.TextViewHelper_android_textAppearance )
                if (typedValue != null) {
                    val style = TypedValueUtils.getStyle(v, typedValue, this)
                    if (style != 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.setTextAppearance(style)
                        }
                    }
                }
            }


            var drawableLeft:Drawable? = null
            var drawableTop:Drawable? = null
            var drawableRight:Drawable? = null
            var drawableBottom:Drawable? = null
            var tintColorStateList: ColorStateList? = null
            for (i in 0 until indexCount) {
                val indeInAttrArray = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indeInAttrArray)
                if (typedValue != null) {
                    when (indeInAttrArray) {

                        R.styleable.TextViewHelper_android_textAppearance -> {


                        }

                        R.styleable.TextViewHelper_android_textColor -> {
                            val colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )

                            colorStateList?.let {
                                textView.setTextColor(it)
                            }

                        }

                        R.styleable.TextViewHelper_android_textColorHighlight -> {
                            val colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                            colorStateList?.let {
                                textView.highlightColor = it.defaultColor
                            }
                        }

                        R.styleable.TextViewHelper_android_textColorHint -> {
                            val colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                            colorStateList?.let {
                                textView.setHintTextColor(it)
                            }
                        }

                        R.styleable.TextViewHelper_android_textColorLink -> {
                            val colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                            colorStateList?.let {
                                textView.setLinkTextColor(it)
                            }
                        }

                        R.styleable.TextViewHelper_android_textCursorDrawable -> {
                            val drawable = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                            drawable?.let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    textView.setTextCursorDrawable(it)
                                }
                            }
                        }


                        R.styleable.TextViewHelper_android_drawableLeft, R.styleable.TextViewHelper_drawableLeftCompat, R.styleable.TextViewHelper_android_drawableStart -> {
                            drawableLeft = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }
                        R.styleable.TextViewHelper_android_drawableTop, R.styleable.TextViewHelper_drawableTopCompat -> {
                            drawableTop = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }
                        R.styleable.TextViewHelper_android_drawableRight, R.styleable.TextViewHelper_drawableRightCompat, R.styleable.TextViewHelper_android_drawableEnd -> {
                            drawableRight = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }

                        R.styleable.TextViewHelper_android_drawableBottom, R.styleable.TextViewHelper_drawableBottomCompat -> {
                            drawableBottom = TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )
                        }


                        R.styleable.TextViewHelper_drawableTint -> {
                            tintColorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                        }


                    }
                }
            }


            if (drawableLeft != null || drawableTop != null || drawableRight != null || drawableBottom != null)
                if(drawableLeft == null){
                    drawableLeft = textView.compoundDrawablesRelative[0]
                }
                if(drawableTop == null){
                    drawableTop = textView.compoundDrawablesRelative[1]
                }
                if(drawableRight == null){
                    drawableRight = textView.compoundDrawablesRelative[2]
                }
                if(drawableBottom == null){
                    drawableBottom = textView.compoundDrawablesRelative[3]
                }
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawableLeft,
                    drawableTop,
                    drawableRight,
                    drawableBottom
                )

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
        typedValue: TypedValue,
        cachedTypedValueArray: CachedTypedValueArray
    ): Boolean {
        if(Arrays.equals(styleable, R.styleable.TextViewHelper) && indexInStyleable == R.styleable.TextViewHelper_android_textAppearance ){
            val styleRes = TypedValueUtils.getStyle(view, typedValue, this)
            if(styleRes != 0){
                styleable.forEachIndexed { index, attrResId ->
                    val cachedTypedValue = cachedTypedValueArray.peekValue( index)
                    if(cachedTypedValue == null){
                        val typedArray = view.context.obtainStyledAttributes(
                            styleRes, intArrayOf(
                                attrResId
                            )
                        )
                        if (typedArray.indexCount > 0) {
                            val typedValue = TypedValue()
                            if (typedArray.getValue(0, typedValue) && isLegalType(typedValue)) {
                                cachedTypedValueArray.putTypeValue(index, typedValue)
                            }

                        }
                        cachedTypedValueArray.putIndexAttr(index, index)
                        typedArray.recycle()
                    }

                }

            }
            return true
        }
        return super.onInterceptPutCacheTypeValue(
            view,
            styleable,
            indexInStyleable,
            typedValue,
            cachedTypedValueArray
        )
    }

}