package com.aliya.uimode.uimodewidget

import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.DrawableCompatUtil

class TypedValueUtils {
    companion object {

        /**
         * 校验 Theme 是否为null
         *
         * @param v a view
         * @return true : 不为null
         */
        fun validTheme(v: View): Boolean {
            return getTheme(v) != null
        }

        /**
         * 从 view 获取 theme
         *
         * @param v a view
         * @return theme
         */
        fun getTheme(v: View): Theme? {
            return v.context.theme
        }

        /**
         * 检索主题中属性的值
         *
         * @param v           a view.
         * @param resId       The resource identifier of the desired theme attribute.
         * @return boolean Returns true if the attribute was found and <var>outValue</var>
         * is valid, else false.
         * @see android.content.res.Resources.Theme.resolveAttribute
         */
        fun resolveAttribute(v: View, resId: Int): TypedValue? {
            val typedValue = AppResourceUtils.getTypedValue()
            val success = v.context.theme?.resolveAttribute(resId, typedValue, true)
            if (success == true) {
                return typedValue
            }
            return null
        }

        fun getDrawable(
            v: View,
            typedValue: TypedValue,
        ): Drawable? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    return DrawableCompatUtil.getDrawable(v.context, typedValue.resourceId)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (validTheme(v)) {
                        val attrTypedValue =
                            resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getDrawable(v, this)
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
        ): ColorStateList? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    return AppCompatResources.getColorStateList(v.context,typedValue.resourceId)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (validTheme(v)) {
                        val attrTypedValue =
                            resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getColorStateList(v, this)
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
        ): Int {
            when (typedValue.type) {
                TypedValue.TYPE_REFERENCE -> {
                    return typedValue.resourceId
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (v.context.theme != null) {
                        val attrTypedValue =
                            resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getStyle(v, this)
                        }
                    }
                }
                else -> {

                }
            }
            return 0
        }


        fun getColor(
            v: View,
            typedValue: TypedValue,
        ): Int? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    return ContextCompat.getColor(v.context, typedValue.resourceId)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (validTheme(v)) {
                        val attrTypedValue =
                            resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getColor(v, this)
                        }
                    }
                }
                else -> {
                    if(typedValue.resourceId != 0){
                        return ContextCompat.getColor(v.context, typedValue.resourceId)
                    }else{
                        if (typedValue.type > TypedValue.TYPE_FIRST_INT && typedValue.type < TypedValue.TYPE_LAST_INT) {
                            return typedValue.data
                        }
                    }

                }
            }
            return null
        }



        fun getPorterDuffMode(
            v: View,
            typedValue: TypedValue,
            abstractWidget: AbstractWidget
        ): PorterDuff.Mode? {
            when (typedValue.type) {
                TypedValue.TYPE_STRING -> {
                    val modeName = v.context.getString(typedValue.resourceId)
                    return parsePorterDuffMode(modeName)
                }
                TypedValue.TYPE_ATTRIBUTE -> {
                    if (validTheme(v)) {
                        val attrTypedValue =
                            resolveAttribute(v, typedValue.resourceId)
                        attrTypedValue?.apply {
                            return getPorterDuffMode(v, this, abstractWidget)
                        }
                    }
                }
                else -> {
                    if(typedValue.resourceId != 0){
                        val modeName = v.context.getString(typedValue.resourceId)
                        return parsePorterDuffMode(modeName)
                    }else{
                        if (typedValue.type > TypedValue.TYPE_FIRST_INT && typedValue.type < TypedValue.TYPE_LAST_INT) {
                            return parsePorterDuffModeFromInt(typedValue.data)
                        }
                    }

                }
            }
            return null
        }

        private fun parsePorterDuffMode(modeName: String): PorterDuff.Mode? {
            return when (modeName.lowercase()) {
                "clear" -> PorterDuff.Mode.CLEAR
                "src" -> PorterDuff.Mode.SRC
                "dst" -> PorterDuff.Mode.DST
                "src_over" -> PorterDuff.Mode.SRC_OVER
                "dst_over" -> PorterDuff.Mode.DST_OVER
                "src_in" -> PorterDuff.Mode.SRC_IN
                "dst_in" -> PorterDuff.Mode.DST_IN
                "src_out" -> PorterDuff.Mode.SRC_OUT
                "dst_out" -> PorterDuff.Mode.DST_OUT
                "src_atop" -> PorterDuff.Mode.SRC_ATOP
                "dst_atop" -> PorterDuff.Mode.DST_ATOP
                "xor" -> PorterDuff.Mode.XOR
                "darken" -> PorterDuff.Mode.DARKEN
                "lighten" -> PorterDuff.Mode.LIGHTEN
                "multiply" -> PorterDuff.Mode.MULTIPLY
                "screen" -> PorterDuff.Mode.SCREEN
                "add" -> PorterDuff.Mode.ADD
                "overlay" -> PorterDuff.Mode.OVERLAY
                else -> null
            }
        }

        private fun parsePorterDuffModeFromInt(value: Int): PorterDuff.Mode? {

            return when (value) {
                0 -> PorterDuff.Mode.CLEAR
                1 -> PorterDuff.Mode.SRC
                2 -> PorterDuff.Mode.DST
                3 -> PorterDuff.Mode.SRC_OVER
                4 -> PorterDuff.Mode.DST_OVER
                5 -> PorterDuff.Mode.SRC_IN
                6 -> PorterDuff.Mode.DST_IN
                7 -> PorterDuff.Mode.SRC_OUT
                8 -> PorterDuff.Mode.DST_OUT
                9 -> PorterDuff.Mode.SRC_ATOP
                10 -> PorterDuff.Mode.DST_ATOP
                11 -> PorterDuff.Mode.XOR
                12 -> PorterDuff.Mode.ADD
                13 -> PorterDuff.Mode.MULTIPLY
                14 -> PorterDuff.Mode.SCREEN
                15 -> PorterDuff.Mode.OVERLAY
                16 -> PorterDuff.Mode.DARKEN
                17 -> PorterDuff.Mode.LIGHTEN
                else -> null
            }
        }
    }
}