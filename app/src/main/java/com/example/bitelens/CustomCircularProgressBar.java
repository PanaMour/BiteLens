package com.example.bitelens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class CustomCircularProgressBar extends View {

    private RectF mRectF;
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private float mSweepAngle;
    private int bColor;
    private int fColor;

    public CustomCircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRectF = new RectF();
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(30);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bColor = getContext().getResources().getColor(R.color.complimentary);
            fColor = getContext().getResources().getColor(R.color.purple_200);
        }
        mBackgroundPaint.setColor(bColor);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(30);
        mProgressPaint.setColor(fColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();

        mRectF.set(15, 15, width - 15, height - 15);

        canvas.drawArc(mRectF, 0, 360, false, mBackgroundPaint);
        canvas.drawArc(mRectF, -90, mSweepAngle, false, mProgressPaint);
    }

    public void setProgress(float progress) {
        mSweepAngle = 360 * progress / 100;
        invalidate();
    }
}
