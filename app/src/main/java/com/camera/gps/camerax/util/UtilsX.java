package com.camera.gps.camerax.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class UtilsX {

    public static void playSound(int i, Context context) {
        if (SharedPrefsSettings.getSoundStatus(context)) {
            MediaPlayer.create(context, i).start();
        }
    }

    public static String getAvailableInternalMemorySize(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return Formatter.formatFileSize(context, statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong());
    }

    public static String getTotalInternalMemorySize(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        return Formatter.formatFileSize(context, statFs.getBlockCountLong() * statFs.getBlockSizeLong());
    }

    public static int getDisplayRotation(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    public static void changeButtonIcon(final ImageButton imageButton, final int i, final Context context) {
        imageButton.animate().scaleY(0.5f).scaleX(0.5f).alpha(0.0f).setDuration(100L).withEndAction(() -> {
            imageButton.setImageDrawable(ContextCompat.getDrawable(context, i));
            imageButton.animate().scaleY(1.0f).scaleX(1.0f).alpha(1.0f).setDuration(100L);
        });
    }

    public static void rotateSwitchCameraButton(final ImageButton imageButton) {
        imageButton.animate().rotationY(180.0f).scaleX(1.3f).scaleY(1.3f).setDuration(300L).withEndAction(() -> {
            imageButton.setRotationY(0.0f);
            imageButton.animate().scaleX(1.0f).scaleY(1.0f);
        });
    }

    public static void rotateExpandButton(final ImageButton imageButton, boolean isExpanded) {
        float targetRotation = isExpanded ? 180.0f : 0.0f;
        imageButton.animate()
                .rotation(targetRotation)
                .setDuration(300L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void rotateSettingsButton(final ImageButton imageButton) {
        imageButton.animate()
                .rotation(imageButton.getRotation() + 90.0f)
                .setDuration(200L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }



    // FIXED: Menu now slides from/to below the top layer instead of above
    public static void slideMenuDown(final View menuLayout) {
        if (menuLayout.getVisibility() != View.VISIBLE) {
            menuLayout.setAlpha(0f);
            menuLayout.setTranslationY(-menuLayout.getHeight());
            menuLayout.setVisibility(View.VISIBLE);
        }

        menuLayout.animate()
                .translationY(0f)
                .alpha(1f) // fade in while sliding
                .setDuration(300L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void slideMenuUp(final View menuLayout) {
        menuLayout.animate()
                .translationY(-menuLayout.getHeight())
                .alpha(0f) // fade out with motion
                .setDuration(300L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    menuLayout.setVisibility(View.GONE);
                    menuLayout.setAlpha(1f); // reset for next time
                    menuLayout.setTranslationY(0); // reset position for next slide down
                })
                .start();
    }


    public static void scaleView(View view, float f) {
        view.animate().scaleX(f).scaleY(f).setDuration(150L);
    }

    public static void toggleRecordingIndicator(View imgCenter, View imgVideo, int tabPosition) {
        if (tabPosition == 1) {
            // 📹 Video mode – show red icon with animation
            imgCenter.setVisibility(View.INVISIBLE);

            imgVideo.setVisibility(View.VISIBLE);
            imgVideo.setScaleX(0f);
            imgVideo.setScaleY(0f);
            scaleView(imgVideo, 1.0f);

        } else if (tabPosition == 0) {
            // 📸 Photo mode – show white icon with animation
            imgVideo.setVisibility(View.GONE);
            imgVideo.setScaleX(0f);
            imgVideo.setScaleY(0f);

            imgCenter.setVisibility(View.VISIBLE);
            imgCenter.setScaleX(0f);
            imgCenter.setScaleY(0f);
            scaleView(imgCenter, 1.0f);

        } else {
            // Hide both for other modes
            imgVideo.setVisibility(View.GONE);
            imgVideo.setScaleX(0f);
            imgVideo.setScaleY(0f);

            imgCenter.setVisibility(View.GONE);
            imgCenter.setScaleX(0f);
            imgCenter.setScaleY(0f);
        }
    }

    public static void setAlpha(View view, float f) {
        view.animate().alpha(f).setDuration(150L);
    }

    public static void animateBtnTakePhoto(final ImageButton imageButton) {
        imageButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50L).withEndAction(() -> imageButton.animate().scaleX(1.0f).scaleY(1.0f));
    }

    public static void animateImgFocus(final ImageView imageView) {
        imageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150L).alpha(0.0f).withEndAction(() -> {
            imageView.setVisibility(View.INVISIBLE);
            imageView.setScaleY(1.3f);
            imageView.setScaleX(1.3f);
            imageView.setAlpha(1.0f);
        });
    }

    public static void moveImgFocus(ImageView imageView, float f, float f2) {
        imageView.setTranslationX(f - 156.0f);
        imageView.setTranslationY(f2 + 156.0f);
        imageView.setVisibility(View.VISIBLE);
    }

    // NEW: Utility methods for zoom functionality
    public static void updateZoomButtons(TextView btn1x, TextView btn2x, TextView btn3x, float currentZoomRatio) {
        btn1x.setSelected(false);
        btn2x.setSelected(false);
        btn3x.setSelected(false);

        if (currentZoomRatio <= 1.5f) {
            btn1x.setSelected(true);
        } else if (currentZoomRatio <= 2.5f) {
            btn2x.setSelected(true);
        } else {
            btn3x.setSelected(true);
        }
    }


    private static void resetZoomButtonStyle(View button) {
        button.setAlpha(0.7f);
        button.setScaleX(1.0f);
        button.setScaleY(1.0f);
    }

    private static void setActiveZoomButtonStyle(View button) {
        button.setAlpha(1.0f);
        button.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150L);
    }

    public static void animateZoomButton(View button) {
        button.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100L)
                .withEndAction(() -> button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100L));
    }
}