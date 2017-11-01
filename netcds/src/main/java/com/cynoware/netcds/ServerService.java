/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.netcds;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.cynoware.netcds.core.ContentManager;
import com.cynoware.netcds.core.HttpServer;


public class ServerService extends Service {

    private static final String LOG_TAG = "ServerService";

    public static final String ACTION_START_SERVER = "START_LSP_SERVER";
    public static final String ACTION_STOP_SERVER = "STOP_LSP_SERVER";

    public static final String ACTION_SERVER_STARTED = "LSP_SERVER_STARTED";
    public static final String ACTION_SERVER_STOPPED = "LSP_SERVER_STOPPED";

    private static final int SERVER_NOTIFICATION_ID = 111;

    private static final int PORT = 80;

    private HttpServer mHttpServer;
    private Handler mHandler;
    private ContentManager mContentManager;

    private SharedPreferences mPreference;

    private PowerManager.WakeLock mWakeLock;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerService.this;
        }
    }

    public Handler getHandler() {
        return mHandler;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {

        Log.d(LOG_TAG, "Service onCreate");

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PosMate");
//        mWakeLock.acquire();

        mContentManager = ContentManager.getInstance();
        mContentManager.init(getResources());
        mHandler = new Handler();

        mPreference = getSharedPreferences("posmate", 0);

        startServer();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");
        return START_STICKY;
    }


    private void showNotify(String message) {
        Intent notificationIntent = new Intent(this, WebViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Posmate Server")
                .setSmallIcon(R.drawable.service)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(SERVER_NOTIFICATION_ID, notification);

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Service onDestroy");

        stopServer();
        super.onDestroy();
//        mWakeLock.release();
    }
    public boolean isServerRunning() {
        return mHttpServer != null;
    }


    private void startServer() {

        Log.d(LOG_TAG, "Starting server ...");

        if (isServerRunning()) {
            Log.e(LOG_TAG, "Server is already running");
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    mHttpServer = new HttpServer(ServerService.this, PORT, new HttpServer.onServerEventListener() {
                        @Override
                        public void onStarted() {
                            Log.d("ServerService", "Server started");
                            showNotify(getString(R.string.server_is_running));
                            broadcast(ACTION_SERVER_STARTED);
                        }

                        @Override
                        public void onStopped() {
                            Log.d("ServerService", "Server stopped");
                            showNotify(getString(R.string.server_not_running));
                            broadcast(ACTION_SERVER_STOPPED);
                        }
                    });
                    mHttpServer.start();

                    mHttpServer = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }


    public void stopServer() {
        Log.d(LOG_TAG, "Stopping server");

        if (mHttpServer != null) {
            mHttpServer.stop();
            broadcast(ACTION_SERVER_STOPPED);
            mHttpServer = null;
        } else {
            Log.e(LOG_TAG, "null HttpServer instance");
        }
    }


//    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (action == null)
//                return;
//
//            if (action.equals(ACTION_START_SERVER)) {
//                startServer();
//            } else if (action.equals(ACTION_STOP_SERVER)) {
//                stopServer();
//            }
//        }
//    };

    public void broadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    public void openUrl(String url) {
        Intent intent = new Intent(WebViewActivity.ACTION_OPEN_URL);
        intent.putExtra("url", url);
        sendBroadcast(intent);
    }



}
