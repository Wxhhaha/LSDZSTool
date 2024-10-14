package com.lsdzs.lsdzs_tool.ble;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;
import com.wxh.basiclib.utils.LogUtil;

public class UpdateMessage {
    private static String ENTER_OTA = "2A2D410155EE000D0A";
    private static String RESET_MCU = "2A2D470152F1000D0A";
    private static int DATA_MAX_LEN = 64;
    private static int index;//回复正确数据后再赋值到下一次发送位置

    /**
     * 进入ota升级
     *
     * @return 进入消息
     */
    public static byte[] enterOtaCmd() {
        index = 0;
        return BleDataConvertUtil.HexString2Bytes(ENTER_OTA);
    }

    public static byte[] prepareCommCmd(byte[] data) {
        int len;
        int totalLen = data.length;
        if (totalLen - (index + 1) <= 0) {
            //已经发送完成，无需再发送flash数据，发送总校验和
            return sendVerifySum(data);
        } else if (totalLen - index < DATA_MAX_LEN) {
            len = totalLen - index;
        } else {
            len = DATA_MAX_LEN;
        }
//        LogUtil.e(index + "");
        byte[] cmd = new byte[4 + 3 + len + 4];
        //起始符，固定不变
        cmd[0] = 0x2A;
        cmd[1] = 0x2D;
        //命令类型:升级数据类型
        cmd[2] = 0x43;
        //数据长度-如果是最后一条，则长度不是0x43
        cmd[3] = (byte) (3 + len);

        //起始地址是 0x1000
        int address = index + 0x1000;
        //数据区域
        int addressH = (address >> 8) & 0xff;
        int addressL = address & 0xff;
        //首地址低字节
        cmd[4] = (byte) addressL;
        //首地址高字节
        cmd[5] = (byte) addressH;
        //数据区数据长度
        cmd[6] = (byte) len;

        //todo 数据区
        byte[] sendData = ArrayUtils.subArray(data, index, index + len);
        for (int i = 0; i < sendData.length; i++) {
            cmd[7 + i] = sendData[i];
        }
        int check = checkSum(ArrayUtils.subArray(cmd, 0, 7 + len));
        cmd[7 + len] = (byte) (check & 0xff);
        cmd[8 + len] = (byte) ((check >> 8) & 0xff);
        cmd[9 + len] = 0x0d;
        cmd[10 + len] = 0x0a;
        return cmd;
    }

    private static byte[] sendVerifySum(byte[] data) {
        byte[] cmd = new byte[10];
        //起始符，固定不变
        cmd[0] = 0x2A;
        cmd[1] = 0x2D;
        //命令类型:升级数据类型
        cmd[2] = 0x45;
        //数据长度-总校验和高低字节
        cmd[3] = 2;
        int cs = checkSum(data);
        LogUtils.e("校验和" + cs);
        //总校验和低字节
        cmd[4] = (byte) (cs & 0xff);
        //总校验和高字节
        cmd[5] = (byte) ((cs >> 8) & 0xff);
        int check = checkSum(ArrayUtils.subArray(cmd, 0, 6));
        cmd[6] = (byte) (check & 0xff);
        cmd[7] = (byte) ((check >> 8) & 0xff);
        cmd[8] = 0x0d;
        cmd[9] = 0x0a;
        return cmd;
    }

    public static byte[] dealMCUResponse(byte[] data) {
        if (data.length < 4) {
            return new byte[0];
        }
        try {
            if (data[0] == 0x2A && data[1] == 0x2D) {
                int check = checkSum(ArrayUtils.subArray(data, 0, 4 + data[3]));
                byte l = (byte) (check & 0xff);
                byte h = (byte) ((check >> 8) & 0xff);
                if (l == data[4 + data[3] & 0xff] && h == data[5 + data[3] & 0xff]) {
                    //校验通过
                    byte[] value = new byte[2];
                    switch (data[2]) {
                        case 0x42:
                            value[0] = 0x42;
                            break;
                        case 0x44:
                            value[0] = 0x44;
                            value[1] = data[6];
                            if (data[6] == (byte) 0xA0) {
                                //发送成功，index赋值，总校验和赋值
                                index += DATA_MAX_LEN;
                            }
                            break;
                        case 0x46:
                            value[0] = 0x46;
                            value[1] = data[4];
                            break;
                        case 0x48:
                            value[0] = 0x48;
                            value[1] = data[4];
                            break;
                    }
                    return value;
                } else {
                    return new byte[0];
                }
            }
        } catch (Exception e) {
            return new byte[0];
        }
        return new byte[0];
    }

    public static byte[] resetMCU() {
        return BleDataConvertUtil.HexString2Bytes(RESET_MCU);
    }

    private static int checkSum(byte[] data) {
        int sum = 0;
        for (byte b : data) {
            sum += (b & 0xff);
        }
        return sum;
    }
}
