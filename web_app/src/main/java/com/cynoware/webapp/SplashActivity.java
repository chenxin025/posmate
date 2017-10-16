/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.webapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static final int TIME_OUT = 3000;

    public static String SYSTEMUI_WEAK_SHOWBAR_ACTION = "com.android.systembar.weak.show";
    public static String SYSTEMUI_FIX_HIDEBAR_ACTION = "com.android.systembar.fix.hide";

    private boolean mIsServerStarted = false;
    private TextView tvDev;

    private SharedPreferences mPreference;
    private boolean mIsDevMode = false;

//    private String URL_TEST = "http://ceshigt.zuanno.cn";
    //private String URL_TEST = "http://192.168.1.55:8080";
    private String URL_TEST = "http://ceshiht.zuanno.cn/a";
    private String URL_PROD = "http://gt.zuanno.cn";

    private String mUrl;
    private int mClickCount = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreference = getSharedPreferences("setting", 0);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        hideStatusBar();

        setContentView(R.layout.activity_splash);

        TextView tvLogo = (TextView) findViewById(R.id.tvLogo);
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvDev = (TextView) findViewById(R.id.tvDev);


        tvVersion.setText(getAppVersionName(this));

        tvLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickCount++;
                if (mClickCount == 3) {
                    if (!mIsDevMode) {
                        setDevMode(true);
                        Toast.makeText(SplashActivity.this, "TEST MODE ON", Toast.LENGTH_SHORT).show();
                    } else {
                        setDevMode(false);
                        Toast.makeText(SplashActivity.this, "TEST MODE OFF", Toast.LENGTH_SHORT).show();
                    }

                    applyDevMode();

                    mClickCount = 0;
                }
            }
        });

        loadDevMode();
        applyDevMode();

        mHandler.postDelayed(mRunnable, TIME_OUT);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            startWebViewActivity(mUrl);
        }
    };


    public void loadDevMode() {
        mIsDevMode = mPreference.getBoolean("dev_mode", false);
    }

    public void setDevMode(boolean value) {
        mIsDevMode = value;
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean("dev_mode", mIsDevMode);
        editor.apply();
    }

    public void applyDevMode() {
        mUrl = mIsDevMode ? URL_TEST : URL_PROD;
        tvDev.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void startWebViewActivity(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        finish();
    }

}
