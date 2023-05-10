package com.aliya.uimode.factory

import android.os.Build
import android.view.View
import android.widget.*
import com.aliya.uimode.uimodewidget.*

object WidgetRegister {


    private val mCacheTypeRegister = HashMap<Class<*>, AbstractWidget>()

    private val mOnViewUiModeChangedMap = HashMap<Class<*>, OnViewUiModeChanged<*>>()

    private val mCustomStyleableMap = HashMap<Class<*>, LinkedHashSet<IntArray>>()



    fun <T:View> registerViewUiModeChanged(clazz: Class<T>,onViewUiModeChanged: OnViewUiModeChanged<T>){
        mOnViewUiModeChangedMap.put(clazz,onViewUiModeChanged)
    }

    fun <T:View> getViewUiModeChanged(clazz: Class<T>): OnViewUiModeChanged<T>?{
        return mOnViewUiModeChangedMap.get(clazz) as? OnViewUiModeChanged<T>?
    }

    fun isContains(clazz: Class<*>): Boolean {
        registerDefault()
        return mCacheTypeRegister.containsKey(clazz)
    }

    fun put(key: Class<*>, widget: AbstractWidget): AbstractWidget {
        registerDefault()
        widget.onRegisterStyleable()
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
                if (widget != null) {
                    return widget
                }
                superclass = superclass.superclass
            }
        }
        return null
    }

    fun getListBySuperclass(key: Class<*>): ArrayList<AbstractWidget> {
        registerDefault()
        val list = ArrayList<AbstractWidget>()
        if (View::class.java.isAssignableFrom(key)) {
            var superclass: Class<*>? = key
            while (superclass != null && View::class.java.isAssignableFrom(key)) {
                val widget = get(superclass)
                if (widget != null) {
                    list.add(widget)
                }
                superclass = superclass.superclass
            }
        }
        return list
    }

    private fun registerDefault() {
        if (mCacheTypeRegister.isNotEmpty()) {
            return
        }
        mCacheTypeRegister.put(View::class.java, ViewWidget())
        mCacheTypeRegister.put(TextView::class.java, TextViewWidget())
        mCacheTypeRegister.put(ImageView::class.java, ImageViewWidget())
        mCacheTypeRegister.put(ProgressBar::class.java, ProgressBarWidget())
        mCacheTypeRegister.put(SeekBar::class.java, SeekbarWidget())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCacheTypeRegister.put(Toolbar::class.java, ToolbarWidget())
        }
        mCacheTypeRegister.values.forEach {
            it.onRegisterStyleable()


        }

    }

}