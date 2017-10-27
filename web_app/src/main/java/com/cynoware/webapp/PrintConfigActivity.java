package com.cynoware.webapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


public class PrintConfigActivity extends Activity {

    private static final String TAG = "USB_HOST";
    private UsbManager myUsbManager;
    private UsbDevice mDevice = null;
    private TextView mTvName, mTvDetail;
    private Button mBtnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCollector.addActivity(this,PrintConfigActivity.class);
        SharePreferenceUtil.getInstance().init(this);

        myUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        setContentView(R.layout.activity_printer_config);

        mTvName = (TextView) findViewById(R.id.tvName);
        mTvDetail = (TextView) findViewById(R.id.tvDetail);

        mBtnOK = (Button) findViewById(R.id.btnOK);
        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeConfigs();
                finish();
            }
        });

        int count = enumerateDevice();

        if (count > 1) {
            Toast.makeText(this, "侦测到多个USB设备，请卸载除打印机以外的设备", Toast.LENGTH_LONG).show();
        }

        showDevice();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(mBroadcastReceiver, filter);
    }


    private int enumerateDevice() {
        if (myUsbManager == null)
            return 0;

        int count = 0;
        HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            for (UsbDevice device : deviceList.values()) {
                if (device.getProductId() == 1828 &&
                        device.getVendorId() == 3034){
                    continue;
                }
                mDevice = device;
                count++;
            }
        }

        return count;
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.i(TAG, "USB Detached - " + device);
                    mDevice = device;
                    showDevice();
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.i(TAG, "USB Detached - " + device);
                    if (mDevice != null && device != null) {
                        if (device.getDeviceName().equals(mDevice.getDeviceName())) {
                            mDevice = null;
                            showDevice();
                        }
                    }

                }
            }
        }
    };


    private void showDevice() {
        if (mDevice == null) {
            mTvName.setText("等待USB打印机连接");
            mTvDetail.setVisibility(View.INVISIBLE);
            mBtnOK.setVisibility(View.INVISIBLE);
        } else {

            String productName = mDevice.getProductName();
            if (productName == null || productName.isEmpty())
                productName = "不明设备";

            mTvName.setText(productName);

            String text = "";
            if (mDevice.getManufacturerName() != null)
                text += mDevice.getManufacturerName();

            text += " VID:" + mDevice.getVendorId();
            text += " PID:" + mDevice.getProductId();

            mTvDetail.setText(text);
            mTvDetail.setVisibility(View.VISIBLE);
            mBtnOK.setVisibility(View.VISIBLE);
        }
    }


    private void writeConfigs() {

        if (mDevice == null)
            return;

        SharePreferenceUtil.getInstance().putInt(PrintConstants.KEY_SP_PID, mDevice.getProductId());
        SharePreferenceUtil.getInstance().putInt(PrintConstants.KEY_SP_VID, mDevice.getVendorId());
        Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
