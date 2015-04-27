package com.meitu.glcamera.widgetUtils;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.DisplayMetrics;

import com.meitu.glcamera.ui.MainGLSurfaceViewActivity;
import com.meitu.glcamera.widgetUtils.CameraHelper.CameraInfo2;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressWarnings("deprecation")
public class CameraLoader {

	private Context context;
	private GPUImage mGPUImage;
	private CameraHelper mCameraHelper;
	private int mCurrentCameraId = 0;
	private Camera mCameraInstance;

	public CameraLoader(Context context, GPUImage mGPUImage,
			CameraHelper mCameraHelper) {
		this.context = context;
		this.mGPUImage = mGPUImage;
		this.mCameraHelper = mCameraHelper;
	}

	public void onResume() {
		setUpCamera(mCurrentCameraId);
	}

	public void onPause() {
		releaseCamera();
	}

	public Camera getCamera() {
		return mCameraInstance;
	}

	public void switchCamera() {
		releaseCamera();
		mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
		setUpCamera(mCurrentCameraId);
	}

	private void setUpCamera(final int id) {
		mCameraInstance = getCameraInstance(id);
		Parameters pm = mCameraInstance.getParameters();
		// TODO adjust by getting supportedPreviewSizes and then choosing
		// the best one for screen size (best fill screen)
		float r = 4.0f / 3.0f;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int mWid = dm.widthPixels;
		List<Size> picList = pm.getSupportedPictureSizes();
		List<Size> preList = pm.getSupportedPreviewSizes();
		Size piSize = CameraParameterUtils.getPropPictureSize(picList, r, mWid);
		Size prSize = CameraParameterUtils.getPropPreviewSize(preList, r, mWid);
		pm.setPictureFormat(ImageFormat.JPEG);
		pm.setPictureSize(piSize.width, piSize.height);
//		pm.setPreviewSize(prSize.width, prSize.height);
		pm.setPreviewSize(720,480);
		if (pm.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			pm.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		mCameraInstance.setParameters(pm);
		int orientation = mCameraHelper.getCameraDisplayOrientation((MainGLSurfaceViewActivity) context, mCurrentCameraId);// ...????????????
		CameraInfo2 cameraInfo = new CameraInfo2();
		mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
		boolean f = false;
		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
			f = true;
		}
		mGPUImage.setUpCamera(mCameraInstance, orientation, f, false);
	}

	/** A safe way to get an instance of the Camera object. */

	private Camera getCameraInstance(final int id) {
		Camera c = null;
		try {
			c = mCameraHelper.openCamera(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	private void releaseCamera() {
		mCameraInstance.setPreviewCallback(null);
		mCameraInstance.release();
		mCameraInstance = null;
	}
}