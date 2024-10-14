package com.lsdzs.lsdzs_tool.ui.update;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleMtuResponse;
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
import com.wxh.basiclib.ble.ControllerDataUtils;
import com.wxh.basiclib.utils.LogUtil;
import com.wxh.basiclib.utils.MathUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ControllerUpdateActivity2 extends BaseNoModelActivity<ActivityControllerUpdateBinding> {
    private static final int UPDATE_FAIL = 101;
    private final ExecutorService singleTaskExecutor = Executors.newSingleThreadExecutor();
    public String BOOT_CMD = "3A5F5253B3410D0A";
    private String resDataString;// 纯文本内容
    private boolean isBreak = false;
    private boolean isWriting = false;
    private boolean isWriteOK = false;
    private boolean isSuccess;
    private boolean isFail;
    private int errorTime = 0;
    private int repeatTime;
    private boolean isForceUpdate;
    private boolean hasDoConnect = false;

    private BluetoothClient client;

    private String[] resultArr;

    private int enhCheckNum;

    private String[] decyptResult;

    private final byte[] key_enh_hex = {(byte) 0x78, (byte) 0x64, (byte) 0x35, (byte) 0x6e, (byte) 0x5d, (byte) 0xe1, (byte) 0x14, (byte) 0x38, (byte) 0x16, (byte) 0xA1, (byte) 0x02, (byte) 0x8E, (byte) 0xE3, (byte) 0xD5, (byte) 0x32, (byte) 0x4B, (byte) 0x79, (byte) 0x62, (byte) 0x57, (byte) 0x76};

    //文件类型
    private int fileType;//0:lsh,1:enh,2:ech

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
                    enhCheckNum = 0;
                    decyptResult = null;
                    isSuccess = false;
                    isFail = false;
                    resetData();
                    dataBinding.rgType.clearCheck();
                    dataBinding.pb.setProgress(0);
                    Uri uri = result.getData().getData();
                    dataBinding.tvPath.setText(uri.getPath());
                    resDataString = FileUtil.getStringFromUri(ControllerUpdateActivity2.this, uri);
                    resultArr = resDataString.split("\n");
                    dataBinding.btStart.setEnabled(true);
                    dataBinding.btStart.setVisibility(View.VISIBLE);

                    String fileName = FileUtil.getFileNameFromUri(ControllerUpdateActivity2.this, uri);
                    LogUtil.e(fileName);
                    dataBinding.rgType.setVisibility(View.VISIBLE);
                    if (fileName.toLowerCase().endsWith(".lsh")) {
                        fileType = 0;
                        dataBinding.rbLsh.setChecked(true);
                    } else if (fileName.toLowerCase().endsWith("enh")) {
                        fileType = 1;
                        dataBinding.rbEnh.setChecked(true);
                    } else if (fileName.endsWith(".ech")) {
                        fileType = 2;
                        dataBinding.rbEch.setChecked(true);
                        if (decyptResult == null) {
                            decyptEchFile();
                        }
                    }
                }
            }
        });

        dataBinding.rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (dataBinding.rbLsh.isChecked()) {
                    BOOT_CMD = "3A5F5244B3410D0A";
                    fileType = 0;
                    if (enhCheckNum == 0) {
                        //获取校验码
                        enhCheckNum = checkSumAllOtaFileCode();
                        LogUtil.e("校验值" + enhCheckNum);
                    }
                    dataBinding.etTimeSplit.setText(35 + "");
                } else if (dataBinding.rbEnh.isChecked()) {
                    fileType = 1;
                    if (enhCheckNum == 0) {
                        //获取校验码
                        enhCheckNum = checkSumAllOtaFileCode();
                        LogUtil.e("校验值" + enhCheckNum);
                    }
                    dataBinding.etTimeSplit.setText(15 + "");
                    //判断升级指令中发41/42/43/44
                    /**
                     * 首先分析enh中数据解密后某行文件的前两个字节是否为F800，如果否：
                     * {
                     * enh文件行数<=3739行，发0x43
                     * 否则发0x44
                     * }
                     * 如果是
                     * {
                     * enh文件行数>3739行，或 （enh未解密之前某行包含040801F1字符，且长度小于20个字节）。发0x40
                     * 否则发0x41
                     * }
                     */
                    if (otaEnhDecrypt()) {
                        //有f800
                        if (resultArr.length > 3739 || checkIfContainEnhOtaStr()) {
                            BOOT_CMD = "3A5F5240B3410D0A";
                        } else {
                            BOOT_CMD = "3A5F5241B3410D0A";
                        }
                    } else {
                        if (resultArr.length <= 3739) {
                            BOOT_CMD = "3A5F5243B3410D0A";
                        } else {
                            BOOT_CMD = "3A5F5244B3410D0A";
                        }
                    }
                } else if (dataBinding.rbEch.isChecked()) {
                    fileType = 2;
                    BOOT_CMD = "3A5F5255B3410D0A";
                    if (decyptResult == null) {
                        decyptEchFile();
                    }
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
            dataBinding.btStart.setText("Stop Update");
            dataBinding.btStart.setEnabled(false);
            dataBinding.message.setText("");
            startUpdate();
        });

        dataBinding.topAppBar.setNavigationOnClickListener(view -> finish());
    }

    /**
     * 校验升级文件，得出一个校验码，将这个校验码与OTA结束后返回的，跟在MPC后面的校验码比较，一致表示成功，不一致表示失败
     *
     * @return
     */
    private int checkSumAllOtaFileCode() {
        int[] allFlashMem = new int[Integer.parseInt("f800", 16)];
        Arrays.fill(allFlashMem, (byte) 0xff);
        boolean after64K = false;
        for (int n = 0; n < resultArr.length; n++) {
            String cmdStr = resultArr[n];
            byte[] cmdData = ControllerDataUtils.HexString2Bytes(cmdStr);
            //解密字节
            int[] cellData = new int[20];
            Arrays.fill(cellData, 0x00);
            int cellLength = cmdData.length;
            if (cellLength < 20 && cmdStr.toUpperCase().contains("040801F1")) {
                for (int k = 0; k < 20; k++) {
                    cellData[k] = k;
                }
                after64K = true;
            } else {
                for (int k = 0; k < cellLength; k++) {
                    cellData[k] = cmdData[k] & 0xff;
                }
            }

            byte[] decryptArray = new byte[20];
            Arrays.fill(decryptArray, (byte) 0x00);
            for (int k = 0; k < 20; k++) {
                if (k != 2) {
                    decryptArray[k] = (byte) (cellData[k] ^ cellData[2] ^ key_enh_hex[k] ^ key_enh_hex[2]);
                }
            }
            decryptArray[2] = 0x00;
            //每一行的这16个数，取高低位合并相加
            int line_address = ((decryptArray[0] & 0xff) << 8) + (decryptArray[1] & 0xff);

            String verifyData = ControllerDataUtils.byte2hex(decryptArray);
//            LogUtil.e("校验地址" + line_address);
            LogUtil.e("校验值" + verifyData);

            if (line_address <= (Integer.parseInt("F800", 16) - Integer.parseInt("10", 16)) && !after64K) {
                for (int i = 0; i < 16; i++) {
                    allFlashMem[line_address + i] = decryptArray[i + 3];
                }
            }

        }
        int otaCheckSum = 0;
        for (int j = Integer.parseInt("1000", 16); j < Integer.parseInt("F800", 16); j = j + 2) {
            otaCheckSum += (((allFlashMem[j + 1]) & 0xff) << 8) + (allFlashMem[j] & 0xff);
        }
        //总校验值返回,取16进制后4位
        return otaCheckSum & 0xffff;
    }

    @Override
    protected void initData() {
        client = ClientManager.getClient(this);
        BleUtils.registerConnectStatus(client, mConnectStatusListener);
        BleUtils.notifyBle(client, mNotifyRsp);
    }

    /**
     * enh文件解密(倒数第5-24行)
     *
     * @return 解密后某一行前两个字节是否为F800
     */
    private boolean otaEnhDecrypt() {
        int totalLineNum = resultArr.length;
        for (int i = 5; i <= 24; i++) {
            String cmdstr = resultArr[totalLineNum - i];
            byte[] hexstr = ControllerDataUtils.HexString2Bytes(cmdstr);
            byte[] decryptArray = new byte[20];
            for (int j = 0; j < 20; j++) {
                if (j != 2) {
                    decryptArray[j] = (byte) (hexstr[j] ^ hexstr[2] ^ key_enh_hex[j] ^ key_enh_hex[2]);//解密
                }
            }
            if (decryptArray[0] == (byte) 0xf8 && decryptArray[1] == 0x00) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断enh未解密之前某行是否包含040801F1字符，且长度小于20个字节
     *
     * @return 包含true
     */
    private boolean checkIfContainEnhOtaStr() {
        for (String s : resultArr) {
            if (s.length() < 20 && s.toLowerCase().contains("040801f1")) {
                return true;
            }
        }
        return false;
    }

    private void decyptEchFile() {
        decyptResult = new String[resultArr.length];
        for (int n = 0; n < resultArr.length; n++) {
            String cmdStr = resultArr[n];
            byte[] cmdData = ControllerDataUtils.HexString2Bytes(cmdStr);
            byte[] decryptArray = new byte[20];
            Arrays.fill(decryptArray, (byte) 0x00);
            for (int k = 0; k < 20; k++) {
                if (k != 2) {
                    decryptArray[k] = (byte) (cmdData[k] ^ cmdData[2] ^ key_enh_hex[k] ^ key_enh_hex[2]);
                }
            }
            decryptArray[2] = 0x00;
            String verifyData = ControllerDataUtils.byte2hex(decryptArray);
            LogUtil.e(verifyData);
            decyptResult[n] = verifyData;
        }
    }

    private String decyptEchFile(String cmdStr) {
        byte[] cmdData = ControllerDataUtils.HexString2Bytes(cmdStr);
        byte[] decryptArray = new byte[20];
        Arrays.fill(decryptArray, (byte) 0x00);
        int[] cellData = new int[20];
        Arrays.fill(cellData, 0x00);
        if (cmdData.length < 20 && cmdStr.toUpperCase().contains("040801F1")) {
            for (int k = 0; k < 20; k++) {
                cellData[k] = k;
            }
        } else {
            for (int k = 0; k < cmdData.length; k++) {
                cellData[k] = cmdData[k] & 0xff;
            }
        }

        for (int k = 0; k < 20; k++) {
            if (k != 2) {
                decryptArray[k] = (byte) (cellData[k] ^ cellData[2] ^ key_enh_hex[k] ^ key_enh_hex[2]);
            }
        }
        decryptArray[2] = 0x00;
        String verifyData = ControllerDataUtils.byte2hex(decryptArray);
        LogUtil.e(verifyData);
        return verifyData;

    }

    private BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            switch (status) {
                case STATUS_CONNECTED:
                    hasDoConnect = false;
                    LogUtil.d("STATUS_CONNECTED");
                    if (isForceUpdate) {
                        dataBinding.message.setText("重连成功");
                    }
                    break;
                case STATUS_DISCONNECTED:
                    LogUtil.d("STATUS_DISCONNECTED");
                    // 在烧录过程中蓝牙连接断开
                    if (isWriting) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                XToast.error(ControllerUpdateActivity.this,"蓝牙已断开");
                                ToastUtils.showShort("蓝牙已断开");
                            }
                        });

                        dataBinding.cm.stop();
                        ClientManager.setDevice(null);
                        finish();
                    } else {
                        if (hasDoConnect || isSuccess || isFail) {
                            return;
                        }
                        dataBinding.message.setText("请手动开机。。。");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hasDoConnect = true;
                                BleUtils.connect(client, ClientManager.getDevice(), mConnectStatusListener, new BleUtils.ConnectResult() {
                                    @Override
                                    public void success() {
                                        BleUtils.notifyBle(client, mNotifyRsp);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                client.requestMtu(ClientManager.getDevice().getMac(), 200, new BleMtuResponse() {
                                                    @Override
                                                    public void onResponse(int i, Integer integer) {
                                                        if (isWriting) {
                                                            return;
                                                        }
                                                        dataBinding.message.setText("开始升级");
                                                        if (fileType == 2) {
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
//                                                                    singleTaskExecutor.execute(new Runnable() {
//                                                                        @Override
//                                                                        public void run() {
                                                                    try {
                                                                        dataBinding.cm.setBase(SystemClock.elapsedRealtime());
                                                                        dataBinding.cm.start();
                                                                        sendECHData();
                                                                    } catch (
                                                                            InterruptedException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
//                                                                        }
//                                                                    });
                                                                }
                                                            }, 3000);
                                                        } else {
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    dataBinding.cm.setBase(SystemClock.elapsedRealtime());
                                                                    dataBinding.cm.start();
                                                                    singleTaskExecutor.execute(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            try {
                                                                                sendLshEnhData();
                                                                            } catch (
                                                                                    InterruptedException e) {
                                                                                throw new RuntimeException(e);
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }, 2000);
                                                        }
                                                    }
                                                });
                                            }
                                        }, 1000);
                                    }

                                    @Override
                                    public void fail() {

                                    }
                                });
                            }
                        }, 3000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == UPDATE_FAIL) {
                isFail = true;
                resetView();
                isForceUpdate = false;
                dataBinding.message.setText("升级失败,稍后再试");
                LogUtil.e(dataBinding.message.getText().toString());
            }
            return false;
        }
    });

    private BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(ClientManager.getservice().getUUID()) && character.equals(ClientManager.getNotifyCharacter().getUuid())) {
                LogUtil.e("返回" + CustomUtil.byte2hex(value));
                LogUtil.d(CustomUtil.bytetoString(value));
                if (dataBinding.tvPath.getText().toString().isEmpty()) {
                    return;
                }
                if (isSuccess || isFail) {
                    return;
                }
                if (CustomUtil.byte2hex(value).equals("00") && isWriting && fileType == 2) {
                    //开始升级后收到00
                    index++;
                    if (index == resultArr.length) {
                        if (errorTime <= 5) {
                            // 打包20个0x55
                            writeMessage(buildCANMessage55());
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if (isWriteOK) {
                                        isSuccess = true;
                                        dataBinding.message.setText("升级成功");
                                        dataBinding.cm.stop();
                                        isForceUpdate = false;
                                        dataBinding.btStart.setEnabled(true);
                                        dataBinding.btStart.setText("Start Update");
                                        resetView();
                                    } else {
                                        handler.sendEmptyMessage(UPDATE_FAIL);
                                    }
                                }
                            }, 1000);
                        }
                    } else {
                        writeEch();
                    }
                } else {
                    String resultData = CustomUtil.bytetoString(value);
                    if (resultData.startsWith("OK")) { // 返回0K，代表成功
                        isWriteOK = true;
                        if (fileType == 0) {
                            //lsh跳过校验
                            //表示成功
                            isSuccess = true;
                            dataBinding.message.setText("升级成功");
                            dataBinding.cm.stop();
                            isForceUpdate = false;
                            dataBinding.btStart.setEnabled(true);
                            dataBinding.btStart.setText("Start Update");
                            resetView();
                        }
                    } else if (resultData.startsWith("MPC") && resultData.length() >= 11 && isWriteOK && fileType != 2) {
                        int repCheckNum = (value[10] & 0xff) | ((value[9] & 0xff) << 8);
                        LogUtil.e("返回校验值" + repCheckNum);
                        if (enhCheckNum == repCheckNum) {
                            //表示成功
                            isSuccess = true;
                            dataBinding.message.setText("升级成功");
                            dataBinding.cm.stop();
                            isForceUpdate = false;
                            dataBinding.btStart.setEnabled(true);
                            dataBinding.btStart.setText("Start Update");
                            resetView();
                        } else {
                            handler.sendEmptyMessage(UPDATE_FAIL);
                        }

                    } else if (resultData.contains("ER") || resultData.contains("ET")) { // 返回包含ER或ET，表示更新出错
                        if (isWriting) {
                            // 发送出现中断
                            isBreak = true;
                            errorTime++;
                            repeatTime = getERNum(resultData);
                            if (fileType == 2) {
                                writeEch();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onResponse(int code) {
            LogUtil.e("notify ok");
        }
    };

    /**
     * 发送命令通知进入bootload
     */
    private void startBoot() {
        singleTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeMessage(CustomUtil.HexString2Bytes(BOOT_CMD));
            }
        });

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

    private boolean checkEchFileNeedWrite(int index) {
        String cmdStr = resultArr[index];
        LogUtil.i(cmdStr);
        byte[] cmdData = ControllerDataUtils.HexString2Bytes(cmdStr);
        byte[] decryptArray = new byte[20];
        Arrays.fill(decryptArray, (byte) 0x00);
        if (cmdData.length < 20 && cmdStr.toUpperCase().contains("040801F1")) {
            return true;
        }

        for (int k = 0; k < 20; k++) {
            if (k != 2) {
                decryptArray[k] = (byte) (cmdData[k] ^ cmdData[2] ^ key_enh_hex[k] ^ key_enh_hex[2]);
            }
        }
        String verifyData = ControllerDataUtils.byte2hex(decryptArray);
        LogUtil.e(verifyData);
        boolean ffFlag = false;
        int line_address = ((decryptArray[0] & 0xff) << 8) + (decryptArray[1] & 0xff);
        for (int i = 3; i < 19; i++) {
            if (decryptArray[i] != (byte) 0xff) {
                ffFlag = true;
            }
        }
        if (line_address % 0x400 == 0 || ffFlag) {
            return true;
        }
        return false;
    }

    int index;//当前数据序列

    /**
     * ech升级发送数据
     */
    private void sendECHData() throws InterruptedException {
        dataBinding.message.setText("升级中...");
        dataBinding.pb.setVisibility(View.VISIBLE);
        isWriting = true;
        index = 0;
        writeEch();
    }

    private void writeEch() {
        if (errorTime == 5) {
            handler.sendEmptyMessage(UPDATE_FAIL);
        } else {
            float percent = MathUtil.delTwoNumPercent(index, resultArr.length);
            dataBinding.message.setText(percent + "%");
            dataBinding.pb.setProgress((int) percent);
            if (checkEchFileNeedWrite(index)) {
                writeEchData(buildCANMessageStr(index));
                if (resultArr[index].toUpperCase().contains("0000040801F1")) {
                    //不等回复，直接发下一条
                    try {
                        Thread.sleep(100);
                        index++;
                        sendEchNext();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                index++;
                sendEchNext();
            }
        }
    }

    private void sendEchNext() {
        if (index == resultArr.length) {
            if (errorTime <= 5) {
                // 打包20个0x55
                writeMessage(buildCANMessage55());
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isWriteOK) {
                            isSuccess = true;
                            dataBinding.message.setText("升级成功");
                            dataBinding.cm.stop();
                            isForceUpdate = false;
                            dataBinding.btStart.setEnabled(true);
                            dataBinding.btStart.setText("Start Update");
                            resetView();
                        } else {
                            handler.sendEmptyMessage(UPDATE_FAIL);
                        }
                    }
                }, 1000);
            }
        } else {
            writeEch();
        }
    }

    /**
     * lsh\enh升级发送数据
     *
     * @throws InterruptedException
     */
    private void sendLshEnhData() throws InterruptedException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataBinding.message.setText("升级中。。。");
                dataBinding.pb.setVisibility(View.VISIBLE);
            }
        });
        int time = Integer.parseInt(dataBinding.etTimeSplit.getText().toString());

        // 一行一行分开发送
        isWriting = true;
        // 循环一行一行取出
        for (int i = 0; i < resultArr.length; i++) {
            if (errorTime > 5) {
                handler.sendEmptyMessage(UPDATE_FAIL);
                break;
            } else if (isBreak) {
                Thread.sleep(50);
                repeatSendData(i, resultArr);
                writeMessage(CustomUtil.HexString2Bytes(resultArr[i]));
                if ((i + 1) % 64 == 0) {
                    Thread.sleep(time * 2L);
                } else {
                    Thread.sleep(time);
                }
                isBreak = false;
            } else {
                if (checkEchFileNeedWrite(i)) {
                    writeMessage(CustomUtil.HexString2Bytes(resultArr[i]));
                }
                final int h = (int) ((float) (i + 1) / (float) resultArr.length * 100);
                int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataBinding.message.setText(MathUtil.delTwoNumPercent(finalI, resultArr.length) + "%");
                        dataBinding.pb.setProgress(h);
                    }
                });

                // 当行数为1,65,129……等（行数-1）%64=0的行时，发完数据延时50ms再发下一行
                if ((i + 1) % 64 == 0) {
                    Thread.sleep(time * 2L);
                } else {
                    Thread.sleep(time);
                }
            }
        }

        if (errorTime <= 5) {
            // 发20个0x55
            byte[] byteArray3 = new byte[20];
            Arrays.fill(byteArray3, (byte) 0x55);
            writeMessage(byteArray3);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWriteOK) {
                    handler.sendEmptyMessage(UPDATE_FAIL);
                }
            }
        }, 5000);
    }

    /**
     * lsh、enh重发
     *
     * @param position
     * @param resultArr
     * @throws InterruptedException
     */
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
     * ech重发
     *
     * @param position
     */
    private void repeatSendEchData(int position) {
        int time = repeatTime * 3;
        for (int i = time; i > 0; i--) {
            if (position - i >= 0) {
                int finalI = i;
                singleTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        writeMessage(buildCANMessageStr(position - finalI));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
    }

    private void startUpdate() {
        isForceUpdate = true;
        resetData();
        startBoot();
    }

    private void resetData() {
        isBreak = false;
        isWriting = false;
        isWriteOK = false;
        errorTime = 0;
    }

    private void resetView() {
        dataBinding.btStart.setEnabled(true);
        dataBinding.btStart.setText("Start Update");
        dataBinding.cm.stop();
        isWriting = false;
    }

    private byte[] buildCANMessageStr(int i) {
        byte[] msg = new byte[26];
        msg[0] = 0x3a;
        if (resultArr[i].toUpperCase().contains("0000040801F1")) {
            //如 果 .ech 文 件 中 有 一 行 内 容 包 含 “0000040801F1”，则从这一行开始此位都为CB,在此 之前此位为CA
            msg[1] = (byte) 0xcb;
        } else {
            msg[1] = (byte) 0xca;
        }
        byte[] org = ConvertUtils.hexString2Bytes(resultArr[i]);
        System.arraycopy(org, 0, msg, 2, org.length);
        int checkSum = sum(msg);
        msg[22] = ControllerDataUtils.getLowBit(checkSum);
        msg[23] = ControllerDataUtils.getHighBit(checkSum);
        msg[24] = 0x0d;
        msg[25] = 0x0a;
        return msg;
    }

    private byte[] buildCANMessage55() {
        byte[] msg = new byte[26];
        msg[0] = 0x3a;
        msg[1] = (byte) 0xca;
        byte[] byteArray3 = new byte[20];
        Arrays.fill(byteArray3, (byte) 0x55);
        System.arraycopy(byteArray3, 0, msg, 2, byteArray3.length);
        int checkSum = sum(msg);
        msg[22] = ControllerDataUtils.getLowBit(checkSum);
        msg[23] = ControllerDataUtils.getHighBit(checkSum);
        msg[24] = 0x0d;
        msg[25] = 0x0a;
        return msg;
    }

    private BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    private int sum(byte[] msg) {
        String s = ControllerDataUtils.byte2hex(msg);
        int sum = 0;
        for (int i = 1; i < msg.length; i++) {
            sum += msg[i] & 0xff;
        }
        return sum;
    }

    /**
     * 写入数据
     *
     * @param message
     */
    private void writeMessage(byte[] message) {
        BleUtils.writeBle(client, message, writeResponse);
    }

    private void writeEchData(byte[] msg) {
        BleUtils.writeBle(client, msg, new BleWriteResponse() {
            @Override
            public void onResponse(int i) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        resetData();
        BleUtils.unNotifyBle(client, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int i) {

            }
        });
        super.onDestroy();
    }
}