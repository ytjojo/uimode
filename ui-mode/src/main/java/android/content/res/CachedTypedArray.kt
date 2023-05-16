package android.content.res

import android.content.Context
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

class CachedTypedArray(val mResources: Resources, val context: Context) : TypedArray() {


    val indexToAttr = SparseIntArray()


    val typeValues = SparseArray<TypedValue>()

    fun isEmpty():Boolean{
       return typeValues.size() == 0
    }

    fun putTypeValue(@StyleableRes index: Int,typedValue: TypedValue){
        typeValues.put(index,typedValue)
    }

    fun putIndexAttr(index: Int,@StyleableRes attr:Int){
        indexToAttr.put(index,attr)
    }

    fun getReferenceId(@StyleableRes index: Int): Int {
        return typeValues.get(index)!!.resourceId
    }


    override fun getBoolean(@StyleableRes index: Int, defValue: Boolean): Boolean {
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

    override fun getChangingConfigurations(): Int {
        throw UnsupportedOperationException("Cannot getChangingConfigurations")
    }

    override fun getColor(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue
        val type: Int = typedValue.type

        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        } else if (type == TypedValue.TYPE_STRING) {
            if (type != TypedValue.TYPE_NULL) {
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

    override fun getColorStateList(@StyleableRes index: Int): ColorStateList? {
        val typedValue = typeValues.get(index) ?: return null
        if (typedValue.type != TypedValue.TYPE_NULL) {
            if (typedValue.type == TypedValue.TYPE_ATTRIBUTE) {
                throw UnsupportedOperationException(
                    "Failed to resolve attribute at index $index: $typedValue"
                )
            }
            return ContextCompat.getColorStateList(context, typedValue.resourceId)
        }
        return null
    }

    override fun getDimension(@StyleableRes index: Int, defValue: Float): Float {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimension(typedValue.data, mResources.displayMetrics)
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

    override fun getDimensionPixelOffset(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelOffset(
                typedValue.data,
                mResources.displayMetrics
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

    override fun getDimensionPixelSize(@StyleableRes index: Int, defValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                mResources.displayMetrics
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

    override fun getDrawable(@StyleableRes index: Int): Drawable? {
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
            return DrawableCompatUtil.getDrawable(context, typedValue.resourceId)
        }
        return null
    }

    override fun getFloat(@StyleableRes index: Int, defValue: Float): Float {
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
    override fun getFont(@StyleableRes index: Int): Typeface? {
        val typedValue = typeValues.get(index) ?: return null

        val type = typedValue.type

        if (type != TypedValue.TYPE_NULL) {
            if (type == TypedValue.TYPE_ATTRIBUTE) {
                throw UnsupportedOperationException(
                    "Failed to resolve attribute at index $index: $typedValue"
                )
            }
            context.resources.getFont(typedValue.resourceId)
        }
        return null
    }

    override fun getFraction(
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

    override fun getIndex(at: Int): Int {
        return indexToAttr[at]
    }

    override fun getIndexCount(): Int {
        return indexToAttr.size()
    }

    override fun getInt(@StyleableRes index: Int, defValue: Int): Int {
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


    override fun getInteger(@StyleableRes index: Int, defValue: Int): Int {
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

    override fun getLayoutDimension(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue

        val type = typedValue.type
        if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                mResources.displayMetrics
            )
        }

        return defValue
    }

    override fun getLayoutDimension(@StyleableRes index: Int, name: String?): Int {


        val typedValue = typeValues.get(index) ?: throw java.lang.UnsupportedOperationException(
            positionDescription
                    + ": You must supply a " + name + " attribute."
        )

        val type = typedValue.type
        if (type >= TypedValue.TYPE_FIRST_INT
            && type <= TypedValue.TYPE_LAST_INT
        ) {
            return typedValue.data
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                mResources.displayMetrics
            )
        }


        throw java.lang.UnsupportedOperationException(
            positionDescription
                    + ": You must supply a " + name + " attribute."
        )
    }

    override fun getNonResourceString(@StyleableRes index: Int, ): String? {

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

    override fun getPositionDescription(): String {
        return "<internal>"
    }

    override fun getResourceId(@StyleableRes index: Int, defValue: Int): Int {
        val typedValue = typeValues.get(index) ?: return defValue
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return defValue
        } else {
            return typedValue.resourceId
        }

        return defValue
    }

    override fun getResources(): Resources {
        return mResources
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getSourceResourceId(@StyleableRes index: Int, defaultValue: Int): Int {

        val typedValue = typeValues.get(index) ?: return defaultValue
        val resid = typedValue.sourceResourceId
        if (resid != 0) {
            return resid
        }
        return defaultValue
    }


    override fun getString(@StyleableRes index: Int): String? {

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

    override fun getText(@StyleableRes index: Int): CharSequence? {

        val typedValue = typeValues.get(index) ?: return null
        val type = typedValue.type
        if (type == TypedValue.TYPE_NULL) {
            return null
        } else if (type == TypedValue.TYPE_STRING) {
            return typedValue.string.toString()
        }
        return typedValue.coerceToString()
    }

    override fun getTextArray(@StyleableRes index: Int): Array<CharSequence>? {
        val typedValue = typeValues.get(index) ?: return null

        return mResources.getTextArray(typedValue.resourceId)
    }

    override fun getType(@StyleableRes index: Int): Int {
        val typedValue = typeValues.get(index)
        return typedValue.type
    }

    override fun getValue(@StyleableRes index: Int, outValue: TypedValue?): Boolean {
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

    override fun length(): Int {
        return indexCount
    }

    override fun hasValueOrEmpty(@StyleableRes index: Int): Boolean {
        val typedValue = typeValues.get(index)?: return false
        val type = typedValue.type
        return type != TypedValue.TYPE_NULL
                || type == TypedValue.DATA_NULL_EMPTY;
    }

    override fun hasValue(@StyleableRes index: Int): Boolean {
        val typedValue = typeValues.get(index)?: return false
        val type = typedValue.type
        return type != TypedValue.TYPE_NULL
    }

    override fun peekValue(@StyleableRes index: Int): TypedValue? {
        return typeValues.get(index)
    }

    override fun recycle() {
    }

    override fun close() {
    }

}