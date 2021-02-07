package com.tn.escooter.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.LinkedList;

@SuppressLint({"NewApi"})
public class BluetoothGattExecutor extends BluetoothGattCallback {
    private volatile ServiceAction currentAction;
    private final LinkedList<ServiceAction> queue = new LinkedList<>();

    public interface ServiceAction {
        public static final ServiceAction NULL = new ServiceAction() {
            public boolean execute(BluetoothGatt bluetoothGatt) {
                return true;
            }
        };

        boolean execute(BluetoothGatt bluetoothGatt);
    }

    @SuppressLint({"NewApi"})
    public void execute(BluetoothGatt gatt) {
        boolean next;
        boolean next2;
        if (currentAction == null) {
            if (!queue.isEmpty()) {
                next = true;
            } else {
                next = false;
            }
            while (next) {
                ServiceAction action = (ServiceAction) queue.pop();
                currentAction = action;
                if (!action.execute(gatt)) {
                    break;
                }
                currentAction = null;
                if (!queue.isEmpty()) {
                    next2 = true;
                } else {
                    next2 = false;
                }
            }
        }
    }

    @SuppressLint({"NewApi"})
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        currentAction = null;
        execute(gatt);
    }

    @SuppressLint({"NewApi"})
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        currentAction = null;
        execute(gatt);
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == 0) {
            queue.clear();
        }
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        currentAction = null;
        execute(gatt);
    }

    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        currentAction = null;
        execute(gatt);
    }

    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        currentAction = null;
        execute(gatt);
    }
}
