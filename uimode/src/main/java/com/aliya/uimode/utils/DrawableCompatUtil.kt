package com.aliya.uimode.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat

class DrawableCompatUtil {


    companion object {


        @JvmStatic
        fun getDrawable(context: Context, @DrawableRes id: Int): Drawable {
            return AppCompatResources.getDrawable(context, id)!!

        }

        @JvmStatic
        fun setDrawableMutate(imageView: ImageView, @DrawableRes id: Int): Drawable {
            val drawable = getDrawable(
                imageView.context,
                id
            )!!.mutate()
            imageView.setImageDrawable(
                drawable
            )
            return drawable

        }

        @JvmStatic
        fun setDrawable(imageView: ImageView, @DrawableRes id: Int): Drawable {
            val drawable = getDrawable(
                imageView.context,
                id
            )!!.mutate()
            imageView.setImageDrawable(
                drawable
            )
            return drawable

        }

        fun tint(drawable: Drawable, @ColorInt color: Int) {
            DrawableCompat.setTint(drawable, color)
        }
    }
}