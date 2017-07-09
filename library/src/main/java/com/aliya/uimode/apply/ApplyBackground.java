package com.aliya.uimode.apply;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

/**
 * 应用android:background属性 {@link android.view.View}
 *
 * @author a_liYa
 * @date 2017/6/26 12:33.
 */
public final class ApplyBackground extends AbsApply {

    @Override
    public boolean onApply(View v, @AttrRes int attrId, Resources.Theme theme) {
        if (argsValid(v, attrId, theme)) {
            theme.resolveAttribute(attrId, sOutValue, true);
            switch (sOutValue.type) {
                case TypedValue.TYPE_INT_COLOR_ARGB4:
                case TypedValue.TYPE_INT_COLOR_ARGB8:
                case TypedValue.TYPE_INT_COLOR_RGB4:
                case TypedValue.TYPE_INT_COLOR_RGB8:
                    v.setBackgroundColor(sOutValue.data);
                    return true;
                case TypedValue.TYPE_STRING:
                    Drawable d = ContextCompat.getDrawable(v.getContext(), sOutValue.resourceId);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        v.setBackground(d);
                    } else {
                        v.setBackgroundDrawable(d);
                    }
                    // 没有使用下面此方法，防止resourceId相等时设置背景无效
//                    v.setBackgroundResource(sOutValue.resourceId);
                    return true;
            }

        }
        return false;
    }
}
