package com.aliya.uimode.uimodewidget

import android.content.Context
import com.aliya.uimode.core.CachedTypedValueArray
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import com.aliya.uimode.R
import com.aliya.uimode.core.UiModeChangeListener
import com.aliya.uimode.core.ViewStore
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.AppUtil
import java.lang.ref.WeakReference


abstract class AbstractWidget : IApplyAttrResourceId {

    var activityTheme = 0
        get() {
            if (field == 0) {
                return appTheme
            } else {
                return field
            }
        }

    var appTheme = 0

    companion object {
        const val TAG = "AbstractWidget"

        const val IGNORE_ATTR_NAME = "uiMode_ignore"
        const val ATTR_NAME_textAppearance = "textAppearance"
    }

    /**
     * 主题元素集合
     */
    protected val mThemeElementKeySet: MutableSet<Int> = HashSet()

    private val mStyleableKeySet: HashSet<IntArray> = HashSet()

    private val mCustomStyleableKeySet: HashSet<IntArray> = HashSet()

    init {
    }

    open fun onRegisterStyleable() {

    }


    @CallSuper
    open fun onInterceptPutCacheTypeValue(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        typedValue: TypedValue,
        typedArray: TypedArray,
        cachedTypedValueArray: CachedTypedValueArray,
    ): Boolean {

        return false
    }

    @CallSuper
    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, CachedTypedValueArray>) {
    }


    override fun assemble(view: View, attributeSet: AttributeSet): Boolean {
        var ignoreValue = ""
        val ignoreAttrNames = HashSet<String>()
        val ignoreAttrIds = HashSet<Int>()
        val attrValueMap = HashMap<String, Int>()
        val N: Int = attributeSet.getAttributeCount()
        val styleResId = attributeSet.styleAttribute
        var isSave = false
        if (styleResId != 0) {
            ViewStore.setViewStyleTag(view, styleResId)
        }
        if (view is UiModeChangeListener) {
            isSave = true
        }
        for (i in 0 until N) {
            val attrName: String = attributeSet.getAttributeName(i)
            if (IGNORE_ATTR_NAME == attrName) {
                ignoreValue = attributeSet.getAttributeValue(i)
                if (ignoreValue.isEmpty()) {
                    return false
                }
            } else {
                val attrValueId = parseAttrId(attributeSet.getAttributeValue(i))
                if (attrValueId != 0) {
//                    val attr = AppResourceUtils.getAttrId(view.context, attrName)
                    attrValueMap.put(attrName, attrValueId)
                }
            }


        }


        if (mStyleableKeySet.isNotEmpty()) {

            if (!TextUtils.isEmpty(ignoreValue)) {
                val ignores: Array<String> = ignoreValue.split("\\|".toRegex()).toTypedArray()
                for (ignore in ignores) {
                    var attrName = ignore
                    if (!TextUtils.isEmpty(ignore.trim { it <= ' ' }.also { attrName = it })) {
                        if (attrName == "style") {
                            view.setTag(R.id.tag_ui_mode_widget_style, null)
                            ViewStore.clearViewStyleTag(view)
                            continue
                        }
                        ignoreAttrNames.add(attrName)
                    }
                }
            }
            ignoreAttrNames.forEach {
                ignoreAttrIds.add(AppResourceUtils.getAttrId(view.context, it))
            }

            val cachedTypedArrayMap = ViewStore.getCreateIfNullCachedTypedArrayMap(view)
            val isHasViewStyleTag = ViewStore.hasViewStyleTag(view)

            for (styleable in mStyleableKeySet) {
                val typedArray =
                    view.context.obtainStyledAttributes(attributeSet, styleable, 0, if(isHasViewStyleTag) styleResId else 0)
                val indexCount = typedArray.indexCount
                if (indexCount > 0) {
                    val cachedTypedArray = cachedTypedArrayMap.get(styleable)?:
                    CachedTypedValueArray(view.resources, WeakReference(view.context))
                    for (i in 0 until indexCount) {
                        val indexInStyleable = typedArray.getIndex(i)
                        val typedValue = TypedValue()
                        if (typedArray.getValue(indexInStyleable, typedValue) && isLegalType(
                                typedValue
                            ) &&
                            !ignoreAttrIds.contains(styleable[indexInStyleable]) && !onAssemble(
                                view,
                                styleable,
                                indexInStyleable,
                                typedValue
                            )
                        ) {
                            if (attrValueMap.isNotEmpty()) {
                                val attrName =
                                    view.context.resources.getResourceName(styleable[indexInStyleable])
                                        .split('/')[1]
                                if (attrValueMap.containsKey(attrName)) {
                                    typedValue.type = TypedValue.TYPE_ATTRIBUTE
                                    typedValue.resourceId = attrValueMap[attrName] ?: 0
                                }
                            }
                            if (!onInterceptPutCacheTypeValue(
                                    view,
                                    styleable,
                                    indexInStyleable,
                                    typedValue,
                                    typedArray,
                                    cachedTypedArray
                                )
                            ) {
                                cachedTypedArray.putTypeValue(indexInStyleable, typedValue)
                                cachedTypedArray.putIndexAttr(indexInStyleable)
                            } else {
                                cachedTypedArray.removeTypeValue(indexInStyleable)
                            }

                        }

                    }
                    if (!cachedTypedArray.isEmpty()) {
                        cachedTypedArrayMap.put(styleable, cachedTypedArray)
                        applyTypedValueWhenOnAssemble(view, styleable, cachedTypedArray)
                    }
                } else {
                    cachedTypedArrayMap.remove(styleable)

                }
                typedArray.recycle()
            }
            if (!cachedTypedArrayMap.isEmpty()) {
                isSave = true
                view.setTag(R.id.tag_ui_mode_type_array_map, cachedTypedArrayMap)
            }else{
                if(cachedTypedArrayMap.isEmpty()){
                    ViewStore.clearViewCachedTypedArrayMap(view)
                }
            }

        }

        if (mCustomStyleableKeySet.isNotEmpty()) {

            val tagcachedTypedArrayMap = view.getTag(R.id.tag_ui_mode_custom_type_array_map)
            val cachedTypedArrayMap =
                if (tagcachedTypedArrayMap != null && tagcachedTypedArrayMap is HashMap<*, *>) tagcachedTypedArrayMap as HashMap<IntArray, CachedTypedValueArray> else HashMap<IntArray, CachedTypedValueArray>()

            for (styleable in mCustomStyleableKeySet) {
                val typedArray = view.context.obtainStyledAttributes(attributeSet, styleable)
                val indexCount = typedArray.indexCount
                val cachedTypedArray =
                    CachedTypedValueArray(view.resources, WeakReference(view.context))
                if (indexCount > 0) {
                    for (i in 0 until indexCount) {
                        val indexInStyleable = typedArray.getIndex(i)
                        val typedValue = TypedValue()
                        if (typedArray.getValue(indexInStyleable, typedValue)) {
                            cachedTypedArray.putTypeValue(indexInStyleable, typedValue)
                            cachedTypedArray.putIndexAttr(indexInStyleable)
                        }
                    }
                }
                onAssembleCustom(view, styleable, typedArray, cachedTypedArray)
                if (!cachedTypedArray.isEmpty()) {
                    cachedTypedArrayMap.put(styleable, cachedTypedArray)
                }
                typedArray.recycle()
            }
            if (!cachedTypedArrayMap.isEmpty()) {
                isSave = true
                view.setTag(R.id.tag_ui_mode_custom_type_array_map, cachedTypedArrayMap)
            }
        }

        return isSave


    }

    open fun onAssembleCustom(
        view: View,
        styleable: IntArray,
        typedArray: TypedArray,
        cachedTypedArray: CachedTypedValueArray
    ) {

    }

    open fun onAssemble(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        typedValue: TypedValue
    ): Boolean {
        return false
    }


    /**
     * 默认情况下不执行onApply更改样式
     */
    open fun onInterceptApplyWhenOnAssemble(
        view: View, styleable: IntArray, typedArray: CachedTypedValueArray
    ): Boolean {
        return true
    }


    /**
     * 生成缓存样式后判断是否要应用样式
     */
    open fun applyTypedValueWhenOnAssemble(
        view: View, styleable: IntArray, typedArray: CachedTypedValueArray
    ): Boolean {

        if (!onInterceptApplyWhenOnAssemble(view, styleable, typedArray)) {
            return this.onApply(view, styleable, typedArray)
        }
        return false
    }


    fun isLegalType(typedValue: TypedValue): Boolean {
        return typedValue.type != TypedValue.TYPE_NULL
    }

    fun isHexColorResourceType(typedValue: TypedValue): Boolean {
        return typedValue.resourceId == 0 && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
    }

    override fun assembleStyle(view: View, @StyleRes styleRes: Int): Int {
        var applyCount = 0

        var tagCachedTypedArrayMap = ViewStore.getCachedTypedArrayMap(view)


        mStyleableKeySet.forEach { styleable ->
            val attrTypedArray = tagCachedTypedArrayMap?.get(styleable) ?: CachedTypedValueArray(
                view.resources,
                WeakReference(view.context)
            )
            styleable.forEachIndexed { index, attrResId ->
                if (attrTypedArray.peekValue(index) == null) {
                    val typedArray = view.context.obtainStyledAttributes(
                        styleRes, intArrayOf(
                            attrResId
                        )
                    )
                    if (typedArray.indexCount > 0) {
                        val typedValue = TypedValue()
                        if (typedArray.getValue(0, typedValue) && isLegalType(typedValue)) {
                            attrTypedArray.putTypeValue(index, typedValue)
                            attrTypedArray.putIndexAttr(index)
                            applyCount++
                        }

                    }
                    typedArray.recycle()
                }


            }
            if (!attrTypedArray.isEmpty()) {
                if (tagCachedTypedArrayMap == null) {
                    tagCachedTypedArrayMap = ViewStore.getCreateIfNullCachedTypedArrayMap(view)
                }
                tagCachedTypedArrayMap.put(styleable, attrTypedArray)
            }
        }
        view.setTag(R.id.tag_ui_mode_widget_style_apply_count, applyCount)
        return applyCount

    }


    protected fun registerAttrArray(styleable: IntArray) {
        mStyleableKeySet.add(styleable)
        styleable.forEach {
            mThemeElementKeySet.add(it)
        }

    }

    protected fun registerCustomAttrArray(styleable: IntArray) {
        mCustomStyleableKeySet.add(styleable)
    }


    /**
     * 校验 Theme 是否为null
     *
     * @param v a view
     * @return true : 不为null
     */
    open fun validTheme(v: View): Boolean {
        return getTheme(v) != null
    }

    /**
     * 从 view 获取 theme
     *
     * @param v a view
     * @return theme
     */
    open fun getTheme(v: View): Theme? {
        return v.context.theme
    }

    /**
     * 检索主题中属性的值
     *
     * @param v           a view.
     * @param resId       The resource identifier of the desired theme attribute.
     * @return boolean Returns true if the attribute was found and <var>outValue</var>
     * is valid, else false.
     * @see android.content.res.Resources.Theme.resolveAttribute
     */
    open fun resolveAttribute(v: View, resId: Int): TypedValue? {
        val typedValue = AppResourceUtils.getTypedValue()
        val success = getTheme(v)?.resolveAttribute(resId, typedValue, true)
        if (success == true) {
            return typedValue
        }
        return null
    }

    private fun getActivityTheme(v: View): Theme? {
        val context: Context = v.context
        val activity = AppUtil.findActivity(context)
        return if (activity != null) activity.theme else context.getTheme()
    }

    private fun parseAttrId(attrValue: String): Int {
        if (!TextUtils.isEmpty(attrValue) && attrValue.startsWith("?")) {
            val subStr = attrValue.substring(1, attrValue.length)
            try {
                val attrId = Integer.valueOf(subStr)
                return attrId
            } catch (e: Exception) {
                // no-op
            }
        }
        return ViewStore.NO_ID
    }


}