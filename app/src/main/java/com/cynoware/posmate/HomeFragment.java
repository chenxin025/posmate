/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {

	public HomeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_home, container,
				false);
		
		MyApplication app = (MyApplication)getActivity().getApplication();
		String ver = app.getAppVersion();
		
		if( ver != null ){
			TextView tvVersion = (TextView) rootView.findViewById(R.id.tvVersion);
			tvVersion.setText(ver);
		}

		return rootView;
	}

}
