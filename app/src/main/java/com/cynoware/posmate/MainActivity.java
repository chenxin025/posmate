/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cynoware.posmate.sdk.SDKInfo;
import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.util.SharePrefManager;


public class MainActivity extends DeviceActivity {

	//private static final String TAG = "PosMate MainActiviy";
    private static final String TAG = "MainActivity";
	public static final int REQUEST_ENABLE_BT = 100;
	//public static final int REQUEST_SETTING_BT_CHANGE = 101; 
	
	public static final String BROADCASTING_CHANNEL_STATUS = "channel_status";
	public static final String BROADCASTING_BUSY_STATUS = "busy_status";
	
	// Define sequence
	private static int[] mEntryResID = { R.id.framePrinter, R.id.frameLED, R.id.frameScan, R.id.frameCashbox, R.id.frameNewland };
	
	private static final int ENTRY_PRINTER = 0;
	private static final int ENTRY_LED = 1;
	private static final int ENTRY_QR_SCANER = 2;
	private static final int ENTRY_CASH_DRAWER = 3;
	private static final int ENTRY_CARD_READER = 4;
		
	private FrameLayout[] mLayoutEntry;
    //private TextView lblMsg;
    //private TextView lbldetail;
	
	private int mColorNormal;
	private int mColorFocus;	
	public Typeface mFontDigit;
	
	FragmentManager mFragmentManager;
	
	private Setting mSetting;    

		
    public static boolean ISCONNECTED = false;
    
	
    //private String newMessage = "", message;
	//private ImageGetter imageGetter;
    //public boolean isnew;    
    
    private ImageView mImgChannelDockUSB = null;
    private ImageView mImgChannelTrayUSB = null;
    private ImageView mImgChannelDockBT = null;
    private ImageView mImgBusyStatus = null;
    
    private ImageView mImgSetting = null; 
    
    
    
    private final BroadcastReceiver mChannelStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (action.equals(BROADCASTING_CHANNEL_STATUS) ) {
//                int channel = intent.getIntExtra("channel", -1 );
//                int status = intent.getIntExtra("status", -1);
//
//                if( channel == ChannelManager.CHANNEL_DOCK_USB && mImgChannelDockUSB != null ){
//                	if( status == 0 )
//                		mImgChannelDockUSB.setImageResource(R.drawable.channel_dock_usb_0);
//                	else if( status == 1 )
//                		mImgChannelDockUSB.setImageResource(R.drawable.channel_dock_usb_1);
//                }else if( channel == ChannelManager.CHANNEL_TRAY_USB && mImgChannelTrayUSB != null ){
//                	if( status == 0 )
//                		mImgChannelTrayUSB.setImageResource(R.drawable.channel_tray_usb_0);
//                	else if( status == 1 )
//                		mImgChannelTrayUSB.setImageResource(R.drawable.channel_tray_usb_1);
//                }else if( channel == ChannelManager.CHANNEL_DOCK_BT && mImgChannelDockBT != null ){
//                	if( status == 0 )
//                		mImgChannelDockBT.setImageResource(R.drawable.channel_dock_bt_0);
//                	else if( status == 1 )
//                		mImgChannelDockBT.setImageResource(R.drawable.channel_dock_bt_1);
//                }
//            }else if( action.equals(BROADCASTING_BUSY_STATUS) ){
            	boolean busy = intent.getBooleanExtra("busy", false);
            	mImgBusyStatus.setImageResource( busy? R.drawable.busy : R.drawable.ready  );
            //}
        }
    };
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SET,1);
        //MyApplication.activity = this;

		// Load resource.
        AssetManager assetMgr = this.getAssets();
    	mFontDigit = Typeface.createFromAsset( assetMgr, "fonts/digit.ttf");
    	
    	// Initialize setting.
    	mSetting = Setting.getInstance( this );
    	mSetting.loadSetting();
        

                
        // Initialize UI.��ʼ��
        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCASTING_CHANNEL_STATUS);
        filter.addAction(BROADCASTING_BUSY_STATUS);        
        registerReceiver(mChannelStatusReceiver, filter);
        
        // processingUnLock();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if( requestCode == REQUEST_ENABLE_BT ){
        	//mDockBTDevice.onActivityResult(requestCode, resultCode, data);
    	}/*else if( requestCode == REQUEST_SETTING_BT_CHANGE ){    		
    		onBTDevicesActivityResult(requestCode, resultCode, data);
        }*/
    }
	

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
		unregisterReceiver(mChannelStatusReceiver);
        //unregisterReceiver(broadcastReceiver_);
		SDKLog.i(TAG,"===========MainActyivity onDestory=============");
		//SDKLog.i(TAG, "===========MainActyivity onDestory=============");

        super.onDestroy();
    }
    

    public void initView(){
    	
    	mFragmentManager = getFragmentManager();

    	setContentView(R.layout.activity_main);
            
        FragmentTransaction transaction = getFragmentManager().beginTransaction();				
		transaction.replace(R.id.viewContainer, new HomeFragment());
		transaction.commit();
		
		ImageView imgLogo = (ImageView) this.findViewById(R.id.imgLogo);
		imgLogo.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				//onClickSetting();
				setFocusEntry( -2 );
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new HomeFragment());
				transaction.commit();
			}});
    	
    	// Initialize setting button.��ʼ�����ð�ť
    	mImgSetting = (ImageView)findViewById(R.id.imgSetting);
    	mImgSetting.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View v) {
				//onClickSetting();
				setFocusEntry( -1 );
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new SettingFragment());
				transaction.commit();
			}});

    	
    	// Initialize colors
        Resources res = getResources();
    	mColorNormal = res.getColor(R.color.graybackground);
    	mColorFocus = res.getColor(R.color.orange);
    	
    	
    	// Initialize function entry�������
    	mLayoutEntry = new FrameLayout[mEntryResID.length];
    	for(int i=0; i<mEntryResID.length; i++){
    		mLayoutEntry[i] = (FrameLayout) findViewById(mEntryResID[i]);
    	}
    	    	    
    	mLayoutEntry[ENTRY_LED].setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				setFocusEntry(ENTRY_LED);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new LEDFragment());
				transaction.commit();
				//onClickLED();
			}
		});
    	
    	mLayoutEntry[ENTRY_PRINTER].setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// onClickPrinter();
				setFocusEntry(ENTRY_PRINTER);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new PrinterFragment());
				transaction.commit();
			}
		});
    	
    	mLayoutEntry[ENTRY_QR_SCANER].setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// onClickQRScaner();
				setFocusEntry(ENTRY_QR_SCANER);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new QRScanerFragment());
				transaction.commit();
			}
		});
    	
    	mLayoutEntry[ENTRY_CASH_DRAWER].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setFocusEntry(ENTRY_CASH_DRAWER);
				//onClickCashDrawer();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new CashDrawerFragment());
				transaction.commit();
			}
		});
    	
    	mLayoutEntry[ENTRY_CARD_READER].setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//onClickCardReader();
				setFocusEntry(ENTRY_CARD_READER);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();				
				transaction.replace(R.id.viewContainer, new CardReaderFragment());
				transaction.commit();
			}
		});
    	
    	mImgChannelDockUSB = (ImageView) this.findViewById(R.id.imgChannelDockUSB);
    	mImgChannelTrayUSB = (ImageView) this.findViewById(R.id.imgChannelTrayUSB);
    	mImgChannelDockBT = (ImageView) this.findViewById(R.id.imgChannelDockBT);
    	if( mSetting.getPosModel().equals(Setting.MODEL_POS_MATE) )
    		mImgChannelTrayUSB.setVisibility(View.GONE);
    	
    	mImgBusyStatus = (ImageView) this.findViewById(R.id.imgBusyStatus);
    	
    	// initSpinnerChannel();
    }

    
    public void setFocusEntry( int entry ){
    	if( entry < -2 || entry > mLayoutEntry.length )
    		return;
    	
    	if( entry == -1 )
    		mImgSetting.setImageResource( R.drawable.setting_focused );
    	else
    		mImgSetting.setImageResource( R.drawable.setting );
    	
    	for( int i=0; i<mLayoutEntry.length; i++ ){
    		mLayoutEntry[i].setBackgroundColor( i==entry? mColorFocus:mColorNormal);
    	}		
    }
}
