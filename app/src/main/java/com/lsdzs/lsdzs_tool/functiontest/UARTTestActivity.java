package com.lsdzs.lsdzs_tool.functiontest;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.VibrateUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.MymqttService;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleDataConvertUtil;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.ControllerDataUtils;
import com.lsdzs.lsdzs_tool.databinding.ActivityFunctionTestUartBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.ble.IOTCmd;
import com.wxh.basiclib.ble.UartSettingData;
import com.wxh.basiclib.location.GpsCoordinateUtils;
import com.wxh.basiclib.utils.LogUtil;
import com.wxh.basiclib.utils.StringUtils;

import org.json.JSONObject;

import java.util.UUID;

public class UARTTestActivity extends BaseNoModelActivity<ActivityFunctionTestUartBinding> {
    private String clientId;
    private int TIMEOUT_VALUE = 5000;

    private static final int PROCESS_TIME = 300;

    private static final int KEY_TIMEOUT = 11;
    private static final int CONTROLLER_TIMEOUT = 12;

    private static final int UNLOCK_TIMEOUT = 13;

    private static final int LOCK_TIMEOUT = 14;
    private BluetoothClient bluetoothClient;
    private int pas, walkmode, light;
    private byte messageKey;
    private boolean isPowerOn;
    private boolean bleConnect = true;

    private int lightSet = 0;
    private int walkSet = 0;
    private int pasSet = 0;
    //    private AMap mAmap;
//    private GPSLocOverlay gpsLocOverlay;
    private String ebikeId;
    private String cycleId;
    private boolean hasBleStarted = false;
    private int process;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case KEY_TIMEOUT:
                    ToastUtils.showShort("获取key失败");
                    break;
                case UNLOCK_TIMEOUT:
                    ToastUtils.showShort("开锁失败");
                    break;
                case LOCK_TIMEOUT:
                    ToastUtils.showShort("关锁失败");
                    break;
                case CONTROLLER_TIMEOUT:
                    ToastUtils.showShort("通讯故障");
                    break;
            }
            return false;
        }
    });

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.lsdzs.freedaretest.mqttservice")) {
                String content = intent.getStringExtra("content");
                try {
                    JSONObject object = new JSONObject(content);
                    if (clientId.equals(object.getString("clientId"))) {
                        String[] msg = object.getString("msg").split(",");
                        if (msg[2].equals("L1") && msg[3].startsWith("0")) {
                            //4G操作
                            if (process == 3) {
                                //开锁成功
                                dismissDialog();
                                handler.removeMessages(UNLOCK_TIMEOUT);
                                isPowerOn = true;
                                dataBinding.llUnlock.setVisibility(View.GONE);
                                dataBinding.rideLayout.setVisibility(View.VISIBLE);
                                //开锁成功后获取控制器数据
                                getControllerData();

                            }
                        } else if (msg[2].equals("L2") && msg[3].startsWith("0")) {
                            //todo 关锁成功
                            if (process == 4) {
                                dismissDialog();
                                handler.removeMessages(LOCK_TIMEOUT);
                                isPowerOn = false;
                                dataBinding.llUnlock.setVisibility(View.VISIBLE);
                                dataBinding.rideLayout.setVisibility(View.GONE);

                            }
                        } else if (msg[2].equals("D0") && msg.length > 9) {
                            LogUtil.e(ArrayUtils.toString(msg));
                            //获取到位置数据
                            String lat = msg[9];
                            String lng = msg[11];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!StringUtils.isEmpty(lat)) {
                                        double[] c = GpsCoordinateUtils.calWGS84toGCJ02(Double.parseDouble(lat), Double.parseDouble(lng));
                                        dataBinding.tvGps.setText(String.format("%s,%s", c[0], c[1]));
                                        //gpsLocOverlay.locationChanged(new LatLng(c[0], c[1]));
                                    }
                                }
                            });
                        } else if (msg[2].equals("H0") && msg.length > 9) {
                            String lat = msg[10];
                            String lng = msg[12];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!StringUtils.isEmpty(lat)) {
                                        double[] c = GpsCoordinateUtils.calWGS84toGCJ02(Double.parseDouble(lat), Double.parseDouble(lng));
                                        dataBinding.tvGps.setText(String.format("%s,%s", c[0], c[1]));
                                        //gpsLocOverlay.locationChanged(new LatLng(c[0], c[1]));
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    };

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        dataBinding.amap.onSaveInstanceState(outState);
//    }

    @Override
    protected int getLayout() {
        return R.layout.activity_function_test_uart;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.bt4gUnlock.setOnClickListener(view -> {
            showDialog("");
            //todo 4G开锁
            process = 3;
            send4gOpenMessage();
        });

        dataBinding.btBleUnlock.setOnClickListener(view -> {
            showDialog("");
            //todo 蓝牙开锁
            process = 1;
            sendUnLockMessage();
        });

        dataBinding.bt4gLock.setOnClickListener(view -> {
            showDialog("");
            //todo 4g关锁
            process = 4;
            send4gCloseMessage();
        });

        dataBinding.btBleLock.setOnClickListener(view -> {
            showDialog("");
            //todo 蓝牙关锁
            process = 2;
            sendLockMessage(messageKey);
        });

//        dataBinding.ivWalk.setOnTouchListener(new WalkTouchEvent());
//
//        dataBinding.ivLight.setOnClickListener(view -> {
//            if (light == 0) {
//                lightSet = 1;
//            } else {
//                lightSet = 0;
//            }
//            changeControllerValue(pas, lightSet, walkmode);
//        });
//
//        dataBinding.ivPasAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (pas < 5) {
//                    pasSet = pas + 1;
//                    changeControllerValue(pasSet, lightSet, walkmode);
//                }
//            }
//        });
//        dataBinding.ivPasSub.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (pas > 1) {
//                    pasSet = pas - 1;
//                    changeControllerValue(pasSet, lightSet, walkmode);
//                }
//            }
//        });
//
//        MapsInitializer.updatePrivacyShow(context, true, true);
//        MapsInitializer.updatePrivacyAgree(context, true);
//
//        dataBinding.amap.onCreate(bundle);
//        mAmap = dataBinding.amap.getMap();
//        gpsLocOverlay = new GPSLocOverlay(mAmap);
    }

    private class WalkTouchEvent implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtil.i("ACTION_DOWN");
                    if (walkSet == 0) {
                        isRelease = false;
                        handler.postDelayed(longRunnable, 2000);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(longRunnable);
                    if (walkSet == 1) {
                        VibrateUtils.vibrate(1000);
                        isRelease = true;
                        walkSet = 0;
                        changeControllerValue(pas, light, walkSet);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private boolean isRelease;
    private Runnable longRunnable = new Runnable() {

        @Override
        public void run() {
            if (!isRelease) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        VibrateUtils.vibrate(1000);
                        walkSet = 1;
                        changeControllerValue(pas, light, walkSet);
                    }
                });
            }
        }
    };

    @Override
    protected void initData() {
        clientId = ClientManager.getDevice().getName();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lsdzs.freedaretest.mqttservice");
        registerReceiver(receiver, filter);

        bluetoothClient = ClientManager.getClient(getApplicationContext());
        BleUtils.registerConnectStatus(bluetoothClient, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int status) {
                switch (status) {
                    case STATUS_CONNECTED:
                        ToastUtils.showShort("蓝牙连接成功");
                        break;
                    case STATUS_DISCONNECTED:
                        ToastUtils.showShort("蓝牙断开连接");
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });
        BleUtils.notifyBle(bluetoothClient, notifyResponse);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPowerOn) {
                    sendKeyMessage();//获取key,显示开锁按钮
                    dataBinding.llUnlock.setVisibility(View.VISIBLE);
                    dataBinding.rideLayout.setVisibility(View.GONE);
                } else {
                    dataBinding.llUnlock.setVisibility(View.GONE);
                    dataBinding.rideLayout.setVisibility(View.VISIBLE);
                }
            }
        }, 500);
    }

    BleNotifyResponse notifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID uuid, UUID uuid1, byte[] value) {
            if (value.length < 3) {
                return;
            }
            switch (value[0]) {
                case (byte) 0xa1:
                    if (value[3] == 0x05) {
                        if (process == 1) {
                            dealUnLockRsponse(value);
                        }
                    } else if (value[3] == 0x01) {//通讯秘钥
                        //保存秘钥
                        dealKeyResponse(value);
                    } else if (value[3] == 0x10) {
                        //收到关锁指令
                        if (process == 2) {
                            dealLockResponse(value);
                        }
                    }

                    break;
                case 0x3a:
                    isPowerOn = true;
                    if (value[1] == 0x28) {
                        dealControllerData28(value);
                    } else if (value[1] == 0x29) {
                        dealControllerData29(value);
                    }

                    break;
            }
            Log.d("接收到", BleDataConvertUtil.byte2hex(value));
        }

        @Override
        public void onResponse(int i) {

        }
    };

    /**
     * 解锁指令返回
     *
     * @param data
     */
    private void dealUnLockRsponse(byte[] data) {
        dismissDialog();
        handler.removeMessages(UNLOCK_TIMEOUT);
        //TODO
        int result = data[4];//1：成功 2：失败或超时
        if (result == 1) {
            isPowerOn = true;
            dataBinding.llUnlock.setVisibility(View.GONE);
            dataBinding.rideLayout.setVisibility(View.VISIBLE);
            //开锁成功后获取控制器数据
            getControllerData();
        }
    }

    /**
     * 获取控制器数据
     */
    private void getControllerData() {
        handler.sendEmptyMessageDelayed(CONTROLLER_TIMEOUT, TIMEOUT_VALUE);
        //发送档位，大灯初始值
//        BleUtils.writeBle(bluetoothClient, UartSettingData.getInstance().sendSettingData(), writeResponse);
        //发送获取控制器数据
        BleUtils.writeBle(bluetoothClient, IOTCmd.sendControllerMessage(messageKey), writeResponse);
    }

    /**
     * 获取控制器数据
     */
    private void changeControllerValue(int pas, int light, int walkmode) {
        handler.sendEmptyMessageDelayed(CONTROLLER_TIMEOUT, TIMEOUT_VALUE);
        UartSettingData.getInstance().setLightStatus(light);
        UartSettingData.getInstance().setPas(pas);
        UartSettingData.getInstance().setWalkMode(walkmode);
        //发送档位，大灯初始值
        writeMessageNew(UartSettingData.getInstance().sendSettingData());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //发送获取控制器数据
//                writeMessageNew(IOTCmd.sendControllerMessage(messageKey));
//            }
//        },200);
    }

    /**
     * 关锁指令返回
     *
     * @param data
     */
    private void dealLockResponse(byte[] data) {
        dismissDialog();
        handler.removeMessages(LOCK_TIMEOUT);
        int result = data[4];//1：成功 0：失败
        if (result == 1) {
            isPowerOn = false;
            dataBinding.llUnlock.setVisibility(View.VISIBLE);
            dataBinding.rideLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 处理获取到的通信key
     *
     * @param data
     */
    private void dealKeyResponse(byte[] data) {
        handler.removeMessages(KEY_TIMEOUT);
        messageKey = data[2];
    }

    private void dealControllerData28(byte[] data) {
        handler.removeMessages(CONTROLLER_TIMEOUT);
        int[] dataA = ControllerDataUtils.dealReceiveDataA(data);
        light = dataA[2];
        pas = dataA[0];
        walkmode = dataA[1];
        int speed = dataA[5];
        int errorCode = dataA[8];
        int cadence = dataA[6];
        int brake = dataA[3];
        int motor = dataA[4];
        int liju = dataA[7];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (light == 0) {
                    dataBinding.tvLightStatus.setText("off");
                } else {
                    dataBinding.tvLightStatus.setText("on");
                }
                if (walkmode == 0) {
                    dataBinding.tvWalkStatus.setText("off");
                } else {
                    dataBinding.tvWalkStatus.setText("on");
                }
                if (brake == 0) {
                    dataBinding.tvBrakeStatus.setText("off");
                } else {
                    dataBinding.tvBrakeStatus.setText("on");
                }
                if (motor == 0) {
                    dataBinding.tvMotorStatus.setText("off");
                } else {
                    dataBinding.tvMotorStatus.setText("on");
                }
                dataBinding.tvSpeed.setText(speed / 10f + "");
                dataBinding.tvPas.setText(pas + "");
                dataBinding.tvTapin.setText(cadence + "");
                dataBinding.tvPedal.setText(liju + "");
                dealErrorCode(errorCode);
            }
        });
    }

    private void dealControllerData29(byte[] data) {
        int[] dataB = ControllerDataUtils.dealReceiveDataB(data);
        int mileage = dataB[0];
        float capacity = data[6] / 10f;
        int battery = dataB[7];
        int controllerTemp = dataB[1] - 50;
        int motorTemp = dataB[2] - 50;
        int batteryTemp = dataB[3] - 50;
        int current = dataB[5];
        int voltage = dataB[4];
        int power = (int) ((voltage / 10f) * (current / 3f));
        int powerpercent = dataB[8];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataBinding.tvOdo.setText(mileage / 10f + "km");
                dataBinding.tvBatteryCurrent.setText(String.format("%.1f", current / 3f));
                dataBinding.tvSocDetail.setText(capacity + "(" + battery + "%)");
                dataBinding.tvControllerTemp.setText(controllerTemp + "");
                dataBinding.tvMotorTemp.setText(motorTemp + "");
                dataBinding.tvBattertTemp.setText(batteryTemp + "");
                dataBinding.tvBatteryVoltage.setText(voltage / 10f + "");
                dataBinding.tvPower.setText(power + "(" + powerpercent + "%)");
            }
        });
    }

    private void dealErrorCode(int errorcode){
        StringBuilder errorBuiler = new StringBuilder();
        String binCode = String.format("%016d", Long.valueOf(Integer.toBinaryString(errorcode)));
        //0001 过压保护
        if (binCode.charAt(15) == '1') {
            errorBuiler.append(getString(R.string.guoyabaohu)).append(" ");
        }
        //0002 欠压保护
        if (binCode.charAt(14) == '1') {
            errorBuiler.append(getString(R.string.qianyabaohu)).append(" ");
        }
        //0004 控制器功率管损坏或过流故障
        if (binCode.charAt(13) == '1') {
            errorBuiler.append(getString(R.string.kongzhiqigonglvguansunhuai)).append(" ");
        }
        //0008 堵转保护
        if (binCode.charAt(12) == '1') {
            errorBuiler.append(getString(R.string.duzhuanbaohu)).append(" ");
        }
        //0010 电机霍尔故障
        if (binCode.charAt(11) == '1') {
            errorBuiler.append(getString(R.string.dianjihuoerguzhang)).append(" ");
        }
        //0020 电机相线故障
        if (binCode.charAt(10) == '1') {
            errorBuiler.append(getString(R.string.dianjixiangxianguzhang)).append(" ");
        }
        //0040 转把电压过高
        if (binCode.charAt(9) == '1') {
            errorBuiler.append(getString(R.string.zhuanbadianyaguogao)).append(" ");
        }
        //0080 控制器/电机过温保护
        if (binCode.charAt(8) == '1') {
            errorBuiler.append(getString(R.string.guowenbaohu)).append(" ");
        }
        //0100 控制器电压故障
        if (binCode.charAt(7) == '1') {
            errorBuiler.append(getString(R.string.kongzhiqidianyaguzhang)).append(" ");
        }
        //0200 异常助力故障
        if (binCode.charAt(6) == '1') {
            errorBuiler.append(getString(R.string.yichangzhuliguzhang)).append(" ");
        }
        //0400 MCU自检故障
        if (binCode.charAt(5) == '1') {
            errorBuiler.append(getString(R.string.mcuzijianguzhang)).append(" ");
        }
        //0800 飞车保护
        if (binCode.charAt(4) == '1') {
            errorBuiler.append(getString(R.string.feichebaohu)).append(" ");
        }
        //1000 踏板传感器故障
        if (binCode.charAt(3) == '1') {
            errorBuiler.append(getString(R.string.tabanzhuanganqiguzhang)).append(" ");
        }
        //2000 速度传感器故障
        if (binCode.charAt(2) == '1') {
            errorBuiler.append(getString(R.string.suduchuanganqiguzhang)).append(" ");
        }
        //4000 刹把故障
        if (binCode.charAt(1) == '1') {
            errorBuiler.append(getString(R.string.shabaguzhang)).append(" ");
        }
        dataBinding.tvErrorCode.setText(errorBuiler.toString());
    }

    /**
     * 开锁指令
     */
    private void sendUnLockMessage() {
        handler.sendEmptyMessageDelayed(UNLOCK_TIMEOUT, TIMEOUT_VALUE);
        writeMessageNew(IOTCmd.sendUnLockMessage(messageKey));
    }

    /**
     * 发送关锁指令
     *
     * @param messageKey
     */
    private void sendLockMessage(byte messageKey) {
        handler.sendEmptyMessageDelayed(LOCK_TIMEOUT, TIMEOUT_VALUE);
        writeMessageNew(IOTCmd.sendLockMessage(messageKey));
    }

    private void sendKeyMessage() {
        handler.sendEmptyMessageDelayed(KEY_TIMEOUT, TIMEOUT_VALUE);
        writeMessageNew(IOTCmd.sendKeyMessage(ClientManager.getDevice().getMac()));
    }

    private void writeMessageNew(byte[] data) {
        if (ClientManager.getDevice() != null) {
            BleUtils.writeBle(bluetoothClient, data, writeResponse);
        }
    }

    BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {
//            LogUtils.e(i);
        }
    };

    private void send4gOpenMessage() {
        handler.sendEmptyMessageDelayed(UNLOCK_TIMEOUT, TIMEOUT_VALUE);
        String openMsg = "D,LS,L1#D";
        try {
            JSONObject object = new JSONObject();
            object.put("msg", openMsg);
            MymqttService.publish(object.toString(), clientId);
        } catch (Exception e) {

        }
    }

    private void send4gCloseMessage() {
        handler.sendEmptyMessageDelayed(LOCK_TIMEOUT, TIMEOUT_VALUE);
        String closeMsg = "D,LS,L2#D";
        try {
            JSONObject object = new JSONObject();
            object.put("msg", closeMsg);
            MymqttService.publish(object.toString(), clientId);
        } catch (Exception e) {

        }
    }

    private void send4gGPSMessage() {
        String closeMsg = "D,LS,D0,1#D";
        try {
            JSONObject object = new JSONObject();
            object.put("msg", closeMsg);
            MymqttService.publish(object.toString(), clientId);
        } catch (Exception e) {

        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        dataBinding.amap.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        dataBinding.amap.onPause();
//        super.onPause();
//    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        BleUtils.unNotifyBle(bluetoothClient, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int i) {

            }
        });
        if (ClientManager.getDevice() != null) {
            BleUtils.disConnect(bluetoothClient);
        }
        handler.removeCallbacksAndMessages(null);
//        dataBinding.amap.onPause();
        super.onDestroy();
    }
}