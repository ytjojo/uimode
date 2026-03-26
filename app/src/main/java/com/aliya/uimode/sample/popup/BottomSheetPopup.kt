package com.aliya.uimode.sample.popup

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.aliya.uimode.sample.R

/**
 * 底部弹窗 - 可滑动，点击外部关闭，带关闭按钮
 *
 * @author a_liYa
 * @date 2026/3/26.
 */
class BottomSheetPopup(context: Context) : PopupWindow(context) {

    private val rootView: View
    private var recyclerView: RecyclerView
    private var closeBtn: ImageView
    private var titleTv: TextView
    private var dataList = mutableListOf<String>()
    private var onItemClickListener: ((String, Int) -> Unit)? = null

    init {
        // 加载布局
        rootView = LayoutInflater.from(context).inflate(R.layout.popup_bottom_sheet, null)
        contentView = rootView

        // 设置宽高
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT

        // 设置焦点
        isFocusable = true
        isOutsideTouchable = true

        // 设置背景 drawable，否则点击外部无效
        setBackgroundDrawable(ColorDrawable())

        // 设置动画
        animationStyle = R.style.BottomSheetAnimation

        // 初始化视图
        recyclerView = rootView.findViewById(R.id.rv_content)
        closeBtn = rootView.findViewById(R.id.iv_close)
        titleTv = rootView.findViewById(R.id.tv_title)

        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = BottomSheetAdapter(dataList)

        // 关闭按钮点击事件
        closeBtn.setOnClickListener {
            dismiss()
        }

        // 点击外部关闭
        setOnDismissListener {
            // 可以在这里做清理工作
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String): BottomSheetPopup {
        titleTv.text = title
        return this
    }

    /**
     * 设置数据
     */
    fun setData(data: List<String>): BottomSheetPopup {
        dataList.clear()
        dataList.addAll(data)
        recyclerView.adapter?.notifyDataSetChanged()
        return this
    }

    /**
     * 设置 item 点击监听
     */
    fun setOnItemClickListener(listener: (String, Int) -> Unit) {
        this.onItemClickListener = listener
    }

    /**
     * 显示在屏幕底部
     */
    fun showAtBottom(parent: View) {
        if (!isShowing) {
            showAtLocation(parent, android.view.Gravity.BOTTOM, 0, 0)
        }
    }

    /**
     * 适配器
     */
    inner class BottomSheetAdapter(private val data: List<String>) :
        RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.tv_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_list_view_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = data[position]
            holder.itemView.setOnClickListener {
                onItemClickListener?.invoke(data[position], position)
                dismiss()
            }
        }

        override fun getItemCount(): Int = data.size
    }
}
