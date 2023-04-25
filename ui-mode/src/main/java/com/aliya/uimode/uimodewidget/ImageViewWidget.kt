package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.aliya.uimode.R

open class ImageViewWidget : ViewWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.AppCompatImageView)
    }

    override fun assemble(view: View, attributeSet: AttributeSet) {
        super.assemble(view, attributeSet)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: TypedArray): Boolean {
        val imageView = v as AppCompatImageView
        if (styleable.equals(R.styleable.AppCompatImageView)) {
            val indexCount = typedArray.indexCount
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.AppCompatImageView_android_src, R.styleable.AppCompatImageView_srcCompat -> {
                            imageView.setImageDrawable(
                                TypedValueUtils.getDrawable(
                                    v,
                                    typedValue,
                                    this
                                )
                            )
                        }
                        R.styleable.AppCompatImageView_tint -> {
                           val colorStateList = TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                                this
                            )
                            ImageViewCompat.setImageTintList(
                                imageView,
                                colorStateList
                            )
                        }

                    }
                }
            }
            return true
        }
        return super.onApply(v, styleable, typedArray)
    }




}