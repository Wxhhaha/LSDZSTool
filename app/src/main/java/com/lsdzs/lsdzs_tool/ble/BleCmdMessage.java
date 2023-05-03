package com.lsdzs.lsdzs_tool.ble;

public class BleCmdMessage {
    /**
     * 获取key指令
     */
    public static byte[] sendKeyMessage() {
        byte[] data = new byte[12];
        data[0] = (byte) 0xa1;
        data[1] = 8;
        data[2] = 10;
        data[3] = 0x01;
        //6位设备key
        String key = genSecret(ClientManager.getDevice().getMac());
        byte[] keys = BleDataConvertUtil.HexString2Bytes(key);
        for (int i = 0; i < keys.length; i++) {
            data[4 + i] = keys[i];
        }
        int len = keys.length;
        data[4 + len] = CRC8Util.calcCrc8(data, 10);
        data[5 + len] = (byte) 0xD1;
        return data;
    }

    /**
     * 发送关锁指令
     *
     * @param messageKey
     */
    public static byte[] sendLockMessage(byte messageKey) {
        byte[] data = new byte[7];
        data[0] = (byte) 0xa1;
        data[1] = 3;
        data[2] = messageKey;
        data[3] = 0x10;
        data[4] = 0x01;
        data[5] = CRC8Util.calcCrc8(data, 5);
        data[6] = (byte) 0xD1;
        return data;
    }

    /**
     * 开锁指令
     */
    public static byte[] sendUnLockMessage(byte messageKey) {
        byte[] data = new byte[7];
        data[0] = (byte) 0xa1;
        data[1] = 3;
        data[2] = messageKey;
        data[3] = 0x05;
        data[4] = 0x01;
        data[5] = CRC8Util.calcCrc8(data, 5);
        data[6] = (byte) 0xD1;
        return data;
    }

    /**
     * 获取定位数据
     */
    public static byte[] sendLocationMessage(byte messageKey,int type) {
        byte[] data = new byte[7];
        data[0] = (byte) 0xa1;
        data[1] = 3;
        data[2] = messageKey;
        data[3] = 0x13;
        data[4] = (byte) type;//0:基站定位 1：搜星定位
        data[5] = CRC8Util.calcCrc8(data, 5);
        data[6] = (byte) 0xD1;
        return data;
    }

    public static String genSecret(String mac) {
        return mac.replace(":", "");
    }
}
