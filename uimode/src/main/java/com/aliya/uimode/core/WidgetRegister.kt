package com.aliya.uimode.core

import android.os.Build
import android.view.View
import android.widget.*
import com.aliya.uimode.uimodewidget.*

object WidgetRegister {


    private val mCacheTypeRegister = LinkedHashMap<Class<*>, AbstractWidget>()

    private val mOnViewUiModeChangedMap = HashMap<Class<*>, OnViewUiModeChanged<*>>()

    private val mCustomStyleableMap = HashMap<Class<*>, LinkedHashSet<IntArray>>()



    fun <T:View> registerViewUiModeChanged(clazz: Class<T>,onViewUiModeChanged: OnViewUiModeChanged<T>){
        mOnViewUiModeChangedMap.put(clazz,onViewUiModeChanged)
    }

    fun <T:View> getViewUiModeChanged(clazz: Class<T>): OnViewUiModeChanged<T>?{
        if (View::class.java.isAssignableFrom(clazz)) {
            var superclass: Class<*>? = clazz
            while (superclass != null && View::class.java.isAssignableFrom(clazz)) {
                val onViewUiModeChanged = mOnViewUiModeChangedMap.get(superclass) as? OnViewUiModeChanged<T>?
                if (onViewUiModeChanged != null) {
                    return onViewUiModeChanged
                }
                superclass = superclass.superclass
            }
        }
        return null
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
        mCacheTypeRegister.put(Toolbar::class.java, ToolbarWidget())
        mCacheTypeRegister.put(CompoundButton::class.java, ButtonWidget())
        mCacheTypeRegister.put(ListView::class.java, DividerWidget())
        mCacheTypeRegister.put(LinearLayout::class.java, DividerWidget())
        mCacheTypeRegister.values.forEach {
            it.onRegisterStyleable()


        }

    }

}