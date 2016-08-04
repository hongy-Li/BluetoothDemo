package com.example.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.content.DialogInterface;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity  implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    public static final String TAG = "BluetoothActivity";
    // 该UUID表示串口服务
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    static final String SPP_UUID = "8ce255c0-200a-11e0-ac64-0800200c9a66";
//    static final String SPP_UUID = "a60f35f0-b93a-11de-8a39-08002009c666";


    ListView lv_devices;
    ListView lv_paireDevices;



    BluetoothAdapter bt_Adapt;
    public static BluetoothSocket bt_Socket;
    BluetoothDevice bluetooth_Device = null;
    private BluetoothA2dp mBTA2DP;
//    private BluetoothHeadset mBTH;

    Button bt_open;
    Button bt_search;
    Button bt_visible;
    BTAdapter mBTAdapter;
    BTAdapter mBTPaireAdapter;

    List<BluetoothDevice> mBTDevicesList = new ArrayList<BluetoothDevice>();//总设备
    List<BluetoothDevice> mBTDevicesUnBoundList = new ArrayList<BluetoothDevice>();//未绑定设备
    List<BluetoothDevice> mBTDevicesBoundList = new ArrayList<BluetoothDevice>();//绑定设备
    List<BluetoothDevice> mBTDevicesBoundingList = new ArrayList<BluetoothDevice>();//绑定中设备

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_open = (Button) findViewById(R.id.bt_open);
        bt_search = (Button) findViewById(R.id.bt_search);
        bt_visible = (Button) findViewById(R.id.bt_search);


        bt_open.setOnClickListener(mOnClickListener);
        bt_search.setOnClickListener(mOnClickListener);
        bt_visible.setOnClickListener(mOnClickListener);


        // ListView及其数据源 适配器
        lv_devices = (ListView) this.findViewById(R.id.lv_devices);
        lv_paireDevices = (ListView) this.findViewById(R.id.lv_paireDevices);

        //配对设备   包括已配对的和正在配对的
        mBTPaireAdapter=new BTAdapter(this,mBTDevicesList);
        lv_paireDevices.setAdapter(mBTPaireAdapter);

        //发现设备
        mBTAdapter = new BTAdapter(this, mBTDevicesUnBoundList);
        lv_devices.setAdapter(mBTAdapter);


        BleUtil.getInstance(this).setBTListener(mBTListener);

        lv_devices.setOnItemClickListener(this);
        lv_paireDevices.setOnItemClickListener(this);

        lv_devices.setOnItemLongClickListener(this);
        lv_paireDevices.setOnItemLongClickListener(this);

    }


    private void changeList() {
        mBTDevicesList.clear();
        mBTDevicesList.addAll(mBTDevicesBoundingList);
        mBTDevicesList.addAll(mBTDevicesBoundList);
//        mBTDevicesList.addAll(mBTDevicesUnBoundList);



        mBTPaireAdapter.notifyDataSetChanged();
        mBTAdapter.notifyDataSetChanged();
    }

    BleUtil.IBTListener mBTListener = new BleUtil.IBTListener() {
        @Override
        public void unBoundDevice(BluetoothDevice device) {
            Log.i(TAG,"unBoundDevice="+device.getName());
            //如果不包含该设备就添加
            if (!mBTDevicesUnBoundList.contains(device)) {
                mBTDevicesUnBoundList.add(device);
            }
            if (mBTDevicesBoundingList.contains(device)) {
                mBTDevicesBoundingList.remove(device);
            }
            if (mBTDevicesBoundList.contains(device)) {
                mBTDevicesBoundList.remove(device);
            }
            changeList();
        }

        @Override
        public void boundDevice(BluetoothDevice device) {
            Log.i(TAG,"boundDevice="+device.getName());
            if (!mBTDevicesBoundList.contains(device)) {
                mBTDevicesBoundList.add(device);

//               int index= mBTDevicesBoundList.indexOf(device);

            }
            if (mBTDevicesUnBoundList.contains(device)) {
                mBTDevicesUnBoundList.remove(device);
            }
            if (mBTDevicesBoundingList.contains(device)) {
                mBTDevicesBoundingList.remove(device);
            }
            changeList();
        }

        @Override
        public void boundingDevice(BluetoothDevice device) {
            Log.i(TAG,"boundingDevice="+device.getName());
            if (!mBTDevicesBoundingList.contains(device)) {
                mBTDevicesBoundingList.add(device);
            }
            if (mBTDevicesBoundList.contains(device)) {
                mBTDevicesUnBoundList.remove(device);
            }
            if (mBTDevicesUnBoundList.contains(device)) {
                mBTDevicesUnBoundList.remove(device);
            }
            changeList();
        }



        @Override
        public void changItemState(String str,BluetoothDevice device) {
            changeItemState(str,device);
        }
    };


    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_open:
                    BleUtil.getInstance(MainActivity.this).openBT();
                    break;
                case R.id.bt_search:
                    BleUtil.getInstance(MainActivity.this).searchDevice();
                    break;
                case R.id.bt_visible:
                    BleUtil.getInstance(MainActivity.this).visible(300);
                    break;

                default:
                    break;
            }
        }
    };

    //判断是否是蓝牙音箱或耳机设配的链接
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
//            if (mBTDevice != null) {
            Log.i(TAG, "onServiceConnected，profile is" + profile);
            try {
                if (profile == BluetoothProfile.HEADSET) {
                    mBTA2DP = (BluetoothA2dp) proxy;
                    mBTA2DP.connect(bluetooth_Device);
                } else if (profile == BluetoothProfile.A2DP) {
                    mBTA2DP = (BluetoothA2dp) proxy;
                    Log.i(TAG, "bluetooth_Device  is" + bluetooth_Device);
                    Log.i(TAG, "mBTA2DP.getConnectionState(bluetooth_Device)  is" + mBTA2DP.getConnectionState(bluetooth_Device));
                    if (mBTA2DP.getConnectionState(bluetooth_Device) != BluetoothProfile.STATE_CONNECTED && bluetooth_Device != null) {
                        mBTA2DP.connect(bluetooth_Device);
                    }
                }
                if (mBTA2DP != null) {
                    connectMessage(bluetooth_Device);

//                    if (adapter.isEnabled()) {
//                        searchParent.setVisibility(View.VISIBLE);
//                        searchDevice(true);
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i(TAG, "onServiceDisconnected，profile is" + profile);
            if (profile == BluetoothProfile.HEADSET) {
            }
            if (profile == BluetoothProfile.A2DP) {
                mBTA2DP = null;
            }
        }
    };

    public void connectMessage(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null)
            bluetooth_Device = bluetoothDevice;
        if (bluetoothDevice != null) {
            connectInt(bluetoothDevice);
        }

    }

    synchronized void connectInt(BluetoothDevice bluetoothDevice) {
        if (!ensurePaired(bluetoothDevice)) {
            return;
        }
        if (mBTA2DP.connect(bluetoothDevice)) {
            //会出现连接成功
            Log.d(TAG, "Command sent successfully:CONNECT " + bluetoothDevice.getAddress().toString());

            return;
        }
        Log.i(TAG, "Failed to connect " + mBTA2DP.toString() + " to " + bluetoothDevice.getName());
    }

    private boolean ensurePaired(BluetoothDevice bluetoothDevice) {
        Log.i(TAG, "bluetoothDevice====" + bluetoothDevice);
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            startPairing(bluetoothDevice);
            return false;
        } else {
            return true;
        }
    }

    boolean startPairing(BluetoothDevice device) {
        // Pairing is unreliable while scanning, so cancel discovery
        if (bt_Adapt.isDiscovering()) {
            bt_Adapt.cancelDiscovery();
        }

        if (!device.createBond()) {
            return false;
        }

        return true;
    }

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.i(TAG, "收到广播 action is " + action);


//            Bundle b = intent.getExtras();
//            Object[] lstName = b.keySet().toArray();
//
//            // 显示所有收到的消息及其细节
//            for (int i = 0; i < lstName.length; i++) {
//                String keyName = lstName[i].toString();
//                Log.e(keyName, String.valueOf(b.get(keyName)));
//            }


            String bluetoothDeviceName = "";

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "Discover Start");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //正在搜索
                Log.i(TAG, "Discover end");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 搜索设备时，取得设备的MAC地址
                bluetooth_Device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//存储设备信息
                bluetoothDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                if (bluetooth_Device.getBondState() == BluetoothDevice.BOND_NONE) {
                    String str = "未配对|" + bluetooth_Device.getName() + "|"
                            + bluetooth_Device.getAddress();
//                    if (lst_Devices.indexOf(str) == -1)// 防止地址被重复添加
//                        lst_Devices.add(str); // 获取设备名称和mac地址
//                    adt_Devices.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                bluetooth_Device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (bluetooth_Device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.i(TAG, "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.i(TAG, "完成配对");
//                        connect(bluetooth_Device);// 连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.i(TAG, "取消配对");
                    default:
                        break;
                }

            } else if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "连接状态改变");
                int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_CONNECTED);
                int oldState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_CONNECTED);

                Log.i(TAG, "newState is" + newState + " oldState is " + oldState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "连接成功");
                }
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "BluetoothAdapter连接状态改变");
                int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                        BluetoothProfile.STATE_CONNECTED);
                Log.i(TAG, "BluetoothAdapter newState is" + newState);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
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
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(TAG, "BluetoothDevice.ACTION_ACL_CONNECTED");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(TAG, "BluetoothDevice.ACTION_ACL_DISCONNECTED");
            }

        }
    };

    @Override
    protected void onDestroy() {
//        this.unregisterReceiver(searchDevices);
        super.onDestroy();
        BleUtil.getInstance(this).unRegist();
        android.os.Process.killProcess(android.os.Process.myPid());
    }




    View mClickItemView=null;
    int mClickPosition;
    /**
     * listView 的点击事件
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mClickItemView=view;
        mClickPosition=i;
        BluetoothDevice device=null;
            switch (adapterView.getId()){
                case R.id.lv_paireDevices://已配对设备
                    device=mBTDevicesList.get(i);
                    break;
                case R.id.lv_devices://已发现设备
                    device=mBTDevicesUnBoundList.get(i);
                    break;

            }
            BleUtil.getInstance(this).paireToDevice(device);

    }
    public void changeItemState(String str,BluetoothDevice device){
        if(device==null){
            return;
        }
        int position=mBTDevicesBoundList.indexOf(device);
        if(position!=-1){
        View view=lv_paireDevices.getChildAt(position);
        Log.i(TAG,"changeItemState,str="+str);
        if(view==null){
            return;
        }
      final   BTAdapter.ViewHolder holder= (BTAdapter.ViewHolder) view.getTag();
        holder.tv_state.setText(str);
        //TODO 如果是连接断开就需要延迟n秒时间回复原状
        if("连接断开".equals(str)){
            holder.tv_state.postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.tv_state.setText("已配对");
                }
            },1500);
        }
        }
    }

    /**
     * listView的长按点击事件
     * @param adapterView
     * @param view
     * @param i
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.lv_paireDevices://长按已配对的设备，提示是否取消配对
                cancelPaireDialog(i);
                break;
        }

        return true;
    }
    private void cancelPaireDialog(final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage("是否取消配对？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG,"确定");
                BleUtil.getInstance(MainActivity.this).cancelParire(mBTDevicesList.get(position));
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG,"取消");
            }
        });
        builder.create();
        builder.show();
    }




    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
//            if (bt_Adapt.isDiscovering())
//                bt_Adapt.cancelDiscovery();
//            String str = lst_Devices.get(arg2);
//            Log.i("str is ",str);
//            String[] values = str.split("\\|");
//            String address = values[2];
//            Log.e("address", values[2]);
//            BluetoothDevice btDev = bt_Adapt.getRemoteDevice(address);
//            try {
//                Boolean returnValue = false;
//                if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
//                    // 利用反射方法调用BluetoothDevice.createBond(BluetoothDevice
//                    // remoteDevice);
//                    Method createBondMethod = BluetoothDevice.class
//                            .getMethod("createBond");
//                    Log.i(TAG, "开始配对");
//                    returnValue = (Boolean) createBondMethod.invoke(btDev);
//
//                } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
//                    connect(btDev);
//
////                    connectMessage(btDev);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

        }

        private BluetoothSocket transferSocket;

        private void connectToServerSocket(BluetoothDevice device, UUID uuid) {
            try {
                BluetoothSocket clientSocket
                        = device.createRfcommSocketToServiceRecord(uuid);

                // Block until server connection accepted.
                clientSocket.connect();

                // Start listening for messages.
                StringBuilder incoming = new StringBuilder();
                listenForMessages(clientSocket, incoming);

                // Add a reference to the socket used to send messages.
                transferSocket = clientSocket;

            } catch (IOException e) {
                Log.e("BLUETOOTH", "Blueooth client I/O Exception", e);
            }
        }


        private void sendMessage(BluetoothSocket socket, String message) {
            OutputStream outStream;
            try {
                outStream = socket.getOutputStream();

                // Add a stop character.
                byte[] byteArray = (message + " ").getBytes();
                byteArray[byteArray.length - 1] = 0;

                outStream.write(byteArray);
            } catch (IOException e) {
                Log.e("MainActivity", "Message send failed.", e);
            }
        }


        /**
         * 处理消息
         *
         * @param socket
         * @param incoming
         */
        private boolean listening = false;

        private void listenForMessages(BluetoothSocket socket,
                                       StringBuilder incoming) {
            listening = true;


            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            try {
                InputStream instream = socket.getInputStream();
                int bytesRead = -1;

                while (listening) {
                    bytesRead = instream.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) &&
                                (buffer[bufferSize - 1] != 0)) {
                            result = result + new String(buffer, 0, bytesRead - 1);
                            bytesRead = instream.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead - 1);
                        incoming.append(result);
                    }
                    socket.close();
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Message received failed.", e);
            } finally {
            }
        }

        private void connect(final BluetoothDevice btDev) {

            UUID uuid = UUID.fromString(SPP_UUID);

            bt_Adapt.cancelDiscovery();


            try {
                bt_Socket = btDev.createRfcommSocketToServiceRecord(uuid);
                Log.i(TAG, "开始连接...");
                bt_Socket.connect();
                Log.i(TAG, "连接成功...");


            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                try {
                    Log.i(TAG, "trying fallback...");


                    bt_Socket = (BluetoothSocket) btDev
                            .getClass()
                            .getMethod("createRfcommSocket",
                                    new Class[]{int.class}).invoke(btDev, 2);
                    bt_Socket.connect();

                    Log.i(TAG, "Connected");
                    sendMessage(bt_Socket, "i am client");
                } catch (Exception e2) {
                    Log.i(TAG,
                            "Couldn't establish Bluetooth connection!" + e.getMessage());
                }

                e.printStackTrace();
            }


        }

//    class ClickEvent implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            if (v == btn_Search)// 搜索蓝牙设备，在BroadcastReceiver显示结果
//            {
//
//
//                if (bt_Adapt.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启
//                    Toast.makeText(MainActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (bt_Adapt.isDiscovering())
//                    bt_Adapt.cancelDiscovery();
//                lst_Devices.clear();
//
//                Object[] lstDevice = bt_Adapt.getBondedDevices().toArray();
//                for (int i = 0; i < lstDevice.length; i++) {
//                    BluetoothDevice device = (BluetoothDevice) lstDevice[i];
//                    String str = "已配对|" + device.getName() + "|"
//                            + device.getAddress();
//                    lst_Devices.add(str); // 获取设备名称和mac地址
//                    adt_Devices.notifyDataSetChanged();
//                }
//                setTitle("本机地址：" + bt_Adapt.getAddress());
//
//                bt_Adapt.startDiscovery();
//
//            } else if (v == btn_Dis)// 本机可以被搜索
//            {
//                Intent discoverableIntent = new Intent(
//                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(
//                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
//            } else if (v == btn_Exit) {
//                try {
//                    if (bt_Socket != null)
//                        bt_Socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                finish();
//            }
//        }

    }

}
