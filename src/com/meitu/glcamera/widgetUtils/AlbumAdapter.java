package com.meitu.glcamera.widgetUtils;

import java.util.List;

import com.meitu.glcamera.ui.AlbumDeleteDialog.ImageDeleteInterfaceCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends PagerAdapter implements OnPageChangeListener, ImageDeleteInterfaceCallback {

	private static final String TAG = "AlbumAdapter";

	private TextView title;
	private Context context;
	private List<Image> images;

	public AlbumAdapter() {
	}

	public AlbumAdapter(Context context, TextView title, List<Image> images) {
		this.context = context;
		this.images = images;
		this.title = title;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	// @Override
	public Object instantiateItem(ViewGroup container, int position) {// 考虑在线程中加载
		ImageView imageView = new ImageView(container.getContext());
		Image image = images.get(position);
		String imgPath = image.getImgPath();
		Bitmap mBitmap = BitmapFactory.decodeFile(imgPath);
		imageView.setImageBitmap(mBitmap);
		container.addView(imageView);
		// new LoadImageView(container,image).execute();
		return imageView;
	}

	private final class LoadImageView extends AsyncTask<Void, Void, ImageView> {

		private Image image;
		private ImageView imageView;
		private ViewGroup container;

		public LoadImageView(ViewGroup container, ImageView imageView,
				Image image) {
			this.image = image;
			this.imageView = imageView;
			this.container = container;
		}

		@Override
		protected ImageView doInBackground(Void... params) {
			Bitmap mBitmap = BitmapFactory.decodeFile(image.getImgPath());
			imageView.setImageBitmap(mBitmap);
			return imageView;
		}

		@Override
		protected void onPostExecute(ImageView result) {
			super.onPostExecute(result);
			container.addView(result);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		Log.i(TAG, "=====onPageScrollStateChanged====arg0:" + arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		Log.i(TAG, "=====onPageScrolled====arg0:" + arg0 + "*****arg2:" + arg2);
	}

	@Override
	public void onPageSelected(int arg0) {
		Log.i(TAG, "=====onPageSelected====arg0:" + arg0);
		curPos = arg0;
		title.setText((arg0 + 1) + "/" + images.size());
	}

	@Override
	public void deleteImg() {

	}

	private int curPos;

	public int getCurrentPosition() {
		return curPos;
	}
}