package com.aliya.uimode.core

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.SparseArray
import android.util.SparseIntArray
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import com.aliya.uimode.utils.DrawableCompatUtil
import java.lang.ref.WeakReference
import androidx.core.util.size

class CachedTypedValueArray(var resources: Resources, var contextRef: WeakReference<Context>) {


    val indexToAttr = SparseIntArray()


    val typeValues = SparseArray<TypedValue>()

    constructor(mResources: Resources, context: Context) : this(mResources, WeakReference(context))

    fun isEmpty(): Boolean {
        return typeValues.size() == 0
    }

    fun putTypeValue(@StyleableRes index: Int, typedValue: TypedValue) {
        typeValues.put(index, typedValue)
        putIndexAttr(index)
    }

    fun putIndexAttr( @StyleableRes indexOfStyleableRes: Int) {
        for (i in 0 until indexToAttr.size){
            if (indexToAttr.get(i) == indexOfStyleableRes) {
                return
            }
        }
        indexToAttr.put(indexToAttr.size, indexOfStyleableRes)
    }

    fun getReferenceId(@StyleableRes index: Int): Int {
        return typeValues.get(index)!!.resourceId
    }


    fun getBoolean(@StyleableRes index: Int, defValue: Boolean): Boolean {
        val typedValue = typeValues.get(index)
        if (typedValue == null) {
            return defValue
        }
        if (typedValue.type >= TypedValue.TYPE_FIRST_INT
            && typedValue.type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data != 0
        }
        return resources.getBoolean(typedValue.resourceId)

    }

    fun getChangingConfigurations(): Int {
        throw UnsupportedOperationException("Cannot getChangingConfigurations")
    }

    fun getColor(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue
        val type: Int = typedValue.type

        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            if (typedValue.resourceId != 0) {
                val context = contextRef.get()
                context ?: return typedValue.data
                return ContextCompat.getColor(context, typedValue.resourceId)
            }
            return typedValue.data
        } else if (type == TypedValue.TYPE_STRING) {
            if (type != TypedValue.TYPE_NULL) {
                val context = contextRef.get()
                context ?: return defValue
                val csl: ColorStateList =
                    ContextCompat.getColorStateList(context, typedValue.resourceId)!!
                return csl.defaultColor
            }
            return defValue
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to color: type=0x" + Integer.toHexString(type)
        )
    }

    fun getColorStateList(@StyleableRes index: Int): ColorStateList? {
        val typedValue = typeValues.get(index) ?: return null
        if (typedValue.type != TypedValue.TYPE_NULL) {
            if (typedValue.type == TypedValue.TYPE_ATTRIBUTE) {
                throw UnsupportedOperationException(
                    "Failed to resolve attribute at index $index: $typedValue"
                )
            }
            val context = contextRef.get()
            context ?: return null
            return ContextCompat.getColorStateList(context, typedValue.resourceId)
        }
        return null
    }

    fun getDimension(@StyleableRes index: Int, defValue: Float): Float {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimension(typedValue.data, resources.displayMetrics)
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to dimension: type=0x" + Integer.toHexString(type)
        )
    }

    fun getDimensionPixelOffset(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelOffset(
                typedValue.data,
                resources.displayMetrics
            )
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to dimension: type=0x" + Integer.toHexString(type)
        )
    }

    fun getDimensionPixelSize(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                resources.displayMetrics
            )
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to dimension: type=0x" + Integer.toHexString(type)
        )
    }

    fun getDrawable(@StyleableRes index: Int): Drawable? {
        return getDrawableForDensity(index, 0)
    }


    fun getDrawableForDensity(@StyleableRes index: Int, density: Int): Drawable? {

        val typedValue = typeValues.get(index) ?: return null

        val type = typedValue.type
        if (type != TypedValue.TYPE_NULL) {
            if (typedValue.type == TypedValue.TYPE_ATTRIBUTE) {
                throw UnsupportedOperationException(
                    "Failed to resolve attribute at index $index: $typedValue"
                )
            }
            val context = contextRef.get()
            context ?: return null
            return DrawableCompatUtil.Companion.getDrawable(context, typedValue.resourceId)
        }
        return null
    }

    fun getFloat(@StyleableRes index: Int, defValue: Float): Float {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type

        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_FLOAT) {
            return java.lang.Float.intBitsToFloat(typedValue.data)
        } else if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data.toFloat()
        }

        if (type != TypedValue.TYPE_NULL) {
            val str = typedValue.coerceToString()
            if (str != null) {
                return str.toString().toFloat()
            }
        }

        // We already checked for TYPE_NULL. This should never happen.
        throw RuntimeException("getFloat of bad type: 0x" + Integer.toHexString(type))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getFont(@StyleableRes index: Int): Typeface? {
        val typedValue = typeValues.get(index) ?: return null

        val type = typedValue.type

        if (type != TypedValue.TYPE_NULL) {
            if (type == TypedValue.TYPE_ATTRIBUTE) {
                throw UnsupportedOperationException(
                    "Failed to resolve attribute at index $index: $typedValue"
                )
            }
            val context = contextRef.get()
            context ?: return null
            context.resources.getFont(typedValue.resourceId)
        }
        return null
    }

    fun getFraction(
        @StyleableRes index: Int,
        base: Int,
        pbase: Int,
        defValue: Float
    ): Float {

        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type

        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_FRACTION) {
            return TypedValue.complexToFraction(
                typedValue.data,
                base.toFloat(),
                pbase.toFloat()
            )
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to fraction: type=0x" + Integer.toHexString(type)
        )
    }

    fun getIndex(at: Int): Int {
        return indexToAttr[at]
    }

    fun getIndexCount(): Int {
        return indexToAttr.size()
    }

    fun getInt(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type

        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        }

        if (type != TypedValue.TYPE_NULL) {
            return resources.getInteger(typedValue.resourceId)
        }
        // We already checked for TYPE_NULL. This should never happen.
        throw RuntimeException("getInt of bad type: 0x" + Integer.toHexString(type))
    }


    fun getInteger(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            throw UnsupportedOperationException(
                "Failed to resolve attribute at index $index: $typedValue"
            )
        }

        throw UnsupportedOperationException(
            "Can't convert value at index " + index
                    + " to integer: type=0x" + Integer.toHexString(type)
        )
    }

    fun getLayoutDimension(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                resources.displayMetrics
            )
        }

        return defValue
    }

    fun getNonResourceString(@StyleableRes index: Int): String? {

        val typedValue = typeValues.get(index) ?: return null
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return null
        } else if (type == TypedValue.TYPE_STRING
        ) {
            return typedValue.string.toString()
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            if (typedValue.assetCookie < 0) {
            }
            return typedValue.string.toString()

        }


        return null

    }

    fun getPositionDescription(): String {
        return "<internal>"
    }

    fun getResourceId(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else {
            return typedValue.resourceId
        }

        return defValue
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSourceResourceId(@StyleableRes index: Int, defaultValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defaultValue
        val resid = typedValue.sourceResourceId
        if (resid != 0) {
            return resid
        }
        return defaultValue
    }


    fun getString(@StyleableRes index: Int): String? {

        val typedValue = typeValues.get(index) ?: return null
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return null
        } else if (type == TypedValue.TYPE_STRING) {
            return typedValue.string.toString()
        }
        val cs = typedValue.coerceToString()
        return cs?.toString()

    }

    fun getText(@StyleableRes index: Int): CharSequence? {

        val typedValue = typeValues.get(index) ?: return null
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return null
        } else if (type == TypedValue.TYPE_STRING) {
            return typedValue.string.toString()
        }
        return typedValue.coerceToString()
    }

    fun getTextArray(@StyleableRes index: Int): Array<CharSequence>? {
        val typedValue = typeValues.get(index) ?: return null

        return resources.getTextArray(typedValue.resourceId)
    }

    fun getType(@StyleableRes index: Int): Int {
        val typedValue = typeValues.get(index)
        return typedValue.type
    }

    fun getValue(@StyleableRes index: Int, outValue: TypedValue?): Boolean {
        val typedValue = typeValues.get(index) ?: return false
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return false
        }
        outValue!!.type = type
        outValue!!.data = typedValue.data
        outValue!!.assetCookie = typedValue.assetCookie
        outValue!!.resourceId = typedValue.resourceId
        outValue!!.changingConfigurations = typedValue.changingConfigurations
        outValue!!.density = typedValue.density
        outValue!!.string = typedValue.string
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            outValue!!.sourceResourceId = typedValue.sourceResourceId
        }
        return true
    }

    fun length(): Int {
        return indexToAttr.size()
    }

    fun hasValueOrEmpty(@StyleableRes index: Int): Boolean {
        val typedValue = typeValues.get(index) ?: return false
        val type = typedValue.type
        return type != TypedValue.TYPE_NULL
                || type == TypedValue.DATA_NULL_EMPTY;
    }

    fun hasValue(@StyleableRes index: Int): Boolean {
        val typedValue = typeValues.get(index) ?: return false
        val type = typedValue.type
        return type != TypedValue.TYPE_NULL
    }

    fun peekValue(@StyleableRes index: Int): TypedValue? {
        return typeValues.get(index)
    }


    fun removeValue(@StyleableRes index: Int){
        typeValues.remove(index)
    }
    fun recycle() {
        indexToAttr.clear()
        typeValues.clear()
        contextRef.clear()
    }

    fun close() {
    }


    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until typeValues.size()) {
            val typedValue = typeValues.valueAt(i)
            sb.append(" ")
            sb.append(typedValue.toString())
            sb.append(" ")
        }
        return sb.toString()
    }
}