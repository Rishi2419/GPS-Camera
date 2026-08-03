package com.camera.gps.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.camera.gps.database.entity.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the app's shared Gallery album and keeps one physical media file that is
 * referenced by both MediaStore/Gallery and the app's Room records.
 */
public final class SharedMediaStore {

    public interface PublishCallback {
        void onSuccess(PublishedMedia media);
        void onError(String error);
    }

    public static final class PublishedMedia {
        private final Uri uri;
        private final String path;

        PublishedMedia(Uri uri, String path) {
            this.uri = uri;
            this.path = path;
        }

        public Uri getUri() {
            return uri;
        }

        public String getPath() {
            return path;
        }
    }

    private static final String PREFS = "shared_media_store";
    private static final String KEY_ALBUM = "selected_album";
    private static final String[] ALBUM_CANDIDATES = {
            "GPS Map Camera",
            "GPS Camera",
            "Map GPS Camera"
    };
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private SharedMediaStore() {
    }

    public static void publishAsync(Context context, File source, boolean video,
                                    PublishCallback callback) {
        Context appContext = context.getApplicationContext();
        IO_EXECUTOR.execute(() -> {
            try {
                PublishedMedia media = publish(appContext, source, video);
                MAIN_HANDLER.post(() -> callback.onSuccess(media));
            } catch (Exception e) {
                MAIN_HANDLER.post(() -> callback.onError(
                        e.getMessage() == null ? "Unable to save media" : e.getMessage()));
            }
        });
    }

    private static PublishedMedia publish(Context context, File source, boolean video)
            throws Exception {
        if (source == null || !source.exists() || source.length() == 0L) {
            throw new IllegalStateException("Captured file is missing or empty");
        }

        String album = getOrChooseAlbum(context);
        String mimeType = video ? "video/mp4" : "image/jpeg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            Uri collection = video
                    ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    : MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, source.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/" + album);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            Uri uri = resolver.insert(collection, values);
            if (uri == null) {
                throw new IllegalStateException("Gallery could not create the media item");
            }

            try {
                try (InputStream input = new FileInputStream(source);
                     OutputStream output = resolver.openOutputStream(uri, "w")) {
                    if (output == null) {
                        throw new IllegalStateException("Gallery output could not be opened");
                    }
                    copy(input, output);
                }

                ContentValues ready = new ContentValues();
                ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
                if (resolver.update(uri, ready, null, null) <= 0) {
                    throw new IllegalStateException("Gallery could not finalize the media item");
                }

                String path = queryPath(resolver, uri);
                if (path == null) {
                    path = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            album + "/" + source.getName()).getAbsolutePath();
                }

                if (!source.delete()) {
                    throw new IllegalStateException("Temporary capture could not be cleaned up");
                }
                return new PublishedMedia(uri, path);
            } catch (Exception e) {
                resolver.delete(uri, null, null);
                throw e;
            }
        }

        File albumFolder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), album);
        if (!albumFolder.exists() && !albumFolder.mkdirs()) {
            throw new IllegalStateException("Gallery album could not be created");
        }

        File destination = uniqueDestination(albumFolder, source.getName());
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(destination)) {
            copy(input, output);
        }

        Uri legacyCollection = video
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues legacyValues = new ContentValues();
        legacyValues.put(MediaStore.MediaColumns.DISPLAY_NAME, destination.getName());
        legacyValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        legacyValues.put(MediaStore.MediaColumns.DATA, destination.getAbsolutePath());
        Uri legacyUri = context.getContentResolver().insert(legacyCollection, legacyValues);
        if (legacyUri == null) {
            destination.delete();
            throw new IllegalStateException("Gallery could not index the media item");
        }

        if (!source.delete()) {
            context.getContentResolver().delete(legacyUri, null, null);
            throw new IllegalStateException("Temporary capture could not be cleaned up");
        }
        return new PublishedMedia(legacyUri, destination.getAbsolutePath());
    }

    public static boolean exists(Context context, String mediaUri, String path) {
        if (mediaUri != null && !mediaUri.isEmpty()) {
            Uri uri = Uri.parse(mediaUri);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return path != null && new File(path).exists();
            }
            try (android.os.ParcelFileDescriptor ignored =
                         context.getContentResolver().openFileDescriptor(uri, "r")) {
                return ignored != null && isInSelectedAlbum(context, uri);
            } catch (Exception ignored) {
                return false;
            }
        }
        return path != null && new File(path).exists();
    }

    public static boolean delete(Context context, String mediaUri, String path) {
        if (!exists(context, mediaUri, path)) {
            return true;
        }
        try {
            Uri uri = mediaUri == null || mediaUri.isEmpty() ? null : Uri.parse(mediaUri);
            if (uri == null && path != null) {
                uri = findUriForPath(context, path);
            }
            if (uri != null && "content".equalsIgnoreCase(uri.getScheme())) {
                return context.getContentResolver().delete(uri, null, null) > 0;
            }
            if (path != null) {
                File file = new File(path);
                boolean deleted = !file.exists() || file.delete();
                if (deleted) {
                    MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
                }
                return deleted;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public static boolean delete(Context context, Photo photo) {
        if (photo == null) {
            return false;
        }
        boolean deleted = delete(context, photo.getMediaUri(), photo.getImagePath());
        if (deleted && photo.getMapImagePath() != null) {
            File mapSnapshot = new File(photo.getMapImagePath());
            if (mapSnapshot.exists()) {
                mapSnapshot.delete();
            }
        }
        return deleted;
    }

    public static Uri getContentUri(Context context, String mediaUri, String path) {
        if (mediaUri != null && !mediaUri.isEmpty()) {
            return Uri.parse(mediaUri);
        }
        Uri resolved = findUriForPath(context, path);
        return resolved != null ? resolved : (path == null ? null : Uri.fromFile(new File(path)));
    }

    public static Object getLoadSource(Photo photo) {
        if (photo != null && photo.getMediaUri() != null && !photo.getMediaUri().isEmpty()) {
            return Uri.parse(photo.getMediaUri());
        }
        return photo == null ? null : photo.getImagePath();
    }

    public static boolean isVideo(Photo photo) {
        if (photo == null) {
            return false;
        }
        if ("video".equalsIgnoreCase(photo.getMediaType())) {
            return true;
        }
        String path = photo.getImagePath();
        return path != null && (path.toLowerCase().endsWith(".mp4")
                || path.toLowerCase().endsWith(".mov")
                || path.toLowerCase().endsWith(".avi"));
    }

    public static boolean share(Context context, Photo photo) {
        Uri uri = getContentUri(context,
                photo == null ? null : photo.getMediaUri(),
                photo == null ? null : photo.getImagePath());
        if (uri == null) {
            return false;
        }

        boolean video = isVideo(photo);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType(video ? "video/mp4" : "image/jpeg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(
                intent, video ? "Share Video!" : "Share Image!"));
        return true;
    }

    public static String resolveCurrentPath(Context context, String mediaUri, String fallbackPath) {
        if (mediaUri == null || mediaUri.isEmpty()) {
            return fallbackPath;
        }
        Uri uri = Uri.parse(mediaUri);
        if (!"content".equalsIgnoreCase(uri.getScheme())) {
            return fallbackPath;
        }
        String currentPath = queryPath(context.getContentResolver(), uri);
        return currentPath == null ? fallbackPath : currentPath;
    }

    private static String getOrChooseAlbum(Context context) {
        android.content.SharedPreferences preferences =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saved = preferences.getString(KEY_ALBUM, null);
        if (saved != null && !saved.isEmpty()) {
            return saved;
        }

        for (String candidate : ALBUM_CANDIDATES) {
            if (albumExists(context, candidate)) {
                preferences.edit().putString(KEY_ALBUM, candidate).apply();
                return candidate;
            }
        }

        preferences.edit().putString(KEY_ALBUM, ALBUM_CANDIDATES[0]).apply();
        return ALBUM_CANDIDATES[0];
    }

    private static boolean albumExists(Context context, String album) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    album).isDirectory();
        }

        String[] projection = {MediaStore.Files.FileColumns._ID};
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] args = {Environment.DIRECTORY_DCIM + "/" + album + "/"};
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                projection, selection, args, null)) {
            return cursor != null && cursor.moveToFirst();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isInSelectedAlbum(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try (Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.MediaColumns.DATA},
                    null, null, null)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    return false;
                }
                String path = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.MediaColumns.DATA));
                if (path == null) {
                    return false;
                }
                File expectedFolder = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM),
                        getOrChooseAlbum(context));
                File parent = new File(path).getParentFile();
                return parent != null && expectedFolder.getAbsolutePath()
                        .equalsIgnoreCase(parent.getAbsolutePath());
            } catch (Exception ignored) {
                return false;
            }
        }
        try (Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.MediaColumns.RELATIVE_PATH},
                null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return false;
            }
            String relativePath = cursor.getString(cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.RELATIVE_PATH));
            String expected = Environment.DIRECTORY_DCIM + "/"
                    + getOrChooseAlbum(context) + "/";
            return expected.equalsIgnoreCase(relativePath);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Uri findUriForPath(Context context, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] projection = {MediaStore.Files.FileColumns._ID};
        String selection = MediaStore.MediaColumns.DATA + "=?";
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection, selection, new String[]{path}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns._ID));
                return ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id);
            }
        } catch (Exception ignored) {
            // Fall through to file URI.
        }
        return null;
    }

    private static String queryPath(ContentResolver resolver, Uri uri) {
        try (Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.MediaColumns.DATA}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.MediaColumns.DATA));
            }
        } catch (Exception ignored) {
            // DATA is a compatibility value. Callers can still use the content URI.
        }
        return null;
    }

    private static File uniqueDestination(File folder, String requestedName) {
        File destination = new File(folder, requestedName);
        if (!destination.exists()) {
            return destination;
        }

        int dot = requestedName.lastIndexOf('.');
        String base = dot > 0 ? requestedName.substring(0, dot) : requestedName;
        String extension = dot > 0 ? requestedName.substring(dot) : "";
        int suffix = 1;
        while (destination.exists()) {
            destination = new File(folder, base + "_" + suffix++ + extension);
        }
        return destination;
    }

    private static void copy(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[1024 * 1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        output.flush();
    }
}
