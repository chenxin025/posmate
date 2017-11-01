package com.cynoware.netcds;

import android.content.Context;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by JieZhuang on 2017/9/5.
 */

public class WebAppJS {

    private Context mContext;
    private TextToSpeech mTTS;
    private WebViewActivity mActivity;

    public WebAppJS(Context context) {
        mContext = context;
        mActivity = (WebViewActivity)context;

        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (isChinese())
                    mTTS.setLanguage(Locale.CHINESE);
                else
                    mTTS.setLanguage(Locale.ENGLISH);
            }
        });
    }

    public static boolean isChinese() {
        Locale locale = Locale.getDefault();
        return locale.equals(Locale.SIMPLIFIED_CHINESE) || locale.equals(Locale.TRADITIONAL_CHINESE);
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

    @JavascriptInterface
    public void playTTS( String text ) {
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}
