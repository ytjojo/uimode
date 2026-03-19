package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

open class ImageViewWidget : AbstractWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(androidx.appcompat.R.styleable.AppCompatImageView)
    }



    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        val imageView = v as ImageView
        if (Arrays.equals(styleable,androidx.appcompat.R.styleable.AppCompatImageView)) {
            val indexCount = typedArray.length()
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        androidx.appcompat.R.styleable.AppCompatImageView_android_src,androidx.appcompat.R.styleable.AppCompatImageView_srcCompat -> {

                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                imageView.setImageDrawable(it)
                            }
                        }
                        androidx.appcompat.R.styleable.AppCompatImageView_tint -> {
                           TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                               ImageViewCompat.setImageTintList(
                                   imageView,
                                   it
                               )
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