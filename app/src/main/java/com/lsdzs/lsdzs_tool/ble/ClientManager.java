package com.lsdzs.lsdzs_tool.ble;

import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattService;

public class ClientManager {
    private static String seriveUUID;
    private static String writeUUID;
    private static String notifyUUID;
    private static BluetoothClient bleClient;
    private static BleGattService service;
    private static BleGattCharacter writeCharacter;
    private static BleGattCharacter readCharacter;
    private static BleGattCharacter notifyCharacter;
    private static Device device;
    public static BluetoothClient getClient(Context context) {
        if (bleClient == null) {
            synchronized (ClientManager.class) {
                if (bleClient == null) {
                    bleClient = new BluetoothClient(context);
                }
            }
        }
        return bleClient;
    }

    public static BleGattService getservice() {
        return service;
    }

    public static void setService(BleGattService service) {
        ClientManager.service = service;
    }

    public static BleGattCharacter getWriteCharacter() {
        return writeCharacter;
    }

    public static void setWriteCharacter(BleGattCharacter writeCharacter) {
        ClientManager.writeCharacter = writeCharacter;
    }

    public static Device getDevice() {
        return device;
    }

    public static void setDevice(Device device) {
        ClientManager.device = device;
    }

    public static BleGattCharacter getReadCharacter() {
        return readCharacter;
    }

    public static void setReadCharacter(BleGattCharacter readCharacter) {
        ClientManager.readCharacter = readCharacter;
    }

    public static BleGattCharacter getNotifyCharacter() {
        return notifyCharacter;
    }

    public static void setNotifyCharacter(BleGattCharacter notifyCharacter) {
        ClientManager.notifyCharacter = notifyCharacter;
    }

    public static String getSeriveUUID() {
        return seriveUUID;
    }

    public static void setSeriveUUID(String seriveUUID) {
        ClientManager.seriveUUID = seriveUUID;
    }

    public static String getWriteUUID() {
        return writeUUID;
    }

    public static void setWriteUUID(String writeUUID) {
        ClientManager.writeUUID = writeUUID;
    }

    public static String getNotifyUUID() {
        return notifyUUID;
    }

    public static void setNotifyUUID(String notifyUUID) {
        ClientManager.notifyUUID = notifyUUID;
    }
}
