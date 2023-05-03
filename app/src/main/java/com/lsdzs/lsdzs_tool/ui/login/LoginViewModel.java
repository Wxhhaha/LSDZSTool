package com.lsdzs.lsdzs_tool.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.api.ApiHolder;
import com.lsdzs.lsdzs_tool.api.entity.Error;
import com.lsdzs.lsdzs_tool.api.entity.LoginResponse;
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

public class LoginViewModel extends BaseViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public void login(RequestBody body, boolean showLoading) {
        showDialog.setValue(showLoading, "Loading...");

        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().login(body),
                new IHttpResult<LoginResponse>() {
                    @Override
                    public void success(LoginResponse myResultResponseResult) {
                        showDialog.setValue(false);
                        loginResult.setValue(myResultResponseResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        showDialog.setValue(false);
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }

    public void tokenLogin(String token) {
        showDialog.setValue(false, "Loading...");
        JSONObject object = new JSONObject();
        try {
            object.put("token", token);
        } catch (JSONException e) {
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
        Disposable disposable = HttpManager.toSubscribe(ApiHolder.getApiInstance().tokenLogin(body),
                new IHttpResult<LoginResponse>() {
                    @Override
                    public void success(LoginResponse myResultResponseResult) {
                        showDialog.setValue(false);
                        loginResult.setValue(myResultResponseResult);
                    }

                    @Override
                    public void error(int i, String s) {
                        showDialog.setValue(false);
                        error.setValue(new Error(i, s));
                    }
                });
        addDisposable(disposable);
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        return !username.trim().isEmpty();

    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}