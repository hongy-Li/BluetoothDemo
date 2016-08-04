package com.example.myapplication;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

/**
 * 蓝牙工具类
 * Created by lhy on 2016/7/28.
 */
public class BleUtil {

    public String TAG = this.getClass().getName();
    private static BleUtil mInstance = null;
    private Context mContext;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mConnectedDevice = null;//连接设备

    private BleUtil(Context context) {
        mContext = context;
        getBTAdapter();// 初始化蓝牙适配器
        registBroadcast();
    }

    public static BleUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BleUtil.class) {
                if (mInstance == null) {
                    mInstance = new BleUtil(context);
                }
            }
        }
        return mInstance;
    }

    IBTListener mListener = null;

    public void setBTListener(IBTListener listener) {
        mListener = listener;
    }


    /**
     * 初始化蓝牙适配器
     *
     * @return
     */
    public BluetoothAdapter getBTAdapter() {
        if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();// 初始化蓝牙适配器
        }
        return mBtAdapter;
    }

    /**
     * 开启蓝牙
     */
    public void openBT() {
        if (!getBTAdapter().isEnabled()) {
            getBTAdapter().enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void closeBT() {
        if (getBTAdapter().isEnabled()) {
            getBTAdapter().disable();
        }
    }

    /**
     * 搜索设备
     */
    public void searchDevice() {

        if (!getBTAdapter().isEnabled()) {
            //TODO 提示先打开蓝牙
            Toast.makeText(mContext, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getBTAdapter().isDiscovering()) {
            Log.i(TAG, "Discovering...");
            //TODO 获取已匹配设备

        } else {

            Set<BluetoothDevice> devices = getBTAdapter().getBondedDevices();
            for (BluetoothDevice device : devices) {
                Log.i(TAG, "Alerady paired DevicesName=" + device.getName() + "----" + "DevicesMac=" + device.getAddress() + "----" + "alias=" + device.getAlias()
                        + "----" + "AliasName=" + device.getAliasName() + "----" + "type=" + device.getType() + "----" + "TrustState=" + device.getTrustState()
                        + "----" + "Uuids=" + device.getUuids());
                if (mListener != null) {
                    mListener.boundDevice(device);
                }
            }

            getBTAdapter().startDiscovery();
            Log.i(TAG, "Begin to discovery>>>>>>>>>>");
        }

    }

    /**
     * 与设备进行配对
     *
     * @param device
     */
    public void paireToDevice(BluetoothDevice device) {

        int bondState = device.getBondState();
        if (BluetoothDevice.BOND_BONDED == bondState) {
            Log.i(TAG, "be bond");
            connectToDevice(device);
        } else if (BluetoothDevice.BOND_NONE == bondState) {
            boolean bool = paire(device);
            if (bool) {
                //TODO 连接设备

            }
        }
    }

    /**
     * 配对
     */
    public boolean paire(BluetoothDevice device) {
        Log.i(TAG, "Prepare to pair----------");
        boolean bool = false;
        if (device != null) {
            //配对前先取消扫描
            if (getBTAdapter().isDiscovering()) {
                Log.i(TAG, "Is Discovering....,cancelDiscovery");
                getBTAdapter().cancelDiscovery();
            }
            //开始配对
            bool = device.createBond();
        }
        return bool;
    }

    /**
     * 取消配对
     *
     * @param device
     */
    public void cancelParire(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
            device.cancelBondProcess();
        }
        device.removeBond();
    }

    /**
     * 开放检测
     *
     * @param sec 秒
     */
    public void visible(int sec) {
        Intent discoverableIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        mContext.startActivity(discoverableIntent);
    }


    /**
     * 注册蓝牙相关广播
     */
    public void registBroadcast() {
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//监听蓝牙开启状态,开启关闭

        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//      intent.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//        intent.addAction(BluetoothDevicePicker.ACTION_DEVICE_SELECTED);
//        intent.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
//        intent.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        intent.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);

        getBTAdapter().getProfileProxy(mContext, mProfileServiceListener, BluetoothProfile.A2DP);

        mContext.registerReceiver(mBleUtilReceiver, intent);
    }


    private BluetoothA2dp mBTA2DP;
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile bluetoothProfile) {
            Log.i(TAG, "onServiceConnected，profile=" + profile);
            switch (profile) {
                case BluetoothProfile.A2DP:
                    mBTA2DP = (BluetoothA2dp) bluetoothProfile;
                    break;
            }

        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i(TAG, "onServiceDisconnected,profile=" + profile);
        }
    };


    /**
     * 反注册广播
     */
    public void unRegist() {
        mContext.unregisterReceiver(mBleUtilReceiver);
    }

    /**
     * 绑定状态改变
     *
     * @param state
     */
    public void bondStateChanged(int state, BluetoothDevice device) {
        switch (state) {
            case BluetoothDevice.BOND_BONDING:
                Log.i(TAG, "Be Pairing");
                if (mListener != null) {
                    mListener.boundingDevice(device);
                }
                break;
            case BluetoothDevice.BOND_BONDED:
                Log.i(TAG, "Be Paired");
                if (mListener != null) {
                    mListener.boundDevice(device);
                }
                //TODO 连接设备
                connectToDevice(device);
                break;
            case BluetoothDevice.BOND_NONE:
                Log.i(TAG, "No Paire");
                if (mListener != null) {
                    mListener.unBoundDevice(device);
                }
            default:
                break;
        }
    }

    /**
     * 连接设备
     *
     * @param device
     */
    public void connectToDevice(BluetoothDevice device) {
        int devicesClass = device.getBluetoothClass().getDeviceClass();
        if (devicesClass == BluetoothClass.Device.PHONE_SMART) {
            Log.i(TAG, "PHONE_SMART Devices");

        } else if(devicesClass==BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
            Log.i(TAG, "Audio Devices,mBTA2DP=" + mBTA2DP);
            boolean bool = isConnected(device);
            if (bool) {
                Log.i(TAG, "Be connected");
            } else {

                if (mBTA2DP != null) {
                    // 断开上次连接
                    if (mConnectedDevice != null) {
                        Log.i(TAG, "Disconnect last connected=" + mConnectedDevice.getName());
                        mBTA2DP.disconnect(mConnectedDevice);
                    }
                    Log.i(TAG, "begin to connect");
                    mConnectedDevice = device;
                    mBTA2DP.connect(device);
                }
            }
        }else{
            Log.i(TAG, "Other Devices");
        }
    }

    /**
     * 判断是否连接
     */

    public boolean isConnected(BluetoothDevice device) {
        boolean bool = false;
        if (mBTA2DP != null) {
            int connectionState = mBTA2DP.getConnectionState(device);//拿到当前设备的连接状态
            if (connectionState == BluetoothProfile.STATE_CONNECTED) {
                bool = true;
            }
        }
        return bool;
    }


    /**
     * 本地蓝牙适配器状态改变
     *
     * @param state
     */
    public void localAdapterStateChanged(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                Log.i(TAG, "BluetoothAdapter.STATE_OFF");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.i(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_ON:
                Log.i(TAG, "BluetoothAdapter.STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.i(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                break;
        }
    }

    /**
     * 广播接受者
     */
    private BroadcastReceiver mBleUtilReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice mBTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//存储设备信息
            Log.i(TAG, "Receive action=" + action);
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i(TAG, "Discover Start>>>>");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(TAG, "Discover End>>>>");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    Log.i(TAG, "DevicesName=" + mBTDevice.getName() + "----" + "DevicesMac=" + mBTDevice.getAddress() + "----" + "alias=" + mBTDevice.getAlias()
                            + "----" + "AliasName=" + mBTDevice.getAliasName() + "----" + "type=" + mBTDevice.getType() + "----" + "TrustState=" + mBTDevice.getTrustState()
                            + "----" + "Uuids=" + mBTDevice.getUuids());
                    int boundState = mBTDevice.getBondState();
                    bondStateChanged(boundState, mBTDevice);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.i(TAG, "Devices bondState changed");
                    int state = mBTDevice.getBondState();
                    bondStateChanged(state, mBTDevice);
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.i(TAG, "BluetoothAdapter connection state changed");
                    int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                            BluetoothProfile.STATE_CONNECTED);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    Log.i(TAG, "BluetoothAdapter  state changed");
                    int adapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    localAdapterStateChanged(adapterState);
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    Log.i(TAG, "BluetoothAdapter.ACTION_STATE_CHANGED");
                    int currentState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTING);
                    if (currentState == BluetoothProfile.STATE_CONNECTED) {//已连接
                        Log.i(TAG, "STATE_CONNECTED=" + mBTDevice.getName());
                        if (mListener != null) {
                            mListener.changItemState("已连接", mBTDevice);
                        }

                    } else if (currentState == BluetoothProfile.STATE_CONNECTING) {//正在连接
                        Log.i(TAG, "STATE_CONNECTING=" + mBTDevice.getName());
                        if (mListener != null) {
                            mListener.changItemState("正在连接", mBTDevice);
                        }
                    } else if (currentState == BluetoothProfile.STATE_DISCONNECTED) {//连接断开
                        Log.i(TAG, "STATE_DISCONNECTED=" + mBTDevice.getName());
                        if (mListener != null) {
                            mListener.changItemState("连接断开", mBTDevice);
                        }

                    }

                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.i(TAG, "BluetoothDevice.ACTION_ACL_CONNECTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.i(TAG, "BluetoothDevice.ACTION_ACL_DISCONNECTED");
                    break;
                default:
                    break;
            }
        }
    };


    public String getDeviceBoundType(int stateCode) {
        String state = "";
        switch (stateCode) {
            case BluetoothDevice.BOND_BONDING:
                state = "正在配对";
                break;
            case BluetoothDevice.BOND_BONDED:
                state = "已配对";
                break;
            case BluetoothDevice.BOND_NONE:
                state = "未配对";
                break;
            default:
                state = "未知状态";
                break;
        }
        return state;
    }

    public String getDeviceType(int code) {
        String type = "";
        switch (code) {
            case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                type = "COMPUTER_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.COMPUTER_DESKTOP:
                type = "COMPUTER_DESKTOP";
                break;
            case BluetoothClass.Device.COMPUTER_SERVER:
                type = "COMPUTER_SERVER";
                break;
            case BluetoothClass.Device.COMPUTER_LAPTOP:
                type = "COMPUTER_LAPTOP";
                break;
            case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                type = "COMPUTER_HANDHELD_PC_PDA";
                break;
            case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                type = "COMPUTER_PALM_SIZE_PC_PDA";
                break;
            case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                type = "PHONE_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.PHONE_CELLULAR:
                type = "PHONE_CELLULAR";
                break;
            case BluetoothClass.Device.PHONE_SMART:
                type = "PHONE_SMART";
                break;
            case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                type = "PHONE_MODEM_OR_GATEWAY";
                break;
            case BluetoothClass.Device.PHONE_CORDLESS:
                type = "PHONE_CORDLESS";
                break;
            case BluetoothClass.Device.PHONE_ISDN:
                type = "PHONE_ISDN";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                type = "AUDIO_VIDEO_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                type = "AUDIO_VIDEO_WEARABLE_HEADSET";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                type = "AUDIO_VIDEO_HANDSFREE";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                type = "AUDIO_VIDEO_LOUDSPEAKER";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                type = "AUDIO_VIDEO_HEADPHONES";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                type = "AUDIO_VIDEO_PORTABLE_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                type = "AUDIO_VIDEO_CAR_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                type = "AUDIO_VIDEO_SET_TOP_BOX";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                type = "AUDIO_VIDEO_HIFI_AUDIO";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                type = "AUDIO_VIDEO_VCR";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA:
                type = "AUDIO_VIDEO_VIDEO_CAMERA";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER:
                type = "AUDIO_VIDEO_CAMCORDER";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR:
                type = "AUDIO_VIDEO_VIDEO_MONITOR";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                type = "AUDIO_VIDEO_VIDEO_CONFERENCING";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY:
                type = "AUDIO_VIDEO_VIDEO_GAMING_TOY";
                break;
            case BluetoothClass.Device.WEARABLE_UNCATEGORIZED:
                type = "WEARABLE_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.WEARABLE_WRIST_WATCH:
                type = "WEARABLE_WRIST_WATCH";
                break;
            case BluetoothClass.Device.WEARABLE_JACKET:
                type = "WEARABLE_JACKET";
                break;
            case BluetoothClass.Device.WEARABLE_HELMET:
                type = "WEARABLE_HELMET";
                break;
            case BluetoothClass.Device.WEARABLE_PAGER:
                type = "WEARABLE_PAGER";
                break;
            case BluetoothClass.Device.TOY_UNCATEGORIZED:
                type = "TOY_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.TOY_ROBOT:
                type = "TOY_ROBOT";
                break;
            case BluetoothClass.Device.TOY_VEHICLE:
                type = "TOY_VEHICLE";
                break;
            case BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE:
                type = "TOY_DOLL_ACTION_FIGURE";
                break;
            case BluetoothClass.Device.TOY_CONTROLLER:
                type = "TOY_CONTROLLER";
                break;
            case BluetoothClass.Device.TOY_GAME:
                type = "TOY_GAME";
                break;
            case BluetoothClass.Device.HEALTH_UNCATEGORIZED:
                type = "HEALTH_UNCATEGORIZED";
                break;
            case BluetoothClass.Device.HEALTH_BLOOD_PRESSURE:
                type = "HEALTH_BLOOD_PRESSURE";
                break;
            case BluetoothClass.Device.HEALTH_THERMOMETER:
                type = "HEALTH_THERMOMETER";
                break;
            case BluetoothClass.Device.HEALTH_WEIGHING:
                type = "HEALTH_WEIGHING";
                break;
            case BluetoothClass.Device.HEALTH_GLUCOSE:
                type = "HEALTH_GLUCOSE";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_OXIMETER:
                type = "HEALTH_PULSE_OXIMETER";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_RATE:
                type = "HEALTH_PULSE_RATE";
                break;
            case BluetoothClass.Device.HEALTH_DATA_DISPLAY:
                type = "HEALTH_DATA_DISPLAY";
                break;
            case BluetoothClass.Device.PERIPHERAL_NON_KEYBOARD_NON_POINTING:
                type = "PERIPHERAL_NON_KEYBOARD_NON_POINTING";
                break;
            case BluetoothClass.Device.PERIPHERAL_POINTING:
                type = "PERIPHERAL_POINTING";
                break;
            case BluetoothClass.Device.PERIPHERAL_KEYBOARD_POINTING:
                type = "PERIPHERAL_KEYBOARD_POINTING";
                break;
            default:
                type = "OTHER_DEVICE_" + code;
                break;
        }


        return type;
    }

    public interface IBTListener {
        /**
         * 未绑定设备
         */
        void unBoundDevice(BluetoothDevice device);

        /**
         * 已绑定设备
         *
         * @param device
         */
        void boundDevice(BluetoothDevice device);

        /**
         * 绑定中设备
         *
         * @param device
         */
        void boundingDevice(BluetoothDevice device);

        void changItemState(String str, BluetoothDevice device);

    }

}
