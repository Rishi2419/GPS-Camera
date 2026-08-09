package com.camera.gps.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;

import androidx.cardview.widget.CardView;
import androidx.exifinterface.media.ExifInterface;

import com.camera.gps.R;
import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileOutputStream;

public final class StampedPhotoComposer {

    public interface Callback {
        void onComplete(boolean success);
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
            callback.onComplete(false);
            return;
        }

        if (googleMap != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            googleMap.snapshot(mapSnapshot -> {
                callback.onComplete(composeIntoImage(
                        imageFile, stampView, mapSnapshot, mapView));
            });
        } else {
            callback.onComplete(composeIntoImage(imageFile, stampView, null, mapView));
        }
    }

    public static Bitmap createStampBitmap(View stampView, Bitmap mapSnapshot, View mapView) {
        Bitmap mapBitmap = createMapBitmap(mapView, mapSnapshot);
        int originalMapVisibility = View.VISIBLE;
        if (mapBitmap != null) {
            originalMapVisibility = mapView.getVisibility();
            mapView.setVisibility(View.INVISIBLE);
        }

        Bitmap stampBitmap = Bitmap.createBitmap(stampView.getWidth(), stampView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas stampCanvas = new Canvas(stampBitmap);
        stampView.draw(stampCanvas);

        if (mapBitmap != null) {
            mapView.setVisibility(originalMapVisibility);

            int[] mapLocation = new int[2];
            int[] stampLocation = new int[2];
            mapView.getLocationOnScreen(mapLocation);
            stampView.getLocationOnScreen(stampLocation);

            int relativeX = mapLocation[0] - stampLocation[0];
            int relativeY = mapLocation[1] - stampLocation[1];

            // Replace the map rectangle with the active stamp background before
            // drawing its rounded snapshot. Clearing it makes the clipped map
            // corners transparent, exposing the photo/video below the stamp.
            Paint clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            stampCanvas.drawRect(
                    relativeX,
                    relativeY,
                    relativeX + mapView.getWidth(),
                    relativeY + mapView.getHeight(),
                    clearPaint
            );
            clearPaint.setXfermode(null);

            Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(getStampBackgroundColor(mapView));
            stampCanvas.drawRect(
                    relativeX,
                    relativeY,
                    relativeX + mapView.getWidth(),
                    relativeY + mapView.getHeight(),
                    backgroundPaint
            );

            Bitmap roundedMap = getRoundedMapBitmap(mapView, mapBitmap);
            stampCanvas.drawBitmap(roundedMap, relativeX, relativeY, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        }

        return stampBitmap;
    }

    private static Bitmap createMapBitmap(View mapView, Bitmap mapSnapshot) {
        if (mapView == null || mapView.getWidth() <= 0 || mapView.getHeight() <= 0) {
            return null;
        }

        if (mapSnapshot != null) {
            return Bitmap.createScaledBitmap(mapSnapshot, mapView.getWidth(), mapView.getHeight(), true);
        }

        Bitmap mapBitmap = Bitmap.createBitmap(mapView.getWidth(), mapView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mapBitmap);
        mapView.draw(canvas);
        return mapBitmap;
    }

    private static Bitmap getRoundedMapBitmap(View mapView, Bitmap bitmap) {
        float cornerRadius = getMapCornerRadius(mapView);
        if (cornerRadius <= 0f) {
            return bitmap;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        canvas.drawColor(Color.TRANSPARENT);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        paint.setXfermode(null);

        return output;
    }

    private static float getMapCornerRadius(View mapView) {
        if (mapView.getParent() instanceof CardView) {
            return ((CardView) mapView.getParent()).getRadius();
        }

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                mapView.getResources().getDisplayMetrics()
        );
    }

    private static int getStampBackgroundColor(View mapView) {
        View current = mapView;
        while (current != null) {
            if (current.getId() == R.id.rel_gps_stamp && current instanceof CardView) {
                ColorStateList color = ((CardView) current).getCardBackgroundColor();
                return color != null ? color.getDefaultColor() : Color.TRANSPARENT;
            }

            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return Color.TRANSPARENT;
    }

    private static boolean composeIntoImage(File imageFile, View stampView,
                                            Bitmap mapSnapshot, View mapView) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            orientation = new ExifInterface(imageFile.getAbsolutePath()).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) {
        }

        Bitmap decoded = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (decoded == null) {
            return false;
        }
        Bitmap source = applyExifOrientation(decoded, orientation);
        if (source != decoded) {
            decoded.recycle();
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
            boolean compressed = result.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            return compressed && imageFile.length() > 0L;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (scaledStamp != stampBitmap) {
                scaledStamp.recycle();
            }
            stampBitmap.recycle();
            result.recycle();
            source.recycle();
        }
    }

    private static Bitmap applyExifOrientation(Bitmap source, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270f);
                break;
            default:
                return source;
        }

        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
