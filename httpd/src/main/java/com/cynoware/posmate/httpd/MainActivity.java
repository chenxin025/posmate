/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static final String ACTION_SERVICE_BIND = "POS_MATE_SERVER_SERVICE_BIND";
    public static final String ACTION_SERVICE_UNBIND = "POS_MATE_SERVER_SERVICE_UNBIND";

    private static final String LOG_TAG = "MainActivity";

    private ServerService mServerService = null;
    private boolean mIsServiceBound;

    private String[] mMenuTitle = {"Status", "Setting", "Exit"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        Intent intent = new Intent(this, ServerService.class);
        bindService(intent, mServerServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsServiceBound) {
            unbindService(mServerServiceConnection);
            mIsServiceBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void initViews() {
        setContentView(R.layout.activity_main);

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText(getAppVersionName(this));

        Fragment statusFragment = new StatusFragment();
        loadFragment(statusFragment);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMenuTitle);

        ListView lvMenu = (ListView) findViewById(R.id.lvMenu);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        loadFragment(new StatusFragment());
                        break;

                    case 1:
                        loadFragment(new SettingFragment());
                        break;

                    case 2:
                        finish();
                        break;
                }
            }
        });
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            if (null != info) {
                return info.versionName;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return null;
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.layoutContainer, fragment);
        transaction.commit();
    }


    private ServiceConnection mServerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(LOG_TAG, "Service connected");
            ServerService.LocalBinder binder = (ServerService.LocalBinder) service;
            mServerService = binder.getService();
            mIsServiceBound = true;
            Intent intent = new Intent(ACTION_SERVICE_BIND);
            sendBroadcast(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(LOG_TAG, "Service disconnected");
            mIsServiceBound = false;
            mServerService = null;
            Intent intent = new Intent(ACTION_SERVICE_UNBIND);
            sendBroadcast(intent);
        }
    };

    public ServerService getServerService() {
        return mServerService;
    }
}
