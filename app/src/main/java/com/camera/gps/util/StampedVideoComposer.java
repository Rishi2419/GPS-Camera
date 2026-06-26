package com.camera.gps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

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

import com.camera.gps.database.entity.Photo;
import com.google.android.gms.maps.GoogleMap;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collections;

@UnstableApi
public final class StampedVideoComposer {

    public interface Callback {
        void onComplete();
    }

    private StampedVideoComposer() {
    }

    public static void writeStampedVideo(Context context,
                                         Photo photo,
                                         android.view.View stampView,
                                         GoogleMap googleMap,
                                         android.view.View mapView,
                                         Callback callback) {
        if (photo == null || photo.getImagePath() == null || stampView == null || stampView.getWidth() == 0 || stampView.getHeight() == 0) {
            callback.onComplete();
            return;
        }

        if (googleMap != null && mapView != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            googleMap.snapshot(mapSnapshot -> exportWithStamp(context, photo, StampedPhotoComposer.createStampBitmap(stampView, mapSnapshot, mapView), callback));
        } else {
            exportWithStamp(context, photo, StampedPhotoComposer.createStampBitmap(stampView, null, mapView), callback);
        }
    }

    private static void exportWithStamp(Context context, Photo photo, Bitmap stampBitmap, Callback callback) {
        File sourceFile = new File(photo.getImagePath());
        if (!sourceFile.exists()) {
            callback.onComplete();
            return;
        }

        File outputFile = new File(sourceFile.getParentFile(), "STAMPED_" + sourceFile.getName());
        Bitmap fullFrameOverlay = VideoStampShareHelper.createFullFrameOverlay(sourceFile, stampBitmap);
        StaticOverlaySettings overlaySettings = new StaticOverlaySettings.Builder()
                .setAlphaScale(1f)
                .setBackgroundFrameAnchor(0f, 0f)
                .setOverlayFrameAnchor(0f, 0f)
                .setScale(1f, 1f)
                .build();
        BitmapOverlay overlay = BitmapOverlay.createStaticBitmapOverlay(fullFrameOverlay, overlaySettings);

        ImmutableList<Effect> videoEffects = ImmutableList.of(new OverlayEffect(ImmutableList.of(overlay)));
        EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(sourceFile)))
                .setEffects(new Effects(Collections.emptyList(), videoEffects))
                .build();

        Transformer transformer = new Transformer.Builder(context.getApplicationContext())
                .addListener(new Transformer.Listener() {
                    @Override
                    public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                        replaceOriginal(sourceFile, outputFile);
                        new Handler(Looper.getMainLooper()).post(callback::onComplete);
                    }

                    @Override
                    public void onError(@NonNull Composition composition, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }
                        new Handler(Looper.getMainLooper()).post(callback::onComplete);
                    }
                })
                .build();

        transformer.start(editedMediaItem, outputFile.getAbsolutePath());
    }

    private static void replaceOriginal(File sourceFile, File outputFile) {
        if (!outputFile.exists()) {
            return;
        }
        if (sourceFile.delete()) {
            outputFile.renameTo(sourceFile);
        }
    }
}
