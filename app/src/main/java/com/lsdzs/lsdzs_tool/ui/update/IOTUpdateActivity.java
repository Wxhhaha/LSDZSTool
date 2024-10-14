package com.lsdzs.lsdzs_tool.ui.update;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.FileUtil;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleDataConvertUtil;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.ble.UpdateMessage;
import com.lsdzs.lsdzs_tool.databinding.ActivityIotupdateBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.ble.IOTCmd;
import com.wxh.basiclib.utils.LogUtil;

import java.util.UUID;

public class IOTUpdateActivity extends BaseNoModelActivity<ActivityIotupdateBinding> {
    private byte[] dataBytes;
    private BluetoothClient client;
    private int errorCount = 0;
    private int totalCount;
    private int currentCount;
    private String UPDATE_CMD = "3A6F0101B1FC0D0A";

    private int repeatCount = 0;//重复次数

    private boolean hasStartWrite = false;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case SEND_DATA_TIMEOUT://发送没有回复，再发一次
                    if (repeatCount < 3) {
                        writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                        handler.sendEmptyMessageDelayed(SEND_DATA_TIMEOUT, SEND_DATA_TIMEOUT_VALUE);
                        repeatCount++;
                    }else{
                        hasStartWrite = false;
                       handler.sendEmptyMessage(UPDATE_FAIL);
                    }

                    break;
                case UPDATE_FAIL:
                    dataBinding.cm.stop();
                    dataBinding.message.setText("升级失败");
                    dataBinding.btStart.setEnabled(true);
                    break;
            }

            return false;
        }
    });

    private static final int SEND_DATA_TIMEOUT = 11;

    private static final int UPDATE_FAIL = 12;

    private static final int SEND_DATA_TIMEOUT_VALUE = 1000;

    @Override
    protected int getLayout() {
        return R.layout.activity_iotupdate;
    }

    @Override
    protected void initView(Bundle bundle) {
        if(ClientManager.getDevice()!=null){
          dataBinding.topAppBar.setTitle(ClientManager.getDevice().getName());
        }
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    String filename = FileUtil.getFileNameFromUri(IOTUpdateActivity.this, uri);
                    dataBinding.tvPath.setText(filename);
                    dataBytes = UriUtils.uri2Bytes(uri);
                    dataBinding.btStart.setVisibility(View.VISIBLE);
                }
            }
        });
        dataBinding.btSelect.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            launcher.launch(intent);
        });

        dataBinding.btStart.setOnClickListener(view -> {
            totalCount = dataBytes.length / 64;
            currentCount = 0;

            dataBinding.message.setText("开始升级");
            dataBinding.btStart.setEnabled(false);
            dataBinding.message.setVisibility(View.VISIBLE);
            dataBinding.pb.setVisibility(View.VISIBLE);

            writeMessage(IOTCmd.sendKeyMessage(ClientManager.getDevice().getMac()));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeMessage(UpdateMessage.enterOtaCmd());
                }
            }, 1000);
            dataBinding.cm.setBase(SystemClock.elapsedRealtime());
            dataBinding.cm.start();

        });

        dataBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void initData() {
        client = ClientManager.getClient(this);
        BleUtils.registerConnectStatus(client, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int i) {
                switch (i) {
                    case STATUS_CONNECTED:
                        ToastUtils.showShort(R.string.ble_connected);
                        finish();
                        break;
                    case STATUS_DISCONNECTED:
                        ToastUtils.showShort(R.string.ble_disconnected);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });
        BleUtils.notifyBle(client, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                LogUtil.e(CustomUtil.byte2hex(bytes));
                if(CustomUtil.byte2hex(bytes).startsWith("a109")){
                    return;
                }
                byte[] resData = UpdateMessage.dealMCUResponse(bytes);
                if (resData.length < 2) {
                    if(hasStartWrite){
                        if(repeatCount==0){
                            writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                            handler.sendEmptyMessageDelayed(SEND_DATA_TIMEOUT, SEND_DATA_TIMEOUT_VALUE);
                            repeatCount++;
                        }
                    }
//                    else if(!dataBinding.btStart.isEnabled()){
//                        handler.sendEmptyMessage(UPDATE_FAIL);
//                    }
                    return;
                }
                switch (resData[0]) {
                    case 0x42://确认进入升级功能，开始发送升级数据
                        hasStartWrite = true;
                        dataBinding.message.setText("写入数据");
                        writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                        break;
                    case 0x44://升级数据确认结果
                        handler.removeMessages(SEND_DATA_TIMEOUT);
                        repeatCount = 0;
                        switch (resData[1]) {
                            case (byte) 0xA0:
                                errorCount = 0;
                                //成功，发送下一条
                                currentCount++;
                                dataBinding.pb.setProgress(currentCount * 100 / totalCount);
                                writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                                handler.sendEmptyMessageDelayed(SEND_DATA_TIMEOUT, SEND_DATA_TIMEOUT_VALUE);
                                break;
                            case (byte) 0xA1:
                                errorCount++;
                                ToastUtils.showShort("Flash擦除错误");
                                if (errorCount < 4) {
                                    writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                                } else {
                                    hasStartWrite = false;
                                    handler.sendEmptyMessage(UPDATE_FAIL);
                                }
                                break;
                            case (byte) 0xA2:
                                errorCount++;
                                ToastUtils.showShort("Flash写入错误");
                                if (errorCount < 4) {
                                    writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                                } else {
                                    hasStartWrite = false;
                                    handler.sendEmptyMessage(UPDATE_FAIL);
                                }
                                break;
                            case (byte) 0xA3:
                                errorCount++;
                                ToastUtils.showShort("其它错误");
                                if (errorCount < 4) {
                                    writeMessage(UpdateMessage.prepareCommCmd(dataBytes));
                                } else {
                                    hasStartWrite = false;
                                    handler.sendEmptyMessage(UPDATE_FAIL);
                                }
                                break;
                        }
                        break;
                    case 0x46:
                        handler.removeMessages(SEND_DATA_TIMEOUT);
                        repeatCount = 0;
                        if (resData[1] == (byte) 0xA0) {
                            dataBinding.message.setText("MCU复位，请等待");
                            writeMessage(UpdateMessage.resetMCU());
                        } else if (resData[1] == (byte) 0xA1) {
                            dataBinding.message.setText("校验失败");
                            dataBinding.btStart.setEnabled(true);
                        }
                        break;
                    case 0x48:
                        hasStartWrite = false;
                        if (resData[1] == (byte) 0xA0) {
                            dataBinding.message.setText("升级成功");
                            dataBinding.cm.stop();
                            dataBinding.pb.setProgress(100);
                        } else {
                            handler.sendEmptyMessage(UPDATE_FAIL);
                        }
                        dataBinding.btStart.setEnabled(true);
                        break;
                }
            }

            @Override
            public void onResponse(int i) {

            }
        });
    }

    private BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    private void writeMessage(byte[] message) {
        BleUtils.writeBle(client, message, writeResponse);
    }
}
