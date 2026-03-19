package com.aliya.uimode.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import java.lang.reflect.Field


@Deprecated("SoonBlockedPrivateApi")
class LayoutInflaterHelper {

    companion object{

        const val FIELD_mFactorySet = "mFactorySet"

        @SuppressLint("SoonBlockedPrivateApi")
        fun setFactorySetFalse(inflater: LayoutInflater){

            val inflaterClass: Class<LayoutInflater> = LayoutInflater::class.java
            try {
                val mFactorySetField: Field = inflaterClass.getDeclaredField(FIELD_mFactorySet)
                mFactorySetField.setAccessible(true)
                mFactorySetField.setBoolean(inflater, false)

            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
        }


    }
}