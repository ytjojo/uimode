package com.aliya.uimode.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.aliya.uimode.sample.base.BaseActivity;
import com.aliya.uimode.sample.view.TopBar;

/**
 * 主界面
 *
 * @author a_liYa
 * @date 2018/2/1 下午3:06.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

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
        ((TopBar) findViewById(R.id.top_bar)).setTitle("主界面");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_simple:
                startActivity(new Intent(this, SimpleActivity.class));
                break;
            case R.id.btn_go_theme_simple:
                startActivity(new Intent(this, ThemeSimpleActivity.class));
                break;
            case R.id.btn_go_image_view_simple:
                startActivity(new Intent(this, ImageViewSimpleActivity.class));
                break;
            case R.id.btn_go_list_view_simple:
                startActivity(new Intent(this, ListViewSimpleActivity.class));
                break;
            case R.id.btn_go_recycler_view_simple:
                startActivity(new Intent(this, RecyclerSimpleActivity.class));
                break;
            case R.id.btn_go_web_view_simple:
                startActivity(new Intent(this, WebViewSimpleActivity.class));
                break;
            case R.id.btn_go_text_view_simple:
                startActivity(new Intent(this, TextViewSimpleActivity.class));
                break;
            case R.id.btn_go_bug_simple:
                startActivity(new Intent(this, BugSimpleActivity.class));
                break;

            case R.id.btn_go_simple_test:
                startActivity(new Intent(this, SimpleTestActivity.class));
                break;
            case R.id.btn_go_local_set:
                startActivity(new Intent(this, LocalModeActivity.class));
                break;

            case R.id.btn_go_style_test:
                startActivity(new Intent(this, StyleTestActivity.class));
                break;

            case R.id.btn_go_backlib_test:
                startActivity(new Intent(this, NooberBackgroundActivity.class));

                break;
        }
    }

}
