package com.aliya.uimode.factory

import android.os.Build
import android.view.View
import android.widget.*
import com.aliya.uimode.uimodewidget.*

object WidgetRegister {


    private val mCacheTypeRegister = HashMap<Class<*>, AbstractWidget>()

    private val mOnUiModeChangedMap = HashMap<Class<*>, OnViewUiModeChanged<*>>()


    fun isContains(clazz: Class<*>): Boolean {
        registerDefault()
        return mCacheTypeRegister.containsKey(clazz)
    }

    fun put(key: Class<*>, widget: AbstractWidget): AbstractWidget {
        registerDefault()
        return mCacheTypeRegister.put(key, widget)!!
    }

    fun get(key: Class<*>): AbstractWidget? {
        registerDefault()
        return mCacheTypeRegister.get(key)
    }


    fun getBySuperclass(key: Class<*>): AbstractWidget? {
        registerDefault()
        if (View::class.java.isAssignableFrom(key)) {
            var superclass: Class<*>? = key
            while (superclass != null && View::class.java.isAssignableFrom(key)) {
                val widget = get(superclass)
                if(widget != null){
                    return widget
                }
                superclass = superclass.superclass
            }
        }
        return null
    }

    private fun registerDefault(){
        if(mCacheTypeRegister.isNotEmpty()){
            return
        }
        mCacheTypeRegister.put(View::class.java,ViewWidget())
        mCacheTypeRegister.put(TextView::class.java,TextViewWidget())
        mCacheTypeRegister.put(ImageView::class.java,ImageViewWidget())
        mCacheTypeRegister.put(ProgressBar::class.java,ProgressBarWidget())
        mCacheTypeRegister.put(SeekBar::class.java,SeekbarWidget())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCacheTypeRegister.put(Toolbar::class.java,ToolbarWidget())
        }
        mCacheTypeRegister.values.forEach{
            it.onRegisterStyleable()


        }

    }

}