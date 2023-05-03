package com.lsdzs.lsdzs_tool.ble;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.lsdzs.lsdzs_tool.R;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/12/1.
 */

public class CustomNumberPicker extends NumberPicker{
    public CustomNumberPicker(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNumberPicker(Context context) {
        super(context);
    }

    @Override
    public void addView(View child) {
        this.addView(child, null);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        this.addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setNumberPicker(child);
    }

    /**
     * 设置CustomNumberPicker的属性 颜色 大小
     * @param view
     */
    public void setNumberPicker(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextColor(getResources().getColor(R.color.white));
            ((EditText) view).setTextSize(18);
        }
    }

    /**
     * 设置分割线的颜色值
     * @param numberPicker
     */
    public void setNumberPickerDividerColor(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(getResources().getColor(R.color.num_dialog_divider)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
