package com.cynoware.posmate.sdk.io;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.cmd.Cmds;
import com.cynoware.posmate.sdk.service.BleChannel;
import com.cynoware.posmate.sdk.cmd.UartCmd;
import com.cynoware.posmate.sdk.service.OnStatusListener;

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

    private boolean mConnected = false;
    private final BluetoothManager mBTManager;
    private final BluetoothAdapter mBTAdapter;
    
    private BleChannel mBleChannel = null;
    
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    // Selected device address
    // Set it's value to null if device is disconnected
    private String mBTAddr = null;
    
    Context mContext = null;
    boolean mIsBroken = false;

    BluetoothGattCharacteristic characteristic6_ = null;    //From android to device
    BluetoothGattCharacteristic characteristic7_ = null;    //From device to android

    public byte[] eventData = new byte[Cmds.MAX_EVENT_SIZE];
    public byte[] hidCmdOut = new byte[Cmds.MAX_CMD_SIZE];
    public byte[] uartCmdOut = new byte[Cmds.UART_CMD_MAX_SIZE];

    public boolean isCmdLocked = false;
    public byte readCmd_ = 0;
    public byte writeCmd_ = 0;
    public int status = UartCmd.ST_INIT;
    

    public BleDevice(Context context) {
        super();
        mContext = context;
        mIsBroken = false;
        mBTManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = mBTManager.getAdapter();

        initIntents();
    }

    public boolean hasBleSDK(){
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
    

    @SuppressLint("NewApi")
    public boolean connect(String deviceAddress, OnStatusListener listener) {
        Log.i(TAG, "connect " + deviceAddress );
        
        if(mLeScanCallback != null) {
            mBTAdapter.stopLeScan(mLeScanCallback);
            mLeScanCallback = null;
        }
        mBTAddr = deviceAddress;
        Log.i(TAG,"mBleChannel ====="+ mBleChannel);
        if(mBleChannel != null)
            mBleChannel.connect(mBTAddr,listener);

        return true;
    }

    public void onResume() {
        Log.i(TAG, "onResume");
        /*final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        mContext.registerReceiver(mGattUpdateReceiver, intentFilter);*/

//        if (mBleChannel != null && mBTAddr != null) {
//            final boolean result = mBleChannel.connect(mBTAddr);
//            Log.i(TAG, "Connect request result=" + result);
//        }
    }

    public void initIntents(){
        Log.i(TAG, "onCreate");

        mBleChannel = BleChannel.getInstance(BleDevice.this,mContext);
        /* Start device IO now */
        //createBleService();
    }
        
 
    public void onDestroy(){
        Log.i(TAG, "onDestroy");

        if (null != mBleChannel) {
            mBleChannel.close();
        }
        mBleChannel = null;
    }
    


    



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



    /*public void testWrite(){
        BluetoothGattService service = null;
        BluetoothGattCharacteristic characteristic6 = null;
        service = mBleChannel.mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
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
            boolean res = mBleChannel.mBluetoothGatt.writeCharacteristic(characteristic6);
            Log.i(TAG, String.format("res = " + (res ? "true" : "false")));
        }
    }*/


    


    
    
	/*private boolean findDevice(int request_enable_bt){
        Log.i(TAG, "findDevice");
        request_enable_bt_ = request_enable_bt;
        bleRequestDevice("PM");
        return false;
    }*/

    public int writeData(byte[] data, int length){
        if(mIsBroken) return -1;
        int res = 0;

        while(isCmdLocked && !mIsBroken && mBleChannel != null)
            this.deviceWait();

        if(mBleChannel != null) {
            writeCmd_ = data[1];
            readCmd_ = 0;
            res = mBleChannel.writeCommand(data, length);
            mBleChannel.writeFlush(characteristic6_);
        }
        return res;
    }
    public int readData(byte[] data){
        if(mIsBroken) return -1;
        int res = 0;
        if(mBleChannel != null) {
            res = mBleChannel.readCommand();
            mBleChannel.writeFlush(characteristic6_);

            if(res < 4){
                res = -1;
                mIsBroken = true;
                return res;
            }

            while(readCmd_ != writeCmd_ && !mIsBroken && mBleChannel != null){
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
    public boolean isHaveConnect(){
        return mConnected;
    }


    public void close(){
        if (mBleChannel == null) {
            return;
        }

        mBleChannel.close();
        //mBleChannel = null;
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
        return isOpened() && isHaveConnect();
    }

    @Override
    public boolean isBroken() {
        return false;
    }
};
