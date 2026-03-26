package com.aliya.uimode.uimodewidget

import android.util.AttributeSet
import android.view.View
import androidx.annotation.StyleRes
import com.aliya.uimode.core.CachedTypedValueArray

interface IApplyAttrResourceId {
    fun onApply(v: View,styleable: IntArray, typedArray: CachedTypedValueArray): Boolean

    fun onApplyCustom(v: View,typedArrayMap: Map<IntArray, CachedTypedValueArray>)
    /**
     * 组装主题的信息到View的TAG中。
     *
     * @param view         View
     * @param attributeSet View在Inflate时的Attribute.
     */
    fun assemble(view: View, attributeSet: AttributeSet):Boolean


    /**
     * View应用主题中的Style
     *
     * @param view
     * @param styleRes Style id
     */
    fun assembleStyle(view: View, @StyleRes styleRes: Int):Int

}