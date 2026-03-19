package com.aliya.uimode.uimodewidget

import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.core.widget.CompoundButtonCompat
import androidx.core.widget.ImageViewCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

class ButtonWidget : AbstractWidget() {

    override fun onRegisterStyleable() {
        super.onRegisterStyleable()
        registerAttrArray(R.styleable.ButtonHelper)
    }


    override fun onApply(v: View, styleable: IntArray, typedArray: CachedTypedValueArray): Boolean {
        val compoundButton = v as? CompoundButton ?: return false
        if (Arrays.equals(styleable, R.styleable.ButtonHelper)) {
            val indexCount = typedArray.length()
            for (i in 0 until indexCount) {
                val indexInStyleable = typedArray.getIndex(i)
                val typedValue = typedArray.peekValue(indexInStyleable)
                if (typedValue != null) {
                    when (indexInStyleable) {
                        R.styleable.ButtonHelper_android_button -> {
                            TypedValueUtils.getDrawable(
                                v,
                                typedValue,
                            )?.let {
                                compoundButton.buttonDrawable = it
                            }



                        }

                        R.styleable.ButtonHelper_android_buttonTintMode -> {
                        }

                        R.styleable.ButtonHelper_android_buttonTint -> {
                            TypedValueUtils.getColorStateList(
                                v,
                                typedValue,
                            )?.let {
                                CompoundButtonCompat.setButtonTintList(
                                    compoundButton,
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