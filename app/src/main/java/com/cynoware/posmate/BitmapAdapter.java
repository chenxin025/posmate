package com.cynoware.posmate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by john on 2016/9/2.
 */
public class BitmapAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<BitmapParams> mData;
    private static final int CORE_POOL_SIZE = 5;
    private static final int TASK_CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(
            10);

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            sPoolWorkQueue, new ThreadPoolExecutor.DiscardOldestPolicy());

    public static final Executor TASK_POOL_EXECUTOR = new ThreadPoolExecutor(
            TASK_CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            sPoolWorkQueue, new ThreadPoolExecutor.DiscardOldestPolicy());

    public BitmapAdapter(Context context,ArrayList<BitmapParams> array){
        mContext = context;
        mData = array;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        BitmapParams params = mData.get(i);
        ViewHolder viewHolder = null;

        if (null == view){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.printfbmp_listview_item,viewGroup,false);
            viewHolder.imageView = (ImageView)view.findViewById(R.id.itemImage);
            viewHolder.bmpTextView = (TextView)view.findViewById(R.id.itemTitle);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.bmpTextView.setText(params.name);

//        Bitmap bitmap = null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        InputStream is;
//        try {
//            is = mContext.getAssets().open("printfbmps/"+params.name);
//            bitmap = BitmapFactory.decodeStream(is,null,options);
//            is.close();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        viewHolder.imageView.setImageBitmap(bitmap);
        viewHolder.imageView.setImageResource(R.drawable.ic_launcher);
        viewHolder.imageView.setTag(params.name);
       viewHolder.task = new ImageLoadedTask(mContext,params.name,viewHolder.imageView,false,i);
       viewHolder.task.executeOnExecutor(THREAD_POOL_EXECUTOR, (Void[]) null);
        view.setFocusable(false);
        return view;
    }

    private static class ViewHolder{
        ImageView imageView;
        TextView bmpTextView;
        ImageLoadedTask task;
    }



}
