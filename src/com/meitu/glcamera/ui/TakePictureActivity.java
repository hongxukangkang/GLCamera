package com.meitu.glcamera.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import com.meitu.glcamera.R;
import com.meitu.glcamera.widgetUtils.CameraCommandImpl;
import com.meitu.glcamera.widgetUtils.CameraImgInterfaceImpl;
import com.meitu.glcamera.widgetUtils.CameraShutterBack;
import com.meitu.glcamera.widgets.CameraSurfaceView;
import com.meitu.glcamera.widgets.FilterView;
import com.meitu.glcamera.widgets.SquareView;

@SuppressWarnings("deprecation")
public class TakePictureActivity extends Activity implements OnClickListener, View.OnTouchListener {

//    static {
//        System.loadLibrary("jni_color");
//    }

//    public native String getString();

//    private GPUImage mGPUImage;

    private TextView title;
    private static final int ALBUM_REQUESTCODE = 0x0003;
    // for Camera
    static final String TAG = "TakePictureActivity";
    private String scale;
    private View rootView;
    private Camera mCamera;
    private Context context;
    private Camera testCamera;
    private Parameters mParams;
    private ShutterCallback shutter;
    private CameraCommandImpl cmdImpl;
    private CameraImgInterfaceImpl imgImpl;
    private CameraSurfaceView mSurfaceView;
    private PhoneOrientationListener orientationListener;

    // for UI click Event
    private TextView txt_Scale;
    private ImageView iv_Flashet;
    private ImageView iv_ModeSet;
    private ImageView iv_albumScan;
    private ImageView iv_squareSet;
    private ImageView iv_takePicture;
    private boolean isModeSetting = false;
    private boolean hasDrawSquared = false;
    private boolean isSquareShowing = false;//

    // for UI Layout
    private int screenWidth;
    private int screenHeight;
    private SquareView mSquareView;
    private LinearLayout inflateView;
    private LinearLayout headerLayout;
    private LinearLayout bottomLayout;
    private boolean frontOnState = false;
    private boolean hasScaleChange = false;
    private LinearLayout filterViewContainer;
    private RelativeLayout inflateViewContainer;
    private UIOnGlobalLayoutListener layoutListener;

    //for exit
    private static boolean isExit = false;
    private static boolean hasTask = false;

    // for touch focus
    private ImageView iv_focus;
    private ImageView iv_metering;
    private Animation focusAnimation;
    private Animation meteringAnimation;
    private RelativeLayout mCameraContainer;
    private FlashMODE flashMode = FlashMODE.FORBIDDN;

    private enum FlashMODE {FORBIDDN,AUTO,OPEN}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // check whether have the camera,if true,check has open;
        boolean isExistCamera = checkExistCamera();
        if (isExistCamera) {
            boolean isOpen = checkCamaerOpen();
            if (isOpen) {
                Toast.makeText(this, R.string.takepic_camera_has_open, Toast.LENGTH_SHORT).show();
            } else {
                closeTestCamera();
                initialAndFindViews();
                registerCallbackForSurfaceView();
                setListeners();
            }
        }
    }

    private void registerCallbackForSurfaceView() {
        cmdImpl = new CameraCommandImpl(this);
        shutter = new CameraShutterBack(this);
        cmdImpl.setMSurfaceView(mSurfaceView);
        imgImpl = new CameraImgInterfaceImpl(mSurfaceView);
        mSurfaceView.registerCameraCommand(cmdImpl);
        mSurfaceView.registerShutterCallback(shutter);
        mSurfaceView.registerCameraImgInterface(imgImpl);
    }

    private void setListeners() {
        txt_Scale.setOnClickListener(this);
        iv_ModeSet.setOnClickListener(this);
        iv_Flashet.setOnClickListener(this);
        iv_albumScan.setOnClickListener(this);
        iv_squareSet.setOnClickListener(this);
        iv_takePicture.setOnClickListener(this);

        layoutListener = new UIOnGlobalLayoutListener();
        ViewTreeObserver vto = rootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(layoutListener);
        mCameraContainer.setOnTouchListener(this);
    }

    private void initialAndFindViews() {
        context = this;
        rootView = findViewById(R.id.rootView);
        title = (TextView) findViewById(R.id.title);
        txt_Scale = (TextView) findViewById(R.id.scale_measure);
        iv_ModeSet = (ImageView) findViewById(R.id.mode_set);
        iv_Flashet = (ImageView) findViewById(R.id.flash_set);
        iv_albumScan = (ImageView) findViewById(R.id.pic_scan);
        iv_squareSet = (ImageView) findViewById(R.id.squares_set);
        iv_takePicture = (ImageView) findViewById(R.id.ibv_take_pic);
        mSurfaceView = (CameraSurfaceView) findViewById(R.id.pic_surfaceview);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        inflateView = (LinearLayout) findViewById(R.id.inflateView);
        headerLayout = (LinearLayout) findViewById(R.id.takePic_header);
        bottomLayout = (LinearLayout) findViewById(R.id.takePic_bottom);
        inflateViewContainer = (RelativeLayout) findViewById(R.id.inflateView_container);
        orientationListener = new PhoneOrientationListener(context);
        orientationListener.enable();
        mCameraContainer = (RelativeLayout) findViewById(R.id.surfaceView_container);

        // for camera focus and met
        focusAnimation = AnimationUtils.loadAnimation(this, R.anim.focus_animation);
        meteringAnimation = AnimationUtils.loadAnimation(this, R.anim.metering_animation);
        iv_focus = new ImageView(this);
        iv_metering = new ImageView(this);
        iv_focus.setImageResource(R.drawable.camera_focus);
        iv_metering.setImageResource(R.drawable.camera_metering);
        mCameraContainer.addView(iv_focus);
        mCameraContainer.addView(iv_metering);
        iv_focus.setVisibility(View.INVISIBLE);
        iv_metering.setVisibility(View.INVISIBLE);

    }

    private void closeTestCamera() {
        testCamera.stopPreview();
        testCamera.release();
        testCamera = null;
    }

    private boolean checkCamaerOpen() {
        try {
            testCamera = Camera.open();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean checkExistCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scale_measure:
                setPreviewAreaForCamera();
                break;
            case R.id.mode_set:
                changetFrontOrBackCamera();
                break;
            case R.id.flash_set:
                setCameraFlash();
                break;
            case R.id.pic_scan:
                gotoAlbumPreviewActivity();
                break;
            case R.id.ibv_take_pic:
                playSoundAndTakePicture();
                break;
            case R.id.squares_set:
                drawSquare();
                break;
            case R.id.pic_surfaceview:
                break;
            case R.id.iv_original:
                orginalAction();
                break;
            case R.id.iv_Gold:
                goldAction();
            break;
            case R.id.iv_Vista:
                showToast("click the vista");
                break;
            case R.id.iv_Iceland:
                showToast("click the iceland");
                break;
            case R.id.iv_Solaris:
                showToast("click the solaris");
                break;
        }
    }

    private void orginalAction(){
        showToast("click the original");
//        mCamera.setPreviewCallback(null);//when preview,reset the previewcallback function
    }

    private void goldAction(){
        showToast("click the Gold");
//        mCamera.setPreviewCallback(null);
    }

    private void showToast(String txt){
        Toast.makeText(this,txt,Toast.LENGTH_SHORT).show();
    }

    private void drawSquare() {
        if (!hasDrawSquared) {
            //judge the formal show with 4:3 or 1:1
            String temp = txt_Scale.getText().toString();
            if (temp.contains("4:3")){
                displaySquare(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            }else {
                int h = mSurfaceView.getHeight() - inflateViewContainer.getHeight();
                displaySquare(mSurfaceView.getWidth(), h);
            }
            hasDrawSquared = true;
            iv_squareSet.setBackgroundResource(R.drawable.square_display_selector);
        } else {
            dismissSquare();
            hasDrawSquared = false;
            iv_squareSet.setBackgroundResource(R.drawable.square_dismiss_selector);
        }
    }

    // start draw the square
    private void displaySquare(int width, int height) {
        isSquareShowing = true;
        mSquareView = new SquareView(this);
        mSquareView.setScreenWidth(width);
        mSquareView.setScreenHeight(height);
        mCameraContainer.addView(mSquareView);
        mSquareView.setVisibility(View.VISIBLE);
    }

    //dismiss the square
    private void dismissSquare() {
        isSquareShowing = false;
        mSquareView.setVisibility(View.INVISIBLE);
    }

    private void changetFrontOrBackCamera() {
        if (frontOnState) {
            frontOnState = false;
            isModeSetting = true;
            mSurfaceView.setCameraBackCmd();
            iv_Flashet.setVisibility(View.VISIBLE);
        } else {
            frontOnState = true;
            isModeSetting = true;
            mSurfaceView.setCameraFrontCmd();
            iv_Flashet.setVisibility(View.INVISIBLE);
        }
        imgImpl.setFrontFlag(frontOnState);
    }

    private void setCameraFlash() {
        if (flashMode == FlashMODE.FORBIDDN) {
            flashMode = FlashMODE.AUTO;
            mParams = mCamera.getParameters();
            mParams.setFlashMode(Parameters.FLASH_MODE_AUTO);
            iv_Flashet.setBackgroundResource(R.drawable.flash_auto_selector);
        } else if (flashMode == FlashMODE.AUTO){
            flashMode = FlashMODE.OPEN;
            mParams = mCamera.getParameters();
            mParams.setFlashMode(Parameters.FLASH_MODE_ON);
            iv_Flashet.setBackgroundResource(R.drawable.flash_open_selector);
        }else {
            flashMode = FlashMODE.FORBIDDN;
            mParams = mCamera.getParameters();
            mParams.setFlashMode(Parameters.FLASH_MODE_OFF);
            iv_Flashet.setBackgroundResource(R.drawable.flash_off_selector);
        }
        mCamera.setParameters(mParams);
    }

    private void gotoAlbumPreviewActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AlbumPreviewActivity.class);
//        startActivity(intent);
        startActivityForResult(intent, ALBUM_REQUESTCODE);
    }

    private void setPreviewAreaForCamera() {
        if (!hasScaleChange) {
            hasScaleChange = true;
            txt_Scale.setText("1:1");
            inflateView.setVisibility(View.VISIBLE);
            if (isSquareShowing) {
                dismissSquare();
                int h = mSurfaceView.getHeight() - inflateViewContainer.getHeight();
                displaySquare(mSurfaceView.getWidth(), h);
            }
        } else {
            hasScaleChange = false;
            txt_Scale.setText("4:3");
            inflateView.setVisibility(View.INVISIBLE);
            if (isSquareShowing) {
                dismissSquare();
                displaySquare(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            }
        }
    }

    private final class UIOnGlobalLayoutListener implements OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            if (!hasCalculated) {
                reCalcuWidgetsLayout();//layout with 4:3;caculate 1:1��inflateView height and width
                hasCalculated = true;
            }
        }
    }

    private void reCalcuWidgetsLayout() {
        LayoutParams params = mSurfaceView.getLayoutParams();
        int h = 4 * screenWidth / 3;
        params.height = h;
        params.width = screenWidth;
        mSurfaceView.setLayoutParams(params);
        int restHeight = screenHeight - h;

        LayoutParams hParam = headerLayout.getLayoutParams();
        hParam.width = screenWidth;
        hParam.height = (restHeight / 3);
        headerLayout.setLayoutParams(hParam);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);

        LayoutParams bParam = bottomLayout.getLayoutParams();
        bParam.width = screenWidth;
        bParam.height = (2 * restHeight / 3);
        bottomLayout.setLayoutParams(bParam);

        int mediaHeight = h - screenWidth;
        LayoutParams inflateParam = inflateView.getLayoutParams();
        inflateParam.width = screenWidth;
        inflateParam.height = mediaHeight;
        inflateView.setLayoutParams(inflateParam);
        inflateView.setVisibility(View.INVISIBLE);

        LayoutParams rParam = inflateViewContainer.getLayoutParams();
        rParam.width = screenWidth;
        rParam.height = mediaHeight;
        inflateViewContainer.setLayoutParams(rParam);
        inflateViewContainer.setGravity(Gravity.CENTER);

        filterViewContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.filterview_container, null);
        filterViewContainer.setVerticalScrollBarEnabled(false);
        inflateViewContainer.addView(filterViewContainer);

        LayoutParams lParam = filterViewContainer.getLayoutParams();
        lParam.height = mediaHeight;
        lParam.width = screenWidth;
        filterViewContainer.setLayoutParams(lParam);
        setTitleForFilterView();
    }

    private void setTitleForFilterView() {

        FilterView original = (FilterView) filterViewContainer.findViewById(R.id.iv_original);
        FilterView gold = (FilterView) filterViewContainer.findViewById(R.id.iv_Gold);
        FilterView vista = (FilterView) filterViewContainer.findViewById(R.id.iv_Vista);
        FilterView iceland = (FilterView) filterViewContainer.findViewById(R.id.iv_Iceland);
        FilterView solaris = (FilterView) filterViewContainer.findViewById(R.id.iv_Solaris);

        FilterView iv_690 = (FilterView) filterViewContainer.findViewById(R.id.iv_690);
        FilterView acros = (FilterView) filterViewContainer.findViewById(R.id.iv_across);
        FilterView seine = (FilterView) filterViewContainer.findViewById(R.id.iv_seine);
        FilterView middy = (FilterView) filterViewContainer.findViewById(R.id.iv_middy);
        FilterView migrant = (FilterView) filterViewContainer.findViewById(R.id.iv_migrant);

        FilterView iv_669 = (FilterView) filterViewContainer.findViewById(R.id.iv_669);
        FilterView woodStock = (FilterView) filterViewContainer.findViewById(R.id.iv_woodStock);
        FilterView creamy = (FilterView) filterViewContainer.findViewById(R.id.iv_Creamy);
        FilterView silver = (FilterView) filterViewContainer.findViewById(R.id.iv_silver);

        original.setTitleForImgFilter("Original");
        gold.setTitleForImgFilter("Gold");
        vista.setTitleForImgFilter("Vista");
        iceland.setTitleForImgFilter("Iceland");
        solaris.setTitleForImgFilter("Solaris");

        iv_690.setTitleForImgFilter("690");
        acros.setTitleForImgFilter("Acros");
        seine.setTitleForImgFilter("Seine");
        middy.setTitleForImgFilter("Middy");
        migrant.setTitleForImgFilter("Migrant");

        iv_669.setTitleForImgFilter("669");
        woodStock.setTitleForImgFilter("WoodStock");
        creamy.setTitleForImgFilter("Creamy");
        silver.setTitleForImgFilter("Silver");

        iceland.getTitleView().setBackgroundColor(getResources().getColor(R.color.palevioletred));
        original.getTitleView().setBackgroundColor(getResources().getColor(R.color.transparent));
        solaris.getTitleView().setBackgroundColor(getResources().getColor(R.color.goldenrod));
        gold.getTitleView().setBackgroundColor(getResources().getColor(R.color.goldenrod));
        vista.getTitleView().setBackgroundColor(getResources().getColor(R.color.plum));

        middy.getTitleView().setBackgroundColor(getResources().getColor(R.color.chocolate));
        iv_690.getTitleView().setBackgroundColor(getResources().getColor(R.color.orchid));
        acros.getTitleView().setBackgroundColor(getResources().getColor(R.color.thistle));
        migrant.getTitleView().setBackgroundColor(getResources().getColor(R.color.peru));
        seine.getTitleView().setBackgroundColor(getResources().getColor(R.color.tan));

        woodStock.getTitleView().setBackgroundColor(getResources().getColor(R.color.mediumvioletred));
        iv_669.getTitleView().setBackgroundColor(getResources().getColor(R.color.indianred));
        creamy.getTitleView().setBackgroundColor(getResources().getColor(R.color.darkkhaki));
        silver.getTitleView().setBackgroundColor(getResources().getColor(R.color.silver));

        resetImageViewSize(original);
        resetImageViewSize(gold);
        resetImageViewSize(vista);
        resetImageViewSize(iceland);
        resetImageViewSize(solaris);

        resetImageViewSize(iv_690);
        resetImageViewSize(acros);
        resetImageViewSize(seine);
        resetImageViewSize(middy);
        resetImageViewSize(migrant);

        resetImageViewSize(iv_669);
        resetImageViewSize(woodStock);
        resetImageViewSize(creamy);
        resetImageViewSize(silver);

       // set OnClickListener for widget
        gold.setOnClickListener(this);
        vista.setOnClickListener(this);
        iceland.setOnClickListener(this);
        solaris.setOnClickListener(this);
        original.setOnClickListener(this);

        acros.setOnClickListener(this);
        seine.setOnClickListener(this);
        middy.setOnClickListener(this);
        iv_690.setOnClickListener(this);
        migrant.setOnClickListener(this);

        iv_669.setOnClickListener(this);
        creamy.setOnClickListener(this);
        silver.setOnClickListener(this);
        woodStock.setOnClickListener(this);

    }

    private void resetImageViewSize(FilterView original) {
        LayoutParams lParams = original.getLayoutParams();
        lParams.width = screenWidth / 5;
        original.setLayoutParams(lParams);
    }

    private boolean hasSetInflateView = false;
    private boolean hasCalculated = false;

    private final class PhoneOrientationListener extends OrientationEventListener {

        public PhoneOrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            scale = txt_Scale.getText().toString();
            imgImpl.setScale(scale);
            if (!hasSetInflateView){
                imgImpl.setInflateViewContainer(inflateViewContainer);
                hasSetInflateView = true;
            }
            if (isModeSetting) {
                return;
            }
            mCamera = mSurfaceView.getMCamera();
            if (mCamera != null) {
                if (!frontOnState){
                    mParams = mCamera.getParameters();
                    mParams.setRotation(90);
                    mParams.set("rotation", 90);
                }
                if ((orientation >= 45) && (orientation < 135)) {
                    if (frontOnState){//
                        setCameraAngle(0);// 90
                    }else {
                        setCameraAngle(180);//music key down,exception
                    }
                } else if ((orientation >= 135) && (orientation < 225)) {
//                    if (frontOnState) {
//                        setCameraAngle(90);// 90
//                    }else {
                        setCameraAngle(270);
//                    }
                } else if ((orientation >= 225) && (orientation < 315)) {
//                    if (frontOnState) {
//                        setCameraAngle(180);// 90
//                    }else{
                        setCameraAngle(0);//music key top,formal
//                    }
                } else {
//                    if (frontOnState) {//portrait take picture
//                        setCameraAngle(0);
//                    }else{
                        setCameraAngle(90);
//                    }
                }
            }
        }
    }

    private void setCameraAngle(int angle){
        mParams.setRotation(angle);
        mParams.set("rotation", angle);
        mSurfaceView.setCurRotation(angle);
    }

    public void setModelSetting(boolean isModeSetting) {
        this.isModeSetting = isModeSetting;
    }

    private long startTakePicTime = System.currentTimeMillis();

    private void playSoundAndTakePicture() {
        long endTakePicTime = System.currentTimeMillis();
        if ((endTakePicTime - startTakePicTime) >= 1000) {
            mSurfaceView.takePictureCmd();
            startTakePicTime = endTakePicTime;
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
            Toast.makeText(this, R.string.take_picture_exit, Toast.LENGTH_SHORT).show();
            if (!hasTask) {
                tExit.schedule(task, 2 * 1000);
            }
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onDestroy(){
//        releaseCamera();
        super.onDestroy();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALBUM_REQUESTCODE) {
            Toast.makeText(context, "return", Toast.LENGTH_LONG).show();
            frontOnState = false;
            imgImpl.setFrontFlag(frontOnState);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mSurfaceView.startAutoFocus((int) event.getRawX(), (int) event.getRawY());//begin to focus
                startAnimationAction(event);
                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }
        return true;
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
}