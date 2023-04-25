package com.aliya.uimode.uimodewidget

import android.content.res.CachedTypeArray
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.aliya.uimode.R
import com.aliya.uimode.factory.ViewStore
import com.aliya.uimode.mode.Attr
import com.aliya.uimode.mode.UiMode
import com.aliya.uimode.utils.AppResourceUtils

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
    }

    /**
     * 主题元素集合
     */
    protected val mThemeElementKeySet: MutableSet<Int> = HashSet()

    private val mStyleableKeySet: HashSet<IntArray> = HashSet()

    private val mCustomtyleableKeySet: HashSet<IntArray> = HashSet()

    init {
    }

    open fun onRegisterStyleable(){

    }

    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, TypedArray>) {
    }

    override fun assemble(view: View, attributeSet: AttributeSet) {

        var ignoreValue = ""
        val ignoreAttrNames = HashSet<String>()
        val ignoreAttrIds = HashSet<Int>()
        val attrValueMap = HashMap<Int,Int>()
        val N: Int = attributeSet.getAttributeCount()
        val styleResId = attributeSet.styleAttribute
        var isSave = false
        if (styleResId != 0) {
            view.setTag(R.id.tag_ui_mode_widget_style, styleResId)
            isSave = true
        }
        for (i in 0 until N) {
            val attrName: String = attributeSet.getAttributeName(i)
            if (Attr.IGNORE == attrName) {
                ignoreValue = attributeSet.getAttributeValue(i)
            }else {
                val attrValueId =  parseAttrId(attributeSet.getAttributeValue(i))
                if(attrValueId != 0){
                    val attr = AppResourceUtils.getAttrId(view.context, attrName)
                    attrValueMap.put(attr,attrValueId)
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

            val cachedTypeArrayMap = HashMap<IntArray, CachedTypeArray>()

            for (styleable in mStyleableKeySet) {
                val typedArray = view.context.obtainStyledAttributes(attributeSet, styleable)
                val indexCount = typedArray.indexCount
                val cachedTypeArray = CachedTypeArray(view.resources, view.context)
                if (indexCount > 0) {
                    for (i in 0 until indexCount) {
                        val indexInStyleable = typedArray.getIndex(i)
                        val typedValue = TypedValue()
                        if (typedArray.getValue(i, typedValue) && isLegalType(typedValue) &&
                            !ignoreAttrIds.contains(styleable[indexInStyleable])
                        ) {
                            if(attrValueMap.containsKey(styleable[indexInStyleable])){
                                typedValue.type = TypedValue.TYPE_ATTRIBUTE
                                typedValue.resourceId = attrValueMap[styleable[indexInStyleable]]?:0
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

        if(mCustomtyleableKeySet.isNotEmpty()){
            val cachedTypeArrayMap = HashMap<IntArray, CachedTypeArray>()

            for (styleable in mCustomtyleableKeySet) {
                val typedArray = view.context.obtainStyledAttributes(attributeSet, styleable)
                val indexCount = typedArray.indexCount
                val cachedTypeArray = CachedTypeArray(view.resources, view.context)
                if (indexCount > 0) {
                    for (i in 0 until indexCount) {
                        val indexInStyleable = typedArray.getIndex(i)
                        val typedValue = TypedValue()
                        if (typedArray.getValue(i, typedValue)) {
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

        if(isSave){
            ViewStore.saveView(view.context,view)
        }


    }

    private fun isLegalType(typedValue: TypedValue): Boolean {
        return typedValue.type != TypedValue.TYPE_NULL && typedValue.resourceId != 0
    }

    override fun applyStyle(view: View, styleRes: Int) {
        mStyleableKeySet.forEach { styleable ->
            val cachedTypeArray = CachedTypeArray(view.resources,view.context)
            styleable.forEachIndexed { index,attrResId->
                val typedArray = view.context.obtainStyledAttributes(
                    styleRes, intArrayOf(
                        attrResId
                    )
                )
                if(attrResId == android.R.attr.textColor){
                    Log.e("","")
                }
                if (typedArray != null && typedArray.indexCount > 0 && attrResId == android.R.attr.textColor) {
                    val typedValue = TypedValue()
                    typedArray.getValue(0, typedValue)
                    if (typedValue.type != TypedValue.TYPE_NULL) {
                        cachedTypeArray.putTypeValue(index,typedValue)
                    }

                }
                cachedTypeArray.putIndexAttr(index,index)
                if (typedArray != null) {
                    typedArray.recycle()
                }
            }
            if(!cachedTypeArray.isEmpty()){
                onApply(view,styleable, cachedTypeArray)
            }
        }

    }




    protected fun registerAttrArray(styleable:IntArray){
        mStyleableKeySet.add(styleable)
        styleable.forEach {
            mThemeElementKeySet.add(it)
        }

    }
    protected fun registerCustomAttrArray(styleable:IntArray){
        mCustomtyleableKeySet.add(styleable)
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
        val success = getTheme(v)!!.resolveAttribute(resId,typedValue , true)
        if(success){
            return typedValue
        }
        return null
    }

    private  fun parseAttrId(attrValue: String): Int {
        if (!TextUtils.isEmpty(attrValue) && attrValue.startsWith("?")) {
            val subStr = attrValue.substring(1, attrValue.length)
            try {
                val attrId = Integer.valueOf(subStr)
                return attrId
            } catch (e: Exception) {
                // no-op
            }
        }
        return UiMode.NO_ID
    }


}