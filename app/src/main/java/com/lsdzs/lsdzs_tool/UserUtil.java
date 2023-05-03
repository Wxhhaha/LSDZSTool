package com.lsdzs.lsdzs_tool;

import com.blankj.utilcode.util.SPUtils;

public class UserUtil {
    public static String token;
    public static boolean isLogin;
    public static String cycleId;
    public static String ebikeId;

    public static void cleanUserData(){
        token = null;
        isLogin = false;
        cycleId = null;
        ebikeId = null;
        SPUtils.getInstance().put("token","");
    }

    public static void cleanBikeData(){
        ebikeId = null;
    }
}
