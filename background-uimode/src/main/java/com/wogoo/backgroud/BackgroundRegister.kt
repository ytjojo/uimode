package com.wogoo.backgroud

import android.view.View
import com.aliya.uimode.core.WidgetRegister

class BackgroundRegister {

    companion object{

        @JvmStatic
        fun register(){
            WidgetRegister.put(View::class.java,BackgroundViewWidget())
        }

    }

}