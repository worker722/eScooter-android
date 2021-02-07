package com.tn.escooter.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tn.escooter.R;
import com.tn.escooter.buletooth.LeDevice;
import com.tn.escooter.utils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tn.escooter.bluetooth.LFBluetootService.deviceService0;
import static com.tn.escooter.utils.utils.bluetooth_type.LENZOD;
import static com.tn.escooter.utils.utils.bluetooth_type.MACWHEEL;

public class BleDevicesAdapter extends BaseAdapter {
    private final ArrayList<BluetoothDevice> BleDevices = new ArrayList<>();
    public String currentDeviceAddress;
    private LayoutInflater inflater = null;
    private Context mContext;
    private final HashMap<String, Integer> rssiMap = new HashMap<>();
    private final HashMap<String, String> scanMap = new HashMap<>();
    private final HashMap<String, String> typeMap = new HashMap<>();

    private static class ViewHolder {
        ImageView blueInfo;
        TextView deviceAddress;
        TextView deviceName;
        TextView deviceRssi;
        ImageView imgCheck;
        TextView scanResult;

        private ViewHolder() {
        }
    }

    public void setContext(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    public void addDevice(LeDevice leDevice, String type) {
        addDevice(leDevice.getDevice(), leDevice.getRssi(), leDevice.getScanRecord(), type);
    }
    public void addDevice(BluetoothDevice device, int rssi, String scanResult, String type) {
        if (!BleDevices.contains(device)) {
            synchronized (this) {
                if (!BleDevices.contains(device)) {
                    BleDevices.add(device);
                    notifyDataSetChanged();
                }
            }
        }
        rssiMap.put(device.getAddress(), Integer.valueOf(rssi));
        scanMap.put(device.getAddress(), scanResult);
        if(type == LENZOD){
            if (scanResult.contains(utils.UuidFilter(deviceService0.toString()).toUpperCase())) {
                type = MACWHEEL;
            }
        }
        typeMap.put(device.getAddress(), type);
    }

    public void removeDevice(int position) {
        BleDevices.remove(position);
    }

    public BluetoothDevice getDevice(int position) {
        return BleDevices.get(position);
    }
    public String getDeviceType(String deviceAddress){
        return typeMap.get(deviceAddress);
    }

    public void clear() {
        currentDeviceAddress = null;
        BleDevices.clear();
        notifyDataSetChanged();
    }

    public void clearButDevices(List<BluetoothDevice> devices) {
        clear();
        if (devices != null && devices.size() >= 1) {
            BluetoothDevice device = (BluetoothDevice) devices.get(devices.size() - 1);
            currentDeviceAddress = device.getAddress();
            if (rssiMap.get(currentDeviceAddress) != null) {
                addDevice(device, (rssiMap.get(currentDeviceAddress)).intValue(), (String) scanMap.get(currentDeviceAddress), typeMap.get(currentDeviceAddress));
            }
            notifyDataSetChanged();
        }
    }

    public int getCount() {
        return BleDevices.size();
    }

    public Object getItem(int i) {
        return BleDevices.get(i);
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (inflater == null) {
            return null;
        }
        if (view == null) {
            view = inflater.inflate(R.layout.item_bluetooth_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.imgCheck = (ImageView) view.findViewById(R.id.img_check);
            viewHolder.blueInfo = (ImageView) view.findViewById(R.id.bluetooth_info);
            viewHolder.scanResult = (TextView) view.findViewById(R.id.scan_result);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        BluetoothDevice device = (BluetoothDevice) BleDevices.get(i);
        String deviceName = device.getName();
        if (deviceName == null || deviceName.length() <= 0) {
            viewHolder.deviceName.setText("Unknown device");
        } else {
            viewHolder.deviceName.setText(deviceName);
        }
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText(String.valueOf(rssiMap.get(device.getAddress())));
        viewHolder.scanResult.setText(typeMap.get(device.getAddress()));
        if (device.getAddress().equals(currentDeviceAddress)) {
            viewHolder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgCheck.setVisibility(View.GONE);
        }
        viewHolder.blueInfo.setOnClickListener(new OnClickListener() {
            @TargetApi(21)
            public void onClick(View v) {
            }
        });
        return view;
    }
}
