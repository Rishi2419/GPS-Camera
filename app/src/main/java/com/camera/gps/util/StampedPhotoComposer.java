package com.camera.gps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileOutputStream;

public final class StampedPhotoComposer {

    public interface Callback {
        void onComplete();
    }

    private StampedPhotoComposer() {
    }

    public static void writeStampedPhoto(Context context,
                                         File imageFile,
                                         View stampView,
                                         GoogleMap googleMap,
                                         View mapView,
                                         Callback callback) {
        if (imageFile == null || stampView == null || stampView.getWidth() == 0 || stampView.getHeight() == 0) {
            callback.onComplete();
            return;
        }

        if (googleMap != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            googleMap.snapshot(mapSnapshot -> {
                composeIntoImage(imageFile, stampView, mapSnapshot, mapView);
                callback.onComplete();
            });
        } else {
            composeIntoImage(imageFile, stampView, null, mapView);
            callback.onComplete();
        }
    }

    public static Bitmap createStampBitmap(View stampView, Bitmap mapSnapshot, View mapView) {
        Bitmap stampBitmap = Bitmap.createBitmap(stampView.getWidth(), stampView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas stampCanvas = new Canvas(stampBitmap);
        stampView.draw(stampCanvas);

        if (mapSnapshot != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            int[] mapLocation = new int[2];
            int[] stampLocation = new int[2];
            mapView.getLocationOnScreen(mapLocation);
            stampView.getLocationOnScreen(stampLocation);

            int relativeX = mapLocation[0] - stampLocation[0];
            int relativeY = mapLocation[1] - stampLocation[1];
            Bitmap scaledMap = Bitmap.createScaledBitmap(mapSnapshot, mapView.getWidth(), mapView.getHeight(), true);
            stampCanvas.drawBitmap(scaledMap, relativeX, relativeY, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        }

        return stampBitmap;
    }

    private static void composeIntoImage(File imageFile, View stampView, Bitmap mapSnapshot, View mapView) {
        Bitmap source = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (source == null) {
            return;
        }

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap stampBitmap = createStampBitmap(stampView, mapSnapshot, mapView);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        float scale = (float) result.getWidth() / stampBitmap.getWidth();
        int scaledHeight = Math.round(stampBitmap.getHeight() * scale);
        Bitmap scaledStamp = Bitmap.createScaledBitmap(stampBitmap, result.getWidth(), scaledHeight, true);
        canvas.drawBitmap(scaledStamp, 0f, result.getHeight() - scaledHeight, paint);

        try (FileOutputStream fos = new FileOutputStream(imageFile, false)) {
            result.compress(Bitmap.CompressFormat.JPEG, 95, fos);
        } catch (Exception ignored) {
        }
    }
}
