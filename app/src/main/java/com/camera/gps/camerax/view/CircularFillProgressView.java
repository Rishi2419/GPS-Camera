package com.camera.gps.camerax.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class CircularFillProgressView extends View {
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF fillBounds = new RectF();
    private ValueAnimator fillAnimator;
    private float sweepAngle;

    public CircularFillProgressView(Context context) {
        this(context, null);
    }

    public CircularFillProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularFillProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fillPaint.setColor(Color.argb(128, 0, 0, 0));
        fillPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        fillBounds.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);
        canvas.drawArc(fillBounds, -90f, sweepAngle, true, fillPaint);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startFilling();
        } else {
            stopFilling();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopFilling();
        super.onDetachedFromWindow();
    }

    private void startFilling() {
        stopFilling();
        fillAnimator = ValueAnimator.ofFloat(0f, 360f);
        fillAnimator.setDuration(1200L);
        fillAnimator.setRepeatCount(0);
        fillAnimator.setInterpolator(new LinearInterpolator());
        fillAnimator.addUpdateListener(animation -> {
            sweepAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        fillAnimator.start();
    }

    private void stopFilling() {
        if (fillAnimator != null) {
            fillAnimator.cancel();
            fillAnimator = null;
        }
        sweepAngle = 0f;
        invalidate();
    }
}
