/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import java.io.IOException;
import java.io.InputStream;

import com.cynoware.posmate.sdk.Beeper;
import com.cynoware.posmate.sdk.Device;
import com.cynoware.posmate.sdk.Printer;
import com.cynoware.posmate.sdk.config;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
		
		mEditEnglish = (EditText)rootView.findViewById(R.id.txtEnglish);
//	    mEditKorean = (EditText)rootView.findViewById(R.id.txtKorean);
//	    mEditJapanese = (EditText)rootView.findViewById(R.id.txtJapanese);
//	    mEditChinese = (EditText)rootView.findViewById(R.id.txtChinese);
	    mEditBarcode = (EditText)rootView.findViewById(R.id.txtBarcode);
	    
    	Spinner spinPrinter = (Spinner)rootView.findViewById( R.id.spinnerPrinter);
    	String types[] = { "YanKe", "JingXin-1", "JingXin-2", "JingXin-3", "JingXin-4" };    	
    	Adapter adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, types );
    	spinPrinter.setAdapter((SpinnerAdapter) adapter);
    	spinPrinter.setSelection(4);
    	
    	spinPrinter.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch( position ){
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
    	
    	Spinner spinChannel = (Spinner)rootView.findViewById( R.id.spinnerPrinterChannel);
    	String channels[] = { "Dock USB", "Dock Bluetooth" };
    	Adapter adapterChannel = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, channels );
    	spinChannel.setAdapter((SpinnerAdapter) adapterChannel);
    	
    	if( mSetting.getPrinterChannel() == ChannelManager.CHANNEL_DOCK_USB )
    		spinChannel.setSelection( 0 );
    	else
    		spinChannel.setSelection( 1 );
    	
    	
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

		Spinner spinCustomText = (Spinner)rootView.findViewById( R.id.spinnerCustomText);
		String customTexts[] = { "English", "Korean","Japanese","Chinese" };
		Adapter adapterCustomText = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1, customTexts );
		spinCustomText.setAdapter((SpinnerAdapter) adapterCustomText);
		spinCustomText.setSelection(0);

		spinCustomText.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						mEditEnglish.setText(getString(R.string.EnglishString));
						Printer.CHARSET_TYPE=0;
						break;
					case 1:
						mEditEnglish.setText(getString(R.string.KoreanString));
						Printer.CHARSET_TYPE=1;
						break;
					case 2:
						mEditEnglish.setText(getString(R.string.JapaneseString));
						Printer.CHARSET_TYPE=2;
						break;
					case 3:
						mEditEnglish.setText(getString(R.string.ChineseString));
						Printer.CHARSET_TYPE=3;
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

    	Button btnEnglish = (Button)rootView.findViewById(R.id.btnEnglish);
    	if(btnEnglish != null){
    		btnEnglish.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String txt = mEditEnglish.getText().toString()+BLANK_ROWS;
					doPrint( txt, Printer.CHARSET_TYPE );
				}
			});
    	}


		Button btnBarcode = (Button)rootView.findViewById(R.id.btnBarcode);
		if(btnBarcode != null){
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

		final ToggleButton mToggleButton =(ToggleButton)rootView.findViewById(R.id.expand);
		final LinearLayout expandWindw = (LinearLayout)rootView.findViewById(R.id.window);
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



    	Button btnDownloadNVImage = (Button)rootView.findViewById(R.id.btnDownloadNVImage);
    	if(btnDownloadNVImage != null){
    		btnDownloadNVImage.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doDownloadNvImage();
				}
			});
    	}

    	Button btnPrintNVImage = (Button)rootView.findViewById(R.id.btnPrintNVImage);
    	if(btnPrintNVImage != null){
    		btnPrintNVImage.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					doPrintNvImage();
				}
			});
    	}
	    
		return rootView;
	}
	
	public void doPrint( final String text, final int charset ){
	   	
		final Activity activity = this.getActivity();
    	if( text == null || text.isEmpty() ){
    		Toast.makeText( activity, "Nothing to print", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	int type = mSetting.getPrinterVendor();
    	int channel = mSetting.getPrinterChannel();
    	
    	// Choose and check deviceѡ��
    	Device device = mChannelMgr.getDevice(channel);
    	
    	if( Util.checkDeviceAvailable(device, activity) == false )
    		return;
        	
    	if (mChannelMgr.isBusy()) {
    		Toast.makeText(activity, getString(R.string.SYSTEM_BUSY), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();
    	
    	final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type );   //CONFIG_PRINTER_UART = 5
    	final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);	//CONFIG_BEEPER_GPIO = 79
    	
    	new Thread() {
    		public void run() {
    			mChannelMgr.setBusy( true );
    			
    			printer.doConfig();
    			
    			boolean bPaper = printer.checkPaper();
    			
    			if( bPaper ){
    				printer.print(text, charset);
    			}
    			else{
    				Util.showMessage(activity, "No paper detected" );
    				beeper.beep();
    			}    			

    	        mChannelMgr.setBusy( false );
	        }
    	}.start();
    }

	public void doPrintBarcode( final String text ){
	   	
		final Activity activity = this.getActivity();
    	if( text == null || text.isEmpty() ){
    		Toast.makeText( activity, "Nothing to print", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
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
    	
    	Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();
    	
    	final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type );   
    	final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);
    	
    	new Thread() {
    		public void run() {
    			mChannelMgr.setBusy( true );
    			
    			printer.doConfig();
    			
    			boolean bPaper = printer.checkPaper();
    			
    			if( bPaper ){
    				printer.printBarcode(text);
    			}
    			else{
    				Util.showMessage(activity, "No paper detected" );
    				beeper.beep();
    			}

    	        mChannelMgr.setBusy( false );
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

	
	public void doDownloadNvImage(){
		
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
	        InputStream is = mAssertManager.open("demo1.bmp");  
	        image = BitmapFactory.decodeStream(is);  
	        is.close();  
	    }  
	    catch (IOException e)  
	    {  
	        e.printStackTrace();
	        Toast.makeText(activity, "Load bitmap fail", Toast.LENGTH_SHORT).show();
	        return;
	    }
		
		Toast.makeText(activity, getString(R.string.DOWNLOAD), Toast.LENGTH_SHORT).show();
		
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
					printer.downloadNVBitmap(bmp);
				}
				else{
					Util.showMessage(activity, "No paper detected" );
					beeper.beep();
				}
	
		        mChannelMgr.setBusy( false );
	        }
		}.start();
	}    
	
	
	public void doPrintNvImage(){
		
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
		
		Toast.makeText(activity, getString(R.string.PRINTING), Toast.LENGTH_SHORT).show();
		
		final Printer printer = new Printer(device, config.CONFIG_PRINTER_UART, type );   
		final Beeper beeper = new Beeper(device, config.CONFIG_BEEPER_GPIO);
		
		
		new Thread() {
			public void run() {
				mChannelMgr.setBusy( true );
				
				printer.doConfig();
				
				boolean bPaper = printer.checkPaper();
				
				if( bPaper ){
					printer.printNVBitmap();
				}
				else{
					Util.showMessage(activity, "No paper detected" );
					beeper.beep();
				}
	
		        mChannelMgr.setBusy( false );
	        }
		}.start();
	}    
}
