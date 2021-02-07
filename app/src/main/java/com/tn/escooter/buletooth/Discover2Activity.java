package com.tn.escooter.buletooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.tn.escooter.MainActivity;
import com.tn.escooter.MyApplication;
import com.tn.escooter.R;
import com.tn.escooter.bluetooth.BleDevicesAdapter;
import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.tn.escooter.utils.utils.CAR_TYPE_HUABAN;
import static com.tn.escooter.utils.utils.CAR_TYPE_NIUNIU;
import static com.tn.escooter.utils.utils.bluetooth_type.LENZOD;

public class Discover2Activity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private OnBluetoothListener bluetoothListener;
    boolean isExit = false;
    public boolean isStart;
    public BleDevicesAdapter mAdapter = new BleDevicesAdapter();
    public Handler mHandler;
    @BindView(R.id.search_result_image)
    ImageView search_result_image;
    @BindView(R.id.searching_again_btn)
    Button searching_again_btn;
    @BindView(R.id.lay_toolbar)
    LinearLayout lay_toolbar;
    ProgressDialog progDailog = null;

    @BindView(R.id.listView)
    ListView listView;
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind((Activity) this);
        getWindow().addFlags(128);
        init();
        initData();
    }
    /* access modifiers changed from: protected */
    public void initData() {
        this.bluetoothListener = new OnBluetoothListener() {
            public void onBlueToothDisconneted() {
                searching_again_btn.setVisibility(View.VISIBLE);
            }
            public void onScanRelult(List<LeDevice> list) {
                mAdapter.clear();
                for (LeDevice leDevice : list) {
                    if (!(leDevice == null || leDevice.getDevice() == null || TextUtils.isEmpty(leDevice.getDevice().getName()))) {
                        mAdapter.addDevice(leDevice, LENZOD);
                    }
                }
            }

            public void onScanFail(int i) {
                searching_again_btn.setVisibility(View.VISIBLE);
                search_result_image.clearAnimation();
            }

            public void onScanComplete() {
                searching_again_btn.setVisibility(View.VISIBLE);
                search_result_image.clearAnimation();
            }

            public void onBlueToothConneted() {
                mHandler.sendEmptyMessage(1);
            }

            public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                String bytesToHexString = BytesUtils.bytesToHexString(bluetoothGattCharacteristic.getValue());
                StringBuilder sb = new StringBuilder();
                sb.append("receive data：");
                sb.append(bytesToHexString);
                if (!TextUtils.isEmpty(bytesToHexString)) {
                    if (CAR_TYPE_NIUNIU.equals(bytesToHexString)) {
                        if(progDailog != null) progDailog.dismiss();
                        MyApplication.TYPE = 1;
                        if (!isStart) {
                            isStart = true;
                            Intent intent = new Intent(Discover2Activity.this, MainActivity.class);
                            intent.putExtra("BTDevice", bluetoothGatt.getDevice());
                            startActivity(intent);
                            finish();
                        }
                    } else if (CAR_TYPE_HUABAN.equals(bytesToHexString)) {
                        if(progDailog != null) progDailog.dismiss();
                        MyApplication.TYPE = 2;
                        TwoWheelActivity.start(Discover2Activity.this, bluetoothGatt.getDevice(), null);
                        finish();
                    }
                }
            }

            public void connectFail() {
                if(progDailog != null) progDailog.dismiss();
                searching_again_btn.setVisibility(View.VISIBLE);
                utils.SuperActivityToast(Discover2Activity.this, getString(R.string.connect_failed), utils.toast.TOAST_WARNING);
            }
        };
    }

    public void init() {
        lay_toolbar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener(this);
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes("FF55010055"));
                    mHandler.sendEmptyMessageDelayed(1, 2000);
                }
            }
        };
    }

    public void onResume() {
        super.onResume();
        preDiscoverDevice();
        apiService.updateOdo();
    }


    public void onStart() {
        super.onStart();
        QBlueToothManager.getInstance().addBluetoothListener(this.bluetoothListener);
    }
    public void onStop() {
        super.onStop();
        QBlueToothManager.getInstance().removeBluetoothListener(this.bluetoothListener);
    }

    @Optional
    @OnClick({R.id.searching_again_btn, R.id.search_result_image})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.searching_again_btn:
            case R.id.search_result_image:
                preDiscoverDevice();
                break;
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 177 && i2 == -1) {
            doWhenBTEnable();
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        boolean z = true;
        if (i == 161) {
            for (int i2 : iArr) {
                if (i2 != 0) {
                    z = false;
                }
            }
            if (z) {
                doWhenHasPermission();
            } else {
                preDiscoverDevice();
            }
        } else if (i == 162) {
            for (int i3 : iArr) {
                if (i3 != 0) {
                    z = false;
                }
            }
            if (z) {
                onOpenBTAndHasPermission();
            } else {
                doWhenBTEnable();
            }
        }
    }

    private void preDiscoverDevice() {
        if (!QBlueToothManager.getInstance().isBlueToothSupport()) {
            utils.SuperActivityToast(this, getString(R.string.no_bluetooth), utils.toast.TOAST_WARNING);
            searching_again_btn.setVisibility(View.VISIBLE);
            return;
        }
        String str = "android.permission.ACCESS_FINE_LOCATION";
        if (ActivityCompat.checkSelfPermission(this, str) != 0) {
            String str2 = "android.permission.ACCESS_COARSE_LOCATION";
            String str3 = "android.permission.BLUETOOTH";
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, str)) {
                ActivityCompat.requestPermissions(this, new String[]{str3, str, str2}, 161);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{str3, str, str2}, 161);
            }
        } else {
            doWhenHasPermission();
        }
    }

    private void doWhenHasPermission() {
        if (QBlueToothManager.getInstance().isEnable()) {
            doWhenBTEnable();
        } else {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 177);
        }
    }

    private void doWhenBTEnable() {
        String str = "android.permission.ACCESS_FINE_LOCATION";
        if (ActivityCompat.checkSelfPermission(this, str) != 0) {
            String str2 = "android.permission.ACCESS_COARSE_LOCATION";
            String str3 = "android.permission.BLUETOOTH";
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, str)) {
                ActivityCompat.requestPermissions(this, new String[]{str3, str, str2}, 162);
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{str3, str, str2}, 162);
            return;
        }
        onOpenBTAndHasPermission();
    }

    private void onOpenBTAndHasPermission() {
        QBlueToothManager.getInstance().unbindDevices();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                QBlueToothManager.getInstance().scanLeDevice(true);
                Animation loadAnimation = AnimationUtils.loadAnimation(Discover2Activity.this, R.anim.bluetooth_jiazai);
                loadAnimation.setInterpolator(new LinearInterpolator());
                loadAnimation.setRepeatCount(-1);
                search_result_image.startAnimation(loadAnimation);
                mAdapter.setContext(Discover2Activity.this);
                listView.setAdapter(mAdapter);
//                mAdapter = new BaseAdapter(Lists.newInstance(), Discover2Activity.this);
//                if (mAdapter.getItemCount() > 0) {
//                    vListTopLine.setVisibility(0);
//                }
            }
        }, 600);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mHandler.removeMessages(1);
    }

    public void onBackPressed() {
        exitBy2Click();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        progDailog =  ProgressDialog.show(this, getString(R.string.connect), getString(R.string.connect));
        BluetoothDevice device = mAdapter.getDevice(position);
        QBlueToothManager.getInstance().connect(device);
    }

    private void exitBy2Click() {
        if (!this.isExit) {
            this.isExit = true;
            Toast.makeText(this, getString(R.string.quit), Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    isExit = false;
                }
            }, 2000);
            return;
        }
        QBlueToothManager.getInstance().disconnect();
//        this.mHandler.postDelayed(new Runnable() {
//            public void run() {
//                finish();
//                Process.killProcess(Process.myPid());
//                try {
//                    DevicesUtil.killProcess(DeviceDiscoverActivityNew.this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 500);
    }
}
