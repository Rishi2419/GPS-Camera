package com.camera.gps.camerax.controller;

import static com.camera.gps.MyApplication.context;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.ExposureState;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.view.PreviewView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.camera.gps.R;
import com.camera.gps.camerax.util.SharedPrefsSettings;
import com.camera.gps.camerax.util.UtilsX;

public class MainController {

    public static ArrayList<String> getTitles() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(context.getString(R.string.photo));
        arrayList.add(context.getString(R.string.video));
        return arrayList;
    }

    public static ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> arrayList = new ArrayList<>();
        for (String ignored : getTitles()) {
            arrayList.add(new Fragment());
        }
        return arrayList;
    }

    public static void startChronometer(Chronometer chronometer) {
        if (chronometer != null) {
            chronometer.setVisibility(View.VISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        }
    }

    public static void stopChronometer(Chronometer chronometer) {
        if (chronometer != null) {
            chronometer.setVisibility(View.INVISIBLE);
            chronometer.stop();
        }
    }

    public static void startRecAnimation(ImageView imageView, Animation animation) {
        if (imageView == null || animation == null) {
            return;
        }
        imageView.startAnimation(animation);
    }

    public static void stopRecAnimation(Animation animation) {
        if (animation != null) {
            animation.cancel();
        }
    }

    public static ScaleGestureDetector.SimpleOnScaleGestureListener getZoomGesture(final Camera camera) {
        return new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                MainController.setZoom(camera, scaleGestureDetector.getScaleFactor());
                return true;
            }
        };
    }

    public static void setZoom(Camera camera, float f) {
        ZoomState value;
        if (camera == null || (value = camera.getCameraInfo().getZoomState().getValue()) == null) {
            return;
        }
        camera.getCameraControl().setZoomRatio(value.getZoomRatio() * f);
    }

    public static void tapToFocus(Camera camera, PreviewView previewView, ImageView imageView, MotionEvent motionEvent) {
        if (camera == null || previewView == null || motionEvent.getAction() != 1) {
            return;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(new SurfaceOrientedMeteringPointFactory(previewView.getWidth(), previewView.getHeight()).createPoint(x, y), FocusMeteringAction.FLAG_AF).setAutoCancelDuration(2L, TimeUnit.SECONDS).build());
        UtilsX.moveImgFocus(imageView, x, y);
        UtilsX.animateImgFocus(imageView);
    }
    private static void setPreviewScaleTypeButtonColor(int i, TextView textView, TextView textView2, TextView textView3, TextView textView4, Context context) {
        int color = ResourcesCompat.getColor(context.getResources(), R.color.blue_primary, null);
        textView.setTextColor(-1);
        textView2.setTextColor(-1);
        textView3.setTextColor(-1);
        textView4.setTextColor(-1);
        if (i == 1) {
            textView.setTextColor(color);
        } else if (i == 3) {
            textView2.setTextColor(color);
        } else if (i == 4) {
            textView3.setTextColor(color);
        } else if (i != 5) {
        } else {
            textView4.setTextColor(color);
        }
    }

    private static void setBtnExposureColor(int i,
                                            TextView textView, TextView textView2, TextView textView3,
                                            TextView textView4, TextView textView5, TextView textView6,
                                            TextView textView7, TextView textView8, TextView textView9,
                                            TextView arrowView, TextView arrowView2, TextView arrowView3,
                                            TextView arrowView4, TextView arrowView5, TextView arrowView6,
                                            TextView arrowView7, TextView arrowView8, TextView arrowView9,
                                            Context context) {
        int selectedColor = ResourcesCompat.getColor(context.getResources(), R.color.blue_primary, null);
        int defaultColor = ResourcesCompat.getColor(context.getResources(), android.R.color.black, null);

        // Reset all text colors to default (BLACK)
        textView.setTextColor(defaultColor);
        textView2.setTextColor(defaultColor);
        textView3.setTextColor(defaultColor);
        textView4.setTextColor(defaultColor);
        textView5.setTextColor(defaultColor);
        textView6.setTextColor(defaultColor);
        textView7.setTextColor(defaultColor);
        textView8.setTextColor(defaultColor);
        textView9.setTextColor(defaultColor);

        // Hide all arrows
        arrowView.setVisibility(View.INVISIBLE);
        arrowView2.setVisibility(View.INVISIBLE);
        arrowView3.setVisibility(View.INVISIBLE);
        arrowView4.setVisibility(View.INVISIBLE);
        arrowView5.setVisibility(View.INVISIBLE);
        arrowView6.setVisibility(View.INVISIBLE);
        arrowView7.setVisibility(View.INVISIBLE);
        arrowView8.setVisibility(View.INVISIBLE);
        arrowView9.setVisibility(View.INVISIBLE);

        // Highlight the selected one in BLUE and show corresponding arrow
        switch (i) {
            case -4:
                textView.setTextColor(selectedColor);
                arrowView.setVisibility(View.VISIBLE);
                break;
            case -3:
                textView2.setTextColor(selectedColor);
                arrowView2.setVisibility(View.VISIBLE);
                break;
            case -2:
                textView3.setTextColor(selectedColor);
                arrowView3.setVisibility(View.VISIBLE);
                break;
            case -1:
                textView4.setTextColor(selectedColor);
                arrowView4.setVisibility(View.VISIBLE);
                break;
            case 0:
                textView5.setTextColor(selectedColor);
                arrowView5.setVisibility(View.VISIBLE);
                break;
            case 1:
                textView6.setTextColor(selectedColor);
                arrowView6.setVisibility(View.VISIBLE);
                break;
            case 2:
                textView7.setTextColor(selectedColor);
                arrowView7.setVisibility(View.VISIBLE);
                break;
            case 3:
                textView8.setTextColor(selectedColor);
                arrowView8.setVisibility(View.VISIBLE);
                break;
            case 4:
                textView9.setTextColor(selectedColor);
                arrowView9.setVisibility(View.VISIBLE);
                break;
        }
    }
    public static void hideLayoutTopView(View view, LinearLayout imageButton, Resources resources) {
        if (view.getVisibility() == View.VISIBLE) {
            toggleVisibilityView(view, imageButton, resources);
        }
    }

    public static void toggleVisibilityView(View view, LinearLayout imageButton, Resources resources) {
        boolean z = view.getVisibility() == View.GONE;
        view.setVisibility(z ? View.VISIBLE : View.GONE);
        //setBtnColor(z, imageButton, resources);
    }
    public static void setExposureCompensation(int i, Camera camera, LinearLayout imageButton,
                                               TextView textView, TextView textView2, TextView textView3,
                                               TextView textView4, TextView textView5, TextView textView6,
                                               TextView textView7, TextView textView8, TextView textView9,
                                               TextView arrowView, TextView arrowView2, TextView arrowView3,
                                               TextView arrowView4, TextView arrowView5, TextView arrowView6,
                                               TextView arrowView7, TextView arrowView8, TextView arrowView9,
                                               Context context) {
        if (camera != null) {
            ExposureState exposureState = camera.getCameraInfo().getExposureState();
            if (!exposureState.isExposureCompensationSupported()) {
                imageButton.setVisibility(View.GONE);
                return;
            }
            if (!exposureState.getExposureCompensationRange().contains(i)) {
                return;
            }
            camera.getCameraControl().setExposureCompensationIndex(i);
            setBtnExposureColor(i, textView, textView2, textView3, textView4, textView5, textView6,
                    textView7, textView8, textView9, arrowView, arrowView2, arrowView3,
                    arrowView4, arrowView5, arrowView6, arrowView7, arrowView8, arrowView9, context);
        }
    }
}
