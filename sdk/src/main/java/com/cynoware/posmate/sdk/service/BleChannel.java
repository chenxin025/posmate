package com.cynoware.posmate.sdk.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.cmd.Cmds;
import com.cynoware.posmate.sdk.cmd.UartCmd;
import com.cynoware.posmate.sdk.io.BleDevice;
import com.cynoware.posmate.sdk.util.Utils;

import java.util.UUID;

/**
 * Created by john on 2017/1/24.
 */

public class BleChannel {

    private static final String TAG = "BleChannel";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;

    public final static int MAX_BLE_CHAR_LEN = 15;
    private boolean isBuffer6Wrting_ = false;
    private Buf buffer6_ = new Buf(64);
    private Buf buffer7_ = new Buf(128);
    private BleDevice mBleDevice = null;
    private OnStatusListener mOnStatusListener = null;

    BluetoothGattCharacteristic characteristic6_ = null;    //From android to device
    BluetoothGattCharacteristic characteristic7_ = null;    //From device to android

    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_SERVICE =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_TX =
            UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_RX =
            UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");

    public static BleChannel sBleStatus = null;
    private Context mContext = null;

    public BleChannel(BleDevice device, Context context){
        mBleDevice = device;
        mContext = context;
    }

    public static BleChannel getInstance(BleDevice device, Context context){
        if (sBleStatus == null){
            sBleStatus = new BleChannel(device, context);
        }
        return sBleStatus;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                //TODO Connected is succeful
                if (mOnStatusListener != null){
                    mOnStatusListener.onBleAttached();
                }

            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                if (mOnStatusListener != null){
                    mOnStatusListener.onBleDetached();
                }
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                initDiscovered();
                //TODO  Need callBack
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            isBuffer6Wrting_ = false;
            writeFlush(characteristic);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            String uuid = characteristic.getUuid().toString();
            Log.i(TAG, uuid);
            if(UUID_POSMATE_RX.equals(characteristic.getUuid())){
                byte[] value = characteristic.getValue();
                String s = "";
                for(int i = 0; i < value.length; ++i){
                    s += String.format("%02x ", value[i]);
                }
                SDKLog.i(TAG, "===========onCharacteristicChanged=========="+s);

                if(value[0] >= MAX_BLE_CHAR_LEN)
                    value[0] = MAX_BLE_CHAR_LEN - 1;
                buffer7_.pushBack(value, 1, value[0]);
                UartCmd.parseUartData(mBleDevice, buffer7_);
            }

            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, String.format("=== BleService.onCharacteristicWrite status = %d", status));

            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    public void writeFlush(BluetoothGattCharacteristic characteristic){
        byte[] data = new byte[MAX_BLE_CHAR_LEN];
        int len = 0;
        synchronized (this) {
            if(!isBuffer6Wrting_) {
                len = (byte) buffer6_.popFront(data, 1, data.length - 1);
                if (len > 0)
                    isBuffer6Wrting_ = true;
            }
        }

        if(len > 0){
            data[0] = (byte)len;
            characteristic.setValue(data);
            Log.i(TAG, String.format("writeFlush len = %d", len));
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public int writeCommand(byte[] data, int length){
        Log.i(TAG, String.format("writeCommand %02x %02x %02x %02x", data[0], data[1], data[2], data[3]));
        int res;
        byte[] head = new byte[4];
        synchronized (this){
            //Append data to writing pool
            head[0] = (byte) Cmds.UART_CMD_WRITE_HEAD;
            head[1] = (byte)length;
            int cksum = Utils.cksum(data, 0, length);
            Utils.short2byte_le(cksum, head, 2);
            res = buffer6_.pushBack(head, 0, head.length);
            res = buffer6_.pushBack(data, 0, length);
        }
        return res;
    }



    public int readCommand(){
        Log.i(TAG, "readCommand");
        int res;
        byte[] head = new byte[4];
        synchronized (this){
            //Append data to writing pool
            head[0] = (byte)Cmds.UART_CMD_READ_HEAD;
            head[1] = 0;
            head[2] = 0;
            head[3] = 0;
            res = buffer6_.pushBack(head, 0, head.length);
        }
        return res;
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(BleDevice bleDevice, Context context ) {
        Log.i(TAG, "BluetoothLeService.initialize");
        mBleDevice = bleDevice;
        mContext = context;
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    public boolean connect(final String address,OnStatusListener listener) {
        Log.i(TAG, "BluetoothLeService.connect");
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                //mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.i(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        return true;
    }



    public boolean connect(final BluetoothDevice device) {
        Log.i(TAG, "BluetoothLeService.connect");
        if (mBluetoothAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified device.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && device.getAddress().equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
               // mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = device.getAddress();
        //mConnectionState = STATE_CONNECTING;
        return true;
    }


    public void disConnect() {
        Log.i(TAG, "BluetoothLeService.disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        Log.i(TAG, "BluetoothLeService.close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //Discovered
    private void initDiscovered(){
        BluetoothGattService service = null;
        characteristic6_ = null;
        characteristic7_ = null;

        if (null == mBluetoothGatt){
            return;
        }
        service = mBluetoothGatt.getService(BleChannel.UUID_POSMATE_SERVICE);
        Log.i(TAG, String.format("BondState = %d", mBluetoothGatt.getDevice().getBondState()));
        if(service != null) {
            characteristic6_ = service.getCharacteristic(BleChannel.UUID_POSMATE_TX);
            characteristic7_ = service.getCharacteristic(BleChannel.UUID_POSMATE_RX);
        }

        if(characteristic7_ != null){
            setCharacteristicNotification(characteristic7_, true);
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        if(descriptor == null) return;
        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] { 0x00, 0x00 });
        mBluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
    }

}
