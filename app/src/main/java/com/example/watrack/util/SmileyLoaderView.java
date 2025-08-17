package com.example.watrack.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SmileyLoaderView extends View {
    private Paint circlePaint, dotPaint, arcPaint;
    private float dotOffset = 0; // Offset for the dots' horizontal movement
    private float arcSweepAngle = 0; // Current sweep angle for the arc
    private boolean isAnimating = true;

    private int outerRadius;

    public SmileyLoaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint for the background circle
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(0xFFFFFFFF); // White color
        circlePaint.setStyle(Paint.Style.FILL);

        // Paint for the moving dots
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(0xFFFFD700); // Gold color
        dotPaint.setStyle(Paint.Style.FILL);

        // Paint for the arc
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(0xFFFFD700); // Gold color
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(15);

        // Start the animation
        startAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        outerRadius = Math.min(width, height) / 2 - 20; // Radius of the outer circle
        int innerRadius = outerRadius / 2; // Inner radius for the arc and dots
        int cx = width / 2; // Center X
        int cy = height / 2; // Center Y

        // Draw the static outer circle
        canvas.drawCircle(cx, cy, outerRadius, circlePaint);

        int numberOfDots = 3; // Number of dots to place
        for (int i = 0; i < numberOfDots; i++) {
            // Calculate dot positions, so that they wrap around
            float offsetForDot = (dotOffset + i * ((float) outerRadius / (numberOfDots - 1))) % (outerRadius); // Ensures wrapping behavior

            // First dot starts at the leftmost side and moves towards right
            // Second dot starts at the center
            // Third dot starts at the rightmost side and moves left
            float x = cx - (float) outerRadius / 2 + offsetForDot;

            float y = (float) (cy); // Keep the vertical position fixed

            // Draw the dot
            canvas.drawCircle(x, y, 10, dotPaint);
        }

        // Draw the arc
        RectF arcBounds = new RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius);
        float startAngle = 240 + 120; // Start angle at the 3rd dot (bottom-right)
        canvas.drawArc(arcBounds, startAngle, arcSweepAngle, false, arcPaint);
    }

    public void startAnimation() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            long duration = 3000;
            while (isAnimating ) {
                // Increment the horizontal offset for the dots (slower movement)
                dotOffset += 2; // Increment offset for sliding
                // The dotOffset now wraps around as it exceeds the outer radius

                // Increment the arc sweep angle
                arcSweepAngle += 5;
                if (arcSweepAngle > 180) { // Reset after completing 180 degrees
                    arcSweepAngle = 0;
                }

                postInvalidate(); // Trigger a redraw
                try {
                    Thread.sleep(50); // Animation frame delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopAnimation() {
        isAnimating = false;
        // Optionally, notify the view to stop drawing or trigger any next steps
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAnimating = false; // Stop animation when the view is detached
    }
}

