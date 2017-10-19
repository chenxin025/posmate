/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import com.cynoware.posmate.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

public class DeviceActivity extends Activity {

	private boolean isExit = false;
	PowerManager pm;
	PowerManager.WakeLock mWakeLock;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 保持屏幕常亮
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PosMate");
	}

	/*
	 * 双击退出
	 */
	@SuppressLint("HandlerLeak")
	Handler exitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			isExit = false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			//exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(this,
					this.getResources().getString(R.string.TIP_Press_To_Exit),
					Toast.LENGTH_SHORT).show();
			exitHandler.sendEmptyMessageDelayed(0, 2000);
		} else {
			/*Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			System.exit(0);*/
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
