package com.lsdzs.lsdzs_tool.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.databinding.ActivityQrscanBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.utils.LogUtil;
import com.xiaoguang.widget.mlkitscanner.ScanManager;
import com.xiaoguang.widget.mlkitscanner.model.ScanConfig;

import java.util.ArrayList;

public class QRscanActivity extends BaseNoModelActivity<ActivityQrscanBinding> implements View.OnClickListener {
    @Override
    protected int getLayout() {
        return R.layout.activity_qrscan;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.ivQr.setOnClickListener(this);
        dataBinding.ivClear.setOnClickListener(this);
        dataBinding.tvConnect.setOnClickListener(this);
        dataBinding.etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    dataBinding.ivClear.setVisibility(View.VISIBLE);
                } else {
                    dataBinding.ivClear.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_qr:
                scanQr();
                break;
            case R.id.iv_clear:
                dataBinding.etCode.setText("");
                break;
            case R.id.tv_connect:
                if (dataBinding.etCode.getText() != null && !dataBinding.etCode.getText().toString().isEmpty()) {
//                    sendClientId = dataBinding.etCode.getText().toString();
//                    sendMessage(dataBinding.etCode.getText().toString());
                    String id = dataBinding.etCode.getText().toString();
                    Intent intent = new Intent(getApplicationContext(), RemoteFunctionActivity.class);
                    intent.putExtra("ebikeId", id);
                    startActivity(intent);
                } else {
                    ToastUtils.showShort("请录入编码");
                }
                break;
        }
    }

    public void scanQr() {
        ScanConfig scanConfig = new ScanConfig.Builder()
                //设置完成震动
                .isShowVibrate(true)
                //扫描完成声音
                .isShowBeep(true)
                //显示相册功能
                .isShowPhotoAlbum(true)
                //显示闪光灯
                .isShowLightController(false)
                //网格扫描线的列数
                .setGridScanLineColumn(30)
                //网格高度
                .setGridScanLineHeight(300)
                //是否全屏扫描,默认全屏
                .setFullScreenScan(true).builder();


        ScanManager.startScan(QRscanActivity.this, scanConfig, (resultCode, data) -> {
            switch (resultCode) {
                case ScanManager.RESULT_SUCCESS:
                    ArrayList<String> resultList = data.getStringArrayListExtra(ScanManager.INTENT_KEY_RESULT_SUCCESS);
                    LogUtil.d(resultList.get(0));
                    dataBinding.etCode.setText(resultList.get(0));
                    break;
                case ScanManager.RESULT_FAIL:
                    String resultError = data.getStringExtra(ScanManager.INTENT_KEY_RESULT_ERROR);
                    LogUtil.d(resultError);
                    ToastUtils.showShort("扫描出错");
                    break;
                case ScanManager.RESULT_CANCLE:
                    //                                        showToast("取消扫码");
                    break;
            }
        });
    }
}