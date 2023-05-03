package com.lsdzs.lsdzs_tool.ble;

import com.blankj.utilcode.util.ConvertUtils;

public class BleDataConvertUtil {
    public static final String byte2hex(byte[] b) {
        return ConvertUtils.bytes2HexString(b);
    }

    public static String bytetoString(byte[] bytearray) {
        return ConvertUtils.bytes2String(bytearray);
    }

    public static byte[] HexString2Bytes(String hexstr) {
        return ConvertUtils.hexString2Bytes(hexstr);
    }

    public static int bytesToInt(byte[] src) {
        return ((src[0]) << 8) | (src[1] & 0xff);
    }

    public static int[] hexByte2byte(byte b[]) {
        int[] a = new int[b.length];
        String[] string = new String[b.length];
        for (int n = 0; n < b.length; n++) {
            String stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                stmp = "0" + stmp;
            }
            string[n] = stmp;
        }
        for (int i = 0; i < string.length; i++) {
            a[i] = Integer.valueOf(string[i], 16);
        }
        return a;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static byte getHighBit(int value) {
        return (byte) (value >> 8);
    }

    public static byte getLowBit(int value) {
        return (byte) (value & 0xff);
    }

    public static int getDataByHighLow(byte high, byte low) {
        return high << 8 | low;
    }
}
