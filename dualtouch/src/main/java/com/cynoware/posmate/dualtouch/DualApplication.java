package com.cynoware.posmate.dualtouch;

import android.app.Application;


public class DualApplication extends Application{
    public int mMediaIndex = 0;
    public Media[] medias = {
            new Media(R.raw.s0),
            new Media(R.raw.s1),
            new Media(R.raw.s2),
            new Media(R.raw.s3),
            new Media(R.raw.s4),
            new Media(R.raw.s5),
            new Media(R.raw.s6),
            new Media(R.raw.s7),
            new Media(R.raw.s8),
            new Media(R.raw.s9),
            //new Media(R.raw.demo, true),
    };
    
    public class Media{
        int mResourceId;
        boolean mIsVideo;
        int mLikeCount;
        
        Media(int resourceId){
            mResourceId = resourceId;
            mIsVideo = false;
            mLikeCount = 0;
        }
        Media(int resourceId, boolean isVideo){
            mResourceId = resourceId;
            mIsVideo = isVideo;
            mLikeCount = 0;
        }
    };     
}