package com.meitu.glcamera.widgetUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import com.meitu.glcamera.enableInterfaces.CameraCommandInterface;
import com.meitu.glcamera.ui.TakePictureActivity;
import com.meitu.glcamera.widgets.CameraSurfaceView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressWarnings("deprecation")
public class CameraCommandImpl implements CameraCommandInterface {

    private static final String TAG = "CameraCommandImpl";
    private CameraSurfaceView mSurfaceView;
    private Parameters mParam;
    private Camera mCamera;
    private Context ctx;

    private boolean isModeSetting = false;

    public CameraCommandImpl(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void setFlashCmd(String value) {

    }

    @Override
    public void takePickCmd() {

    }

    @Override
    public void setExposureCmd(int value) {

    }

    @SuppressLint("NewApi")
    @Override
    public void setCameraFrontCmd() {
        try {
			releaseCamera();
            if (isModeSetting) return;
            isModeSetting = true;
            int num = Camera.getNumberOfCameras();
            int curCameraId = 1;
            for (int i = 0; i < num; i++) {
                CameraInfo info = new CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    curCameraId = i;
                    break;
                }
            }
            mCamera = Camera.open(curCameraId);
            mParam = mCamera.getParameters();
            SurfaceHolder holder = mSurfaceView.getmSurfaceHolder();
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                showRightPreviewPosition(mCamera, 90);
            } else {
                mParam.setRotation(90);
            }
            setCameraParameter();
            setCamera(holder);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "打开前置摄像头失败....");
        }
    }

    @SuppressLint("InlinedApi")
    private void setCameraParameter() {
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

        List<String> supFlashModes = mParam.getSupportedFlashModes();
        if ((supFlashModes != null) && supFlashModes.contains("off")) {
            mParam.setFlashMode(Parameters.FLASH_MODE_OFF);
        }
        setFocusMode();
        mParam.setJpegQuality(100);
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

    private void releaseCamera() {
        mCamera = mSurfaceView.getMCamera();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void setCameraBackCmd() {
        try {
			releaseCamera();
            if (isModeSetting)return;
            isModeSetting = true;
            int num = Camera.getNumberOfCameras();
            int curCameraId = 0;
            for (int i = 0; i < num; i++) {
                CameraInfo info = new CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                    curCameraId = i;
                    break;
                }
            }
            mCamera = Camera.open(curCameraId);
            mParam = mCamera.getParameters();
            SurfaceHolder holder = mSurfaceView.getmSurfaceHolder();
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                showRightPreviewPosition(mCamera, 90);
            } else {
                mParam.setRotation(90);
            }
            setCameraParameter();
            setCamera(holder);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "打开前置摄像头失败....");
        }
    }

    private void setCamera(SurfaceHolder holder) throws IOException {
        mCamera.setPreviewDisplay(holder);
        mCamera.setParameters(mParam);
        mCamera.startPreview();
        isModeSetting = false;
        ((TakePictureActivity) ctx).setModelSetting(false);
        mSurfaceView.setPreviewing(true);
        mSurfaceView.setMCamera(mCamera);
    }

    private void showRightPreviewPosition(Camera camera, int i) {
        Method showMethod;
        try {
            showMethod = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (showMethod != null) {
                showMethod.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.i(TAG, "图像显示位置设置出错");
        }
    }

    public CameraSurfaceView getMSurfaceView() {
        return mSurfaceView;
    }

    public void setMSurfaceView(CameraSurfaceView mSurfaceView) {
        this.mSurfaceView = mSurfaceView;
    }
}