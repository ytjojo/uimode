package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.UiModeManager
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.widget.MaskImageView
import java.util.Arrays

open class ImageViewWidget : AbstractWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(androidx.appcompat.R.styleable.AppCompatImageView)
        registerCustomAttrArray(R.styleable.MaskImageView)
        registerCustomAttrArray(R.styleable.Round)
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



    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, CachedTypedValueArray>) {
        if(v is MaskImageView){
            if(typedArrayMap.containsKey(R.styleable.MaskImageView)){
                val cachedTypedValueArray = typedArrayMap[R.styleable.MaskImageView]
                if(cachedTypedValueArray != null){
                    v.onApplyUiModeChanged(R.styleable.MaskImageView,cachedTypedValueArray)
                }
            }else if(typedArrayMap.containsKey(R.styleable.Round)){
                val cachedTypedValueArray = typedArrayMap[R.styleable.Round]
                if(cachedTypedValueArray != null){
                    v.onApplyUiModeChanged(R.styleable.Round,cachedTypedValueArray)
                }
            }
        }

    }

    override fun onAssembleCustom(
        view: View,
        styleable: IntArray,
        typedArray: TypedArray,
        cachedTypedArray: CachedTypedValueArray
    ) {
        if(view is MaskImageView){
            view.onAssemble(styleable,cachedTypedArray)
        }

    }

}