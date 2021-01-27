package com.egesio.test.egesioservices.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.egesio.test.egesioservices.bean.DeviceBean;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<DeviceBean> deviceBeens;
    private Context context;
    private DeviceBean deviceBean;

    public DeviceListAdapter(Context context, ArrayList<DeviceBean> deviceBeens) {
        super();
        this.mLeDevices = new ArrayList<>();
        this.deviceBeens = deviceBeens;
        this.context = context;
    }

    public void addDevice(DeviceBean deviceBean) {
        if (!mLeDevices.contains(deviceBean.getDevice())) {
            deviceBeens.add(deviceBean);
        }
    }

    public void addDevice(BluetoothDevice deviceBean) {
        if (!mLeDevices.contains(deviceBean)) {
            mLeDevices.add(deviceBean);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return deviceBeens.get(position).getDevice();
    }

    public void clear() {
        deviceBeens.clear();
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return deviceBeens.size();
    }

    @Override
    public Object getItem(int i) {
        return deviceBeens.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        return null;
    }


}
