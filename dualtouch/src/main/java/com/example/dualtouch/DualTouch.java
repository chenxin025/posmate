package com.example.dualtouch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
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

@SuppressLint("NewApi")
public class DualTouch extends Activity {

    private static final String TAG = "DualTouch";
    
    private MediaRouter mMediaRouter;
    private SecondScreen mSecondScreen;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // Get the media router service.
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);        
    } 
    
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);

        // Update the presentation based on the currently selected route.
        updatePresentation();
        //Log.d(TAG, String.format("%d %x", this.getTheme().hashCode(), this.getTheme().hashCode()));
        
        updateDisplayConfigFile(null, true);
    }

    @Override
    protected void onPause() {
        // Be sure to call the super class.
        super.onPause();

        // Stop listening for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);

        // Pause rendering.
        onUpdateContents();
        
        updateDisplayConfigFile(null, false);
    }

    @Override
    protected void onStop() {
        // Be sure to call the super class.
        super.onStop();

        // Dismiss the presentation when the activity is not visible.
        if (mSecondScreen != null) {
            Log.i(TAG, "Dismissing presentation because the activity is no longer visible.");
            mSecondScreen.dismiss();
            mSecondScreen = null;
            updateDisplayConfigFile(null, false);
        }
    }
    
    private void updatePresentation() {
        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // Dismiss the current presentation if the display has changed.
        if (route != null && mSecondScreen != null && mSecondScreen.getDisplay() != presentationDisplay) {
        //if (route != null && mPresentation != null) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mSecondScreen.dismiss();
            mSecondScreen = null;
        }

        // Show a new presentation if needed.
        if (mSecondScreen == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            updateDisplayConfigFile(presentationDisplay, true);
            mSecondScreen = new SecondScreen(this, presentationDisplay);
            mSecondScreen.setOnDismissListener(mOnDismissListener);
            mSecondScreen.setOnShowListener(mOnShowListener);

            try {                
                mSecondScreen.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mSecondScreen = null;
            }
        }

        // Update the contents playing in this activity.
        onUpdateContents();
    }

    protected void onUpdateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        if (mSecondScreen != null) {
            //mInfoTextView.setText("There are two displays");
                    //mPresentation.getDisplay().getName());
        } else {
            //mInfoTextView.setText("There is only one display. Try connecting a secondary display and watch what happens.");
                    //getWindowManager().getDefaultDisplay().getName()));
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
            updatePresentation();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
            updatePresentation();
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
            updatePresentation();
        }
    };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
        //@Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog == mSecondScreen) {
                updateDisplayConfigFile(null, false);
                Log.i(TAG, "Presentation was dismissed.");
                mSecondScreen = null;
            }
        }
    };
    
    private final DialogInterface.OnShowListener mOnShowListener =
            new DialogInterface.OnShowListener() {
        //@Override
        public void onShow(DialogInterface dialog) {
            if (dialog == mSecondScreen) {
                updateDisplayConfigFile(null, true);
            }
        }
    };    
    
    private void updateDisplayConfigFile(Display display, boolean displayDiff){
        if(display == null){
            MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                    MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
            display = route != null ? route.getPresentationDisplay() : null;
        }
        if(display == null) return;
        
        final String CALIBRATION_FILE_SECOND = "/data/calibration/second.conf";
        File file = new File(CALIBRATION_FILE_SECOND);
        String text = "";
        try{
            FileReader r = new FileReader(file);
            BufferedReader in = new BufferedReader(r);
            String line = null;
            while ((line = in.readLine()) != null) {
                text += line;
            }
            in.close();
            r.close();
        }catch(Exception e){};
        
        //Update display ID and size
        int pos = text.indexOf(',');
        if(pos != -1){
            String oldIdAndSize = text.substring(0, pos);
            
            Point size = new Point();
            display.getRealSize(size);
            String newIdAndSize = String.format("%d:%d:%d*%d", (displayDiff ? 1 : 0), display.getDisplayId(), size.x, size.y);
            if(!newIdAndSize.equals(oldIdAndSize)){
                //Write new id and size
                String s = newIdAndSize + text.substring(pos);
                try{
                    FileWriter w = new FileWriter(file);
                    BufferedWriter out = new BufferedWriter(w);
                    out.write(s);
                    out.close();
                    w.close();
                }
                catch(Exception e){
                }
            }
        }
        
    }
    
    boolean isSecondShowing(){
        return (mSecondScreen != null);
    }
    
    void showSecond(){
        if(mSecondScreen != null) return;
        updatePresentation();
    }
    
    void hideSecond(){
        if(mSecondScreen != null){
            mSecondScreen.dismiss();
        }
    }
    
    void toggleSecond(){
        if(this.isSecondShowing())
            this.hideSecond();
        else
            this.showSecond();
    }
}
