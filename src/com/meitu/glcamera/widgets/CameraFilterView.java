package com.meitu.glcamera.widgets;

import com.meitu.glcamera.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

/**
 * Created by mtdiannao on 2015/4/16.
 */
public class CameraFilterView extends HorizontalScrollView {

    private Paint mPaint;
    private Context context;

    private ImageView iv_original;
    private ImageView iv_Gold;
    private ImageView iv_Vista;
    private ImageView iv_Iceland;
    private ImageView iv_soloris;

    private ImageView iv_690;
    private ImageView iv_Acros;
    private ImageView iv_seine;
    private ImageView iv_middy;
    private ImageView iv_migrant;

    private ImageView iv_669;
    private ImageView iv_woodStock;
    private ImageView iv_Creamy;
    private ImageView iv_silver;

    public CameraFilterView(Context context) {
        super(context);
        initialParams(context);
    }

    public CameraFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialParams(context);
    }

    private void initialParams(Context context) {
        this.context = context;
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.0f);
    }

    @Override
    public void onDraw(Canvas canvas){
        inflateFilterViews();
    }

    private void inflateFilterViews() {
        View container = LayoutInflater.from(context).inflate(R.layout.filterview_container, null);
    }

    private void addFilterView() {
        iv_original = new ImageView(context);
        iv_Gold = new ImageView(context);
        iv_Vista = new ImageView(context);
        iv_Iceland = new ImageView(context);
        iv_soloris = new ImageView(context);

        iv_690 = new ImageView(context);
        iv_Acros = new ImageView(context);
        iv_seine = new ImageView(context);
        iv_middy = new ImageView(context);
        iv_migrant = new ImageView(context);

        iv_669 = new ImageView(context);
        iv_woodStock = new ImageView(context);
        iv_Creamy = new ImageView(context);
        iv_silver = new ImageView(context);

        addView(iv_original);
        addView(iv_Gold);
        addView(iv_Vista);
        addView(iv_Iceland);
        addView(iv_soloris);

        addView(iv_690);
        addView(iv_Acros);
        addView(iv_seine);
        addView(iv_middy);
        addView(iv_migrant);

        addView(iv_669);
        addView(iv_woodStock);
        addView(iv_Creamy);
        addView(iv_silver);
    }
}
