package com.aliya.uimode.core

import android.view.View
import android.widget.*
import com.aliya.uimode.R
import com.aliya.uimode.uimodewidget.*

object WidgetRegister {


    private val mTypeWidgetRegisterMap = LinkedHashMap<Class<*>, ArrayList<out AbstractWidget>>()


    private val mTypeWidgetCachedMap = HashMap<Class<*>, ArrayList<out AbstractWidget>?>()

    private val mOnViewUiModeChangedRegisterMap = HashMap<Class<*>, OnViewCreateUiModeChanged<*>>()


    private val mOnViewUiModeChangedCached =
        HashMap<Class<*>, ArrayList<OnViewCreateUiModeChanged<*>>?>()


    /**
     * 注册 OnViewCreateUiModeChanged
     *
     * @param clazz view 类
     * @param onViewUiModeChanged OnViewCreateUiModeChanged
     */
    fun <T : View> registerViewCreateUiModeChanged(
        clazz: Class<T>,
        onViewUiModeChanged: OnViewCreateUiModeChanged<T>
    ) {
        mOnViewUiModeChangedRegisterMap.put(clazz, onViewUiModeChanged)
    }

    /**
     * 获取 view 相关 OnViewCreateUiModeChanged 列表
     *
     * @param clazz view 类
     * @return widget 列表
     */
    fun <T : View> getViewCreateUiModeChanged(clazz: Class<T>): ArrayList<OnViewCreateUiModeChanged<T>>? {

        if (mOnViewUiModeChangedCached.containsKey(clazz)) {
            return mOnViewUiModeChangedCached.get(clazz) as? ArrayList<OnViewCreateUiModeChanged<T>>?
        }
        val list = ArrayList<OnViewCreateUiModeChanged<T>>()
        if (mOnViewUiModeChangedRegisterMap.isNotEmpty()) {
            var superclass: Class<*>? = clazz
            while (superclass != null) {
                val onViewUiModeChanged =
                    mOnViewUiModeChangedRegisterMap.get(superclass) as? OnViewCreateUiModeChanged<T>?
                if (onViewUiModeChanged != null) {
                    list.add(onViewUiModeChanged)
                }
                if (superclass == View::class.java) {
                    break
                }
                superclass = superclass.superclass
            }
        }
        if (list.isNotEmpty()) {
            mOnViewUiModeChangedCached.put(clazz, list as ArrayList<OnViewCreateUiModeChanged<*>>)
        } else {
            mOnViewUiModeChangedCached.put(clazz, null)
        }
        return list
    }


    /**
     * 获取 view 相关 OnViewCreateUiModeChanged 列表
     *
     * @param view view
     * @return widget 列表
     */
    fun <T : View> getViewCreateUiModeChanged(view: View): ArrayList<OnViewCreateUiModeChanged<T>>? {
        val clazz = view.javaClass
        if (mOnViewUiModeChangedCached.containsKey(clazz)) {
            return mOnViewUiModeChangedCached.get(clazz) as? ArrayList<OnViewCreateUiModeChanged<T>>?
        }
        val list = ArrayList<OnViewCreateUiModeChanged<T>>()
        if (mOnViewUiModeChangedRegisterMap.isNotEmpty()) {
            mOnViewUiModeChangedRegisterMap.forEach { t, u ->
                if (t.isAssignableFrom(clazz)) {
                    list.add(u as OnViewCreateUiModeChanged<T>)
                }
            }
        }
        if (list.isNotEmpty()) {
            mOnViewUiModeChangedCached.put(clazz, list as ArrayList<OnViewCreateUiModeChanged<*>>)
        } else {
            mOnViewUiModeChangedCached.put(clazz, null)
        }
        return list
    }

    fun isContains(clazz: Class<*>): Boolean {
        registerDefault()
        return mTypeWidgetRegisterMap.containsKey(clazz)
    }

    /**
     * 注册 widget
     *
     * @param key widget 类
     * @param widget widget
     * @return widget
     */
    fun put(key: Class<*>, widget: AbstractWidget): AbstractWidget {
        registerDefault()

        if (mTypeWidgetRegisterMap.containsKey(key)) {
            val list = mTypeWidgetRegisterMap.get(key) as ArrayList<AbstractWidget>
            list.add(widget)
        } else {
            val list = ArrayList<AbstractWidget>()
            list.add(widget)
            mTypeWidgetRegisterMap.put(key, list)
        }
        widget.onRegisterStyleable()
        return widget


    }

    fun get(key: Class<*>): ArrayList<out AbstractWidget>? {
        registerDefault()
        return mTypeWidgetRegisterMap.get(key)
    }

    /**
     * 获取 view 相关 widget 列表
     *
     * @param key view 类
     * @return widget 列表
     */
    fun getListBySuperclass(key: Class<*>): ArrayList<AbstractWidget>? {
        registerDefault()
        if (mTypeWidgetCachedMap.containsKey(key)) {
            return mTypeWidgetCachedMap.get(key) as? ArrayList<AbstractWidget>?
        }
        val list = (mTypeWidgetCachedMap.get(key) as? ArrayList<AbstractWidget>?)
            ?: ArrayList<AbstractWidget>()
        if (list.isNotEmpty()) {
            return list
        }
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
        if (list.isNotEmpty()) {
            mTypeWidgetCachedMap.put(key, list)
        } else {
            mTypeWidgetCachedMap.put(key, null)
        }
        return list
    }

    /**
     * 获取 view 相关 widget 列表
     *
     * @param view view
     * @return widget 列表
     */
    fun getWidgetList(view: View): ArrayList<AbstractWidget>? {

        val list = (view.getTag(R.id.tag_ui_mode_view_widget_list) as? ArrayList<AbstractWidget>)
            ?: getListBySuperclass(view.javaClass)
        view.setTag(R.id.tag_ui_mode_view_widget_list, list)
        return list
    }

    /**
     * 注册默认 widget
     */
    private fun registerDefault() {
        if (mTypeWidgetRegisterMap.isNotEmpty()) {
            return
        }
        mTypeWidgetRegisterMap.put(
            View::class.java,
            ArrayList<AbstractWidget>().apply { this.add(ViewWidget()) })
        mTypeWidgetRegisterMap.put(
            TextView::class.java,
            ArrayList<AbstractWidget>().apply { this.add(TextViewWidget()) })
        mTypeWidgetRegisterMap.put(
            ImageView::class.java,
            ArrayList<AbstractWidget>().apply { this.add(ImageViewWidget()) })
        mTypeWidgetRegisterMap.put(
            ProgressBar::class.java,
            ArrayList<AbstractWidget>().apply { this.add(ProgressBarWidget()) })
        mTypeWidgetRegisterMap.put(
            SeekBar::class.java,
            ArrayList<AbstractWidget>().apply { this.add(SeekbarWidget()) })
        mTypeWidgetRegisterMap.put(
            Toolbar::class.java,
            ArrayList<AbstractWidget>().apply { this.add(ToolbarWidget()) })
        mTypeWidgetRegisterMap.put(
            CompoundButton::class.java,
            ArrayList<AbstractWidget>().apply { this.add(ButtonWidget()) })
        mTypeWidgetRegisterMap.put(
            ListView::class.java,
            ArrayList<AbstractWidget>().apply { this.add(DividerWidget()) })
        mTypeWidgetRegisterMap.put(
            LinearLayout::class.java,
            ArrayList<AbstractWidget>().apply { this.add(DividerWidget()) })
        mTypeWidgetRegisterMap.values.forEach { list ->
            for (widget in list) {
                widget.onRegisterStyleable()
            }


        }

    }

}