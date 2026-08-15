package com.camera.gps.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.maps.GoogleMap;

import java.util.concurrent.atomic.AtomicBoolean;

/** Takes a map snapshot only while its activity and rendering view are usable. */
public final class SafeMapSnapshot {

    private static final long SNAPSHOT_TIMEOUT_MS = 3000L;

    public interface Callback {
        void onSnapshot(@Nullable Bitmap bitmap);
    }

    private SafeMapSnapshot() {
    }

    public static boolean canCapture(Context context, @Nullable View mapView) {
        Activity activity = findActivity(context);
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return false;
        }
        if (activity instanceof LifecycleOwner
                && !((LifecycleOwner) activity).getLifecycle().getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {
            return false;
        }
        return mapView != null
                && mapView.isAttachedToWindow()
                && mapView.isLaidOut()
                && mapView.isShown()
                && mapView.getWidth() > 0
                && mapView.getHeight() > 0;
    }

    public static void capture(Context context,
                               @Nullable GoogleMap googleMap,
                               @Nullable View mapView,
                               Callback callback) {
        if (googleMap == null || !canCapture(context, mapView)) {
            callback.onSnapshot(null);
            return;
        }

        int width = mapView.getWidth();
        int height = mapView.getHeight();
        final Bitmap target;
        try {
            // Supplying a valid target prevents Maps from allocating a bitmap
            // from a rendering surface that may collapse while backgrounding.
            target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (RuntimeException | OutOfMemoryError error) {
            callback.onSnapshot(null);
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        AtomicBoolean completed = new AtomicBoolean(false);
        Runnable timeout = () -> {
            if (completed.compareAndSet(false, true)) {
                callback.onSnapshot(null);
            }
        };
        mainHandler.postDelayed(timeout, SNAPSHOT_TIMEOUT_MS);

        try {
            googleMap.snapshot(snapshot -> {
                if (!completed.compareAndSet(false, true)) {
                    return;
                }
                mainHandler.removeCallbacks(timeout);
                boolean usable = snapshot != null
                        && snapshot.getWidth() > 0
                        && snapshot.getHeight() > 0
                        && canCapture(context, mapView);
                callback.onSnapshot(usable ? snapshot : null);
            }, target);
        } catch (RuntimeException error) {
            mainHandler.removeCallbacks(timeout);
            if (completed.compareAndSet(false, true)) {
                callback.onSnapshot(null);
            }
        }
    }

    @Nullable
    private static Activity findActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == current) {
                break;
            }
            current = base;
        }
        return current instanceof Activity ? (Activity) current : null;
    }
}
