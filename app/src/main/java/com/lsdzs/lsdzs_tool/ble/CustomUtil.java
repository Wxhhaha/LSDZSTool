package com.lsdzs.lsdzs_tool.ble;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.wxh.basiclib.utils.LogUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

public class CustomUtil {
    /**
     * 判断字符串是否不为空
     *
     * @param string
     * @return
     */
    public static boolean isNotBlank(String string) {
        if (string != null && !string.equals("null") && string.length() > 0) {
            return true;
        }
        return false;
    }

    public static byte[] HexString2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    public static final String byte2hex(byte b[]) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    public static final String byteToString(int b) {
        String stmp = Integer.toHexString(b);
        if (stmp.length() % 2 != 0) {
            return "0" + stmp;
        } else {
            return stmp;
        }
    }

    public static int bytesToInt(byte[] src) {
        return ((src[0]) << 8) | (src[1] & 0xff);
    }

    public static final String byteToString(byte b) {
        String stmp = Integer.toHexString(b & 0xff);
        if (stmp.length() == 1) {
            return "0" + stmp;
        } else {
            return stmp;
        }
    }

    public static final byte stringToByte(String string) {
        char c0 = string.charAt(0);
        char c1 = string.charAt(1);
        return (byte) ((parse(c0) << 4) | parse(c1));
    }

    public static final int[] hexByte2byte(byte b[]) {
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
        if (c >= 'a') return (c - 'a' + 10) & 0x0f;
        if (c >= 'A') return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static String bytetoString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    public static void logArray(int[] array) {
        StringBuilder sbBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sbBuilder.append(array[i]).append(";");
        }
        LogUtil.d("data", sbBuilder.toString());
    }

    public static void logArray(byte[] array) {
        StringBuilder sbBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sbBuilder.append(array[i]).append(";");
        }
        LogUtil.d("data", sbBuilder.toString());
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static int getDisPlayWidth(Context activity) {
        WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 字符串转成map
     *
     * @param array
     * @return
     */
    public static LinkedHashMap<Integer, Integer> array2Map(int[] array) {
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                map.put(i + 1, array[i]);
            }
        }
        return map;
    }

    /**
     * 设置屏幕亮度
     *
     * @param num
     */
    public static void setScreenBrightness(Activity activity, float num) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = num;// 设置屏幕的亮度
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * 保留两位小数
     *
     * @param num
     * @return
     */
    public static float formatTwoDouble(double num) {
        BigDecimal bg = new BigDecimal(num);
        BigDecimal result = bg.setScale(2, BigDecimal.ROUND_HALF_UP);
        return result.floatValue();
    }

    /**
     * 计时
     *
     * @param time
     * @return
     */
    public static String showTimeCount(long time) {
        if (time >= 360000000) {
            return "00:00:00";
        }
        String timeCount = "";
        long hourc = time / 3600000;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length() - 2, hour.length());

        long minuec = (time - hourc * 3600000) / (60000);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length() - 2, minue.length());

        long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
        String sec = "0" + secc;
        sec = sec.substring(sec.length() - 2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

    /**
     * 从计时器中获取秒数
     *
     * @param s
     * @return
     */
    public static int getSeconds(String s) {
        String[] split = s.split(":");
        String string2 = split[0];
        int hour = Integer.parseInt(string2);
        int Hours = hour * 3600;
        String string3 = split[1];
        int min = Integer.parseInt(string3);
        int Mins = min * 60;
        int SS = Integer.parseInt(split[2]);
        return Hours + Mins + SS;
    }

    /**
     * 公里转英里
     *
     * @param kmValue
     * @return
     */
    public static float km2mi(double kmValue) {
        return formatTwoDouble(kmValue * 0.6213712);
    }

    /**
     * 英里转公里
     *
     * @param miValue
     * @return
     */
    public static float mi2km(double miValue) {
        return formatTwoDouble(miValue * 1.609344);
    }

    /**
     * 计算listview高度并展示
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);

            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * dip 转换成px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float sDensity = context.getResources().getDisplayMetrics().density;
        final float scale = sDensity;
        return (int) (dipValue * scale + 0.5f);
    }



    /**
     * bitmap 转file
     *
     * @param bitmap
     */
    public static void saveBitmapFile(Context context, Bitmap bitmap, String name) {
        File file = new File(context.getCacheDir().getPath() + name);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public static String genSecret(String mac) {
        return mac.replace(":","");
    }
}
