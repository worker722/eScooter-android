package com.tn.escooter.buletooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import java.util.List;

public interface OnBluetoothListener {
    void connectFail();

    void onBlueToothConneted();

    void onBlueToothDisconneted();

    void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic);

    void onScanComplete();

    void onScanFail(int i);

    void onScanRelult(List<LeDevice> list);
}
