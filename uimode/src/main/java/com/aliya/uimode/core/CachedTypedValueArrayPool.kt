package com.aliya.uimode.core


import android.content.Context
import android.content.res.Resources
import com.aliya.uimode.UiModeManager
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * CachedTypedValueArray 对象池管理器
 *
 * 功能：
 * 1. 重用 CachedTypedValueArray 对象，避免重复创建
 * 2. 自动管理对象回收
 * 3. 线程安全的对象池
 * 4. 限制池大小，防止内存泄漏
 */
object CachedTypedValueArrayPool {
    const val DEFAULT_MAX_POOL_SIZE = 20

    /**
     * 对象池存储
     * Key: Resources (用于区分不同资源的数组)
     * Value: 可重用的 CachedTypedValueArray 列表
     */
    private val pool =  ArrayList<CachedTypedValueArray>()


    private var default : CachedTypedValueArray? = null

    fun getDefault(): CachedTypedValueArray? {
        if (default == null) {
            default = CachedTypedValueArray(UiModeManager.getAppContext()!!.resources, UiModeManager.getAppContext()!!)
        }
        return default
    }


    /**
     * 锁用于线程安全操作
     */
    private val lock = ReentrantLock()

    /**
     * 最大池大小
     */
    var maxPoolSize = DEFAULT_MAX_POOL_SIZE

    /**
     * 是否启用调试模式（跟踪未回收的对象）
     */
    var debugMode = false

    /**
     * 从池中获取或创建一个新的 CachedTypedValueArray
     *
     * @param resources Resources 对象
     * @param context Context 的弱引用
     * @return 可用的 CachedTypedValueArray 实例
     */
    fun obtain(resources: Resources,context :Context): CachedTypedValueArray {
        return lock.withLock {
            val key = resources

            // 尝试从池中获取
            if (!pool.isNullOrEmpty()) {
                val cachedArray = pool.removeAt(pool.size - 1)

                // 重置状态
                resetCachedArray(cachedArray)

                return@withLock cachedArray
            }

            // 池为空，创建新实例
            val newArray = CachedTypedValueArray(resources, WeakReference(context))



            newArray
        }
    }

    /**
     * 回收 CachedTypedValueArray 到对象池
     *
     * @param array 需要回收的 CachedTypedValueArray
     */
    fun recycle(array: CachedTypedValueArray?) {
        if (array == null) return

        lock.withLock {
            // 从使用中移除


            // 检查是否已经在池中，避免重复回收
            val availableList = pool

            if (availableList != null && availableList.contains(array)) {
                return@withLock // 已经在池中，无需重复回收
            }

            // 清理数据
            array.recycle()

            resetCachedArray(array)

            // 检查池大小
            if (availableList == null || availableList.size >= maxPoolSize) {
                // 池已满，不添加，让 GC 回收
                return@withLock
            }

            // 添加到池
            availableList.add(array)
        }
    }

    /**
     * 批量回收多个 CachedTypedValueArray
     */
    fun recycleAll(arrays: Collection<CachedTypedValueArray?>) {
        arrays.forEach { recycle(it) }
    }

    /**
     * 清空对象池
     * 通常在应用退出或配置改变时调用
     */
    fun clear() {
        lock.withLock {
            pool.forEach { array ->
                array.recycle()
            }
            pool.clear()
        }
    }

    /**
     * 重置 CachedTypedValueArray 的状态
     */
    private fun resetCachedArray(
        array: CachedTypedValueArray,
    ) {
        // 更新资源和上下文引用
        // 注意：由于 CachedTypedValueArray 的 resources 和 contextRef 是 val，
        // 我们需要确保重用的对象来自相同的 Resources

        // 清除数据（虽然 recycle() 已经做过，但为了安全再执行一次）
        array.recycle()
        UiModeManager.getAppContext()?.let {
            array.resources = it.resources
        }
    }

}