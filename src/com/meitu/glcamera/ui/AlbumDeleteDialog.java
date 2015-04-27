package com.meitu.glcamera.ui;

import com.meitu.glcamera.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class AlbumDeleteDialog extends Dialog implements OnClickListener {

	private Window window;
	private Button okBtn;
	private Button cancelBtn;

	private ImageDeleteInterfaceCallback callback;

	public void registerDeleteCallback(ImageDeleteInterfaceCallback callback) {
		this.callback = callback;
	}

	public AlbumDeleteDialog(Context context) {
		super(context);
	}

	public void display() {
		setProperty();
		setTitle(R.string.album_delete_recommd);
		show();
	}

	public Button getOkBtn() {
		return okBtn;
	}

	public Button getCancelBtn() {
		return cancelBtn;
	}

	@SuppressLint("ResourceAsColor")
	public void setProperty() {
		window = getWindow();//
		// window.getDecorView().setBackgroundColor(R.color.lightcyan);
		// window.getDecorView().set
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.alpha = 0.9f;
		wl.gravity = Gravity.CENTER;
		window.setAttributes(wl);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_delete_dialog);
		okBtn = (Button) findViewById(R.id.album_del_btn_ok);
		cancelBtn = (Button) findViewById(R.id.album_del_btn_cancel);
		okBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.album_del_btn_ok:
			callback.deleteImg();
			break;
		case R.id.album_del_btn_cancel:
			dismiss();
			break;
		}
	}

	public interface ImageDeleteInterfaceCallback {
		public void deleteImg();
	}
}