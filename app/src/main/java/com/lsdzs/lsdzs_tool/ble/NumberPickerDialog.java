package com.lsdzs.lsdzs_tool.ble;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.lsdzs.lsdzs_tool.R;

/**
 * Created by Administrator on 2016/11/30.
 */

public class NumberPickerDialog extends Dialog {
    private ClickListenerInterface clickListenerInterface;
    private Context context;
    private String title;
    private int minValue1, minValue2;
    private int maxValue1, maxValue2;
    private int value1, value2;
    private int num;
    private int num1;
    private int num2;
    private String[] datas;
    private int valueIndex;
    private boolean isString;

    public NumberPickerDialog(Context context) {
        super(context);
    }

    public NumberPickerDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public NumberPickerDialog(Context context, String title, String[] datas, int value) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.title = title;
        this.datas = datas;
        this.valueIndex = value;
        isString = true;
        this.num = 1;
    }

    public NumberPickerDialog(Context context, String title, float minValue, float maxValue, float value) {
        super(context, R.style.MyDialog);
        this.context = context;
        isString = false;
        this.title = title;
        this.num = 1;
        minValue1 = (int) minValue;
        maxValue1 = (int) maxValue;
        value1 = (int) value;
        num1 = value1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);
        setContentView(view);

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        TextView tvConfirm = (TextView) view.findViewById(R.id.tv_submit);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvTitle.setText(title);
        CustomNumberPicker npv1 = (CustomNumberPicker) view.findViewById(R.id.npv_1);
        npv1.setNumberPickerDividerColor(npv1);
        npv1.setMaxValue(maxValue1);
        npv1.setMinValue(minValue1);
        npv1.setValue(value1);
        num1 = value1;

        npv1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                num1 = i1;
            }
        });

        tvConfirm.setOnClickListener(new clickListener());
        tvCancel.setOnClickListener(new clickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ScreenUtils.getScreenWidth();
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    public interface ClickListenerInterface {
        void doConfirm(String value, int index);
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.tv_submit:
                    String value;
                    if (isString) {
                        value = datas[num1];
                        clickListenerInterface.doConfirm(value, num1);
                        dismiss();
                    } else {
                        if (num == 1) {
                            value = num1 + "";
                        } else {
                            value = num1 + "." + num2;
                        }
                        clickListenerInterface.doConfirm(value, -1);
                    }
                    break;
                case R.id.tv_cancel:
                    dismiss();
                    break;
            }
        }

    }

}
