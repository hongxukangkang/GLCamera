package com.meitu.glcamera.uiUtils;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.meitu.glcamera.R;
import com.meitu.glcamera.ui.MainGLSurfaceViewActivity;
import com.meitu.glcamera.widgets.FilterView;

public class MainGLSurfaceViewActivityUtils {
	
//	private static MainGLSurfaceViewActivity mActivity;
	
	public static void setTitleForFilterView(Context ctx,MainGLSurfaceViewActivity mActivity,LinearLayout filterViewContainer,int screenWidth) {

		FilterView original = (FilterView) filterViewContainer.findViewById(R.id.iv_original);
		FilterView gold = (FilterView) filterViewContainer.findViewById(R.id.iv_Gold);
		FilterView vista = (FilterView) filterViewContainer.findViewById(R.id.iv_Vista);
		FilterView iceland = (FilterView) filterViewContainer.findViewById(R.id.iv_Iceland);
		FilterView solaris = (FilterView) filterViewContainer.findViewById(R.id.iv_Solaris);

		FilterView iv_690 = (FilterView) filterViewContainer.findViewById(R.id.iv_690);
		FilterView acros = (FilterView) filterViewContainer.findViewById(R.id.iv_across);
		FilterView seine = (FilterView) filterViewContainer.findViewById(R.id.iv_seine);
		FilterView middy = (FilterView) filterViewContainer.findViewById(R.id.iv_middy);
		FilterView migrant = (FilterView) filterViewContainer.findViewById(R.id.iv_migrant);

		FilterView iv_669 = (FilterView) filterViewContainer.findViewById(R.id.iv_669);
		FilterView woodStock = (FilterView) filterViewContainer.findViewById(R.id.iv_woodStock);
		FilterView creamy = (FilterView) filterViewContainer.findViewById(R.id.iv_Creamy);
		FilterView silver = (FilterView) filterViewContainer.findViewById(R.id.iv_silver);

		original.setTitleForImgFilter("Original");
		gold.setTitleForImgFilter("Gold");
		vista.setTitleForImgFilter("Vista");
		iceland.setTitleForImgFilter("Iceland");
		solaris.setTitleForImgFilter("Solaris");

		iv_690.setTitleForImgFilter("690");
		acros.setTitleForImgFilter("Acros");
		seine.setTitleForImgFilter("Seine");
		middy.setTitleForImgFilter("Middy");
		migrant.setTitleForImgFilter("Migrant");

		iv_669.setTitleForImgFilter("669");
		woodStock.setTitleForImgFilter("WoodStock");
		creamy.setTitleForImgFilter("Creamy");
		silver.setTitleForImgFilter("Silver");

		iceland.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.palevioletred));
		original.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.transparent));
		solaris.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.goldenrod));
		gold.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.goldenrod));
		vista.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.plum));

		middy.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.chocolate));
		iv_690.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.orchid));
		acros.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.thistle));
		migrant.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.peru));
		seine.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.tan));

		woodStock.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.mediumvioletred));
		iv_669.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.indianred));
		creamy.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.darkkhaki));
		silver.getTitleView().setBackgroundColor(ctx.getResources().getColor(R.color.silver));

		resetImageViewSize(original, screenWidth);
		resetImageViewSize(gold, screenWidth);
		resetImageViewSize(vista, screenWidth);
		resetImageViewSize(iceland, screenWidth);
		resetImageViewSize(solaris, screenWidth);

		resetImageViewSize(iv_690, screenWidth);
		resetImageViewSize(acros, screenWidth);
		resetImageViewSize(seine, screenWidth);
		resetImageViewSize(middy, screenWidth);
		resetImageViewSize(migrant, screenWidth);

		resetImageViewSize(iv_669, screenWidth);
		resetImageViewSize(woodStock, screenWidth);
		resetImageViewSize(creamy, screenWidth);
		resetImageViewSize(silver, screenWidth);

		// set OnClickListener for widget
		gold.setOnClickListener(mActivity);
		vista.setOnClickListener(mActivity);
		iceland.setOnClickListener(mActivity);
		solaris.setOnClickListener(mActivity);
		original.setOnClickListener(mActivity);

		acros.setOnClickListener(mActivity);
		seine.setOnClickListener(mActivity);
		middy.setOnClickListener(mActivity);
		iv_690.setOnClickListener(mActivity);
		migrant.setOnClickListener(mActivity);

		iv_669.setOnClickListener(mActivity);
		creamy.setOnClickListener(mActivity);
		silver.setOnClickListener(mActivity);
		woodStock.setOnClickListener(mActivity);

	}
	
	private static void resetImageViewSize(FilterView original, int screenWidth) {
		LayoutParams lParams = original.getLayoutParams();
		lParams.width = screenWidth / 5;
		original.setLayoutParams(lParams);
	}

}
