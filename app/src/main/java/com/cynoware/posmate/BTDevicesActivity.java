/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;


import java.lang.reflect.Method;
import java.util.Set;

import com.cynoware.posmate.R;
import com.cynoware.posmate.sdk.io.BleDevice;
import com.cynoware.posmate.sdk.io.UsbHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BTDevicesActivity extends Activity {

	public static final int REQUEST_ENABLE_BT = 102;
	
	private BluetoothAdapter mBTAdapter;
	private BTDevicesAdapter mDevPairedAdapter;
	private BTDevicesAdapter mDevNewAdapter;
	private String mChannel;
	
	
	/*private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING:
                        Log.d("BlueToothTestActivity", "it is pairing");
                        break;
                case BluetoothDevice.BOND_BONDED:
                        Log.d("BlueToothTestActivity", "finish");
                        //connect(device);
                        break;
                case BluetoothDevice.BOND_NONE:
                        Log.d("BlueToothTestActivity", "cancel");
                default:
                        break;
                }
            }
        }
    };
    
    
    private void registerBroadcastReceiver(){
    	IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }*/

	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ( action.equals(BluetoothDevice.ACTION_FOUND)) {

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mDevNewAdapter.add(device, true);
				}
			}else if( action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ){
				BTDevicesActivity.this.setTitle("Devices");
			}

		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setTitle("Searching device...");
		
		Intent intent = this.getIntent();
		mChannel = intent.getStringExtra("channel");
		
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// registerBroadcastReceiver();
		
		setContentView(R.layout.activity_bt_devices);
		
		// Initialize paired devices' list
		ListView lvPairedDev = (ListView)findViewById(R.id.lvPairedDevices);
		mDevPairedAdapter = new BTDevicesAdapter(this);
		lvPairedDev.setAdapter(mDevPairedAdapter);
		mDevPairedAdapter.setOnItemClickListener( new BTDevicesAdapter.OnItemClickListener() {

			@Override
			public void onClick(int position) {
				BluetoothDevice item = (BluetoothDevice) mDevPairedAdapter.getItem(position);
				unpairDevice( item );
				mDevPairedAdapter.removeItem(position, true);
			}			
		});
		
		lvPairedDev.setOnItemClickListener( new OnItemClickListener(){
			@SuppressLint("NewApi") @Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
				BluetoothDevice item = (BluetoothDevice) mDevPairedAdapter.getItem(position);
				
				Intent intent = new Intent();
				String name = item.getName();
				String addr = item.getAddress();
				
				if( name == null || name.isEmpty() )
					name = addr;
				
				intent.putExtra( "name", name );
				intent.putExtra( "addr", addr );
				intent.putExtra( "channel", mChannel );
				
				setResult( Activity.RESULT_OK, intent );
				finish();				
			}});

		// Initialize new devices’ list
		ListView lvNewDev = (ListView)findViewById(R.id.lvNewDevices);
		mDevNewAdapter = new BTDevicesAdapter(this, false);
		lvNewDev.setAdapter(mDevNewAdapter);
		
		lvNewDev.setOnItemClickListener( new OnItemClickListener(){
			@SuppressLint("NewApi") @Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
				BluetoothDevice item = (BluetoothDevice) mDevNewAdapter.getItem(position);
				
				//boolean ret = item.createBond();
				
				Intent intent = new Intent();
				String name = item.getName();
				String addr = item.getAddress();
				
				if( name == null || name.isEmpty() )
					name = addr;
				
				intent.putExtra( "name", name );
				intent.putExtra( "addr", addr );
				intent.putExtra( "channel", mChannel );
				
				setResult( Activity.RESULT_OK, intent );
				finish();
			}});
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		
		initPairedDeviceList();
		
		if( mBTAdapter.isEnabled() )
			mBTAdapter.startDiscovery();
		else{
			if (!mBTAdapter.isEnabled()) { 
			   Intent enableIntent = new Intent(  BluetoothAdapter.ACTION_REQUEST_ENABLE); 
			   startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
		}			
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_ENABLE_BT ) {
            if (resultCode == Activity.RESULT_OK) {
            	mBTAdapter.startDiscovery();
            } else {
            	this.setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }
    }
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mBTAdapter != null) {
			mBTAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		unregisterReceiver(mReceiver);
	}
	
	
	private void initPairedDeviceList(){
		Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
    	// If there are paired devices
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    	    for (BluetoothDevice device : pairedDevices) {
    	        // Add the name and address to an array adapter to show in a ListView
    	    	/*BTDevice item = new BTDevice(device.getName() == null ? device.getAddress() : device.getName(),
						device.getAddress());*/					
    	    	
    	    	mDevPairedAdapter.add(device, false);
    	    }
    	    
    	    mDevPairedAdapter.notifyDataSetChanged();
    	}    	
	}   
	
	
	private void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass().getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e( "BTDevicesActivity:", e.getMessage());
		}
	}
	

}
