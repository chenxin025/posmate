/*     */ package com.cynoware.posmate.sdk.util;
/*     */ 
/*     */

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ public class GpUtils
/*     */ {
/*  17 */   private static int[] p0 = { 0, 128 };
/*  18 */   private static int[] p1 = { 0, 64 };
/*  19 */   private static int[] p2 = { 0, 32 };
/*  20 */   private static int[] p3 = { 0, 16 };
/*  21 */   private static int[] p4 = { 0, 8 };
/*  22 */   private static int[] p5 = { 0, 4 };
/*  23 */   private static int[] p6 = { 0, 2 };
/*     */ 
/*  25 */   private static int[][] Floyd16x16 = { 
/*  26 */     { 0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 
/*  27 */     170 }, 
/*  28 */     { 192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 
/*  29 */     234, 106 }, 
/*  30 */     { 48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 
/*  31 */     26, 154 }, 
/*  32 */     { 240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 
/*  33 */     122, 218, 90 }, 
/*  34 */     { 12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 
/*  35 */     166 }, 
/*  36 */     { 204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 
/*  37 */     230, 102 }, 
/*  38 */     { 60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 
/*  39 */     22, 150 }, 
/*  40 */     { 252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 
/*  41 */     118, 214, 86 }, 
/*  42 */     { 3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 
/*  43 */     169 }, 
/*  44 */     { 195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 
/*  45 */     233, 105 }, 
/*  46 */     { 51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 
/*  47 */     25, 153 }, 
/*  48 */     { 243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 
/*  49 */     121, 217, 89 }, 
/*  50 */     { 15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 
/*  51 */     165 }, 
/*  52 */     { 207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 
/*  53 */     229, 101 }, 
/*  54 */     { 63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 
/*  55 */     21, 149 }, 
/*  56 */     { 254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 
/*  57 */     117, 213, 85 } };
/*     */ 
/*  59 */   private static int[][] Floyd8x8 = { { 0, 32, 8, 40, 2, 34, 10, 42 }, 
/*  60 */     { 48, 16, 56, 24, 50, 18, 58, 26 }, 
/*  61 */     { 12, 44, 4, 36, 14, 46, 6, 38 }, 
/*  62 */     { 60, 28, 52, 20, 62, 30, 54, 22 }, 
/*  63 */     { 3, 35, 11, 43, 1, 33, 9, 41 }, 
/*  64 */     { 51, 19, 59, 27, 49, 17, 57, 25 }, 
/*  65 */     { 15, 47, 7, 39, 13, 45, 5, 37 }, 
/*  66 */     { 63, 31, 55, 23, 61, 29, 53, 21 } };
/*     */   public static final int ALGORITHM_DITHER_16x16 = 16;
/*     */   public static final int ALGORITHM_DITHER_8x8 = 8;
/*     */   public static final int ALGORITHM_TEXTMODE = 2;
/*     */   public static final int ALGORITHM_GRAYTEXTMODE = 1;
/*     */ 
/*     */   public static Bitmap resizeImage(Bitmap bitmap, int w, int h)
/*     */   {
/*  74 */     Bitmap BitmapOrg = bitmap;
/*     */ 
/*  76 */     int width = BitmapOrg.getWidth();
/*  77 */     int height = BitmapOrg.getHeight();
/*  78 */     int newWidth = w;
/*  79 */     int newHeight = h;
/*     */ 
/*  81 */     float scaleWidth = newWidth / width;
/*  82 */     float scaleHeight = newHeight / height;
/*  83 */     Matrix matrix = new Matrix();
/*     */ 
/*  85 */     matrix.postScale(scaleWidth, scaleHeight);
/*     */ 
/*  87 */     Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, 
/*  88 */       height, matrix, true);
/*     */ 
/*  90 */     return resizedBitmap;
/*     */   }
/*     */ 
/*     */   public static void saveMyBitmap(Bitmap mBitmap) {
/*  94 */     File f = new File(Environment.getExternalStorageDirectory().getPath(), 
/*  95 */       "Btatotest.jpeg");
/*     */     try {
/*  97 */       f.createNewFile();
/*     */     } catch (IOException localIOException) {
/*     */     }
/* 100 */     FileOutputStream fOut = null;
/*     */     try {
/* 102 */       fOut = new FileOutputStream(f);
/* 103 */       mBitmap.compress(CompressFormat.PNG, 100, fOut);
/* 104 */       fOut.flush();
/* 105 */       fOut.close();
/*     */     } catch (FileNotFoundException localFileNotFoundException) {
/*     */     } catch (IOException localIOException1) {
/*     */     }
/*     */   }
/*     */
/*     */   public static Bitmap toGrayscale(Bitmap bmpOriginal) {
/* 112 */     int height = bmpOriginal.getHeight();
/* 113 */     int width = bmpOriginal.getWidth();
/* 114 */     Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
/* 115 */       Config.RGB_565);
/* 116 */     Canvas c = new Canvas(bmpGrayscale);
/* 117 */     Paint paint = new Paint();
/* 118 */     ColorMatrix cm = new ColorMatrix();
/* 119 */     cm.setSaturation(0.0F);
/* 120 */     ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
/* 121 */     paint.setColorFilter(f);
/* 122 */     c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
/* 123 */     return bmpGrayscale;
/*     */   }
/*     */   static byte[] pixToEscRastBitImageCmd(byte[] src, int nWidth, int nMode) {
/* 126 */     int nHeight = src.length / nWidth;
/* 127 */     byte[] data = new byte[8 + src.length / 8];
/* 128 */     data[0] = 29;
/* 129 */     data[1] = 118;
/* 130 */     data[2] = 48;
/* 131 */     data[3] = (byte)(nMode & 0x1);
/* 132 */     data[4] = (byte)(nWidth / 8 % 256);
/* 133 */     data[5] = (byte)(nWidth / 8 / 256);
/* 134 */     data[6] = (byte)(nHeight % 256);
/* 135 */     data[7] = (byte)(nHeight / 256);
/* 136 */     int i = 8; for (int k = 0; i < data.length; i++) {
/* 137 */       data[i] = 
/* 139 */         (byte)(p0[src[k]] + p1[src[(k + 1)]] + p2[src[(k + 2)]] + 
/* 138 */         p3[src[(k + 3)]] + p4[src[(k + 4)]] + p5[src[(k + 5)]] + 
/* 139 */         p6[src[(k + 6)]] + src[(k + 7)]);
/* 140 */       k += 8;
/*     */     }
/* 142 */     return data;
/*     */   }
/*     */   static byte[] pixToEscRastBitImageCmd(byte[] src) {
/* 145 */     byte[] data = new byte[src.length / 8];
/* 146 */     int i = 0; for (int k = 0; i < data.length; i++) {
/* 147 */       data[i] = 
/* 149 */         (byte)(p0[src[k]] + p1[src[(k + 1)]] + p2[src[(k + 2)]] + 
/* 148 */         p3[src[(k + 3)]] + p4[src[(k + 4)]] + p5[src[(k + 5)]] + 
/* 149 */         p6[src[(k + 6)]] + src[(k + 7)]);
/* 150 */       k += 8;
/*     */     }
/* 152 */     return data;
/*     */   }
/*     */   static byte[] pixToEscNvBitImageCmd(byte[] src, int width, int height) {
/* 155 */     byte[] data = new byte[src.length / 8+4 ];

	         // data[0] = (byte)width;
/* 156 */     data[0] = (byte)(width / 8 % 256);
/* 157 */     data[1] = (byte)(width / 8 / 256);
/* 158 */     data[2] = (byte)(height / 8 % 256);
/* 159 */     data[3] = (byte)(height / 8 / 256);
/* 160 */     int k = 0;
/* 161 */     for (int i = 0; i < width; i++) {
/* 162 */       k = 0;
/* 163 */       for (int j = 0; j < height / 8; j++) {
/* 164 */         data[( 4+j + i * height / 8)] =
/* 166 */           (byte)(p0[src[(i + k)]] + p1[src[(i + k + 1 * width)]] + p2[src[(i + k + 2 * width)]] + 
/* 165 */           p3[src[(i + k + 3 * width)]] + p4[src[(i + k + 4 * width)]] + p5[src[(i + k + 5 * width)]] + 
/* 166 */           p6[src[(i + k + 6 * width)]] + src[(i + k + 7 * width)]);
/* 167 */         k += 8 * width;
/*     */       }
/*     */     }
/* 170 */     return data;
/*     */   }
/*     */   public static byte[] pixToTscCmd(byte[] src) {
/* 173 */     byte[] data = new byte[src.length / 8];
/*     */ 
/* 175 */     int k = 0; for (int j = 0; k < data.length; k++) {
/* 176 */       byte temp = (byte)(p0[src[j]] + p1[src[(j + 1)]] + p2[src[(j + 2)]] + 
/* 177 */         p3[src[(j + 3)]] + p4[src[(j + 4)]] + p5[src[(j + 5)]] + 
/* 178 */         p6[src[(j + 6)]] + src[(j + 7)]);
/* 179 */       data[k] = (byte)(temp ^ 0xFFFFFFFF);
/* 180 */       j += 8;
/*     */     }
/* 182 */     return data;
/*     */   }
/*     */ 
/*     */   public static byte[] pixToTscCmd(int x, int y, int mode, byte[] src, int nWidth) {
/* 186 */     int height = src.length / nWidth;
/* 187 */     int width = nWidth / 8;
/* 188 */     String str = "BITMAP " + x + "," + y + "," + width + "," + height + "," + 
/* 189 */       mode + ",";
/* 190 */     byte[] bitmap = null;
/*     */     try {
/* 192 */       bitmap = str.getBytes("GB2312");
/*     */     }
/*     */     catch (UnsupportedEncodingException e) {
/* 195 */       e.printStackTrace();
/*     */     }
/* 197 */     byte[] arrayOfByte = new byte[src.length / 8];
/*     */ 
/* 199 */     int k = 0; for (int j = 0; k < arrayOfByte.length; k++) {
/* 200 */       byte temp = (byte)(p0[src[j]] + p1[src[(j + 1)]] + p2[src[(j + 2)]] + 
/* 201 */         p3[src[(j + 3)]] + p4[src[(j + 4)]] + p5[src[(j + 5)]] + 
/* 202 */         p6[src[(j + 6)]] + src[(j + 7)]);
/* 203 */       arrayOfByte[k] = (byte)(temp ^ 0xFFFFFFFF);
/* 204 */       j += 8;
/*     */     }
/* 206 */     byte[] data = new byte[bitmap.length + arrayOfByte.length];
/* 207 */     System.arraycopy(bitmap, 0, data, 0, bitmap.length);
/* 208 */     System.arraycopy(arrayOfByte, 0, data, bitmap.length, 
/* 209 */       arrayOfByte.length);
/* 210 */     return data;
/*     */   }
/*     */ 
/*     */   private static void format_K_dither16x16(int[] orgpixels, int xsize, int ysize, byte[] despixels)
/*     */   {
/* 215 */     int k = 0;
/* 216 */     for (int y = 0; y < xsize; y++)
/* 217 */       for (int x = 0; x < ysize/8; x++) {
/* 218 */         if ((orgpixels[k] & 0xFF) > Floyd16x16[(y & 0xF)][(x & 0xF)])
/* 219 */           despixels[k] = 0;
/*     */         else {
/* 221 */           despixels[k] = 1;
/*     */         }
/* 223 */         k++;
/*     */       }
/*     */   }
/*     */ 
/*     */   public static byte[] bitmapToBWPix(Bitmap mBitmap)
/*     */   {
/* 230 */     int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
/* 231 */     byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
/* 232 */     Bitmap grayBitmap = toGrayscale(mBitmap);
/* 233 */     grayBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, 
/* 234 */       mBitmap.getWidth(), mBitmap.getHeight());
/*     */ 
/* 236 */     format_K_dither16x16(pixels, grayBitmap.getWidth(), 
/* 237 */       grayBitmap.getHeight(), data);
/*     */ 
/* 239 */     return data;
/*     */   }


/*     */ 
/*     */   private static void format_K_dither16x16_int(int[] orgpixels, int xsize, int ysize, int[] despixels)
/*     */   {
/* 245 */     int k = 0;
/* 246 */     for (int y = 0; y < ysize; y++)
/* 247 */       for (int x = 0; x < xsize; x++) {
/* 248 */         if ((orgpixels[k] & 0xFF) > Floyd16x16[(x & 0xF)][(y & 0xF)])
/* 249 */           despixels[k] = -1;
/*     */         else {
/* 251 */           despixels[k] = -16777216;
/*     */         }
/* 253 */         k++;
/*     */       }
/*     */   }
/*     */ 
/*     */   private static void format_K_dither8x8_int(int[] orgpixels, int xsize, int ysize, int[] despixels)
/*     */   {
/* 260 */     int k = 0;
/* 261 */     for (int y = 0; y < ysize; y++)
/* 262 */       for (int x = 0; x < xsize; x++) {
/* 263 */         if ((orgpixels[k] & 0xFF) >> 2 > Floyd8x8[(x & 0x7)][(y & 0x7)])
/* 264 */           despixels[k] = -1;
/*     */         else {
/* 266 */           despixels[k] = -16777216;
/*     */         }
/* 268 */         k++;
/*     */       }
/*     */   }
/*     */ 
/*     */   public static int[] bitmapToBWPix_int(Bitmap mBitmap, int algorithm)
/*     */   {
/* 274 */     int[] pixels = new int[0];
				Bitmap grayBitmap; 
/* 275 */     switch (algorithm) {
/*     */     case 8:
/* 277 */       grayBitmap = toGrayscale(mBitmap);
/* 278 */       pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
/* 279 */       grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0, 
/* 280 */         grayBitmap.getWidth(), grayBitmap.getHeight());
/* 281 */       format_K_dither8x8_int(pixels, grayBitmap.getWidth(), 
/* 282 */         grayBitmap.getHeight(), pixels);
/* 283 */       break;
/*     */     case 2:
/* 285 */       break;
/*     */     case 16:
/*     */     default:
/* 288 */       grayBitmap = toGrayscale(mBitmap);
/* 289 */       pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
/* 290 */       grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0, 
/* 291 */         grayBitmap.getWidth(), grayBitmap.getHeight());
/* 292 */       format_K_dither16x16_int(pixels, grayBitmap.getWidth(), 
/* 293 */         grayBitmap.getHeight(), pixels);
/*     */     }
/*     */ 
/* 296 */     return pixels;
/*     */   }
/*     */ 
/*     */   public static Bitmap toBinaryImage(Bitmap mBitmap, int nWidth, int algorithm) {
/* 300 */     int width = (nWidth + 7) / 8 * 8;
/* 301 */     int height = mBitmap.getHeight() * width / mBitmap.getWidth();
/* 302 */     Bitmap rszBitmap = resizeImage(mBitmap, width, height);
/*     */ 
/* 304 */     int[] pixels = bitmapToBWPix_int(rszBitmap, algorithm);
/* 305 */     rszBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
/*     */ 
/* 307 */     return rszBitmap;
/*     */   }
/*     */ }

/* Location:           C:\Users\Administrator\Desktop\反编译\jd-gui-0.3.3.windows\gprinter-2.1.2.jar
 * Qualified Name:     com.gprinter.command.GpUtils
 * JD-Core Version:    0.6.0
 */