package com.meitu.glcamera.widgets;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

import com.meitu.glcamera.widgetUtils.CameraParameterUtils;

/**
 * Created by mtdiannao on 2015/4/22.
 */
@SuppressLint("WrongCall")
@SuppressWarnings("deprecation")
public class CameraGLSurfaceView extends GLSurfaceView {

	private final String TAG = "CameraGLSurfaceView";

	private Context ctx;
	private int cameraNum;
	private int curCameraID;

	private Camera mCamera;
	private Camera.Parameters mParam;
	private SurfaceHolder mSurfaceHolder;

	private GPUImageRenderer mRender;

	private boolean isPreviewing = false;
	private boolean flashOnState = false;
	
	private GPUImage mGPUImage;
	private GPUImageFilter mFilter;
	
	public GPUImage getGPUImage(){
		return mGPUImage;
	}

	public CameraGLSurfaceView(Context context) {
		super(context);
		initialData(context);
	}

	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialData(context);
	}

	private void initialData(Context context) {
		this.ctx = context;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mFilter = new GPUImageFilter();
		mRender = new GPUImageRenderer(mFilter);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			 openCamera();
			 initialParam();
			 previewSurface();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "open Camera wrong..");
		}
	}
	
	public Camera getCamer(){
		return mCamera;
	}

	private void previewSurface() throws Exception {
		mCamera.setPreviewDisplay(mSurfaceHolder);
		mCamera.startPreview();
		isPreviewing = true;
		
		mGPUImage = new GPUImage(ctx);
		mGPUImage.setGLSurfaceView(this);
		mFilter = new GPUImageHueFilter();
		mGPUImage.setFilter(mFilter);
		mGPUImage.setUpCamera(mCamera, 90, false, false);
	}

	@SuppressLint("NewApi")
	private void openCamera() {
		if (Integer.parseInt(Build.VERSION.SDK) >= 9) {
			cameraNum = Camera.getNumberOfCameras();
			Camera.CameraInfo info = null;
			for (int i = 0; i < cameraNum; i++) {
				info = new Camera.CameraInfo();
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					curCameraID = i;
				}
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		releaseSources();
	}

	private void initialParam() {
		try {
			mCamera = Camera.open();
			mParam = mCamera.getParameters();
			if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
				showPreviewPosition(mCamera, 90);
			} else {
				mParam.setRotation(90);
			}
			float r = 4.0f / 3.0f;
			DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
			int mWid = dm.widthPixels;
			List<Camera.Size> picList = mParam.getSupportedPictureSizes();
			List<Camera.Size> preList = mParam.getSupportedPreviewSizes();
			Camera.Size piSize = CameraParameterUtils.getPropPictureSize(picList, r, mWid);
			Camera.Size prSize = CameraParameterUtils.getPropPreviewSize(preList, r, mWid);
			mParam.setPictureFormat(ImageFormat.JPEG);
			mParam.setPictureSize(piSize.width, piSize.height);
			mParam.setPreviewSize(prSize.width, prSize.height);
			if (!flashOnState) {
				mParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			} else {
				mParam.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			}
			mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mParam.setJpegQuality(100);
			mCamera.setParameters(mParam);

		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "Open Camera Failure....");
		}
	}

	private void showPreviewPosition(Camera camera, int i) {
		Method showMethod;
		try {
			showMethod = camera.getClass().getMethod("setDisplayOrientation",
					new Class[] { int.class });
			if (showMethod != null) {
				showMethod.invoke(camera, new Object[] { i });
			}
		} catch (Exception e) {
			Log.i(TAG, "set preview wrong");
		}
	}

	private void releaseSources() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

//	private final class GLRender implements GLSurfaceView.Renderer {
//		@Override
//		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
//			gl.glShadeModel(GL10.GL_SMOOTH);//
//			gl.glClearDepthf(1.0f);//
//			gl.glEnable(GL10.GL_DEPTH_TEST);//
//			gl.glDepthFunc(GL10.GL_LEQUAL);//
//			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);//
//			Log.i(TAG, "*****onSurfaceCreated*****");
//		}
//
//		@Override
//		public void onSurfaceChanged(GL10 gl, int width, int height) {
//			Log.i(TAG, "*****onSurfaceChanged*****");
//			gl.glViewport(0, 0, width, height);
//			gl.glMatrixMode(GL10.GL_PROJECTION);//
//			gl.glLoadIdentity();//
//			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
//					100.0f);//
//			gl.glMatrixMode(GL10.GL_MODELVIEW);//
//			gl.glLoadIdentity();//
//		}
//
//		@Override
//		public void onDrawFrame(GL10 gl) {
//			gl.glClearColor(mRed, mGreen, mBlue, 1.0f);
//			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
//			Log.i(TAG, "*****onDrawFrame*****");
//		}
//
//		public void setColor(float r, float g, float b) {
//			mRed = r;
//			mGreen = g;
//			mBlue = b;
//		}
//
//		private float mRed;
//		private float mGreen;
//		private float mBlue;
//	}
}