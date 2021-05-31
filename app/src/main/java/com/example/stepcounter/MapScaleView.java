package com.example.stepcounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class MapScaleView extends View {
    private static final float SCALE_WIDTH = 0.3f;
    private static final int VERTICAL_HEIGHT = 10;
    private static final int TEXT_SIZE = 40;

    private Paint mPaint;
    private String text = "10m";

    public MapScaleView(Context context) {
        this(context, null);
        initView();
    }

    public MapScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2f);
        mPaint.setTextSize(TEXT_SIZE);
        setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawScaleLines(canvas);
        this.writeScaleText(canvas);
    }

    private void drawScaleLines(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        canvas.drawLine(0, this.getTop() + (int) (VERTICAL_HEIGHT * 0.5), 0, this.getTop() + (int) (VERTICAL_HEIGHT * 1.5), mPaint);
        canvas.drawLine(0, this.getTop() + VERTICAL_HEIGHT, (int) (SCALE_WIDTH * 1000), this.getTop() + VERTICAL_HEIGHT, mPaint);
        canvas.drawLine((int) (SCALE_WIDTH * 1000), this.getTop() + (int) (VERTICAL_HEIGHT * 0.5), (int) (SCALE_WIDTH * 1000), this.getTop() + (int) (VERTICAL_HEIGHT * 1.5), mPaint);
    }

    private void writeScaleText(Canvas canvas) {
        mPaint.setColor(Color.GREEN);
        canvas.drawText(text + TEXT_SIZE, 10, this.getTop(), mPaint);
    }

    public void update(String scaleSizeText) {
        this.text = scaleSizeText;
        this.invalidate();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int resultWidth;
//        if (widthMode == MeasureSpec.EXACTLY) {
//            resultWidth = widthSize;
//        } else {
//            resultWidth = SCALE_WIDTH;
//            if (widthMode == MeasureSpec.AT_MOST) {
//                resultWidth = Math.min(resultWidth, widthSize);
//            }
//        }
//        int heightSize = getHeightSize(heightMeasureSpec) + 20;
//        setMeasuredDimension(resultWidth, heightSize);
//    }

//    private int getHeightSize(int heightMeasureSpec) {
//        int mode = MeasureSpec.getMode(heightMeasureSpec);
//        int height = 0;
//        switch (mode) {
//            case MeasureSpec.AT_MOST:
//                height = TEXT_SIZE + scaleSpaceText + scaleHeight;
//                break;
//            case MeasureSpec.EXACTLY: {
//                height = MeasureSpec.getSize(heightMeasureSpec);
//                break;
//            }
//            case MeasureSpec.UNSPECIFIED: {
//                height = Math.max(TEXT_SIZE + scaleSpaceText + scaleHeight, MeasureSpec.getSize(heightMeasureSpec));
//                break;
//            }
//        }
//        return height;
//    }

}