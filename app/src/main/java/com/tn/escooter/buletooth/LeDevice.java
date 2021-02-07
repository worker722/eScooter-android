package com.tn.escooter.buletooth;

import android.bluetooth.BluetoothDevice;

import com.tn.escooter.utils.BytesUtils;

public class LeDevice {
    private BluetoothDevice device;
    private int rssi;
    private byte[] scanRecord;

    public LeDevice(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
        this.device = bluetoothDevice;
        this.rssi = i;
        this.scanRecord = bArr;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public int getRssi() {
        return this.rssi;
    }

//    public byte[] getScanRecord() {
//        return this.scanRecord;
//    }
    public String getScanRecord() {
        return BytesUtils.BytesToString(this.scanRecord);
    }

    public LeDevice() {
    }
}
