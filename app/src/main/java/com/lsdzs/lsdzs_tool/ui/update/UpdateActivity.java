package com.lsdzs.lsdzs_tool.ui.update;

import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.databinding.ActivityUpdateBinding;
import com.lsdzs.lsdzs_tool.ui.update.IOTUpdateActivity;
import com.wxh.basiclib.base.BaseNoModelActivity;

public class UpdateActivity extends BaseNoModelActivity<ActivityUpdateBinding> {
    @Override
    protected int getLayout() {
        return R.layout.activity_update;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
        dataBinding.tvIotUpdate.setOnClickListener(view -> {
            ActivityUtils.startActivity(IOTUpdateActivity.class);
        });
        dataBinding.tvMeterUpdate.setOnClickListener(view -> {
            ActivityUtils.startActivity(MeterUpdateActivity.class);
        });
        dataBinding.tvControllerUpdate.setOnClickListener(view -> {
            ActivityUtils.startActivity(ControllerUpdateActivity2.class);
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        if(ClientManager.getDevice()!=null){
            BleUtils.disConnect(ClientManager.getClient(this));
        }
        super.onDestroy();
    }
}