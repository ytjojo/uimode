package com.wogoo.backgroud.drawable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aliya.uimode.core.CachedTypedValueArray;

/**
 * Author: xiaoqi
 * Date: 2022/8/17 3:13 下午
 * Description:
 */
public interface ITextViewOperator {
    void invoke(Context context, CachedTypedValueArray typedArray, TextView textView);
}
