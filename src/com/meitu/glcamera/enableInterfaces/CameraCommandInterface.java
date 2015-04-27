package com.meitu.glcamera.enableInterfaces;

public interface CameraCommandInterface {
	/**set flash on or off**/
	public void setFlashCmd(String value);
	/**take picture command**/
	public void takePickCmd();
	/**set camera exposure**/
	public void setExposureCmd(int value);
	/**set front camera on**/
	public void setCameraFrontCmd();
	/**set back camera on**/
	public void setCameraBackCmd();
}
