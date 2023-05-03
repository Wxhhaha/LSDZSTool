package com.lsdzs.lsdzs_tool;

import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.wxh.basiclib.base.BaseApplication;
import com.wxh.basiclib.http.HttpManager;

public class App extends BaseApplication {
    @Override
    public void setBaseUrl() {
        HttpManager.setBaseUrl(Urls.HTTP_URL);
    }

    @Override
    public void showLog() {
        HttpManager.setShowLog(true);
    }

    @Override
    public void configAutoSize() {

    }

    @Override
    public void create() {
        ClientManager.setSeriveUUID("0000ffe0-0000-1000-8000-00805f9b34fb");
        ClientManager.setNotifyUUID("0000ffe1-0000-1000-8000-00805f9b34fb");
        ClientManager.setWriteUUID("0000ffe1-0000-1000-8000-00805f9b34fb");
        BleUtils.setBluetooth(this);
    }
}
