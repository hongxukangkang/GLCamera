package com.meitu.glcamera.ui;

import com.meitu.glcamera.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

//import android.app.ProgressDialog;
//import android.widget.Button;

public class ImageDetailDialog extends Dialog implements View.OnClickListener {

//	private Button okBtn;
	private TextView txt_time;
	private TextView txt_size;
	private TextView img_sizek;

	public ImageDetailDialog(Context context) {
		super(context);
		setTitle(R.string.img_detailInfo);
		setContentView(R.layout.img_detailinfo_layout);
		txt_time = (TextView) findViewById(R.id.take_time);
		txt_size = (TextView) findViewById(R.id.img_size);
		img_sizek = (TextView) findViewById(R.id.img_sizek);
//		okBtn = (Button) findViewById(R.id.ok);
//		okBtn.setOnClickListener(this);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.gravity=Gravity.CENTER;
		window.setAttributes(params);
	}
	
	public void disPlay(){
		show();
	};
	
	public TextView getTimeView(){
		return txt_time;
	}
	
	public TextView getSizeView(){
		return txt_size;
	}
	
	public TextView getSizeKView(){
		return img_sizek;
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}
}
