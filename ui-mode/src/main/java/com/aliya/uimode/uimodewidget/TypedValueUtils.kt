package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.aliya.uimode.utils.DrawableCompatUtil

class TypedValueUtils {
    companion object {


        fun getDrawable(
            v: View,
            typedValue: TypedValue,
            abstractWidget: AbstractWidget
        ): Drawable? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    return DrawableCompatUtil.getDrawable(v.context, typedValue.resourceId)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (abstractWidget.validTheme(v)) {
                        val attrTypedValue =
                            abstractWidget.resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getDrawable(v, this, abstractWidget)
                        }
                    }
                }
                else -> {
                    if(typedValue.resourceId != 0){
                        return DrawableCompatUtil.getDrawable(v.context, typedValue.resourceId)
                    }else{
                        if (typedValue.type > TypedValue.TYPE_FIRST_INT && typedValue.type < TypedValue.TYPE_LAST_INT) {
                            return ColorDrawable(typedValue.data)
                        }
                    }

                }
            }
            return null
        }

        fun getColorStateList(
            v: View,
            typedValue: TypedValue,
            abstractWidget: AbstractWidget
        ): ColorStateList? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    return AppCompatResources.getColorStateList(v.context,typedValue.resourceId)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (abstractWidget.validTheme(v)) {
                        val attrTypedValue =
                            abstractWidget.resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getColorStateList(v, this, abstractWidget)
                        }
                    }
                }
                else -> {
                    if(typedValue.resourceId != 0){
                       return AppCompatResources.getColorStateList(v.context,typedValue.resourceId)
                    }else{
                        if (typedValue.type > TypedValue.TYPE_FIRST_INT && typedValue.type < TypedValue.TYPE_LAST_INT) {
                            return ColorStateList.valueOf(typedValue.data)
                        }
                    }

                }
            }
            return null
        }

        fun getStyle(
            v: View,
            typedValue: TypedValue,
            abstractWidget: AbstractWidget
        ): Int {
            when (typedValue.type) {
                TypedValue.TYPE_REFERENCE -> {
                    return typedValue.resourceId
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (abstractWidget.validTheme(v)) {
                        val attrTypedValue =
                            abstractWidget.resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getStyle(v, this, abstractWidget)
                        }
                    }
                }
                else -> {

                }
            }
            return 0
        }
    }
}