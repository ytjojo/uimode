package com.aliya.uimode.core

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater.Factory2
import android.view.View

/**
 *
 * 扩展 [LayoutInflater.setFactory2] 只能设置一个Factory的限制
 *
 * @author a_liYa
 * @date 2019/2/28 23:14.
 */
class FactoryMerger(private val before:ArrayList<Factory2> = ArrayList(), private val mAfter: Factory2) : Factory2 {
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        if (before.isNotEmpty()) {
            for (factory in before) {
                val v = factory.onCreateView(name, context, attrs)
                if (v != null) return v
            }
        }

        val v = mAfter.onCreateView(name, context, attrs)
        if (v != null) return v

        return null
    }

    fun addBeforeFactory(factory: Factory2) {
        if (before.contains(factory)) return
        if (mAfter == factory) return
        for(f in before) {
            if (f.javaClass == factory.javaClass) return
        }
        if (mAfter.javaClass == factory.javaClass) return
        before.add(factory)
    }
    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        if (before.isNotEmpty()) {
            for (factory in before) {
                val v = factory.onCreateView(parent, name, context, attrs)
                if (v != null) return v
            }
        }
        val v = mAfter.onCreateView(parent, name, context, attrs)
        if (v != null) return v

        return null
    }
}
