package com.aliya.uimode.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.aliya.uimode.HideLog
import com.aliya.uimode.R
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.AppUtil
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

object ViewStore {

    private const val TAG = "ViewStore"
    private val mContextViewMap: MutableMap<Context, MutableSet<WeakReference<View>>> = HashMap()
    private val mActivityViewMap: MutableMap<Context, MutableSet<WeakReference<View>>> = HashMap()
    private val referenceQueue = ReferenceQueue<View>()

    private val uiModeChangeListenerMap =
        HashMap<LifecycleOwner, ArrayList<WeakReference<UiModeChangeListener>>>()
    const val NO_ID = 0 // 这里只能是0

    fun saveView(ctx: Context?, v: View?) {
        if (ctx == null || v == null) return
        if (v.getTag(R.id.tag_ui_mode_is_save_store) == true) return
        v.setTag(R.id.tag_ui_mode_is_save_store, true)
        // 寻找 context 装饰器对应的 activity 或 application
        if (ctx is Application) {
            putView2Map(ctx, v, mContextViewMap, referenceQueue)
        } else {
            val activity = AppUtil.findActivity(ctx)
            if (activity != null && !AppResourceUtils.isRecreateOnUiModeChange(activity)) {
                putView2Map(activity, v, mActivityViewMap, null)
            }
        }
    }
    fun removeView(ctx: Context?, v: View?) {
        if (ctx == null || v == null) return
        v.setTag(R.id.tag_ui_mode_is_save_store, null)
        // 寻找 context 装饰器对应的 activity 或 application
        if (ctx is Application) {
            mContextViewMap[ctx]?.let { set ->
                removeItemIf(set) { refer ->
                    refer.get() == v
                }
            }
        } else {
            val activity = AppUtil.findActivity(ctx)
            if (activity != null) {
                mActivityViewMap[activity]?.let { set ->
                    removeItemIf<WeakReference<View>>(set) { refer ->
                        refer.get() == v
                    }
                }
            }
        }
    }


    private fun <E> removeItemIf(
        collection: MutableCollection<E>,
        filter: (E) -> Boolean
    ): Boolean {
        var removed = false
        val each: MutableIterator<E> = collection.iterator()
        while (each.hasNext()) {
            if (filter.invoke(each.next())) {
                each.remove()
                removed = true
            }
        }
        return removed
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
        val startTime = SystemClock.elapsedRealtime()
        // 1、先执行Activity相关的View
        for ((key, value) in mActivityViewMap) {
            if (AppResourceUtils.isRecreateOnUiModeChange(key)) {
                // Activity#recreate()会调用，无需动态替换
                continue
            }
            val activityStartTime = SystemClock.elapsedRealtime()
            onApplyUiMode(value)
            val costTime = SystemClock.elapsedRealtime() - activityStartTime
            if (HideLog.isDebuggable()) {
                HideLog.d(
                    TAG,
                    "dispatchApplyUiMode: " + key.javaClass.simpleName + " costTime: ${costTime} ms: "
                )
            }
        }

        // 2、在执行ApplicationContext相关的View
        for ((_, value) in mContextViewMap) {
            onApplyUiMode(value)
        }
        ViewStore.dispatchUiModeChangeListener()
        if (HideLog.isDebuggable()) {
            HideLog.d(
                TAG,
                "dispatchApplyUiMode:  costTime: " + (SystemClock.elapsedRealtime() - startTime)
            )
        }
    }

    fun applyUiMode(activity: Activity) {
        if (!AppResourceUtils.isRecreateOnUiModeChange(activity)) { // 若Activity#recreate()会调用，无需动态替换
            onApplyUiMode(mActivityViewMap[activity])
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
        mActivityViewMap.remove(activity)?.clear()
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
            ref = referenceQueue.poll()
        }
    }


    fun registerUiModeChangeListener(
        lifecycleOwner: LifecycleOwner,
        listener: UiModeChangeListener
    ) {
        if (!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            return
        }
        val list = uiModeChangeListenerMap[lifecycleOwner]
        if (list == null) {
            uiModeChangeListenerMap[lifecycleOwner] = ArrayList()
            uiModeChangeListenerMap[lifecycleOwner]!!.add(WeakReference(listener))
        } else {
            list.add(WeakReference(listener))
        }
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(
                source: LifecycleOwner,
                event: Lifecycle.Event
            ) {
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        uiModeChangeListenerMap.remove(lifecycleOwner)?.clear()
                    }

                    else -> {

                    }
                }
            }

        })
    }

    fun dispatchUiModeChangeListener() {
        for ((_, value) in uiModeChangeListenerMap) {
            for (weakListener in value) {
                weakListener.get()?.onUiModeChange()
            }
        }
    }


}

