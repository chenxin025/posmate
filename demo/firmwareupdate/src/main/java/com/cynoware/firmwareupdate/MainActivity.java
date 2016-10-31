package com.cynoware.firmwareupdate;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

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

    private static final String TAG = "USB_HOST_IAIOT";
    private static final int USB_REQUEST_TYPE_INTERFACE = 0x01;
    private static final int CMD_TIMEOUT_MS = 0x2000;

    OpenApDev apDev = new OpenApDev(MainActivity.this);
    OpenLdDev ldDev = new OpenLdDev(MainActivity.this);
    DevUtils devUtils = new DevUtils(MainActivity.this);
    FileUtils fileUtils = new FileUtils();
    Burning burning = new Burning();

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
                    main_et_filesize.setText(" " + fileUtils.filelen + " Bytes");
                    main_et_checksum.setText(" " + fileUtils.md5str);
                    main_et_filedata_aprom.setText(fileUtils.bytestr);
                    main_et_baseaddress.setText(" NA");
                    readfile_fra.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Read successfully", Toast.LENGTH_SHORT).show();
                    fileUtils.bytestr = "";
                    break;
                case 2:
                    main_progressbar.setProgress(msg.arg1);
                    break;
                case 3:
                    /*new AlertDialog.Builder(MainActivity.this)
                            .setTitle("未枚举到设备！")
                            .setMessage("请先连接设备，再重启程序。。")
                            .setCancelable(false)
                            .setNeutralButton("确定", null)
                            .show();*/
                    break;
                case 4:
                    Toast.makeText(MainActivity.this, "没有权限,请重启程序并授权...", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(MainActivity.this, "固件升级成功~", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    main_tv_connect_result.setText("Connection succeeded");
                    main_btn_connect.setText("Disconnect");
                    break;
                case 7:
                    main_tv_connect_result.setText("connecting...");
                    break;
                case 8:
                    main_tv_connect_result.setText("Connection failed");
                    break;
                case 9:
                    main_tv_connect_result.setText("Connection timed out");
                    break;
                case 10:
                    Toast.makeText(MainActivity.this, "DOCK设备未连接", Toast.LENGTH_SHORT).show();
                    break;
                case 11:
                    Toast.makeText(MainActivity.this, "将DOCK(240)引导至LDROM(更新模式)后获取不到设备", Toast.LENGTH_SHORT).show();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                apDev.tryGetUsbPermission(handler);
            }
        }).start();


        // 测试断开连接
        /*main_tv_connect_result.setText("connecting...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (devUtils.isfindlddev()) {
                    ldDev.tryGetUsbPermission(handler);
                    devUtils.isConned = true;
                    // 修改界面为连接成功并可操作
                    handler.sendEmptyMessage(6);
                } else {
                    handler.sendEmptyMessage(9);
                    devUtils.isConned = false;
                }
            }
        }).start();*/


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
            fileUtils.uri = data.getData();
            main_et_filename.setText(" " + fileUtils.uri.getPath());
            fileUtils.file = new File(fileUtils.uri.getPath());
            fileUtils.filelen = (int) fileUtils.file.length();
            if (fileUtils.filelen > fileUtils.MAX_BIN_FILE_SIZE) {
                readfile_fra.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "File size is too big", Toast.LENGTH_LONG).show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fileUtils.getEditUIData(handler);
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
        main_et_filedata_aprom.setText("");
        if (apDev.mApTrayDev.getUsbDevConn() != null) {
            apDev.mApTrayDev.getUsbDevConn().close();
        }
        if (apDev.mApDockDev.getUsbDevConn() != null) {
            apDev.mApDockDev.getUsbDevConn().close();
        }
    }

    // 事件监听
    private void setListener() {
        main_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!devUtils.isConned) {
                    byte[] buf = new byte[32];
                    buf[0] = 0X02;
                    buf[1] = 0X0D;  //13 0X0D
                    buf[2] = 0x00;  //0x00
                    buf[3] = 0x00;
                    if (channels[0].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                        if (apDev.mApDockDev.getUsbDevConn() != null) {
                        /*int ret = burning.WriteData(apDev.mApDockDev.getUsbDevConn(), buf);
                        Log.d(TAG, "Dock_Connect_btn_RET:" + ret);
                        main_tv_connect_result.setText("Connection failed");*/
                            Toast.makeText(MainActivity.this, "将DOCK(240)引导至LDROM(更新模式)后获取不到设备", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "未检测到Dock设备,请重启APP再试...", Toast.LENGTH_SHORT).show();
                        }
                    } else if (channels[1].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                        if (apDev.mApTrayDev.getUsbDevConn() != null) {
                            int ret = burning.WriteData(apDev.mApTrayDev.getUsbDevConn(), buf);
                            Log.d(TAG, "Tray_Connect_btn_RET:" + ret);
                            // 在这里还要加入禁止界面操作的代码
                            main_tv_connect_result.setText("connecting...");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (devUtils.isfindlddev()) {
                                        ldDev.tryGetUsbPermission(handler);
                                        devUtils.isConned = true;
                                        // 修改界面为连接成功并可操作
                                        handler.sendEmptyMessage(6);
                                    } else {
                                        handler.sendEmptyMessage(9);
                                        devUtils.isConned = false;
                                    }
                                }
                            }).start();
                        } else {
                            Toast.makeText(MainActivity.this, "未检测到Tray设备,请重启APP再试...", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // 方便测试，先把断开事件放在长按事件里面
                }
            }
        });
        // 测试用：长按发送断开连接的命令
        main_btn_connect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                byte[] buf = new byte[32];
                //fileUtils.clearArr(buf, 64);
                buf[1] = (byte) 0xAB;   // 171
                buf[5] = 0X0B;          // 11
                //int ret = burning.BulkWriteData(ldDev.mLdDev.getUsbDevConn(), buf, ldDev.mLdDev.getUsbEPOut());
                //Log.d(TAG, "Dock_DisConnect_btn_RET:" + ret);

                int ret = ldDev.mLdDev.getUsbDevConn().controlTransfer(0x21, 0x09, 0x0302, 0x00, buf, buf.length, 0x2000);
                Log.d(TAG, "Dock_DisConnect_btn_RET:" + ret);

                /*UsbRequest request = new UsbRequest();
                request.initialize(ldDev.mLdDev.getUsbDevConn(), ldDev.mLdDev.getUsbEPOut());


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buf = new byte[32];
                        byte i;
                        buf[1] = (byte) 0xAB;
                        for (i = 0; i < 100; i++) {
                            buf[5] = i;
                            int ret = burning.BulkWriteData(ldDev.mLdDev.getUsbDevConn(), buf, ldDev.mLdDev.getUsbEPOut());
                            Log.d(TAG, "Dock_DisConnect_btn_RET:" + ret);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();*/



                /*if (channels[0].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                    //usbDevUtils.mDockDeviceConnection.claimInterface(usbDevUtils.mDockOutInterface, true);
                    //int ret = burning.WriteData(usbDevUtils.mDockDeviceConnection, buf);
                    int ret = burning.BulkWriteData(ldDev.mLdDev.getUsbDevConn(), buf, ldDev.mLdDev.getUsbEPOut());
                    Log.d(TAG, "Dock_DisConnect_btn_RET:" + ret);
                } else if (channels[1].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                    //int ret = burning.BulkWriteData(ldDev.mLdDev.getUsbDevConn(), buf, ldDev.mLdDev.getUsbEPOut());
                    //Log.d(TAG, "Tray_DisConnect_btn_RET:" + ret);
                }*/
                return true;
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
                if ("".equals(fileUtils.uri) || fileUtils.uri == null) {
                    Toast.makeText(MainActivity.this, "Please select a file...", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (channels[0].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                                handler.sendEmptyMessage(11);
                                //burning.startBurning(ldDev.mLdDev.getUsbDevConn(), ldDev.mLdDev.getUsbEPOut(), ldDev.mLdDev.getUsbEPIn(), fileUtils.data, handler);
                            } else if (channels[1].equals(main_spinnerUSBChannel.getSelectedItem().toString())) {
                                if (ldDev.mLdDev.getUsbDevConn() != null) {
                                    burning.startBurning(ldDev.mLdDev.getUsbDevConn(), ldDev.mLdDev.getUsbEPOut(), ldDev.mLdDev.getUsbEPIn(), fileUtils.data, handler);
                                } else {
                                    handler.sendEmptyMessage(10);
                                }
                            }
                        }
                    }).start();
                }
            }
        });
        //长按事件留着测试用，之后删除
        main_btn_start.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //burning.startBurning(burnBy41750.usbDeviceConnection, burnBy41750.epOut, burnBy41750.epIn, fileUtils.data, handler);
                        /*byte[] buf = new byte[32];
                        int ret = burning.BulkWriteData(burnBy41750.usbDeviceConnection, buf, burnBy41750.epOut);
                        Log.d(TAG, "===============" + ret);*/
                        /*byte[] buf = new byte[9];
                        buf[0] = 0X02;
                        buf[1] = 0X00;
                        buf[2] = 0x00;
                        buf[3] = 0x00;
                        int ret = burning.WriteData(burnBy41750.usbDeviceConnection, buf);
                        Log.d(TAG, "===============" + ret);*/
                        /*byte[] buf = new byte[32];
                        int ret = burning.WriteData(burnBy41750.usbDeviceConnection, buf);
                        Log.d(TAG, "===============" + ret);*/

                        byte[] buf = new byte[32];
                        int ret = ldDev.mLdDev.getUsbDevConn().controlTransfer(UsbConstants.USB_TYPE_CLASS
                                        | UsbConstants.USB_DIR_OUT | USB_REQUEST_TYPE_INTERFACE, 0x09, /*
                                                                                 * Set
																				 * report
																				 */
                                0x0302, /* Request type: feature */
                                0x00, /* Interface number */
                                buf, buf.length, CMD_TIMEOUT_MS);
                        Log.d(TAG, "===============" + ret);
                    }
                }).start();
                return true;
            }
        });
    }

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

    private void init() {
        SpinnerAdapter adapterChannel = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, channels);
        main_spinnerUSBChannel.setAdapter(adapterChannel);
        main_et_filedata_aprom.setText("");
    }

}
