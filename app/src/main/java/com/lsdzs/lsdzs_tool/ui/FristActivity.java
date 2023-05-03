package com.lsdzs.lsdzs_tool.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.databinding.ActivityFristBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;

public class FristActivity extends BaseNoModelActivity<ActivityFristBinding> {


    @Override
    protected int getLayout() {
        return R.layout.activity_frist;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.btFunction.setOnClickListener(view -> {
            //
        });
    }

    @Override
    protected void initData() {

    }
}