package com.meitu.glcamera.widgets;

import com.meitu.glcamera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by mtdiannao on 2015/4/16.
 */
public class FilterView extends LinearLayout {

    private View component;
    private TextView titleView;
    private ImageView imageView;

    public FilterView(Context context) {
        super(context);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        intialParam(context);
    }

    private void intialParam(Context context) {
        component = LayoutInflater.from(context).inflate(R.layout.widget_img_txt_layout, this);
        titleView = (TextView) component.findViewById(R.id.filterView_title);
        imageView = (ImageView) component.findViewById(R.id.filter_imgSample);
    }

    public void setTitleForImgFilter(String title) {
        titleView.setText(title);
    }

    public TextView getTitleView() {
        return titleView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setTitleBackgroundColor(int color_id) {
        titleView.setBackgroundColor(color_id);
    }
}
