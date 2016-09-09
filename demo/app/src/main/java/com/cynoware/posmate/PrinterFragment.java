/**
 * POSMATE ANDROID
 * <p>
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * <p>
 * VERSION 1.1.0, 20160503, Jie Zhuang
 */

package com.cynoware.posmate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.cynoware.posmate.sdk.Beeper;
import com.cynoware.posmate.sdk.Device;
import com.cynoware.posmate.sdk.GpUtils;
import com.cynoware.posmate.sdk.LogUtil;
import com.cynoware.posmate.sdk.Printer;
import com.cynoware.posmate.sdk.UART;
import com.cynoware.posmate.sdk.config;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class PrinterFragment extends Fragment {

    private static final String TAG = "PrinterFragment";
    private ChannelManager mChannelMgr;
    private Setting mSetting;

    private static final String BLANK_ROWS = "\n\n\n\n\n\n\n";
    AssetManager mAssertManager;


    EditText mEditEnglish, mEditKorean, mEditJapanese, mEditChinese, mEditBarcode;

    public PrinterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MainActivity activity = (MainActivity) this.getActivity();
        mSetting = Setting.getInstance(activity);
        mChannelMgr = activity.mChannelMgr;


        mAssertManager = getResources().getAssets();

        View rootView = inflater.inflate(R.layout.activity_printer, container, false);

        mEditEnglish = (EditText) rootView.findViewById(R.id.txtEnglish);
//	    mEditKorean = (EditText)rootView.findViewById(R.id.txtKorean);
//	    mEditJapanese = (EditText)rootView.findViewById(R.id.txtJapanese);
//	    mEditChinese = (EditText)rootView.findViewById(R.id.txtChinese);
        mEditBarcode = (EditText) rootView.findViewById(R.id.txtBarcode);

        Spinner spinPrinter = (Spinner) rootView.findViewById(R.id.spinnerPrinter);
        String types[] = {"YanKe", "JingXin-1", "JingXin-2", "JingXin-3", "JingXin-4"};
        Adapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, types);
        spinPrinter.setAdapter((SpinnerAdapter) adapter);
        spinPrinter.setSelection(4);

        spinPrinter.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mSetting.setPrinterVendor(Printer.TYPE_YANKE);
                        break;

                    case 1:
                        mSetting.setPrinterVendor(Printer.TYPE_JINGXIN_1);
                        break;

                    case 2:
                        mSetting.setPrinterVendor(Printer.TYPE_JINGXIN_2);
                        break;

                    case 3:
                        mSetting.setPrinterVendor(Printer.TYPE_JINGXIN_3);
                        break;

                    case 4:
                        mSetting.setPrinterVendor(Printer.TYPE_JINGXIN_4);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spinChannel = (Spinner) rootView.findViewById(R.id.spinnerPrinterChannel);
        String channels[] = {"Dock USB", "Dock Bluetooth"};
        Adapter adapterChannel = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, channels);
        spinChannel.setAdapter((SpinnerAdapter) adapterChannel);

        if (mSetting.getPrinterChannel() == ChannelManager.CHANNEL_DOCK_USB)
            spinChannel.setSelection(0);
        else
            spinChannel.setSelection(1);


        spinChannel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mSetting.setPrinterChannel(ChannelManager.CHANNEL_DOCK_USB);
                        break;

                    case 1:
                        mSetting.setPrinterChannel(ChannelManager.CHANNEL_DOCK_BT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spinCustomText = (Spinner) rootView.findViewById(R.id.spinnerCustomText);
        String customTexts[] = {"English", "Korean", "Japanese", "Chinese"};
        Adapter adapterCustomText = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, customTexts);
        spinCustomText.setAdapter((SpinnerAdapter) adapterCustomText);
        spinCustomText.setSelection(0);

        spinCustomText.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mEditEnglish.setText(getString(R.string.EnglishString));
                        Printer.CHARSET_TYPE = 0;
                        break;
                    case 1:
                        mEditEnglish.setText(getString(R.string.KoreanString));
                        Printer.CHARSET_TYPE = 1;
                        break;
                    case 2:
                        mEditEnglish.setText(getString(R.string.JapaneseString));
                        Printer.CHARSET_TYPE = 2;
                        break;
                    case 3:
                        mEditEnglish.setText(getString(R.string.ChineseString));
                        Printer.CHARSET_TYPE = 3;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button btnEnglish = (Button) rootView.findViewById(R.id.btnEnglish);
        if (btnEnglish != null) {
            btnEnglish.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String txt = mEditEnglish.getText().toString() + BLANK_ROWS;
                    doPrint(txt, Printer.CHARSET_TYPE);
                }
            });
        }


        Button btnBarcode = (Button) rootView.findViewById(R.id.btnBarcode);
        if (btnBarcode != null) {
            btnBarcode.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String txt = mEditBarcode.getText().toString();
                    doPrintBarcode(txt);
                }
            });
        }
        /*Button btnKorean = (Button)rootView.findViewById(R.id.btnKorean);
        if(btnKorean != null){
    		btnKorean.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					String txt = mEditKorean.getText().toString()+BLANK_ROWS;
					doPrint( txt, Printer.CHARSET_KOREAN );
				}
			});
    	}
    	
    	Button btnJapanese = (Button)rootView.findViewById(R.id.btnJapanese);
    	if(btnJapanese != null){
    		btnJapanese.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String txt = mEditJapanese.getText().toString()+BLANK_ROWS;
					doPrint( txt, Printer.CHARSET_JAPANESE );			//CHARSET_JAPANESE = 2
				}
			});
    	}
    	
    	Button btnChinese = (Button)rootView.findViewById(R.id.btnChinese);
    	if(btnChinese != null){
    		btnChinese.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String txt = mEditChinese.getText().toString()+BLANK_ROWS;
					doPrint( txt, Printer.CHARSET_CHINESE );						
				}
			});
    	}*/

        final ToggleButton mToggleButton = (ToggleButton) rootView.findViewById(R.id.expand);
        final LinearLayout expandWindw = (LinearLayout) rootView.findViewById(R.id.window);
        final Drawable de = this.getResources().getDrawable(R.drawable.expand);
        final Drawable dc = this.getResources().getDrawable(R.drawable.collapse);
        if (mToggleButton != null) {
            OnCheckedChangeListener listener = new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        expandWindw.setVisibility(View.INVISIBLE);
                        mToggleButton.setBackgroundDrawable(de);
                    } else {
                        expandWindw.setVisibility(View.VISIBLE);
                        mToggleButton.setBackgroundDrawable(dc);
                    }
                }
            };
            mToggleButton.setOnCheckedChangeListener(listener);
        }


        Button btnDownloadNVImage = (Button) rootView.findViewById(R.id.btnDownloadNVImage);
        if (btnDownloadNVImage != null) {
            btnDownloadNVImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    generateView();
                }
            });
        }

        Button btnPrintNVImage = (Button) rootView.findViewById(R.id.btnPrintNVImage);
        if (btnPrintNVImage != null) {
            btnPrintNVImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //doPrintNvImage();
                    doPrintBitmap(Printer.TYPE_PRINTF_BITMAP_0);
                }
            });
        }

        return rootView;
    }


    public void doPrint(final String text, final int charset) {

        final Activity activity = this.getActivity();
        if (text == null || text.isEmpty()) {
            Toast.makeText(activity, "Nothing to print", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = mSetting.getPrinterVendor();
        final  DeviceInfo info = Util.getPrinterInfo(getActivity(),mChannelMgr);

        // Choose and check deviceѡ��

        if (Util.checkDeviceAvailable(info.device, activity) == false)
            return;

        if (mChannelMgr.isBusy()) {
            Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();

        final Printer printer = new Printer(info.device, info.port, type);   //CONFIG_PRINTER_UART = 5
        final Beeper beeper = new Beeper(info.device, config.CONFIG_BEEPER_GPIO);    //CONFIG_BEEPER_GPIO = 79

        new Thread() {
            public void run() {
                mChannelMgr.setBusy(true);
                printer.doConfig();

                boolean bPaper = printer.checkPaper();

                if (bPaper) {
                    printer.printChar(text, charset);
                } else {
                    Util.showMessage(activity, "No paper detected");
                    beeper.beep();
                }

                mChannelMgr.setBusy(false);
            }
        }.start();
    }

    public void doPrintBarcode(final String text) {

        final Activity activity = this.getActivity();
        if (text == null || text.isEmpty()) {
            Toast.makeText(activity, "Nothing to print", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = mSetting.getPrinterVendor();
        final DeviceInfo info = Util.getPrinterInfo(getActivity(),mChannelMgr);


        // Choose and check device

        if (Util.checkDeviceAvailable(info.device, activity) == false)
            return;

        if (mChannelMgr.isBusy()) {
            Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();

        final Printer printer = new Printer(info.device, info.port, type);
        final Beeper beeper = new Beeper(info.device, config.CONFIG_BEEPER_GPIO);

        new Thread() {
            public void run() {
                mChannelMgr.setBusy(true);

                printer.doConfig();

                boolean bPaper = printer.checkPaper();

                if (bPaper) {
                    printer.printBarcode(text);
                } else {
                    Util.showMessage(activity, "No paper detected");
                    beeper.beep();
                }

                mChannelMgr.setBusy(false);
            }
        }.start();
    }


	/*public void doPrintBitmap(  ){
		
		final Activity activity = this.getActivity();
		
		
		int type = mSetting.getPrinterVendor();
		int channel = mSetting.getPrinterChannel();
		
		// Choose and check device
		Device device = mChannelMgr.getDevice(channel);
		
		if( Util.checkDeviceAvailable(device, activity) == false )
			return;
	    	
		if (mChannelMgr.isBusy()) {
			Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();    		
			return;
		}
		
		Bitmap image = null;  
	    
	    try  
	    {  
	        InputStream is = mAssertManager.open("black.bmp");  
	        image = BitmapFactory.decodeStream(is);  
	        is.close();  
	    }  
	    catch (IOException e)  
	    {  
	        e.printStackTrace();
	        Toast.makeText(activity, "Load bitmap fail", Toast.LENGTH_SHORT).show();
	        return;
	    }
		
		Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();
		
		final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type );   
		final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);
		
		final Bitmap bmp = image;
		
		new Thread() {
			public void run() {
				mChannelMgr.setBusy( true );
				
				printer.doConfig();
				//printer.reset();
				// printer.printBitmap(bmp);
				
				boolean bPaper = printer.checkPaper();
				
				if( bPaper ){
					printer.printBitmap2(bmp);
				}
				else{
					Util.showMessage(activity, "No paper detected" );
					beeper.beep();
				}
	
		        mChannelMgr.setBusy( false );
	        }
		}.start();
	}    */




    private ArrayList<BitmapParams> getAssetsFiles(){
        String[] array = null;
        try {
            array = getActivity().getAssets().list("printfbmps");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == array){
            return null;
        }
        ArrayList<BitmapParams> list = new ArrayList<BitmapParams>();
        for (int i = 0; i < array.length; i++){
            BitmapParams params = new BitmapParams();
            params.name = array[i];
            LogUtil.i(TAG,"=================asset name="+params.name);
            list.add(params);
        }
        return list;
    }

    private void generateView(){
        ArrayList<BitmapParams> dataList = getAssetsFiles();
        LinearLayout linearLayoutMain = new LinearLayout(getActivity());
        linearLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ListView listView = new ListView(getActivity());
        listView.setFadingEdgeLength(0);

        BitmapAdapter myAdapter = new BitmapAdapter(getActivity(),dataList);
        listView.setAdapter(myAdapter);
        linearLayoutMain.addView(listView);

        DialogHelper dialog = new DialogHelper(getActivity());
        dialog.setTitle("打印的图片选择");
        dialog.setView(linearLayoutMain);
        dialog.setNegativeButton("取消",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.showDialog(getActivity());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BitmapParams params = (BitmapParams) adapterView.getAdapter().getItem(i);
                doDownloadNvImage(params.name);
            }
        });

    }

    public void doDownloadNvImage(String bmpName) {

        final Activity activity = this.getActivity();


        int type = mSetting.getPrinterVendor();
        int channel = mSetting.getPrinterChannel();

        // Choose and check device
        Device device = mChannelMgr.getDevice(channel);

        if (Util.checkDeviceAvailable(device, activity) == false)
            return;

        if (mChannelMgr.isBusy()) {
            Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
            return;
        }


        Bitmap image = null;

        try {
            InputStream is = mAssertManager.open("printfbmps/"+bmpName);
            //InputStream is = mAssertManager.open("testqq.jpg");
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Load bitmap fail", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Printer.getBitmapSizeWithKB(image) >= 8){
            return;
        }
        Toast.makeText(activity, getString(R.string.DOWNLOAD), Toast.LENGTH_SHORT).show();

        final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type);
        final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);

        final Bitmap bmp = image;

        new Thread() {
            public void run() {
                mChannelMgr.setBusy(true);

                printer.doConfig();
                //printer.reset();
                // printer.printBitmap(bmp);

                boolean bPaper = printer.checkPaper();

                if (bPaper) {
                    printer.downloadNVBitmap(bmp);
                } else {
                    Util.showMessage(activity, "No paper detected");
                    beeper.beep();
                }

                mChannelMgr.setBusy(false);
            }
        }.start();
    }


    public void doPrintNvImage() {

        final Activity activity = this.getActivity();


        int type = mSetting.getPrinterVendor();
        int channel = mSetting.getPrinterChannel();

        // Choose and check device
        Device device = mChannelMgr.getDevice(channel);

        if (Util.checkDeviceAvailable(device, activity) == false)
            return;

        if (mChannelMgr.isBusy()) {
            Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();

        final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type);
        final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);


        new Thread() {
            public void run() {
                mChannelMgr.setBusy(true);

                printer.doConfig();

                boolean bPaper = printer.checkPaper();

                if (bPaper) {
                    printer.printNVBitmap();
                } else {
                    Util.showMessage(activity, "No paper detected");
                    beeper.beep();
                }

                mChannelMgr.setBusy(false);
            }
        }.start();
    }

    private boolean getPrintStatusIsOK(Device device) {
        final Activity activity = this.getActivity();
        int channel = mSetting.getPrinterChannel();

        // Choose and check device
        if (Util.checkDeviceAvailable(device, activity) == false)
            return false;

        if (mChannelMgr.isBusy()) {
            Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Bitmap getBitmapWithName(String bitmapName) {
        Bitmap image = null;
        try {
            //InputStream is = mAssertManager.open("demo1.bmp");
            InputStream is = mAssertManager.open(bitmapName);//"testqq.jpg"
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            return null;
        }
        return image;
    }

    private void doPrintBitmap(final int printType) {
        //获取打印机的通信通道
        int channel = mSetting.getPrinterChannel();
        int type = mSetting.getPrinterVendor();

        // Choose and check device
        final DeviceInfo info = Util.getPrinterInfo(getActivity(),mChannelMgr);
        boolean status = getPrintStatusIsOK(info.device);
        if (!status) {
            return;
        }

        Bitmap printfBitmap = null;
        if (printType == Printer.TYPE_PRINTF_BITMAP_0) {
            printfBitmap = null;
        }else if (printType == Printer.TYPE_PRINTF_BITMAP_1){
            printfBitmap = getBitmapWithName("testqq.jpg");
            if (null == printfBitmap){
                return;
            }
        }
        final Bitmap bitmap = printfBitmap;

        Util.showMessage(getActivity(), getString(R.string.PRINTING));
        final Printer printer = new Printer(info.device, info.port, type);
        final Beeper beeper = new Beeper(info.device, config.CONFIG_BEEPER_GPIO);

        new Thread() {
            public void run() {
                mChannelMgr.setBusy(true);
                printer.doConfig();
                boolean bPaper = printer.checkPaper();

                if (bPaper) {
                    printer.printBitmap(printType, bitmap);
                } else {
                    Util.showMessage(getActivity(), "No paper detected");
                    beeper.beep();
                }
                mChannelMgr.setBusy(false);
            }
        }.start();

    }

}
