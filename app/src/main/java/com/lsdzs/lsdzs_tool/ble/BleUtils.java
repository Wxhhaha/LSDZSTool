package com.lsdzs.lsdzs_tool.ble;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;

import android.app.Application;
import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.wxh.basiclib.utils.LogUtil;

import java.util.HashMap;
import java.util.List;

public class BleUtils {
    public static HashMap<String, Device> deviceMap;

    public static void resetDevicesList() {
        if (deviceMap != null) {
            deviceMap.clear();
        }
    }

    public static void setBluetooth(Application app) {
        BluetoothContext.set(app);
    }

    public static BluetoothClient getClient(Context context) {
        return ClientManager.getClient(context);
    }

    /**
     * 搜索蓝牙
     *
     * @param client
     * @param duration
     * @param times
     * @param searchResponse
     */
    public static void searchBluetooth(BluetoothClient client, int duration, int times, SearchResponse searchResponse) {
        if (!client.isBluetoothOpened()) {
            client.openBluetooth();
        } else {
            SearchRequest request = new SearchRequest.Builder().searchBluetoothLeDevice(duration, times).build();
            client.search(request, searchResponse);
        }
    }

    public static void connect(BluetoothClient client, Device device, BleConnectStatusListener connectListener, ConnectResult result) {
        client.registerConnectStatusListener(device.getMac(), connectListener);
        BleConnectOptions options =
                new BleConnectOptions.Builder().setConnectRetry(3).setConnectTimeout(10000).setServiceDiscoverRetry(3).setServiceDiscoverTimeout(3000).build();

        client.connect(device.getMac(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (data == null) {
                    result.fail();
                } else {
                    if (code == REQUEST_SUCCESS) {
                        List<BleGattService> services = data.getServices();
                        for (BleGattService service : services) {
                            if (ClientManager.getSeriveUUID() != null && service.getUUID().toString().equals(ClientManager.getSeriveUUID())) {
                                List<BleGattCharacter> characters = service.getCharacters();
                                for (BleGattCharacter character : characters) {
                                    if (ClientManager.getWriteUUID() != null && character.getUuid().toString().equals(ClientManager.getWriteUUID())) {
                                        ClientManager.setService(service);
                                        ClientManager.setWriteCharacter(character);
                                        ClientManager.setDevice(device);
                                    }
                                    if (ClientManager.getNotifyUUID() != null && character.getUuid().toString().equals(ClientManager.getNotifyUUID())) {
                                        ClientManager.setService(service);
                                        ClientManager.setNotifyCharacter(character);
                                        ClientManager.setDevice(device);
                                    }
                                }
                            }
                        }
                        result.success();
                    } else {
                        result.fail();
                    }
                }
            }
        });
    }

    public static void disConnect(BluetoothClient client) {
        client.disconnect(ClientManager.getDevice().getMac());
        ClientManager.setDevice(null);
    }

    public static void notifyBle(BluetoothClient client, BleNotifyResponse notifyRsp) {
        client.notify(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getNotifyCharacter().getUuid(), notifyRsp);
    }

    public static void unNotifyBle(BluetoothClient client, BleUnnotifyResponse unNotifyRsp) {
        client.unnotify(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getNotifyCharacter().getUuid(), unNotifyRsp);
    }

    public static void registerConnectStatus(BluetoothClient client, BleConnectStatusListener connectStatusListener) {
        client.registerConnectStatusListener(ClientManager.getDevice().getMac(),
                connectStatusListener);
    }

    public static void unRegisterConnectStatus(BluetoothClient client, BleConnectStatusListener connectStatusListener) {
        client.unregisterConnectStatusListener(ClientManager.getDevice().getMac(),
                connectStatusListener);
    }

    public static void readBle(BluetoothClient client, BleReadResponse readRsp) {
        client.read(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getReadCharacter().getUuid(), readRsp);
    }

    public static void writeBle(BluetoothClient client, byte[] data, BleWriteResponse writeRsp) {
        LogUtil.d("发送"+BleDataConvertUtil.byte2hex(data));
        client.writeNoRsp(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getWriteCharacter().getUuid(), data, writeRsp);
    }

    public static void indicateBle(BluetoothClient client, BleNotifyResponse notifyRsp) {
        client.indicate(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getNotifyCharacter().getUuid(), notifyRsp);
    }

    public static void unIndicateBle(BluetoothClient client, BleUnnotifyResponse notifyRsp) {
        client.unindicate(ClientManager.getDevice().getMac(),
                ClientManager.getservice().getUUID(), ClientManager.getNotifyCharacter().getUuid(), notifyRsp);
    }

    public interface ConnectResult {
        void success();

        void fail();
    }
}
