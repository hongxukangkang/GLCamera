package com.meitu.glcamera.ui;

import static android.os.Build.VERSION.SDK_INT;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

import com.meitu.glcamera.R;
import com.meitu.glcamera.uiUtils.MainGLSurfaceViewActivityUtils;
import com.meitu.glcamera.widgetUtils.CameraHelper;
import com.meitu.glcamera.widgetUtils.CameraLoader;
import com.meitu.glcamera.widgetUtils.SnapshotSound;
import com.meitu.glcamera.widgets.SquareView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@SuppressLint({ "InflateParams", "RtlHardcoded", "ClickableViewAccessibility" })
public class MainGLSurfaceViewActivity extends Activity implements OnClickListener {
	
	private final String TAG = "MainGLSurfaceViewActivity";
	private Context context;
	// For UI layout
	private int screenWidth ;
	private int screenHeght;
	private View root;
	private SquareView mSquareView;
	private LinearLayout inflateView;
	private RelativeLayout headerView;
	// private RelativeLayout bottomView;
	private LinearLayout headerContainer;
	private LinearLayout bottomContainer;
	private GLSurfaceView mGLSurfaceView;
	private RelativeLayout inflateViewContainer;
	private RelativeLayout glSurfaceViewContainer;
	private PhoneOnGlobalLayoutListener layoutListener;
	private boolean hasLayout = false;
	private boolean isSquareShowing = false;

	// for Filters
	private GPUImage mGPUImage;
	private GPUImageFilter mFilter;
	private CameraHelper mCameraHelper;
	private CameraLoader mCameraLoader;
	private LinearLayout filterViewContainer;

	private Camera mCamera;
	private JPEGPicture jpeg;
	
	// for exit
	private static boolean isExit = false;
	private static boolean hasTask = false;

	// for click relation to camera
	private boolean isPreviewing = true;
	private TextView tv_scale;
	private ImageView ibv_takePic;
	private ImageView ibv_squares;
	private ImageView ibv_albumScan;
	
	private ImageView iv_focus;
    private ImageView iv_metering;
    private Animation focusAnimation;
    private Animation meteringAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_glsurfaceview_activity);

		calculateWidthAndHeight();
		findViewsAndintialParam();
		setOnClickListenerForWidgets();
		openCameraAndInitialGPUImage();
		
	}

	private void calculateWidthAndHeight() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeght = dm.heightPixels;
	}

	private void setOnClickListenerForWidgets() {
		tv_scale.setOnClickListener(this);
		ibv_squares.setOnClickListener(this);
		ibv_takePic.setOnClickListener(this);
		ibv_albumScan.setOnClickListener(this);
	}

	private void openCameraAndInitialGPUImage() {
		
		mGPUImage = new GPUImage(this);
		mGPUImage.setGLSurfaceView(mGLSurfaceView);
		mCameraHelper = new CameraHelper(this);
		mCameraLoader = new CameraLoader(this, mGPUImage, mCameraHelper);

	}

	private void findViewsAndintialParam() {
		context = this;
		// for UI
		root = findViewById(R.id.rootView);
		inflateView = (LinearLayout) findViewById(R.id.inflateView);
		headerView = (RelativeLayout) findViewById(R.id.header_view);
		// bottomView = (RelativeLayout) findViewById(R.id.bottom_view);
		headerContainer = (LinearLayout) findViewById(R.id.headerContainer);
		bottomContainer = (LinearLayout) findViewById(R.id.bottomContainer);
		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.preview_glsurfaceview);
		inflateViewContainer = (RelativeLayout) findViewById(R.id.inflateView_container);
		glSurfaceViewContainer = (RelativeLayout) findViewById(R.id.surfaceView_container);

		// for click event
		tv_scale = (TextView) findViewById(R.id.scale_measure);
		ibv_albumScan = (ImageView) findViewById(R.id.pic_scan);
		ibv_squares = (ImageView) findViewById(R.id.squares_set);
		ibv_takePic = (ImageView) findViewById(R.id.ibv_take_pic);

		touchListener = new GLSurfaceViewOnTouchListener();
		layoutListener = new PhoneOnGlobalLayoutListener();
		glSurfaceViewContainer.setOnTouchListener(touchListener);
		root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		
		mSquareView = (SquareView) findViewById(R.id.squarview);
		
		focusAnimation = AnimationUtils.loadAnimation(this, R.anim.focus_animation);
        meteringAnimation = AnimationUtils.loadAnimation(this, R.anim.metering_animation);
        iv_focus = new ImageView(this);
        iv_metering = new ImageView(this);
        iv_focus.setImageResource(R.drawable.camera_focus);
        iv_metering.setImageResource(R.drawable.camera_metering);
        glSurfaceViewContainer.addView(iv_focus);
        glSurfaceViewContainer.addView(iv_metering);
        iv_focus.setVisibility(View.INVISIBLE);
        iv_metering.setVisibility(View.INVISIBLE);
	}

	private final class PhoneOnGlobalLayoutListener implements OnGlobalLayoutListener {
		@Override
		public void onGlobalLayout() {
			resetLayoutView();
		}
	}

	private void resetLayoutView() {
		if (!hasLayout) {
			hasLayout = true;
			DisplayMetrics dm = getResources().getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			// for headerView
			LayoutParams hvlp = headerView.getLayoutParams();
			hvlp.width = screenWidth / 2;
			headerView.setLayoutParams(hvlp);
			// for CameraGLSurfaceView
			int mediaHeight = screenWidth * 4 / 3;
			LayoutParams glp = mGLSurfaceView.getLayoutParams();
			glp.width = screenWidth;
			glp.height = mediaHeight;
			mGLSurfaceView.setLayoutParams(glp);
			// for HeaderContainer
			int tempHeight = screenHeight - mediaHeight;
			LayoutParams hlp = headerContainer.getLayoutParams();
			hlp.width = screenWidth;
			hlp.height = tempHeight / 3;
			headerContainer.setLayoutParams(hlp);
			if (SDK_INT >= 14) {
				headerContainer.setGravity(Gravity.CENTER | Gravity.END);
			} else {
				headerContainer.setGravity(Gravity.CENTER | Gravity.RIGHT);
			}
			// for BottomContainer
			LayoutParams blp = bottomContainer.getLayoutParams();
			blp.width = screenWidth;
			blp.height = tempHeight * 2 / 3;
			bottomContainer.setLayoutParams(blp);
			bottomContainer.setGravity(Gravity.CENTER);

			int mediaHei = mediaHeight - screenWidth;
			LayoutParams rParam = inflateViewContainer.getLayoutParams();
			rParam.width = screenWidth;
			rParam.height = mediaHei;
			inflateViewContainer.setLayoutParams(rParam);
			inflateViewContainer.setGravity(Gravity.CENTER);

			filterViewContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.filterview_container, null);
			filterViewContainer.setVerticalScrollBarEnabled(false);
			inflateViewContainer.addView(filterViewContainer);

			LayoutParams lParam = filterViewContainer.getLayoutParams();
			lParam.height = mediaHeight;
			lParam.width = screenWidth;
			filterViewContainer.setLayoutParams(lParam);

			LayoutParams flp = inflateView.getLayoutParams();
			flp.width = screenWidth;
			flp.height = mediaHeight - screenWidth;
			inflateView.setLayoutParams(flp);
			inflateView.setVisibility(View.INVISIBLE);
			
			MainGLSurfaceViewActivityUtils.setTitleForFilterView(this, this,filterViewContainer, screenWidth);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}

	private void exit() {
		if (isExit == false) {
			isExit = true;
			showToast(R.string.take_picture_exit);
			if (!hasTask) {
				tExit.schedule(task, 2 * 1000);
			}
		} else {
			finish();
			System.exit(0);
		}
	}

	Timer tExit = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			isExit = false;
			hasTask = true;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mCameraLoader.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCameraLoader.onPause();
	}

	private void showToast(int id) {
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}

	private void showToastContent(String contennt) {
		Toast.makeText(this, contennt, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scale_measure:
			displayOrDismissInflateView();
			break;
		case R.id.pic_scan:
			gotoAlbumPreviewActivity();
			break;
		case R.id.squares_set:
			setSquarsDisplayOrDismiss();
			break;
		case R.id.ibv_take_pic:
			playSoundAndTakePictureAction();
			break;
		case R.id.iv_original:
			orginalAction();
			break;
		case R.id.iv_Gold:
			goldAction();
			break;
		case R.id.iv_Vista:
			vistaAction();
			break;
		case R.id.iv_Iceland:
			iceLandAction();
			break;
		case R.id.iv_Solaris:
			solarisAction();
			break;
		case R.id.iv_690:
			iv690Action();
			break;
		case R.id.iv_across:
			ivAcrossAction();
			break;
		}
	}

	private void setSquarsDisplayOrDismiss() {
		if (isSquareShowing) {
			isSquareShowing = false;
			dimissSquare();
		}else {
			isSquareShowing = true;
			String scale = tv_scale.getText().toString();
			int width = glSurfaceViewContainer.getWidth();
			if (scale.contains("4:3")) {
				int height = glSurfaceViewContainer.getHeight();
				disPlaySquare(width, height);
			}else {
				int height = glSurfaceViewContainer.getHeight();
				int tempHt = inflateViewContainer.getHeight();
				disPlaySquare(width, height - tempHt);
			}
		}
		if (hasTouched) {
			
		}
	}
	
	private void dimissSquare(){
		mSquareView.setVisibility(View.INVISIBLE);
		ibv_squares.setBackgroundResource(R.drawable.square_dismiss_selector);
	}
	
	private void disPlaySquare(int width,int height){
        mSquareView.setScreenWidth(width);
        mSquareView.setScreenHeight(height);
        mSquareView.setVisibility(View.VISIBLE);
//        glSurfaceViewContainer.addView(mSquareView);
        ibv_squares.setBackgroundResource(R.drawable.square_display_selector);
	}

	private final int REQUESTCODE = 0x0010;
	private void gotoAlbumPreviewActivity() {
		Intent intent = new Intent();
		intent.setClass(context, AlbumPreviewActivity.class);
		startActivityForResult(intent, REQUESTCODE);
	}
	
	private void playSoundAndTakePictureAction() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				new SnapshotSound(context).playSound();
			}
		}).start();
		jpeg = new JPEGPicture();
		mCamera = mCameraLoader.getCamera();
		mCamera.takePicture(null, null, jpeg);
	}

	private boolean inflateViewShow = false;

	private void displayOrDismissInflateView() {
		if (inflateViewShow) {
			inflateViewShow = false;
			tv_scale.setText("4:3");
			inflateView.setVisibility(View.INVISIBLE);
			if (isSquareShowing) {
				mSquareView.setVisibility(View.INVISIBLE);
				int width = glSurfaceViewContainer.getWidth();
				int height = glSurfaceViewContainer.getHeight();
				disPlaySquare(width, height);
			}
		} else {
			inflateViewShow = true;
			tv_scale.setText("1:1");
			inflateView.setVisibility(View.VISIBLE);
			if (isSquareShowing) {
				mSquareView.setVisibility(View.INVISIBLE);
				int width = glSurfaceViewContainer.getWidth();
				int height = glSurfaceViewContainer.getHeight();
				int tempHt = inflateViewContainer.getHeight();
				disPlaySquare(width, height - tempHt);
			}
		}
	}

	private void ivAcrossAction() {
		mFilter = new GPUImageGammaFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void iv690Action() {
		mFilter = new GPUImageColorInvertFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void solarisAction() {
		mFilter = new GPUImageSepiaFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void iceLandAction() {
		mFilter = new GPUImageGammaFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void vistaAction() {
		mFilter = new GPUImageGrayscaleFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void goldAction() {
		mFilter = new GPUImageHueFilter();
		mGPUImage.setFilter(mFilter);
	}

	private void orginalAction() {
		mFilter = new GPUImageFilter();
		mGPUImage.setFilter(mFilter);
	}
	
	private final class JPEGPicture implements PictureCallback{
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			//保存图像
			isPreviewing = false;
			//imgImpl.setScale(tv_scale.getText().toString());
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			long curTime = System.currentTimeMillis();
			
			mGPUImage.saveToPictures(bm, "GLCamera", "" + curTime + ".jpg", new OnPictureSavedListener() {
				@Override
				public void onPictureSaved(Uri arg0) {
					try {
						mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
						mCamera.startPreview();
						isPreviewing = true;
					} catch (Exception e) {
						Log.i(TAG, "=====savePicture wrong===");
						isPreviewing = false;
					}
				}
			});
		}
	}
	
	private int lastX;
	private int lastY;
	
	private boolean hasTouched = false;
	
	private GLSurfaceViewOnTouchListener touchListener;
	private final class GLSurfaceViewOnTouchListener implements OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
//				showToastContent("danji");
				
				recordCurPost(event);
				startAnimationAction(event);
				break;
			case MotionEvent.ACTION_MOVE:
				moveAction(v,event);
				break;
			}
			return true;
		}
	}
	
	private void startAnimationAction(MotionEvent event) {
        int width = iv_focus.getWidth();
        int height = iv_focus.getHeight();
        int left = (int) event.getX() - width / 2;
        int top = (int) event.getY() - height / 2;
        int right = (int) event.getX() + width / 2;
        int bottom = (int) event.getY() + height / 2;
        iv_focus.layout(left, top, right, bottom);
        iv_metering.layout(left, top, right, bottom);
        iv_focus.startAnimation(focusAnimation);
        iv_metering.startAnimation(meteringAnimation);
    }

	public void recordCurPost(MotionEvent event) {
		
		hasTouched = true;
		lastX = (int)event.getRawX();
		lastY = (int)event.getRawY();
		borderHeight = mGLSurfaceView.getHeight();
			
	}

	private int borderHeight;
	
	public void moveAction(View v, MotionEvent event) {
		int deltaX = (int) event.getRawX() - lastX;
		int deltaY = (int) event.getRawY() - lastY;
		int left = iv_metering.getLeft() + deltaX;
		int top = iv_metering.getTop() + deltaY;
		int right = iv_metering.getRight() + deltaX;
		int bottom = iv_metering.getBottom() + deltaY;
		// 设置不能出界
		if (left < 0) {
			left = 0;
			right = left + iv_metering.getWidth();
		}
		if (right > screenWidth) {
			right = screenWidth;
			left = right - iv_metering.getWidth();
		}
		if (top < 0) {
			top = 0;
			bottom = top + iv_metering.getHeight();
		}
		
		String scale = tv_scale.getText().toString();
		if (scale.contains("4:3")) {
			if (bottom > borderHeight) {
				bottom = borderHeight;
				top = bottom - iv_metering.getHeight();
			}
		}else {
			int height = borderHeight - inflateViewContainer.getHeight();
			if (bottom > height) {
				bottom = height;
				top = bottom - iv_metering.getHeight();
			}
		}
		iv_metering.layout(left, top, right, bottom);
		lastX = (int) event.getRawX();
		lastY = (int) event.getRawY();
	}
}