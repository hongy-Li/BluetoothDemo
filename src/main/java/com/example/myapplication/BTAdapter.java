package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

/**
 * Created by lhy on 2016/7/29.
 */
public class BTAdapter extends BaseAdapter {

    List<BluetoothDevice> mBTDevices;
    Context mContext;

    public BTAdapter(Context context, List<BluetoothDevice> devices) {
        mContext = context;
        mBTDevices = devices;
    }

    @Override
    public int getCount() {
        return mBTDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mBTDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(mContext, R.layout.item_lv_bt_adapter, null);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_mac = (TextView) view.findViewById(R.id.tv_mac);
            holder.tv_state = (TextView) view.findViewById(R.id.tv_state);
            holder.tv_devicesClass = (TextView) view.findViewById(R.id.tv_devicesClass);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        int stateCode = mBTDevices.get(i).getBondState();

        int devicesClass = mBTDevices.get(i).getBluetoothClass().getDeviceClass();
        String name = mBTDevices.get(i).getName();
        String mac = mBTDevices.get(i).getAddress();
        String state = BleUtil.getInstance(mContext).getDeviceBoundType(stateCode);
        String deviceType = BleUtil.getInstance(mContext).getDeviceType(devicesClass);

        state=BleUtil.getInstance(mContext).isConnected(mBTDevices.get(i))?"已连接":state;

        //当一个已经配对过的设备，并不在附近的时候，这时候给该设备接触配对，设备列表上不应该显示该设备
        if(!TextUtils.isEmpty(name)){
            holder.tv_name.setText(name);
            holder.tv_mac.setText(mac);
            holder.tv_state.setText(state);
            holder.tv_devicesClass.setText(deviceType);
        }




        return view;
    }

    class ViewHolder {
        TextView tv_name;
        TextView tv_mac;
        TextView tv_state;
        TextView tv_devicesClass;
    }

}
