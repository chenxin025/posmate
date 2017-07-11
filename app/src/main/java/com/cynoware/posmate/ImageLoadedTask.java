package com.cynoware.posmate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.cynoware.posmate.sdk.SDKLog;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by john on 2016/9/2.
 */
public class ImageLoadedTask extends AsyncTask<Void, Void, Bitmap> {

    private Context mContext;
    private String mBmpName;
    private ImageView mImageView;
    private boolean mFromType;
    private int mId;

    public ImageLoadedTask(Context context, String name, ImageView imageView, boolean isDowned,int pos) {
        SDKLog.i("test","====================================pos:"+pos);
        mContext = context;
        mBmpName = name;
        mImageView = imageView;
        mFromType = isDowned;
        mId = pos;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        InputStream is;
        try {
            is = mContext.getAssets().open("printfbmps/"+mBmpName);
            bitmap = BitmapFactory.decodeStream(is,null,options);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        SDKLog.i("test","@@@@@@@@@@@@@@@@@@@="+mBmpName + "   id="+mId + "  bitmap="+bitmap);
        synchronized(ImageLoadedTask.this) {
            if (bitmap != null && mImageView.getTag().equals(mBmpName)){
                mImageView.setImageBitmap(bitmap);
            }

        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
