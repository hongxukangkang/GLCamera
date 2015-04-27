package com.meitu.glcamera.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import com.meitu.glcamera.enableInterfaces.CameraCommandInterface;
import com.meitu.glcamera.enableInterfaces.CameraImgInterface;
import com.meitu.glcamera.widgetUtils.CameraParameterUtils;
import com.meitu.glcamera.widgetUtils.SnapshotSound;

/**
 * initial Camera:open the back camera,flash off,auto focus ***
 */
@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressWarnings("deprecation")
public class CameraSurfaceView extends SurfaceView implements Callback {

    private final String TAG = "CameraSurfaceView2";
    private Context ctx;
    private int cameraNum;
    private Camera mCamera;
    private int curRotation;
    private int curCameraID;
    private Parameters mParam;
    private ShutterCallback shutter;
    private AutoFocusCallback focusCB;
    private CameraCommandInterface cmd;
    private CameraPictureCallback jpeg;
    private SavePictureTask saveImgTask;
    private SurfaceHolder mSurfaceHolder;
    private TouchFocusAutoCallback touchCB;
    private CameraImgInterface imgInterface;
//    private Camera.PreviewCallback mPreCallBack;
    private CameraAutoFocusCallBack mAutoFocusCallBack;


    // private boolean isFocusing = false;
    private boolean isPreviewing = false;
    private boolean flashOnState = false;
    private boolean cameraFrontOnState = false;

    private CameraPreviewCallback mCameraPreviewCallBack;

    public CameraSurfaceView(Context context) {
        super(context);
        initData(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    private void initData(Context ctx) {
        this.ctx = ctx;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        jpeg = new CameraPictureCallback();
        mAutoFocusCallBack = new CameraAutoFocusCallBack();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initialParamAndOpenCamera();
    }

    /**
     * Default open the back Camera **
     */
    private void initialParamAndOpenCamera() {
        try {
            setCamera();
            setCameraParameter();
            openCameraPreview();
        } catch (Exception e) {
            isPreviewing = false;
            releaseCameraSources();
            Log.i(TAG, "*******initial camera Failed********");
            e.printStackTrace();
        }
    }

    private void openCameraPreview() throws IOException {
        mCameraPreviewCallBack = new CameraPreviewCallback();
        mCamera.setPreviewDisplay(mSurfaceHolder);
//        mCamera.setPreviewCallback(mCameraPreviewCallBack);
        mCamera.startPreview();
        mCamera.autoFocus(mAutoFocusCallBack);
        isPreviewing = true;
    }

    private void setCamera() {
        if (Integer.parseInt(Build.VERSION.SDK) >= 9) {
            cameraNum = Camera.getNumberOfCameras();
            CameraInfo info = null;
            for (int i = 0; i < cameraNum; i++) {
                info = new CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                    curCameraID = i;
                }
            }
        }
        mCamera = Camera.open(curCameraID);
    }

    private void setCameraParameter() {
        mParam = mCamera.getParameters();
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            showPreviewPosition(mCamera, 90);
        } else {
            mParam.setRotation(90);
        }

        float r = 4.0f / 3.0f;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int mWid = dm.widthPixels;
        List<Size> picList = mParam.getSupportedPictureSizes();
        List<Size> preList = mParam.getSupportedPreviewSizes();
        Size piSize = CameraParameterUtils.getPropPictureSize(picList, r, mWid);
        Size prSize = CameraParameterUtils.getPropPreviewSize(preList, r, mWid);
        mParam.setPictureFormat(ImageFormat.JPEG);
        mParam.setPictureSize(piSize.width, piSize.height);
        mParam.setPreviewSize(prSize.width, prSize.height);
        // mParam.setPreviewSize(640, 480);
        if (!flashOnState) {
            mParam.setFlashMode(Parameters.FLASH_MODE_OFF);
        } else {
            mParam.setFlashMode(Parameters.FLASH_MODE_ON);
        }
        // setFocusable(true);
        // mParam.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mParam.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        mParam.setJpegQuality(100);
        mCamera.setParameters(mParam);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCameraSources();
    }

    private void releaseCameraSources() {
        Log.i(TAG, "=====release camera====");
        if ((mCamera != null)) {// && isPreviewing
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setFlashCmd(String value) {
        mParam = mCamera.getParameters();
        mParam.setFlashMode(value);
        mCamera.setParameters(mParam);
    }

    public void takePictureCmd() {
        if (isPreviewing && (mCamera != null) && !isFocusing) {
            new Thread() {
                @Override
                public void run() {
                    new SnapshotSound(ctx).playSound();
                }
            }.start();
            mCamera.takePicture(null, null, jpeg);
        }
    }

    public void setCameraFrontCmd() {
        cmd.setCameraFrontCmd();
    }

    public void setCameraBackCmd() {
        cmd.setCameraBackCmd();
    }

    public void registerCameraCommand(CameraCommandInterface cmd) {
        this.cmd = cmd;
    }

    public void registerCameraImgInterface(CameraImgInterface imgCallback) {
        this.imgInterface = imgCallback;
    }

    private void showPreviewPosition(Camera camera, int i) {
        Method showMethod;
        try {
            showMethod = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (showMethod != null) {
                showMethod.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.i(TAG, "set preview wrong");
        }
    }

    private final class CameraPictureCallback implements PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            saveImgTask = new SavePictureTask(data);
            saveImgTask.execute();
        }
    }

    private final class CameraAutoFocusCallBack implements AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mCamera.cancelAutoFocus();
            }
        }
    }

    public void startAutoFocus(int x, int y) {
        try {
            mCamera.autoFocus(null);
            mParam = mCamera.getParameters();
            touchCB = new TouchFocusAutoCallback();
            List<Area> focusAreas = new ArrayList<Area>();// focus area...
            List<Area> meteringAreas = new ArrayList<Area>();// metering area...
            Rect focusRect = calculateTapArea(x, y, 1.0f);
            if (mParam.getMaxNumFocusAreas() > 0) {
                focusAreas.add(new Area(focusRect, 1000));
            }

            Rect meterRect = calculateTapArea(x, y, 1.5f);

            if (mParam.getMaxNumMeteringAreas() > 0) {
                meteringAreas.add(new Area(meterRect, 1000));
            }
            setPreviewAndPictureSize();
            setFocusable(true);
            mParam.setFocusAreas(focusAreas);
            mParam.setMeteringAreas(meteringAreas);
            setFocusMode();
            mCamera.setParameters(mParam);
            mCamera.autoFocus(touchCB);
        } catch (Exception e) {
            e.printStackTrace();
            releaseCameraSources();
        }
    }

    private void setPreviewAndPictureSize() {
        float r = 4.0f / 3.0f;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int mWid = dm.widthPixels;
        List<Size> picList = mParam.getSupportedPictureSizes();
        List<Size> preList = mParam.getSupportedPreviewSizes();
        Size piSize = CameraParameterUtils.getPropPictureSize(picList, r, mWid);
        Size prSize = CameraParameterUtils.getPropPreviewSize(preList, r, mWid);
        mParam.setPictureFormat(ImageFormat.JPEG);
        mParam.setPictureSize(piSize.width, piSize.height);
        mParam.setPreviewSize(prSize.width, prSize.height);
    }

    private void setFocusMode() {
        List<String> supFocusModes = mParam.getSupportedFocusModes();
        if (supFocusModes.contains("macro")) {
            mParam.setFocusMode(Parameters.FOCUS_MODE_MACRO);
        } else if (supFocusModes.contains("auto")) {
            mParam.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        } else if (supFocusModes.contains("continuous-picture")) {
            mParam.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (supFocusModes.contains("continuous-video")) {
            mParam.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (supFocusModes.contains("infinity")) {
            mParam.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
    }

    public void registerShutterCallback(ShutterCallback shutter) {
        this.shutter = shutter;
    }

    private final class SavePictureTask extends AsyncTask<Void, Void, Boolean> {
        private byte[] data;

        public SavePictureTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                isPreviewing = false;
                imgInterface.savePictureToSDCardss(data, curRotation);
                return true;
            } catch (Exception e) {
                Log.i(TAG, "=====savePicture wrong===");
                isPreviewing = false;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mCamera != null) {
                    mCamera.startPreview();
                    isPreviewing = true;
                }
            } else {
                isPreviewing = false;
            }
        }
    }

    public Camera getMCamera() {
        return mCamera;
    }

    public void setMCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public void setCurRotation(int curRotation) {
        this.curRotation = curRotation;
    }

    public SurfaceHolder getmSurfaceHolder() {
        return mSurfaceHolder;
    }

    public void setPreviewing(boolean isPreviewing) {
        this.isPreviewing = isPreviewing;
    }

    private boolean isFocusing = false;

    private final class TouchFocusAutoCallback implements AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.i(TAG, "focus success....");
                isFocusing = false;
                mCamera.cancelAutoFocus();
            } else {
                isFocusing = true;
                Log.i(TAG, "focus fail....");
            }
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x / getResolution().width * 2000 - 1000);
        int centerY = (int) (y / getResolution().height * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);

        if ((bottom - top) == 0) {
            if (bottom == -1000) {
                bottom += 1;
            } else if (bottom == 1000) {
                top = top - 1;
            }
        }
        if ((right - left) == 0) {
            if (right == -1000) {
                right += 1;
            } else if (right == 1000) {
                left = left - 1;
            }
        }
        return new Rect(left, top, right, bottom);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }


    public void setHandle(Handler handler) {
        mHandler = handler;
    }

    private Handler mHandler;

    private final class CameraPreviewCallback implements Camera.PreviewCallback {
        public CameraPreviewCallback() {

        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (data != null) {
                int imageWidth = mCamera.getParameters().getPreviewSize().width;
                int imageHeight = mCamera.getParameters().getPreviewSize().height;
                int RGBData[] = new int[imageWidth * imageHeight];
                decodeYUV420SP(RGBData, data, imageWidth, imageHeight); // ½âÂë
                Bitmap bm = Bitmap.createBitmap(RGBData, imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            }
        }
    }

    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}