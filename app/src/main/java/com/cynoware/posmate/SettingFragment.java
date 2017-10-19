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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;


public class SettingFragment extends Fragment {

	public static final int REQUEST_SETTING_BT_CHANGE = 102; 
	//private ChannelManager mChannelMgr;

	private Setting mSetting;
	TextView mTvDockBTDesp, mTvCardBTDesp;
	ImageView mImgChannelTrayUSB;

	public SettingFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MainActivity activity = (MainActivity) this
				.getActivity();
		
		mImgChannelTrayUSB = (ImageView) activity
				.findViewById(R.id.imgChannelTrayUSB);
		
		mSetting = Setting.getInstance(activity);


		View rootView = inflater.inflate(R.layout.activity_setting, 
				container, false);


		TextView tvAppVersion = (TextView) rootView.findViewById(R.id.app_version_key);
		TextView tvJarVersion = (TextView) rootView.findViewById(R.id.jar_version_key);
		tvAppVersion.setText("app version: "+MyApplication.getVersion());
		//tvJarVersion.setText("sdk version: "+ config.getJarVersion());
		// POS Type
		RadioGroup group = (RadioGroup) rootView
				.findViewById(R.id.radioGroupPOSType);
		String model = mSetting.getPosModel();
		RadioButton radio = null;
		
		if (model.equals(Setting.MODEL_POS_MATE))
			radio = (RadioButton) group.findViewById(R.id.radioPOSMate);
		else if (model.equals(Setting.MODEL_NP10))
			radio = (RadioButton) group.findViewById(R.id.radioNP10);
		
		radio.setChecked(true);

		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int radioButtonId = arg0.getCheckedRadioButtonId();
				if (radioButtonId == R.id.radioPOSMate) {
					setTypePosMate();
				} else if (radioButtonId == R.id.radioNP10) {
					setTypeNP10();
				}
			}
		});

		// DOCK USB
		Switch switchDockUSB = (Switch) rootView
				.findViewById(R.id.switchDockUSB);
//		switchDockUSB.setChecked(mSetting
//				.getChannelEnable(ChannelManager.CHANNEL_DOCK_USB));

		switchDockUSB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isChecked)
					openDockAndTrayUSB();
				else
					closeDockAndTrayUSB();
				}
			});

		// DOCK BT
		Switch switchDockBT = (Switch) rootView.findViewById(R.id.switchDockBT);

//		switchDockBT.setChecked(mSetting
//				.getChannelEnable(ChannelManager.CHANNEL_DOCK_BT));

		switchDockBT
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {						
						if (isChecked)
							openDockBT();							
						else
							closeDockBT();
							
					}
				});

		mTvDockBTDesp = (TextView) rootView.findViewById(R.id.tvDockConnBTDesp);
		
		String strDockBTDesp = mSetting.getDockBTName() + "("
				+ mSetting.getDockBTAddr() + ")";
		
		mTvDockBTDesp.setText(strDockBTDesp);

		ImageView imgDockBTChange = (ImageView) rootView
				.findViewById(R.id.imgDockBTChange);
		imgDockBTChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseDockBTDevice();				
			}
		});

		/*
		 * RadioGroup rgDockConn =
		 * (RadioGroup)this.rootView.findViewById(R.id.radiogroupDockConn); if(
		 * mSetting.getDockConnection() == Setting.USB ){
		 * rgDockConn.check(R.id.radioDockConn_USB); }else if(
		 * mSetting.getDockConnection() == Setting.BT ){
		 * rgDockConn.check(R.id.radioDockConn_BT); }
		 * 
		 * rgDockConn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 * 
		 * @Override public void onCheckedChanged(RadioGroup group, int arg1) {
		 * int radioId = group.getCheckedRadioButtonId(); //RadioButton rb =
		 * (RadioButton)group.rootView.findViewById(radioButtonId); if( radioId
		 * == R.id.radioDockConn_USB ){ mSetting.setDockConnection(Setting.USB);
		 * }else if( radioId == R.id.radioDockConn_BT ){
		 * mSetting.setDockConnection(Setting.BT); Intent intent = new Intent(
		 * MainActivity.this, BTDevicesActivity.class ); startActivity(intent);
		 * 
		 * } } });
		 */

		// CARD BT
		/*Switch switchCardBT = (Switch) rootView.findViewById(R.id.switchCardBT);

		switchCardBT.setChecked(mSetting
				.getChannelEnable(ChannelManager.CHANNEL_CARD_BT));

		switchCardBT
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						
						if (isChecked) {
							initCardBT();
						} else
							mChannelMgr.close(ChannelManager.CHANNEL_CARD_BT);
					}
				});*/

		mTvCardBTDesp = (TextView) rootView.findViewById(R.id.tvCardBTDesp);
		String strCardBTDesp = mSetting.getCardBTName() + "("
				+ mSetting.getCardBTAddr() + ")";
		mTvCardBTDesp.setText(strCardBTDesp);

		ImageView imgCardBTChange = (ImageView) rootView
				.findViewById(R.id.imgCardBTChange);
		imgCardBTChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseCardBTDevice();				
			}
		});

		/*Spinner spinner = (Spinner) rootView.findViewById(R.id.spinnerCardType);
		String channels[] = { "ME30", "ME31", "IM81", "N900", "AUDIO" };
		Adapter adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_list_item_1, channels);
		spinner.setAdapter((SpinnerAdapter) adapter);

		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					break;

				case 1:
					break;

				case 2:
					break;

				case 3:
					break;

				case 4:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});*/

		return rootView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if( requestCode == REQUEST_SETTING_BT_CHANGE ){    		
			if( resultCode == Activity.RESULT_OK ){
	    		String name = data.getStringExtra("name");
	    		String addr = data.getStringExtra("addr");
	    		String channel = data.getStringExtra("channel");
	    		
	    		if( channel.equals("dock_bt") ){
	    			mSetting.setDockBTName(name);
	    			mSetting.setDockBTAddr(addr);
	    			mTvDockBTDesp.setText( name + "(" + addr + ")" );
	    		}else{
	    			mSetting.setCardBTName(name);
	    			mSetting.setCardBTAddr(addr);
	    			mTvCardBTDesp.setText( name + "(" + addr + ")" );
	    			
	    			//if( mSetting.getChannelEnable(ChannelManager.CHANNEL_CARD_BT))	    			
	    			//mChannelMgr.open(ChannelManager.CHANNEL_CARD_BT);
	    		}
	    	}
		}
	}

	private void setTypePosMate() {
		mSetting.setPosModel(Setting.MODEL_POS_MATE);
		mImgChannelTrayUSB.setVisibility(View.GONE);
	}

	private void setTypeNP10() {
		mSetting.setPosModel(Setting.MODEL_NP10);
		mImgChannelTrayUSB.setVisibility(View.VISIBLE);
	}

	private void openDockAndTrayUSB() {
//		mSetting.setChannelEnable( ChannelManager.CHANNEL_DOCK_USB, true );
//		mChannelMgr.open(ChannelManager.CHANNEL_DOCK_USB);
	}

	private void closeDockAndTrayUSB() {
//		mSetting.setChannelEnable( ChannelManager.CHANNEL_DOCK_USB, false );
//		mSetting.setChannelEnable( ChannelManager.CHANNEL_TRAY_USB, false );
//		mChannelMgr.close(ChannelManager.CHANNEL_DOCK_USB);
//		mChannelMgr.close(ChannelManager.CHANNEL_TRAY_USB);
	}

	private void openDockBT() {
//		mSetting.setChannelEnable( ChannelManager.CHANNEL_DOCK_BT, true);
//		mChannelMgr.open(ChannelManager.CHANNEL_DOCK_BT);
	}

	private void closeDockBT() {
//		mSetting.setChannelEnable( ChannelManager.CHANNEL_DOCK_BT, false);
//		mChannelMgr.close(ChannelManager.CHANNEL_DOCK_BT);
	}
	
	private void chooseDockBTDevice(){
		Intent intent = new Intent(this.getActivity(),BTDevicesActivity.class);
		intent.putExtra("channel", "dock_bt");
		startActivityForResult(intent,REQUEST_SETTING_BT_CHANGE);		
	}

	/*private void initCardBT() {
		mSetting.setChannelEnable(ChannelManager.CHANNEL_CARD_BT, true);
		mChannelMgr.open(ChannelManager.CHANNEL_CARD_BT);
	}*/
	
	private void chooseCardBTDevice(){
		Intent intent = new Intent(this.getActivity(), BTDevicesActivity.class);
		intent.putExtra("channel", "card_bt");
		startActivityForResult(intent, REQUEST_SETTING_BT_CHANGE);		
	}

}
