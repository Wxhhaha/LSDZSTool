package com.lsdzs.lsdzs_tool.ui.settings;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.MapUtils;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.CustomUtil;
import com.lsdzs.lsdzs_tool.ble.DialogUtil;
import com.lsdzs.lsdzs_tool.ble.ParamModel;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSCmd;
import com.lsdzs.lsdzs_tool.ble.canbus.CANBUSResponse;
import com.lsdzs.lsdzs_tool.databinding.FragmentSystemBinding;
import com.lsdzs.lsdzs_tool.ui.BaseFragment;
import com.wxh.basiclib.view.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SystemFragment extends BaseFragment<FragmentSystemBinding> {

    private static final String TYPE_FLAG = "TYPE_FLAG";
    private int type;
    private ParamValueAdapter adapter;
    private List<ParamModel> paramModels;
    private Map<Integer, ParamModel> paramModelMap;
    String[] titleName = null;
    int[] pgnList = null;
    int[] valueMin = null;
    int[] valueMax = null;
    int flag = 0;

    public SystemFragment() {
    }

    public static SystemFragment newInstance(int param1) {
        SystemFragment fragment = new SystemFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE_FLAG, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE_FLAG);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_system;
    }

    @Override
    protected void initView() {
        adapter = new ParamValueAdapter(mContext);
        dataBinding.rv.setAdapter(adapter);
        Drawable dividerDrawable = ContextCompat.getDrawable(mContext, R.drawable.divider);
        dataBinding.rv.addItemDecoration(dataBinding.rv.new DividerItemDecoration(dividerDrawable));
        dataBinding.rv.setLoadingMoreEnabled(false);
        dataBinding.rv.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                getSettingData();
            }

            @Override
            public void onLoadMore() {

            }
        });
        switch (type) {
            case 0:
                titleName = getResources().getStringArray(R.array.system);
                pgnList = getResources().getIntArray(R.array.system_pgn);
                valueMin = null;
                valueMax = null;
                break;
            case 1:
                titleName = getResources().getStringArray(R.array.custom);
                pgnList = getResources().getIntArray(R.array.custom_pgn);
                valueMin = getResources().getIntArray(R.array.custom_min);
                valueMax = getResources().getIntArray(R.array.custom_max);
                break;
            case 2:
                titleName = getResources().getStringArray(R.array.battery);
                pgnList = getResources().getIntArray(R.array.battery_pgn);
                valueMin = getResources().getIntArray(R.array.battery_min);
                valueMax = getResources().getIntArray(R.array.battery_max);
                break;
            case 3:
                titleName = getResources().getStringArray(R.array.motor);
                pgnList = getResources().getIntArray(R.array.motor_pgn);
                valueMin = getResources().getIntArray(R.array.motor_min);
                valueMax = getResources().getIntArray(R.array.motor_max);
                break;
            case 4:
                titleName = getResources().getStringArray(R.array.throttle);
                pgnList = getResources().getIntArray(R.array.throttle_pgn);
                valueMin = getResources().getIntArray(R.array.throttle_min);
                valueMax = getResources().getIntArray(R.array.throttle_max);
                break;
            case 5:
                titleName = getResources().getStringArray(R.array.sensor);
                pgnList = getResources().getIntArray(R.array.sensor_pgn);
                valueMin = getResources().getIntArray(R.array.sensor_min);
                valueMax = getResources().getIntArray(R.array.sensor_max);
                break;
            case 6:
                titleName = getResources().getStringArray(R.array.speed);
                pgnList = getResources().getIntArray(R.array.speed_pgn);
                valueMin = getResources().getIntArray(R.array.speed_min);
                valueMax = getResources().getIntArray(R.array.speed_max);
                break;
            case 7:
                titleName = getResources().getStringArray(R.array.display);
                pgnList = getResources().getIntArray(R.array.display_pgn);
                valueMin = getResources().getIntArray(R.array.display_min);
                valueMax = getResources().getIntArray(R.array.display_max);
                break;
        }
        if (titleName != null) {
            paramModels = new ArrayList<>();
            paramModelMap = new HashMap<>();
            for (int i = 0; i < titleName.length; i++) {
                ParamModel model = new ParamModel(titleName[i], pgnList[i]);
                if(valueMin!=null){
                    model.setMin(valueMin[i]);
                    model.setMax(valueMax[i]);
                }
                paramModels.add(model);
                paramModelMap.put(pgnList[i], model);
            }
            adapter.getItems().addAll(paramModels);
            adapter.notifyDataSetChanged();
        }

        adapter.setItemClick(model -> {
            //弹出修改
            DialogUtil.showNumberPickerDialog(false, mContext, model.getName(),
                    model.getMin(), model.getMax(), model.getValue()
                    , new DialogUtil.NumberPickerConfirm() {
                        @Override
                        public void doConfirm(Dialog dialog, String value) {
                            model.setValue(Integer.parseInt(value));
                            writeTCMessage(CANBUSCmd.writeControllerData(0x8702,Integer.parseInt(value)));
                            adapter.notifyDataSetChanged();
                        }
                    });
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CANBUSCmd.setKeys(0, 0, 0, 0);
        BleUtils.notifyBle(ClientManager.getClient(mContext), new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                if (bytes.length < 5) {
                    return;
                }
                LogUtils.e("设置返回" + CustomUtil.byte2hex(bytes));
                int[] result = CANBUSResponse.dealResponse(bytes);
                if (result.length < 1) {
                    return;
                }
                if (result[0] == 65029) {
                    int address = result[1];
                    int data = result[2];
                    if (paramModelMap != null) {
                        if (paramModelMap.get(address) != null) {
                            paramModelMap.get(address).setValue(data);
                            adapter.notifyDataSetChanged();
                            LogUtils.e(paramModelMap.get(address).getName() + "-" + data);
                        }
                    }
                }
            }

            @Override
            public void onResponse(int i) {
                dataBinding.rv.refresh();
            }
        });
    }

    private void getSettingData() {
        flag = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                writeTCMessage(CANBUSCmd.readControllerData(pgnList[flag]));
                if (flag < pgnList.length - 1) {
                    flag++;
                    handler.postDelayed(this, 100);
                } else {
                    flag = 0;
                    dataBinding.rv.refreshComplete();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDialog();
//                            dealResult();
//                        }
//                    }, 1000);
                }

            }
        }, 10);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            return false;
        }
    });

    /**
     * 发送透传数据
     *
     * @param value
     */
    private void writeTCMessage(byte[] value) {
        byte[] data = ArrayUtils.add(value, 0, (byte) 0x33);
        BleUtils.writeBle(ClientManager.getClient(mContext), data, new BleWriteResponse() {
            @Override
            public void onResponse(int i) {
                if (i == -1) {
                    BleUtils.writeBle(ClientManager.getClient(mContext), data, this);
                }
            }
        });
    }

}