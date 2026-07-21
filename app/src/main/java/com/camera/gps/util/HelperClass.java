package com.camera.gps.util;

import android.content.Context;
import android.graphics.Typeface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.camera.gps.R;

public class HelperClass {

    private static final String DEFAULT_FONT = "Roboto Regular.ttf";

    public String setDateTime(String str, String str2, Context context) {
        return new SimpleDateFormat(str, Locale.getDefault()).format(Calendar.getInstance().getTime());
    }


    public void setBottomDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.getAttributes().windowAnimations = R.style.BottomDialogSlideAnimation;
        }
    }

    public void setFullscreenBottomDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.getInsetsController().hide(WindowInsets.Type.systemBars());
                window.getInsetsController().setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }

            window.getAttributes().windowAnimations = R.style.BottomDialogSlideAnimation;
        }
    }

    public Typeface getFontStyle(Context context, String str) {
        if (context == null) {
            return Typeface.DEFAULT;
        }

        String fontName = str == null || str.trim().isEmpty() ? DEFAULT_FONT : str;
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "font/" + fontName);
        try {
            if (file.exists()) {
                return Typeface.createFromFile(file);
            }
            return Typeface.createFromAsset(context.getAssets(), fontName);
        } catch (Exception ignored) {
            try {
                return Typeface.createFromAsset(context.getAssets(), DEFAULT_FONT);
            } catch (Exception fallbackError) {
                return Typeface.DEFAULT;
            }
        }
    }
}
