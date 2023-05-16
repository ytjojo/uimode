package com.aliya.uimode.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aliya.uimode.sample.base.BaseActivity;

/**
 * WebView相关示例
 *
 * @author a_liYa
 * @date 2018/4/13 上午9:35.
 */
public class WebViewSimpleActivity extends BaseActivity implements View.OnClickListener {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_simple);

        mWebView = (WebView) findViewById(R.id.web_view);

//        mWebView.setBackgroundColor(Color.TRANSPARENT);


        findViewById(R.id.tv_load).setOnClickListener(this);
        WebSettings webSettings = mWebView.getSettings();


        webSettings.setJavaScriptEnabled(true);


        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存

        webSettings.setAllowFileAccess(true); //设置可以访问文件

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口

        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片

        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                //使用WebView加载显示url
//                view.loadUrl(url);
//                //返回true
//                return true;


                if (url == null || url.startsWith("http://") || url.startsWith("https://")) return false;

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return true;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        mWebView.loadUrl("https://www.baidu.com/");
    }

}