package com.camera.gps.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.camera.gps.R;

public final class StampBackgroundUtils {

    private StampBackgroundUtils() {
    }

    public static void applyRoundedGlassColor(Context context, int color, View... views) {
        float cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                context.getResources().getDisplayMetrics()
        );
        for (View view : views) {
            if (view == null) {
                continue;
            }

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setColor(color);
            background.setCornerRadius(cornerRadius);
            view.setBackground(background);
        }
    }

    public static void applyRoundedDefaultGlass(Context context, View... views) {
        applyRoundedGlassColor(context, ContextCompat.getColor(context, R.color.bg_glass), views);
    }
}
