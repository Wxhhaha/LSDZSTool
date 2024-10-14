package com.lsdzs.lsdzs_tool;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

    /**
     * 读取文本文件
     *
     * @param fileName
     * @return
     */
    public static String readFileStringData(String fileName) {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(new File(fileName));
            byte[] buff = new byte[1024];
            int hasRead = 0;
            StringBuilder sb = new StringBuilder("");
            while ((hasRead = fin.read(buff)) > 0) {
                sb.append(new String(buff, 0, hasRead, "UTF-8"));
            }
            fin.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 读取十六进制文件返回字节字节数组
     *
     * @param fileName
     * @return
     */
    public static byte[] readFileByteData(String fileName) {
        try {
            FileInputStream fin;
            fin = new FileInputStream(new File(fileName));
            byte[] buff = new byte[fin.available()];
            fin.read(buff);
            fin.close();
            return buff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getFileSize(File file) {
        return file.length() / 1024;
    }

    public static String getStringFromUri(Context context,Uri uri){
        String content = null;
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
           content = total.toString();
        }catch (Exception e) {

        }
        return content;
    }

    public static String getFileNameFromUri(Context context,Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DISPLAY_NAME };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
