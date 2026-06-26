package com.camera.gps.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.BitmapOverlay;
import androidx.media3.effect.OverlayEffect;
import androidx.media3.effect.StaticOverlaySettings;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;

import com.camera.gps.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Collections;

@UnstableApi
public final class StampedGallerySaver {
    private static final String TAG = "StampedGallerySaver";

    private StampedGallerySaver() {
    }

    public static void savePhoto(Context context,
                                 File imageFile,
                                 View stampView,
                                 GoogleMap googleMap,
                                 View mapView) {
        if (context == null || imageFile == null || !imageFile.exists() || stampView == null
                || stampView.getWidth() == 0 || stampView.getHeight() == 0) {
            return;
        }

        if (googleMap != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            googleMap.snapshot(mapSnapshot -> savePhotoWithSnapshot(context, imageFile, stampView, mapSnapshot, mapView));
        } else {
            savePhotoWithSnapshot(context, imageFile, stampView, null, mapView);
        }
    }

    public static void saveVideo(Context context,
                                 File videoFile,
                                 View stampView,
                                 GoogleMap googleMap,
                                 View mapView) {
        if (context == null || videoFile == null || !videoFile.exists() || stampView == null
                || stampView.getWidth() == 0 || stampView.getHeight() == 0) {
            return;
        }

        if (googleMap != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            googleMap.snapshot(mapSnapshot -> saveVideoWithSnapshot(context, videoFile, stampView, mapSnapshot, mapView));
        } else {
            saveVideoWithSnapshot(context, videoFile, stampView, null, mapView);
        }
    }

    private static void savePhotoWithSnapshot(Context context,
                                              File imageFile,
                                              View stampView,
                                              Bitmap mapSnapshot,
                                              View mapView) {
        Bitmap source = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (source == null) {
            return;
        }

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap stampBitmap = StampedPhotoComposer.createStampBitmap(stampView, mapSnapshot, mapView);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        float scale = (float) result.getWidth() / stampBitmap.getWidth();
        int scaledHeight = Math.round(stampBitmap.getHeight() * scale);
        Bitmap scaledStamp = Bitmap.createScaledBitmap(stampBitmap, result.getWidth(), scaledHeight, true);
        canvas.drawBitmap(scaledStamp, 0f, result.getHeight() - scaledHeight, paint);

        Uri uri = createImageUri(context, imageFile.getName());
        if (uri == null) {
            return;
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                result.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            }
            publishPending(context, uri);
        } catch (Exception e) {
            deleteUri(context, uri);
            Log.e(TAG, "Failed to save stamped photo to Gallery", e);
        }
    }

    private static void saveVideoWithSnapshot(Context context,
                                              File videoFile,
                                              View stampView,
                                              Bitmap mapSnapshot,
                                              View mapView) {
        Bitmap stampBitmap = StampedPhotoComposer.createStampBitmap(stampView, mapSnapshot, mapView);
        Bitmap fullFrameOverlay = VideoStampShareHelper.createFullFrameOverlay(videoFile, stampBitmap);
        File outputFile = new File(context.getCacheDir(), "gallery_stamped_" + System.currentTimeMillis() + ".mp4");

        StaticOverlaySettings overlaySettings = new StaticOverlaySettings.Builder()
                .setAlphaScale(1f)
                .setBackgroundFrameAnchor(0f, 0f)
                .setOverlayFrameAnchor(0f, 0f)
                .setScale(1f, 1f)
                .build();
        BitmapOverlay overlay = BitmapOverlay.createStaticBitmapOverlay(fullFrameOverlay, overlaySettings);
        ImmutableList<Effect> videoEffects = ImmutableList.of(new OverlayEffect(ImmutableList.of(overlay)));
        EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(videoFile)))
                .setEffects(new Effects(Collections.emptyList(), videoEffects))
                .build();

        Transformer transformer = new Transformer.Builder(context.getApplicationContext())
                .addListener(new Transformer.Listener() {
                    @Override
                    public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            copyVideoToGallery(context, outputFile, videoFile.getName());
                            if (outputFile.exists()) {
                                outputFile.delete();
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Composition composition,
                                        @NonNull ExportResult exportResult,
                                        @NonNull ExportException exportException) {
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }
                        Log.e(TAG, "Failed to export stamped video for Gallery", exportException);
                    }
                })
                .build();

        transformer.start(editedMediaItem, outputFile.getAbsolutePath());
    }

    private static void copyVideoToGallery(Context context, File sourceFile, String displayName) {
        if (!sourceFile.exists()) {
            return;
        }

        Uri uri = createVideoUri(context, displayName);
        if (uri == null) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                deleteUri(context, uri);
                return;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            publishPending(context, uri);
        } catch (Exception e) {
            deleteUri(context, uri);
            Log.e(TAG, "Failed to save stamped video to Gallery", e);
        }
    }

    private static Uri createImageUri(Context context, String displayName) {
        ContentValues values = baseValues(context, displayName, "image/jpeg", Environment.DIRECTORY_PICTURES);
        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private static Uri createVideoUri(Context context, String displayName) {
        ContentValues values = baseValues(context, displayName, "video/mp4", Environment.DIRECTORY_MOVIES);
        return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    private static ContentValues baseValues(Context context, String displayName, String mimeType, String directory) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, directory + "/" + context.getString(R.string.app_name));
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        } else {
            File outputDir = new File(Environment.getExternalStoragePublicDirectory(directory), context.getString(R.string.app_name));
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            values.put(MediaStore.MediaColumns.DATA, new File(outputDir, displayName).getAbsolutePath());
        }
        return values;
    }

    private static void publishPending(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
    }

    private static void deleteUri(Context context, Uri uri) {
        try {
            context.getContentResolver().delete(uri, null, null);
        } catch (Exception ignored) {
        }
    }
}
