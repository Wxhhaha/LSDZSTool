package com.lsdzs.lsdzs_tool.api;

import com.wxh.basiclib.http.HttpManager;

public class ApiHolder {
    private static ApiService apiService;

    public static ApiService getApiInstance() {
        if (apiService == null) {
            apiService = HttpManager.service(ApiService.class);
        }
        return apiService;
    }
}
