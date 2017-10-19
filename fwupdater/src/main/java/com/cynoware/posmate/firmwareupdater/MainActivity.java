package com.cynoware.posmate.firmwareupdater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;

import static android.hardware.usb.UsbConstants.USB_DIR_OUT;

// USB通讯步骤：发现设备->枚举设备->找到设备的接口->连接(打开)设备->分配相应的端点->进行通讯
// 枚举设备->找到设备的接口->连接设备->分配相应的端点->在IN端点进行读操作，在OUT端点进行写操作
// 注意：首先得得到用户的授权，才能来操作USB设备

public class MainActivity extends AppCompatActivity {

    // 控件
    private Button main_btn_connect;
    private Button main_btn_aprom;
    private Button main_btn_start;
    public TextView main_tv_connect_result;
    public TextView main_tv_ram;
    public EditText main_et_filedata_aprom;
    public TextView main_tv_aprom;
    public TextView main_tv_dataflash;
    public TextView main_tv_ver;
    public TextView main_et_filename;
    public TextView main_et_filesize;
    public TextView main_et_checksum;
    public TextView main_et_partno;
    public TextView main_et_baseaddress;
    public ProgressBar main_progressbar;
    private Spinner main_spinnerUSBChannel;
    private FrameLayout readfile_fra;
    String channels[] = {"Dock USB", "Tray USB"};

    // 文件选择
    final long MAX_BIN_FILE_SIZE = 1024 * 512;
    static int filelen = 0;
    Uri uri;
    File file = null;
    String md5str = "";
    String bytestr = "";
    byte[] data = null;

    // 常量
    public static final int MAX_CMD_SIZE = 32;
    public static final int CMD_REPORT_ID = 2;
    private static final int HID_EVENT_SIZE = 9;
    private static final int USB_REQUEST_TYPE_INTERFACE = 0x01;
    private static final int CMD_TIMEOUT_MS = 0x2000;

    // 连接USB
    private static final String TAG = "USB_HOST_IAIOT";
    private UsbManager mUsbManager;
    public UsbDevice mUsbDevice;
    public UsbDevice mDockUsbDevice;
    public UsbDevice mTrayUsbDevice;
    private UsbInterface mInterface;
    private UsbInterface mDockInterface;
    private UsbInterface mTrayInterface;
    public UsbDeviceConnection mDeviceConnection;
    public UsbDeviceConnection mDockDeviceConnection;
    public UsbDeviceConnection mTrayDeviceConnection;
    private final int VendorID = 1046;
    private final int ProductID = 45056;   //41750
    // private final int ProductID = 41750;
    public UsbEndpoint eDockpOut;
    public UsbEndpoint eTraypOut;
    public UsbEndpoint eDockpIn;
    public UsbEndpoint eTraypIn;

    // burning
    int MAX_PACKET = 64;
    int writed = 0;
    byte[] WriteReportBuffer = new byte[65];
    int tranBufStartAddr;
    int startCpIndex = 0;
    byte g_packno = 1;
    int ret = -2;

    // 获取USB权限
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    readfile_fra.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "File does not exist or Not authorized", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    main_et_filesize.setText(" " + filelen + " Bytes");
                    main_et_checksum.setText(" " + md5str);
                    main_et_filedata_aprom.setText(bytestr);
                    main_et_baseaddress.setText(" NA");
                    readfile_fra.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Read successfully", Toast.LENGTH_SHORT).show();
                    bytestr = "";
                    break;
                case 2:
                    main_progressbar.setProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        init();
        setListener();
        tryGetUsbPermission();
    }

    private void tryGetUsbPermission() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mUsbManager == null) {
            return;
        }
        //IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //registerReceiver(mUsbPermissionActionReceiver, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            for (UsbDevice usbDevice : deviceList.values()) {
                if (usbDevice.getVendorId() == VendorID && usbDevice.getProductId() == ProductID) {
                    mUsbDevice = usbDevice;
                    Log.d(TAG, "枚举设备成功");
                    if (mUsbManager.hasPermission(usbDevice)) {
                        afterGetUsbPermission(usbDevice);
                    } else {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                        afterGetUsbPermission(usbDevice);
                    }
                } else if (usbDevice.getVendorId() == VendorID && usbDevice.getProductId() == 41750) {
                    mUsbDevice = usbDevice;
                    Log.d(TAG, "枚举41750设备成功");
                    if (mUsbManager.hasPermission(usbDevice)) {
                        afterGetUsbPermission(usbDevice);
                    } else {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                        afterGetUsbPermission(usbDevice);
                    }
                } else {
                    Log.d(TAG, "不合适的：VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId());
                }
            }
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("未枚举到设备！")
                    .setMessage("请先连接设备，再重启程序。。")
                    .setCancelable(false)
                    .setNeutralButton("确定", null)
                    .show();
        }
    }

    private void afterGetUsbPermission(UsbDevice usbDevice) {
        Log.d(TAG, "Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId());
        Log.d(TAG, "Got permission for usb device: " + usbDevice);
        findIntfAndEpt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {  //是否已选择
            main_et_filesize.setText("");
            main_et_checksum.setText("");
            main_et_filedata_aprom.setText("");
            main_et_filename.setText("");
            main_et_baseaddress.setText("");
            uri = data.getData();
            main_et_filename.setText(" " + uri.getPath());
            file = new File(uri.getPath());
            filelen = (int) file.length();
            if (filelen > MAX_BIN_FILE_SIZE) {
                readfile_fra.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "File size is too big", Toast.LENGTH_LONG).show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getEditUIData();
                    }
                }).start();
            }
        } else {
            readfile_fra.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mUsbPermissionActionReceiver);
        main_et_filedata_aprom.setText("");
        if (mDeviceConnection != null) {
            mDeviceConnection.close();
        }
        if (mTrayDeviceConnection != null) {
            mTrayDeviceConnection.close();
        }
        if (mDockDeviceConnection != null) {
            mDockDeviceConnection.close();
        }
    }

    // 事件监听
    private void setListener() {
        main_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] buf = new byte[32];
                buf[0] = CMD_REPORT_ID;
                buf[1] = 0X0D;  //13 0X0D
                buf[2] = 0x00;  //0x00
                buf[3] = 0;
                if (channels[0].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                    if (mDockDeviceConnection != null) {
                        int ret = mDockDeviceConnection.controlTransfer(
                                UsbConstants.USB_TYPE_CLASS | UsbConstants.USB_DIR_OUT | USB_REQUEST_TYPE_INTERFACE,
                                0x09, 0x0302, 0x00, buf, buf.length, 0x2000);
                        Log.d(TAG, "Dock_Connect_btn_RET:" + ret);
                    } else {
                        Toast.makeText(MainActivity.this, "未检测到Dock设备,请重启APP再试...", Toast.LENGTH_SHORT).show();
                    }
                } else if (channels[1].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                    if (mTrayDeviceConnection != null) {
                        int ret = mTrayDeviceConnection.controlTransfer(33, 9, 770, 0, buf, buf.length, 8192);
                        //int ret = mDeviceConnection.bulkTransfer(epOut, buf, buf.length, 10000);
                        Log.d(TAG, "Tray_Connect_btn_RET:" + ret);
                    } else {
                        Toast.makeText(MainActivity.this, "未检测到Tray设备,请重启APP再试...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        main_btn_aprom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 调用系统文件管理器选择文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("file/*");
                readfile_fra.setVisibility(View.VISIBLE);
                startActivityForResult(intent, 1);
            }
        });
        main_btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(uri) || uri == null) {
                    Toast.makeText(MainActivity.this, "Please select a file...", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startBurning(mDockDeviceConnection, data);
                        }
                    }).start();
                }
                //tryGetUsbPermission();
            }
        });
    }

    public void startBurning(UsbDeviceConnection connection, byte[] data) {
        synchronized (mDockDeviceConnection) {
            Log.d(TAG, "StartBurning ");

            WriteReportBuffer[1] = (byte) 0XA4; //164
            WriteReportBuffer[5] = g_packno;
            WriteReportBuffer[9] = 1;
            ret = connection.controlTransfer(33, 9, 770, 0, WriteReportBuffer, WriteReportBuffer.length, 8192);
            Log.d(TAG, "Burning01:" + ret);
            ret = -2;
            g_packno += 2;

            WriteReportBuffer[1] = (byte) 0XA0; //160
            WriteReportBuffer[5] = g_packno;
            WriteReportBuffer[9] = 0;
            WriteReportBuffer[13] = 68;
            WriteReportBuffer[14] = 18;

            System.arraycopy(data, 0, WriteReportBuffer, 17, 48);

            ret = connection.controlTransfer(33, 9, 770, 0, WriteReportBuffer, WriteReportBuffer.length, 8192);
            Log.d(TAG, "Burning02:" + ret);
            ret = -2;
            writed = 65;

            while (writed < filelen) {
                g_packno += 2;
                WriteReportBuffer[5] = (byte) g_packno;

                //arraycopy(被复制的数组, 从第几个元素开始复制, 要复制到的数组, 从第几个元素开始粘贴, 一共需要复制的元素个数);
                if (filelen - writed > MAX_PACKET - 8) {
                    System.arraycopy(data, startCpIndex, WriteReportBuffer, 9, MAX_PACKET - 8);
                    writed += (MAX_PACKET - 8);
                    startCpIndex += (MAX_PACKET - 8);
                    burnAndSetProgress(connection, (writed / filelen) * 100);
                } else {
                    System.arraycopy(data, startCpIndex, WriteReportBuffer, 9, filelen - writed);
                    writed = filelen;
                    startCpIndex = filelen;
                    burnAndSetProgress(connection, 100);
                }
            }
        }
    }

    private void burnAndSetProgress(UsbDeviceConnection connection, int nCurProgress) {
        ret = connection.controlTransfer(33, 9, 770, 0, WriteReportBuffer, WriteReportBuffer.length, 8192);
        Log.d(TAG, "Burning:" + ret);
        ret = -2;
        Message msg = new Message();
        msg.arg1 = nCurProgress;
        msg.what = 2;
        handler.sendMessage(msg);
    }

    // 寻找设备的接口和通信端点
    private void findIntfAndEpt() {
        if (mUsbDevice == null) {
            return;
        }
        Log.d(TAG, "interfaceCounts: " + mUsbDevice.getInterfaceCount());
        for (int i = 0; i < mUsbDevice.getInterfaceCount(); ) {
            UsbInterface usbInterface = mUsbDevice.getInterface(i);
            Log.d(TAG, i + " " + usbInterface);
            mInterface = usbInterface;
            Log.d(TAG, "找到设备接口");
            break;
        }
        openDevice();
    }

    // 打开设备
    private void openDevice() {
        if (mInterface != null) {
            UsbDeviceConnection connection = null;
            // 判断是否有权限
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限，可以查阅相关资料
            if (mUsbManager.hasPermission(mUsbDevice)) {
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                connection = mUsbManager.openDevice(mUsbDevice);
                if (connection == null) {
                    return;
                }
                if (connection.claimInterface(mInterface, true)) {
                    mDeviceConnection = connection; // 到此android设备已经连上HID设备
                    Log.d(TAG, "打开设备成功");
                    if ("PosMate".equals(getDeviceName(mDeviceConnection))) {
                        Log.d(TAG, "PosMate(Dock) is OK");
                        mDockUsbDevice = mUsbDevice;
                        mDockDeviceConnection = mDeviceConnection;
                        mDockInterface = mInterface;
                        assignEndpoint(mDockDeviceConnection, mDockInterface, "Dock");
                    } else if ("PMFrame".equals(getDeviceName(mDeviceConnection))) {
                        Log.d(TAG, "PMFrame(Tray) is OK");
                        mTrayUsbDevice = mUsbDevice;
                        mTrayDeviceConnection = mDeviceConnection;
                        mTrayInterface = mInterface;
                        assignEndpoint(mTrayDeviceConnection, mTrayInterface, "Tray");
                    } else {
                        Log.d(TAG, "DeviceName is other：" + getDeviceName(mDeviceConnection));
                    }
                } else {
                    Log.d(TAG, "打开设备失败");
                    connection.close();
                }
            } else {
                Log.d(TAG, "没有权限");
                Toast.makeText(MainActivity.this, "没有权限,请重启程序并授权...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 分配端点，IN | OUT，即输入输出；1为OUT端点，0为IN，也可以通过判断
    private void assignEndpoint(UsbDeviceConnection connection, UsbInterface usbInterface, String usbtype) {
        if (usbInterface != null) {
            if ("Dock".equals(usbtype)) {
                Log.d(TAG, "Dock.EndpointCount:" + usbInterface.getEndpointCount());
                for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = usbInterface.getEndpoint(i);
                    Log.d(TAG, "Dock.ep:" + ep.toString());
                    Log.d(TAG, "Dock.ep.getType:" + ep.getType());
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                        Log.d(TAG, "Dock.ep.getDirection:" + ep.getDirection());
                        if (ep.getDirection() == USB_DIR_OUT) {
                            eDockpOut = ep;
                            Log.d(TAG, usbtype + "：eDockpOut is OK");
                        } else {
                            eDockpIn = ep;
                            Log.d(TAG, usbtype + "：eDockpIn is OK");
                        }
                    }
                }
            } else if ("Tray".equals(usbtype)) {
                Log.d(TAG, "Tray.EndpointCount:" + usbInterface.getEndpointCount());
                for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = usbInterface.getEndpoint(i);
                    Log.d(TAG, "Tray.ep:" + ep.toString());
                    Log.d(TAG, "Tray.ep.getType:" + ep.getType());
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                        if (ep.getDirection() == USB_DIR_OUT) {
                            eTraypOut = ep;
                            Log.d(TAG, usbtype + "：eTraypOut is OK");
                        } else {
                            eTraypIn = ep;
                            Log.d(TAG, usbtype + "：eTraypIn is OK");
                        }
                    }
                }
            }
        }
        Log.d(TAG, usbtype + "：assignEndpoint is finish");
    }

    // 根据DeviceName判断是Dock还是Tray
    private static String getDeviceName(UsbDeviceConnection conn) {
        String str = "";
        byte[] buf = new byte[32];
        buf[0] = 2;
        buf[1] = 9;
        int res = setFeature(conn, buf);
        if (res < 0) {
            return str;
        } else {
            res = getFeature(conn, (byte) 2, buf);
            if (res < 0) {
                return str;
            } else {
                try {
                    int ex;
                    for (ex = 8; ex < buf.length && buf[ex] != 0; ++ex) {
                        ;
                    }

                    str = new String(buf, 8, ex - 8, "UTF-8");
                } catch (UnsupportedEncodingException var5) {
                    ;
                }

                return str;
            }
        }
    }

    static int getFeature(UsbDeviceConnection conn, byte reportID, byte[] buf) {
        buf[0] = reportID;
        int res = conn.controlTransfer(161, 1, 770, 0, buf, buf.length, 8192);
        return res;
    }

    private static int setFeature(UsbDeviceConnection connection, byte[] buf) {
        int res = connection.controlTransfer(33, 9, 770, 0, buf, buf.length, 8192);
        return res;
    }

    // memcpy(pMainDlg->WriteReportBuffer + 13, &tranBufSize, 4);
    // 文件大小需要从低位开始给WriteReportBuffer赋值，该方法实现
    private static byte getByteHighLowBit(int size, int index) {
        int h1 = size % 256;
        int h2 = size / 256;
        byte buf13 = (byte) h1;
        byte buf14 = (byte) h2;
        if (index == 13)
            return buf13;
        else if (index == 14)
            return buf14;
        else
            return 0;
    }

    // 获取文件信息线程
    private void getEditUIData() {
        md5str = getFileMD5(file);
        bytestr = getFileToByte();
        while (true) {
            if (!("".equals(bytestr))) {
                handler.sendEmptyMessage(1);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // byte数组初始化
    private static void clearArr(byte[] arr, int endIndex) {
        for (int i = 0; i < arr.length; i++) {
            if (i <= endIndex) {
                arr[i] = 0;
            } else {
                arr[i] = (byte) 204;
            }
        }
    }

    //  得到比较好看的16进制数据用于显示(显示的和windows版本一样了)
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase() + " ";
        }
        return ret;
    }

    // 得到文件字节流
    public String getFileToByte() {
        FileInputStream is = null;
        data = new byte[filelen];
        try {
            is = new FileInputStream(uri.getPath());
            is.read(data);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "data:" + Bytes2HexString(data));
        /*Log.d(TAG, "data[0]:" + data[0] + "data[1]:" + data[1] + "data[2]:" + data[2] +
                "data[3]:" + data[3] + "data[4]:" + data[4] + "data[5]:" + data[5] +
                "data[6]:" + data[6] + "data[7]:" + data[7] + "data[8]:" + data[8] +
                "data[9]:" + data[9] + "data[10]:" + data[10] + "data[11]:" + data[11] +
                "data[12]:" + data[12] + "data[13]:" + data[13] + "data[14]:" + data[14]);*/
        //Log.d(TAG, "data.length:" + data.length);
        //Log.d(TAG, "bin:" + strToBinstr(new String(data, 0, filelen)));
        //Log.d(TAG, "data:" + string2HexString(new String(data, 0, filelen)));
        return Bytes2HexString(data);
    }

    // 文件字节流转换为十六进制
    public static String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    // 文件字节流转换为二进制
    private String strToBinstr(String str) {
        char[] strChar = str.toCharArray();
        String result = "";
        for (int i = 0; i < strChar.length; i++) {
            result += Integer.toBinaryString(strChar[i]) + " ";
        }
        return result;
    }

    // 获取文件MD5值
    public String getFileMD5(File file) {
        if (!file.isFile()) {
            handler.sendEmptyMessage(0);
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[filelen];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    // 绑定控件
    private void findView() {
        main_btn_connect = (Button) findViewById(R.id.main_btn_connect);
        main_tv_connect_result = (TextView) findViewById(R.id.main_tv_connect_result);
        main_et_partno = (TextView) findViewById(R.id.main_et_partno);
        main_tv_ram = (TextView) findViewById(R.id.main_tv_ram);
        main_tv_aprom = (TextView) findViewById(R.id.main_tv_aprom);
        main_tv_dataflash = (TextView) findViewById(R.id.main_tv_dataflash);
        main_tv_ver = (TextView) findViewById(R.id.main_tv_ver);
        main_btn_aprom = (Button) findViewById(R.id.main_btn_aprom);
        main_et_filename = (TextView) findViewById(R.id.main_et_filename);
        main_et_filesize = (TextView) findViewById(R.id.main_et_filesize);
        main_et_checksum = (TextView) findViewById(R.id.main_et_checksum);
        main_et_baseaddress = (TextView) findViewById(R.id.main_et_baseaddress);
        main_et_filedata_aprom = (EditText) findViewById(R.id.main_et_filedata_aprom);
        main_progressbar = (ProgressBar) findViewById(R.id.main_progressbar);
        main_btn_start = (Button) findViewById(R.id.main_btn_start);
        main_spinnerUSBChannel = (Spinner) findViewById(R.id.main_spinnerUSBChannel);
        readfile_fra = (FrameLayout) findViewById(R.id.readfile_fra);
    }

    // 初始化
    private void init() {
        SpinnerAdapter adapterChannel = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, channels);
        main_spinnerUSBChannel.setAdapter(adapterChannel);
        main_et_filedata_aprom.setText("");
    }

    // 请求USB设备权限的广播
    /*private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.d(TAG, "发现新设备");
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device" + usbDevice);
                    }
                }
            }
        }
    };*/
}
