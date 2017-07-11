/**
 * Implement a EOS/POS command helper
 * 
 * For JinXing JX2R01
 * 
 * 2016, Jie Zhuang
 */

package com.cynoware.posmate.sdk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.util.Log;

public class EscCommand {

	private ArrayList<byte[]> mCmdList = new ArrayList<byte[]>();

	class WrongParaException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	class ExceedBmpSizeException extends Exception {
		private static final long serialVersionUID = 2L;
	}

	public void clear() {
		mCmdList.clear();
	}

	public void add(byte[] cmd) {
		mCmdList.add(cmd);
	}

	public byte[] createCommandBuffer() {
		if (mCmdList == null)
			return null;

		int length = 0;
		for (byte[] cmd : mCmdList) {
			length += cmd.length;
		}

		byte[] buf = null;

		buf = new byte[length];

		int count = 0;
		for (byte[] cmd : mCmdList) {
			System.arraycopy(cmd, 0, buf, count, cmd.length);
			count += cmd.length;
		}

		return buf;
	}

	/**
	 * 换行
	 */
	public void addPrintAndLineFeed() {
		byte[] command = { 10 };
		add(command);
	}

	/**
	 * 字符右间距
	 * 
	 * @param n
	 */
	public void addSetRightSideCharacterSpacing(byte n) {
		byte[] command = { 27, 32, 0 };
		command[2] = n;
		add(command);
	}

	/**
	 * 允许/禁止上划线
	 * 
	 * @param onoff
	 */
	public void addSetOverlineMode(boolean onoff) {
		byte[] command = { 27, 43, 0 };
		command[2] = (byte) (onoff ? 1 : 0);
		add(command);
	}

	/**
	 * 允许/禁止下划线
	 * 
	 * @param onoff
	 */
	public void addSetUnderlineMode(boolean onoff) {
		byte[] command = { 27, 45, 0 };
		command[2] = (byte) (onoff ? 1 : 0);
		add(command);
	}

	/**
	 * 设置行间距
	 * 
	 * @param n
	 */
	public void addSetLineSpacing(byte n) {
		byte[] cmd = { 0x1B, 0x31, n };
		add(cmd);
	}

	/**
	 * 设置默认行高(4*0.0625mm)
	 * 
	 */
	public void addSelectDefaultLineHeight() {
		byte[] cmd = { 0x1B, 0x32 };
		add(cmd);
	}

	/**
	 * 设置行高
	 * 
	 * @param n
	 */
	public void addSetLineHeight(byte n) {
		byte[] cmd = { 0x1B, 0x33, n };
		add(cmd);
	}

	/**
	 * 初始化
	 */
	public void addInitializePrinter() {
		byte[] cmd = { 0x1B, 0x40 };
		add(cmd);
	}

	/**
	 * 走纸
	 * 
	 * @param n
	 *            行
	 */
	public void addPrintAndFeedPaper(byte n) {
		byte[] command = { 0x1B, 0x4A, n };
		add(command);
	}

	/**
	 * 右侧不打印的字符数
	 * 
	 * @param n
	 */
	public void addSetRightBlankCharacters(byte n) {
		byte[] command = { 0x1B, 0x51, n };
		add(command);
	}

	/**
	 * 设置横向放大倍数
	 * 
	 * @param n
	 */
	public void addSetXScale(byte n) {
		byte[] command = { 0x1B, 0x55, n };
		add(command);
	}

	/**
	 * 设置纵向放大倍数
	 * 
	 * @param n
	 */
	public void addSetYScale(byte n) {
		byte[] command = { 0x1B, 0x56, n };
		add(command);
	}

	/**
	 * 设置放大倍数(横向与纵向同时)
	 * 
	 * @param n
	 */
	public void addSetScale(byte n) {
		byte[] command = { 0x1B, 0x57, n };
		add(command);
	}

	/**
	 * 允许或禁止反白模式
	 * 
	 * @param onoff
	 */
	public void addSetReverseMode(boolean onoff) {
		byte[] command = { 0x1D, 0x42, (byte) (onoff ? 1 : 0) };
		add(command);
	}

	/**
	 * 右侧不打印的字符数
	 * 
	 * @param n
	 */
	public void addSetLeftBlankCharacters(byte n) {
		byte[] command = { 0x1B, 0x6C, n };
		add(command);
	}

	/**
	 * 设置字符间距
	 * 
	 * @param n
	 */
	public void addSetCharacterSpacing(byte n) {
		byte[] command = { 0x1B, 0x70, n };
		add(command);
	}

	/**
	 * 查询软件版本
	 * 
	 * @param n
	 */
	public void addCheckVersion() {
		byte[] command = { 0x1B, 0x1B, 0x56 };
		add(command);
	}

	/**
	 * 查询是否缺纸
	 */
	public void addCheckPaper() {
		byte[] cmd = { 0x1B, 0x1B, 0x70 };
		add(cmd);
	}

	/**
	 * 设置字符放大倍数
	 * 
	 * @param x
	 *            - 横向倍数
	 * @param y
	 *            - 纵向倍数
	 * @return
	 */
	public boolean addSetCharcterSize(byte x, byte y) {
		if (x < 0 || x > 5 || y < 0 || y > 5)
			return false;

		byte[] command = { 0x1D, 0x21, 0x00 };
		command[2] = (byte) ((y << 4) | x);
		add(command);

		return true;
	}

	/**
	 * 设置条码布局
	 * 
	 * @param n
	 *            BARCODE_TEXT_ABOVE or BARCODE_TEXT_BELOW;
	 */
	public static final byte BARCODE_TEXT_ABOVE = 0x31;
	public static final byte BARCODE_TEXT_BELOW = 0x32;

	public void addBarCodeLayout(byte n) {
		byte[] cmd = { 0x1D, 0x48, n };
		add(cmd);
	}

	/**
	 * 设置条码高度
	 * 
	 * @param n
	 *            height in pt.
	 */
	public void addBarCodeHeight(byte n) {
		byte[] cmd = { 0x1D, 0x68, n };
		add(cmd);
	}

	/**
	 * 设置条码宽度
	 * 
	 * @param n
	 *            always 0x02
	 */
	public void addBarCodeWidth(byte n) {
		byte[] cmd = { 0x1D, 0x77, n };
		add(cmd);
	}

	/**
	 * 打印条码
	 * 
	 * @param m
	 *            BARCODE_CODE_39 or BARCODE_CODE_128
	 * @param n
	 *            length - 2
	 * @param code
	 */
	public static final byte BARCODE_CODE_39 = 0x04;
	public static final byte BARCODE_CODE_128 = 0x49;

	public void addBarCodePrint(byte m, byte n, byte[] d)
			throws WrongParaException {
		byte[] cmd;

		if (m == BARCODE_CODE_39) {
			cmd = new byte[3 + d.length + 1];
			cmd[0] = 0x1D;
			cmd[1] = 0x6B;
			cmd[2] = m;
			System.arraycopy(d, 0, cmd, 3, d.length);
		} else if (m == BARCODE_CODE_128) {
			cmd = new byte[4 + d.length + 1];
			cmd[0] = 0x1D;
			cmd[1] = 0x6B;
			cmd[2] = m;
			cmd[3] = n;
			System.arraycopy(d, 0, cmd, 4, d.length);
		} else {
			throw new WrongParaException();
		}

		cmd[cmd.length - 1] = 0x00;

		add(cmd);
	}

	/**
	 * @param n
	 *            default 30.
	 */
	public void addLineHeight(byte n) {
		byte[] cmd = { 0x1B, 0x33, n };
		add(cmd);
	}

	/**
	 * 
	 * @param m
	 * @param bmp
	 */

	public static final byte BMP_8_1 = 0x00;
	public static final byte BMP_8_2 = 0x01;
	public static final byte BMP_24_1 = 0x20;
	public static final byte BMP_24_2 = 0x21;

	private static final byte BMP_MAX_NH = 1;

	public void addBmpPrint(Bitmap bmp) throws WrongParaException,
			ExceedBmpSizeException {

		if (bmp == null) {
			throw new WrongParaException();
		}

		int width = bmp.getWidth();
		int height = bmp.getHeight();

		byte nL = (byte) (width % 256);
		byte nH = (byte) (width / 256);

		if (nH > BMP_MAX_NH) {
			throw new ExceedBmpSizeException();
		}

		byte[] buf = draw2PxPoint(bmp);
		add(buf);

		// byte[] cmd = new byte[] { 0x1B, 0x2A, BMP_24_2, nL, nH, 0x00, 0x00,
		// 0x00 };
		/*
		 * byte[] data = new byte[] { 0x00, 0x00, 0x00}; byte[] cmd = new byte[]
		 * { 0x1B, 0x2A, BMP_24_2, nL, nH };
		 * 
		 * int step_height = 24; int pixel;
		 * 
		 * 
		 * for (int i = 0; i < (height / step_height + 1); i++) {
		 * 
		 * 
		 * //循环宽 for (int px = 0; px < width; px++) { int y_start = i *
		 * step_height; int y_end = y_start + step_height;
		 * 
		 * for ( int py = y_start, k=0; py < y_end; py++, k++) {
		 * 
		 * if (py < height) // if within the BMP size { pixel = bmp.getPixel(px,
		 * py); if ( Color.red(pixel)== 0) { data[k / 8] += (byte)(0x80 >> (k %
		 * 8)); } } }
		 * 
		 * add( cmd ); add( data ); } }
		 */

	}

	public static byte[] draw2PxPoint(Bitmap bmp) {

		byte[] data = new byte[16290];
		int k = 0;
		for (int j = 0; j < 15; j++) {
			data[k++] = 0x1B;
			data[k++] = 0x2A;
			data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
			data[k++] = 0x68;
			data[k++] = 0x01;
			for (int i = 0; i < 360; i++) {
				for (int m = 0; m < 3; m++) {
					for (int n = 0; n < 8; n++) {
						byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
						data[k] += data[k] + b;
					}
					k++;
				}
			}
			data[k++] = 0x0A;
		}
		return data;
	}

	public static byte px2Byte(int x, int y, Bitmap bit) {
		byte b;
		int pixel = bit.getPixel(x, y);
		int red = (pixel & 0x00ff0000) >> 16; // 取高两位
		int green = (pixel & 0x0000ff00) >> 8; // 取中两位
		int blue = pixel & 0x000000ff; // 取低两位

		int gray = (int) (0.29900 * red + 0.58700 * green + 0.11400 * blue); // 灰度转化公式
		if (gray < 128) {
			b = 1;
		} else {
			b = 0;
		}
		return b;
	}

	/**
	 * 设置下载到Nvram的位图
	 * 
	 * @param n
	 *            - id
	 * @param bmp
	 */

	public void addDownloadNvBitImage(Bitmap[] bitmap) {
		if (bitmap != null) {
			Log.d("BMP", "bitmap.length " + bitmap.length);
			int n = bitmap.length;
			if (n > 0) {
				byte[] command = new byte[3];
				command[0] = 28;
				command[1] = 113;
				command[2] = (byte) n;
				add(command);

				for (int i = 0; i < n; i++) {
					int height = (bitmap[i].getHeight() + 7) / 8 * 8;
					int width = bitmap[i].getWidth() * height
							/ bitmap[i].getHeight();
					Bitmap grayBitmap = GpUtils.toGrayscale(bitmap[i]);
					Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width,
							height);
					byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
					height = src.length / width;
					Log.d("BMP", "bmp  Width " + width);
					Log.d("BMP", "bmp  height " + height);
					byte[] codecontent = GpUtils.pixToEscNvBitImageCmd(src,
							width, height);
					
					add(codecontent);
					/*(for (int k = 0; k < codecontent.length; k++) {
						this.Command.add(Byte.valueOf(codecontent[k]));
					}*/
				}
			}
		} else {
			Log.d("BMP", "bmp.  null ");
			return;
		}
	}

	/*
	 * private static final int NV_BMP_MAX_WIDTH = 1023; private static final
	 * int NV_BMP_MAX_HEIGHT = 288;
	 * 
	 * public void addNVBmpDownload(byte n, Bitmap bmp) throws
	 * WrongParaException, ExceedBmpSizeException {
	 * 
	 * if (bmp == null) { throw new WrongParaException(); }
	 * 
	 * int width = bmp.getWidth(); int height = bmp.getHeight();
	 * 
	 * if (width > NV_BMP_MAX_WIDTH || height > NV_BMP_MAX_HEIGHT) { throw new
	 * ExceedBmpSizeException(); }
	 * 
	 * byte xL = (byte) (width % 256); byte xH = (byte) (width / 256); byte yL =
	 * (byte) (height % 256); byte yH = (byte) (height / 256);
	 * 
	 * byte[] escFSQ = new byte[] { 0x1C, 0x71, 0x01, xL, xH, yL, yH };
	 * 
	 * ByteArrayOutputStream stream = new ByteArrayOutputStream();
	 * bmp.compress(Bitmap.CompressFormat.PNG, 100, stream); byte[] byteBmp =
	 * stream.toByteArray();
	 * 
	 * byte[] final_buf = new byte[escFSQ.length + byteBmp.length]; int count =
	 * 0;
	 * 
	 * System.arraycopy(escFSQ, 0, final_buf, 0, escFSQ.length); count +=
	 * escFSQ.length;
	 * 
	 * System.arraycopy(byteBmp, 0, final_buf, count, byteBmp.length); count +=
	 * byteBmp.length;
	 * 
	 * add(final_buf); }
	 */

	public void addPrintNvBitmap(byte n, byte mode) {
		byte[] command = { 28, 112, n, mode };
		add(command);
	}

	/**
	 * 打印光栅位图
	 * 
	 * @param bitmap
	 * @param nWidth
	 * @param nMode
	 */
	public void addRastBitImage(Bitmap bitmap, int nWidth, int nMode) {
		if (bitmap != null) {
			int width = (nWidth + 7) / 8 * 8;
			int height = bitmap.getHeight() * width / bitmap.getWidth();
			Bitmap grayBitmap = GpUtils.toGrayscale(bitmap);
			Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width, height);
			byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
			byte[] command = new byte[8];
			height = src.length / width;
			command[0] = 29;
			command[1] = 118;
			command[2] = 48;
			command[3] = (byte) (nMode & 0x1);
			command[4] = (byte) (width / 8 % 256);
			command[5] = (byte) (width / 8 / 256);
			command[6] = (byte) (height % 256);
			command[7] = (byte) (height / 256);
			add(command);
			byte[] codecontent = GpUtils.pixToEscRastBitImageCmd(src);
			add(codecontent);
			/*for (int k = 0; k < codecontent.length; k++) {
				this.Command.add(Byte.valueOf(codecontent[k]));
			}*/
		} else {
			Log.d("BMP", "bmp.  null ");
		}
	}

	/*
	 * public static final byte CHINESE_PRINT_MODE_DOUBLE_WIDTH = 0x04; public
	 * static final byte CHINESE_PRINT_MODE_DOUBLE_HEIGHT = 0x08; public static
	 * final byte CHINESE_PRINT_MODE_UNDERLINE = (byte) 0x80;
	 * 
	 * public void addSetChinesePrintMode(byte n) { byte[] cmd = { 0x1C, 0x21, n
	 * }; add(cmd); }
	 */

	/**
	 * 设置打印字符
	 */
	public static final byte CHARSET_JAPANESE = 0;
	public static final byte CHARSET_KOREAN = 1;

	public void addSetCharset(byte n) {
		byte[] cmd = { 0x1C, 0x26, n };
		add(cmd);
	}
}
