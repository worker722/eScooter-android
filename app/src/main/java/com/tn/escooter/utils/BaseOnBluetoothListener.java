package com.tn.escooter.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.tn.escooter.buletooth.LeDevice;
import com.tn.escooter.buletooth.OnBluetoothListener;

import java.util.List;

public abstract class BaseOnBluetoothListener implements OnBluetoothListener {
    public void connectFail() {
    }

    public void onBlueToothConneted() {
    }

    public void onBlueToothDisconneted() {
    }

    public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
    }

    public void onScanComplete() {
    }

    public void onScanFail(int i) {
    }

    public void onScanRelult(List<LeDevice> list) {
    }
}