package com.camera.gps.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
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
import com.camera.gps.database.entity.Photo;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@UnstableApi
public final class VideoStampShareHelper {

    private static final Set<Transformer> ACTIVE_TRANSFORMERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private VideoStampShareHelper() {
    }

    public static void shareVideoWithStamp(Context context, Photo photo, Bitmap stampBitmap) {
        if (stampBitmap == null || stampBitmap.getWidth() == 0 || stampBitmap.getHeight() == 0) {
            shareVideoOnly(context, photo);
            return;
        }

        File sourceFile = new File(photo.getImagePath());
        if (!sourceFile.exists()) {
            shareVideoOnly(context, photo);
            return;
        }

        Context appContext = context.getApplicationContext();
        Toast.makeText(context, R.string.preparing_video, Toast.LENGTH_SHORT).show();

        File outputFile = new File(context.getCacheDir(), "shared_video_stamp_" + System.currentTimeMillis() + ".mp4");
        Bitmap fullFrameOverlay = createFullFrameOverlay(sourceFile, stampBitmap);
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

        final Transformer[] transformerHolder = new Transformer[1];
        Transformer transformer = new Transformer.Builder(appContext)
                .setPortraitEncodingEnabled(true)
                .addListener(new Transformer.Listener() {
                    @Override
                    public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                        ACTIVE_TRANSFORMERS.remove(transformerHolder[0]);
                        new Handler(Looper.getMainLooper()).post(() -> shareFile(context, outputFile, "video/mp4", context.getString(R.string.share_video)));
                    }

                    @Override
                    public void onError(@NonNull Composition composition, @NonNull ExportResult exportResult, @NonNull ExportException exportException) {
                        ACTIVE_TRANSFORMERS.remove(transformerHolder[0]);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, R.string.error_in_creating_file, Toast.LENGTH_SHORT).show();
                            shareVideoOnly(context, photo);
                        });
                    }
                })
                .build();

        transformerHolder[0] = transformer;
        ACTIVE_TRANSFORMERS.add(transformer);
        transformer.start(editedMediaItem, outputFile.getAbsolutePath());
    }

    public static void shareVideoOnly(Context context, Photo photo) {
        shareFile(context, new File(photo.getImagePath()), "video/mp4", context.getString(R.string.share_video));
    }

    public static Bitmap createFullFrameOverlay(File videoFile, Bitmap stampBitmap) {
        int videoWidth = 1080;
        int videoHeight = 1920;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoFile.getAbsolutePath());
            videoWidth = parseMetadataInt(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH, videoWidth);
            videoHeight = parseMetadataInt(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT, videoHeight);
            int rotation = parseMetadataInt(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION, 0);
            if (rotation == 90 || rotation == 270) {
                int swappedWidth = videoHeight;
                videoHeight = videoWidth;
                videoWidth = swappedWidth;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }

        Bitmap overlay = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888);
        overlay.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        float scale = Math.min((float) videoWidth / stampBitmap.getWidth(), (float) videoHeight / stampBitmap.getHeight());
        int scaledWidth = Math.round(stampBitmap.getWidth() * scale);
        int scaledHeight = Math.round(stampBitmap.getHeight() * scale);
        Bitmap scaledStamp = Bitmap.createScaledBitmap(stampBitmap, scaledWidth, scaledHeight, true);

        float left = (videoWidth - scaledWidth) / 2f;
        float top = videoHeight - scaledHeight;
        canvas.drawBitmap(scaledStamp, left, top, paint);
        return overlay;
    }

    private static int parseMetadataInt(MediaMetadataRetriever retriever, int key, int fallback) {
        String value = retriever.extractMetadata(key);
        if (value == null || value.isEmpty()) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void shareFile(Context context, File file, String mimeType, String chooserTitle) {
        Uri uriForFile = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        // App link text is disabled until the app is published.
        // intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_name) + "\n\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName());
        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, chooserTitle));
    }
}
