package com.lsdzs.lsdzs_tool.ui.update;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.autonavi.base.amap.mapcore.FileUtil;
import com.blankj.utilcode.util.UriUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.databinding.ActivityMeterUpdateBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.utils.LogUtil;
import com.wxh.basiclib.utils.MathUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MeterUpdateActivity extends BaseNoModelActivity<ActivityMeterUpdateBinding> implements View.OnClickListener {
    private byte[] lastSendData;
    private byte[] lastFileData;
    private int BLE_MAX_DATA_LENGTH = 16;
    private Timer timer;
    private TimerTask timerTask;
    private static final int CMD_CONNECT = 111;//连接命令
    private static final int CMD_SYNC_PACKNO = 112;//文件头
    private static final int CMD_GET_FWVER = 113;//握手指令
    private static final int CMD_GET_DEVICEID = 114;
    private static final int CMD_READ_CONFIG = 115;
    private static final int CMD_UPDATA_STOP = 116;//停止升级
    private static final int CMD_UPDATE_APROM = 117;//文件升级
    private static final int CMD_REQUEST_REPEAT = 118;//校验失败，重发
    private boolean isStartFile;
    private int cmd_flag;
    private byte[] resDataArray;
    private int len;
    private int index;//文件中字节位置
    private int ID = 0;
    private int connectID = 0;
    private int connectTimes;
    private long lastSendTime = System.currentTimeMillis();
    private boolean hasStart;
    private boolean isSuccess;
    private BluetoothClient bluetoothClient;
    private ActivityResultLauncher<Intent> launcher;

    private Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (hasStart && System.currentTimeMillis() - lastSendTime > 10000) {
                sendStopMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataBinding.btStart.setEnabled(true);
                    }
                });
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.activity_meter_update;
    }

    @Override
    protected void initView(Bundle bundle) {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    dataBinding.tvPath.setText(uri.getPath());
                    resDataArray = UriUtils.uri2Bytes(uri);
                    len = resDataArray.length;
                    dataBinding.btStart.setVisibility(View.VISIBLE);
                    dataBinding.btStart.setEnabled(true);
                }
            }
        });
        dataBinding.btSelect.setOnClickListener(this);
        dataBinding.btStart.setOnClickListener(this);
        dataBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void initData() {
        bluetoothClient = ClientManager.getClient(this);
        bluetoothClient.registerConnectStatusListener(ClientManager.getDevice().getMac(),
                mConnectStatusListener);
        bluetoothClient.notify(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getNotifyCharacter().getUuid(), mNotifyRsp);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_select:
                showFileSelectDialog();
                break;
            case R.id.bt_start:
                if (hasStart) {
                    isSuccess = false;
                    sendStopMessage();
                    dataBinding.btStart.setEnabled(true);
                } else {
                    if (dataBinding.tvPath.getText().toString().isEmpty()) {
                        Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isSuccess = false;
                    dataBinding.btStart.setEnabled(false);
                    dataBinding.message.setText("");
                    startUpdate();
                    lastSendTime = System.currentTimeMillis();
                    handler.post(runnable);
                }
                break;
        }
    }


    private void startUpdate() {
        hasStart = true;
        startTalk();
    }

    /**
     * 开始连接，先连发三条连接命令，等待回复
     */
    private void startTalk() {
        dataBinding.message.setText("连接中");
        connectTimes = 0;
        connectID = 0;
        cmd_flag = CMD_CONNECT;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                switch (cmd_flag) {
                    case CMD_CONNECT:
                        if (connectTimes < 3) {
                            if (connectTimes == 1) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (cmd_flag != CMD_CONNECT) {
                                cancel();
                                timer.purge();
                                timer.cancel();
                                return;
                            }
                            byte[] sa = {(byte) 0xAE, 0x00, 0x00, 0x00};
                            lastSendData = sendOutData(sa, connectID, null);
                            writeMessage(lastSendData);
                            connectID += 2;
                            if (connectID > 65535) {
                                connectID = 0;
                            }
                            connectTimes++;
                        }
                        break;
                }
            }
        };
        timer.schedule(timerTask, 0, 250);
    }

    private void showFileSelectDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        launcher.launch(intent);
    }

    /**
     * app发送的64位数据校验和
     * <p>
     * chH:(s >> 8) & 0xff
     * chL:sum&0xff
     *
     * @param data
     */
    private int checkVerify(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i] & 0xff;
        }
        return sum;
    }

    private byte getdataH(int data) {
        return (byte) (data >> 8);
    }

    private byte getdataL(int data) {
        return (byte) (data & 0xff);
    }

    private byte[] sendOutData(byte[] sa, int id, byte[] data) {
        byte[] sendData = new byte[64];
        sendData[0] = sa[0];
        sendData[1] = sa[1];
        sendData[2] = sa[2];
        sendData[3] = sa[3];
        sendData[4] = getdataL(id);
        sendData[5] = getdataH(id);
        sendData[6] = 0;
        sendData[7] = 0;
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                sendData[8 + i] = data[i];
            }
        }
        return sendData;
    }

    /**
     * 校验返回值
     *
     * @param UD2
     * @return
     */
    private boolean processUD2(byte[] UD2) {
        int ch;
        if (cmd_flag == CMD_CONNECT) {
            int dealID = ((UD2[5] & 0xff) << 8) | (UD2[4] & 0xff);
            ch = 0xae + dealID - 1;
        } else {
            ch = checkVerify(lastSendData);
        }
        byte chH = (byte) (ch >> 8);
        byte chL = (byte) (ch & 0xff);
        //        byte IDL = (byte) ((ID + 1) & 0xff);
        //        byte IDH = (byte) ((ID + 1) >> 8);
        //        if (chL == UD2[0] && chH == UD2[1] && UD2[4] == IDL && UD2[5] == IDH) {
        return chL == UD2[0] && chH == UD2[1];
    }

    private BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(ClientManager.getservice().getUUID()) && character.equals(ClientManager.getNotifyCharacter().getUuid())) {
                LogUtil.e("返回数据：" + CustomUtil.byte2hex(value));
                if (CustomUtil.bytetoString(value).startsWith("AT")) {
                    return;
                }
                if (isSuccess) {
                    return;
                }
                if (processUD2(value)) {
                    switch (cmd_flag) {
                        case CMD_CONNECT:
                            cmd_flag = CMD_SYNC_PACKNO;
                            timerTask.cancel();
                            timer.purge();
                            timer.cancel();
                            //进入握手指令
                            dataBinding.message.setText("进入握手命令");
                            ID = 1;
                            byte[] sa1 = {(byte) 0xA4, 0x00, 0x00, 0x00};
                            lastSendData = sendOutData(sa1, ID, new byte[]{0x01});
                            writeMessage(lastSendData);
                            break;
                        case CMD_SYNC_PACKNO:
                            if (!isStartFile) {
                                //握手指令，软件版本
                                cmd_flag = CMD_GET_FWVER;
                                ID += 2;
                                byte[] sa2 = {(byte) 0xA6, 0x00, 0x00, 0x00};
                                lastSendData = sendOutData(sa2, ID, null);
                                writeMessage(lastSendData);
                            } else {
                                //开始发送文件
                                dataBinding.message.setText("发送文件");
                                dataBinding.pb.setVisibility(View.VISIBLE);
                                cmd_flag = CMD_UPDATE_APROM;
                                index = 0;
                                //刚开始第一条
                                byte[] sa6 = {(byte) 0xA0, 0x00, 0x00, 0x00};
                                byte[] data1 = new byte[56];
                                data1[0] = 0;
                                data1[1] = 0;
                                data1[2] = 0;
                                data1[3] = 0;
                                data1[4] = (byte) (len & 0xff);
                                data1[5] = (byte) (len >> 8);
                                data1[6] = 0;
                                data1[7] = 0;
                                for (int m = 0; m < 48; m++) {
                                    data1[8 + m] = resDataArray[m];
                                }
                                index = 48;
                                lastSendData = sendOutData(sa6, ID, data1);
                                lastFileData = lastSendData;
                                writeMessage(lastSendData);
                            }

                            break;
                        case CMD_UPDATE_APROM:
                            if (index >= len) {
                                // 升级完成
                                isSuccess = true;
                                dataBinding.message.setText("成功！！！！！");
                                Toast.makeText(MeterUpdateActivity.this, "升级成功！！！", Toast.LENGTH_SHORT).show();
                            } else {
                                //第N条,N>1
                                ID += 2;
                                byte[] sa7 = {(byte) 0x00, 0x00, 0x00, 0x00};
                                byte[] data1 = new byte[56];
                                int writeNum;
                                if (index + 56 > len) {
                                    writeNum = len - index;
                                } else {
                                    writeNum = 56;
                                }
                                for (int n = 0; n < writeNum; n++) {
                                    data1[n] = resDataArray[n + index];
                                }
                                index += writeNum;
                                LogUtil.e("index=" + (index));
                                dataBinding.message.setText(MathUtil.delTwoNumPercent(index,len)+"%");
                                dataBinding.pb.setProgress((int) MathUtil.delTwoNumPercent(index,len));
                                lastSendData = sendOutData(sa7, ID, data1);
                                lastFileData = lastSendData;
                                writeMessage(lastSendData);
                            }
                            break;
                        case CMD_GET_FWVER:
                            //仪表ID
                            cmd_flag = CMD_GET_DEVICEID;
                            ID += 2;
                            byte[] sa3 = {(byte) 0xB1, 0x00, 0x00, 0x00};
                            lastSendData = sendOutData(sa3, ID, null);
                            writeMessage(lastSendData);

                            break;
                        case CMD_GET_DEVICEID:
                            //仪表配置数据
                            cmd_flag = CMD_READ_CONFIG;
                            ID += 2;
                            byte[] sa4 = {(byte) 0xA2, 0x00, 0x00, 0x00};
                            lastSendData = sendOutData(sa4, ID, null);
                            writeMessage(lastSendData);
                            break;
                        case CMD_READ_CONFIG:
                            //握手成功，开始发送升级文件
                            ID = 1;
                            isStartFile = true;
                            //数据包头
                            dataBinding.message.setText("握手成功，发送文件头");
                            cmd_flag = CMD_SYNC_PACKNO;
                            byte[] sa = {(byte) 0xA4, 0x00, 0x00, 0x00};
                            lastSendData = sendOutData(sa, ID, new byte[]{0x01});
                            writeMessage(lastSendData);
                            break;
                        case CMD_REQUEST_REPEAT:
                            cmd_flag = CMD_UPDATE_APROM;
                            lastSendData = lastFileData;
                            writeMessage(lastSendData);

                            break;
                    }
                } else if (cmd_flag == CMD_UPDATE_APROM) {
                    if (index == 48) {
                        dataBinding.message.setText("升级失败，10S后重试");
                        //停止升级
                        sendStopMessage();
                        dataBinding.btStart.setEnabled(true);
                    } else {
                        LogUtil.e("写入失败,重发" + index);
                        dataBinding.message.setText("写入失败,重发" + index + "\n");
                        sendRepeatMessage();
                        cmd_flag = CMD_REQUEST_REPEAT;
                    }
                } else {
                    dataBinding.message.setText("升级失败，10S后重试");
                    //停止升级
                    sendStopMessage();
                    dataBinding.btStart.setEnabled(true);
                }
            }
        }

        @Override
        public void onResponse(int code) {

        }
    };

    private void sendRepeatMessage() {
        cmd_flag = CMD_UPDATA_STOP;
        byte[] sa5 = {(byte) 0xFF, 0x01, 0x02, 0x03};
        lastSendData = sendOutData(sa5, 0, null);
        writeMessage(lastSendData);
    }

    private void sendStopMessage() {
        hasStart = false;
        int id;
        if (cmd_flag == CMD_CONNECT) {
            id = connectID;
        } else {
            id = ID;
        }
        cmd_flag = CMD_UPDATA_STOP;
        byte[] sa5 = {(byte) 0xFF, 0x00, 0x00, 0x00};
        lastSendData = sendOutData(sa5, id, null);
        writeMessage(lastSendData);
        handler.removeCallbacks(runnable);
    }

    /**
     * 写入数据
     *
     * @param data
     */
    private void writeMessage(final byte[] data) {
        lastSendTime = System.currentTimeMillis();
        LogUtil.e(CustomUtil.byte2hex(data));
        if (ClientManager.getDevice() != null) {
            if (data.length > BLE_MAX_DATA_LENGTH) {
//                    ClientManager.getClient().requestMtu(ClientManager.getmDevice().getAddress(), 67, new BleMtuResponse() {
//                        @Override
//                        public void onResponse(int i, Integer integer) {
//                            Toast.makeText(getApplicationContext(),i+"--"+integer.intValue(),Toast.LENGTH_LONG).show();
//                            BluetoothUtil.writeSingle(data);
//                        }
//                    });

                for (int i = 0; i < data.length; i += BLE_MAX_DATA_LENGTH) {
                    if (i + BLE_MAX_DATA_LENGTH < data.length) {
                        writeSingle(subBytes(data, i, BLE_MAX_DATA_LENGTH));
                        try {
                            if (cmd_flag == CMD_UPDATE_APROM) {
                                Thread.sleep(1);
                            } else {
                                Thread.sleep(30);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        writeSingle(subBytes(data, i, data.length - i));
                    }
                }
            } else {
                writeSingle(data);
            }
        }
    }

    public void writeSingle(final byte[] data) {
        BleUtils.writeBle(bluetoothClient, data, new BleWriteResponse() {
            @Override
            public void onResponse(int i) {
                if (i == REQUEST_SUCCESS) {
                    LogUtil.e(CustomUtil.byte2hex(data) + "写入成功");

                }
            }
        });
    }

    /**
     * 大数据分包
     *
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    private BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            switch (status) {
                case STATUS_CONNECTED:
                    LogUtil.d("STATUS_CONNECTED");

                    break;
                case STATUS_DISCONNECTED:
                    LogUtil.d("STATUS_DISCONNECTED");
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        BleUtils.unNotifyBle(bluetoothClient, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int i) {

            }
        });
        BleUtils.unRegisterConnectStatus(bluetoothClient, mConnectStatusListener);
        BleUtils.disConnect(bluetoothClient);
        super.onDestroy();
    }
}