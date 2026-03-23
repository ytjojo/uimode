package com.aliya.uimode.uimodewidget

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aliya.uimode.R

class DrawableMutateHelper {


    companion object{

        fun mutate(view: View,drawable: Drawable?): Drawable? {
            if(view.getTag(R.id.tag_mutate_drawable) == true){
                return drawable?.mutate()
            }
            return drawable
        }
        fun mutateTargetDrawable(view: View) {
           if(view.getTag(R.id.tag_mutate_drawable) == true){
               if (view is ImageView) {
                   view.setImageDrawable(view.drawable.mutate())
               } else if (view is TextView) {
                   var isHasCompoundDrawable = false
                   view.getCompoundDrawablesRelative().forEach {
                       if (it != null) {
                           isHasCompoundDrawable = true
                       }
                   }
                   if (isHasCompoundDrawable) {
                       val mutates = view.getCompoundDrawablesRelative().map {
                           it?.mutate() ?: it
                       }
                       view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                           mutates[0],
                           mutates[1],
                           mutates[2],
                           mutates[3]
                       )
                   }

                   if (!isHasCompoundDrawable) {
                       view.compoundDrawables.forEach {
                           if (it != null) {
                               isHasCompoundDrawable = true
                           }
                       }
                       if (isHasCompoundDrawable) {
                           val mutates = view.compoundDrawables.map {
                               it?.mutate() ?: it
                           }
                           view.setCompoundDrawablesWithIntrinsicBounds(
                               mutates[0],
                               mutates[1],
                               mutates[2],
                               mutates[3]
                           )
                       }
                   }
                   if (!isHasCompoundDrawable) {
                       view.background = view.background?.mutate()
                       view.foreground = view.foreground?.mutate()
                   }

               } else {
                   view.background = view.background?.mutate()
                   view.foreground = view.foreground?.mutate()
               }
           }
        }
    }

}