package com.meitu.glcamera.widgetUtils;

import java.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

	private int imgSize;
	private int height;
	private int width;
	private String imgPath;
	private long millSecond;
	private Calendar takeTime;// 年月日时分秒

	public long getMillSecond() {
		return millSecond;
	}

	public void setMillSecond(long millSecond) {
		this.millSecond = millSecond;
	}

	public Image() {
	}

	public Image(Parcel source) {
		imgSize = source.readInt();
		height = source.readInt();
		width = source.readInt();
		imgPath = source.readString();
		millSecond = source.readLong();
		int year = source.readInt();
		int month = source.readInt();
		int day = source.readInt();
		int hour = source.readInt();
		int minute = source.readInt();
		int second = source.readInt();
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute, second);
		takeTime = calendar;
	}

	public int getImgSize() {
		return imgSize;
	}

	public void setImgSize(int imgSize) {
		this.imgSize = imgSize;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Calendar getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(Calendar takeTime) {
		this.takeTime = takeTime;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(imgSize);
		dest.writeInt(height);
		dest.writeInt(width);
		dest.writeString(imgPath);
		dest.writeLong(millSecond);
		dest.writeInt(takeTime.get(Calendar.YEAR));
		dest.writeInt(takeTime.get(Calendar.MONTH) + 1);
		dest.writeInt(takeTime.get(Calendar.DAY_OF_MONTH));
		dest.writeInt(takeTime.get(Calendar.HOUR_OF_DAY));
		dest.writeInt(takeTime.get(Calendar.MINUTE));
		dest.writeInt(takeTime.get(Calendar.SECOND));
	}

	public static Creator<Image> CREATOR = new Creator<Image>() {
		@Override
		public Image createFromParcel(Parcel source) {
			return new Image(source);
		}

		@Override
		public Image[] newArray(int size) {
			return new Image[size];
		}
	};

}
