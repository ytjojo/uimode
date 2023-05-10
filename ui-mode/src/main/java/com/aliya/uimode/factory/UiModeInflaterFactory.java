package com.aliya.uimode.factory;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aliya.uimode.R;
import com.aliya.uimode.mode.ResourceEntry;
import com.aliya.uimode.utils.AppResourceUtils;
import com.aliya.uimode.utils.AppUtil;
import com.aliya.uimode.utils.ViewInflater;
import com.aliya.uimode.widget.MaskDrawable;
import com.aliya.uimode.widget.MaskHelper;
import com.aliya.uimode.widget.MaskImageView;

import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


/**
 * Xml创建View拦截器 - Factory2
 *
 * @author a_liYa
 * @date 2016/11/24 19:20.
 */
public class UiModeInflaterFactory implements LayoutInflater.Factory2 {

    private static ThreadLocal<Map<String, ResourceEntry>> sAttrIdsLocal = new ThreadLocal<>();

    private LayoutInflater.Factory2 mInflaterFactory;

    public UiModeInflaterFactory(LayoutInflater.Factory2 factory) {
        mInflaterFactory = factory;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return uiModeCreateView(parent, name, context, attrs);
    }

    /**
     * 拦截创建具有UiMode属性的View, 通过{@link View#setTag(int, Object)}携带属性资源
     *
     * @param parent  parent
     * @param name    class name
     * @param context context
     * @param attrs   AttributeSet
     * @return 返回创建的View
     */
    private View uiModeCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Activity activity = AppUtil.findActivity(context);
        AppResourceUtils.correctConfigUiMode(context , activity);

        View view = null;
        switch (name) { // 拦截所有的ImageView、AppCompatImageView
            case "ImageView":
            case "android.support.v7.widget.AppCompatImageView":
                view = new MaskImageView(context, attrs);
                break;
            default:
                if (activity instanceof AppCompatActivity) {
                    /**
                     * @see androidx.appcompat.app.AppCompatDelegateImpl#createView(View, String, Context, AttributeSet)
                     */
                    AppCompatDelegate delegate = ((AppCompatActivity) activity).getDelegate();
                    view = delegate.createView(parent, name, context, attrs);
                }
                if (view == null) {
                    if (mInflaterFactory != null) {
                        view = mInflaterFactory.onCreateView(parent, name, context, attrs);
                    }
                }
                break;
        }

        if (view == null) { // 系统没有创建
            view = ViewInflater.createViewFromTag(name, context, attrs);
        }
        if(view != null){
            UiModeDelegate.INSTANCE.onViewCreated(view,attrs);
        }
        return onInterceptView(context, attrs, view);
    }

    private View onInterceptView(Context context, AttributeSet attrs, View view) {
        if (view instanceof TextView) {
            MaskHelper maskHelper = new MaskHelper(context, attrs);
            view.setTag(R.id.tag_ui_mode_mask_drawable, maskHelper);
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            boolean ifTrue = false;
            for (int i = 0; i < drawables.length; i++) {
                if (drawables[i] != null) {
                    drawables[i] = new MaskDrawable(drawables[i], maskHelper);
                    ifTrue = true;
                }
            }
            if (ifTrue) {
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                        drawables[0], drawables[1], drawables[2], drawables[3]);
            }
        }
        return view;
    }

    private int parseAttrId(String attrVal) {
        if (!TextUtils.isEmpty(attrVal) && attrVal.startsWith("?")) {
            String subStr = attrVal.substring(1, attrVal.length());
            try {
                Integer attrId = Integer.valueOf(subStr);
                return attrId;

            } catch (Exception e) {
                // no-op
            }
        }
        return ViewStore.NO_ID;
    }

    private int parseResId(String attrVal) {
        if (!TextUtils.isEmpty(attrVal) && attrVal.startsWith("@")) {
            String subStr = attrVal.substring(1, attrVal.length());
            try {
                return Integer.valueOf(subStr);
            } catch (Exception e) {
                // no-op
            }
        }
        return ViewStore.NO_ID;
    }

}
