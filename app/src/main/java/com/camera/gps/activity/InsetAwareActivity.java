package com.camera.gps.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Applies system-bar insets to regular screens when edge-to-edge is enforced
 * by newer target SDKs. Full-screen camera and preview screens intentionally
 * do not extend this class.
 */
public abstract class InsetAwareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View content = findViewById(android.R.id.content);
        if (content instanceof android.view.ViewGroup && ((android.view.ViewGroup) content).getChildCount() > 0) {
            View root = ((android.view.ViewGroup) content).getChildAt(0);
            final int initialLeft = root.getPaddingLeft();
            final int initialTop = root.getPaddingTop();
            final int initialRight = root.getPaddingRight();
            final int initialBottom = root.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
                Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(
                        initialLeft + bars.left,
                        initialTop + bars.top,
                        initialRight + bars.right,
                        initialBottom + bars.bottom);
                return windowInsets;
            });
            ViewCompat.requestApplyInsets(root);
        }
    }
}
