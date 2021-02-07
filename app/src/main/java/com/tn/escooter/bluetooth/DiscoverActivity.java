package com.tn.escooter.bluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tn.escooter.MainActivity;
import com.tn.escooter.R;
import com.tn.escooter.buletooth.LeDevice;
import com.tn.escooter.buletooth.OnBluetoothListener;
import com.tn.escooter.buletooth.QBlueToothManager;
import com.tn.escooter.buletooth.TwoWheelActivity;
import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.utils.utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tn.escooter.utils.utils.CAR_TYPE_HUABAN;
import static com.tn.escooter.utils.utils.CAR_TYPE_NIUNIU;
import static com.tn.escooter.utils.utils.bluetooth_type.LENZOD;
import static com.tn.escooter.utils.utils.bluetooth_type.MACWHEEL;

public class DiscoverActivity extends Activity implements OnItemClickListener, OnClickListener {
    private static final boolean D = true;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "DiscoverActivity";
    private BleDevicesAdapter adapter;
    /* access modifiers changed from: private */
    public LFBluetootService bleService = null;
    private BluetoothAdapter bluetoothAdapter;
    /* access modifiers changed from: private */
    public int count_time = 0;
    /* access modifiers changed from: private */
    public boolean isShow = false;
    /* access modifiers changed from: private */
    public boolean is_run = D;
    public int car_type = 0;
    private boolean is_search_device = false;
    @BindView(R.id.listView)
    ListView listView;

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    @SuppressLint({"NewApi"})
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LFBluetootService.ACTION_GATT_CONNECTED.equals(action)) {
            } else if (LFBluetootService.ACTION_BLE_CODE_OK.equals(action)) {
                MainActivity.getInstance().is_click_blue_connect = false;
                finish();
            } else if (LFBluetootService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (LFBluetootService.ACTION_DATA_AVAILABLE.equals(action)) {
                BytesUtils.BytesToString(intent.getByteArrayExtra(LFBluetootService.EXTRA_DATA));
            } else if (LFBluetootService.ACTION_BLE_REQUEST_PASSWORD.equals(action)) {
                requestPassword();
            } else if (LFBluetootService.ACTION_BLE_REQUEST_PASSWORD_AGAIN.equals(action)) {
                utils.SuperActivityToast(DiscoverActivity.this, "Bluetooth connect error.", utils.toast.TOAST_ERROR);
                requestPassword();
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                QBlueToothManager.getInstance().write(BytesUtils.StringToBytes("FF55010055"));
                mHandler.sendEmptyMessageDelayed(1, 2000);
            }
        }
    };

    public final OnBluetoothListener bluetoothListener = new OnBluetoothListener() {
        public void onBlueToothDisconneted() {
        }

        public void onScanRelult(List<LeDevice> list) {
            if (bluetoothAdapter != null) {
                LFBluetootService.getInstent().myBluetoothAdapter.clear();
                for (LeDevice leDevice : list) {
                    if (!(leDevice == null || leDevice.getDevice() == null || TextUtils.isEmpty(leDevice.getDevice().getName()))) {
                        LFBluetootService.getInstent().myBluetoothAdapter.addDevice(leDevice, LENZOD);
                    }
                }
            }
        }

        public void onScanFail(int i) {
            utils.SuperActivityToast(DiscoverActivity.this, "scan failed", utils.toast.TOAST_INFO);
        }

        public void onScanComplete() {
            utils.SuperActivityToast(DiscoverActivity.this, "scan complete", utils.toast.TOAST_INFO);
        }

        public void onBlueToothConneted() {
            utils.SuperActivityToast(DiscoverActivity.this, "bluetooth connected", utils.toast.TOAST_INFO);
            mHandler.sendEmptyMessage(1);
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {

            String bytesToHexString = BytesUtils.bytesToHexString(bluetoothGattCharacteristic.getValue());
            StringBuilder sb = new StringBuilder();
            sb.append("receive data：");
            sb.append(bytesToHexString);
            utils.SuperActivityToast(DiscoverActivity.this, sb.toString(), utils.toast.TOAST_INFO);
            if (!TextUtils.isEmpty(bytesToHexString)) {
                if (CAR_TYPE_NIUNIU.equals(bytesToHexString)) {
                    car_type = 1;
                        Intent intent = new Intent(DiscoverActivity.this, MainActivity.class);
                        intent.putExtra("BTDevice", bluetoothGatt.getDevice());
                        startActivity(intent);
                        finish();
                } else if (CAR_TYPE_HUABAN.equals(bytesToHexString)) {
                    car_type = 2;
                    TwoWheelActivity.start(DiscoverActivity.this, bluetoothGatt.getDevice(), null);
                    finish();
                }
            }
        }

        public void connectFail() {
            utils.SuperActivityToast(DiscoverActivity.this, "connect failed", utils.toast.TOAST_INFO);
        }
    };

    private Animation operatingAnim;
    @BindView(R.id.search_result_image)
    ImageView search_result_image;
    @BindView(R.id.searching_again_btn)
    Button searching_again_btn;
    private Thread thread = new Thread(new Runnable() {
        public void run() {
            while (is_run) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                count_time =
                if (count_time + 1 < 10) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            search_ui();
                        }
                    });
                }
            }
        }
    });

    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind((Activity) this);
        initGPS();
        initView();
        initEvent();
        init();
        registerBroadCast();
        m_page_title.setText(R.string.search_device);
        thread.start();
    }

    private void initEvent() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleService = LFBluetootService.getInstent();
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.bluetooth_jiazai);
        operatingAnim.setInterpolator(new LinearInterpolator());
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 18:
                if (grantResults[0] == 0) {
                }
                return;
            default:
                return;
        }
    }

    private void initGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled("gps");
        boolean isProviderEnabled = locationManager.isProviderEnabled("network");
        if (!gps) {
            Builder dialog = new Builder(this);
            dialog.setMessage("Open gps");
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    startActivityForResult(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"), 0);
                }
            });
            dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            dialog.show();
        }
    }

    @TargetApi(18)
    private void initView() {
        listView.setOnItemClickListener(this);
    }

    @SuppressLint({"NewApi"})
    private void init() {
        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }
        if (adapter == null) {
            adapter = bleService.getBleDevicesAdapter();
            adapter.setContext(this);
            adapter.clearButDevices(bleService.bluetoothManager.getConnectedDevices(7));
            listView.setAdapter(adapter);
        }
    }

    private void registerBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_START_SCAN);
        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LFBluetootService.ACTION_BLE_REQUEST_PASSWORD);
        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
        filter.addAction(LFBluetootService.ACTION_BLE_REQUEST_PASSWORD_AGAIN);
        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);



//        IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.FOUND");
//        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
//        intentFilter.addAction("android.bluetooth.adapter.action.SCAN_MODE_CHANGED");
//        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
//        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
//        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        registerReceiver(mReceiver, filter);


        QBlueToothManager.getInstance().addBluetoothListener(this.bluetoothListener);
        QBlueToothManager.getInstance().unbindDevices();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                QBlueToothManager.getInstance().scanLeDevice(true);
            }
        }, 600);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (bluetoothAdapter.isEnabled()) {
            if (bleService.isScanning()) {
                bleService.stopScan();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        bleService.startScan();
                    }
                }, 1000);
            } else {
                bleService.startScan();
            }
            search_result_image.startAnimation(operatingAnim);
        }
        registerBroadCast();
    }

    /* access modifiers changed from: private */
    public void search_ui() {
        if (adapter.getCount() > 0) {
            is_search_device = D;
            count_time = 10;
            bleService.stopScan();
            search_result_image.clearAnimation();
            search_result_image.setImageResource(R.drawable.serach_pause_icon);
            listView.setVisibility(View.VISIBLE);
            searching_again_btn.setVisibility(View.INVISIBLE);
        } else if (count_time == 10) {
            is_search_device = false;
            bleService.stopScan();
            search_result_image.clearAnimation();
            search_result_image.setImageResource(R.drawable.serach_fail_icon);
            searching_again_btn.setVisibility(View.VISIBLE);
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.lay_back, R.id.search_result_image, R.id.searching_again_btn, R.id.lay_home})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lay_back /*2131689731*/:
                finish();
                return;
            case R.id.search_result_image /*2131689785*/:
                if (!is_search_device) {
                    return;
                }
                break;
            case R.id.searching_again_btn /*2131689786*/:
                break;
            case R.id.lay_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            default:
                return;
        }
        if (bluetoothAdapter.isEnabled()) {
            searching_again_btn.setVisibility(View.INVISIBLE);
            search_result_image.setImageResource(R.drawable.serach_ing_icon);
            bleService.startScan();
            count_time = 0;
            if (operatingAnim != null) {
                search_result_image.startAnimation(operatingAnim);
                return;
            }
            return;
        }
        utils.SuperActivityToast(this, getString(R.string.turn_on_bluetootn), utils.toast.TOAST_WARNING);
    }

    public void requestPassword() {
        if (!isShow) {
            isShow = D;
            final Dialog dialog = new Dialog(this, R.style.myStyle);
            dialog.setContentView(R.layout.pwd_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            final EditText pwd_num = (EditText) dialog.findViewById(R.id.age_num);
            pwd_num.setFilters(new InputFilter[]{new LengthFilter(6)});
            dialog.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    dialog.dismiss();
                    isShow = false;
                }
            });
            dialog.findViewById(R.id.confirm).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    String input = pwd_num.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        utils.SuperActivityToast(DiscoverActivity.this, "password can not be empty.", utils.toast.TOAST_ERROR);
                        return;
                    }
                    LFBluetootService.getInstent().password = input;
                    LFBluetootService.getInstent().sendString("CODE=" + input);
                    dialog.dismiss();
                    isShow = false;
                }
            });
        }
    }

//    private void showBlueMusicSetting(Context context) {
//        if (!isShow) {
//            isShow = D;
//            final Dialog dialog = new Dialog(this, R.style.myStyle);
//            dialog.setContentView(R.layout.blue_music_dialog);
//            dialog.findViewById(R.id.setting_cancel).setOnClickListener(new OnClickListener() {
//                public void onClick(View arg0) {
//                    isShow = false;
//                    dialog.dismiss();
//                    startActivity(new Intent(DiscoverActivity.this, NewMainActivity.class));
//                    finish();
//                }
//            });
//            dialog.findViewById(R.id.setting_btn).setOnClickListener(new OnClickListener() {
//                public void onClick(View arg0) {
//                    isShow = false;
//                    dialog.dismiss();
//                    startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));
//                }
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//        }
//    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        BluetoothDevice device = adapter.getDevice(position);
        if (device == null) {
            adapter.removeDevice(position);
            adapter.notifyDataSetChanged();
            return;
        }
        MainActivity.getInstance().is_click_blue_connect = D;
        String device_address = device.getAddress();
        String device_type = adapter.getDeviceType(device_address);
        if(device_type == MACWHEEL){
            if (!device_address.equals(bleService.getConnectedAddress())) {
                bleService.connect(device.getAddress());
                return;
            }
            bleService.connect(device.getAddress());
        }else if(device_type == LENZOD){
            QBlueToothManager.getInstance().connect(device);
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        is_run = false;
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception err){

        }
    }
}
