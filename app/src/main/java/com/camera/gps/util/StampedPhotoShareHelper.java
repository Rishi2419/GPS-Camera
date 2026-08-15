package com.camera.gps.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import androidx.core.content.FileProvider;

import com.camera.gps.R;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.io.FileOutputStream;

public final class StampedPhotoShareHelper {

    private StampedPhotoShareHelper() {
    }

    public static void shareImageWithStamp(
            Context context,
            SupportMapFragment mapFragment,
            View previewFrame,
            View mapViewContainer,
            float mapCornerRadiusPx
    ) {
        if (mapFragment != null && SafeMapSnapshot.canCapture(context, mapViewContainer)) {
            mapFragment.getMapAsync(googleMap -> SafeMapSnapshot.capture(
                    context,
                    googleMap,
                    mapViewContainer,
                    mapSnapshot -> createAndShareImage(
                            context,
                            previewFrame,
                            mapViewContainer,
                            mapSnapshot,
                            mapCornerRadiusPx)));
        } else {
            createAndShareImage(context, previewFrame, mapViewContainer, null, mapCornerRadiusPx);
        }
    }

    private static void createAndShareImage(
            Context context,
            View previewFrame,
            View mapViewContainer,
            Bitmap mapSnapshot,
            float mapCornerRadiusPx
    ) {
        try {
            Bitmap mapBitmap = createMapBitmap(mapViewContainer, mapSnapshot);
            int originalMapVisibility = View.VISIBLE;
            if (mapBitmap != null) {
                originalMapVisibility = mapViewContainer.getVisibility();
                mapViewContainer.setVisibility(View.INVISIBLE);
            }

            Bitmap containerBitmap = viewToImage(previewFrame);
            if (mapBitmap != null) {
                mapViewContainer.setVisibility(originalMapVisibility);
            }
            Canvas canvas = new Canvas(containerBitmap);

            if (mapBitmap != null) {
                int[] mapLocation = new int[2];
                int[] containerLocation = new int[2];

                mapViewContainer.getLocationInWindow(mapLocation);
                previewFrame.getLocationInWindow(containerLocation);

                int relativeX = mapLocation[0] - containerLocation[0];
                int relativeY = mapLocation[1] - containerLocation[1];

                Bitmap roundedMap = getRoundedCornerBitmap(mapBitmap, mapCornerRadiusPx);
                canvas.drawBitmap(roundedMap, relativeX, relativeY, null);
            }

            File sharedFile = new File(context.getCacheDir(), "shared_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(sharedFile);
            containerBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            shareFile(context, sharedFile);
        } catch (Exception e) {
            if (mapViewContainer != null) {
                mapViewContainer.setVisibility(View.VISIBLE);
            }
            e.printStackTrace();
            Constant.Companion.showToast(context, context.getString(R.string.error_in_creating_file));
        }
    }

    private static Bitmap createMapBitmap(View mapViewContainer, Bitmap mapSnapshot) {
        if (mapViewContainer == null || mapViewContainer.getWidth() <= 0 || mapViewContainer.getHeight() <= 0) {
            return null;
        }

        if (mapSnapshot != null) {
            return Bitmap.createScaledBitmap(
                    mapSnapshot,
                    mapViewContainer.getWidth(),
                    mapViewContainer.getHeight(),
                    false
            );
        }

        return viewToImage(mapViewContainer);
    }

    private static Bitmap viewToImage(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();
            view.layout(0, 0, width, height);
        }

        Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);

        Drawable background = view.getBackground();
        if (background != null) {
            background.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        view.draw(canvas);
        return returnedBitmap;
    }

    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float cornerRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private static void shareFile(Context context, File file) {
        Uri uriForFile = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        // App link text is disabled until the app is published.
        // intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_name) + "\n\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName());
        intent.setType("image/jpeg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Image!"));
    }
}
