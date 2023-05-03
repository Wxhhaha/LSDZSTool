package com.lsdzs.lsdzs_tool.ui.device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.Device;
import com.lsdzs.lsdzs_tool.databinding.DeviceItemBinding;
import com.wxh.basiclib.adapter.BaseBindingAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/8/2.
 */

public class DeviceAdapter extends BaseBindingAdapter<Device, DeviceItemBinding> {
private OnItemClickeListener onItemClickeListener;

    public OnItemClickeListener getOnItemClickeListener() {
        return onItemClickeListener;
    }

    public void setOnItemClickeListener(OnItemClickeListener onItemClickeListener) {
        this.onItemClickeListener = onItemClickeListener;
    }

    public DeviceAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId(int i) {
        return R.layout.device_item;
    }

    @Override
    protected void onBindItem(DeviceItemBinding deviceItemBinding, Device device,int position) {
        deviceItemBinding.tvName.setText(device.getName());
        deviceItemBinding.tvMac.setText(device.getMac());
        deviceItemBinding.tvRssi.setText(device.getRssi()+" dbm");
        deviceItemBinding.container.setOnClickListener(view -> {
            onItemClickeListener.onItemClick(device);
        });
    }

    public interface OnItemClickeListener{
        void onItemClick(Device device);
    }
}
