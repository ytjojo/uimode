package com.aliya.uimode.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Xfermode
import android.os.Build
import android.view.View
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import com.aliya.uimode.core.ViewStore
import kotlin.math.max

/**
 * 实现圆角 - 助手
 *
 * @author a_liYa
 * @date 2018/1/30 14:31.
 */
internal class RoundHelper(private val mContext: Context) {
    private var mRect: RectF? = null
    private var mClipPath: Path? = null
    private var mOverallPath: Path? = null

    // 圆角半径
    private var radiusLeftTop = 0f
    private var radiusLeftBottom = 0f
    private var radiusRightTop = 0f
    private var radiusRightBottom = 0f
    private var radiusOval = false // 是否为椭圆
    private val radii = FloatArray(8) // left-top, top-right, bottom-right, bottom-left

    private var borderColor = 0
    private var borderWidth = 0f

    // private boolean borderOverlay; // 开发者可通过设置 Padding 达到同样效果
    private var borderColorRes = ViewStore.NO_ID

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private fun getValue(a: CachedTypedValueArray, @StyleableRes index: Int): Float {
        return max(a.getDimension(index, 0f), 0f)
    }

    /**
     * 校验是否需要绘画
     *
     * @return true:需要
     */
    fun validNeedDraw(): Boolean {
        return radiusOval || radiusLeftTop > 0 || radiusLeftBottom > 0 || radiusRightTop > 0 || radiusRightBottom > 0 ||
                (borderWidth > 0 && borderColor != Color.TRANSPARENT)
    }

    fun onDraw(canvas: Canvas, v: View) {
        if (!validNeedDraw()) return

        if (mRect == null) mRect = RectF()
        if (mClipPath == null) mClipPath = Path()

        // 处理圆角
        run {
            mClipPath!!.reset()
            mRect!!.left = v.getPaddingLeft().toFloat()
            mRect!!.top = v.getPaddingTop().toFloat()
            mRect!!.right = (v.getWidth() - v.getPaddingRight()).toFloat()
            mRect!!.bottom = (v.getHeight() - v.getPaddingBottom()).toFloat()
            if (radiusOval) {
                mClipPath!!.addOval(mRect!!, Path.Direction.CW)
            } else {
                radii[1] = radiusLeftTop
                radii[0] = radii[1]
                radii[3] = radiusRightTop
                radii[2] = radii[3]
                radii[5] = radiusRightBottom
                radii[4] = radii[5]
                radii[7] = radiusLeftBottom
                radii[6] = radii[7]
                mClipPath!!.addRoundRect(mRect!!, radii, Path.Direction.CW)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (mOverallPath == null) mOverallPath = Path()
                else mOverallPath!!.reset()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // 范围上、下、左、右均扩大 1 是因执行缩放动画时边缘偶现1px未被清除.
                    mOverallPath!!.addRect(
                        -1f,
                        -1f,
                        (canvas.getWidth() + 1).toFloat(),
                        (canvas.getHeight() + 1).toFloat(),
                        Path.Direction.CW
                    )
                } else {
                    mOverallPath!!.addRect(
                        0f, 0f,
                        canvas.getWidth().toFloat(), canvas.getHeight().toFloat(), Path.Direction.CW
                    )
                }
                mClipPath!!.op(mOverallPath!!, Path.Op.XOR)
            }

            mPaint.reset()
            mPaint.setAntiAlias(true)
            mPaint.setDither(true)
            mPaint.setXfermode(mXfermode)
            mPaint.setStyle(Paint.Style.FILL)
            canvas.drawPath(mClipPath!!, mPaint)
        }

        // 处理边框
        if (borderWidth > 0 && borderColor != Color.TRANSPARENT) {
            mClipPath!!.reset()
            mRect!!.left = mRect!!.left - v.getPaddingLeft() + borderWidth / 2
            mRect!!.top = mRect!!.top - v.getPaddingTop() + borderWidth / 2
            mRect!!.right = mRect!!.right + v.getPaddingRight() - borderWidth / 2
            mRect!!.bottom = mRect!!.bottom + v.getPaddingBottom() - borderWidth / 2

            if (radiusOval) {
                mClipPath!!.addOval(mRect!!, Path.Direction.CW)
            } else {
                radii[1] = max(radiusLeftTop - borderWidth / 2, 0f)
                radii[0] = radii[1]
                radii[3] = max(radiusRightTop - borderWidth / 2, 0f)
                radii[2] = radii[3]
                radii[5] = max(radiusRightBottom - borderWidth / 2, 0f)
                radii[4] = radii[5]
                radii[7] = max(radiusLeftBottom - borderWidth / 2, 0f)
                radii[6] = radii[7]
                mClipPath!!.addRoundRect(mRect!!, radii, Path.Direction.CW)
            }
            mPaint.reset()
            mPaint.setAntiAlias(true)
            mPaint.setStrokeWidth(borderWidth)
            mPaint.setColor(borderColor)
            mPaint.setStyle(Paint.Style.STROKE)
            canvas.drawPath(mClipPath!!, mPaint)
        }
    }


    fun onAssemble(cachedTypedValueArray: CachedTypedValueArray): Boolean{
        radiusOval = cachedTypedValueArray.getBoolean(R.styleable.Round_radius_oval, false)
        if (!radiusOval) {
            if (cachedTypedValueArray.hasValue(R.styleable.Round_radius)) {
                val r = cachedTypedValueArray.getDimension(R.styleable.Round_radius, 0f)
                if (r > 0) {
                    radiusRightBottom = r
                    radiusRightTop = radiusRightBottom
                    radiusLeftBottom = radiusRightTop
                    radiusLeftTop = radiusLeftBottom
                }
            } else {
                radiusLeftTop = getValue(cachedTypedValueArray, R.styleable.Round_radius_leftTop)
                radiusRightTop = getValue(cachedTypedValueArray, R.styleable.Round_radius_rightTop)
                radiusRightBottom = getValue(cachedTypedValueArray, R.styleable.Round_radius_rightBottom)
                radiusLeftBottom = getValue(cachedTypedValueArray, R.styleable.Round_radius_leftBottom)
            }
        }

        borderWidth = cachedTypedValueArray.getDimension(R.styleable.Round_border_width, 0f)
        borderColor = cachedTypedValueArray.getColor(R.styleable.Round_border_color, Color.TRANSPARENT)
        borderColorRes = cachedTypedValueArray.getResourceId(R.styleable.Round_border_color, ViewStore.NO_ID)
        if(borderColorRes == ViewStore.NO_ID){
            cachedTypedValueArray.recycle()
            return false
        }else {
            return true
        }
    }

    fun onApplyUiModeChanged(cachedTypedValueArray: CachedTypedValueArray) {
        if (borderColorRes != ViewStore.NO_ID) {
            try {
                borderColor = ContextCompat.getColor(mContext, borderColorRes)
            } catch (e: Exception) {
                // no-op
            }
        }
    }

}
