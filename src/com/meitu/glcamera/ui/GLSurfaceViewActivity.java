package com.meitu.glcamera.ui;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

import com.meitu.glcamera.R;
import com.meitu.glcamera.widgetUtils.CameraHelper;
import com.meitu.glcamera.widgetUtils.CameraHelper.CameraInfo2;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

@SuppressWarnings("deprecation")
public class GLSurfaceViewActivity extends Activity implements View.OnClickListener {

	private final String TAG = "TakePictureActivity";

	private Button btn_Gray;
	private Button btn_Gold;
	private Button btn_Silver;
	private Button btn_Specia;
	private Button btn_Formal;
	private Button btn_Invert;
	// for UI Layout
	private View rootview;
	private GLSurfaceView mGLSurfaceview;
	private LinearLayout filterContainer;
	private boolean isLayoutSet = false;
	private PhoneOnGlobalLayoutListener uiLayoutListener;

	private Button btn_mode;

	// for filters
	private GPUImage mGPUImage;
	private GPUImageFilter mFilter;
	private CameraHelper mCameraHelper;
	private CameraLoader mCamera;
//	private FilterAdjuster mFilterAdjuster;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);

		findViews();
		setListeners();

		initialGPUImage();

	}

	private void initialGPUImage() {
		mGPUImage = new GPUImage(this);
		mGPUImage.setGLSurfaceView(mGLSurfaceview);
		mCameraHelper = new CameraHelper(this);
		mCamera = new CameraLoader();

		if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
			btn_mode.setVisibility(View.GONE);
		}

	}

	private void setListeners() {
		rootview.getViewTreeObserver().addOnGlobalLayoutListener(uiLayoutListener);
	}

	private void findViews() {

		btn_Gray = (Button) findViewById(R.id.grayFilter);
		btn_Gold = (Button) findViewById(R.id.hueFilter);
		btn_Silver = (Button) findViewById(R.id.gammaFilter);
		btn_Specia = (Button) findViewById(R.id.speciaFilter);
		btn_Formal = (Button) findViewById(R.id.formalFilter);
		btn_Invert = (Button) findViewById(R.id.invertFilter);

		btn_mode = (Button) findViewById(R.id.btn_mode);
		rootview = findViewById(R.id.rootView);
		uiLayoutListener = new PhoneOnGlobalLayoutListener();
		filterContainer = (LinearLayout) findViewById(R.id.filterContainer);
		mGLSurfaceview = (GLSurfaceView) findViewById(R.id.glCameraSurfaceView);

		btn_Gray.setOnClickListener(this);
		btn_Gold.setOnClickListener(this);
		btn_Silver.setOnClickListener(this);
		btn_Specia.setOnClickListener(this);
		btn_Formal.setOnClickListener(this);
		btn_Invert.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.grayFilter:
			mFilter = new GPUImageGrayscaleFilter();
			break;
		case R.id.hueFilter:
			mFilter = new GPUImageHueFilter();
			break;
		case R.id.gammaFilter:
			mFilter = new GPUImageGammaFilter();
			break;
		case R.id.speciaFilter:
			mFilter = new GPUImageSepiaFilter();
			break;
		case R.id.formalFilter:
			mFilter = new GPUImageFilter();
			break;
		case R.id.invertFilter:
			mFilter = new GPUImageColorInvertFilter();
			break;
		}
		mGPUImage.setFilter(mFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		mGLSurfaceview.onResume();
		mCamera.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCamera.onPause();
	}

	private final class PhoneOnGlobalLayoutListener implements
			ViewTreeObserver.OnGlobalLayoutListener {
		@Override
		public void onGlobalLayout() {
			if (!isLayoutSet) {

				Log.i(TAG, "====onGlobalLayout====");

				DisplayMetrics dm = getResources().getDisplayMetrics();
				int screenWidth = dm.widthPixels;
				int screenHeight = dm.heightPixels;

				// for GLSurfaceView
				ViewGroup.LayoutParams pl = mGLSurfaceview.getLayoutParams();
				int glHeight = screenWidth * 4 / 3;
				pl.width = screenWidth;
				pl.height = glHeight;
				mGLSurfaceview.setLayoutParams(pl);

				// for filters
				ViewGroup.LayoutParams flp = filterContainer.getLayoutParams();
				flp.width = screenWidth;
				flp.height = screenHeight - glHeight;
				filterContainer.setLayoutParams(flp);

				isLayoutSet = true;
			}
		}
	}

	private class CameraLoader {
		private int mCurrentCameraId = 0;
		private Camera mCameraInstance;

		public void onResume() {
			setUpCamera(mCurrentCameraId);
		}

		public void onPause() {
			releaseCamera();
		}
		
		public Camera getCamera(){
			return mCameraInstance;
		}

		public void switchCamera() {
			releaseCamera();
			mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
			setUpCamera(mCurrentCameraId);
		}

		private void setUpCamera(final int id) {
			mCameraInstance = getCameraInstance(id);
			Parameters parameters = mCameraInstance.getParameters();
			// TODO adjust by getting supportedPreviewSizes and then choosing
			// the best one for screen size (best fill screen)
			parameters.setPreviewSize(720, 480);
			if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			mCameraInstance.setParameters(parameters);
			int orientation = mCameraHelper.getCameraDisplayOrientation(GLSurfaceViewActivity.this, mCurrentCameraId);
			CameraInfo2 cameraInfo = new CameraInfo2();
			mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
			boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
			mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
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
}
