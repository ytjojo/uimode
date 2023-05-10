package com.aliya.uimode.sample;

import android.os.Bundle;
import android.widget.ImageView;

import com.aliya.uimode.UiModeManager;
import com.aliya.uimode.sample.base.BaseActivity;

/**
 * ImageView相关的UiMode使用示例页
 *
 * @author a_liYa
 * @date 2018/2/7 下午6:34.
 */
public class ImageViewSimpleActivity extends BaseActivity {

    ImageView ivWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_simple);

        ivWord = (ImageView) findViewById(R.id.iv_word);
        UiModeManager.INSTANCE.saveViewValue(ivWord, R.styleable.AppCompatImageView,R.styleable.AppCompatImageView_android_src, R.mipmap.ic_ui_mode_word);

    }

}