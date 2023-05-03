package com.lsdzs.lsdzs_tool.ble;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.lsdzs.lsdzs_tool.R;

/**
 * Created by Administrator on 2017/7/29.
 */

public class DialogUtil {
    /**
     * 数字选择对话框
     *
     * @param context
     * @param title    标题
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param value    默认值
     */
    public static void showNumberPickerDialog(final boolean isNeedVerify, Context context, String title, float minValue, float maxValue, float value, final NumberPickerConfirm listener) {
        final NumberPickerDialog dialog = new NumberPickerDialog(context, title, minValue, maxValue, value);
        dialog.setClicklistener(new NumberPickerDialog.ClickListenerInterface() {
            @Override
            public void doConfirm(String value, int index) {
                if (!isNeedVerify) {
                    dialog.dismiss();
                    listener.doConfirm(dialog, value);
                } else {
                    listener.doConfirm(dialog, value);
                }
            }

        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        dialog.show();
    }

    public interface NumberPickerConfirm {
        void doConfirm(Dialog dialog, String value);
    }
}
