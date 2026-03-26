package com.aliya.uimode.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aliya.uimode.sample.base.BaseActivity;
import com.aliya.uimode.sample.dialog.CustomBottomSheetDialog;

/**
 * 自定义 BottomSheetDialog 示例
 *
 * @author a_liYa
 * @date 2026/3/26.
 */
public class CustomBottomSheetDialogActivity extends BaseActivity implements View.OnClickListener {

    private CustomBottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_bottom_sheet);
        findViewById(R.id.btn_show_dialog).setOnClickListener(this);

        // 初始化 Dialog
        bottomSheetDialog = new CustomBottomSheetDialog(this);
        
        // 设置自定义布局
        bottomSheetDialog.setView(R.layout.dialog_custom_bottom_sheet);
        // 获取 View

        Button cancelBtn = bottomSheetDialog.findViewById(R.id.btn_dialog_cancel);


        // 设置按钮点击事件
        cancelBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_show_dialog) {
            // 显示 Dialog
            bottomSheetDialog.show();
        }
    }
}
