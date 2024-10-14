package com.lsdzs.lsdzs_tool.functiontest;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.VibrateUtils;
import com.google.gson.Gson;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleMtuResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.Constant;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.UserUtil;
import com.lsdzs.lsdzs_tool.api.entity.CreatePayOrderBean;
import com.lsdzs.lsdzs_tool.api.entity.CycleOrder;
import com.lsdzs.lsdzs_tool.api.entity.EmptyBean;
import com.lsdzs.lsdzs_tool.api.entity.NotifyUnLockResponse;
import com.lsdzs.lsdzs_tool.ble.BleDataConvertUtil;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.databinding.ActivityFunctionTestBinding;
import com.lsdzs.lsdzs_tool.socket.BikeData;
import com.lsdzs.lsdzs_tool.socket.GisData;
import com.lsdzs.lsdzs_tool.socket.GyroData;
import com.lsdzs.lsdzs_tool.socket.JWebSocketClient;
import com.lsdzs.lsdzs_tool.socket.JWebSocketClientService;
import com.lsdzs.lsdzs_tool.socket.SendMsgData;
import com.lsdzs.lsdzs_tool.socket.SendMsgDataUtil;
import com.wxh.basiclib.base.BaseActivity;
import com.wxh.basiclib.ble.LDBLCmd;
import com.wxh.basiclib.ble.LDBLResponse;
import com.wxh.basiclib.location.GpsCoordinateUtils;
import com.wxh.basiclib.utils.LogUtil;
import com.wxh.basiclib.utils.StringUtils;

import org.java_websocket.enums.ReadyState;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class FunctionTestActivity extends BaseActivity<RideViewModel, ActivityFunctionTestBinding> {
    private ChatMessageReceiver chatMessageReceiver;
    //websocket相关
    private JWebSocketClient client;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private JWebSocketClientService jWebSClientService;

    private BluetoothClient bluetoothClient;

    private boolean setPowerOn;//开机操作
    private boolean setPowerOff;//关机操作
    private boolean setLight;//设置大灯
    private int light;//当前大灯状态
    private boolean setPasAdd;//档位加
    private boolean setPasSub;//档位减
    private boolean setWalkOn;//推行开
    private int pas;
    private boolean isPowerOn;
    private boolean bleConnect = true;
    private AMap mAmap;
    private GPSLocOverlay gpsLocOverlay;
    private String ebikeId;
    private String cycleId;
    private boolean hasBleStarted = false;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
            }
            return false;
        }
    });

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dataBinding.amap.onSaveInstanceState(outState);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_function_test;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.bt4gUnlock.setOnClickListener(view -> {
            showDialog("");
            //todo 4G开锁
            viewModel.iot4GUnlock(ebikeId);
        });

        dataBinding.btBleUnlock.setOnClickListener(view -> {
            showDialog("");
            //todo 蓝牙开锁
            viewModel.btUnlockApply(ebikeId);
            setPowerOn = true;
            if (!hasBleStarted) {
                getMeterData();
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPowerOn) {
                        viewModel.notifyUnlock(ebikeId);
                    }
                }
            }, 2000);
        });

        dataBinding.bt4gLock.setOnClickListener(view -> {
            showDialog("");
            //todo 4g关锁
            viewModel.ioT4GLock(ebikeId);
        });

        dataBinding.btBleLock.setOnClickListener(view -> {
            showDialog("");
            //todo 蓝牙关锁
            setPowerOff = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPowerOn) {
                        viewModel.notifyLock(ebikeId);
                    }
                }
            }, 2000);
        });

//        dataBinding.ivWalk.setOnTouchListener(new WalkTouchEvent());
//
//        dataBinding.ivLight.setOnClickListener(view -> {
//            setLight = true;
//        });

        dataBinding.ivPasAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pas < 9) {
                    setPasAdd = true;
                }
            }
        });
        dataBinding.ivPasSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pas > 0) {
                    setPasSub = true;
                }
            }
        });

        MapsInitializer.updatePrivacyShow(context, true, true);
        MapsInitializer.updatePrivacyAgree(context, true);

        dataBinding.amap.onCreate(bundle);
        mAmap = dataBinding.amap.getMap();
        gpsLocOverlay = new GPSLocOverlay(mAmap);

    }

    private class WalkTouchEvent implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtil.i("ACTION_DOWN");
                    if (!setWalkOn) {
                        isRelease = false;
                        handler.postDelayed(longRunnable, 2000);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(longRunnable);
                    if (setWalkOn) {
                        VibrateUtils.vibrate(1000);
                        isRelease = true;
                        setWalkOn = false;
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
                        setWalkOn = true;
                    }
                });
            }
        }
    };

    BleWriteResponse writeResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    private void initBleData() {
        bluetoothClient = ClientManager.getClient(getApplicationContext());
        BleUtils.registerConnectStatus(bluetoothClient, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int status) {
                switch (status) {
                    case STATUS_CONNECTED:
                        ToastUtils.showShort("蓝牙连接成功");
                        bleConnect = true;
                        break;
                    case STATUS_DISCONNECTED:
                        ToastUtils.showShort("蓝牙断开连接");
                        bleConnect = false;
                        if (ClientManager.getDevice() != null) {
                            BleUtils.connect(bluetoothClient, ClientManager.getDevice(), this, new BleUtils.ConnectResult() {
                                @Override
                                public void success() {
                                    bluetoothClient.requestMtu(ClientManager.getDevice().getMac(), 200, new BleMtuResponse() {
                                        @Override
                                        public void onResponse(int i, Integer integer) {

                                        }
                                    });
                                }

                                @Override
                                public void fail() {

                                }
                            });
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleUtils.notifyBle(bluetoothClient, notifyResponse);
            }
        }, 50);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ClientManager.getDevice() == null) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //先读取一下仪表状态
                writeMessage(LDBLCmd.readBikeData());
            }
        }, 500);
    }

    BleNotifyResponse notifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID uuid, UUID uuid1, byte[] value) {
            if (value.length < 3) {
                return;
            }
            runOnUiThread(() -> {
                if (value[0] == (byte) 0x3a && value[1] == (byte) 0xAB) {
                    //行车数据回复
                    int[] result = LDBLResponse.dealMeterMessage(value);
                    if (result[0] == 1) {
                        if (!hasBleStarted) {
                            getMeterData();
                        }
                    }
                    if (result[0] == 1) {//当前开机状态
                        isPowerOn = true;
                        dataBinding.llUnlock.setVisibility(View.GONE);
                        dataBinding.rideLayout.setVisibility(View.VISIBLE);
                    } else if (result[0] == 0 || result[0] == 3) {
                        //骑按键关机，进入开锁view
                        isPowerOn = false;
                        dataBinding.llUnlock.setVisibility(View.VISIBLE);
                        dataBinding.rideLayout.setVisibility(View.GONE);
                    }

                    if (!isPowerOn) {
                        return;
                    }
                    light = result[1];
                    pas = result[2];
                    int mileage = result[3];
                    int current = result[4];

                    int battery = result[10];
                    int speed = result[11];

                    int errorCode = result[12];
                    int controllerTemp = result[19];
                    int motorTemp = result[20];
                    int cadence = result[18];
                    int voltage = result[21];
                    int power = (int) ((voltage / 10f) * (current / 3f));
                    int brake = result[23];

                    BikeData bikeData = new BikeData();
                    bikeData.setEbikeId(ebikeId);
                    bikeData.setCycleId(cycleId);
                    bikeData.setRecTime(System.currentTimeMillis());
                    bikeData.setControllerTemperature(controllerTemp);
                    bikeData.setMotorTemperature(motorTemp);
                    bikeData.setCurrent((int) (current / 3f * 10));//0.1A
                    bikeData.setVoltage(voltage);
                    bikeData.setCadence(cadence);
                    bikeData.setPower(power);
                    bikeData.setSpeed(speed);
                    bikeData.setMileage(mileage);
                    bikeData.setFaultId(CustomUtil.byteToString(errorCode));
                    bikeData.setLamp(light);
                    if (pas == 10) {
                        bikeData.setSwkm6(1);
                    } else {
                        bikeData.setSwkm6(0);
                    }
                    bikeData.setBrake(brake);
                    bikeData.setSoc(battery);
                    SendMsgData bike = SendMsgDataUtil.createJsonData(2, 1, UserUtil.token, Constant.ebikeBizCode, bikeData);
                    sendMsg(new Gson().toJson(bike));


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (light == 0) {
                                dataBinding.ivLight.setImageResource(R.mipmap.ic_blue_light_off);
                            } else {
                                dataBinding.ivLight.setImageResource(R.mipmap.ic_blue_light_on);
                            }
                            dataBinding.tvOdo.setText(mileage / 10f + "km");
                            dataBinding.tvBatteryCurrent.setText(String.format("%.1f", current / 3f));
                            dataBinding.tvSoc.setText(battery + "%");
                            dataBinding.tvSpeed.setText(speed / 10f + "");
                            dataBinding.tvPas.setText(pas + "");
                            dataBinding.tvTapin.setText(cadence + "");
                            dataBinding.tvControllerTemp.setText(controllerTemp + "");
                            dataBinding.tvMotorTemp.setText(motorTemp + "");
                            dataBinding.tvBatteryVoltage.setText(voltage / 10f + "");
                        }
                    });

//                    String sb = "大灯：" + (light == 0 ? "关" : "开") + "\n" +
//                            "档位：" + pas + "\n" +
//                            "里程：" + mileage + "\n" +
//                            "电流：" + String.format("%.1f", current / 3f) + "\n" +
//                            "限速：" + speedLimit + "\n" +
//                            "轮径：" + wheel + "\n" +
//                            "轮径校准：" + correct + "\n" +
//                            "限流：" + currentLimit + "\n" +
//                            "电量：" + battery + "%" + "\n" +
//                            "速度：" + speed + "\n" +
//                            "故障：" + getError((byte) errorCode);

                } else if (value[0] == (byte) 0x3a && value[1] == (byte) 0x28) {
                    //GPS数据回复
                    String[] gpsResult = LDBLResponse.dealGPSData(value);
                    if (gpsResult.length < 7) {
                        return;
                    }
                    if (!StringUtils.isEmpty(gpsResult[0])) {
                        double[] c = GpsCoordinateUtils.calWGS84toGCJ02(Double.parseDouble(gpsResult[0]), Double.parseDouble(gpsResult[1]));
                        gpsLocOverlay.locationChanged(new LatLng(c[0], c[1]));

                        //推送位置数据
                        GisData gisData = new GisData();
                        gisData.setEbikeId(ebikeId);
                        gisData.setCycleId(cycleId);
                        gisData.setRecTime(System.currentTimeMillis());
                        gisData.setLng(gpsResult[1]);
                        gisData.setLat(gpsResult[0]);
                        gisData.setAlt(gpsResult[2]);
                        gisData.setSpeed(gpsResult[3]);
                        SendMsgData data1 = SendMsgDataUtil.createJsonData(2, 1, UserUtil.token, Constant.gisBizCode, gisData);
                        sendMsg(new Gson().toJson(data1));
                    }

                    GyroData gyroData = new GyroData();
                    gyroData.setRecTime(System.currentTimeMillis());
                    gyroData.setEbikeId(ebikeId);
                    gyroData.setCycleId(cycleId);
                    gyroData.setGyroYaw(String.valueOf(gpsResult[6]));
                    gyroData.setGyroPitc(String.valueOf(gpsResult[5]));
                    gyroData.setGyroRoll(String.valueOf(gpsResult[4]));
                    SendMsgData data1 = SendMsgDataUtil.createJsonData(2, 1, UserUtil.token, Constant.gyroBizCode, gyroData);
                    sendMsg(new Gson().toJson(data1));

                    //"纬度：" + c[0] + "\n经度：" + c[1] + "\n海拔：" + gpsResult[2] + "\n速度：" + gpsResult[3]);
                }
                Log.d("接收到", BleDataConvertUtil.byte2hex(value));
            });
        }

        @Override
        public void onResponse(int i) {

        }
    };

    private String getError(byte code) {
        String error = "";
        switch (code) {
            case 0x01:
                error = "过流保护故障";
                break;
            case 0x02:
                error = "欠压保护故障";
                break;
            case 0x03:
                error = "过压保护故障";
                break;
            case 0x04:
                error = "堵转保护故障";
                break;
            case 0x05:
                error = "驱动mos管上桥故障";
                break;
            case 0x06:
                error = "驱动mos管下桥故障";
                break;
            case 0x07:
                error = "霍尔故障";
                break;
            case 0x08:
                error = "控制器内部过温";
                break;
            case 0x09:
                error = "刹把故障";
                break;
            case 0x10:
                error = "转把故障";
                break;
            case 0x12:
                error = "控制器通信故障";
                break;
            case 0x13:
                error = "电池通信故障";
                break;
            case 0x31:
                error = "电机过温";
                break;
            case 0x33:
                error = "电池过温";
                break;
            case 0x40:
                error = "后刹车故障";
                break;
            case 0x00:
                error = "空闲无故障";
                break;
        }
        return error;
    }

    @Override
    protected void initData() {
        if (ClientManager.getDevice() != null) {
            initBleData();
        }
        //启动服务
        startJWebSClientService();
        //绑定服务
        bindService();
        //注册广播
        doRegisterReceiver();

        viewModel.getNotifyUnlockResult().observe(this, notifyUnLockResponse -> {
            dismissDialog();
            isPowerOn = true;
            cycleId = notifyUnLockResponse.getCycleOrder().getCycleId();
            if (!hasBleStarted) {
                getMeterData();
            }
        });

        viewModel.getNotifylockResult().observe(this, createPayOrderBean -> {
            dismissDialog();
            isPowerOn = false;
            handler.removeCallbacksAndMessages(null);
            hasBleStarted = false;
        });

        viewModel.getEbikeDetailResult().observe(this, ebikeDetailResponse -> {
            ebikeId = ebikeDetailResponse.getIotEbike().getEbikeId();
        });
        String mac = ClientManager.getDevice().getMac();
        viewModel.findEbikeDetailByMac(mac);
    }

    private int sendCount = 0;

    private void getMeterData() {
        hasBleStarted = true;
        sendCount = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bleConnect) {
                    if (sendCount == 1) {
                        writeMessage(LDBLCmd.iotGpsData());
                        sendCount = 0;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (setPowerOff) {//关机操作
                                    writeMessage(LDBLCmd.bikePowerOff());
//                            writeMessage(LDBLCmd.bikeLock());
                                    setPowerOff = false;
                                } else if (setPowerOn) {//开机操作
                                    writeMessage(LDBLCmd.bikePowerOn());
                                    setPowerOn = false;
                                } else if (setLight && light == 0) {
                                    writeMessage(LDBLCmd.bikeLightOn());
                                    setLight = false;
                                } else if (setLight && light == 1) {
                                    writeMessage(LDBLCmd.bikeLightOff());
                                    setLight = false;
                                } else if (setPasAdd) {
                                    writeMessage(LDBLCmd.bikeCPas(pas + 1));
                                    setPasAdd = false;
                                } else if (setPasSub) {
                                    writeMessage(LDBLCmd.bikeCPas(pas - 1));
                                    setPasSub = false;
                                } else if (setWalkOn) {
                                    writeMessage(LDBLCmd.bikeWalk());
                                } else {
                                    //只读
                                    writeMessage(LDBLCmd.readBikeData());
                                }
                            }
                        }, 100);
                    } else {
                        if (setPowerOff) {//关机操作
                            writeMessage(LDBLCmd.bikePowerOff());
//                            writeMessage(LDBLCmd.bikeLock());
                            setPowerOff = false;
                        } else if (setPowerOn) {//开机操作
                            writeMessage(LDBLCmd.bikePowerOn());
                            setPowerOn = false;
                        } else if (setLight && light == 0) {
                            writeMessage(LDBLCmd.bikeLightOn());
                            setLight = false;
                        } else if (setLight && light == 1) {
                            writeMessage(LDBLCmd.bikeLightOff());
                            setLight = false;
                        } else if (setPasAdd) {
                            writeMessage(LDBLCmd.bikeCPas(pas + 1));
                            setPasAdd = false;
                        } else if (setPasSub) {
                            writeMessage(LDBLCmd.bikeCPas(pas - 1));
                            setPasSub = false;
                        } else if (setWalkOn) {
                            writeMessage(LDBLCmd.bikeWalk());
                        } else {
                            //只读
                            writeMessage(LDBLCmd.readBikeData());
                        }
                        sendCount++;
                    }
                }
                handler.postDelayed(this, 200);
            }
        }, 50);
    }

    private void writeMessage(byte[] data) {
        if (ClientManager.getDevice() != null) {
            BleUtils.writeBle(bluetoothClient, data, writeResponse);
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(String msg) {
        LogUtil.e(msg);
        if (jWebSClientService != null && client == null) {
            client = jWebSClientService.client;
        }

        if (null != client) {
            LogUtil.d("JWebSocketClientService", "发送的消息：" + msg);
            if (client.getReadyState().equals(ReadyState.OPEN)) {
                client.send(msg);
            }
        }
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(this, JWebSocketClientService.class);
        bindService(bindIntent, socketServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection socketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtil.d("服务与活动绑定");
            binder = (JWebSocketClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.d("服务与活动断开");
        }
    };

    /**
     * 启动服务（websocket客户端服务）
     */
    private void startJWebSClientService() {
        Intent intent = new Intent(this, JWebSocketClientService.class);
        startService(intent);
    }

    /**
     * 动态注册广播
     */
    private void doRegisterReceiver() {
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter(Constant.WEBSOCKET_SERVICE_ACTION);
        registerReceiver(chatMessageReceiver, filter);
    }

    @Override
    protected RideViewModel initViewModel() {
        return new ViewModelProvider(this).get(RideViewModel.class);
    }

    @Override
    protected void showError(Object o) {
        dismissDialog();
        if (o instanceof Error) {
            ToastUtils.showShort(((Error) o).getMessage());
        }

    }

    /**
     * websocket消息接收及处理
     */
    private class ChatMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            LogUtil.e("收到推送--" + message);
            JSONObject object = null;
            try {
                object = new JSONObject(message);
                if (object.getInt("msgType") == 1) {
                    //收到推送的支付订单
                    if (object.getString("bizCode").equals("payOrder")) {
                        dismissDialog();
                        handler.removeCallbacksAndMessages(null);
                        hasBleStarted = false;

                    } else if (object.getString("bizCode").equals("cycleOrder")) {
                        dismissDialog();
                        //开始骑行
                        CycleOrder cycleOrder = new Gson().fromJson(object.getString("content"), CycleOrder.class);
                        cycleId = cycleOrder.getCycleId();
                        if (!hasBleStarted) {
                            getMeterData();
                        }
                    }
                }
//                else {
//                    if (object.getString("bizCode").equals("authFail")) {
//                        Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
//                        startActivity(intent1);
//                        finish();
//                    }
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetoothClient != null && ClientManager.getDevice() != null) {
            BleUtils.unNotifyBle(bluetoothClient, new BleUnnotifyResponse() {
                @Override
                public void onResponse(int i) {

                }
            });
            BleUtils.disConnect(bluetoothClient);
            ClientManager.setDevice(null);
        }
        if (socketServiceConnection != null && jWebSClientService != null) {
            unbindService(socketServiceConnection);
        }
        if (chatMessageReceiver != null) {
            unregisterReceiver(chatMessageReceiver);
        }
        super.onDestroy();
        dataBinding.amap.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataBinding.amap.onResume();
    }

    @Override
    protected void onPause() {
        dataBinding.amap.onPause();
        super.onPause();
    }
}