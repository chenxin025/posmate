package com.cynoware.webapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cynoware.printer.sdklib.bean.PrinterInfo;
import com.cynoware.printer.sdklib.callback.OnResultResponse;
import com.cynoware.printer.sdklib.callback.OnSendResponse;
import com.cynoware.printer.sdklib.service.PrinterService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebViewActivity extends Activity {

    private WebView mWebView;
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int REQUEST_IMAGE_GALLERY = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri photoURI;

    private ServiceConnection mConn = null;
    private Intent mIntentConnectionPrinter;
    private PrinterService mPrinterService;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCollector.addActivity(this,WebViewActivity.class);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);


        Intent intent = this.getIntent();
        String title = intent.getStringExtra("title");
        if (title != null)
            setTitle(title);


        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.webView);

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
//                super.onProgressChanged(view, progress);
//                view.requestFocus();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]>
                    filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                showFileInputChooser();
                return true;
            }
        });


        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.getSettings().setBuiltInZoomControls(false);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.addJavascriptInterface(new WebAppJS(this), "WebApp");

        mWebView.setWebViewClient(new WebViewClient() {
            /*
             * @Override public boolean onJsAlert(mWebView view, String url,
             * String message, JsResult result) { return super.onJsAlert(view,
             * url, message, result); }
             */

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
            }

        });

        String url = intent.getStringExtra("url");

        if (url != null && !url.isEmpty())
            mWebView.loadUrl(url);

        if (AppApplication.getmInstance().getmAppChannel()
                .equals(AppApplication.CHANNEL_COLLECT)) {
            initPrinterService();
            initReceiver();
        }
    }

    private void  initReceiver(){
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null){
                    return;
                }
                String action = intent.getAction();
                Log.i("test","initReceiver============="+action);
                switch (action){
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:

                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        int currentPid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_PID,0);
                        int currentVid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_VID,0);
                       // Log.i("test","currentPid ="+currentPid);
                       // Log.i("test","currentVid="+currentVid);

                        //Log.i("test","dev   Vid="+device.getVendorId());
                        //Log.i("test","dev   Vid="+device.getVendorId());
                        if (currentPid == device.getProductId() && currentVid == device.getVendorId()){
                            Log.i("test","====initPrinterService====");
                            initPrinterService();
                        }


                        break;

                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        UsbDevice device1 = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        int Pid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_PID,0);
                        int Vid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_VID,0);
                        if (Pid == device1.getProductId() && Vid == device1.getVendorId()) {

                            Toast.makeText(WebViewActivity.this, "打印机连接断开", Toast.LENGTH_SHORT).show();
                            if (mConn != null && mPrinterService != null) {
                                mPrinterService.closePrinter();
                                unbindService(mConn);
                                mConn = null;
                                mPrinterService = null;
                            }
                        }
                        break;
                }
            }
        };

        registerReceiver(mReceiver,mIntentFilter);
    }


    private Dialog mDialog;
    private boolean mIsCanceled = true;

    private void showFileInputChooser() {

        if (mDialog == null) {
            String[] menu = {getString(R.string.take_photo), getString(R.string.use_gallery_photo)};
            ListView lv = new ListView(this);
            lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, menu));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (id == 0) {
                        dispatchTakePictureIntent();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent, "File Chooser"),
                                REQUEST_IMAGE_GALLERY);
                    }
                    mIsCanceled = false;
                    mDialog.dismiss();
                    mDialog = null;
                }
            });

            mDialog = new AlertDialog.Builder(this).setView(lv).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if( mIsCanceled && mUploadMessage != null ) {
                        mUploadMessage.onReceiveValue(null);
                        mUploadMessage = null;
                    }

                    mIsCanceled = true;
                }
            }).create();
        }

        mDialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

//        if (mConn != null && mPrinterService != null) {
//            mPrinterService.closePrinter();
//            unbindService(mConn);
//        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        mWebView.clearCache(true);
        mWebView.clearHistory();

        if (AppApplication.getmInstance().getmAppChannel()
                .equals(AppApplication.CHANNEL_COLLECT)) {
            if (mConn != null && mPrinterService != null) {
                mPrinterService.closePrinter();
                unbindService(mConn);
            }
            if (null != mReceiver) {
                unregisterReceiver(mReceiver);
            }
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = Uri.fromFile(photoFile);
//                ,FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mUploadMessage == null)
            return;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Uri[] results = null;

            if (resultCode == RESULT_OK) {
                results = new Uri[1];
                results[0] = photoURI;
            }
            mUploadMessage.onReceiveValue(results);
            mUploadMessage = null;
        } else if (requestCode == REQUEST_IMAGE_GALLERY ){

            Uri[] results = null;
            if( resultCode == RESULT_OK && data != null ){
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }

            mUploadMessage.onReceiveValue(results);
            mUploadMessage = null;
        }
    }


    private void initPrinterAttach() {

        SharePreferenceUtil.getInstance().init(this);
        int pid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_PID,0);
        int vid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_VID,0);
        PrinterInfo printerInfo = new PrinterInfo();
        printerInfo.mProductId = pid;
        printerInfo.mVendId = vid;
        final PrinterInfo parapms = printerInfo;



        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mPrinterService = ((PrinterService.MyBinder) iBinder).getService();
                Log.i("test", "###########" + mPrinterService.isConnected());

                mPrinterService.openPrintDevice(WebViewActivity.this, new OnResultResponse() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WebViewActivity.this, "已连接打印机", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(WebViewActivity.this, "未连接打印机", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRead(byte[] bytes) {

                    }
                }, new Handler(),parapms);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        mIntentConnectionPrinter = new Intent(this, PrinterService.class);
        bindService(mIntentConnectionPrinter, mConn, Context.BIND_AUTO_CREATE);

    }


    private void initPrinterService() {

        SharePreferenceUtil.getInstance().init(this);
        int pid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_PID,0);
        int vid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_VID,0);
        PrinterInfo printerInfo = new PrinterInfo();
        printerInfo.mProductId = pid;
        printerInfo.mVendId = vid;
        final PrinterInfo parapms = printerInfo;



        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mPrinterService = ((PrinterService.MyBinder) iBinder).getService();
                Log.i("test", "###########" + mPrinterService.isConnected());

                mPrinterService.openPrintDevice(WebViewActivity.this, new OnResultResponse() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WebViewActivity.this, "已连接打印机", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(WebViewActivity.this, "未连接打印机", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRead(byte[] bytes) {

                    }
                }, new Handler(),parapms);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        mIntentConnectionPrinter = new Intent(this, PrinterService.class);
        bindService(mIntentConnectionPrinter, mConn, Context.BIND_AUTO_CREATE);

    }


    void print( String text ) {
        if( mPrinterService == null )
            return;

        mPrinterService.sendBytes(new OnSendResponse() {
            @Override
            public void onSuccess() {
                Toast.makeText(WebViewActivity.this, "正在打印...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(WebViewActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
            }

        }, new Handler(), text, true);

    }
}