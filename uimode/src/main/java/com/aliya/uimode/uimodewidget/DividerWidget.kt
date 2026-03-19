package com.aliya.uimode.uimodewidget

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

class DividerWidget : ViewWidget() {


    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.DividerHelper)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        if (Arrays.equals(styleable,R.styleable.DividerHelper)) {
            val indexCount = typedArray.length()

            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.DividerHelper_android_divider -> {


                            if(v is ListView){
                                TypedValueUtils.getDrawable(
                                    v,
                                    typedValue,
                                )?.let {
                                    v.divider = it
                                }

                            }else if(v is LinearLayout){
                                TypedValueUtils.getDrawable(
                                    v,
                                    typedValue,
                                )?.let {
                                    v.dividerDrawable = it
                                }
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