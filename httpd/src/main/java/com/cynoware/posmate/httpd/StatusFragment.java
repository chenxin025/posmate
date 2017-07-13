/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class StatusFragment extends Fragment {

    private MainActivity mActivity;
    private TextView mTvServerStatus, mTvServerURL, mTvPOSSuite, mTVDeviceID, mTvDockUSB, mTvDockUSBLabel, mTvTrayUSB, mTvTrayUSBLabel;
    private Button mBtnSwitchServer;
    private ServerService mServerService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        mTvServerStatus = (TextView) rootView.findViewById(R.id.tvServerStatus);
        mTVDeviceID = (TextView) rootView.findViewById(R.id.tvDeviceID);
        mTvPOSSuite = (TextView) rootView.findViewById(R.id.tvSuite);
        mTvDockUSB = (TextView) rootView.findViewById(R.id.tvDockUSB);
        mTvDockUSBLabel = (TextView) rootView.findViewById(R.id.tvDockUSBLabel);
        mTvTrayUSB = (TextView) rootView.findViewById(R.id.tvTrayUSB);
        mTvTrayUSBLabel = (TextView) rootView.findViewById(R.id.tvTrayUSBLabel);
        mTvServerURL = (TextView) rootView.findViewById(R.id.tvServerURL);

        mBtnSwitchServer = (Button) rootView.findViewById(R.id.btnSwitchServer);

        mBtnSwitchServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBtnSwitchServer.getText().equals("START")) {
                    Intent intent = new Intent(ServerService.ACTION_START_SERVER);
                    mActivity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(ServerService.ACTION_STOP_SERVER);
                    mActivity.sendBroadcast(intent);
                }
            }
        });

        checkStatus();

        return rootView;
    }


    private void checkStatus() {
        ServerService serverService = mActivity.getServerService();
        if (serverService == null) {
            mTvServerStatus.setText("Unknown");
            mBtnSwitchServer.setVisibility(View.INVISIBLE);
        } else {
            mBtnSwitchServer.setVisibility(View.VISIBLE);

            if (serverService.isServerRunning()) {
                mTvServerStatus.setText("Running");
                mTvServerURL.setText("http://127.0.0.1:8080");
                mBtnSwitchServer.setText("STOP");
            } else {
                mTvServerStatus.setText("Stopped");
                mTvServerURL.setText("N/A");
                mBtnSwitchServer.setText("START");
            }


            switch (serverService.getSuite()) {
                case ServerService.SUITE_NP10:
                    mTvPOSSuite.setText("NP10");
                    mTvDockUSBLabel.setVisibility(View.VISIBLE);
                    mTvDockUSB.setVisibility(View.VISIBLE);
                    mTvDockUSB.setText(serverService.isDockUsbAttached() ? "Attached" : "Detached");

                    mTvTrayUSBLabel.setVisibility(View.VISIBLE);
                    mTvTrayUSB.setVisibility(View.VISIBLE);
                    mTvTrayUSB.setText(serverService.isTrayUsbAttached() ? "Attached" : "Detached");
                    break;

                case ServerService.SUITE_NP11:
                    mTvPOSSuite.setText("NP11");
                    mTvDockUSBLabel.setVisibility(View.GONE);
                    mTvDockUSB.setVisibility(View.GONE);

                    mTvTrayUSBLabel.setVisibility(View.VISIBLE);
                    mTvTrayUSB.setVisibility(View.VISIBLE);
                    mTvTrayUSB.setText(serverService.isTrayUsbAttached() ? "Attached" : "Detached");
                    break;

                case ServerService.SUITE_P140:
                    mTvPOSSuite.setText("P140");
                    mTvDockUSBLabel.setVisibility(View.GONE);
                    mTvDockUSB.setVisibility(View.GONE);
                    mTvTrayUSBLabel.setVisibility(View.GONE);
                    mTvTrayUSB.setVisibility(View.GONE);
                    break;
            }


            mTVDeviceID.setText(serverService.getDeviceID());
        }
    }

    private BroadcastReceiver mServerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            checkStatus();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerService.ACTION_SERVER_STARTED);
        filter.addAction(ServerService.ACTION_SERVER_STOPPED);
        filter.addAction(ServerService.ACTION_TRAY_USB_ATTACHED);
        filter.addAction(ServerService.ACTION_TRAY_USB_DETACHED);
        filter.addAction(ServerService.ACTION_DOCK_USB_ATTACHED);
        filter.addAction(ServerService.ACTION_DOCK_USB_DETACHED);


        filter.addAction(MainActivity.ACTION_SERVICE_BIND);
        filter.addAction(MainActivity.ACTION_SERVICE_UNBIND);


        getActivity().registerReceiver(mServerBroadcastReceiver, filter);
    }


    @Override
    public void onDetach() {
        super.onDetach();

        mActivity.unregisterReceiver(mServerBroadcastReceiver);
    }

}
