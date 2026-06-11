//package com.example.gps.util;
//
//import static com.example.gps.MyApplication.context;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.BitmapShader;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffXfermode;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.Shader;
//import android.graphics.drawable.Drawable;
//import android.widget.Toast;
//
//import androidx.collection.LruCache;
//
//import kotlin.jvm.internal.DefaultConstructorMarker;
//import com.example.gps.R;
//
//public final class Constant {
//
//    private static final String KEY_TEMPLATE = "key_template";
//    private static final String PREF_USER = "pref_user";
//    private static final LruCache<String, Bitmap> bitmapCache;
//    public static final Companion Companion = new Companion(null);
//
//    public static final class Companion {
//        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
//            this();
//        }
//
//        private Companion() {
//        }
//
//        public void saveTemplate(Context context, int i) {
//            context.getSharedPreferences(Constant.PREF_USER, 0).edit().putInt(Constant.KEY_TEMPLATE, i).apply();
//        }
//
//        public int getTemplate(Context context) {
//            return context.getSharedPreferences(Constant.PREF_USER, 0).getInt(Constant.KEY_TEMPLATE, 1);
//        }
//
//        public void showToast(Context context, String msg) {
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//        }
//
//        public LruCache<String, Bitmap> getBitmapCache() {
//            return Constant.bitmapCache;
//        }
//
//        public void saveBitmapToCache(String key, Bitmap bitmap) {
//            getBitmapCache().put(key, bitmap);
//        }
//
//        public Bitmap getBitmapFromCache(String key) {
//            return getBitmapCache().get(key);
//        }
//
//        public Bitmap getRoundedCornerBitmap(Bitmap bitmap, int i) {
//            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(createBitmap);
//            Paint paint = new Paint();
//            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//            RectF rectF = new RectF(rect);
//            paint.setAntiAlias(true);
//            canvas.drawRoundRect(rectF, (float) i, (float) i, paint);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(bitmap, rect, rect, paint);
//            return createBitmap;
//        }
//
//        public Bitmap createUserBitmap(Context context, String path) {
//            Bitmap result = null;
//            try {
//                result = Bitmap.createBitmap(dp(80), dp(100), Bitmap.Config.ARGB_8888); // size for background
//                result.eraseColor(Color.TRANSPARENT);
//                Canvas canvas = new Canvas(result);
//                Drawable drawable = context.getResources().getDrawable(R.drawable.mapbg);
//                drawable.setBounds(0, 0, dp(80), dp(100));
//                drawable.draw(canvas);
//
//                Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                RectF bitmapRect = new RectF();
//                canvas.save();
//
//                Bitmap bitmap = BitmapFactory.decodeFile(path);
//                if (bitmap != null) {
//                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                    Matrix matrix = new Matrix();
//                    float scale = dp(40) / (float) bitmap.getWidth();
//                    matrix.postTranslate(dp(5), dp(5));
//                    matrix.postScale(scale, scale);
//                    roundPaint.setShader(shader);
//                    shader.setLocalMatrix(matrix);
//                    bitmapRect.set(dp(5), dp(5), dp(70 + 5), dp(70 + 5)); // size for image
//                    canvas.drawRoundRect(bitmapRect, dp(50), dp(50), roundPaint);
//                }
//                canvas.restore();
//                canvas.setBitmap(null);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//            return result;
//        }
//
//        public int dp(float value) {
//            if (value == 0) {
//                return 0;
//            }
//            return (int) Math.ceil(context.getResources().getDisplayMetrics().density * value);
//        }
//    }
//
//    static {
//        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 8);
//        bitmapCache = new LruCache<>(maxMemory);
//    }
//}

package com.camera.gps.util;

import static com.camera.gps.MyApplication.context;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.collection.LruCache;
import androidx.exifinterface.media.ExifInterface;

import kotlin.jvm.internal.DefaultConstructorMarker;
import com.camera.gps.R;

import java.io.File;
import java.io.IOException;

public final class Constant {

    private static final String KEY_TEMPLATE = "key_template";
    private static final String PREF_USER = "pref_user";
    private static final LruCache<String, Bitmap> bitmapCache;
    public static final Companion Companion = new Companion(null);

    public static final class Companion {
        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public void saveTemplate(Context context, int i) {
            context.getSharedPreferences(Constant.PREF_USER, 0).edit().putInt(Constant.KEY_TEMPLATE, i).apply();
        }

        public int getTemplate(Context context) {
            return context.getSharedPreferences(Constant.PREF_USER, 0).getInt(Constant.KEY_TEMPLATE, 1);
        }

        public void showToast(Context context, String msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

        public LruCache<String, Bitmap> getBitmapCache() {
            return Constant.bitmapCache;
        }

        public void saveBitmapToCache(String key, Bitmap bitmap) {
            getBitmapCache().put(key, bitmap);
        }

        public Bitmap getBitmapFromCache(String key) {
            return getBitmapCache().get(key);
        }

        public Bitmap getRoundedCornerBitmap(Bitmap bitmap, int i) {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);
            paint.setAntiAlias(true);
            canvas.drawRoundRect(rectF, (float) i, (float) i, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return createBitmap;
        }

        // ENHANCED METHOD - Now handles rotation, videos, and proper error handling
        public Bitmap createUserBitmap(Context context, String path) {
            Bitmap result = null;
            try {
                // Check if file exists
                File file = new File(path);
                if (!file.exists()) {
                    return createDefaultMarkerBitmap(context);
                }

                // Check if it's a video or image
                boolean isVideo = isVideoFile(path);

                result = Bitmap.createBitmap(dp(80), dp(100), Bitmap.Config.ARGB_8888);
                result.eraseColor(Color.TRANSPARENT);
                Canvas canvas = new Canvas(result);

                // Draw background
                Drawable drawable = context.getResources().getDrawable(R.drawable.mapbg);
                drawable.setBounds(0, 0, dp(80), dp(100));
                drawable.draw(canvas);

                Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                RectF bitmapRect = new RectF();
                canvas.save();

                Bitmap bitmap = null;

                if (isVideo) {
                    // Create video thumbnail
                    bitmap = createVideoThumbnailBitmap(path);
                } else {
                    // Load and correct image rotation
                    bitmap = loadAndCorrectImage(path);
                }

                if (bitmap != null) {
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Matrix matrix = new Matrix();
                    float scale = Math.min(dp(70) / (float) bitmap.getWidth(), dp(70) / (float) bitmap.getHeight());
                    matrix.postTranslate(dp(5), dp(5));
                    matrix.postScale(scale, scale);
                    roundPaint.setShader(shader);
                    shader.setLocalMatrix(matrix);
                    bitmapRect.set(dp(5), dp(5), dp(70 + 5), dp(70 + 5));
                    canvas.drawRoundRect(bitmapRect, dp(35), dp(35), roundPaint);

                    // Add video play icon if it's a video
                    if (isVideo) {
                        drawVideoPlayIcon(canvas, bitmapRect);
                    }

                    // Clean up bitmap if it's not the original
                    bitmap.recycle();
                }
                canvas.restore();
                canvas.setBitmap(null);
            } catch (Throwable t) {
                t.printStackTrace();
                return createDefaultMarkerBitmap(context);
            }
            return result;
        }

        // NEW METHOD - Load image and correct rotation based on EXIF
        private Bitmap loadAndCorrectImage(String imagePath) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);

                // Calculate inSampleSize for memory efficiency
                options.inSampleSize = calculateInSampleSize(options, dp(70), dp(70));
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

                if (bitmap != null) {
                    // Correct rotation based on EXIF data
                    return correctImageRotation(bitmap, imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // NEW METHOD - Correct image rotation based on EXIF orientation
        private Bitmap correctImageRotation(Bitmap bitmap, String imagePath) {
            try {
                ExifInterface exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                );

                Matrix matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90f);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180f);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270f);
                        break;
                    case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                        matrix.preScale(-1f, 1f);
                        break;
                    case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                        matrix.preScale(1f, -1f);
                        break;
                    case ExifInterface.ORIENTATION_TRANSPOSE:
                        matrix.preScale(-1f, 1f);
                        matrix.postRotate(90f);
                        break;
                    case ExifInterface.ORIENTATION_TRANSVERSE:
                        matrix.preScale(-1f, 1f);
                        matrix.postRotate(270f);
                        break;
                    default:
                        return bitmap; // No rotation needed
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                }
                return rotatedBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return bitmap; // Return original if rotation fails
            }
        }

        // NEW METHOD - Create video thumbnail
        private Bitmap createVideoThumbnailBitmap(String videoPath) {
            try {
                return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // NEW METHOD - Draw video play icon overlay
        private void drawVideoPlayIcon(Canvas canvas, RectF bitmapRect) {
            Paint playIconPaint = new Paint();
            playIconPaint.setColor(Color.WHITE);
            playIconPaint.setAntiAlias(true);
            playIconPaint.setStyle(Paint.Style.FILL);
            playIconPaint.setShadowLayer(dp(2), 0f, dp(1), Color.BLACK);

            // Calculate center and size
            float centerX = bitmapRect.centerX();
            float centerY = bitmapRect.centerY();
            float playSize = dp(8);

            // Draw play triangle
            Path path = new Path();
            path.moveTo(centerX - playSize, centerY - playSize);
            path.lineTo(centerX - playSize, centerY + playSize);
            path.lineTo(centerX + playSize, centerY);
            path.close();

            canvas.drawPath(path, playIconPaint);
        }

        // NEW METHOD - Check if file is a video
        private boolean isVideoFile(String filePath) {
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
            return extension.equals("mp4") || extension.equals("avi") || extension.equals("mov") ||
                    extension.equals("mkv") || extension.equals("3gp") || extension.equals("webm") ||
                    extension.equals("flv");
        }

        // NEW METHOD - Calculate sample size for memory efficiency
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        // NEW METHOD - Create default marker when image loading fails
        private Bitmap createDefaultMarkerBitmap(Context context) {
            try {
                Bitmap result = Bitmap.createBitmap(dp(80), dp(100), Bitmap.Config.ARGB_8888);
                result.eraseColor(Color.TRANSPARENT);
                Canvas canvas = new Canvas(result);

                // Draw background
                Drawable drawable = context.getResources().getDrawable(R.drawable.mapbg);
                drawable.setBounds(0, 0, dp(80), dp(100));
                drawable.draw(canvas);

                // Draw default circle
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLUE);
                canvas.drawCircle(dp(40), dp(40), dp(30), paint);

                canvas.setBitmap(null);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // NEW METHOD - Clear bitmap cache
        public void clearCache() {
            bitmapCache.evictAll();
        }

        public int dp(float value) {
            if (value == 0) {
                return 0;
            }
            return (int) Math.ceil(context.getResources().getDisplayMetrics().density * value);
        }
    }

    static {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 8);
        bitmapCache = new LruCache<>(maxMemory);
    }
}