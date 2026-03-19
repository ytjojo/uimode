package com.aliya.uimode.uimodewidget

import android.view.View
import android.widget.SeekBar
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

open class SeekbarWidget : AbstractWidget() {
    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.SeekBarHelper)
    }

    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        if (Arrays.equals(styleable, R.styleable.SeekBarHelper)) {
            val indexCount = typedArray.length()
            val seekBar = v as? SeekBar ?: return false
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.SeekBarHelper_android_thumb -> {
                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                seekBar.thumb = it
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