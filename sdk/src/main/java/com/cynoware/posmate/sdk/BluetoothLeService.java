/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cynoware.posmate.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
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
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private final static String TAG = "BLEService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_SERVICE =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_TX =
            UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_POSMATE_RX =
            UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");

    public final static int MAX_BLE_CHAR_LEN = 15;
    BleDevice mBleDevice = null;
    boolean isBuffer6Wrting_ = false;
    buf buffer6_ = new buf(64);
    buf buffer7_ = new buf(128);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange:" + newState );
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "=== BluetoothLeService.onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "=== BluetoothLeService.onCharacteristicRead");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "=== BluetoothLeService.onCharacteristicChanged");
            String uuid = characteristic.getUuid().toString();
            Log.i(TAG, uuid);
            if(UUID_POSMATE_RX.equals(characteristic.getUuid())){
                byte[] value = characteristic.getValue();
                String s = "";
                for(int i = 0; i < value.length; ++i){
                    s += String.format("%02x ", value[i]);
                }
                Log.i(TAG, s);

                if(value[0] >= MAX_BLE_CHAR_LEN)
                    value[0] = MAX_BLE_CHAR_LEN - 1;
                buffer7_.pushBack(value, 1, value[0]);
                uart_cmd.parseUartData(mBleDevice, buffer7_);
            }
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status){
            Log.i(TAG, String.format("=== BluetoothLeService.onCharacteristicWrite status = %d", status));
            isBuffer6Wrting_ = false;
            writeFlush(characteristic);
            //Log.i(TAG, (res ? "true" : "false"));
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.i(TAG, "=== BluetoothLeService.onDescriptorRead");
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.i(TAG, "=== BluetoothLeService.onDescriptorWrite");
            if(mBleDevice != null) {
                Intent intentConnected = new Intent(mBleDevice.ACTION_BT_CONNECTED);
                mContext.sendBroadcast(intentConnected);
            }
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i(TAG, "=== BluetoothLeService.onReliableWriteCompleted");
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG, "=== BluetoothLeService.onReadRemoteRssi");
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        /*@Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i(TAG, "=== BluetoothLeService.onMtuChanged");
            super.onMtuChanged(gatt, mtu, status);
        }*/

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
            head[0] = (byte)cmds.UART_CMD_WRITE_HEAD;
            head[1] = (byte)length;
            int cksum = util.cksum(data, 0, length);
            util.short2byte_le(cksum, head, 2);
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
            head[0] = (byte)cmds.UART_CMD_READ_HEAD;
            head[1] = 0;
            head[2] = 0;
            head[3] = 0;
            res = buffer6_.pushBack(head, 0, head.length);
        }
        return res;
    }

    private void broadcastUpdate(final String action) {
        Log.i(TAG, "BluetoothLeService.broadcastUpdate1");
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "BluetoothLeService.broadcastUpdate2");
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();
    private Context mContext;

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
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        Log.i(TAG, "BluetoothLeService.connect");
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
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
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = device.getAddress();
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        Log.i(TAG, "BluetoothLeService.disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.i(TAG, "BluetoothLeService.close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
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

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
