package com.lsdzs.lsdzs_tool.ui.update;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.lsdzs.lsdzs_tool.FileUtil;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.databinding.ActivityControllerUpdateBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.utils.LogUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerUpdateActivity extends BaseNoModelActivity<ActivityControllerUpdateBinding> {
    private static final int UPDATE_FAIL = 101;
    private final ExecutorService singleTaskExecutor = Executors.newSingleThreadExecutor();
    //    private String UPDATE_CMD = "3A6F0101B1FC0D0A";
    private String BOOT_CMD = "3A5F5253B3410D0A";
    private String resDataString;// 纯文本内容
    private boolean isBreak = false;
    //    private boolean isSend44Fail = false;
    private boolean isStartWrite = false;
    private boolean isWriteOK = false;
    private boolean getMPC = false;
    private boolean hasPromptMPC = false;
    private boolean isFail = false;
    private int errorTime = 0;
    private int repeatTime;
    private String[] resultArr;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case UPDATE_FAIL:
//                    mTimeCount.stop();
//                    isForceUpdate = false;
                    dataBinding.message.setText("升级失败");
//                    resetView();
                    break;
            }
            return false;
        }
    });
    private BluetoothClient client;

    @Override
    protected int getLayout() {
        return R.layout.activity_controller_update;
    }

    @Override
    protected void initView(Bundle bundle) {
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    dataBinding.tvPath.setText(uri.getPath());
//                    byte[] bytes = UriUtils.uri2Bytes(uri);

//                    File file = UriUtils.uri2File(uri);

//                    resDataString = FileUtil.readFileStringData(file.getName());
                    resDataString = FileUtil.getStringFromUri(ControllerUpdateActivity.this, uri);
                    resultArr = resDataString.split("\n");
//                    LogUtils.e(resDataString);
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
//            writeMessage(CustomUtil.HexString2Bytes(UPDATE_CMD));
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
            startUpdate();
//                }
//            }, 500);
        });
    }

    @Override
    protected void initData() {
        client = ClientManager.getClient(this);
        BleUtils.registerConnectStatus(client, mConnectStatusListener);
        BleUtils.notifyBle(client, mNotifyRsp);
    }

    private boolean hasDoConnect;
    private BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            switch (status) {
                case STATUS_CONNECTED:
                    hasDoConnect = false;
                    LogUtil.d("STATUS_CONNECTED");
                    break;
                case STATUS_DISCONNECTED:
                    LogUtil.d("STATUS_DISCONNECTED");
                    // 在烧录过程中蓝牙连接断开
                    Toast.makeText(ControllerUpdateActivity.this, "蓝牙已断开", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    private BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(ClientManager.getservice().getUUID()) && character.equals(ClientManager.getNotifyCharacter().getUuid())) {
                LogUtil.e("返回" + CustomUtil.byte2hex(value));
                LogUtil.d(CustomUtil.bytetoString(value));
                if (CustomUtil.byte2hex(value).equals("00")) {
                    return;
                }
                String resultData = CustomUtil.bytetoString(value);
                if (dataBinding.tvPath.getText().toString().isEmpty()) {
                    return;
                }
                if (resultData.startsWith("MPC") && !isWriteOK) {
                    dataBinding.message.setText("开始升级");
                    //收到MPC
                    getMPC = true;
                    if (!isFail) {
                        // 烧录开始
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendFirmware();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                } else if (resultData.startsWith("OK")) { // 返回0K，代表成功
                    isWriteOK = true;
                } else if (resultData.contains("ER") || resultData.contains("ET")) { // 返回包含ER或ET，表示更新出错
                    if (isStartWrite) {
                        // 发送出现中断
                        isBreak = true;
                        errorTime++;
                        repeatTime = getERNum(resultData);
                    }
//                    else {
//                        isSend44Fail = true;
//                    }
                }
            }
        }

        @Override
        public void onResponse(int code) {

        }
    };

    private void startUpdate() {
        resetData();
        startBoot();
    }

    /**
     * 发送命令通知进入bootload
     */
    private void startBoot() {
        singleTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeMessage(CustomUtil.HexString2Bytes(BOOT_CMD));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!getMPC && !isFail) {
                    isFail = true;
                    isStartWrite = false;
                    hasPromptMPC = true;
                    dataBinding.message.setText("请重试");
//                    resetView();
                }
            }
        }, 10000);
    }

    private void resetData() {
        isBreak = false;
//        isSend44Fail = false;
        isStartWrite = false;
        isWriteOK = false;
        getMPC = false;
        errorTime = 0;
        isFail = false;
    }

    /**
     * 开始程序更新流程
     *
     * @throws InterruptedException 抛出的异常
     */
    private void sendFirmware() throws InterruptedException {
        // 发20个0x44
        byte[] byteArray = new byte[20];
        for (int i = 0; i < byteArray.length; i++) {
            if (i == 15) {
                if (resultArr.length >= 3840) {
                    byteArray[i] = (byte) 0x40;
                } else {
                    byteArray[i] = (byte) 0x41;
                }
            } else {
                byteArray[i] = (byte) 0x44;
            }
        }
        // 发送
//        for (int i = 0; i < 5; i++) {
//            isSend44Fail = false;
//            Thread.sleep(50);
        writeMessage(byteArray);
//        }
//        if (isSend44Fail) {
//            handler.sendEmptyMessage(UPDATE_FAIL);
//            return;
//        }
        // 等待100ms
        Thread.sleep(5 * 1000);
        sendRealData();
    }

    private LinkedList<String> splitString20(String s) {
        LinkedList<String> addressList = new LinkedList<>();
        int splitLength = s.length();
        while (splitLength >= 40) {
            String tmp = s.substring(0, 40);
            addressList.add(tmp);
            s = s.substring(40);
            splitLength = s.length();
        }
        if (splitLength != 0) {
            addressList.add(s);
        }
        return addressList;
    }

    private void sendRealData() throws InterruptedException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mTimeCount.setBase(SystemClock.elapsedRealtime());
//                mTimeCount.start();
                dataBinding.message.setText("写入数据");
            }
        });
        // 一行一行分开发送
//        List<String> datalist;
//        if (resultArr.length == 1) {
//            //每20进行分割
//            String result = resultArr[0];
//            datalist = splitString20(result);
//            for(String s1:datalist){
//                LogUtils.e(s1);
//            }
//        }else{
//            datalist = ArrayUtils.asArrayList(resultArr);
//        }

        // 循环一行一行取出
        for (int i = 0; i < resultArr.length; i++) {
            if (isFail) {
                break;
            }
            isStartWrite = true;
            if (errorTime > 5) {
                handler.sendEmptyMessage(UPDATE_FAIL);
                break;
            } else if (isBreak) {
                Thread.sleep(150);
                repeatSendData(i, resultArr);
                writeMessage(CustomUtil.HexString2Bytes(resultArr[i]));
                if (i % 64 == 0) {
                    Thread.sleep(100);
                } else {
                    Thread.sleep(50);
                }
                isBreak = false;
            } else {
                writeMessage(CustomUtil.HexString2Bytes(resultArr[i]));
                final int h = (int) ((float) (i + 1) / (float) resultArr.length * 100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataBinding.message.setText(h + "%");
                    }
                });

                // 当行数为1,65,129……等（行数-1）%64=0的行时，发完数据延时50ms再发下一行
                if (i % 64 == 0) {
                    Thread.sleep(100);
                } else {
                    Thread.sleep(50);
                }
            }
        }

        if (errorTime <= 5 && !isFail) {
            // 发20个0x55
            byte[] byteArray3 = new byte[20];
            for (int i = 0; i < byteArray3.length; i++) {
                byteArray3[i] = (byte) 0x55;
            }
            writeMessage(byteArray3);
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (isWriteOK) {
                        dataBinding.message.setText("烧录成功");
//                        hasStart = false;
//                        if (isForceUpdate) {
//                            isForceUpdate = false;
//                        }
                        dataBinding.btStart.setText("Start Update");
                    } else {
                        handler.sendEmptyMessage(UPDATE_FAIL);
                    }
                }
            }, 1000);
        }
    }

    private void repeatSendData(int position, String[] resultArr) throws InterruptedException {
        int time = repeatTime * 3;
        for (int i = time; i > 0; i--) {
            if (position - i >= 0) {
                writeMessage(CustomUtil.HexString2Bytes(resultArr[position - i]));
                if (position % 64 == 0) {
                    Thread.sleep(100);
                } else {
                    Thread.sleep(50);
                }
            }
        }
    }

    /**
     * 获取回滚条数
     *
     * @param str
     * @return
     */
    private int getERNum(String str) {
        String des1 = "ER";
        String des2 = "ET";
        int cnt = 0;
        int offset = 0;
        while ((offset = str.indexOf(des1, offset)) != -1) {
            offset = offset + des1.length();
            cnt++;
        }
        offset = 0;
        while ((offset = str.indexOf(des2, offset)) != -1) {
            offset = offset + des2.length();
            cnt++;
        }
        return cnt;
    }

    private BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    /**
     * 写入数据
     *
     * @param message
     */
    private void writeMessage(byte[] message) {
        BleUtils.writeBle(client, message, writeResponse);
    }
}