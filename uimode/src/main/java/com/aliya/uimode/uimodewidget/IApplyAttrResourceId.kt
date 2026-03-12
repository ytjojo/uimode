package com.aliya.uimode.uimodewidget

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StyleRes

interface IApplyAttrResourceId {
    fun onApply(v: View,styleable: IntArray, typedArray: TypedArray): Boolean

    fun onApplyCustom(v: View,typedArrayMap: Map<IntArray, TypedArray>)
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
    fun applyStyle(view: View, @StyleRes styleRes: Int)

}