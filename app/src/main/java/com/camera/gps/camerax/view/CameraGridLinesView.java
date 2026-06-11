package com.camera.gps.camerax.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.camera.gps.camerax.util.SharedPrefsSettings;

public class CameraGridLinesView extends View {
    private Paint paint;
    private int gridType;

    // Grid type constants
    public static final int GRID_OFF = 0;
    public static final int GRID_3X3 = 1;
    public static final int GRID_4X4 = 2;

    public CameraGridLinesView(Context context) {
        super(context);
        init();
        configPaint();
    }

    public CameraGridLinesView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
        configPaint();
    }

    public CameraGridLinesView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
        configPaint();
    }

    public CameraGridLinesView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init();
        configPaint();
    }

    private void init() {
        this.gridType = SharedPrefsSettings.getGridType(getContext());
        this.paint = new Paint();
    }

    private void configPaint() {
        this.paint.setAntiAlias(true);
        this.paint.setStrokeWidth(1.0f);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.argb(200, 255, 255, 255));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (gridType) {
            case GRID_3X3:
                draw3x3Grid(canvas);
                break;
            case GRID_4X4:
                draw4x4Grid(canvas);
                break;
            case GRID_OFF:
            default:
                break;
        }
    }

    public void setGridType(int gridType) {
        this.gridType = gridType;
        setVisibility(gridType == GRID_OFF ? View.GONE : View.VISIBLE);
        invalidate();
    }

    // Keep for backward compatibility
    public void showGrid(boolean show) {
        setGridType(show ? GRID_3X3 : GRID_OFF);
    }

    private void draw3x3Grid(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        // Draw vertical lines (2 lines for 3x3 grid)
        float verticalSpacing = width / 3.0f;
        for (int i = 1; i < 3; i++) {
            float x = i * verticalSpacing;
            canvas.drawLine(x, 0, x, height, paint);
        }

        // Draw horizontal lines (2 lines for 3x3 grid)
        float horizontalSpacing = height / 3.0f;
        for (int i = 1; i < 3; i++) {
            float y = i * horizontalSpacing;
            canvas.drawLine(0, y, width, y, paint);
        }
    }

    private void draw4x4Grid(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        // Draw vertical lines (3 lines for 4x4 grid)
        float verticalSpacing = width / 4.0f;
        for (int i = 1; i < 4; i++) {
            float x = i * verticalSpacing;
            canvas.drawLine(x, 0, x, height, paint);
        }

        // Draw horizontal lines (3 lines for 4x4 grid)
        float horizontalSpacing = height / 4.0f;
        for (int i = 1; i < 4; i++) {
            float y = i * horizontalSpacing;
            canvas.drawLine(0, y, width, y, paint);
        }
    }
}