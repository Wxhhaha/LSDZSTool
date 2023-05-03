package com.lsdzs.lsdzs_tool.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.UserUtil;
import com.lsdzs.lsdzs_tool.api.entity.AccountLoginRequest;
import com.lsdzs.lsdzs_tool.api.entity.Error;
import com.lsdzs.lsdzs_tool.api.entity.LoginResponse;
import com.lsdzs.lsdzs_tool.databinding.ActivityLoginBinding;
import com.lsdzs.lsdzs_tool.socket.JWebSocketClientService;
import com.lsdzs.lsdzs_tool.ui.device.DeviceListActivity;
import com.wxh.basiclib.base.BaseActivity;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class LoginActivity extends BaseActivity<LoginViewModel,ActivityLoginBinding> {

    @Override
    protected int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView(Bundle bundle) {
        final EditText usernameEditText = dataBinding.username;
        final EditText passwordEditText = dataBinding.password;
        final Button loginButton = dataBinding.login;

//        viewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
//            @Override
//            public void onChanged(@Nullable LoginFormState loginFormState) {
//                if (loginFormState == null) {
//                    return;
//                }
//                loginButton.setEnabled(loginFormState.isDataValid());
//                if (loginFormState.getUsernameError() != null) {
//                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
//                }
//                if (loginFormState.getPasswordError() != null) {
//                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
//                }
//            }
//        });

        viewModel.getLoginResult().observe(this, new Observer<LoginResponse>() {
            @Override
            public void onChanged(@Nullable LoginResponse loginResult) {
                if (loginResult == null) {
                    return;
                }
                UserUtil.token = loginResult.getToken();
                UserUtil.isLogin = true;
                SPUtils.getInstance().put("token", UserUtil.token);
                ActivityUtils.startActivity(DeviceListActivity.class);
                finish();
            }
        });


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                  login();
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               login();
            }
        });

    }

    @Override
    protected void initData() {

    }

    private void login(){
        AccountLoginRequest request1 = new AccountLoginRequest();
        request1.setLoginName(dataBinding.username.getText().toString());
        request1.setLoginPwd(dataBinding.password.getText().toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(request1));
        viewModel.login(body, true);
    }

    @Override
    protected LoginViewModel initViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }


    @Override
    protected void showError(Object o) {
        if(o instanceof Error){
            switch (((Error)o).getCode()) {
                case 205:
                    break;
                case 401:
                    ToastUtils.showShort(((Error)o).getMsg());
                    //令牌失效
                    stopService(new Intent(this, JWebSocketClientService.class));
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    ActivityUtils.finishAllActivities();
                    break;
                case 403:
                    ToastUtils.showShort("无权访问");
                    break;
                case 504:
                    ToastUtils.showShort("timeout");
                    break;
                default:
                    ToastUtils.showShort(((Error)o).getMsg());
                    break;
            }
        }
    }
}