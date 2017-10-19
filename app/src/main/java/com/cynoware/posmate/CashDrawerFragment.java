/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import com.cynoware.posmate.sdk.drawer.CashDrawer;
import com.cynoware.posmate.sdk.io.Device;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class CashDrawerFragment extends Fragment {

	private static final int CONFIG_CASHDRAWER_GPIO = (32*2+1);
    //private ChannelManager mChannelMgr;
    private MainActivity mMainActivity;
    private Setting mSetting;

	public CashDrawerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		MainActivity activity = mMainActivity = (MainActivity) this.getActivity();

		mSetting = Setting.getInstance(activity);
		
		View rootView = inflater.inflate(R.layout.activity_cashdrawer, container,
				false);
		
		Spinner spinChannel = (Spinner)rootView.findViewById( R.id.spinnerCashDrawerChannel);
    	String channels[] = { "Dock USB", "Dock Bluetooth" };    	
    	Adapter adapterChannel = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, channels );
    	spinChannel.setAdapter((SpinnerAdapter) adapterChannel);
    	
//    	if( mSetting.getCashDrawerChannel() == ChannelManager.CHANNEL_DOCK_USB )
//    		spinChannel.setSelection( 0 );
//    	else
//    		spinChannel.setSelection( 1 );
    	
    	
    	spinChannel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch( position ){
				case 0:
					//mSetting.setCashDrawerChannel(ChannelManager.CHANNEL_DOCK_USB);
					break;
					
				case 1:
					//mSetting.setCashDrawerChannel(ChannelManager.CHANNEL_DOCK_BT);
					break;	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}    
    	});    	
    	
    	
    	Button btnOpen = (Button)rootView.findViewById(R.id.btnCashDrawerOpen);
    	if(btnOpen != null){
    		btnOpen.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					doOpenCashDrawer();
				}
			});
    	}    	
	    
		return rootView;
	}
	
	public void doOpenCashDrawer(){
		MyApplication.getInstance().getPosService().openCachDrawer(null,null);
    	// Choose and check device
    	
//    	if( Util.checkDeviceAvailable(device, mMainActivity) == false )
//    		return;
//
//    	if (mChannelMgr.isBusy()) {
//    		Toast.makeText(mMainActivity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
//    		return;
//    	}
//
//    	Toast.makeText(mMainActivity, "Opening cash drawer...", Toast.LENGTH_SHORT).show();
//
//    	final CashDrawer drawer = new CashDrawer( device, CONFIG_CASHDRAWER_GPIO );
//
//    	new Thread() {
//            public void run() {
//            	mChannelMgr.setBusy(true);
//
//            	drawer.open();
//
//		    	mChannelMgr.setBusy(false);
//            }
//        }.start();
    	
    	//startCashDrawerTask(device);
    }
    
    
    /*private void startCashDrawerTask(final Device device){
    	if (mChannelMgr.isBusy()) {
    		Toast.makeText(mMainActivity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();    		
    		return;
    	}
    	
    	Toast.makeText(mMainActivity, "Opening cash drawer...", Toast.LENGTH_SHORT).show(); 
    	
    	new Thread() {
            public void run() {
            	mChannelMgr.setBusy(true);
		    	
				int CONFIG_CASHDRAWER_GPIO = (32*2+1);
				GPIO.setMode(device, CONFIG_CASHDRAWER_GPIO, 1, 0);
		    	
		    	try {
		    		GPIO.output(device, CONFIG_CASHDRAWER_GPIO, 1);
		            Thread.sleep(200);
		            GPIO.output(device, CONFIG_CASHDRAWER_GPIO, 0);
		            Thread.sleep(200);
		        } catch (Exception ex) {
		        	ex.printStackTrace();
		        }
		    	mChannelMgr.setBusy(false);
            }
        }.start();
	}*/
}
