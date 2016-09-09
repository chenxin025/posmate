package com.cynoware.posmate.sdk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ServiceConnection;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;

public class BleDevice extends Device {
    private static final String TAG = "BLEDevice";


    public static final String ACTION_BT_SCANNED = "com.cynoware.posmate.BT_SCANNED";
    public static final String ACTION_BT_ERROR = "com.cynoware.posmate.BT_ERROR";
    public static final String ACTION_BT_CONNECTED = "com.cynoware.postmate.BT_CONNECTED";

    public static final String BT_DEVICE_NAME = "BT_DEVICE_NAME";
    public static final String BT_DEVICE_ADDRESS = "BT_DEVICE_ADDRESS";
    public static final String BT_ERROR_INFO = "BT_ERROR_INFO";

    //private int request_enable_bt_;
    //private String prefixName_;

    private final BluetoothManager mBTManager;
    private final BluetoothAdapter mBTAdapter;
    
    private BluetoothLeService mBleService = null;
    
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    // Selected device address
    // Set it's value to null if device is disconnected
    private String mBTAddr = null;
    
    Context mContext = null;
    boolean mIsBroken = false;

    BluetoothGattCharacteristic characteristic6_ = null;    //From android to device
    BluetoothGattCharacteristic characteristic7_ = null;    //From device to android

    byte[] eventData = new byte[cmds.MAX_EVENT_SIZE];
    byte[] hidCmdOut = new byte[cmds.MAX_CMD_SIZE];
    byte[] uartCmdOut = new byte[cmds.UART_CMD_MAX_SIZE];
    
    boolean isCmdLocked = false;
    byte readCmd_ = 0;
    byte writeCmd_ = 0;
    int status = uart_cmd.ST_INIT;
    
    //List<BluetoothDevice> pmdevices;
    //BluetoothDevice paredDevice = null;

    public boolean hasBleSDK(){
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
    

    @android.annotation.SuppressLint("NewApi")
    public boolean connect(String deviceAddress) {
        Log.i(TAG, "connect " + deviceAddress );
        
        if(mLeScanCallback != null) {
            mBTAdapter.stopLeScan(mLeScanCallback);
            mLeScanCallback = null;
        }

        //if(mBTAddr != null && deviceAddress.equals(deviceAddress))
        //    return true;    //Already selected
        
        mBTAddr = deviceAddress;


        Log.i(TAG,"mBleService ====="+mBleService);
        if(mBleService != null)
            mBleService.connect(mBTAddr);
        /* ... */
        return true;
    }

    public void onResume() {
        Log.i(TAG, "onResume");
        /*final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        mContext.registerReceiver(mGattUpdateReceiver, intentFilter);*/

        if (mBleService != null && mBTAddr != null) {
            final boolean result = mBleService.connect(mBTAddr);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    public void onPause() {
        Log.i(TAG, "onPause");
        //mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    public void onCreate(){
        Log.i(TAG, "onCreate");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        mContext.registerReceiver(mGattUpdateReceiver, intentFilter);
        /* Start device IO now */
        createBleService();        
    }
        
 
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        mContext.unbindService(mServiceConnection);
        mContext.unregisterReceiver(mGattUpdateReceiver);
        if (null != mBleService) {
            mBleService.close();
        }
        mBleService = null;
    }
    
    private void createBleService(){
    	Intent intent = new Intent(mContext, BluetoothLeService.class);
        mContext.bindService(intent, mServiceConnection, Activity.BIND_AUTO_CREATE);
      //mContext.startService(gattServiceIntent);
    }

    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogUtil.i(TAG, "===================onServiceConnected====================");
            mBleService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBleService.initialize(BleDevice.this, mContext)) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                Intent intent = new Intent(ACTION_BT_ERROR);
                intent.putExtra(BT_ERROR_INFO, "Unable to initialize Bluetooth");
                mContext.sendBroadcast(intent);
            }
            // Automatically connects to the device upon successful start-up initialization.
            if(mBTAddr != null)
                mBleService.connect(mBTAddr);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.i(TAG, "======================onServiceDisconnected================");
            mBleService  = null;
        }
    };


    /*private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics_ =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.i(TAG, "displayGattServices");
        if (gattServices == null) return;
        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        gattCharacteristics_ = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.i(TAG, String.format("service uuid = " + uuid));

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            //ArrayList<BluetoothGattCharacteristic> charas =
            //        new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                Log.i(TAG, String.format("char uuid = " + uuid));

            }
        }
    }*/

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) return;

            final String action = intent.getAction();
            Log.i(TAG, "BroadcastReceiver onReceive: " + action);
            //mContext.setStatus(action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBleService.getSupportedGattServices());

                //Test with our interested service and acharacteristic
                BluetoothGattService service = null;
                characteristic6_ = null;
                characteristic7_ = null;

                if (null == mBleService || null == mBleService.mBluetoothGatt){
                    return;
                }
                service = mBleService.mBluetoothGatt.getService(BluetoothLeService.UUID_POSMATE_SERVICE);
                Log.i(TAG, String.format("BondState = %d", mBleService.mBluetoothGatt.getDevice().getBondState()));
                if(service != null) {
                    characteristic6_ = service.getCharacteristic(BluetoothLeService.UUID_POSMATE_TX);
                    characteristic7_ = service.getCharacteristic(BluetoothLeService.UUID_POSMATE_RX);
                }

                if(characteristic7_ != null){
                    mBleService.setCharacteristicNotification(characteristic7_, true);
                }

                //Intent intentConnected = new Intent(ACTION_BT_CONNECTED);
                //mContext.sendBroadcast(intentConnected);

                //if(characteristic6_ != null) {
                    //mBleService.mBluetoothGatt.beginReliableWrite();
                    //characteristic6_.setValue(new byte[]{0x01, 0x02, 0x03, 0x04,
                    //        0x05, 0x06, 0x07, 0x08,
                    //        0x09, 0x0A, 0x0B, 0x0C,
                    //        0x0D, 0x0E, 0x0F
                    //});
                    //boolean res = mBleService.mBluetoothGatt.writeCharacteristic(characteristic6);
                    //Log.i(TAG, (res ? "true" : "false"));
                    //mBleService.mBluetoothGatt.executeReliableWrite();

                //if(characteristic != null){
                //    mBleService.readCharacteristic(characteristic);
                //}

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    /*public void testWrite(){
        BluetoothGattService service = null;
        BluetoothGattCharacteristic characteristic6 = null;
        service = mBleService.mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
        if(service != null)
            characteristic6 = service.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));

        if(characteristic6 != null) {
            int i = (int)(Math.random() * 32);
            characteristic6.setValue(new byte[]{
                    (byte)(0x01 + i), (byte)(0x02 + i), (byte)(0x03 + i), (byte)(0x04 + i),
                    (byte)(0x05 + i), (byte)(0x06 + i), (byte)(0x07 + i), (byte)(0x08 + i),
                    (byte)(0x09 + i), (byte)(0x0A + i), (byte)(0x0B + i), (byte)(0x0C + i),
                    (byte)(0x0D + i), (byte)(0x0E + i), (byte)(0x0F + i)
            });
            boolean res = mBleService.mBluetoothGatt.writeCharacteristic(characteristic6);
            Log.i(TAG, String.format("res = " + (res ? "true" : "false")));
        }
    }*/


    
    @SuppressLint("NewApi")
	public BleDevice(Context context) {
        super();
        mContext = context;
        mIsBroken = false;
        mBTManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = mBTManager.getAdapter();
    }

    
    
	/*private boolean findDevice(int request_enable_bt){
        Log.i(TAG, "findDevice");
        request_enable_bt_ = request_enable_bt;
        bleRequestDevice("PM");
        return false;
    }*/

    public int writeData(byte[] data, int length){
        if(mIsBroken) return -1;
        int res = 0;

        while(isCmdLocked && !mIsBroken && mBleService != null)
            this.deviceWait();

        if(mBleService != null) {
            writeCmd_ = data[1];
            readCmd_ = 0;
            res = mBleService.writeCommand(data, length);
            mBleService.writeFlush(characteristic6_);
        }
        return res;
    }
    public int readData(byte[] data){
        if(mIsBroken) return -1;
        int res = 0;
        if(mBleService != null) {
            res = mBleService.readCommand();
            mBleService.writeFlush(characteristic6_);

            if(res < 4){
                res = -1;
                mIsBroken = true;
                return res;
            }

            while(readCmd_ != writeCmd_ && !mIsBroken && mBleService != null){
                Log.i(TAG, String.format("cmd = %02x %02x", readCmd_, writeCmd_));
                isCmdLocked = true;   //Lock the command and waiting RX thread
                this.deviceWait();
                isCmdLocked = false;  //Unlock the command
            }
            Log.i(TAG, "read OK ****************");

            if(mIsBroken)
                return -1;

            if(readCmd_ == writeCmd_){
                int size = Math.max(data.length, hidCmdOut.length);
                System.arraycopy(hidCmdOut, 0, data, 0, size);
                res = size;
            }
            if(res < 0){
                mIsBroken = true;
            }
            return res;

        }
        return res;
    }
    public int waitEvent(byte[] data){
        return -1;
    }

    public boolean isOpened(){
        return characteristic6_ != null && characteristic7_ != null;
    }
    
    
    /* 检查连接状态 */
    public boolean isBroken(){
        return false;
    }

    public void disConnectWithBle(){
        if (null == mBleService){
            return;
        }
        mBleService.disconnect();
    }

    // TODO
    public void close(){
        if (mBleService == null) {
            return;
        }

        mBleService.close();
        //mBleService = null;
    }

    
    /**
     * Search BT device named PM.
     */
    /*public void detectBt(int request_enable_bt){
        findDevice(request_enable_bt);
    }*/

    /*public boolean openBt(String deviceAddress){
        return selectDevice(deviceAddress);
    }*/

    public boolean isConnected(){
        return isOpened() && !isBroken();
    }

};
