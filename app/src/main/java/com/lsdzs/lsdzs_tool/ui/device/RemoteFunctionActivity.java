package com.lsdzs.lsdzs_tool.ui.device;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lsdzs.lsdzs_tool.MymqttService;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.databinding.ActivityRemoteFunctionBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;
import com.wxh.basiclib.location.GpsCoordinateUtils;
import com.wxh.basiclib.utils.LogUtil;
import com.wxh.basiclib.utils.StringUtils;

import org.json.JSONObject;

public class RemoteFunctionActivity extends BaseNoModelActivity<ActivityRemoteFunctionBinding> {
    private String clientId;
    private static final int UNLOCK_TIMEOUT = 13;

    private static final int LOCK_TIMEOUT = 14;

    private int TIMEOUT_VALUE = 5000;

    private int H0_TIMEOUT_VALUE = 5 * 60 * 1000;

    private static final int H0_TIMEOUT = 15;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case UNLOCK_TIMEOUT:
                    dismissDialog();
                    ToastUtils.showShort("开锁失败");
                    break;
                case LOCK_TIMEOUT:
                    dismissDialog();
                    ToastUtils.showShort("关锁失败");
                    break;
                case H0_TIMEOUT:
                    dataBinding.tvGps.setText("Device Offline");
                    break;
            }
            return false;
        }
    });

    @Override
    protected int getLayout() {
        return R.layout.activity_remote_function;
    }

    @Override
    protected void initView(Bundle bundle) {

    }

    @Override
    protected void initData() {
        clientId = getIntent().getStringExtra("ebikeId");
        dataBinding.bt4gUnlock.setOnClickListener(v -> {
            showDialog("");
            send4gOpenMessage();
        });

        dataBinding.bt4gLock.setOnClickListener(v -> {
            showDialog("");
            send4gCloseMessage();
        });
        send4gGPSMessage();
        handler.sendEmptyMessageDelayed(H0_TIMEOUT, H0_TIMEOUT_VALUE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lsdzs.freedaretest.mqttservice");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.lsdzs.freedaretest.mqttservice")) {
                String content = intent.getStringExtra("content");
                try {
                    JSONObject object = new JSONObject(content);
                    if (clientId.equals(object.getString("clientId"))) {
                        handler.removeMessages(H0_TIMEOUT);
                        String[] msg = object.getString("msg").split(",");
                        if (msg[2].equals("L1") && msg[3].startsWith("0")) {
                            //4G操作
                            //开锁成功
                            dismissDialog();
                            handler.removeMessages(UNLOCK_TIMEOUT);
                            dataBinding.bt4gUnlock.setVisibility(View.GONE);
                            dataBinding.bt4gLock.setVisibility(View.VISIBLE);

                        } else if (msg[2].equals("L2") && msg[3].startsWith("0")) {
                            //todo 关锁成功
                            dismissDialog();
                            handler.removeMessages(LOCK_TIMEOUT);
                            dataBinding.bt4gUnlock.setVisibility(View.VISIBLE);
                            dataBinding.bt4gLock.setVisibility(View.GONE);
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
                        } else if (msg[2].equals("H0")) {
                            //有心跳，说明设备在线
                            if(msg[3].equals("1")){
                                //关锁状态
                                dataBinding.bt4gUnlock.setVisibility(View.VISIBLE);
                                dataBinding.bt4gLock.setVisibility(View.GONE);
                            }else{
                                //开锁状态
                                dataBinding.bt4gUnlock.setVisibility(View.GONE);
                                dataBinding.bt4gLock.setVisibility(View.VISIBLE);
                            }
                            if (msg.length > 9) {
                                String lat = msg[10];
                                String lng = msg[12];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!StringUtils.isEmpty(lat)) {
                                            double[] c = GpsCoordinateUtils.calWGS84toGCJ02(Double.parseDouble(lat), Double.parseDouble(lng));
                                            dataBinding.tvGps.setText(String.format("%s,%s", c[0], c[1]));
                                        }
                                    }
                                });
                            }

                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    };
}