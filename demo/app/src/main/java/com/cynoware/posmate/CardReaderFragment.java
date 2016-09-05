/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.newland.mesdk.device.BlueToothDevice;
import com.newland.mesdk.device.USBDevice;
import com.newland.mesdk.event.RadioGroupChangeListener;
import com.newland.mesdk.interfaceImpl.CardReaderInterfaceImpl;
import com.newland.mesdk.interfaceImpl.EmvInterfaceImpl;
import com.newland.mesdk.interfaceImpl.ICCardInterfaceImpl;
//import com.newland.mesdk.interfaceImpl.PinInputInterfaceImpl;
import com.newland.mesdk.interfaceImpl.RFCardInterfaceImpl;
import com.newland.mesdk.interfaceImpl.SwiperInterfaceImpl;
//import com.newland.mesdk.moduleinterface.DeviceControllerInterface;
import com.newland.mesdk.util.Const;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.emv.AIDConfig;
import com.newland.mtype.module.common.emv.CAPublicKey;
import com.newland.mtype.module.common.emv.EmvCardInfo;
import com.newland.mtype.module.common.iccard.ICCardSlot;
import com.newland.mtype.module.common.iccard.ICCardType;
//import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.module.common.rfcard.RFCardType;
import com.newland.mtype.module.common.rfcard.RFResult;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class CardReaderFragment extends Fragment {
	
	
    private ChannelManager mChannelMgr;
    private Setting mSetting;
    private MainActivity mActivity;
    AlertDialog dialogInit;
    private static boolean mIsReading;
    
    private LinearLayout mReadingLayout;
    private Button nBtnLoadKey, mBtnStartReading, mBtnStopReading;
    private ArrayListAdapter mAdapterMessage;
    
    
	// Card reader related	
	//public static boolean ISCONNECTED = false;
	private static BlueToothDevice mBTDevice;
	private static USBDevice mUSBDevice;

	//public String connectType;
	//private DeviceControllerInterface controller;
	
	//private PinInputInterfaceImpl mPinInputInterfaceI;
	//private K21EmvInterfaceImpl mK21EmvInterface;
	private EmvInterfaceImpl mEmvInterface;
	private CardReaderInterfaceImpl mCardReaderInterface;
	private ICCardInterfaceImpl mICCardInterface;
	private SwiperInterfaceImpl mSwiperInterface;
	private RFCardInterfaceImpl mRFCardInterface;
	
	private RFCardType qpCardType = RFCardType.M1CARD;
	
	private Dialog nccard_dialog;
	private RadioGroupChangeListener radioGroupChangeListener;
	private ICCardSlot icCardSlot;
	private Dialog iccard_dialog;
	
	private int keyIndex = 87;
	private byte[] rid = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x03, 0x33 };
	protected static final String MAINKEY = "253C9D9D7C2FBBFA253C9D9D7C2FBBFA";// 主密钥预设输入值
	protected static final String WORKINGKEY_DATA_MAC = "DBFE96D0A5F09D24";// MAC秘钥预设输入值
	protected static final String WORKINGKEY_DATA_TRACK = "DBFE96D0A5F09D24DBFE96D0A5F09D24";// TRACK秘钥预设输入值
	protected static final String WORKINGKEY_DATA_PIN = "D2CEEE5C1D3AFBAF00374E0CC1526C86";// PIN秘钥预设输入值

	public CardReaderFragment() {
	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		MainActivity activity = mActivity = (MainActivity) this.getActivity();
		mSetting = Setting.getInstance(activity); 
				
		mChannelMgr = activity.mChannelMgr;
		mBTDevice = mChannelMgr.mCardBTDevice;
		mUSBDevice = mChannelMgr.mCardUSBDevice;
		
		View rootView = inflater.inflate(R.layout.activity_cardreader, container,
				false);
		
		mReadingLayout = (LinearLayout) rootView.findViewById( R.id.layoutReading );
		mReadingLayout.setVisibility( View.INVISIBLE );
		
		ListView lvMessage = (ListView)rootView.findViewById(R.id.lvCardMsg);
		//mListMessage = new ArrayList();
		//mAdapterMessage = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, mListMessage );
		mAdapterMessage = new ArrayListAdapter(activity); 
		lvMessage.setAdapter( mAdapterMessage);
		lvMessage.setDivider(null);	
    	
	    
    	Spinner spinChannel = (Spinner)rootView.findViewById( R.id.spinnerCardReaderChannel);
    	String channels[] = { "Card USB", "Card Bluetooth" };    	
    	Adapter adapterChannel = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, channels );
    	spinChannel.setAdapter((SpinnerAdapter) adapterChannel);
    	
    	if( mSetting.getCardReaderChannel() == ChannelManager.CHANNEL_CARD_USB )
    		spinChannel.setSelection( 0 );
    	else
    		spinChannel.setSelection( 1 );
    	
    	
    	spinChannel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch( position ){
				case 0:
					mSetting.setCardReaderChannel(ChannelManager.CHANNEL_CARD_USB);
					break;
					
				case 1:
					mSetting.setCardReaderChannel(ChannelManager.CHANNEL_CARD_BT);
					break;	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}    
    	});    	
    	
    	
    	/*Button btnInit = (Button)rootView.findViewById(R.id.btnInitCardReader);
    	if(btnInit != null){
    		btnInit.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					doInitCardReader();
				}
			});
    	}*/
    	
    	nBtnLoadKey = (Button)rootView.findViewById(R.id.btnLoadKey);
    		nBtnLoadKey.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doLoadKey();
				}
			});
    	
    	mBtnStartReading = (Button)rootView.findViewById(R.id.btnStartCardReader);
    		mBtnStartReading.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doStartReading();
				}
			});
    	
    	mBtnStopReading = (Button)rootView.findViewById(R.id.btnStopCardReader);
    	mBtnStopReading.setEnabled( false );
    	mBtnStopReading.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doStopReading();
				}
			});
	    
    	setReadingUI( mIsReading );
    	
		return rootView;
	}
	
	
	/*private boolean checkUSBConnection(){
		if( !mUSBDevice.isControllerAlive() ){
			showMessage( "USB is not initilized");
			return false;
		}
		
		if( !mUSBDevice.isConnected() ){
			showMessage( "Please check USB cable connection");
			return false;
		}
		
		return true;
	}
	
	
	private boolean checkBTConnection(){
		if( !mBTDevice.isControllerAlive() ){
			showMessage( "Bluetooth is not initilized");
			return false;
		}
		
		if( !mBTDevice.isConnected() ){
			Log.d( "CardReader UI", "checkBTConnection : not connected" );
		}
		
		return true;
	}*/
	
    
  //装载密钥
  	public void doLoadKey(){
  		clearMessages();
  		
  		final int channel = mSetting.getCardReaderChannel();
  		
  		// Check connection
  		/*if ( channel == ChannelManager.CHANNEL_CARD_USB ){
  			if( !checkUSBConnection() )
  				return;
  		}
  		else if ( channel == ChannelManager.CHANNEL_CARD_BT ){
  			if( !checkBTConnection() )
  				return;
  		}*/
  		
  		/*else if(mBTDevice.isControllerAlive()){
  			connectDevice();
  		}
  		
  		mPinInputInterfaceI = new PinInputInterfaceImpl();
  		mK21EmvInterface = new K21EmvInterfaceImpl();
  		mEmvInterface = new EmvInterfaceImpl();*/
  		new Thread(new Runnable() {

  			@Override
  			public void run() {
  				mChannelMgr.setBusy(true);
  				
  				try {
  					
  					
  					
  					//if( channel == ChannelManager.CHANNEL_CARD_BT ){
  						connectDevice();
  			  		//}
  					
  					// PinInputInterfaceImpl pinInputInterface = new PinInputInterfaceImpl();
  			  		// mK21EmvInterface = new K21EmvInterfaceImpl();
  			  		EmvInterfaceImpl emvInterface = new EmvInterfaceImpl();
  			  		
  			  		if( emvInterface != null && emvInterface.emvModule != null ){
	  			  		//LoadMainKey();
	  					//Thread.sleep(500);
	  					LoadAID();
	  					//Thread.sleep(500);
	  					//LoadWorkKey();
	  					//Thread.sleep(1000);
	  					LoadPublicKey(emvInterface);
	  					// showMessage("Load key success!");
	  					
	  					mEmvInterface = emvInterface;
	  					showMessage( "Load key success!");
	  					
  			  		}else{
  			  			showMessage("Emv Interface init fail");
  			  		}
  				} 
  				catch (Exception e) {  					
  					showMessage( "Load key fail" + e );
  				}
  				
  				mChannelMgr.setBusy(false);
  			}
  		}).start();
  	}
  	
  	//关闭读卡器
  	public void doStopReading(){
  		
  		final int channel = mSetting.getCardReaderChannel();
  						
  		// Check connection
  		/*if ( channel == ChannelManager.CHANNEL_CARD_USB ){
  			if( !checkUSBConnection() )
  				return;
  		}
  		else if ( channel == ChannelManager.CHANNEL_CARD_BT ){
  			if( !checkBTConnection() )
  				return;
  		}*/
  		
  		clearMessages();
  		
  		new Thread(new Runnable() {
  	
  			@Override
  			public void run() {
  				try {
  					//if( channel == ChannelManager.CHANNEL_CARD_BT ){
  						connectDevice();
  			  		//}
  					
  					if(mCardReaderInterface != null){
  						mCardReaderInterface.cancelCardRead();
  						mCardReaderInterface.closeCardReader();
  					}

  					mChannelMgr.setBusy(false);
  				} catch (Exception e) {
  					showMessage( "Error" );
  				}
  				
  				
  				mChannelMgr.setBusy( false);			    	
		    	mIsReading = false;
				setReadingUI(false);				  				
  			}
  		}).start();
  	}
  	
  	
  	
    
  //刷卡器测试
    public void doStartReading(){
    	
    	clearMessages();

		final int channel = mSetting.getCardReaderChannel();
  		
    	// Check connection
  		/*if ( channel == ChannelManager.CHANNEL_CARD_USB ){
  			if( !checkUSBConnection() )
  				return;
  		}
  		else if ( channel == ChannelManager.CHANNEL_CARD_BT ){
  			if( !checkBTConnection() )
  				return;
  		}*/
		
  		if (mChannelMgr.isBusy()) {
    		Toast.makeText(mActivity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();    		
    		return;
    	}

		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					mChannelMgr.setBusy( true );			    	
			    	mIsReading = true;
					setReadingUI(true);
					
					mCardReaderInterface = new CardReaderInterfaceImpl();
					mRFCardInterface = new RFCardInterfaceImpl();
					mSwiperInterface = new SwiperInterfaceImpl();
					mICCardInterface = new ICCardInterfaceImpl();
					
					//if( channel == ChannelManager.CHANNEL_CARD_BT ){
  						connectDevice();
  			  		//}
					
					showMessage("Reading...");
					
					mCardReaderInterface.openCardReader("Please swipe card or insert IC card", new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER }, null, true, true, 60, TimeUnit.SECONDS, new DeviceEventListener<OpenCardReaderEvent>() {
						@Override
						public void onEvent(OpenCardReaderEvent openCardReaderEvent, Handler handler) {
							
							if (openCardReaderEvent.isSuccess()) {
								switch (openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]) {
								case MSCARD:
									showMessage("MS Card Detected");
									GetMSRCardNum();
									break;
								case ICCARD:
									showMessage("IC Card Detected");
									GetICCardNum();
									break;
								case RFCARD:
									showMessage("RF Card Detected");
									GetRFCardNum();
									break;
									
								default:
									break;
								}								
							} else if (openCardReaderEvent.isUserCanceled()) {
								showMessage("Reading Stoped");
								mChannelMgr.setBusy(false);
							} else if (openCardReaderEvent.isFailed()) {
								showMessage("Card reader open failed");
							}
							
							mChannelMgr.setBusy( false);			    	
					    	mIsReading = false;
							setReadingUI(false);
						}

						@Override
						public Handler getUIHandler() {
							return null;
						}
					});

				} catch (Exception e) {
					mChannelMgr.setBusy( false);			    	
			    	mIsReading = false;
					setReadingUI(false);
					
					e.printStackTrace();
					showMessage("Please Load key...");
					mChannelMgr.setBusy(false);
				}
			}
		}).start();
    }
    
    
    private void setReadingUI( final boolean isReading ){
    	
    	mActivity.runOnUiThread( new Runnable(){
			@Override
			public void run() {
				if( isReading ){
		    		mReadingLayout.setVisibility( View.VISIBLE );
					nBtnLoadKey.setEnabled(false);
					mBtnStartReading.setEnabled(false);
					mBtnStopReading.setEnabled(true);
		    	}else{
		    		mReadingLayout.setVisibility( View.INVISIBLE );
		      		nBtnLoadKey.setEnabled(true);
		    		mBtnStartReading.setEnabled(true);
		    		mBtnStopReading.setEnabled(false);
		    	}
			}});
    	
    }
 	
	public boolean connectDevice() {
		int channel = mSetting.getCardReaderChannel();
		
		if (channel == ChannelManager.CHANNEL_CARD_BT) {

			String name = mSetting.getCardBTName();
			showMessage("Connecting to " + name + " via bluetooth...");
			
			// Init controller
			if( !mBTDevice.isControllerAlive() ){
				String addr = mSetting.getCardBTAddr();
				mBTDevice.setBTAddress(addr);
				mBTDevice.initController();
				
				if( !mBTDevice.isControllerAlive() ){
					showMessage("Device controller not initialized!");
					return false;
				}
			}
			
			// Connect
			if( !mBTDevice.isConnected()){
				try {
					mBTDevice.connect();
				} catch (Exception e) {
					showMessage("Exception :" + e);
				}
			}
			
			return mBTDevice.isConnected();
		}
		else if (channel == ChannelManager.CHANNEL_CARD_USB) {
			showMessage("Connecting to device via USB...");
			
			if( !mUSBDevice.isControllerAlive() ){
				mBTDevice.initController();
				
				if( !mBTDevice.isControllerAlive() ){
					showMessage("Device controller not initialized!");
					return false;
				}
			}
			
			if( !mUSBDevice.isConnected() ){
				try {
					mUSBDevice.connect();
				}catch (Exception e) {
					showMessage("Exception :" + e);
				}				
			}
			return mBTDevice.isConnected();
		} else {
			showMessage("No connection mode! Please reinitialize!");
			return false;
		}
	}
	
	
	// IC卡卡槽上电对话框
 	public void GetICCardNum() {
 		mActivity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);
 				LayoutInflater inflater = LayoutInflater.from(mActivity);
 				final View view = inflater.inflate(R.layout.dialog_iccard, null);
 				LinearLayout ic_send_data = (LinearLayout) view.findViewById(R.id.ic_send_data);
 				ic_send_data.setVisibility(View.GONE);
 				radioGroupChangeListener = new RadioGroupChangeListener(view, Const.DialogView.IC_CARD_ICCardSlot_DIALOG);
 				builder.setTitle("IC Power on:");
 				builder.setView(view);
 				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						try {
 							icCardSlot = radioGroupChangeListener.getiCCardSlot();
 							//System.out.println("上电卡槽====" + icCardSlot);
 							ICCardType selecttype = radioGroupChangeListener.getIcCardType();
 							byte result[] = mICCardInterface.powerOn(icCardSlot, selecttype);

 							showMessage("Power result :" + Dump.getHexDump(result));
 							if(result != null){
 								Set<Integer> set = new HashSet<Integer>();
 								set.add(1);
 								set.add(2);
 								set.add(3);
 								EmvCardInfo emvCardInfo;
 								/*Removed by JieZhuang, HW don't support K21
 								 * if (connectType.equals(Const.ConnectTypeName.K21)) {
 									mK21EmvInterface.initEmvModule(MyApplication.activity);
 									emvCardInfo = mK21EmvInterface.getAccountInfo(set);
 								} else {
 									emvCardInfo = mEmvInterface.getAccountInfo(set);
 								}*/
 								
 								emvCardInfo = mEmvInterface.getAccountInfo(set);

 								String cardMo = emvCardInfo.getCardNo();
 								String cardSecuenceNumber = emvCardInfo.getCard_sequence_number();
 								String cardExpirationData = emvCardInfo.getCardExpirationDate();
 								if (emvCardInfo != null) {
 									showMessage("Access to the EMV process under the success of the account information!");
 									showMessage("Card Num.:" + cardMo);
 									showMessage("Card serial number:" + cardSecuenceNumber);
 									showMessage("Validity period:" + cardExpirationData);
 									
 									showMessage(cardMo);
 								}
 								mICCardInterface.powerOff(icCardSlot, ICCardType.CPUCARD);
 							}
 							mCardReaderInterface.cancelCardRead();
 							//showMessage("卡槽：" + icCardSlot.toString() + "上电完成");
 							mChannelMgr.setBusy(false);
 						} catch (Exception e) {
 							//showMessage("卡槽上电异常:" + e.getMessage());
 							showMessage("Please check whether the card slot has been inserted into the IC card!");
 							mChannelMgr.setBusy(false);
 						} 
 					}
 				});
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						iccard_dialog.dismiss();
 						mChannelMgr.setBusy(false);
 					}
 				});
 				
 				iccard_dialog = builder.create();
 				iccard_dialog.show();
 				iccard_dialog.setCancelable(false);
 				iccard_dialog.setCanceledOnTouchOutside(false);
 			}
 		});

 	}
 	
 	//获取普通卡信息
 	public void GetMSRCardNum(){
 		new Thread(new Runnable() {

 			@Override
 			public void run() {
 				
 				mChannelMgr.setBusy(true);
 				try {
 					showMessage("Begin to express the way to return the results of the credit card:");
 					SwipResult swipRslt = mSwiperInterface.readPlainResult(new SwiperReadModel[] {
 							SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK });
 					if (null != swipRslt && swipRslt.getRsltType() == SwipResultType.SUCCESS) {

 						// ((MyApplication) MyApplication.appcontext).setSwipResult(swipRslt);
 						byte[] secondTrack = swipRslt.getSecondTrackData();
 						//byte[] thirdTrack = swipRslt.getThirdTrackData();
 						showMessage("SECOND_TRACK:" + (secondTrack == null ? "null" : Dump.getHexDump(secondTrack)));
 						//showMessage("THIRD_TRACK:" + (thirdTrack == null ? "null" : Dump.getHexDump(thirdTrack)));
 						if(secondTrack == null){
 							showMessage("Please swipe again!\r\n");
 						}
 						else{
 							String secondstr="";
 							//String thirdstr="";
 							try {
 								secondstr = new String(secondTrack,"GBK");
 								//thirdstr = new String(thirdTrack,"GBK");
 								secondstr = secondstr.split("=")[0];
 							} catch (UnsupportedEncodingException e) {
 								e.printStackTrace();
 							}
 							showMessage(
 									"Card Number (GBK):" + secondstr);
 							showMessage(secondstr);
 						}
 						mCardReaderInterface.cancelCardRead();
 					} else {
 						showMessage("The credit card is empty.");
 					}
 					mChannelMgr.setBusy(false);
 				} catch (Exception e) {
 					showMessage("Exception：" + e);
 					//showMessage("是否已经加载主密钥、工作秘钥、AID、公钥！");
 					//showMessage("是否已经开启读卡器并刷卡！");
 					mChannelMgr.setBusy(false);
 				}
 			}
 		}).start();
 	}
 	
 	
    //获取RF卡信息
    public void GetRFCardNum(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				mChannelMgr.setBusy(true);
				showRFCardPowerOnDialog();

			}
		}).start();
	}
    
    
	// 非接卡上电对话框
	public void showRFCardPowerOnDialog() {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);
				builder.setTitle("RF Card type:");
				builder.setSingleChoiceItems(new String[] { "A card", "B card", "M1 card" }, 2,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								switch (arg1) {
								case 0:
									qpCardType = RFCardType.ACARD;
									break;
								case 1:
									qpCardType = RFCardType.BCARD;
									break;
								case 2:
									qpCardType = RFCardType.M1CARD;
									break;
								}

							}
						});
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							new Thread(new Runnable() {

								@Override
								public void run() {
									//showMessage("请刷非接卡 ！");
									try{
										RFResult qPResult = mRFCardInterface.powerOn(qpCardType, 8);
										showMessage("RF CARD NAME:" + qPResult.getQpCardType());
										showMessage("RF CARD TYPE:" + qPResult.getQpCardType());
										if (qPResult.getCardSerialNo() == null) {
											showMessage("RF Card serial number:null");
										} else {
											showMessage(
													"RF Card serial number:" + Dump.getHexDump(qPResult.getCardSerialNo()));
											showMessage(Dump.getHexDump(qPResult.getCardSerialNo()));
										}

										if (qPResult.getATQA() == null) {
											showMessage("RF Card ATQA:null");
										} else {
											showMessage("RF Card ATQA:" + Dump.getHexDump(qPResult.getATQA()));
										}
										showMessage("Power on sucess");
										
										mCardReaderInterface.cancelCardRead();
									}catch(Exception e){
										showMessage("Power on Exception:" + e.getMessage());
										mChannelMgr.setBusy(false);
									}
									
									
								}
							}).start();
							nccard_dialog.dismiss();

						} catch (Exception e) {
							showMessage("Power on Exception:" + e.getMessage());
							mChannelMgr.setBusy(false);
						}

					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						nccard_dialog.dismiss();
						mChannelMgr.setBusy(false);
					}
				});
				mChannelMgr.setBusy(false);
				nccard_dialog = builder.create();
				nccard_dialog.show();
				nccard_dialog.setCancelable(false);
				nccard_dialog.setCanceledOnTouchOutside(false);
			}
		});
	}

		//装载AID
		private void LoadAID(){
			showMessage("Loading AID...");			
			AIDConfig aidConfig = new AIDConfig();
			aidConfig.setAid(ISOUtils.hex2byte("A000000333010102"));// 0x9f06
			aidConfig.setAppSelectIndicator(0);// 0xDF01
			aidConfig.setAppVersionNumberTerminal(new byte[] { 0x00, (byte) 0x20 });// 0x9f09
			aidConfig.setTacDefault(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF11
			aidConfig.setTacOnLine(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF12
			aidConfig.setTacDenial(ISOUtils.hex2byte("0010000000"));// 0xDF13
			aidConfig.setTerminalFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x05 });// 0x9f1b
			aidConfig.setThresholdValueForBiasedRandomSelection(new byte[] { 0x00, 0x00, 0x00,
					(byte) 0x28 });// 0xDF15
			aidConfig.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
			aidConfig.setTargetPercentageForRandomSelection(14);// 0xDF17
			aidConfig.setDefaultDDOL(ISOUtils.hex2byte("9F3704"));// 0xDF14
			aidConfig.setOnLinePinCapability(1);// 0xDF18
			aidConfig.setEcTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0x9F7B
			aidConfig.setNciccOffLineFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF19
			aidConfig.setNciccTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF20
			aidConfig.setNciccCVMLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00 });// 0xDF21
			aidConfig.setEcCapability(0);// 0xDF24
			aidConfig.setCoreConfigType(2);// 0xDF25
			//boolean addAIDResult1 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				//addAIDResult1 = mK21EmvInterface.addAID(aidConfig);
			} else {
				//addAIDResult1 = mEmvInterface.addAID(aidConfig);
			}*/
			//showMessage("添加AID结果:" + addAIDResult1);

			AIDConfig aidConfig2 = new AIDConfig();
			aidConfig2.setAid(ISOUtils.hex2byte("A000000333010101"));// 0x9f06
			aidConfig2.setAppSelectIndicator(0);// 0xDF01
			aidConfig2.setAppVersionNumberTerminal(new byte[] { 0x00, (byte) 0x20 });// 0x9f09
			aidConfig2.setTacDefault(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF11
			aidConfig2.setTacOnLine(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF12
			aidConfig2.setTacDenial(ISOUtils.hex2byte("0010000000"));// 0xDF13
			aidConfig2.setTerminalFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x05 });// 0x9f1b
			aidConfig2.setThresholdValueForBiasedRandomSelection(new byte[] { 0x00, 0x00, 0x00,
					(byte) 0x28 });// 0xDF15
			aidConfig2.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
			aidConfig2.setTargetPercentageForRandomSelection(14);// 0xDF17
			aidConfig2.setDefaultDDOL(ISOUtils.hex2byte("9F3704"));// 0xDF14
			aidConfig2.setOnLinePinCapability(1);// 0xDF18
			aidConfig2.setEcTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0x9F7B
			aidConfig2.setNciccOffLineFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF19
			aidConfig2.setNciccTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF20
			aidConfig2.setNciccCVMLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00 });// 0xDF21
			aidConfig2.setEcCapability(0);// 0xDF24
			aidConfig2.setCoreConfigType(2);// 0xDF25
			//boolean addAIDResult2 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				//addAIDResult2 = mK21EmvInterface.addAID(aidConfig2);
			} else {
				//addAIDResult2 = mEmvInterface.addAID(aidConfig2);
			}*/
			//showMessage("添加AID2结果:" + addAIDResult2);
			AIDConfig aidConfig3 = new AIDConfig();
			aidConfig3.setAid(ISOUtils.hex2byte("A000000333010103"));// 0x9f06
			aidConfig3.setAppSelectIndicator(0);// 0xDF01
			aidConfig3.setAppVersionNumberTerminal(new byte[] { 0x00, (byte) 0x20 });// 0x9f09
			aidConfig3.setTacDefault(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF11
			aidConfig3.setTacOnLine(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF12
			aidConfig3.setTacDenial(ISOUtils.hex2byte("0010000000"));// 0xDF13
			aidConfig3.setTerminalFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x05 });// 0x9f1b
			aidConfig3.setThresholdValueForBiasedRandomSelection(new byte[] { 0x00, 0x00, 0x00,
					(byte) 0x28 });// 0xDF15
			aidConfig3.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
			aidConfig3.setTargetPercentageForRandomSelection(14);// 0xDF17
			aidConfig3.setDefaultDDOL(ISOUtils.hex2byte("9F3704"));// 0xDF14
			aidConfig3.setOnLinePinCapability(1);// 0xDF18
			aidConfig3.setEcTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0x9F7B
			aidConfig3.setNciccOffLineFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF19
			aidConfig3.setNciccTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF20
			aidConfig3.setNciccCVMLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00 });// 0xDF21
			aidConfig3.setEcCapability(0);// 0xDF24
			aidConfig3.setCoreConfigType(2);// 0xDF25
			//boolean addAIDResult3 = false;

			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				//addAIDResult3 = mK21EmvInterface.addAID(aidConfig3);
			} else {
				//addAIDResult3 = mEmvInterface.addAID(aidConfig3);
			}*/
			
			//showMessage("添加AID3结果:" + addAIDResult3);
			AIDConfig aidConfig4 = new AIDConfig();
			aidConfig4.setAid(ISOUtils.hex2byte("A000000333010106"));// 0x9f06
			aidConfig4.setAppSelectIndicator(0);// 0xDF01
			aidConfig4.setAppVersionNumberTerminal(new byte[] { 0x00, (byte) 0x20 });// 0x9f09
			aidConfig4.setTacDefault(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF11
			aidConfig4.setTacOnLine(ISOUtils.hex2byte("FC78FCF8F0"));// 0xDF12
			aidConfig4.setTacDenial(ISOUtils.hex2byte("0010000000"));// 0xDF13
			aidConfig4.setTerminalFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x05 });// 0x9f1b
			aidConfig4.setThresholdValueForBiasedRandomSelection(new byte[] { 0x00, 0x00, 0x00,
					(byte) 0x28 });// 0xDF15
			aidConfig4.setMaxTargetPercentageForBiasedRandomSelection(32);// 0xDF16
			aidConfig4.setTargetPercentageForRandomSelection(14);// 0xDF17
			aidConfig4.setDefaultDDOL(ISOUtils.hex2byte("9F3704"));// 0xDF14
			aidConfig4.setOnLinePinCapability(1);// 0xDF18
			aidConfig4.setEcTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0x9F7B
			aidConfig4.setNciccOffLineFloorLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF19
			aidConfig4.setNciccTransLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });// 0xDF20
			aidConfig4.setNciccCVMLimit(new byte[] { 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00 });// 0xDF21
			aidConfig4.setEcCapability(0);// 0xDF24
			aidConfig4.setCoreConfigType(2);// 0xDF25

			//boolean addAIDResult4 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				//addAIDResult4 = mK21EmvInterface.addAID(aidConfig4);
			} else {
				//addAIDResult4 = mEmvInterface.addAID(aidConfig4);
			}*/

			showMessage("AID loaded!");
		}
		
		//装载工作密钥
		/*private void LoadWorkKey( PinInputInterfaceImpl pinInputInterface ){
			showMessage("Work key...");
			int mkIndex = Const.MKIndexConst.DEFAULT_MK_INDEX;
			byte[] wk_pin = pinInputInterface.loadWorkingKey(WorkingKeyType.PININPUT,
					mkIndex, Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX,
					ISOUtils.hex2byte(WORKINGKEY_DATA_PIN), ISOUtils.hex2byte("58A2BBF9"));
			if (wk_pin != null) {
				showMessage("pin work key success!");
				//processingUnLock();
			} else {
				showMessage("pin work key failed!");
				//processingUnLock();
			}

			byte[] wk_encrypt = pinInputInterface.loadWorkingKey(
					WorkingKeyType.DATAENCRYPT, mkIndex,
					Const.DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX,
					ISOUtils.hex2byte(WORKINGKEY_DATA_TRACK), ISOUtils.hex2byte("5B4C8BED"));
			if (wk_encrypt != null) {
				showMessage("Track work key success!");
				//processingUnLock();
			} else {
				showMessage("Track work key failed!");
				//processingUnLock();
			}

			byte[] wk_mac = pinInputInterface.loadWorkingKey(WorkingKeyType.MAC, mkIndex,
					Const.MacWKIndexConst.DEFAULT_MAC_WK_INDEX,
					ISOUtils.hex2byte(WORKINGKEY_DATA_MAC), ISOUtils.hex2byte("5B4C8BED"));
			if (wk_mac != null) {
				showMessage("mac work key success!");
				//processingUnLock();
			} else {
				showMessage("mac work key failed!");
			}
		}*/
		
		
		//装载公开密钥
		private void LoadPublicKey( EmvInterfaceImpl emvInterface ){
			showMessage("Loading public key...");
			int P9f22_1 = 1;
			keyIndex = P9f22_1;
			byte[] df02_2 = ISOUtils
					.hex2byte("BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93B");
			byte[] df04_2 = ISOUtils.hex2byte("000003");
			byte[] df03_2 = ISOUtils.hex2byte("E881E390675D44C2DD81234DCE29C3F5AB2297A0");
			CAPublicKey caKey2 = new CAPublicKey(P9f22_1, 1, 1, df02_2, df04_2, df03_2, "20091231");
			boolean addCAPK2 = false;
			
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				addCAPK2 = mK21EmvInterface.addCAPublicKey(rid, caKey2);
			} else {
				addCAPK2 = mEmvInterface.addCAPublicKey(rid, caKey2);
			}*/
			
			addCAPK2 = emvInterface.addCAPublicKey(rid, caKey2);
			
			//showMessage("添加公钥1结果:" + addCAPK2);
			//showMessage("索引号为:" + P9f22_1);

			int P9f22_2 = 2;
			keyIndex = P9f22_2;
			byte[] df02 = ISOUtils
					.hex2byte("A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57");
			byte[] df04 = ISOUtils.hex2byte("000003");
			byte[] df03 = ISOUtils.hex2byte("03BB335A8549A03B87AB089D006F60852E4B8060");
			CAPublicKey caKey1 = new CAPublicKey(P9f22_2, 1, 1, df02, df04, df03, "20141231");
			boolean addCAPK1 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				addCAPK1 = mK21EmvInterface.addCAPublicKey(rid, caKey1);
			} else {
				addCAPK1 = mEmvInterface.addCAPublicKey(rid, caKey1);
			}*/
			addCAPK1 = emvInterface.addCAPublicKey(rid, caKey1);
			
			//showMessage("添加公钥2结果:" + addCAPK1);
			//showMessage("索引号为:" + P9f22_2);

			int P9f22_3 = 3;
			keyIndex = P9f22_3;
			byte[] df02_3 = ISOUtils
					.hex2byte("B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33D");
			byte[] df04_3 = ISOUtils.hex2byte("000003");
			byte[] df03_3 = ISOUtils.hex2byte("87F0CD7C0E86F38F89A66F8C47071A8B88586F26");
			CAPublicKey caKey3 = new CAPublicKey(P9f22_3, 1, 1, df02_3, df04_3, df03_3, "20171231");
			boolean addCAPK3 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				addCAPK3 = mK21EmvInterface.addCAPublicKey(rid, caKey3);
			} else {
				addCAPK3 = mEmvInterface.addCAPublicKey(rid, caKey3);
			}*/
			addCAPK3 = emvInterface.addCAPublicKey(rid, caKey3);

			//showMessage("添加公钥3结果:" + addCAPK3);
			//showMessage("索引号为:" + P9f22_3);

			int P9f22_4 = 4;
			keyIndex = P9f22_4;
			byte[] df02_4 = ISOUtils
					.hex2byte("BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1");
			byte[] df04_4 = ISOUtils.hex2byte("000003");
			byte[] df03_4 = ISOUtils.hex2byte("F527081CF371DD7E1FD4FA414A665036E0F5E6E5");
			CAPublicKey caKey4 = new CAPublicKey(P9f22_4, 1, 1, df02_4, df04_4, df03_4, "20171231");

			boolean addCAPK4 = false;
			/*if (connectType.equals(Const.ConnectTypeName.K21)) {
				mK21EmvInterface.initEmvModule(MyApplication.activity);
				addCAPK4 = mK21EmvInterface.addCAPublicKey(rid, caKey4);
			} else {
				addCAPK4 = mEmvInterface.addCAPublicKey(rid, caKey4);
			}*/
			
			addCAPK4 = emvInterface.addCAPublicKey(rid, caKey4);

			//showMessage("添加公钥4结果:" + addCAPK4);
			showMessage("Public key loaded!");
		}
		
		
		void showMessage( final String message){
			mActivity.runOnUiThread( new Runnable(){
				@Override
				public void run() {
					mAdapterMessage.add(message, true);
				}} );			
			
		}
		
		public void clearMessages() {
	 		
			mActivity.runOnUiThread( new Runnable(){

				@Override
				public void run() {
					mAdapterMessage.removeAll();
				}} );	
	 	}
}
