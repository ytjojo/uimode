package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.view.View
import android.widget.ProgressBar
import androidx.core.widget.ImageViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

open class ProgressBarWidget: AbstractWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.ProgressBarHelper)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        if (Arrays.equals(styleable,R.styleable.ProgressBarHelper)) {
            val indexCount = typedArray.length()
            val progressBar = v as? ProgressBar ?: return false
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.ProgressBarHelper_android_progressDrawable -> {
                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                                this
                            )?.let {
                                progressBar.progressDrawable = it
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