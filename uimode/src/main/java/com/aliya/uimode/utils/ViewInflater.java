package com.aliya.uimode.utils;

import android.content.Context;
import androidx.collection.ArrayMap;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.TextView;

import com.aliya.uimode.R;
import com.aliya.uimode.widget.MaskDrawable;
import com.aliya.uimode.widget.MaskHelper;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * 通过反射创建View实例
 *
 * @author a_liYa
 * @date 2017/1/1 23:23.
 */
public class ViewInflater {

    private static final Object[] mConstructorArgs = new Object[2];
    private static final Map<String, Constructor<? extends View>> sConstructorMap
            = new ArrayMap<>();

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.view.",
            "android.webkit.",
            "android.app."
    };
    private static final Class<?>[] sConstructorSignature = new Class[]{
            Context.class, AttributeSet.class};


    public static View createViewFromTag(String name, Context context, AttributeSet attrs) {
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }

        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;

            if (-1 == name.indexOf('.')) {
                for (String classPrefix : sClassPrefixList) {
                    final View view = createView(context, name, classPrefix);
                    if (view != null) {
                        return view;
                    }
                }
                return null;
            } else {
                return createView(context, name, null);
            }
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater try
            return null;
        } finally {
            // Don't retain references on context.
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
    }


    public static void applyTextViewMaskDrawable(Context context, View view, AttributeSet attrs){
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
    }

    /**
     * 通过反射创建View
     */
    private static View createView(Context context, String name, String prefix)
            throws InflateException {

        Constructor<? extends View> constructor = sConstructorMap.get(name);

        try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                Class<? extends View> clazz = context.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater try
            return null;
        }
    }

}
