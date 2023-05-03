package com.lsdzs.lsdzs_tool.ble.canbus;

import com.lsdzs.lsdzs_tool.ble.ControllerDataUtils;

public class CANBUSCmd {
    public static byte key1, key2, key3, key4;
    public static int x1, x2, x3, x4;

    public static byte[] getControllerData(int p, int pgn) {
        byte[] data = new byte[14];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = (byte) (p << 2);
        data[3] = (byte) 0xEA;//固定值，代表查询
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0xD5;//固定值 IOT
        data[6] = 0x03;//数据长度
        data[7] = (byte) (pgn & 0xff);
        data[8] = (byte) (pgn >> 8);
        data[9] = (byte) (pgn >> 16);
        data[10] = (byte) ControllerDataUtils.CRC16H(data, 10);
        data[11] = (byte) ControllerDataUtils.CRC16L(data, 10);
        data[12] = 0x0D;
        data[13] = 0x0A;
        return data;
    }

    public static byte[] writePasLightDta(int pas, int walkMode, int lightStatus) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 3 << 2;
        data[3] = (byte) 0xFD;
        data[4] = (byte) 0x01;
        data[5] = (byte) 0xD5;//固定值 测试设备
        data[6] = 0x08;//数据长度
        data[7] = (byte) (pas << 4);
        if (walkMode != 0) { // 6km推行
            data[7] |= 0x08;
        } else {
            data[7] &= ~0x08;
        }
        if (lightStatus != 0) { // 大灯
            data[7] |= 0x04;
        } else {
            data[7] &= ~0x04;
        }
        for (int i = 1; i < 7; i++) {
            data[7 + i] = 0;
        }
        data[15] = (byte) ControllerDataUtils.CRC16H(data, 15);
        data[16] = (byte) ControllerDataUtils.CRC16L(data, 15);
        data[17] = 0x0D;
        data[18] = 0x0A;
        return data;
    }

    public static byte[] getBMSData(int p, int pgn) {
        byte[] data = new byte[14];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = (byte) (p << 2);
        data[3] = (byte) 0xEA;//固定值，代表查询
        data[4] = (byte) 0xF4;//SA ,控制器EF 电池F4
        data[5] = (byte) 0xD5;//固定值 IOT
        data[6] = 0x03;//数据长度
        data[7] = (byte) (pgn & 0xff);
        data[8] = (byte) (pgn >> 8);
        data[9] = (byte) (pgn >> 16);
        data[10] = (byte) ControllerDataUtils.CRC16H(data, 10);
        data[11] = (byte) ControllerDataUtils.CRC16L(data, 10);
        data[12] = 0x0D;
        data[13] = 0x0A;
        return data;
    }

    public static byte[] writeControllerData(int address, int value) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE0;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        byte[] setdata = getConfigWriteData(address, value);
        for (int i = 0; i < setdata.length; i++) {
            data[7 + i] = setdata[i];
        }
        data[7 + setdata.length] = (byte) ControllerDataUtils.CRC16H(data, 7 + setdata.length);
        data[8 + setdata.length] = (byte) ControllerDataUtils.CRC16L(data, 7 + setdata.length);
        data[9 + setdata.length] = 0x0D;
        data[10 + setdata.length] = 0x0A;
        return data;
    }

    /**
     * 读取控制器参数
     *
     * @param address
     * @return
     */
    public static byte[] readControllerData(int address) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE1;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        byte[] setdata = getConfigReadData(address, 0);
        for (int i = 0; i < setdata.length; i++) {
            data[7 + i] = setdata[i];
        }
        data[7 + setdata.length] = (byte) ControllerDataUtils.CRC16H(data, 7 + setdata.length);
        data[8 + setdata.length] = (byte) ControllerDataUtils.CRC16L(data, 7 + setdata.length);
        data[9 + setdata.length] = 0x0D;
        data[10 + setdata.length] = 0x0A;
        return data;
    }

    /**
     * 故障检测
     *
     * @param flag   标记哪个设备
     * @param status 0：结束 1：开始
     * @return
     */
    public static byte[] sendCheckData(int flag, int status) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE2;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        data[7] = (byte) flag;
        data[8] = (byte) status;
        data[9] = 0;
        data[10] = 0;
        data[11] = 0;
        data[12] = 0;
        data[13] = 0;
        data[14] = 0;
        data[15] = (byte) ControllerDataUtils.CRC16H(data, 15);
        data[16] = (byte) ControllerDataUtils.CRC16L(data, 15);
        data[17] = 0x0D;
        data[18] = 0x0A;
        return data;
    }

    /**
     * 进入参数设置模式
     *
     * @param address
     * @return
     */
    public static byte[] enterControllerSet(int address) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE1;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        byte[] setdata = getConfigReadData(address, 1);
        for (int i = 0; i < setdata.length; i++) {
            data[7 + i] = setdata[i];
        }
        data[7 + setdata.length] = (byte) ControllerDataUtils.CRC16H(data, 7 + setdata.length);
        data[8 + setdata.length] = (byte) ControllerDataUtils.CRC16L(data, 7 + setdata.length);
        data[9 + setdata.length] = 0x0D;
        data[10 + setdata.length] = 0x0A;
        return data;
    }

    /**
     * 退出参数查询功能
     *
     * @param address
     * @return
     */
    public static byte[] existControllerSet(int address) {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE1;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        byte[] setdata = getConfigReadData(address, 2);
        for (int i = 0; i < setdata.length; i++) {
            data[7 + i] = setdata[i];
        }
        data[7 + setdata.length] = (byte) ControllerDataUtils.CRC16H(data, 7 + setdata.length);
        data[8 + setdata.length] = (byte) ControllerDataUtils.CRC16L(data, 7 + setdata.length);
        data[9 + setdata.length] = 0x0D;
        data[10 + setdata.length] = 0x0A;
        return data;
    }

    /**
     * 控制器复位
     *
     * @return
     */
    public static byte[] resetController() {
        byte[] data = new byte[19];
        data[0] = 0x3A;
        data[1] = 0x5B;
        data[2] = 6 << 2;
        data[3] = (byte) 0xE3;//固定值
        data[4] = (byte) 0xEF;//SA ,控制器EF 电池F4
        data[5] = (byte) 0x20;//固定值 测试设备
        data[6] = 0x08;//数据长度
        byte[] setdata = {0x4D, 0x43, 0x52, 0x45, 0x42, 0x4F, 0x4F, 0x54};
        for (int i = 0; i < setdata.length; i++) {
            data[7 + i] = setdata[i];
        }
        data[7 + setdata.length] = (byte) ControllerDataUtils.CRC16H(data, 7 + setdata.length);
        data[8 + setdata.length] = (byte) ControllerDataUtils.CRC16L(data, 7 + setdata.length);
        data[9 + setdata.length] = 0x0D;
        data[10 + setdata.length] = 0x0A;
        return data;
    }


    public static void setKeys(int key01, int key02, int key03, int key04) {
        if (key01 == key02 && key02 == key03 && key03 == key04 && key01 == 0) {
            key1 = 0x59;
            key2 = 0x74;
            key3 = 0x21;
            key4 = 0x25;
        } else {
            key1 = (byte) key01;
            key2 = (byte) key02;
            key3 = (byte) key03;
            key4 = (byte) key04;
        }
        byte[] keyArray1 = {(byte) key1, (byte) key3};
        x1 = CRC16H(keyArray1, 2);
        x2 = CRC16L(keyArray1, 2);
        byte[] keyArray2 = {(byte) key2, (byte) key4};
        x3 = CRC16H(keyArray2, 2);
        x4 = CRC16L(keyArray2, 2);
    }

    public static byte[] getConfigWriteData(int address, int data) {
        byte[] sendOut = new byte[8];
        sendOut[0] = (byte) EncryptionAL(address);
        sendOut[1] = (byte) EncryptionAH(address);
        sendOut[2] = (byte) EncryptionDL(data);
        sendOut[3] = (byte) EncryptionDH(data);
        sendOut[4] = key1;
        sendOut[5] = key2;
        sendOut[6] = key3;
        sendOut[7] = key4;
        return sendOut;
    }

    public static byte[] getConfigReadData(int address, int data) {
        byte[] sendOut = new byte[8];
        sendOut[0] = (byte) EncryptionAL(address);
        sendOut[1] = (byte) EncryptionAH(address);
        sendOut[2] = 0;
        sendOut[3] = (byte) data;
        sendOut[4] = key1;
        sendOut[5] = key2;
        sendOut[6] = key3;
        sendOut[7] = key4;
        return sendOut;
    }

    /**
     * 获取Address-H
     *
     * @param address
     * @return Address-H
     */
    private static int EncryptionAH(int address) {
        return (address >> 8) ^ x1 ^ 0x7D;
    }

    /**
     * 获取Address-L
     *
     * @param address
     * @return Address-L
     */
    private static int EncryptionAL(int address) {
        return (address & 0xff) ^ x2 ^ 0x9B;
    }

    /**
     * 获取Data-H
     *
     * @param data
     * @return Data-H
     */
    private static int EncryptionDH(int data) {
        return (data >> 8) ^ x3 ^ 0x54;
    }

    /**
     * 获取Data-L
     *
     * @param data
     * @return Data-L
     */
    private static int EncryptionDL(int data) {
        return (data & 0xff) ^ x4 ^ 0x3E;
    }

    public static int CRC16(byte[] bytes, int len) {
        int crc = 0xffff;
        int i, j;
        for (j = 0; j < len; j++) {
            crc ^= ((int) bytes[j] & 0xff);
            for (i = 0; i < 8; i++) {
                if ((crc & 0x0001) > 0) {
                    crc >>= 1;
                    crc ^= 0xa001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc;
    }

    /**
     * 校验位高字节
     *
     * @param bytes
     * @param len
     * @return
     */
    public static int CRC16H(byte[] bytes, int len) {
        return CRC16(bytes, len) >> 8;
    }

    /**
     * 校验位低字节
     *
     * @param bytes
     * @param len
     * @return
     */
    public static int CRC16L(byte[] bytes, int len) {
        return CRC16(bytes, len) & 0xff;
    }
}
