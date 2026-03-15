package com.wogoo.backgroud;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliya.uimode.core.CachedTypedValueArray;
import com.aliya.uimode.core.CachedTypedValueArrayPool;
import com.wogoo.backgroud.drawable.DrawableFactory;
import com.wogoo.backgroud.drawable.TextViewFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BackgroundTypedArrayDelegate {


    private static final HashMap<String, HashMap<String, Method>> methodMap = new HashMap<>();


    private static CachedTypedValueArray getTypedArray(View v, int[] styleableAttrs, Map<int[], CachedTypedValueArray> typedArrayMap) {
        CachedTypedValueArray typedArray = typedArrayMap.get(styleableAttrs);
        if (typedArray != null) {
            int count = typedArray.getIndexCount();
            for (int i = 0; i < count; i++) {
                int index = typedArray.getIndex(i);
                TypedValue typedValue = typedArray.peekValue(index);
                if(typedValue != null && typedValue.type != TypedValue.TYPE_NULL && typedValue.resourceId != 0){
                    typedValue.data = v.getResources().getColor(typedValue.resourceId);
                }
            }

            return typedArray;
        } else {
            return CachedTypedValueArrayPool.INSTANCE.getDefault();
        }
    }

    @Nullable
    public static View setViewBackground(Context context, Map<int[], CachedTypedValueArray> typedArrayMap, View view) {
        CachedTypedValueArray typedArray = getTypedArray(view,com.noober.background. R.styleable.background, typedArrayMap);
        CachedTypedValueArray pressTa = getTypedArray(view, com.noober.background.R.styleable.background_press, typedArrayMap);
        CachedTypedValueArray selectorTa = getTypedArray(view, com.noober.background.R.styleable.background_selector, typedArrayMap);
        CachedTypedValueArray textTa = getTypedArray(view,com.noober.background. R.styleable.text_selector, typedArrayMap);
        CachedTypedValueArray buttonTa = getTypedArray(view,com.noober.background. R.styleable.background_button_drawable, typedArrayMap);
        CachedTypedValueArray otherTa = getTypedArray(view,com.noober.background. R.styleable.bl_other, typedArrayMap);
        CachedTypedValueArray animTa = getTypedArray(view,com.noober.background. R.styleable.bl_anim, typedArrayMap);
        CachedTypedValueArray multiSelTa = getTypedArray(view,com.noober.background. R.styleable.background_multi_selector, typedArrayMap);
        CachedTypedValueArray multiTextTa = getTypedArray(view,com.noober.background. R.styleable.background_multi_selector_text, typedArrayMap);
        CachedTypedValueArray textViewTa = getTypedArray(view,com.noober.background.  R.styleable.bl_text, typedArrayMap);

        try {
            if (typedArray.getIndexCount() == 0 && selectorTa.getIndexCount() == 0 && pressTa.getIndexCount() == 0
                    && textTa.getIndexCount() == 0 && buttonTa.getIndexCount() == 0 && animTa.getIndexCount() == 0
                    && multiSelTa.getIndexCount() == 0 && multiTextTa.getIndexCount() == 0 && textViewTa.getIndexCount() == 0
                    && otherTa.getIndexCount() == 0) {
                return view;
            }
            if (view == null) {
                return null;
            }
            //R.styleable.background_selector 和 R.styleable.background_multi_selector的属性不能同时使用
            if (selectorTa.getIndexCount() > 0 && multiSelTa.getIndexCount() > 0) {
                throw new IllegalArgumentException("Background_selector and background_multi_selector cannot be used simultaneously");
            }
            if (textTa.getIndexCount() > 0 && multiTextTa.getIndexCount() > 0) {
                throw new IllegalArgumentException("text_selector and background_multi_selector_text cannot be used simultaneously");
            }

            GradientDrawable drawable = null;
            StateListDrawable stateListDrawable = null;
            if (buttonTa.getIndexCount() > 0 && view instanceof CompoundButton) {
                view.setClickable(true);
                ((CompoundButton) view).setButtonDrawable(DrawableFactory.getButtonDrawable(typedArray, buttonTa));
            } else if (selectorTa.getIndexCount() > 0) {
                stateListDrawable = DrawableFactory.getSelectorDrawable(typedArray, selectorTa);
                view.setClickable(true);
                setDrawable(stateListDrawable, view, otherTa, typedArray);
            } else if (pressTa.getIndexCount() > 0) {
                drawable = DrawableFactory.getDrawable(typedArray);
                stateListDrawable = DrawableFactory.getPressDrawable(drawable, typedArray, pressTa);
                view.setClickable(true);
                setDrawable(stateListDrawable, view, otherTa, typedArray);
            } else if (multiSelTa.getIndexCount() > 0) {
                stateListDrawable = DrawableFactory.getMultiSelectorDrawable(context, multiSelTa, typedArray);
                setBackground(stateListDrawable, view, typedArray);
            } else if (typedArray.getIndexCount() > 0) {
                drawable = DrawableFactory.getDrawable(typedArray);
                setDrawable(drawable, view, otherTa, typedArray);
            } else if (animTa.getIndexCount() > 0) {
                AnimationDrawable animationDrawable = DrawableFactory.getAnimationDrawable(animTa);
                setBackground(animationDrawable, view, typedArray);
                if (animTa.getBoolean(com.noober.background.R.styleable.bl_anim_bl_anim_auto_start, false)) {
                    animationDrawable.start();
                }
            }

            if (view instanceof TextView && textTa.getIndexCount() > 0) {
                ((TextView) view).setTextColor(DrawableFactory.getTextSelectorColor(textTa));
            } else if (view instanceof TextView && multiTextTa.getIndexCount() > 0) {
                ((TextView) view).setTextColor(DrawableFactory.getMultiTextColorSelectorColorCreator(context, multiTextTa));
            } else if (view instanceof TextView && textViewTa.getIndexCount() > 0) {
                TextViewFactory.setTextGradientColor(context, textViewTa, (TextView) view);
            }

            if (typedArray.getBoolean(com.noober.background.R.styleable.background_bl_ripple_enable, false) &&
                    typedArray.hasValue(com.noober.background.R.styleable.background_bl_ripple_color)) {
                int color = typedArray.getColor(com.noober.background.R.styleable.background_bl_ripple_color, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable contentDrawable = (stateListDrawable == null ? drawable : stateListDrawable);
                    RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(color), contentDrawable, contentDrawable);
                    view.setClickable(true);
                    setBackground(rippleDrawable, view, typedArray);
                } else if (stateListDrawable == null) {
                    StateListDrawable tmpDrawable = new StateListDrawable();
                    GradientDrawable unPressDrawable = DrawableFactory.getDrawable(typedArray);
                    unPressDrawable.setColor(color);
                    tmpDrawable.addState(new int[]{-android.R.attr.state_pressed}, drawable);
                    tmpDrawable.addState(new int[]{android.R.attr.state_pressed}, unPressDrawable);
                    view.setClickable(true);
                    setDrawable(tmpDrawable, view, otherTa, typedArray);
                }
            }

            if (otherTa.hasValue(com.noober.background.R.styleable.bl_other_bl_function)) {
                String methodName = otherTa.getString(com.noober.background.R.styleable.bl_other_bl_function);
                if (!TextUtils.isEmpty(methodName)) {
                    final Context currentContext = view.getContext();
                    final Class parentClass = currentContext.getClass();
                    final Method method = getMethod(parentClass, methodName);
                    if (method != null) {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    method.invoke(currentContext);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            return view;
        } finally {
            typedArray.recycle();
            pressTa.recycle();
            selectorTa.recycle();
            textTa.recycle();
            buttonTa.recycle();
            otherTa.recycle();
            animTa.recycle();
            multiSelTa.recycle();
            multiTextTa.recycle();
            textViewTa.recycle();
        }
    }


    private static void setDrawable(Drawable drawable, View view, CachedTypedValueArray otherTa, CachedTypedValueArray typedArray) {

        if (view instanceof TextView) {
            if (otherTa.hasValue(com.noober.background.R.styleable.bl_other_bl_position)) {
                if (otherTa.getInt(com.noober.background.R.styleable.bl_other_bl_position, 0) == 1) {
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    ((TextView) view).setCompoundDrawables(drawable, null, null, null);
                } else if (otherTa.getInt(com.noober.background.R.styleable.bl_other_bl_position, 0) == 2) {
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    ((TextView) view).setCompoundDrawables(null, drawable, null, null);
                } else if (otherTa.getInt(com.noober.background.R.styleable.bl_other_bl_position, 0) == 4) {
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    ((TextView) view).setCompoundDrawables(null, null, drawable, null);
                } else if (otherTa.getInt(com.noober.background.R.styleable.bl_other_bl_position, 0) == 8) {
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    ((TextView) view).setCompoundDrawables(null, null, null, drawable);
                }
            } else {
                setBackground(drawable, view, typedArray);
            }
        } else {
            setBackground(drawable, view, typedArray);
        }

    }


    private static void setBackground(Drawable drawable, View view, CachedTypedValueArray typedArray) {
        if (typedArray.hasValue(com.noober.background.R.styleable.background_bl_stroke_width) && typedArray.hasValue(com.noober.background.R.styleable.background_bl_stroke_position)) {
            //bl_stroke_position flag默认值
            int left = 1 << 1;
            int top = 1 << 2;
            int right = 1 << 3;
            int bottom = 1 << 4;
            float width = typedArray.getDimension(com.noober.background.R.styleable.background_bl_stroke_width, 0f);
            int position = typedArray.getInt(com.noober.background.R.styleable.background_bl_stroke_position, 0);
            float leftValue = hasStatus(position, left) ? width : -width;
            float topValue = hasStatus(position, top) ? width : -width;
            float rightValue = hasStatus(position, right) ? width : -width;
            float bottomValue = hasStatus(position, bottom) ? width : -width;
            drawable = new LayerDrawable(new Drawable[]{drawable});
            ((LayerDrawable) drawable).setLayerInset(0, (int) leftValue, (int) topValue, (int) rightValue, (int) bottomValue);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    private static boolean hasStatus(int flag, int status) {
        return (flag & status) == status;
    }


    private static Method getMethod(Class clazz, String methodName) {
        Method method = null;
        HashMap<String, Method> methodHashMap = methodMap.get(clazz.getCanonicalName());
        if (methodHashMap != null) {
            method = methodMap.get(clazz.getCanonicalName()).get(methodName);
        } else {
            methodHashMap = new HashMap<>();
            methodMap.put(clazz.getCanonicalName(), methodHashMap);
        }
        if (method == null) {
            method = findMethod(clazz, methodName);
            if (method != null) {
                methodHashMap.put(methodName, method);
            }
        }
        return method;
    }


    private static Method findMethod(Class clazz, String methodName) {
        Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            method = findDeclaredMethod(clazz, methodName);
        }
        return method;
    }

    private static Method findDeclaredMethod(Class clazz, String methodName) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null) {
                method = findDeclaredMethod(clazz.getSuperclass(), methodName);
            }
        }
        return method;
    }
}