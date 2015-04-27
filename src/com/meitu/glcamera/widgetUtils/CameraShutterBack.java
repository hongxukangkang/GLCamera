package com.meitu.glcamera.widgetUtils;

import android.content.Context;
import android.hardware.Camera.ShutterCallback;

@SuppressWarnings("deprecation")
public class CameraShutterBack implements ShutterCallback {
	
	private Context context;
	public CameraShutterBack(Context context){
		this.context = context;
	}

	@Override
	public void onShutter() {
		new SnapshotSound(context).playSound();
	}

}
