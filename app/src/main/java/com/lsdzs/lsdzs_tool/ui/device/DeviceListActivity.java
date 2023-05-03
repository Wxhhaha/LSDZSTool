package com.lsdzs.lsdzs_tool.ui.device;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleMtuResponse;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.lsdzs.lsdzs_tool.UserUtil;
import com.lsdzs.lsdzs_tool.functiontest.FunctionTestActivity;
import com.lsdzs.lsdzs_tool.ui.update.IOTUpdateActivity;
import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.BleUtils;
import com.lsdzs.lsdzs_tool.ble.ClientManager;
import com.lsdzs.lsdzs_tool.ble.Device;
import com.lsdzs.lsdzs_tool.databinding.ActivityDeviceListBinding;
import com.lsdzs.lsdzs_tool.ui.login.LoginActivity;
import com.lsdzs.lsdzs_tool.ui.login.LoginViewModel;
import com.lsdzs.lsdzs_tool.ui.update.UpdateActivity;
import com.permissionx.guolindev.PermissionX;
import com.wxh.basiclib.base.BaseActivity;
import com.wxh.basiclib.view.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListActivity extends BaseActivity<LoginViewModel, ActivityDeviceListBinding> {
    private DeviceAdapter usefulAdapter;
    private List<Device> deviceList = new ArrayList<>();
    private HashMap<String, Device> deviceMap = new HashMap<>();
    private BluetoothClient bluetoothClient;

    @Override
    protected int getLayout() {
        return R.layout.activity_device_list;
    }

    @Override
    protected void initView(Bundle bundle) {
        dataBinding.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.refresh:
                        searchBluetooth();
                        return true;
                }
                return false;
            }
        });
        dataBinding.lvDevice.setPullRefreshEnabled(true);
        dataBinding.lvDevice.setLoadingMoreEnabled(false);
        dataBinding.lvDevice.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                searchBluetooth();
            }

            @Override
            public void onLoadMore() {

            }
        });
    }

    @Override
    protected void initData() {
        if (!UserUtil.isLogin) {
            String token = SPUtils.getInstance().getString("token");
            if (StringUtils.isEmpty(token)) {
                ActivityUtils.startActivity(LoginActivity.class);
                finish();
            } else {
                try {
                    viewModel.tokenLogin(token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        bluetoothClient = ClientManager.getClient(getApplicationContext());
        usefulAdapter = new DeviceAdapter(this);
        usefulAdapter.setOnItemClickeListener(device -> {
            showProgress();
            connect(device);
        });
        dataBinding.lvDevice.setAdapter(usefulAdapter);
        requestPermissions();
    }

    private void requestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }


        PermissionX.init(this)
                .permissions(permissions)
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList, getString(R.string.permission_des), getString(R.string.ok), getString(R.string.cancel));
                })
//                .onForwardToSettings((scope, deniedList) ->
//                        scope.showForwardToSettingsDialog(deniedList, "请前往设置页面进行授权", "去授权", "拒绝")
//                )
                .request((allGranted, grantedList, deniedList) -> {
                    searchBluetooth();
                });
    }

    private void searchBluetooth() {
        if (!bluetoothClient.isBluetoothOpened()) {
            bluetoothClient.openBluetooth();
        } else {
            SearchRequest request = new SearchRequest.Builder().searchBluetoothLeDevice(3 * 1000, 1).build();
            bluetoothClient.search(request, mSearchResponse);
        }
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            deviceList.clear();
            usefulAdapter.getItems().clear();
            deviceMap.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            if (!StringUtils.isEmpty(device.getName()) && !device.getName().equals("NULL")) {
                if (!deviceMap.containsKey(device.getName())) {
                    //未加入列表中，新建加入
                    Device device1 = new Device();
                    device1.setName(device.getName());
                    device1.setMac(device.getAddress());
                    device1.setRssi(device.rssi);
                    deviceList.add(device1);
                    deviceMap.put(device.getName(), device1);
                    usefulAdapter.getItems().add(device1);
                } else {
                    Device device1 = deviceMap.get(device.getName());
                    device1.setMac(device1.getMac());
                    device1.setRssi(device.rssi);
                }

                usefulAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSearchStopped() {

        }

        @Override
        public void onSearchCanceled() {

        }
    };

    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onResume() {
        super.onResume();
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//        searchBluetooth();
//            }
//        };
//
//        timer.schedule(timerTask, 0, 5000);
    }

    public void showProgress() {
        showDialog("Loading");
    }

    public void stopProgress() {
        dismissDialog();
    }

    @Override
    protected void onPause() {
        stopProgress();
        super.onPause();
    }

    private void connect(Device device) {
        BleUtils.connect(bluetoothClient, device, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {
                switch (status) {
                    case STATUS_CONNECTED:
//                        ToastUtils.showShort(R.string.ble_connected);
                        break;
                    case STATUS_DISCONNECTED:
//                        ToastUtils.showShort(R.string.ble_disconnected);
                        break;
                    default:
                        break;
                }
            }
        }, new BleUtils.ConnectResult() {
            @Override
            public void success() {
                stopProgress();
                //todo 跳转到测试页
                bluetoothClient.requestMtu(ClientManager.getDevice().getMac(), 200, new BleMtuResponse() {
                    @Override
                    public void onResponse(int i, Integer integer) {
                        String[] items = {"性能测试", "固件升级"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int p) {
                                switch (p) {
                                    case 0:
                                        ActivityUtils.startActivity(FunctionTestActivity.class);
                                        break;
                                    case 1:
                                        ActivityUtils.startActivity(UpdateActivity.class);
                                        break;

                                }
                            }
                        });
                        builder.create().show();
                    }
                });

            }

            @Override
            public void fail() {
                ToastUtils.showShort(R.string.ble_failed);
            }
        });
    }

    @Override
    protected LoginViewModel initViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    protected void showError(Object o) {

    }

}