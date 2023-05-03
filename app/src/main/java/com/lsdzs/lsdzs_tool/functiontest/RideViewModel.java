package com.lsdzs.lsdzs_tool.functiontest;

import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.lsdzs.lsdzs_tool.UserUtil;
import com.lsdzs.lsdzs_tool.api.ApiHolder;
import com.lsdzs.lsdzs_tool.api.entity.CreatePayOrderBean;
import com.lsdzs.lsdzs_tool.api.entity.EmptyBean;
import com.lsdzs.lsdzs_tool.api.entity.Error;
import com.lsdzs.lsdzs_tool.api.entity.NotifyUnLockResponse;
import com.wxh.basiclib.http.HttpManager;
import com.wxh.basiclib.http.IHttpResult;
import com.wxh.basiclib.http.MyResult;
import com.wxh.basiclib.http.ResponseResult;
import com.wxh.basiclib.lifecycle.BaseViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RideViewModel extends BaseViewModel {
    protected MutableLiveData<EmptyBean> btApplyResult = new MutableLiveData<>();
    protected MutableLiveData<EmptyBean> iotUnlockResult = new MutableLiveData<>();
    protected MutableLiveData<EmptyBean> iotLockResult = new MutableLiveData<>();
    protected MutableLiveData<NotifyUnLockResponse> notifyUnlockResult = new MutableLiveData<>();
    protected MutableLiveData<CreatePayOrderBean> notifylockResult = new MutableLiveData<>();
    protected MutableLiveData<EbikeDetailResponse> ebikeDetailResult = new MutableLiveData<>();

    public MutableLiveData<EmptyBean> getIotLockResult() {
        return iotLockResult;
    }

    public MutableLiveData<EbikeDetailResponse> getEbikeDetailResult() {
        return ebikeDetailResult;
    }

    public MutableLiveData<NotifyUnLockResponse> getNotifyUnlockResult() {
        return notifyUnlockResult;
    }

    public MutableLiveData<CreatePayOrderBean> getNotifylockResult() {
        return notifylockResult;
    }

    public MutableLiveData<EmptyBean> getBtApplyResult() {
        return btApplyResult;
    }

    public MutableLiveData<EmptyBean> getIotUnlockResult() {
        return iotUnlockResult;
    }


    public void btUnlockApply(String ebikeId) {
        JSONObject object = new JSONObject();
        try {
            object.put("ebikeId", ebikeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().BtUnlockApply(UserUtil.token, body),
                new IHttpResult<EmptyBean>() {
                    @Override
                    public void success(EmptyBean myResult) {
                        btApplyResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 4g开锁
     *
     * @param ebikeId
     */
    public void iot4GUnlock(String ebikeId) {
        JSONObject object = new JSONObject();
        try {
            object.put("ebikeId", ebikeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().iot4GUnlock(UserUtil.token, body),
                new IHttpResult<EmptyBean>() {
                    @Override
                    public void success(EmptyBean myResult) {
                        iotUnlockResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        error.setValue(new Error(i, s));
                    }

                });
        addDisposable(disposable);
    }

    /**
     * 4g锁车
     *
     * @param ebikeId
     */
    public void ioT4GLock(String ebikeId) {
        JSONObject object = new JSONObject();
        try {
            object.put("ebikeId", ebikeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().iot4Glock(UserUtil.token, body),
                new IHttpResult<EmptyBean>() {
                    @Override
                    public void success(EmptyBean myResult) {
                        iotLockResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        error.setValue(new Error(i, s));
                    }

                });
        addDisposable(disposable);
    }

    /**
     * 通知蓝牙开锁成功
     *
     * @param ebikeId
     */
    public void notifyUnlock(String ebikeId) {
        JSONObject object = new JSONObject();
        try {
            object.put("ebikeId", ebikeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().dealunLockSuccess(UserUtil.token, body),
                new IHttpResult<NotifyUnLockResponse>() {
                    @Override
                    public void success(NotifyUnLockResponse myResult) {
                        notifyUnlockResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 通知后台关锁成功
     *
     * @param ebikeId
     */
    public void notifyLock(String ebikeId) {
        JSONObject object = new JSONObject();
        try {
            object.put("ebikeId", ebikeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().dealLockSuccess(UserUtil.token, body),
                new IHttpResult<CreatePayOrderBean>() {
                    @Override
                    public void success(CreatePayOrderBean myResult) {
                        notifylockResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 根据车辆mac地址获取详情
     *
     * @param mac
     */
    public void findEbikeDetailByMac(String mac) {
        showDialog.setValue(true);
        JSONObject object = new JSONObject();
        try {
            object.put("mac", mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().findEbikeDetailByMac(UserUtil.token, body),
                new IHttpResult<EbikeDetailResponse>() {
                    @Override
                    public void success(EbikeDetailResponse myResult) {
                        showDialog.setValue(false);
                        ebikeDetailResult.setValue(myResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        showDialog.setValue(false);
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }
}
