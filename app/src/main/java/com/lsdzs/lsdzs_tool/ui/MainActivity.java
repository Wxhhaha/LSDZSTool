package com.lsdzs.lsdzs_tool.ui;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleCmdMessage;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.databinding.ActivityMainBinding;
import com.lsdzs.lsdzs_tool.ui.settings.SettingActivity;
import com.lsdzs.lsdzs_tool.ui.update.IOTUpdateActivity;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.utils.LogUtil;

import java.util.UUID;

public class MainActivity extends BaseNoModelActivity<ActivityMainBinding> {

    private static final int READ_REQUEST_CODE = 1;
    private BluetoothClient client;
    private byte messageKey;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.topAppBar.setNavigationOnClickListener(view -> {
            if (client != null && ClientManager.getDevice() != null) {
                BleUtils.unNotifyBle(client, new BleUnnotifyResponse() {
                    @Override
                    public void onResponse(int i) {
                        BleUtils.disConnect(client);
                    }
                });
            }
            finish();
        });
        String[] options = getResources().getStringArray(R.array.option_items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        dataBinding.lv.setAdapter(adapter);
        dataBinding.lv.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0:
                    BleUtils.writeBle(client, BleCmdMessage.sendUnLockMessage(messageKey), writeResponse);
                    break;
                case 1:
                    BleUtils.writeBle(client, BleCmdMessage.sendLockMessage(messageKey), writeResponse);
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4://IOT升级
                    ActivityUtils.startActivity(IOTUpdateActivity.class);
                    break;
                case 5:
                    ActivityUtils.startActivity(SettingActivity.class);
                    break;
            }
        });

//        dataBinding.bt.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("*/*");
//            startActivityForResult(intent, READ_REQUEST_CODE);
//        });
    }

    @Override
    protected void initData() {
        setBle();
    }

    private void setBle() {
        client = ClientManager.getClient(this);
        BleUtils.registerConnectStatus(client, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int i) {
                switch (i) {
                    case STATUS_CONNECTED:
                        ToastUtils.showShort(R.string.ble_connected);
                        break;
                    case STATUS_DISCONNECTED:
                        ToastUtils.showShort(R.string.ble_disconnected);
                        ClientManager.setDevice(null);
                        if (ActivityUtils.getTopActivity().equals(MainActivity.this)) {
                            finish();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        BleUtils.notifyBle(client, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                LogUtil.d(CustomUtil.byte2hex(bytes));
            }

            @Override
            public void onResponse(int i) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BleUtils.writeBle(client, BleCmdMessage.sendKeyMessage(), writeResponse);
            }
        });
    }


    private BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                LogUtils.i("Uripath: " + uri.getPath());
                byte[] bytes = UriUtils.uri2Bytes(uri);
                LogUtils.i("len: " + bytes.length);
            }
        }
    }
}