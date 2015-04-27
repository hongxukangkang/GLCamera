package com.meitu.glcamera.widgetUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.meitu.glcamera.enableInterfaces.CameraImgInterface;
import com.meitu.glcamera.widgets.CameraSurfaceView;

public final class CameraImgInterfaceImpl implements CameraImgInterface {

//    private RelativeLayout inflateViewContainer;
    private CameraSurfaceView surfaceView;
    private String scale;
    
    public CameraImgInterfaceImpl() {
    }

    public CameraImgInterfaceImpl(CameraSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    private boolean frontFlag;

    public void setFrontFlag(boolean frontFlag){
        this.frontFlag = frontFlag;
    }

    @Override
    public void savePictureToSDCardss(byte[] data, int rotation) throws IOException {
        if (frontFlag){
            if ((scale != null) && scale.contains("4:3")) {
                saveImgFileToSDcardRotateWithBack(data, rotation);
            } else {
                saveImagWithFrontOneToOne(data, rotation);
            }
            return;
        }
        if ((scale != null) && scale.contains("4:3")) {
            saveImgFileToSDcardRotate(data, rotation);
        } else {
            saveImagWithOneToOne(data, rotation);
        }
    }

    private void saveImagWithFrontOneToOne(byte[] data, int rotation){

    }

    private void saveImgFileToSDcardRotateWithBack(byte[] data, int rotation) throws IOException{
        String imgNm = getImgName();
        String sdPath = SDCardUtils.getSDCardPath();
        String path = sdPath + "MTCamera";
        File fm = new File(path);
        if (!fm.exists() && !fm.isDirectory()) {
            fm.mkdir();
        }
        String filePath = path + File.separator + "meitu_" + imgNm;
        File file = new File(filePath);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(rotation);
        FileOutputStream out = new FileOutputStream(file);
        Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] newData = baos.toByteArray();
        out.write(newData);
        out.close();
    }

    public void setInflateViewContainer(RelativeLayout inflateViewContainer) {
//        this.inflateViewContainer = inflateViewContainer;
    }

    private String getImgName() {
        Calendar now = Calendar.getInstance();
        String year = "" + now.get(Calendar.YEAR);
        String month = "" + (now.get(Calendar.MONTH) + 1);
        String day = "" + now.get(Calendar.DAY_OF_MONTH);
        String millSds = "" + now.getTimeInMillis();
        String imgNm = year + "_" + month + "_" + day + "_" + millSds + ".jpg";
        return imgNm;
    }


    private void saveImgFileToSDcardRotate(byte[] data, int rotation) throws IOException {
        String imgNm = getImgName();
        String sdPath = SDCardUtils.getSDCardPath();
        String path = sdPath + "MTCamera";
        File fm = new File(path);
        if (!fm.exists() && !fm.isDirectory()) {
            fm.mkdir();
        }
        String filePath = path + File.separator + "meitu_" + imgNm;
        File file = new File(filePath);
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation);
        FileOutputStream out = new FileOutputStream(file);
        Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] newData = baos.toByteArray();
        out.write(newData);
        out.close();
    }

    private void saveImagWithOneToOne(byte[] data, int rotation) throws FileNotFoundException,
            IOException {
        String imgNm = getImgName();
        String sdPath = SDCardUtils.getSDCardPath();
        String path = sdPath + "MTCamera";
        File fm = new File(path);
        if (!fm.exists() && !fm.isDirectory()) {
            fm.mkdir();
        }
        String filePath = path + File.separator + "meitu_" + imgNm;
        File file = new File(filePath);
        FileOutputStream out = new FileOutputStream(file);
        // start to clip the newBitmap
        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation);
        Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
        if (rotation == 0) {// 音乐键在上
            bitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth() * 3 / 4, newBitmap.getHeight());
        } else if (rotation == 90) {// 竖屏正常拍
            bitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight() * 3 / 4);
        } else if (rotation == 180) {// 竖屏音乐键在下
            bitmap = Bitmap.createBitmap(newBitmap, newBitmap.getWidth() / 4, 0, newBitmap.getWidth() * 3 / 4, newBitmap.getHeight());
        } else if (rotation == 270) {// 拍照按钮在正上方
            bitmap = Bitmap.createBitmap(newBitmap, 0, newBitmap.getHeight() / 4, newBitmap.getWidth(), newBitmap.getHeight() * 3 / 4);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] newData = baos.toByteArray();
        out.write(newData);
        out.close();
    }

    public void setScale(String scale) {
        this.scale = scale;
    }
}