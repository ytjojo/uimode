package com.aliya.uimode.sample.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.aliya.uimode.sample.R

/**
 * 自定义底部弹窗 Dialog - 支持自由设置 layout 布局
 *
 * @author a_liYa
 * @date 2026/3/26.
 */
class CustomBottomSheetDialog @JvmOverloads constructor(
    context: Context,
    theme: Int = R.style.BottomSheetDialogStyle
) : Dialog(context, theme) {


    init {
        // 设置取消逻辑
        setCanceledOnTouchOutside(true)

    }

    /**
     * 设置自定义布局
     * @param layoutResId 布局资源 ID
     */
    fun setView(@LayoutRes layoutResId: Int): CustomBottomSheetDialog {
        val view = LayoutInflater.from(context).inflate(layoutResId, null)
        setContentView(view)
        return this
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置窗口属性
        window?.apply {
            // 设置背景为透明，否则圆角不显示
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 获取 WindowManager.LayoutParams
            val attributes = attributes ?: return@apply

            // 设置宽度为满屏
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
            // 高度自适应
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT

            // 设置位置在底部
            attributes.gravity = Gravity.BOTTOM

            // 设置动画
            attributes.windowAnimations = R.style.BottomSheetAnimation

            // 应用设置
            decorView.setPadding(0, 0, 0, 0)
            this.attributes = attributes
        }
    }
    /**
     * 设置自定义 View
     * @param view 自定义 View
     */
    override fun setContentView(view: View) {
        super.setContentView(view)


    }


    /**
     * 设置点击外部是否可取消
     */
    fun setCancelableOnTouchOutside(cancelable: Boolean): CustomBottomSheetDialog {
        setCanceledOnTouchOutside(cancelable)
        return this
    }

    /**
     * 显示在底部
     */
    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
    }
}
