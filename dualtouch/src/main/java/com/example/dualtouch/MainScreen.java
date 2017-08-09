package com.example.dualtouch;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.widget.Button;
import android.app.Dialog;
import java.util.Arrays;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.BaseAdapter;
import android.widget.AdapterView;

public class MainScreen extends DualTouch
	implements View.OnClickListener {

    private static final String TAG = "DualTouch";
    
    //private TextView mInfoTextView;
    private VideoView mVideoView2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_display_activity);
        Button button = (Button)findViewById(R.id.button_screen);
        button.setOnClickListener(this);
        
        setRequestedOrientation( android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //setRequestedOrientation( android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Get a text view where we will show information about what's happening.
        //mInfoTextView = (TextView)findViewById(R.id.main_display_info);
        if(true){
        mVideoView2 = (VideoView)findViewById(R.id.videoView1);
        mVideoView2.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() +
                "/" + R.raw.demo));
        //mVideoView.setMediaController(new MediaController(this.mContext));
        //mVideoView.requestFocus();
        
        mVideoView2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {   
            public void onPrepared(MediaPlayer mp) {  
                mp.start();// 播放  
                mp.setLooping(true);      
            }
        });  
        
        mVideoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { 
            public void onCompletion(MediaPlayer mp) {
               
                mVideoView2.setVideoURI(Uri.parse("android.resource://" + MainScreen.this.getPackageName() +
                        "/" + R.raw.demo));
                mVideoView2.start(); 
            } 
        });
        
        mVideoView2.start();
        }
    }
    
    @Override
    public void onClick(View v){
        if(v instanceof Button){
        	this.toggleSecond();
        }
    }
    
    protected void onUpdateContents() {
        super.onUpdateContents();
    }
}
