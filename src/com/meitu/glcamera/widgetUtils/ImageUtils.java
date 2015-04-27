package com.meitu.glcamera.widgetUtils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("SimpleDateFormat")
public class ImageUtils {
	
	private ImageUtils(){}
	private static ImageUtils instance = new ImageUtils();
	
	public static ImageUtils getInstance(){
		return instance;
	}
	
	public  Calendar getCalendarTime(String millSeconds) {
		long timeMillis = Long.valueOf(millSeconds);
		Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(timeMillis);
	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    format.format(calendar.getTime());
	    return calendar;
	}
}
