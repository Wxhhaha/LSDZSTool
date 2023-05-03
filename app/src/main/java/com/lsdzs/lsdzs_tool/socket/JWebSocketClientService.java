package com.lsdzs.lsdzs_tool.socket;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import com.lsdzs.lsdzs_tool.Constant;
import com.lsdzs.lsdzs_tool.Urls;
import com.lsdzs.lsdzs_tool.UserUtil;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

/**
 * 创建时间:2022/1/21 13:34
 * 作者:wxh
 */
public class JWebSocketClientService extends Service {
    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;

    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
//        PowerManager.WakeLock wakeLock;//锁屏唤醒
//        //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
//        @SuppressLint("InvalidWakeLockTag")
//        private void acquireWakeLock()
//        {
//            if (null == wakeLock)
//            {
//                PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
//                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,
//                "PostLocationService");
//                if (null != wakeLock)
//                {
//                    wakeLock.acquire();
//                }
//            }
//        }

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public JWebSocketClientService getService() {
            return JWebSocketClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化websocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测

        //设置service为前台服务，提高优先级
        //        if (Build.VERSION.SDK_INT < 18) {
        //            //Android4.3以下 ，隐藏Notification上的图标
        //            startForeground(GRAY_SERVICE_ID, new Notification());
        //        } else if(Build.VERSION.SDK_INT>18 && Build.VERSION.SDK_INT<25){
        //            //Android4.3 - Android7.0，隐藏Notification上的图标
        //            Intent innerIntent = new Intent(this, GrayInnerService.class);
        //            startService(innerIntent);
        //            startForeground(GRAY_SERVICE_ID, new Notification());
        //        }else{
        //            //Android7.0以上app启动后通知栏会出现一条"正在运行"的通知
        //            startForeground(GRAY_SERVICE_ID, new Notification());
        //        }

        //        acquireWakeLock();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

    public JWebSocketClientService() {
    }


    /**
     * 初始化websocket连接
     */
    private void initSocketClient() {
        URI uri = URI.create(Urls.WS_URL);
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                Log.e("JWebSocketClientService", "收到的消息：" + message);

                Intent intent = new Intent();
                intent.setAction(Constant.WEBSOCKET_SERVICE_ACTION);
                intent.putExtra("message", message);
                sendBroadcast(intent);
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                Log.e("JWebSocketClientService", "websocket连接成功");
                if(UserUtil.token!=null){
                    JSONObject object = new JSONObject();
                    try {
                        object.put("requestType", 1);
                        object.put("token", UserUtil.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendMsg(object.toString());
                }
            }
        };
        connect();
    }

    /**
     * 连接websocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(String msg) {
        if (null != client) {
            Log.e("JWebSocketClientService", "发送的消息：" + msg);
            client.send(msg);
        }
    }

    /**
     * 断开连接
     */
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    //    -------------------------------------websocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }
            } else {
                //如果client已为空，重新初始化连接
                client = null;
                initSocketClient();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("JWebSocketClientService", "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
