package com.meitu.glcamera.widgets;

import com.meitu.glcamera.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mtdiannao on 2015/4/15.
 */
public class SquareView extends View {

    private Paint mPaint;
    Context context;

    public SquareView(Context context) {
        super(context);
        initialData(context);
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialData(context);
    }

    private void initialData(Context context) {
        this.context = context;
        mPaint = new Paint();
//        mPaint.setColor(Color.WHITE);
        mPaint.setColor(getResources().getColor(R.color.lightcyan));
//        mPaint.setColor(getResources().getColor(R.color.blanchedalmond));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.5f);
    }

    private int width;
    private int height;

    public void setScreenWidth(int width) {
        this.width = width;
    }

    public void setScreenHeight(int height) {
        this.height = height;
    }

    @Override
    public void onDraw(Canvas canvas) {
//    	 canvas.drawLine(0, 0,100,100, mPaint);
    	
        canvas.drawLine(0, height / 3, width, height / 3, mPaint);
        canvas.drawLine(0, 2 * height / 3, width, 2 * height / 3, mPaint);

        canvas.drawLine(width / 3, 0, width / 3, height, mPaint);
        canvas.drawLine(2 * width / 3, 0, 2 * width / 3, height, mPaint);
    }
}