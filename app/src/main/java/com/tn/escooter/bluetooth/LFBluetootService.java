package com.tn.escooter.bluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;

import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.utils.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static com.tn.escooter.utils.utils.bluetooth_type.MACWHEEL;

public class LFBluetootService extends Service {
    private static final String INTENT_PREFIX = "MACWHEEL";
    public static final String ACTION_BLE_CODE_OK = (INTENT_PREFIX + ".ACTION_BLE_CODE_OK");
    public static final String ACTION_BLE_REQUEST_PASSWORD = (INTENT_PREFIX + ".ACTION_BLE_REQUEST_PASSWORD");
    public static final String ACTION_BLE_REQUEST_PASSWORD_AGAIN = (INTENT_PREFIX + ".ACTION_BLE_REQUEST_PASSWORD_AGAIN");
    public static final String ACTION_DATA_AVAILABLE = (INTENT_PREFIX + ".ACTION_DATA_AVAILABLE");
    public static final String ACTION_DISCONNECTED = (INTENT_PREFIX + ".ACTION_DISCONNECTED");
    public static final String ACTION_GATT_CONNECTED = (INTENT_PREFIX + ".ACTION_GATT_CONNECTED");
    public static final String ACTION_GATT_CONNECTING = (INTENT_PREFIX + ".ACTION_GATT_CONNECTING");
    public static final String ACTION_GATT_DISCONNECTED = (INTENT_PREFIX + ".ACTION_GATT_DISCONNECTED");
    public static final String ACTION_GATT_SERVICES_DISCOVERED = (INTENT_PREFIX + ".ACTION_GATT_SERVICES_DISCOVERED");
    public static final String ACTION_START_SCAN = (INTENT_PREFIX + ".ACTION_START_SCAN");
    public static final UUID CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final boolean D = true;
    public static final String EXTRA_CHARACTERISTIC_UUID = (INTENT_PREFIX + ".EXTRA_CHARACTERISTIC_UUI");
    public static final String EXTRA_DATA = (INTENT_PREFIX + ".EXTRA_DATA");
    public static final String EXTRA_SERVICE_UUID = (INTENT_PREFIX + ".EXTRA_SERVICE_UUID");
    public static final String EXTRA_TEXT = (INTENT_PREFIX + ".EXTRA_TEXT");
    public static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STOPTIME = 1800;
    /* access modifiers changed from: private */
    public static final String TAG = (LFBluetootService.class.getSimpleName() + "LFTest");
    public static final UUID deviceService0 = UUID.fromString("68f2bd90-7285-11ea-bc55-0002a5d5c51b");
    public static final UUID deviceService3 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String deviceService1 = "00008888-0000-1000-8000-00805f9b34fb";
    public static final String deviceService2 = "00008877-0000-1000-8000-00805f9b34fb";
    /* access modifiers changed from: private */
    public static LFBluetootService mbleService;
    public static final UUID readUUID = UUID.fromString("68f2bd94-7285-11ea-bc55-0002a5d5c51b");
    public static final UUID writeUUID = UUID.fromString("68f2bd91-7285-11ea-bc55-0002a5d5c51b");
    /* access modifiers changed from: private */
    public String aimAddress;
    public BluetoothAdapter bleAdapter;
    public BluetoothManager bluetoothManager;
    private CheckDevice checkDevice;
    public static SharedPreferences preferences;

    private Thread check_is_disconnect = new Thread(new Runnable() {
        public void run() {
            while (true) {
                if (connectionState == 2 && System.currentTimeMillis() - receive_num_time > 2000) {
                    connectionState = 0;
                    broadcastUpdate(ACTION_DISCONNECTED);
                    scanTime = 0;
                }
            }
        }
    });
    /* access modifiers changed from: private */
    public String connectedAddress;
    public int connectionState = 0;
    /* access modifiers changed from: private */
    public BluetoothDevice currentDevice;
    private final BluetoothGattExecutor executor = new BluetoothGattExecutor() {
        @SuppressLint({"NewApi"})
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();
            if (newState == 2) {
                currentDevice = device;
                connectedAddress = device.getAddress();
                myBluetoothAdapter.currentDeviceAddress = currentDevice.getAddress();
                gatt = gatt;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices();
                gattList.add(gatt);
            } else if (newState == 0) {
                connectionState = 0;
                if (device.getAddress() == connectedAddress) {
                    connectedAddress = null;
                    currentDevice = null;
                    aimAddress = null;
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                }
                mRequestConnectionPriority = false;
                gatt.close();
            } else if (newState == 133) {
                gatt.disconnect();
                BluetoothDevice device2 = gatt.getDevice();
                if (device2.getAddress().equals(aimAddress)) {
                    connect(device2.getAddress());
                }
            }
        }

        public boolean refreshDeviceCache(BluetoothGatt gatt) {
            if (gatt == null) {
                return false;
            }
            BluetoothGatt localBluetoothGatt = gatt;
            try {
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    return ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        @TargetApi(18)
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == 0) {
                writeCharacteristic = null;
                readCharacteristic = null;
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                    if (gattCharacteristics.isEmpty()) {
                    } else {
                    }
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        UUID characteristicUUID = gattCharacteristic.getUuid();
                        if (characteristicUUID.equals(writeUUID)) {
                            writeCharacteristic = gattCharacteristic;
                        } else if (characteristicUUID.equals(readUUID)) {
                            readCharacteristic = gattCharacteristic;
                        }
                    }
                }
                if (!(writeCharacteristic == null || readCharacteristic == null)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    gatt.setCharacteristicNotification(readCharacteristic, D);
                    BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(CHARACTERISTIC_CONFIG);
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                    gatt.setCharacteristicNotification(writeCharacteristic, D);
                    faultPasswordOfTimes = 0;
                    isFirstReceiveCODE_OK = D;
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                final BluetoothGatt bluetoothGatt = gatt;
                new Thread(new ThreadGroup("delay1"), new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2500);
                            password = getPassword(bluetoothGatt.getDevice().getAddress());
                            if (password != null) {
                                sendString("CODE=" + password);
                            } else {
                                broadcastUpdate(ACTION_BLE_REQUEST_PASSWORD);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Thread.currentThread().interrupt();
                    }
                }).start();
                new Thread(new ThreadGroup("delay1"), new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(200);
                            BleMutualAuthentication.getBleAuthentication().reset().startAuth();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Thread.currentThread().interrupt();
                    }
                }).start();
                return;
            }
        }

        @SuppressLint({"NewApi"})
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == 0) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x00cd  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x011d  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0176  */
        /* JADX WARNING: Removed duplicated region for block: B:9:0x0072  */
        @TargetApi(18)
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String address;
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data = characteristic.getValue();
            receive_num_time = System.currentTimeMillis();
            String string = null;
            try {
                String string2 = new String(data, "UTF-8");
                final String finalString = string2;
                new Thread(new ThreadGroup("delay1"), new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Thread.currentThread().interrupt();
                    }
                }).start();
                string = string2;
            } catch (UnsupportedEncodingException e2) {
                e2.printStackTrace();
                address = gatt.getDevice().getAddress();
                if (!address.equals(connectedAddress)) {
                }
                if (!address.toString().equals(myBluetoothAdapter.currentDeviceAddress)) {
                }
                if (string.equals("CODE_OK")) {
                }
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            address = gatt.getDevice().getAddress();
            if (!address.equals(connectedAddress)) {
                currentDevice = gatt.getDevice();
                connectedAddress = gatt.getDevice().getAddress();
            }
            if (!address.toString().equals(myBluetoothAdapter.currentDeviceAddress)) {
                myBluetoothAdapter.currentDeviceAddress = gatt.getDevice().getAddress();
                myBluetoothAdapter.notifyDataSetChanged();
            }
            if (string.equals("CODE_OK")) {
                if (faultPasswordOfTimes != 0) {
                    faultPasswordOfTimes = 0;
                }
                if (isFirstReceiveCODE_OK) {
                    isFirstReceiveCODE_OK = false;
                    savePassword(address, password);
                }
                connectionState = 2;
                scanTime = STOPTIME;
                broadcastUpdate(ACTION_BLE_CODE_OK, characteristic);
                new Thread(new ThreadGroup("delay1"), new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(200);
                            sendString("GETDEVID");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else if (string.equals("CODE_NG")) {
                faultPasswordOfTimes = faultPasswordOfTimes + 1;
                if (faultPasswordOfTimes >= 3) {
                    faultPasswordOfTimes = 0;
                    removePassword(address);
                    disconnect(address);
                    showToast("Please make sure you are connected to your scooter.", utils.toast.TOAST_ERROR);
                } else {
                    broadcastUpdate(ACTION_BLE_REQUEST_PASSWORD_AGAIN);
                }
            }
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @RequiresApi(api = 21)
        public void onConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout, int status) {
            requestConnectionPriority(1);
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == 0) {
                Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                intent.putExtra(EXTRA_TEXT, Integer.toString(rssi));
                sendBroadcast(intent);
            }
        }
    };
    /* access modifiers changed from: private */
    public int faultPasswordOfTimes = 0;
    /* access modifiers changed from: private */
    public BluetoothGatt gatt;
    /* access modifiers changed from: private */
    public List<BluetoothGatt> gattList;
    /* access modifiers changed from: private */
    public boolean isFirstReceiveCODE_OK = false;
    /* access modifiers changed from: private */
    public boolean isScanning = false;
    private final IBinder mBinder = new LocalBinder();
    @SuppressLint({"NewApi"})
    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            String DeviceUuid = device.getUuids().toString();
//            deviceService1.equals(DeviceUuid) || deviceService2.equals(DeviceUuid) ||
            String str = BytesUtils.BytesToString(scanRecord);

            if (str.contains(utils.UuidFilter(deviceService0.toString()).toUpperCase())) {
                boolean shouldConnect = false;
                synchronized (this) {
                    if (connectionState == 0) {
                        shouldConnect = D;
                    }
                }
                if (shouldConnect && getPassword(device.getAddress()) != null) {
                    connectionState = 1;
                    connect(device.getAddress());
                }
                myBluetoothAdapter.addDevice(device, rssi, str, MACWHEEL);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mRequestConnectionPriority = false;
    /* access modifiers changed from: private */
    public BleDevicesAdapter myBluetoothAdapter;
    /* access modifiers changed from: private */
    public ArrayList<String> oldAddress;
    public String password;
    /* access modifiers changed from: private */
    public BluetoothGattCharacteristic readCharacteristic = null;
    /* access modifiers changed from: private */
    public long receive_num_time = 0;
    /* access modifiers changed from: private */
    public int scanTime = 0;
    private Scanner scanner;
    /* access modifiers changed from: private */
    public BluetoothGattCharacteristic writeCharacteristic = null;

    private static class CheckDevice extends Thread {
        private final BluetoothAdapter bluetoothAdapter;

        CheckDevice(BluetoothAdapter adapter) {
            bluetoothAdapter = adapter;
        }

        @SuppressLint({"NewApi"})
        public void run() {
            while (true) {
                try {
                    if (!(mbleService == null || mbleService.oldAddress == null)) {
                        List<BluetoothDevice> gattDevices = mbleService.bluetoothManager.getConnectedDevices(7);
                        List connectedDevices = mbleService.bluetoothManager.getConnectedDevices(8);
                        if (gattDevices.size() == 1) {
                            if (!(mbleService.myBluetoothAdapter.currentDeviceAddress == mbleService.aimAddress || mbleService.myBluetoothAdapter.currentDeviceAddress == ((BluetoothDevice) gattDevices.get(0)).getAddress())) {
                                mbleService.connectedAddress = ((BluetoothDevice) gattDevices.get(0)).getAddress();
                                mbleService.myBluetoothAdapter.currentDeviceAddress = ((BluetoothDevice) gattDevices.get(0)).getAddress();
                                mbleService.broadcastUpdate(ACTION_GATT_CONNECTED);
                            }
                        } else if (gattDevices.size() > 1) {
                            for (BluetoothDevice device : gattDevices) {
                                if (!device.getAddress().equals(mbleService.aimAddress)) {
                                    for (int i = 0; i < mbleService.gattList.size(); i++) {
                                        if (device.getAddress().equals(((BluetoothGatt) mbleService.gattList.get(i)).getDevice().getAddress())) {
                                            ((BluetoothGatt) mbleService.gattList.get(i)).close();
                                            mbleService.gattList.remove(i);
                                        }
                                    }
                                } else {
                                    mbleService.myBluetoothAdapter.currentDeviceAddress = mbleService.aimAddress;
                                    mbleService.broadcastUpdate(ACTION_GATT_CONNECTED);
                                }
                            }
                            continue;
                        } else if (gattDevices.size() == 0 && mbleService.connectedAddress != null) {
                            mbleService.connectedAddress = null;
                        }
                    }
                    sleep(3000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public LFBluetootService getService() {
            return LFBluetootService.this;
        }
    }

    private static class Scanner extends Thread {
        private final BluetoothAdapter bluetoothAdapter;
        private int bluetoothState = 0;
        private boolean isStart = false;
        private final LeScanCallback mLeScanCallback;

        Scanner(BluetoothAdapter adapter, LeScanCallback callback) {
            bluetoothAdapter = adapter;
            mLeScanCallback = callback;
        }

        @SuppressLint({"NewApi"})
        public void startScanning() {
            synchronized (this) {
                if (!isStart) {
                    isStart = D;
                    start();
                }
            }
        }

        @SuppressLint({"NewApi"})
        public void run() {
            while (true) {
                try {
                    mbleService.scanTime = mbleService.scanTime + 1;
                    mbleService.scanTime = STOPTIME < mbleService.scanTime ? STOPTIME : mbleService.scanTime;
                    int adapterState = bluetoothAdapter.getState();
                    if (adapterState == 12) {
                        bluetoothState = 12;
                        if (mbleService.scanTime < STOPTIME && mbleService.scanTime > 2 && !mbleService.isScanning) {
                            bluetoothAdapter.startLeScan(mLeScanCallback);
                            mbleService.isScanning = D;
                        } else if (mbleService.scanTime >= STOPTIME && mbleService.isScanning) {
                            bluetoothAdapter.stopLeScan(mLeScanCallback);
                            mbleService.isScanning = false;
                        }
                    } else if (adapterState == 11) {
                        mbleService.scanTime = 0;
                    } else if (adapterState == 10 && bluetoothState != 10) {
                        bluetoothState = 10;
                        mbleService.broadcastUpdate(ACTION_GATT_DISCONNECTED);
                        if (mbleService.isScanning()) {
                            bluetoothAdapter.stopLeScan(mLeScanCallback);
                            mbleService.isScanning = false;
                        }
                    }
                    sleep(100);
                } catch (InterruptedException e) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    return;
                } catch (Throwable th) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    throw th;
                }
            }
        }
    }

    public static LFBluetootService getInstent() {
        return mbleService;
    }

    public BleDevicesAdapter getBleDevicesAdapter() {
        return myBluetoothAdapter;
    }

    @TargetApi(18)
    public void startScan() {
        myBluetoothAdapter.clear();
        List<BluetoothDevice> gattDevices = bluetoothManager.getConnectedDevices(7);
        for (BluetoothDevice device : gattDevices) {
            myBluetoothAdapter.addDevice(device, 0, null, MACWHEEL);
            if (device.getAddress().equals(aimAddress)) {
                myBluetoothAdapter.currentDeviceAddress = aimAddress;
            }
        }
        if (gattDevices.size() != 0 && myBluetoothAdapter.currentDeviceAddress == null) {
            myBluetoothAdapter.currentDeviceAddress = ((BluetoothDevice) gattDevices.get(0)).getAddress();
            myBluetoothAdapter.notifyDataSetChanged();
        }
        broadcastUpdate(ACTION_START_SCAN);
        scanTime = 0;
        bleAdapter.startLeScan(mLeScanCallback);
        mbleService.isScanning = D;
    }

    public void stopScan() {
        scanTime = STOPTIME;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public String getConnectedAddress() {
        return connectedAddress;
    }

    public BluetoothDevice getCurrentDevice() {
        return currentDevice;
    }

    @RequiresApi(api = 21)
    public void requestConnectionPriority(int priority) {
        if (gatt != null && !mRequestConnectionPriority) {
            boolean requestConnectionPriority = gatt.requestConnectionPriority(priority);
            mRequestConnectionPriority = D;
        }
    }

    /* access modifiers changed from: private */
    public void broadcastUpdate(String action) {
        sendBroadcast(new Intent(action));
    }

    /* access modifiers changed from: private */
    @SuppressLint({"NewApi"})
    public void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_SERVICE_UUID, characteristic.getService().getUuid().toString());
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        sendBroadcast(intent);
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @SuppressLint({"NewApi"})
    public boolean initialize() {
        if (mbleService != null) {
            return D;
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }
        }
        bleAdapter = bluetoothManager.getAdapter();
        if (bleAdapter == null) {
//            bleAdapter = BluetoothAdapter.getDefaultAdapter();
            return false;
        }
        if (scanner == null) {
            scanner = new Scanner(bleAdapter, mLeScanCallback);
        }
        if (checkDevice == null) {
            oldAddress = new ArrayList<>();
            checkDevice = new CheckDevice(bleAdapter);
            checkDevice.start();
        }
        myBluetoothAdapter = new BleDevicesAdapter();
        mbleService = this;
        scanner.startScanning();
        gattList = new ArrayList();
        check_is_disconnect.start();
        return D;
    }

    @SuppressLint({"NewApi"})
    public boolean connect(String address) {
        if (bleAdapter == null || address == null) {
            return false;
        }
        if (gatt != null) {
            gatt.disconnect();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        aimAddress = address;
        BluetoothDevice device = bleAdapter.getRemoteDevice(aimAddress);
        if (device == null) {
            return false;
        }
        gatt = device.connectGatt(this, false, executor);
        if (gatt == null) {
            gatt = device.connectGatt(this, false, executor);
        }
        connectionState = 1;
        if (gatt != null) {
            gatt.connect();
        }
        if (!oldAddress.contains(address)) {
            oldAddress.add(address);
        }
//        if (!NewMainActivity.is_click_blue_connect) {
            broadcastUpdate(ACTION_GATT_CONNECTING);
//        }
        return D;
    }

    public boolean again_connect() {
        if (bleAdapter == null || aimAddress == null) {
            return false;
        }
        BluetoothDevice device = bleAdapter.getRemoteDevice(aimAddress);
        if (device == null) {
            return false;
        }
        gatt = device.connectGatt(this, false, executor);
        if (gatt != null) {
            gatt.connect();
        }
        if (!oldAddress.contains(aimAddress)) {
            oldAddress.add(aimAddress);
        }
        return D;
    }

    @TargetApi(18)
    public boolean disconnect(String address) {
        if (address == null) {
            return false;
        }
        if (bleAdapter == null || address == null) {
            return false;
        } else if (bleAdapter.getRemoteDevice(address) == null) {
            return false;
        } else {
            if (gatt != null && gatt.getDevice().getAddress().equals(address)) {
                gatt.disconnect();
                gatt = null;
                if (aimAddress != null && aimAddress.equals(address)) {
                    aimAddress = null;
                }
                removePassword(address);
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
            return D;
        }
    }

    public String getAimAddress() {
        return aimAddress;
    }

    @TargetApi(18)
    public void close() {
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    @SuppressLint({"NewApi"})
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bleAdapter == null || gatt == null) {
        } else {
            gatt.readCharacteristic(characteristic);
        }
    }

    @SuppressLint({"NewApi"})
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bleAdapter == null || gatt == null) {
        } else {
            gatt.writeCharacteristic(characteristic);
        }
    }

    @TargetApi(18)
    public boolean readRemoteRssi() {
        if (bleAdapter != null && gatt != null) {
            return gatt.readRemoteRssi();
        }
        return false;
    }

    @SuppressLint({"NewApi"})
    public List<BluetoothGattService> getSupportedGattServices() {
        if (gatt == null) {
            return null;
        }
        return gatt.getServices();
    }

    @SuppressLint({"NewApi"})
    public void sendString(String string) {
        try {
            sendData(string.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"NewApi"})
    public void sendHexString(String string) {
        try {
            sendData(BytesUtils.StringToBytes(string));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"NewApi"})
    public void sendData(byte[] bytes) {
        if (bleAdapter == null || gatt == null) {
        } else if (writeCharacteristic != null) {
            writeCharacteristic.setValue(bytes);
            gatt.writeCharacteristic(writeCharacteristic);
        }
    }

    public String getPassword(String address) {
        String password2 = preferences.getString(address, null);
        return password2;
    }

    public void savePassword(String address, String password2) {
        Editor prefsEditor = preferences.edit();
        prefsEditor.putString(address, password2);
        prefsEditor.commit();
    }

    public void removePassword(String address) {
        Editor prefsEditor = preferences.edit();
        prefsEditor.remove(address);
        prefsEditor.commit();
    }

    public void showToast(String string, int type) {
        utils.SuperActivityToast(this, string, type);
    }

}
