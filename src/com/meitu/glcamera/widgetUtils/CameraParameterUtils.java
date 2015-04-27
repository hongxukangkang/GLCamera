package com.meitu.glcamera.widgetUtils;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraParameterUtils {

	private final static String TAG = "CameraParameterUtils";
	private static CameraSizeComparator sizeComparator = new CameraSizeComparator();

	private static final int RB = 1000;
	private static final int LT = -1000;

	public static Size getCameraPictureSize(Parameters params) {
		return params.getPictureSize();
	}

	public static Size getCameraPreviewSize(Parameters params) {
		return params.getPreviewSize();
	}

	public static void printCameraFocuMode(Parameters params) {
		Log.i(TAG, "==========+++support focusmode:" + params.getFocusMode());
		List<String> focusModes = params.getSupportedFocusModes();
		for (String str : focusModes) {
			Log.i(TAG, "==========+++support focusmode:" + str);
		}
	}

	public static void printCameraPictureSize(Parameters params) {
		List<Size> supportSizes = params.getSupportedPictureSizes();
		for (Size s : supportSizes) {
			Log.i(TAG, "**Picture+++support height:" + s.height
					+ "--->support width:" + s.width);
		}
	}

	public static void printCameraPreviewSize(Parameters params) {
		List<Size> supportSizes = params.getSupportedPreviewSizes();
		for (Size s : supportSizes) {
			Log.i(TAG, "===Preview+++support height:" + s.height
					+ "--->support width:" + s.width);
		}
	}

	public static float getScreenRate(Context context) {
		Point P = getScreenMetrics(context);
		float H = P.y;
		float W = P.x;
		return (H / W);
	}

	private static Point getScreenMetrics(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		Log.i(TAG, "Screen---Width = " + w_screen + " Height = " + h_screen
				+ " densityDpi = " + dm.densityDpi);
		return new Point(w_screen, h_screen);
	}

	public static Size getPropPictureSize(List<Size> list, float rate,
			int minWidth) {
		Collections.sort(list, sizeComparator);
		int i = 0;

		for (Size s : list) {

			if ((s.height == minWidth) && equalRate(s, rate)) {
				Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}

		if (i == list.size()) {
			i = 0;
			for (Size s : list) {
				if ((s.height > minWidth) && equalRate(s, rate)) {
					Log.i(TAG, "PictureSize : w = " + s.width + "h = "
							+ s.height);
					break;
				}
				i++;
			}
		}

		if (i == list.size()) {
			i = 0;
			for (Size s : list) {
				if ((s.height < minWidth) && equalRate(s, rate)) {
					Log.i(TAG, "PictureSize : w = " + s.width + "h = "
							+ s.height);
					break;
				}
				i++;
			}
		}

		if (i == list.size()) {
			i = 0;
		}
		return list.get(i);
	}

	private static boolean equalRate(Size s, float rate) {
		float r = (float) (s.width) / (float) (s.height);
		if (Math.abs(r - rate) <= 0.03) {
			return true;
		} else {
			return false;
		}
	}

	private static class CameraSizeComparator implements
			Comparator<Size> {
		// public CameraSizeComparator(){}
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public static Size getPropPreviewSize(List<Size> list, float rate,
			int minWidth) {
		Collections.sort(list, sizeComparator);

		int i = 0;
		for (Size s : list) {
			if ((s.width >= minWidth) && equalRate(s, rate)) {
				Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}

		List<Size> backUpSizes = new ArrayList<Size>();
		for (Size s : list) {
			if (equalRate(s, rate)) {
				backUpSizes.add(s);
			}
			i++;
		}

		Size tempSize = null;
		if (backUpSizes.size() > 0) {
			for (Size s : backUpSizes) {
				if ((s.width >= minWidth)) {
					tempSize = s;
					return tempSize;
				}
			}
		}

		if (tempSize == null && (backUpSizes.size() > 0)) {
			return findBestPreviewSizeForPhone(backUpSizes, minWidth);
		}

		if (i == list.size()) {
			i = 0;
			for (Size s : list) {
				if (equalRate(s, rate)) {
					break;
				}
				i++;
			}
		}

		if (i == list.size()) {
			i = 0;
		}

		return list.get(i);
	}

	// find a size which is closest to the phone width
	private static Size findBestPreviewSizeForPhone(List<Size> backUpSizes,
			int width) {
		for (Size s : backUpSizes) {
			if (s.width >= width) {
				return s;
			}
		}
		return backUpSizes.get(backUpSizes.size() - 1);
	}


	/**
	 * 璁＄畻鐒︾偣鍙婃祴鍏夊尯鍩�
	 * @param areaMultiple
	 * @param x
	 * @param y
	 *            Rect(left,top,right,bottom) :
	 *            left銆乼op銆乺ight銆乥ottom鏄互鏄剧ず鍖哄煙涓績涓哄師鐐圭殑鍧愭爣
	 */
	public Rect calculateTapArea(int focusW, int focusH, float areaMultiple,
			float x, float y, int pLeft, int pRight, int pTop, int pBottom) {
		int areaW = (int) (focusW * areaMultiple);
		int areaH = (int) (focusH * areaMultiple);
		int centerX = (pLeft + pRight) / 2;
		int centerY = (pTop + pBottom) / 2;
		double tx = ((double) pRight - (double) pLeft) / 2000;
		double ty = ((double) pBottom - (double) pTop) / 2000;
		int left = clamp((int) (((x - areaW / 2) - centerX) / tx), LT, RB);
		int top = clamp((int) (((y - areaH / 2) - centerY) / ty), LT, RB);
		int right = clamp((int) (left + areaW / tx), LT, RB);
		int bottom = clamp((int) (top + areaH / ty), LT, RB);
		return new Rect(left, top, right, bottom);
	}

	public int clamp(int x, int min, int max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}
}
