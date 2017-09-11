package com.cynoware.webapp;

import android.content.Context;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by JieZhuang on 2017/9/5.
 */

public class WebAppJS {

    private Context mContext;

    public WebAppJS(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void showToast(String webMessage) {
        Toast.makeText(mContext, webMessage, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getDeviceID() {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @JavascriptInterface
    public String getDeviceType() {
        String type = android.os.Build.MODEL;
        if (type.equals("SABRESD-MX6DQ"))
            return "N1193";
        else if (type.equals("intel_cht"))
            return "NP10";

        return type;
    }
}
