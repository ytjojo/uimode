package com.aliya.uimode.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.aliya.uimode.R
import com.aliya.uimode.core.CachedTypedValueArray
import java.util.Arrays

/**
 * 遮罩ImageView
 *
 *  *  一、实现圆角功能
 *  *  二、实现固定宽高比功能
 *  *
 * 三、实现夜间模式
 * 1、先获取app:maskColor=""
 * 2、再获取style &lt;item name="maskColor"&gt; &lt;/item&gt;
 * 3、最后获取 R.color.uiMode_maskColor
 *
 *
 * 通过调用invalidate()刷新日夜模式
 *
 * @author a_liYa
 * @date 16/11/4 21:05.
 */
class MaskImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val mMaskHelper: ImageMaskColorHelper
    private val mRatioHelper: RatioHelper
    private val mRoundHelper: RoundHelper

    init {
        mMaskHelper = ImageMaskColorHelper(context)
        mRatioHelper = RatioHelper(context, attrs)
        mRoundHelper = RoundHelper(context)
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            mRatioHelper.widthMeasureSpec(
                widthMeasureSpec, heightMeasureSpec,
                getLayoutParams()
            ),
            mRatioHelper.heightMeasureSpec(
                widthMeasureSpec, heightMeasureSpec,
                getLayoutParams()
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (mMaskHelper.validMaskColor() || mRoundHelper.validNeedDraw()) {
            val saveCount = canvas.saveLayer(
                0f,
                0f,
                canvas.getWidth().toFloat(),
                canvas.getHeight().toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
            super.onDraw(canvas)

            mMaskHelper.drawMaskColor(canvas) // 处理遮罩
            mRoundHelper.onDraw(canvas, this) // 处理圆角

            canvas.restoreToCount(saveCount)
        } else {
            super.onDraw(canvas)
        }
    }


    fun onAssemble(styleable: IntArray,cachedTypedValueArray: CachedTypedValueArray): Boolean {
        if(Arrays.equals(styleable, R.styleable.MaskImageView)){
           return  mMaskHelper.onAssemble(cachedTypedValueArray)
        }else if(Arrays.equals(styleable,R.styleable.Round)){
           return  mRoundHelper.onAssemble(cachedTypedValueArray)
        }
        return false
    }

    fun onApplyUiModeChanged(styleable: IntArray,cachedTypedValueArray: CachedTypedValueArray){
        if(Arrays.equals(styleable, R.styleable.MaskImageView)){
            mMaskHelper.onApplyUiModeChanged(cachedTypedValueArray)
        }else if(Arrays.equals(styleable,R.styleable.Round)){
            mRoundHelper.onApplyUiModeChanged(cachedTypedValueArray)
        }
        if (getDrawable() != null) {
            invalidate()
        }
    }

}
