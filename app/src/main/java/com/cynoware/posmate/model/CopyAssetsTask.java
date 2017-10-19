package com.cynoware.posmate.model;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.cynoware.posmate.sdk.SDKLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by john on 2016/9/2.
 */
public class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "CopyAssetsTask";
    private Context mContext;


    public CopyAssetsTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //copy ASC16  and HZK16 to data dir
        SDKLog.i(TAG,"copyAssets====================start");
        boolean success = false;
        success = copyAssets("ASC16");
        SDKLog.i(TAG,"copyAssets=========ASC16===========end =="+ success);
        success = copyAssets("HZK16");
        SDKLog.i(TAG,"copyAssets=========HZK16===========end =="+ success);
        return null;
    }

    private boolean copyAssets(final  String fileName){
        if (TextUtils.isEmpty(fileName)){
            return false;
        }
        InputStream in = null;
        FileOutputStream out = null;
        String path = mContext.getApplicationContext().getFilesDir().getAbsolutePath() +File.separator+ fileName;
        File file = new File(path);

        if (!file.exists()){
            try {
                in = mContext.getAssets().open(fileName);
                out = new FileOutputStream(file);

                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1){
                    out.write(buf, 0, length);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (in != null ) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null ) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}
