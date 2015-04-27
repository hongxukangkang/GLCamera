package com.meitu.glcamera.enableInterfaces;

import java.io.IOException;

public interface CameraImgInterface {
	
	public void savePictureToSDCardss(byte[] data, int rotation)throws IOException;

}
