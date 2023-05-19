package com.aliya.uimode.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import com.aliya.uimode.R
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.AppUtil
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

object ViewStore {


    private val mContextViewMap: MutableMap<Context, MutableSet<WeakReference<View>>> = HashMap()
    private val mActivityViewMap: MutableMap<Context, MutableSet<WeakReference<View>>> = HashMap()
    private val referenceQueue = ReferenceQueue<View>()
    const val NO_ID = 0 // 这里只能是0

    fun saveView(ctx: Context?, v: View?) {
        if (ctx == null || v == null) return
        v.setTag(R.id.tag_ui_mode_is_save_store,true)
        // 寻找 context 装饰器对应的 activity 或 application
        if (ctx is Application) {
            putView2Map(ctx, v, mContextViewMap, referenceQueue)
        } else {
            val activity = AppUtil.findActivity(ctx)
            if (activity != null) {
                putView2Map(activity, v, mActivityViewMap, null)
            }
        }
    }

    private fun putView2Map(
        ctx: Context, v: View, map: MutableMap<Context, MutableSet<WeakReference<View>>>,
        queue: ReferenceQueue<View>?
    ) {
        var weakViewSet = map[ctx]
        if (weakViewSet == null) {
            weakViewSet = HashSet()
            map[ctx] = weakViewSet
        }
        weakViewSet.add(if (queue == null) WeakReference(v) else WeakReference(v, queue))
    }


    /**
     * 分发执行全部 view UiMode
     *
     * @param policy apply 策略
     */
    fun dispatchApplyUiMode() {
        // 1、先执行Activity相关的View
        for ((key, value) in mActivityViewMap) {
            if (AppResourceUtils.isRecreateOnUiModeChange(key)) {
                // Activity#recreate()会调用，无需动态替换
                continue
            }
            onApplyUiMode(value)
        }

        // 2、在执行ApplicationContext相关的View
        for ((_, value) in mContextViewMap) {
            onApplyUiMode(value)
        }
    }

    fun applyUiMode(activity: Activity) {
        if (!AppResourceUtils.isRecreateOnUiModeChange(activity)) { // 若Activity#recreate()会调用，无需动态替换
            onApplyUiMode( mActivityViewMap[activity])
        }
    }


    /**
     * apply UiMode to Sets View
     *
     * @param policy      apply 策略
     * @param weakViewSet view sets WeakReference
     */
    private fun onApplyUiMode(weakViewSet: MutableSet<WeakReference<View>>?) {
        if (weakViewSet != null) {
            val iterator = weakViewSet.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next != null) {
                    val view = next.get()
                    if (view != null) {
                        UiModeDelegate.onUiModeChanged(view)
                    } else {
                        iterator.remove()
                    }
                }
            }
        }
    }


    fun removeUselessViews(activity: Activity) {
        mActivityViewMap.remove(activity)
        clearUselessContextViews()
    }

    private fun clearUselessContextViews() {
        var ref = referenceQueue.poll()
        while (ref != null) {
            val ketSet = mContextViewMap.keys
            val iterator = ketSet.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                val weakSet =
                    mContextViewMap[next]!!
                weakSet.remove(ref)
                if (weakSet.isEmpty()) { // value Set is empty, remove it.
                    iterator.remove()
                }
            }
            referenceQueue.poll()
        }
    }

}