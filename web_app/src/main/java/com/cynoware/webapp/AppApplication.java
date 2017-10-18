package com.cynoware.webapp;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by chenxin on 2017/10/18.
 */

public class AppApplication extends Application {

    private static final String TAG = "WebAppApplication";

    private static AppApplication mInstance = null;
    private String mAppChannel = null;

    public static final String CHANNEL_BILLING = "billing";
    public static final String CHANNEL_COLLECT = "collectapp";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG,"##onLowMemory##");
    }

    public static AppApplication getmInstance(){
        return mInstance;
    }

    private void init(){
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            setmAppChannel(appInfo.metaData.getString("CYNO_CHANNEL"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            setmAppChannel(null);
        }

    }


    public String getmAppChannel() {
        return mAppChannel;
    }

    public void setmAppChannel(String mAppChannel) {
        this.mAppChannel = mAppChannel;
    }
}
