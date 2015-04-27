package com.meitu.glcamera.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.meitu.glcamera.R;
import com.meitu.glcamera.widgetUtils.AlbumAdapter;
import com.meitu.glcamera.widgetUtils.Image;
import com.meitu.glcamera.widgetUtils.ImageUtils;
import com.meitu.glcamera.widgetUtils.SDCardUtils;

/** Use for scanning the pictures ***/
@SuppressLint("NewApi")
public class AlbumPreviewActivity extends Activity implements OnClickListener {

	protected static final String TAG = "AlbumPreviewActivity";

	private TextView titleView;
	private ImageView leftView;
	private ImageView rightView;
	private ImageView delImgView;
	private ImageView infoImageView;
	private Context context;

	private List<Image> imgList;
	private ViewPager mViewPager;
	private AlbumAdapter adapter;
	private ProgressDialog showImgDialogPrg;
	private final int OBTAIN_IMGINFO_DIALOG = 0x0004;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.album_preview_activity);
		findViewsAndInitialUI();
		loadImageDatas();
		showDialog(OBTAIN_IMGINFO_DIALOG);
		setListeners();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case OBTAIN_IMGINFO_DIALOG:
			showImgDialogPrg = ProgressDialog.show(this, "", getString(R.string.loading_Img_datas), true, true);
			showImgDialogPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dimissDialog();
				}
			});
			return showImgDialogPrg;
		default:
			return null;
		}
	}

	private void dimissDialog() {
		if (showImgDialogPrg != null && showImgDialogPrg.isShowing()) {
			showImgDialogPrg.dismiss();
		}
	}

	private void setListeners() {

		leftView.setOnClickListener(this);
		delImgView.setOnClickListener(this);
		infoImageView.setOnClickListener(this);

	}

	private void findViewsAndInitialUI() {
		leftView = (ImageView) findViewById(R.id.take_pic_main_pager);
		rightView = (ImageView) findViewById(R.id.mode_set);
		delImgView = (ImageView) findViewById(R.id.ivv_del);
		titleView = (TextView) findViewById(R.id.take_pic_home);
		titleView.setText(getString(R.string.album_preview));
		rightView.setVisibility(View.GONE);
		infoImageView = (ImageView) findViewById(R.id.ivv_info);
		mViewPager = (ViewPager) findViewById(R.id.album_viewpager);
		imgList = new ArrayList<Image>();
		context = AlbumPreviewActivity.this;
		leftView.setImageResource(R.drawable.album_left_back_btn_selector);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.take_pic_main_pager:
			backToTakePictureUI();
			break;
		case R.id.ivv_info:
			showImgInfoWork();
			break;
		case R.id.ivv_del:
			imageDeleteWorkAction();
			
			break;
		case R.id.album_del_btn_ok:
			dimissDeleteDialog();
			int curPos = adapter.getCurrentPosition();
			Image img = imgList.get(curPos);
			final String imgPath = img.getImgPath();
			new Thread() {
				@Override
				public void run() {
					File file = new File(imgPath);
					file.delete();
				}
			}.start();
			imgList.remove(curPos);
			adapter = new AlbumAdapter(context, titleView,imgList);
			mViewPager.setAdapter(adapter);
			if (curPos==0&&(imgList.size()>=1)) {
				titleView.setText("1/"+imgList.size());
			}else if ((imgList.size()>=1)){
				titleView.setText(curPos+"/"+imgList.size());
				mViewPager.setCurrentItem(curPos);
			}
			break;
		default:
			break;
		}
	}

	private void showImgInfoWork() {
		if (imgList!=null&&imgList.size()>0) {
			showImgInfoDialog();
			return;
		}
		showToasContent(getString(R.string.album_img_null));
	}

	private void imageDeleteWorkAction() {
		if ((imgList!=null)&&(imgList.size()>0)) {
			showDeleteImgDialog();
			delDialog.getOkBtn().setOnClickListener(this);
			return;
		}
		showToasContent(getString(R.string.album_null));
	}

	private void dimissDeleteDialog() {
		if (delDialog != null && delDialog.isShowing()) {
			delDialog.dismiss();
		}
	}

	private ImageDetailDialog detailDialog;

	private void showImgInfoDialog() {
		detailDialog = new ImageDetailDialog(context);
		detailDialog.disPlay();
		int post = mViewPager.getCurrentItem();
		Image image = imgList.get(post);
		Calendar c = image.getTakeTime();
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH) + 1;
		int d = c.get(Calendar.DAY_OF_MONTH);

		int h = c.get(Calendar.HOUR_OF_DAY);
		int mi = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);
		String cont = y + "/" + m + "/" + d;
		cont += (" " + h + ":" + mi + ":" + s);
		detailDialog.getTimeView().setText(cont);

		String path = image.getImgPath();
		Bitmap bm = BitmapFactory.decodeFile(path);
		int height = bm.getHeight();
		int width = bm.getWidth();
		String size = height + "*" + width;
		detailDialog.getSizeView().setText(size);

		long countbm;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			countbm = bm.getByteCount();
		} else {
			countbm = bm.getRowBytes() * bm.getHeight();
		}
		float showK = (float) (countbm / (1024));
		detailDialog.getSizeKView().setText(""+showK+"KB");
	}

	private void loadImageDatas() {
		new LoadImgDataTask().execute();
	}

	private final class LoadImgDataTask extends
			AsyncTask<Void, Void, List<Image>> {
		public LoadImgDataTask() {
		}

		@Override
		protected List<Image> doInBackground(Void... params) {
			
			File temppath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			
//			String temppath = SDCardUtils.getSDCardPath();
			String path = temppath + File.separator + "GLCamera";
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}

			String[] imgStr = file.list();
			if ((imgStr == null) || (imgStr.length == 0)) {
				return null;
			}

			for (int i = 0; i < imgStr.length; i++) {
				String[] imgInfo = imgStr[i].split("_");
				String mlS = imgInfo[imgInfo.length - 1].replace(".jpg", "");
				Calendar calr = ImageUtils.getInstance().getCalendarTime(mlS);
				Image img = new Image();
				String imgPath = path + File.separator + imgStr[i];
				img.setImgPath(imgPath);
				img.setTakeTime(calr);
				imgList.add(img);
			}
			Collections.sort(imgList, new CameraSizeComparator());
			return imgList;
		}

		@Override
		protected void onPostExecute(List<Image> result) {
			super.onPostExecute(result);
			dimissDialog();
			if (result == null) {
				String content = getString(R.string.album_preview_img_null);
				showToasContent(content);
				return;
			}
			titleView.setText("1/"+result.size());
			adapter = new AlbumAdapter(context, titleView,result);
			mViewPager.setOnPageChangeListener(adapter);
			mViewPager.setAdapter(adapter);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	};

	private AlbumDeleteDialog delDialog;

	private void showDeleteImgDialog() {
		delDialog = new AlbumDeleteDialog(context);
		delDialog.display();
	}

	private void showToasContent(String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

	private void backToTakePictureUI() {
		AlbumPreviewActivity.this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToTakePictureUI();
		}
		return super.onKeyDown(keyCode, event);
	}

	private static class CameraSizeComparator implements Comparator<Image> {
		@Override
		public int compare(Image lhs, Image rhs) {
			long time1 = lhs.getTakeTime().getTimeInMillis();
			long time2 = rhs.getTakeTime().getTimeInMillis();
			if (time1 > time2) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}