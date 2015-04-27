package com.meitu.glcamera.widgetUtils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class ImageFileUtils {

	private static final String TAG = "ImageFileUtils";

	@SuppressLint("SdCardPath")
	public static void saveImgFileToSDcard(byte[] data) throws IOException {
		Log.i(TAG, "*****onPictureTaken*****");
		String imgNm = getImgName();
		String sdPath = SDCardUtils.getSDCardPath();
		String path = sdPath + "MTCamera";
		File fm = new File(path);
		if (!fm.exists() && !fm.isDirectory()) {
			fm.mkdir();
		}
		String filePath = path + File.separator + "meitu_" + imgNm;
		File file = new File(filePath);
		Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(file));
		// Bitmap mBitmap = Bitmap.createScaledBitmap(bm, 1080, 800, false);
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}

	private static String getImgName() {
		Calendar now = Calendar.getInstance();
		String year = "" + now.get(Calendar.YEAR);
		String month = "" + (now.get(Calendar.MONTH) + 1);
		String day = "" + now.get(Calendar.DAY_OF_MONTH);
		String millSds = "" + now.getTimeInMillis();
		String imgNm = year + "_" + month + "_" + day + "_" + millSds + ".jpg";
		return imgNm;
	}

	public static void saveImgFileToSDcardRotate(byte[] data, int rotation)
			throws IOException {
		String imgNm = getImgName();
		String sdPath = SDCardUtils.getSDCardPath();
		String path = sdPath + "MTCamera";
		File fm = new File(path);
		if (!fm.exists() && !fm.isDirectory()) {
			fm.mkdir();
		}
		String filePath = path + File.separator + "meitu_" + imgNm;
		File file = new File(filePath);
		Matrix matrix = new Matrix();
		matrix.setRotate(rotation);
		FileOutputStream out = new FileOutputStream(file);
		Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0,
				oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] newData = baos.toByteArray();
		out.write(newData);
		out.close();
	}
}