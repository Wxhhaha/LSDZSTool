package com.lsdzs.lsdzs_tool.ble;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;

import java.util.LinkedHashMap;

public class ControllerDataUtils {
    private static ControllerDataUtils instance;
    private byte key1, key2, key3, key4;
    private int x1, x2, x3, x4;
    public LinkedHashMap<Integer, Integer> paraAddressValueHashMap = new LinkedHashMap<>();

    public static ControllerDataUtils getInstance() {
        if (instance == null) {
            instance = new ControllerDataUtils();
        }
        return instance;
    }

//    public String sendOutData(ParaModel model) {
//        byte[] sendOut = new byte[10];
//        sendOut[0] = 0x3A;
//        sendOut[1] = 0x53;
//        sendOut[2] = (byte) EncryptionAH(model.getAddress());
//        sendOut[3] = (byte) EncryptionAL(model.getAddress());
//        sendOut[4] = (byte) EncryptionDH(model.getValue());
//        sendOut[5] = (byte) EncryptionDL(model.getValue());
//        sendOut[6] = (byte) CRC16H(sendOut, 6);
//        sendOut[7] = (byte) CRC16L(sendOut, 6);
//        sendOut[8] = 0x0D;
//        sendOut[9] = 0x0A;
//        return CustomUtil.byte2hex(sendOut);
//    }

    public void setKeys(byte[] values) {
        key1 = values[2];
        key2 = values[3];
        key3 = values[4];
        key4 = values[5];
        byte[] keyArray1 = {key1, key3};
        x1 = CRC16H(keyArray1, 2);
        x2 = CRC16L(keyArray1, 2);
        byte[] keyArray2 = {key2, key4};
        x3 = CRC16H(keyArray2, 2);
        x4 = CRC16L(keyArray2, 2);
    }


    /**
     * 获取参数命令
     *
     * @param address
     * @return 发送命令
     */
    public byte[] getConfigData(int address) {
        byte[] sendOut = new byte[8];
        sendOut[0] = 0x3A;
        sendOut[1] = 0x52;
        sendOut[2] = (byte) EncryptionAH(address);
        sendOut[3] = (byte) EncryptionAL(address);
        sendOut[4] = (byte) CRC16H(sendOut, 4);
        sendOut[5] = (byte) CRC16L(sendOut, 4);
        sendOut[6] = 0x0D;
        sendOut[7] = 0x0A;
        return ArrayUtils.add(sendOut, 0, (byte) 0x33);
    }

    public void dealConfigResultData(byte[] value) {
        int addH, addL, valH, valL;
        addH = (value[2] & 0xff) ^ x1 ^ 0x7D;
        addL = (value[3] & 0xff) ^ x2 ^ 0x9B;
        valH = (value[4] & 0xff) ^ x3 ^ 0x54;
        valL = (value[5] & 0xff) ^ x4 ^ 0x3E;
        if ((((value[6] & 0xff) << 8) + (value[7] & 0xff)) == CRC16(value, 6) && ((valH << 8) + valL) != 0xFFFF) {
            int address = (addH << 8) + addL;
            int valueData = (valH << 8) + valL;
            LogUtils.e(ConvertUtils.int2HexString(address)+"---"+valueData);
            paraAddressValueHashMap.put(address, valueData);
        }
    }

    public String sendOutData(int address, int data) {
        byte[] sendOut = new byte[10];
        sendOut[0] = 0x3A;
        sendOut[1] = 0x53;
        sendOut[2] = (byte) EncryptionAH(address);
        sendOut[3] = (byte) EncryptionAL(address);
        sendOut[4] = (byte) EncryptionDH(data);
        sendOut[5] = (byte) EncryptionDL(data);
        sendOut[6] = (byte) CRC16H(sendOut, 6);
        sendOut[7] = (byte) CRC16L(sendOut, 6);
        sendOut[8] = 0x0D;
        sendOut[9] = 0x0A;
        return BleDataConvertUtil.byte2hex(sendOut);
    }


    public static int[] dealReceiveDataA(byte[] value) {
        int[] dataA = new int[11];
        dataA[0] = value[3] & 0x0f;//助力档位
        dataA[1] = (value[3] >> 4) & 0x01;//6km推行
        dataA[2] = (value[3] >> 5) & 0x01;//大灯
        dataA[3] = (value[3] >> 6) & 0x01;//刹车状态
        dataA[4] = (value[3] >> 7) & 0x01;//电机运转
//3A2808   05 00  15   000000000086FD0D0A
        int speedH, speedL;
        speedH = value[4] & 0x0f;
        speedL = value[5] & 0xff;
        dataA[5] = (speedH << 8) + speedL;//速度*10

        dataA[6] = value[6] & 0xff;//踏频
        dataA[7] = value[7] & 0xff;//脚踏力矩
        dataA[8] = (value[8] & 0xff) << 8 | (value[9] & 0xff);//错误代码
        dataA[9] = (value[4] >> 4) & 0x01;//仪表屏幕有显示
        dataA[10] = (value[10] >> 1) & 0x01;//电源键状态
        return dataA;
    }

    public static int[] dealReceiveDataB(byte[] value) {
        int[] dataB = new int[9];
        int data1 = value[3] & 0xff;//总里程2
        int data2 = value[4] & 0xff;//总里程1
        int data3 = value[5] & 0xff;//总里程0
        dataB[0] = data3 << 16 | (data2 << 8) | data1;//总里程
        if ((value[6] & 0xff) == 255) {
            dataB[1] = 50;
        } else {
            dataB[1] = (value[6] & 0xff);//控制器温度+50
        }
        if ((value[7] & 0xff) == 255) {
            dataB[2] = 50;
        } else {
            dataB[2] = (value[7] & 0xff);//电机温度+50
        }
        if ((value[8] & 0xff) == 255) {
            dataB[3] = 50;
        } else {
            dataB[3] = (value[8] & 0xff);//电池温度+50
        }
        dataB[4] = ((value[9] & 0xff) << 8) | (value[10] & 0xff);//电池电压*10
        dataB[5] = ((value[11] & 0xff) << 8) | (value[12] & 0xff);//电池电流*10
        dataB[6] = value[13] & 0xff;//电池剩余容量*10
        dataB[7] = value[14] & 0xff;//SOC
        dataB[8] = value[15] & 0xff;//实时功率百分比
        return dataB;//3A290D  000000  3A2808050015000000000086FD0D0A
    }

    /**
     * 获取Address-H
     *
     * @param address
     * @return Address-H
     */
    private int EncryptionAH(int address) {
        return (address >> 8) ^ x1 ^ 0x7D;
    }

    /**
     * 获取Address-L
     *
     * @param address
     * @return Address-L
     */
    private int EncryptionAL(int address) {
        return (address & 0xff) ^ x2 ^ 0x9B;
    }

    /**
     * 获取Data-H
     *
     * @param data
     * @return Data-H
     */
    private int EncryptionDH(int data) {
        return (data >> 8) ^ x3 ^ 0x54;
    }

    /**
     * 获取Data-L
     *
     * @param data
     * @return Data-L
     */
    private int EncryptionDL(int data) {
        return (data & 0xff) ^ x4 ^ 0x3E;
    }

    private static int CRC16(byte[] bytes, int len) {
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
