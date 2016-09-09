/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

public class MyApplication extends Application {
	
	//public static MainActivity activity;
	public static Context appcontext;
	
	// private String NLDPath, NLDPath_Image; // APP更新文件夹路径
	
	// private SwipResult SwipResult = null; // 刷卡结果
	private static String mVersion = null;
	/*private String NLDPathString = null; // APP更新文件路径
	private int Ic_pinInput_flag = 0; // 当前做出是否是IC外部输入密码模式，0否，1是
	private int Open_card_reader_flag = 0; // 当前操作是否是开启读卡器操作，0否，1是
	private EmvTransController controller;
	private BigDecimal amt;
	private byte[] result;
	private Map<ICCardSlot, ICCardSlotState> map = new HashMap<ICCardSlot, ICCardSlotState>(); // 卡槽状态
	private byte[] icCardSecondTrcad;
	private TLVPackage tlvPackage;
	private byte[] icCardPlainSecondTrackData;
	private String icCardNum;
	private byte[] pin;*/

    //public static boolean isQrReader = false;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		appcontext = this;
		
		mVersion = getAppVersion();
		
		/*if (ifSDCardExit()) {
			NLDPath = "/sdcard/data/data/com.example.mainapp/update";
			NLDPath_Image = "/sdcard/data/data/com.example.mainapp/image";
		} else {
			NLDPath = "/data/data/com.example.mainapp/update";
			NLDPath_Image = "/data/data/com.example.mainapp/image";
		}

		File updateFile = new File(NLDPath);
		if (!updateFile.exists()) {
			updateFile.mkdirs();
			System.out.println("======= =====================");
		}
		File imageFile = new File(NLDPath_Image);
		if (!imageFile.exists()) {
			imageFile.mkdirs();
		}
		try {
			InputStream is = this.getResources().getAssets().open("ic_launcher.png");
			NLDPath_Image = NLDPath_Image + "/ic_launcher.png";
			File file = new File(NLDPath_Image);
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			byte temp[] = new byte[1024];
			while (is.read(temp) != -1) {
				os.write(temp);
			}
			System.out.println("图片创建成功,位置" + NLDPath_Image);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	/*============================================================================*/
	/**
	 * 判断SD卡是否存在
	 * 
	 * @return
	 */
	public boolean ifSDCardExit() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getAppVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public static String getVersion() {
		return mVersion;
	}

	/*public String getNLDPath() {
		return NLDPath;
	}

	public void setNLDPath(String nLDPath) {
		NLDPath = nLDPath;
	}

	public String getNLDPathString() {
		return NLDPathString;
	}

	public void setNLDPathString(String nLDPathString) {
		NLDPathString = nLDPathString;
	}

	public SwipResult getSwipResult() {
		return SwipResult;
	}

	public void setSwipResult(SwipResult swipResult) {
		SwipResult = swipResult;
	}

	/*public int getIc_pinInput_flag() {
		return Ic_pinInput_flag;
	}

	public void setIc_pinInput_flag(int ic_pinInput_flag) {
		Ic_pinInput_flag = ic_pinInput_flag;
	}

	public int getOpen_card_reader_flag() {
		return Open_card_reader_flag;
	}

	public void setOpen_card_reader_flag(int open_card_reader_flag) {
		Open_card_reader_flag = open_card_reader_flag;
	}

	public BigDecimal getAmt() {
		return amt;
	}

	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}

	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}

	public Map<ICCardSlot, ICCardSlotState> getMap() {
		return map;
	}

	public void setMap(Map<ICCardSlot, ICCardSlotState> map) {
		this.map = map;
	}

	public EmvTransController getController() {
		return controller;
	}

	public void setController(EmvTransController controller) {
		this.controller = controller;
	}

	public byte[] getIcCardSecondTrcad() {
		return icCardSecondTrcad;
	}

	public void setIcCardSecondTrcad(byte[] icCardSecondTrcad) {
		this.icCardSecondTrcad = icCardSecondTrcad;
	}

	public TLVPackage getTlvPackage() {
		return tlvPackage;
	}

	public void setTlvPackage(TLVPackage tlvPackage) {
		this.tlvPackage = tlvPackage;
	}

	public byte[] getIcCardPlainSecondTrackData() {
		return icCardPlainSecondTrackData;
	}

	public void setIcCardPlainSecondTrackData(byte[] icCardPlainSecondTrackData) {
		this.icCardPlainSecondTrackData = icCardPlainSecondTrackData;
	}

	public String getIcCardNum() {
		return icCardNum;
	}

	public void setIcCardNum(String icCardNum) {
		this.icCardNum = icCardNum;
	}

	public byte[] getPin() {
		return pin;
	}

	public void setPin(byte[] pin) {
		this.pin = pin;
	}*/
	/*============================================================================*/
}
