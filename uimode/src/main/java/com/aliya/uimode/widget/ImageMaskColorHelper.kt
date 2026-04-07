package com.aliya.uimode.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.TypedValue
import com.aliya.uimode.R
import com.aliya.uimode.UiModeManager
import com.aliya.uimode.core.CachedTypedValueArray

class ImageMaskColorHelper {


    private var porterDuffXfermode: PorterDuffXfermode? = null


    // true:遮罩层与原始图片取交集; false:取并集
    private var maskUnion = false

    private val mContext: Context

    // 实际需要应用的 color
    private var mApplyMaskColor = Color.TRANSPARENT
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    constructor(context: Context) {
        mContext = context
    }


    fun onAssemble(cachedTypedValueArray: CachedTypedValueArray): Boolean {

        val isSupportViewMask = cachedTypedValueArray.getBoolean(
            R.styleable.MaskImageView_view_useMaskColor,
            UiModeManager.isSupportImageViewMask
        )
        if (cachedTypedValueArray.peekValue(R.styleable.MaskImageView_maskColor) == null && isSupportViewMask) {
            val maskColorTypedValue = TypedValue()
            maskColorTypedValue.type = TypedValue.TYPE_STRING
            maskColorTypedValue.resourceId = R.color.uiMode_maskColor
            cachedTypedValueArray.putTypeValue(
                R.styleable.MaskImageView_maskColor,
                maskColorTypedValue
            )
            maskUnion = cachedTypedValueArray.getBoolean(R.styleable.MaskImageView_maskUnion, false)
            this.onApplyUiModeChanged(cachedTypedValueArray)
            return true

        } else if (cachedTypedValueArray.peekValue(R.styleable.MaskImageView_maskColor) != null && !isSupportViewMask) {
            cachedTypedValueArray.recycle()
            return false
        }
        return false
    }

    fun drawMaskColor(canvas: Canvas) {
        if (validMaskColor()) {
            mPaint.setXfermode(getXfermodeMask())
            canvas.drawRect(
                0f,
                0f,
                canvas.getWidth().toFloat(),
                canvas.getHeight().toFloat(),
                mPaint
            )
        }
    }

    private fun getXfermodeMask(): PorterDuffXfermode {
        if (porterDuffXfermode == null) {
            porterDuffXfermode = if (maskUnion)
                PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // 遮罩层与原始图片取并集
            else
                PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP) // 遮罩层与原始图片取交集
        }
        return porterDuffXfermode!!
    }

    fun validMaskColor(): Boolean {
        return mApplyMaskColor != Color.TRANSPARENT
    }


    fun onApplyUiModeChanged(cachedTypedValueArray: CachedTypedValueArray) {
        mApplyMaskColor =
            cachedTypedValueArray.getColor(R.styleable.MaskImageView_maskColor, Color.TRANSPARENT)
        mPaint.setColor(mApplyMaskColor)
    }


}