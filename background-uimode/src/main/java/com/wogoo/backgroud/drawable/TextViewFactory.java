package com.wogoo.backgroud.drawable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aliya.uimode.core.CachedTypedValueArray;


/**
 * Author: xiaoqi
 * Date: 2022/8/17 3:11 下午
 * Description:
 */
public class TextViewFactory {

    public static void setTextGradientColor(Context context, CachedTypedValueArray typedValueArray, final TextView textView){
       new TextViewGradientColor().invoke(context, typedValueArray, textView);
    }
}
