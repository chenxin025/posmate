/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import com.cynoware.posmate.sdk.Device;
import com.cynoware.posmate.sdk.LED;
import com.cynoware.posmate.sdk.config;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;


public class LEDFragment extends Fragment {
	
	private static final int LED_DEVICE_ON_BOARD = 0;
    private static final int LED_DEVICE_EXTERNAL = 1;

    private ChannelManager mChannelMgr;
    private MainActivity mMainActivity;
    private Setting mSetting;
    private EditText mEditText;
    private byte[] mSpecial;
	private int mPriceType = -1;
    private String mText;

	public LEDFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		MainActivity activity = mMainActivity = (MainActivity) this.getActivity();
		mSetting = Setting.getInstance(activity);
		
		Typeface font = activity.mFontDigit;
		mChannelMgr = activity.mChannelMgr;
		
		View rootView = inflater.inflate(R.layout.activity_led, container,
				false);
	    
	    Spinner spinnerDevice = (Spinner)rootView.findViewById( R.id.spinnerLEDDevice);
    	String devices[] = { "On Board", "External" };    	
    	Adapter adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, devices );
    	spinnerDevice.setAdapter((SpinnerAdapter) adapter);
    	spinnerDevice.setSelection( mSetting.getLEDDevice() );    	
    	
    	spinnerDevice.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch( position ){
				case 0:
					mSetting.setLEDDevice(LED_DEVICE_ON_BOARD);
					break;
					
				case 1:
					mSetting.setLEDDevice(LED_DEVICE_EXTERNAL);
					break;			
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}    
    	});    	
    	
    	Spinner spinChannel = (Spinner)rootView.findViewById( R.id.spinnerLEDChannel);
    	String channels[] = { "Dock USB", "Dock Bluetooth" };    	
    	Adapter adapterChannel = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, channels );
    	spinChannel.setAdapter((SpinnerAdapter) adapterChannel);
    	
    	if( mSetting.getLEDChannel() == ChannelManager.CHANNEL_DOCK_USB )
    		spinChannel.setSelection( 0 );
    	else
    		spinChannel.setSelection( 1 );
    	
    	
    	spinChannel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch( position ){
				case 0:
					mSetting.setLEDChannel(ChannelManager.CHANNEL_DOCK_USB);
					break;
					
				case 1:
					mSetting.setLEDChannel(ChannelManager.CHANNEL_DOCK_BT);
					break;	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}    
    	});    	
    	
	    
    	Button btnInit = (Button)rootView.findViewById(R.id.btnInitialization);
    	if(btnInit != null){
    		btnInit.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					mText = null;
					doDisplayLED(LED.CMD_INIT_TYPE);
				}
			});
    	}
   	
    	Button btnPrice = (Button)rootView.findViewById(R.id.btnPrice);
    	btnPrice.setTypeface(font);
    	if(btnPrice != null){
    		btnPrice.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doDisplayLED(LED.CMD_PRICE_TYPE);
				}
			});
    	}
    	
    	Button btnTotal = (Button)rootView.findViewById(R.id.btnTotal);
    	btnTotal.setTypeface(font);
    	if(btnTotal != null){
    		btnTotal.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doDisplayLED(LED.CMD_TOTAL_TYPE);
				}
			});
    	}
    	
    	Button btnCollect = (Button)rootView.findViewById(R.id.btnCollect);
    	btnCollect.setTypeface(font);
    	if(btnCollect != null){
    		btnCollect.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doDisplayLED(LED.CMD_COLLECT_TYPE);
				}
			});
    	}
    	
    	Button btnChange = (Button)rootView.findViewById(R.id.btnChange);
    	btnChange.setTypeface(font);
    	if(btnChange != null){
    		btnChange.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doDisplayLED(LED.CMD_CHANGE_TYPE);
				}
			});
    	}
    	
    	mEditText = (EditText)rootView.findViewById(R.id.txtShowtxt);
    	Button btnShowtxt = (Button)rootView.findViewById(R.id.btnShowtxt);
    	if(btnShowtxt != null){
    		btnShowtxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mText = mEditText.getText().toString();
					doDisplayLED(getmPriceType());
				}
			});
    	}
		return rootView;
	}
	
	
	public void doDisplayLED(final int which){
    	setmPriceType(which);
    	// Choose and check device
    	Device device = mChannelMgr.getDevice( mSetting.getLEDChannel() );
    	
    	if( Util.checkDeviceAvailable(device,mMainActivity) == false )
    		return;
    	
    	boolean bBoard = mSetting.getLEDDevice()==LED_DEVICE_ON_BOARD;
    	
    	// startDisplayLEDTask( device, bBoard, special, text );
    	
    	Activity activity = this.getActivity();
    	
    	if (mChannelMgr.isBusy()) {
    		Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();    		
    		return;
    	}
    	
    	int uart = bBoard? config.CONFIG_ONBOARD_LED_UART : config.CONFIG_LED_UART;
    	
    	final LED led = new LED( device, uart, bBoard );
    	
    	new Thread() {
    		public void run() {
    			mChannelMgr.setBusy(true);

    			//led.config();
    			//led.showSpecial(mSpecial);
    			//led.showText(mText);
				led.ledShowText(getmPriceType(),mText);
    			
		   	 	mChannelMgr.setBusy(false);
    		}
	    }.start();
    }

	public void setmPriceType(int type){
		mPriceType = type;
	}

	public int getmPriceType(){
		return mPriceType;
	}
}
