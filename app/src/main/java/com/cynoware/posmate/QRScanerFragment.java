/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;


import android.app.Activity;
import android.app.Fragment;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;


public class QRScanerFragment extends Fragment {
	
	
    //private ChannelManager mChannelMgr;
    private Setting mSetting;
    private Activity mActivity;
    private static boolean mIsScaning = false;
    private LinearLayout mLayoutScaning;
    private TextView mTvTIPScaning, mTvScanResult;
    private Button mBtnStart, mBtnStop;

	public QRScanerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mActivity =  this.getActivity();
		mSetting = Setting.getInstance(mActivity);
		
		// TODO RISK
		//mChannelMgr = ((MainActivity)mActivity).mChannelMgr;
		
		View rootView = inflater.inflate(R.layout.activity_qrscaner, container,
				false);
		
		mLayoutScaning = (LinearLayout) rootView.findViewById(R.id.layoutScaning);
		mLayoutScaning.setVisibility(View.GONE);
		
		mTvTIPScaning = (TextView)rootView.findViewById(R.id.tvTIPScaning );
		mTvScanResult = (TextView)rootView.findViewById(R.id.tvScanResult );
		
		Spinner spinChannel = (Spinner)rootView.findViewById( R.id.spinnerQRScannerChannel);
    	

		String channels_4[] = {"Tray USB"};
		String channels_3[] = { "Dock USB", "Dock Bluetooth", "Tray USB" };
		String channels_2[] = { "Dock USB", "Dock Bluetooth" };
		String channels[];
		
    	String model = mSetting.getPosModel();
    	if(model.equals(Setting.MODEL_NP10))
    		channels = channels_3;//channels_3;
    	else
    		channels = channels_2;    		
    	
    	Adapter adapterChannel = new ArrayAdapter<String>(mActivity,android.R.layout.simple_list_item_1, channels );
    	spinChannel.setAdapter((SpinnerAdapter) adapterChannel);
    	
    	int channel = mSetting.getQRScanerChannel();
//    	if( channel == ChannelManager.CHANNEL_DOCK_USB )
//    		spinChannel.setSelection( 0 );
//    	else if( channel == ChannelManager.CHANNEL_DOCK_BT )
//    		spinChannel.setSelection( 1 );
//    	else if( channel == ChannelManager.CHANNEL_TRAY_USB )
//    		spinChannel.setSelection( 2 );
    	
    	spinChannel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch( position ){
				case 0:
					//mSetting.setQRScanerChannel(ChannelManager.CHANNEL_DOCK_USB);
					break;
					
				case 1:
					//mSetting.setQRScanerChannel(ChannelManager.CHANNEL_DOCK_BT);
					break;	
					
				case 2:
					//mSetting.setQRScanerChannel(ChannelManager.CHANNEL_TRAY_USB);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}    
    	});    	
		
    	mBtnStart = (Button)rootView.findViewById(R.id.btnStart);
    	mBtnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyApplication.getInstance().getPosService().startScanner(new ResultCallBack() {

					@Override
					public void onFailed() {

					}

					@Override
					public void onStrResult(String str) {
						Log.i("sdk","========================str;"+str);
					}
				},new Handler());
			}
		});
    	
    	mBtnStop = (Button)rootView.findViewById(R.id.btnStop);
    	//mBtnStop.setEnabled( false );
    	mBtnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyApplication.getInstance().getPosService().closeScanner();
			}
		});
    	
    	setScaningUI( mIsScaning );
	    
		return rootView;
	}
	
	private void setScaningUI( final boolean flag ){
		
		// Notify UI.
		mActivity.runOnUiThread( new Runnable(){
			@Override
			public void run() {				
				mTvTIPScaning.setText( R.string.TIP_Scaning);
				mLayoutScaning.setVisibility( flag? View.VISIBLE:View.INVISIBLE);
		    	mBtnStart.setEnabled( !flag );
		    	//mBtnStop.setEnabled( flag );
		    	if( flag )
		    		mTvScanResult.setText("");
			}} );
	}

//	public void doStartScan(){
//
//    	// Choose and check device
//		final DeviceInfo info = Util.getQRScannerInfo(getActivity(),mChannelMgr);
//    	//if( Utils.checkDeviceAvailable(info.device, mActivity) == false )
//    	//	return;
//
//    	// Check busy
//    	if( mChannelMgr.isBusy()) {
//    		Toast.makeText(mActivity, "System busy, please wait...",
//    				Toast.LENGTH_SHORT).show();
//			return;
//		}
//
//    	new Thread() {
//    		public void run() {
//            	mChannelMgr.setBusy( true );
//            	setScaningUI( mIsScaning = true );
//				QrReader.initQrReader(info.device, info.port);
//				mIsScaning = true;
//            	try {
//					while (mIsScaning) {
//						String str = QrReader.startScan(info.device, info.port,2000);
//						if (null != str && str.trim().length() <= 2) {
//							str = "";
//						}
//						if (str != null && str.length() > 0) {
//							// TODO 显示结果
//							showResult(str);
//						}
//					}
//
//				}catch(Exception ex){
//					ex.printStackTrace();
//					mIsScaning = false;
//					QrReader.stopScan(info.device, info.port);
//					QrReader.closeQRScanner(info.device);
//
//            	}finally{
//
//            	}
//            	setScaningUI( false );
//            	mChannelMgr.setBusy( false );
//            }
//    	}.start();
//	}
	
	
	private void showResult(final String result){
		ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
		toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
		
		mActivity.runOnUiThread( new Runnable(){
			@Override
			public void run() {				
				mTvScanResult.setText(result);
			}} );
	}
	
	
//	public void doStopScan(){
//		mIsScaning = false;
//		final  DeviceInfo info = Util.getQRScannerInfo(getActivity(),mChannelMgr);
//		QrReader.stopScan(info.device, info.port);
//		QrReader.closeQRScanner(info.device);
//
//		Toast.makeText(mActivity, "Not in scaning", Toast.LENGTH_SHORT).show();
//		mTvTIPScaning.setText( R.string.TIP_Cancel_Scaning );
//		mBtnStop.setEnabled( false );
//
//		/**
//		 * revised by puzhimin 2016.7.25
//		 * 增加了下面一句 解决界面假死的现象
//		 */
//		setScaningUI(false);
//
//    }
    
    /*public void startQRScanTask( final Device device, final boolean bStart ) {
    	
    	Activity activity = this.getActivity();
    	
    	// Check busy
    	if( bStart && mChannelMgr.isBusy()) {
    		Toast.makeText(activity, "Please wait...", Toast.LENGTH_SHORT).show();   
			return;
		}

    	// Change UI.
    	mLayoutScaning.setVisibility( bStart? View.VISIBLE:View.GONE);
    	mBtnStart.setEnabled( !bStart );
    	mBtnStop.setEnabled( bStart );
    	
    	new Thread() {
            public void run() {
            	mChannelMgr.setBusy( true );
            	
            	if( bStart ){
            		// START
	            	try{
				        mIsScaning = true;
				    	// showMessage("Start QRCode!",MessageTag.TIP);
				        //QrReader.setRawData(baseDevice, config.CONFIG_DOCK_QRREADER_UART);
				        while(mIsScaning){
				        	//String str = QrReader.scan(baseDevice, config.CONFIG_DOCK_QRREADER_UART,QrReader.kTriggerMode_PressKey);
					        String str = QrReader.scan(device, config.CONFIG_DOCK_QRREADER_UART);
					        if(str.trim().length() <= 2){
					        	str = "";
				        	}
					        if (str != null && str.length() > 0) {
					        	// showMessage(MyApplication.activity.getResources().getString(R.string.qrreader)+str,MessageTag.NORMAL);					        	
					        	QrReader.stopScan(device, config.CONFIG_DOCK_QRREADER_UART);
					        	// showMessage(str,MessageTag.DETAIL);
					        	break;
					        }
				        }
				        
					}catch(Exception ex){
						ex.printStackTrace();
						QrReader.stopScan(device, config.CONFIG_DOCK_QRREADER_UART);
					}
            	}else{
            		// STOP
            		try{
                		QrReader.stopScan(device, config.CONFIG_DOCK_QRREADER_UART);
                		// showMessage("Stop QrScaner!",MessageTag.TIP);
                    	mIsScaning = false;
                	}
                	catch(Exception ex){
                		// showMessage("Exception:"+ex.getMessage(),MessageTag.ERROR);
                	}
            	}
            	
            	mChannelMgr.setBusy(false);
            }
		}.start();
    }
    */
	

}
