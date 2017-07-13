/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.cynoware.posmate.httpd.core.ContentManager;
import com.cynoware.posmate.httpd.core.HttpServer;
import com.cynoware.posmate.sdk.SDKInfo;
import com.cynoware.posmate.sdk.cmd.EscCommand;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.led.LED;
import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;
import com.cynoware.posmate.sdk.printer.Printer;
import com.cynoware.posmate.sdk.service.OnStatusListener;
import com.cynoware.posmate.sdk.service.PosService;
import com.cynoware.posmate.sdk.util.SharePrefManager;


public class ServerService extends PosService {

    private static final String LOG_TAG = "ServerService";

    public static final String ACTION_START_SERVER = "START_LSP_SERVER";
    public static final String ACTION_STOP_SERVER = "STOP_LSP_SERVER";
    public static final String ACTION_SERVER_STARTED = "LSP_SERVER_STARTED";
    public static final String ACTION_SERVER_STOPPED = "LSP_SERVER_STOPPED";
    public static final String ACTION_DOCK_USB_ATTACHED = "POS_MATE_DOCK_USB_ATTACHED";
    public static final String ACTION_DOCK_USB_DETACHED = "POS_MATE_DOCK_USB_DETACHED";
    public static final String ACTION_TRAY_USB_ATTACHED = "POS_MATE_TRAY_USB_ATTACHED";
    public static final String ACTION_TRAY_USB_DETACHED = "POS_MATE_TRAY_USB_DETACHED";

    public static final int PORT = 7777;

    private static final int SERVER_NOTIFICATION_ID = 101;

    private HttpServer mHttpServer;
    private Handler mHandler;
    private ContentManager mContentManager;

    public static final int COM_0 = 0x01;
    public static final int COM_1 = 0x02;
    public static final int COM_2 = 0x04;


    private static final String PREF_CDS = "cds";
    private static final int DEFAULT_CDS = COM_0 | COM_1 | COM_2;
    private int mCDS = DEFAULT_CDS;


    public static final int SUITE_NP10 = 0x01;
    public static final int SUITE_NP11 = 0x02;
    public static final int SUITE_P140 = 0x03;

    private static final String PREF_SUITE = "suite";
    private static final int DEFAULT_SUITE = SUITE_NP10;
    private int mSuite = DEFAULT_SUITE;

    private boolean mIsDockUsbAttached;
    private boolean mIsTrayUsbAttached;

    private SharedPreferences mPreference;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        ServerService getService() {
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
        mContentManager = ContentManager.getInstance();
        mContentManager.init(getResources());
        mHandler = new Handler();

        mPreference = getSharedPreferences("posmate", 0);

        mSuite = mPreference.getInt(PREF_SUITE, getCurrentSuite());
        mCDS = mPreference.getInt(PREF_CDS, DEFAULT_CDS);

        setSuite(SUITE_NP10);
        setOnStatusListener(mOnStatusListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_START_SERVER);
        filter.addAction(ACTION_STOP_SERVER);
        registerReceiver(mBroadcastReceiver, filter);

        showNotify(getString(R.string.server_not_running));
        super.onCreate();
    }

    private int getCurrentSuite() {
        String mode = android.os.Build.MODEL;
        ;
        if (mode.equals("SABRESD-MX6DQ"))
            return SUITE_P140;
        else
            return SUITE_NP10;
    }


    private void showNotify(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("POS Server")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(SERVER_NOTIFICATION_ID, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    private OnStatusListener mOnStatusListener = new OnStatusListener() {
        @Override
        public void onTrayUsbAttached() {
            setIsTrayUsbAttached(true);
            Log.i(LOG_TAG, "Tray Usb Attached");
            Intent intent = new Intent(ACTION_TRAY_USB_ATTACHED);
            sendBroadcast(intent);
        }

        @Override
        public void onTrayUsbDetached() {
            setIsTrayUsbAttached(false);
            Log.i(LOG_TAG, "Tray Usb Detached");
            Intent intent = new Intent(ACTION_TRAY_USB_DETACHED);
            sendBroadcast(intent);
        }

        @Override
        public void onDockUsbAttached() {
            setIsDockUsbAttached(true);
            Toast.makeText(ServerService.this, "POS Attached", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ACTION_DOCK_USB_ATTACHED);
            sendBroadcast(intent);
        }

        @Override
        public void onDockUsbDetached() {
            setIsDockUsbAttached(false);
            Toast.makeText(ServerService.this, "POS Detached", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ACTION_DOCK_USB_DETACHED);
            sendBroadcast(intent);
        }

        @Override
        public void onBleAttached() {
//                    Log.i(LOG_TAG, "Ble Attached");
        }

        @Override
        public void onBleDetached() {

        }

        @Override
        public void onBleOpenFailed() {

        }
    };


    public boolean isDockUsbAttached() {
        return mIsDockUsbAttached;
    }

    public void setIsDockUsbAttached(boolean mIsDockUsbAttached) {
        this.mIsDockUsbAttached = mIsDockUsbAttached;
    }

    public boolean isTrayUsbAttached() {
        return mIsTrayUsbAttached;
    }

    public void setIsTrayUsbAttached(boolean mIsTrayUsbAttached) {
        this.mIsTrayUsbAttached = mIsTrayUsbAttached;
    }

    public boolean isServerRunning() {
        return mHttpServer != null;
    }


    private void startServer() {

        Log.d("ServerService", "Starting server ...");

        if (isServerRunning()) {
            Log.e("ServerService", "Server is already running");

            broadcast(ACTION_SERVER_STARTED);
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
        Log.d("ServerService", "Stopping server");

        if (mHttpServer != null) {
            mHttpServer.stop();
            broadcast(ACTION_SERVER_STOPPED);
            mHttpServer = null;
        } else {
            Log.e("ServerService", "null LSPServer instance");
        }
    }


    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null)
                return;

            if (action.equals(ACTION_START_SERVER)) {
                startServer();
            } else if (action.equals(ACTION_STOP_SERVER)) {
                stopServer();
            }
        }
    };

    public void broadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    public String getDeviceID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public void setSuite(int suite) {

        switch (suite) {
            case SUITE_NP10:
                setPosSet(0);
                SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SET, 0);
                break;

            case SUITE_NP11:
                setPosSet(1);
                SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SET, 1);
                break;

            case SUITE_P140:
                setPosSet(2);
                SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SET, 2);
                break;
        }

        mSuite = suite;
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt(PREF_SUITE, mSuite);
        editor.apply();
    }

    public int getSuite() {
        return mSuite;
    }


    public void print(String content) {

        EscCommand escCmd = new EscCommand();

        escCmd.addInitializePrinter();
        escCmd.addSelectMode(Printer.SELECT_CENTER_MODE);
        escCmd.addSetCharset(EscCommand.CHARSET_KOREAN);
        escCmd.add(content.getBytes());
        escCmd.add(new byte[]{0x0D, 0x0A, 0x0D, 0x0A});
        escCmd.addPrintAndFeedPaper((byte) 3);

        printText(escCmd.createCommandBuffer(), null, getHandler());
    }


    public void startScanner(ResultCallBack callBack) {
        startScanner(callBack, mHandler);
    }


    public boolean getCDS(int com) {
        return (mCDS & com) != 0;
    }

    public void setCDS(int com, boolean enable) {
        if (enable)
            mCDS |= com;
        else
            mCDS &= ~com;

        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt(PREF_CDS, mCDS);
        editor.apply();
    }

    public void showLED(int type, String num) {
        if (getCDS(COM_0))
            showLED(BaseConfig.COM_PORT_0, type, num);
        if (getCDS(COM_1))
            showLED(BaseConfig.COM_PORT_1, type, num);
        if (getCDS(COM_2))
            showLED(BaseConfig.COM_PORT_2, type, num);
    }

    public void showLED(int com, int type, String num) {
        showLedText(com, type, num, new ResponseCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {
                //Toast.makeText(mContext, "请检查是否已经连接客显设备！", Toast.LENGTH_SHORT).show();
            }
        }, mHandler);
    }

    public void initLED() {
        if (getCDS(COM_0))
            initLED(BaseConfig.COM_PORT_0);
        if (getCDS(COM_1))
            initLED(BaseConfig.COM_PORT_1);
        if (getCDS(COM_2))
            initLED(BaseConfig.COM_PORT_2);
    }

    public void initLED(int com) {
        showLedText(com, LED.CMD_INIT_TYPE, "", null, mHandler);
    }
}
