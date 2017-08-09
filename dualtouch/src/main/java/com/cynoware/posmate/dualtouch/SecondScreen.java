package com.cynoware.posmate.dualtouch;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

public class SecondScreen extends Presentation
    implements View.OnClickListener {
    static final String TAG = "DualTouch";

    private DualApplication mApp;
    private Context mContext;
    private VideoView mVideoView;
    private ImageView mImageView;
    private Button mButtonLike;
    private Button mButtonPrev;
    private Button mButtonNext;
    private Button mButtonScreen;
    private Handler mHandler;
    
    public SecondScreen(Context context, Display display) {
        super(context, display, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        display.getDisplayId();
        this.mContext = context;
        mApp = (DualApplication)((Activity)context).getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.secondary_display_activity);
        
        mImageView = (ImageView)findViewById(R.id.imageView);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN)
                    next(1);
                return false;
            }  
        });
        
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {   
            public void onPrepared(MediaPlayer mp) {  
                //mp.start();
                mp.setLooping(true);      
            }
        });
        
        mButtonScreen = (Button)findViewById(R.id.button_screen);
        mButtonScreen.setOnClickListener(this);
        mButtonPrev = (Button)findViewById(R.id.button_prev);
        mButtonPrev.setOnClickListener(this);
        mButtonNext = (Button)findViewById(R.id.button_next);
        mButtonNext.setOnClickListener(this);
        
        mButtonLike = (Button)findViewById(R.id.button_like);
        mButtonLike.setOnClickListener(this);
        
        mButtonPrev.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                Log.d(TAG, String.format("%d", e.getAction()));
                if(e.getAction() == e.ACTION_CANCEL){
                    Log.d(TAG, String.format("%d", e.getAction()));
                    //return true;
                }
                return false;
            }
        });
        next(0);

        mHandler.postDelayed(mRunable, 2000);
    }


    private Runnable mRunable = new Runnable() {
        @Override
        public void run() {
            next(1);
            mHandler.postDelayed( this, 2000);
        }
    };

    int mClickCount = 0;
    int mLikeCount = 0;
    private void addLike(){
        DualApplication.Media media = mApp.medias[mApp.mMediaIndex];
        ++media.mLikeCount;
        updateLike(media);
    }
    private void updateLike(DualApplication.Media media){
        if(media.mLikeCount + 1 <= 9)
            mButtonLike.setText(String.format("+%d  ", media.mLikeCount + 1));
        else
            mButtonLike.setText(String.format("+%d ", media.mLikeCount + 1));
    }
    

    private void next(int add){
        mApp.mMediaIndex += (mApp.medias.length + add);
        mApp.mMediaIndex %= mApp.medias.length;
        DualApplication.Media media = mApp.medias[mApp.mMediaIndex];
        if(media.mIsVideo){
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mVideoView.setVideoURI(Uri.parse("android.resource://" + mContext.getPackageName() +
                    "/" + media.mResourceId));        
            mVideoView.start();
        }
        else{
            if(mVideoView.isPlaying())
                mVideoView.stopPlayback();
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);            
            
            if(false) getResources().getDrawable(media.mResourceId);
            else{
            mImageView.setImageDrawable(null);
            BitmapDrawable drawable = ((BitmapDrawable)mImageView.getDrawable());
            if(drawable != null){
                drawable.getBitmap().recycle();
            }
            System.gc();
            mImageView.setImageDrawable(getResources().getDrawable(media.mResourceId));
            }
        }
        updateLike(media);
    }
    
    @Override
    public void onClick(View v){
        if(v instanceof Button){
            if(v.equals(mButtonScreen))
            	((DualTouch)mContext).toggleSecond();
            else if(v.equals(mButtonLike))
                addLike();
            else if(v.equals(mButtonPrev))
                next(-1);
            else if(v.equals(mButtonNext))
                next(1);
        }
    }
}
