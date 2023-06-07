package com.example.bitelens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Collections;
import java.util.List;

public class BarGraphView extends View {
    private Paint paint;
    private List<Integer> data;
    private int maxValue;

    public BarGraphView(Context context, List<Integer> data) {
        super(context);
        this.data = data;
        paint = new Paint();
        maxValue = Collections.max(data);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data != null) {
            int width = getWidth() / data.size();  // The width of each bar

            // Set the paint color for drawing the axis
            paint.setColor(android.graphics.Color.BLACK);
            paint.setStrokeWidth(2);
            paint.setTextSize(30); // Set a text size for the labels.

            // Draw y-axis, note that it starts at getHeight() - 200 now, not at 0.
            canvas.drawLine(100, getHeight() - 200, 100, 0, paint);

            // Draw x-axis
            canvas.drawLine(100, getHeight() - 200, getWidth(), getHeight() - 200, paint);

            if (maxValue == 0) {
                // If maxValue is zero, display a "No Data" message.
                paint.setTextSize(60); // Set a bigger text size for the no data message.
                paint.setTextAlign(Paint.Align.CENTER); // Center the text.
                canvas.drawText("No Data", getWidth() / 2, getHeight() / 2, paint);
                return; // No need to do more drawing, so return here.
            }

            // Draw y-axis labels
            for (int i = 500; i <= maxValue; i += 500) {
                int textHeight = (int) (getHeight() - 200 - ((i / (float) maxValue) * (getHeight() - 200)));
                canvas.drawText(String.valueOf(i), 30, textHeight + 15, paint); // Adjusted x-coordinate from 50 to 30
            }

            // Set the paint color for drawing the bars
            paint.setColor(Color.DKGRAY);

            for (int i = 0; i < data.size(); i++) {
                int height = (getHeight() - 200) * data.get(i) / maxValue;  // The height of the bar
                canvas.drawRect(100 + (i * width), getHeight() - height - 200, 100 + ((i + 1) * width), getHeight() - 200, paint);
            }
        }
    }

}
