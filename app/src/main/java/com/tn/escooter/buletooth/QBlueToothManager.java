package com.tn.escooter.buletooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tn.escooter.utils.BytesUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.util.functions.Action1;

import static android.content.Context.BLUETOOTH_SERVICE;

@SuppressLint({"NewApi"})
public class QBlueToothManager {
    public static final long SKIP_STEP_TEN_SECONDS_IN_MS = 10000;
    public static final int STATE_CLOSE = 4;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_DISCONNECT = 1;
    private static final int STATE_FAIL = 5;
    private static final String TAG = "QBlueToothManager";
    private static final String UUID1 = "00008888-0000-1000-8000-00805f9b34fb";
    private static final String UUID2 = "00008877-0000-1000-8000-00805f9b34fb";
    private static final String UUID_Des = "00002902-0000-1000-8000-00805f9b34fb";
    /* access modifiers changed from: private */
    public static BluetoothGattCharacteristic gattCharacteristic_char1;
    private static BluetoothGattCharacteristic gattCharacteristic_char2;
    private BluetoothManager bluetoothManager;
    /* access modifiers changed from: private */
    public int connectState;
    private Context context;
    /* access modifiers changed from: private */
    public byte[] currentDatas;
    /* access modifiers changed from: private */
    public List<LeDevice> deviceList;
    /* access modifiers changed from: private */
    public BluetoothGatt gatt;
    /* access modifiers changed from: private */
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattCallback mGattCallback;
    /* access modifiers changed from: private */
    @SuppressLint({"HandlerLeak"})
    public Handler mHandler;
    /* access modifiers changed from: private */
    public BluetoothAdapter.LeScanCallback mLeScanCallback;
    /* access modifiers changed from: private */
    public ScanCallback mScanCallback;
    /* access modifiers changed from: private */
    public boolean mScanning;
    /* access modifiers changed from: private */
    public List<OnBluetoothListener> onBluetoothListeners;
    /* access modifiers changed from: private */
    public Timer timer;

    private static class QBlueToothManagerHolder {
        /* access modifiers changed from: private */
        public static final QBlueToothManager INSTANCE = new QBlueToothManager();

        private QBlueToothManagerHolder() {
        }
    }

    /* access modifiers changed from: private */
    public void startReadData() {
    }

    public void reconnect() {
        BluetoothGatt bluetoothGatt = this.gatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
        }
    }

    public void addBluetoothListener(OnBluetoothListener onBluetoothListener) {
        if (onBluetoothListener != null) {
            if (this.onBluetoothListeners == null) {
                this.onBluetoothListeners = new ArrayList();
            }
            if (this.connectState == 2) {
                onBluetoothListener.onBlueToothConneted();
            }
            this.onBluetoothListeners.add(onBluetoothListener);
        }
    }

    public void removeBluetoothListener(OnBluetoothListener onBluetoothListener) {
        List<OnBluetoothListener> list = this.onBluetoothListeners;
        if (list != null) {
            list.remove(onBluetoothListener);
        }
    }

    public BluetoothDevice getCurrentDevice() {
        BluetoothGatt bluetoothGatt = this.gatt;
        if (bluetoothGatt != null) {
            return bluetoothGatt.getDevice();
        }
        return null;
    }

    private QBlueToothManager() {
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 2 && currentDatas != null) {
                    String bytesToHexStringTwo = BytesUtils.bytesToHexStringTwo(currentDatas, currentDatas.length);
                    if (analysisData1(bytesToHexStringTwo) == Integer.parseInt(bytesToHexStringTwo.substring(bytesToHexStringTwo.length() - 2, bytesToHexStringTwo.length()), 16) && onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                        for (OnBluetoothListener onBluetoothListener : onBluetoothListeners) {
                            BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(QBlueToothManager.gattCharacteristic_char1.getUuid(), 0, 0);
                            bluetoothGattCharacteristic.setValue(currentDatas);
                            onBluetoothListener.onCharacteristicChanged(gatt, bluetoothGattCharacteristic);
                        }
                    }
                } else if (message.what == 3) {
                    if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                        for (OnBluetoothListener onScanComplete : onBluetoothListeners) {
                            onScanComplete.onScanComplete();
                        }
                    }
                } else if (message.what == 4 && onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                    for (OnBluetoothListener onBluetoothListener2 : onBluetoothListeners) {
                        disconnect();
                        onBluetoothListener2.connectFail();
                    }
                }
            }
        };
    }

    public void init(Context context2) {
        this.context = context2;
        this.deviceList = new ArrayList();
        getBluetoothManager();
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.FOUND");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x0049, code lost:
                if (r5 != 13) goto L_0x0058;
             */
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                StringBuilder sb = new StringBuilder();
                sb.append("bluetooth listener：");
                sb.append(action);
                Log.d(QBlueToothManager.TAG, sb.toString());
                if (!TextUtils.isEmpty(action)) {
                    char c = 65535;
                    if (action.hashCode() == -1530327060 && action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                        c = 0;
                    }
                    if (c == 0) {
                        int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0);
                        if (intExtra != 10) {
                            if (intExtra == 12) {
                                connectState = 0;
                            }
                        }
                        connectState = 4;
                    }
                }
            }
        };
        try{
            context2.unregisterReceiver(receiver);
        }catch (Exception err){
        }
        context2.registerReceiver(receiver, intentFilter);
        this.mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
                if (i != 0 && !haveDeviceAdd(bluetoothDevice)) {
                    deviceList.add(new LeDevice(bluetoothDevice, i, bArr));
                    if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                        for (OnBluetoothListener onScanRelult : onBluetoothListeners) {
                            onScanRelult.onScanRelult(deviceList);
                        }
                    }
                }
            }
        };
        this.mScanCallback = new ScanCallback() {
            public void onScanResult(int i, ScanResult scanResult) {
                if (!haveDeviceAdd(scanResult.getDevice())) {
                    deviceList.add(new LeDevice(scanResult.getDevice(), scanResult.getRssi(), scanResult.getScanRecord().getBytes()));
                    if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                        for (OnBluetoothListener onScanRelult : onBluetoothListeners) {
                            onScanRelult.onScanRelult(deviceList);
                        }
                    }
                }
            }

            public void onBatchScanResults(List<ScanResult> list) {
                super.onBatchScanResults(list);
            }

            public void onScanFailed(int i) {
                super.onScanFailed(i);
                if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                    for (OnBluetoothListener onScanFail : onBluetoothListeners) {
                        onScanFail.onScanFail(i);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public boolean haveDeviceAdd(BluetoothDevice bluetoothDevice) {
        List<LeDevice> list = this.deviceList;
        if (list != null && list.size() > 0) {
            for (LeDevice device : this.deviceList) {
                if (device.getDevice().getAddress().equals(bluetoothDevice.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static QBlueToothManager getInstance() {
        return QBlueToothManagerHolder.INSTANCE;
    }

    public boolean write(byte[] bArr) {
        if (this.gatt != null) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = gattCharacteristic_char2;
            if (bluetoothGattCharacteristic != null) {
                bluetoothGattCharacteristic.setValue(bArr);
                return this.gatt.writeCharacteristic(gattCharacteristic_char2);
            }
        }
        List<OnBluetoothListener> list = this.onBluetoothListeners;
        if (list != null && list.size() > 0) {
            for (OnBluetoothListener onBlueToothDisconneted : this.onBluetoothListeners) {
                onBlueToothDisconneted.onBlueToothDisconneted();
            }
        }
        return false;
    }

    public boolean isConnected() {
        return this.connectState == 2;
    }

    public void disconnect() {
        BluetoothGatt bluetoothGatt = this.gatt;
        if (bluetoothGatt != null) {
            refreshDeviceCache(bluetoothGatt);
            this.gatt.disconnect();
            this.gatt.close();
            this.gatt = null;
        }
        gattCharacteristic_char1 = null;
        gattCharacteristic_char2 = null;
        this.gatt = null;
        getBluetoothManager();
    }

    public boolean isBlueToothSupport() {
        return this.context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    @SuppressLint({"NewApi"})
    public BluetoothManager getBluetoothManager() {
        this.bluetoothManager = (BluetoothManager) this.context.getSystemService(BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = this.bluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            return null;
        }
        return this.bluetoothManager;
    }

    public void startBlueBooth(Activity activity, int i) {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), i);
        }
    }

    public void unbindDevices() {
        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (bondedDevices != null && bondedDevices.size() > 0) {
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                if (bluetoothDevice != null) {
                    removeBond(bluetoothDevice);
                }
            }
        }
    }

    private void removeBond(@NonNull BluetoothDevice bluetoothDevice) {
        try {
            bluetoothDevice.getClass().getMethod("removeBond", new Class[0]).invoke(bluetoothDevice, new Object[0]);
        } catch (Exception unused) {
        }
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        String str = TAG;
        Log.e(str, "-------1");
        BluetoothGatt bluetoothGatt = this.gatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        Log.e(str, "-------2");
        this.gatt = bluetoothDevice.connectGatt(this.context, true, getBluetoothGattCallback());
        this.mHandler.removeMessages(4);
        this.mHandler.sendEmptyMessageDelayed(4, SKIP_STEP_TEN_SECONDS_IN_MS);
    }

    private BluetoothGattCallback getBluetoothGattCallback() {
        if (this.mGattCallback == null) {
            this.mGattCallback = new BluetoothGattCallback() {
                public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
                    super.onConnectionStateChange(bluetoothGatt, i, i2);
                    Log.e(QBlueToothManager.TAG, "-------3");
                    StringBuilder sb = new StringBuilder();
                    sb.append("onConnectionStateChange newState :");
                    sb.append(i2);
                    Log.w("QBluetoothManager", sb.toString());
                    if (i2 == 2) {
                        bluetoothGatt.discoverServices();
                    } else if (i2 == 0) {
                        connectState = 1;
                        close();
                        for (OnBluetoothListener onBlueToothDisconneted : onBluetoothListeners) {
                            onBlueToothDisconneted.onBlueToothDisconneted();
                        }
                    }
                }

                public void onServicesDiscovered(final BluetoothGatt bluetoothGatt, int i) {
                    super.onServicesDiscovered(bluetoothGatt, i);
                    String str = QBlueToothManager.TAG;
                    Log.e(str, "-------4");
                    StringBuilder sb = new StringBuilder();
                    sb.append("onServicesDiscovered status :");
                    sb.append(i);
                    Log.w("QBluetoothManager", sb.toString());
                    if (i == 0) {
                        connectState = 2;
                        gatt = bluetoothGatt;
                        Observable.timer(600, TimeUnit.MILLISECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
                            public void call(Long l) {
                                initCharacter(bluetoothGatt);
                                mHandler.removeMessages(4);
                                if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                                    Observable.from(onBluetoothListeners).observeOn(AndroidSchedulers.mainThread()).subscribe( new Action1<OnBluetoothListener>() {
                                        public void call(OnBluetoothListener onBluetoothListener) {
                                            onBluetoothListener.onBlueToothConneted();
                                        }
                                    }, (Action1<Throwable>) new Action1<Throwable>() {
                                        public void call(Throwable th) {
                                            th.printStackTrace();
                                        }
                                    });
                                }
                            }
                        }, (Action1<Throwable>) new Action1<Throwable>() {
                            public void call(Throwable th) {
                            }
                        });
                    }
                    Log.e(str, "-------4end");
                }

                public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
                    super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, i);
                    Log.e(QBlueToothManager.TAG, "-------5");
                    StringBuilder sb = new StringBuilder();
                    sb.append("onCharacteristicRead status :");
                    sb.append(i);
                    sb.append("======data:");
                    sb.append(BytesUtils.Bytes2HexString(bluetoothGattCharacteristic.getValue()));
                    Log.w("QBluetoothManager", sb.toString());
                }

                public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
                    super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
                    Log.e(QBlueToothManager.TAG, "-------6");
                    if (bluetoothGattCharacteristic.getValue() != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("onCharacteristicWrite status :");
                        sb.append(i);
                        sb.append("======data:");
                        sb.append(BytesUtils.Bytes2HexString(bluetoothGattCharacteristic.getValue()));
                        Log.w("QBluetoothManager", sb.toString());
                        startReadData();
                    }
                }

                public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
                    super.onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor, i);
                    StringBuilder sb = new StringBuilder();
                    sb.append("onDescriptorWrite status :");
                    sb.append(i);
                    Log.w("QBluetoothManager", sb.toString());
                }

                public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
                    super.onDescriptorRead(bluetoothGatt, bluetoothGattDescriptor, i);
                    StringBuilder sb = new StringBuilder();
                    sb.append("onDescriptorRead status :");
                    sb.append(i);
                    Log.w("QBluetoothManager", sb.toString());
                }

                public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                    super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
                    Log.e(QBlueToothManager.TAG, "-------7");
                    StringBuilder sb = new StringBuilder();
                    sb.append("onCharacteristicChanged======data:");
                    sb.append(BytesUtils.Bytes2HexString(bluetoothGattCharacteristic.getValue()));
                    Log.w("QBluetoothManager", sb.toString());
                    if (bluetoothGattCharacteristic.getValue() != null) {
                        String bytesToHexStringTwo = BytesUtils.bytesToHexStringTwo(bluetoothGattCharacteristic.getValue(), bluetoothGattCharacteristic.getValue().length);
                        if (analysisData1(bytesToHexStringTwo) == Integer.parseInt(bytesToHexStringTwo.substring(bytesToHexStringTwo.length() - 2, bytesToHexStringTwo.length()), 16)) {
                            if (onBluetoothListeners != null && onBluetoothListeners.size() > 0) {
                                for (OnBluetoothListener onCharacteristicChanged : onBluetoothListeners) {
                                    onCharacteristicChanged.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
                                }
                            }
                            boolean z = true;
                            String substring = bytesToHexStringTwo.substring(0, 6);
                            char c = 65535;
                            if (substring.hashCode() == 2070318434 && substring.equals("FF5502")) {
                                c = 0;
                            }
                            if (c == 0) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                                z = false;
                            }
                            if (z) {
                                timer();
                            }
                        }
                    }
                }
            };
        }
        return this.mGattCallback;
    }

    public void timer() {
        if (this.timer == null) {
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    write(BytesUtils.hexStringToBytes("FF5508005C"));
                }
            }, 0, 5000);
        }
    }

    public synchronized void close() {
        if (this.gatt != null) {
            refreshDeviceCache(this.gatt);
            this.gatt.disconnect();
            this.gatt.close();
        }
        gattCharacteristic_char1 = null;
        gattCharacteristic_char2 = null;
        this.gatt = null;
        getBluetoothManager();
    }

    private void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        try {
            Method method = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (method != null) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("回收蓝牙");
                sb.append(((Boolean) method.invoke(bluetoothGatt, new Object[0])).booleanValue());
                Log.e(str, sb.toString());
            }
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    public void initCharacter(BluetoothGatt bluetoothGatt) {
        for (BluetoothGattService characteristics : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics.getCharacteristics()) {
                if (!UUID1.equals(bluetoothGattCharacteristic.getUuid().toString())) {
                    if (UUID2.equals(bluetoothGattCharacteristic.getUuid().toString())) {
                        gattCharacteristic_char2 = bluetoothGattCharacteristic;
                    }
                } else if (gattCharacteristic_char1 == null) {
                    gattCharacteristic_char1 = bluetoothGattCharacteristic;
                    setCharacteristicNotification(gattCharacteristic_char1);
                }
            }
        }
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (this.gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)) {
            for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattCharacteristic.getDescriptors()) {
                if (bluetoothGattDescriptor != null) {
                    bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    this.gatt.writeDescriptor(bluetoothGattDescriptor);
                }
            }
        }
    }

    public boolean isEnable() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    public void scanLeDevice(boolean z) {
        List<LeDevice> list = this.deviceList;
        if (list != null) {
            list.clear();
        }
        BluetoothGatt bluetoothGatt = this.gatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            this.gatt.close();
        }
        this.mHandler.sendEmptyMessageDelayed(3, SKIP_STEP_TEN_SECONDS_IN_MS);
        if (Build.VERSION.SDK_INT >= 21) {
            final BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            if (z) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mScanning = false;
                        bluetoothLeScanner.stopScan(mScanCallback);
                    }
                }, SKIP_STEP_TEN_SECONDS_IN_MS);
                this.mScanning = true;
                bluetoothLeScanner.startScan(this.mScanCallback);
                return;
            }
            this.mScanning = false;
            bluetoothLeScanner.stopScan(this.mScanCallback);
        } else if (z) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SKIP_STEP_TEN_SECONDS_IN_MS);
            this.mScanning = true;
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
        } else {
            this.mScanning = false;
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
    }

    /* access modifiers changed from: private */
    public int analysisData1(String str) {
        int i = 0;
        String substring = str.substring(0, str.length() - 2);
        new StringBuilder();
        int i2 = 0;
        while (i <= substring.length() - 1) {
            int i3 = i + 2;
            i2 += Integer.valueOf(substring.substring(i, i3), 16).intValue();
            StringBuilder sb = new StringBuilder();
            sb.append("    ");
            sb.append(substring);
            sb.append("       ");
            sb.append(substring.substring(i, i3));
            Log.d("analysisData", sb.toString());
            i = i3;
        }
        return i2 % 256;
    }
}
