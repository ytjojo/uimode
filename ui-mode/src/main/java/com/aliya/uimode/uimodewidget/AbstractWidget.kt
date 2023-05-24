package com.aliya.uimode.uimodewidget

import android.content.Context
import android.content.res.CachedTypedArray
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import com.aliya.uimode.HideLog
import com.aliya.uimode.R
import com.aliya.uimode.core.UiModeChangeListener
import com.aliya.uimode.core.ViewStore
import com.aliya.uimode.utils.AppResourceUtils
import com.aliya.uimode.utils.AppUtil


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
    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, TypedArray>) {
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
            view.setTag(R.id.tag_ui_mode_widget_style, styleResId)
            isSave = true
        }
        if (view is UiModeChangeListener) {
            isSave = true
        }
        for (i in 0 until N) {
            val attrName: String = attributeSet.getAttributeName(i)
            if (IGNORE_ATTR_NAME == attrName) {
                ignoreValue = attributeSet.getAttributeValue(i)
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
                        ignoreAttrNames.add(attrName)
                    }
                }
            }
            ignoreAttrNames.forEach {
                ignoreAttrIds.add(AppResourceUtils.getAttrId(view.context, it))
            }

            val tagCachedTypeArrayMap = view.getTag(R.id.tag_ui_mode_type_array_map)
            val cachedTypeArrayMap =
                if (tagCachedTypeArrayMap != null && tagCachedTypeArrayMap is HashMap<*, *>) tagCachedTypeArrayMap as HashMap<IntArray, CachedTypedArray> else HashMap<IntArray, CachedTypedArray>()

            for (styleable in mStyleableKeySet) {
                val typedArray = view.context.obtainStyledAttributes(attributeSet, styleable)
                val indexCount = typedArray.indexCount
                val cachedTypeArray = CachedTypedArray(view.resources, view.context)
                if (indexCount > 0) {
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
                            HideLog.i(
                                "assemble",
                                "view  = " + view.toString() + " attrName " + view.context.resources.getResourceName(
                                    styleable[indexInStyleable]
                                ) + " resourceId = " + Integer.toHexString(typedValue.resourceId) + " resourceName = " +  view.context.resources.getResourceName(typedValue.resourceId)
                            )
                            if (attrValueMap.isNotEmpty()) {
                                val attrName =
                                    view.context.resources.getResourceName(styleable[indexInStyleable])
                                        .split('/')[1]
                                if (attrValueMap.containsKey(attrName)) {
                                    typedValue.type = TypedValue.TYPE_ATTRIBUTE
                                    typedValue.resourceId = attrValueMap[attrName] ?: 0
                                }
                            }

                            cachedTypeArray.putTypeValue(indexInStyleable, typedValue)
                        }
                        cachedTypeArray.putIndexAttr(i, indexInStyleable)
                    }
                    if (!cachedTypeArray.isEmpty()) {
                        cachedTypeArrayMap.put(styleable, cachedTypeArray)
                    }
                }
                typedArray.recycle()
            }
            if (!cachedTypeArrayMap.isEmpty()) {
                isSave = true
                view.setTag(R.id.tag_ui_mode_type_array_map, cachedTypeArrayMap)
            }
        }

        if (mCustomStyleableKeySet.isNotEmpty()) {

            val tagCachedTypeArrayMap = view.getTag(R.id.tag_ui_mode_custom_type_array_map)
            val cachedTypeArrayMap =
                if (tagCachedTypeArrayMap != null && tagCachedTypeArrayMap is HashMap<*, *>) tagCachedTypeArrayMap as HashMap<IntArray, CachedTypedArray> else HashMap<IntArray, CachedTypedArray>()

            for (styleable in mCustomStyleableKeySet) {
                val typedArray = view.context.obtainStyledAttributes(attributeSet, styleable)
                val indexCount = typedArray.indexCount
                val cachedTypeArray = CachedTypedArray(view.resources, view.context)
                if (indexCount > 0) {
                    for (i in 0 until indexCount) {
                        val indexInStyleable = typedArray.getIndex(i)
                        val typedValue = TypedValue()
                        if (typedArray.getValue(indexInStyleable, typedValue)) {
                            cachedTypeArray.putTypeValue(indexInStyleable, typedValue)
                        }
                        cachedTypeArray.putIndexAttr(i, indexInStyleable)
                    }
                    if (!cachedTypeArray.isEmpty()) {
                        cachedTypeArrayMap.put(styleable, cachedTypeArray)
                    }
                }
                typedArray.recycle()
            }
            if (!cachedTypeArrayMap.isEmpty()) {
                isSave = true
                view.setTag(R.id.tag_ui_mode_custom_type_array_map, cachedTypeArrayMap)
            }
        }

        return isSave


    }

    open fun onAssemble(
        view: View,
        styleable: IntArray,
        indexInStyleable: Int,
        typedValue: TypedValue
    ): Boolean {
        return false
    }

    private fun isLegalType(typedValue: TypedValue): Boolean {
        return typedValue.type != TypedValue.TYPE_NULL && typedValue.resourceId != 0
    }

    override fun applyStyle(view: View, @StyleRes styleRes: Int) {

        val tagCachedTypeArrayMap =
            view.getTag(R.id.tag_ui_mode_type_array_map) as? HashMap<IntArray, CachedTypedArray>?

        mStyleableKeySet.forEach { styleable ->
            val styleTypedArray = CachedTypedArray(view.resources, view.context)
            val attrTypedArray = tagCachedTypeArrayMap?.get(styleable) as? CachedTypedArray?
            styleable.forEachIndexed { index, attrResId ->
                val typedArray = view.context.obtainStyledAttributes(
                    styleRes, intArrayOf(
                        attrResId
                    )
                )
                if (typedArray != null && typedArray.indexCount > 0) {
                    val typedValue = TypedValue()
                    if (typedArray.getValue(0, typedValue) && isLegalType(typedValue)) {
                        styleTypedArray.putTypeValue(index, typedValue)
                        attrTypedArray?.putTypeValue(index, typedValue)
                    }

                }
                styleTypedArray.putIndexAttr(index, index)
                if (typedArray != null) {
                    typedArray.recycle()
                }
            }
            if (!styleTypedArray.isEmpty()) {
                onApply(view, styleable, styleTypedArray)
            }
        }

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