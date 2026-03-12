package com.aliya.uimode.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

class AppUtil {


    companion object{


        /**
         * 通过 context 找到所依附的 Activity
         */
        @JvmStatic
        fun findActivity(context: Context?): Activity? {
            var contextWrapper = context
            while (contextWrapper is ContextWrapper) {
                if (contextWrapper is Activity) {
                    return contextWrapper
                }
                contextWrapper = contextWrapper.baseContext
            }
            return null
        }
    }
}