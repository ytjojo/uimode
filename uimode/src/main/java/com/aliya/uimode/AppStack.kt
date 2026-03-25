package com.aliya.uimode

import android.app.Activity
import java.lang.ref.WeakReference
import java.util.Stack
import kotlin.concurrent.Volatile

/**
 * Activity栈集合
 *
 * @author a_liYa
 * @date 2017/6/28 15:58.
 */
internal object AppStack {
    @Volatile
    private var sStack: Stack<WeakReference<Activity>?>? = null

    fun pushActivity(activity: Activity?) {
        if (activity == null) return
        createStack()

        sStack!!.push(WeakReference<Activity>(activity))
    }

    fun removeActivity(activity: Activity?) {
        createStack()
        if (sStack != null && activity != null) {
            for (weakActivity in sStack!!) {
                if (weakActivity != null && weakActivity.get() === activity) {
                    sStack!!.remove(weakActivity)
                    break
                }
            }
        }
    }

    fun getAppStack(): ArrayList<Activity>? {
        createStack()
        val list = ArrayList<Activity>()
        for (activity in sStack!!) {
            if (activity != null) {
                list.add(activity.get()!!)
            }
        }
        return list
    }
    fun topActivity(): Activity? {
        createStack()
        return if (sStack != null && !sStack!!.empty()) {
            sStack!!.peek()?.get()
        } else null
    }

    private fun createStack() {
        if (sStack != null) {
            return
        }
        synchronized(AppStack::class.java) {
            if (sStack == null) {
                sStack = Stack<WeakReference<Activity>?>()
            }
        }
    }
}
