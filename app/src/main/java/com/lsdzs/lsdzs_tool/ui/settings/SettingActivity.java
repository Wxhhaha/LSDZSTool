package com.lsdzs.lsdzs_tool.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleCmdMessage;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSCmd;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSResponse;
import com.lsdzs.lsdzs_tool.databinding.ActivitySettingBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;

import java.util.UUID;

public class SettingActivity extends BaseNoModelActivity<ActivitySettingBinding> {

    @Override
    protected int getLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
        String[] options = getResources().getStringArray(R.array.setting_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        dataBinding.lv.setAdapter(adapter);
        dataBinding.lv.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(getApplicationContext(),SettingDetailActivity.class);
            intent.putExtra("select",i);
            startActivity(intent);
        });
    }
    private boolean isReadMode;
    @Override
    protected void initData() {
        isReadMode = false;
        showDialog("Loading...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isReadMode){
                    writeTCMessage(CANBUSCmd.enterControllerSet(0x8006));
                    handler.postDelayed(this,3000);
                }
            }
        },3000);
        CANBUSCmd.setKeys(0, 0, 0, 0);
        BleUtils.notifyBle(ClientManager.getClient(this), new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                if (bytes.length < 5) {
                    return;
                }
                int[] result = CANBUSResponse.dealResponse(bytes);
                if (result.length < 1) {
                    return;
                }
                if (result[0] == 65029) {
                    int address = result[1];
                    if (address == 0x8006) {//已进入查询模式
                        //开始发送需要查询的数据
                        handler.removeCallbacksAndMessages(null);
                        dismissDialog();
                    }
                }
            }

            @Override
            public void onResponse(int i) {
                writeTCMessage(CANBUSCmd.enterControllerSet(0x8006));
            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            return false;
        }
    });

    /**
     * 发送透传数据
     *
     * @param value
     */
    private void writeTCMessage(byte[] value) {
        byte[] data = ArrayUtils.add(value, 0, (byte) 0x33);
        BleUtils.writeBle(ClientManager.getClient(this), data, new BleWriteResponse() {
            @Override
            public void onResponse(int i) {
                if (i == -1) {
                    BleUtils.writeBle(ClientManager.getClient(SettingActivity.this), data, this);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        BleUtils.unNotifyBle(ClientManager.getClient(this), new BleUnnotifyResponse() {
            @Override
            public void onResponse(int i) {

            }
        });
        super.onPause();
    }
}