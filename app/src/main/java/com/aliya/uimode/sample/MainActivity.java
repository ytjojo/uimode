package com.aliya.uimode.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.aliya.uimode.sample.base.BaseActivity;
import com.aliya.uimode.sample.view.TopBar;
import com.aliya.viewtreedebug.ViewTreeDebugTool;

/**
 * 主界面
 *
 * @author a_liYa
 * @date 2018/2/1 下午3:06.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView mDebugToggleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_go_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_theme_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_image_view_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_list_view_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_recycler_view_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_web_view_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_text_view_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_bug_simple).setOnClickListener(this);
        findViewById(R.id.btn_go_simple_test).setOnClickListener(this);
        findViewById(R.id.btn_go_local_set).setOnClickListener(this);
        findViewById(R.id.btn_go_style_test).setOnClickListener(this);
        findViewById(R.id.btn_go_backlib_test).setOnClickListener(this);
        findViewById(R.id.btn_go_tint_colorFilter_test).setOnClickListener(this);
        findViewById(R.id.btn_toggle_view_tree_debug).setOnClickListener(this);
        mDebugToggleView = findViewById(R.id.btn_toggle_view_tree_debug);
        bindDebugToggleText();
        ((TopBar) findViewById(R.id.top_bar)).setTitle("主界面");

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindDebugToggleText();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_go_simple) {
            startActivity(new Intent(this, SimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_theme_simple) {
            startActivity(new Intent(this, ThemeSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_image_view_simple) {
            startActivity(new Intent(this, ImageViewSimpleActivity.class));

        } else if (v.getId() == R.id.btn_go_list_view_simple) {
            startActivity(new Intent(this, ListViewSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_recycler_view_simple) {
            startActivity(new Intent(this, RecyclerSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_web_view_simple) {
            startActivity(new Intent(this, WebViewSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_text_view_simple) {
            startActivity(new Intent(this, TextViewSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_bug_simple) {
            startActivity(new Intent(this, BugSimpleActivity.class));
        } else if (v.getId() == R.id.btn_go_simple_test) {
            startActivity(new Intent(this, SimpleTestActivity.class));
        } else if (v.getId() == R.id.btn_go_local_set) {
            startActivity(new Intent(this, LocalModeActivity.class));

        } else if (v.getId() == R.id.btn_go_style_test) {
            startActivity(new Intent(this, StyleTestActivity.class));
        } else if (v.getId() == R.id.btn_go_backlib_test) {
            startActivity(new Intent(this, NooberBackgroundActivity.class));
        } else if (v.getId() == R.id.btn_go_tint_colorFilter_test) {
            startActivity(new Intent(this, TintColorFilterActivity.class));
        } else if (v.getId() == R.id.btn_toggle_view_tree_debug) {
            if (ViewTreeDebugTool.isEnabled()) {
                ViewTreeDebugTool.disable();
            } else if (ViewTreeDebugTool.ensureOverlayPermission(this)) {
                ViewTreeDebugTool.enable();
            }
            bindDebugToggleText();
        }
    }

    private void bindDebugToggleText() {
        if (mDebugToggleView == null) {
            return;
        }
        if (ViewTreeDebugTool.isEnabled()) {
            mDebugToggleView.setText("关闭 View 树调试工具");
        } else {
            mDebugToggleView.setText("开启 View 树调试工具");
        }
    }

}
