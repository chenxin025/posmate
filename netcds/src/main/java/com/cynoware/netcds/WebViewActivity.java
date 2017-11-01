package com.cynoware.netcds;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebViewActivity extends Activity {

    public static final String ACTION_OPEN_URL = "OPEN_URL";

    private static final String LOG_TAG = "WebViewActivity";
    private WebView mWebView;
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int REQUEST_IMAGE_GALLERY = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri photoURI;

    private ServiceConnection mConn = null;

    private ServerService mServerService = null;
    private boolean mIsServiceBound;

    private ServiceConnection mServerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(LOG_TAG, "Service connected");
            ServerService.LocalBinder binder = (ServerService.LocalBinder) service;
            mServerService = binder.getService();
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(LOG_TAG, "Service disconnected");
            mIsServiceBound = false;
            mServerService = null;
        }
    };

    public ServerService getServerService() {
        return mServerService;
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null)
                return;

            if (action.equals(ACTION_OPEN_URL)) {
                String url = intent.getStringExtra("url");
                if( url != null || !url.isEmpty() )
                    mWebView.loadUrl(url);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        Intent intent = new Intent(this, ServerService.class);
        bindService(intent, mServerServiceConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.webView);

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]>
                    filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                showFileInputChooser();
                return true;
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OPEN_URL);
        registerReceiver(mBroadcastReceiver, filter);

        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

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

        //String url = intent.getStringExtra("url");
        String url = "http://www.baidu.com";

        if (url != null && !url.isEmpty())
            mWebView.loadUrl(url);
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
                    if (mIsCanceled && mUploadMessage != null) {
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

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWebView.clearCache(true);
        mWebView.clearHistory();

        if (mConn != null) {
            unbindService(mConn);
        }

        if (mIsServiceBound) {
            unbindService(mServerServiceConnection);
            mIsServiceBound = false;
        }

        unregisterReceiver(mBroadcastReceiver);
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
        } else if (requestCode == REQUEST_IMAGE_GALLERY) {

            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
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
}