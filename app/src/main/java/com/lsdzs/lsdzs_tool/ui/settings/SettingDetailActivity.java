package com.lsdzs.lsdzs_tool.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSCmd;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSResponse;
import com.lsdzs.lsdzs_tool.databinding.ActivitySettingDetailBinding;
import com.wxh.basiclib.base.BaseNoModelActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SettingDetailActivity extends BaseNoModelActivity<ActivitySettingDetailBinding> {
    String[] options;
    @Override
    protected int getLayout() {
        return R.layout.activity_setting_detail;
    }

    @Override
    protected void initView(Bundle bundle) {
        options = getResources().getStringArray(R.array.setting_list);
        MainMenuFragmentAdapter adapter = new MainMenuFragmentAdapter(this);
        dataBinding.viewpager.setAdapter(adapter);

        new TabLayoutMediator(dataBinding.tablayout, dataBinding.viewpager,
                (tab, position) -> tab.setText(options[position])
        ).attach();
        dataBinding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                dataBinding.viewpager.setCurrentItem(tab.getPosition(),false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        dataBinding.viewpager.setCurrentItem(getIntent().getIntExtra("select",0),false);
    }

    @Override
    protected void initData() {

    }

    public static class MainMenuFragmentAdapter extends FragmentStateAdapter {

        public MainMenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return SystemFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 8;
        }
    }

    /**
     * 发送透传数据
     *
     * @param value
     */
    private void writeTCMessage(byte[] value) {
        byte[] data = ArrayUtils.add(value, 0, (byte) 0x33);
        BleUtils.writeBle(ClientManager.getClient(this), data, new BleWriteResponse() {
            @Override
            public void onResponse(int i) {
                if (i == -1) {
                    BleUtils.writeBle(ClientManager.getClient(SettingDetailActivity.this), data, this);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        writeTCMessage(CANBUSCmd.existControllerSet(0x8006));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                writeTCMessage(CANBUSCmd.resetController());
            }
        },100);
        super.onDestroy();
    }
}