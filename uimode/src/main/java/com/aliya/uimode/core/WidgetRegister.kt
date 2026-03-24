package com.aliya.uimode.core

import android.view.View
import android.widget.*
import com.aliya.uimode.uimodewidget.*

object WidgetRegister {


    private val mCacheTypeRegister = LinkedHashMap<Class<*>,ArrayList<out AbstractWidget>>()

    private val mOnViewUiModeChangedMap = HashMap<Class<*>, OnViewCreateUiModeChanged<*>>()


    fun <T:View> registerViewCreateUiModeChanged(clazz: Class<T>, onViewUiModeChanged: OnViewCreateUiModeChanged<T>){
        mOnViewUiModeChangedMap.put(clazz,onViewUiModeChanged)
    }

    fun <T:View> getViewCreateUiModeChanged(clazz: Class<T>): ArrayList<OnViewCreateUiModeChanged<T>>?{
        val list = ArrayList<OnViewCreateUiModeChanged<T>>()
        if (View::class.java.isAssignableFrom(clazz)) {
            var superclass: Class<*>? = clazz
            while (superclass != null && View::class.java.isAssignableFrom(clazz)) {
                val onViewUiModeChanged = mOnViewUiModeChangedMap.get(superclass) as? OnViewCreateUiModeChanged<T>?
                if (onViewUiModeChanged != null) {
                    list.add(onViewUiModeChanged)
                }
                superclass = superclass.superclass
            }
        }
        return list
    }

    fun isContains(clazz: Class<*>): Boolean {
        registerDefault()
        return mCacheTypeRegister.containsKey(clazz)
    }

    fun put(key: Class<*>, widget: AbstractWidget): AbstractWidget {
        registerDefault()

        if (mCacheTypeRegister.containsKey(key)) {
            val list = mCacheTypeRegister.get(key) as ArrayList<AbstractWidget>
            list.add(widget)
        } else {
            val list = ArrayList<AbstractWidget>()
            list.add(widget)
            mCacheTypeRegister.put(key, list)
        }
        widget.onRegisterStyleable()
        return widget


    }

    fun get(key: Class<*>): ArrayList<out AbstractWidget>? {
        registerDefault()
        return mCacheTypeRegister.get(key)
    }

    fun getListBySuperclass(key: Class<*>): ArrayList<AbstractWidget> {
        registerDefault()
        val list = ArrayList<AbstractWidget>()
        if (View::class.java.isAssignableFrom(key)) {
            var superclass: Class<*>? = key
            while (superclass != null && View::class.java.isAssignableFrom(key)) {
                val widgets = get(superclass)
                if (widgets != null) {
                    list.addAll(widgets)
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
        mCacheTypeRegister.put(View::class.java, ArrayList<AbstractWidget>().apply { this.add( ViewWidget()) })
        mCacheTypeRegister.put(TextView::class.java, ArrayList<AbstractWidget>().apply { this.add(TextViewWidget())})
        mCacheTypeRegister.put(ImageView::class.java,  ArrayList<AbstractWidget>().apply { this.add(ImageViewWidget())})
        mCacheTypeRegister.put(ProgressBar::class.java,  ArrayList<AbstractWidget>().apply { this.add(ProgressBarWidget())})
        mCacheTypeRegister.put(SeekBar::class.java, ArrayList<AbstractWidget>().apply { this.add( SeekbarWidget())})
        mCacheTypeRegister.put(Toolbar::class.java, ArrayList<AbstractWidget>().apply { this.add( ToolbarWidget())})
        mCacheTypeRegister.put(CompoundButton::class.java,  ArrayList<AbstractWidget>().apply { this.add(ButtonWidget())})
        mCacheTypeRegister.put(ListView::class.java,  ArrayList<AbstractWidget>().apply { this.add(DividerWidget())})
        mCacheTypeRegister.put(LinearLayout::class.java,  ArrayList<AbstractWidget>().apply { this.add(DividerWidget())})
        mCacheTypeRegister.values.forEach {list->
            for (widget in list){
                widget.onRegisterStyleable()
            }



        }

    }

}