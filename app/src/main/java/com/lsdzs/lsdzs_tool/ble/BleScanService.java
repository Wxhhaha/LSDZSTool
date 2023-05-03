package com.lsdzs.lsdzs_tool.ble;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.HashMap;
import java.util.Timer;

public class BleScanService extends Service {
//    private BluetoothClient client;
    private Timer timer;

    @Override
    public void onCreate() {
   //     client = ClientManager.getClient(getApplicationContext());
//        if (!client.isBluetoothOpened()) {
//            client.openBluetooth();
//        }
//        timer = new Timer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public BleScanService getService() {
            return BleScanService.this;
        }
    }

    public void startScan(BluetoothClient client) {
        BleUtils.deviceMap = new HashMap<>();
        SearchRequest request = new SearchRequest.Builder().searchBluetoothLeDevice(5000, 1).build();
//        if (ClientManager.getDevice() == null) {
            client.search(request, new SearchResponse() {
                @Override
                public void onSearchStarted() {

                }

                @Override
                public void onDeviceFounded(SearchResult searchResult) {
                    //todo,保存符合条件的设备
                    if (searchResult.getName() != null && searchResult.getName().length() > 0) {
                        Device device = new Device();
                        device.setMac(searchResult.getAddress().toUpperCase());
                        device.setName(searchResult.getName());
                        BleUtils.deviceMap.put(device.getMac(), device);
                    }
                }

                @Override
                public void onSearchStopped() {

                }

                @Override
                public void onSearchCanceled() {

                }
            });
//        }
    }

    public void stopScan() {
        ClientManager.getClient(getApplicationContext()).stopSearch();
    }
}
