package com.camera.gps.camerax.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefsSettings {
    public static final String GRID_LINES = "GridLinesKey";
    public static final String GRID_TYPE = "GridTypeKey";
    public static final String IMAGE_MAX_QUALITY = "ImageMaxQualityKey";
    public static final String IMAGE_SIZE = "ImageSizeKey";
    public static final String PREVIEW_SCALE_TYPE = "PreviewScaleTypeKey";
    public static final String SHARED_PREFS_SETTINGS = "PrefsSettings";
    public static final String SOUND = "SoundKey";
    public static final String VIDEO_FPS = "VideoFPSKey";
    public static final String VIDEO_RESOLUTION = "VideoResolutionKey";

    // Grid type constants
    public static final int GRID_OFF = 0;
    public static final int GRID_3X3 = 1;
    public static final int GRID_4X4 = 2;

    public static SharedPreferences getPrefsSettings(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0);
    }

    public static void setImageSize(int i, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putInt(IMAGE_SIZE, i);
        edit.apply();
    }

    public static void setVideoSize(int i, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putInt(VIDEO_RESOLUTION, i);
        edit.apply();
    }

    public static void setFps(int i, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putInt(VIDEO_FPS, i);
        edit.apply();
    }

    public static void setSoundStatus(boolean z, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putBoolean(SOUND, z);
        edit.apply();
    }

    // Updated method to store grid type instead of boolean
    public static void setGridType(int gridType, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putInt(GRID_TYPE, gridType);
        edit.apply();
    }

    // Keep for backward compatibility
    public static void setGridLinesStatus(boolean z, Context context) {
        setGridType(z ? GRID_3X3 : GRID_OFF, context);
    }

    public static void setImageMaxQuality(boolean z, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putBoolean(IMAGE_MAX_QUALITY, z);
        edit.apply();
    }

    public static void setPreviewScaleType(int i, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).edit();
        edit.putInt(PREVIEW_SCALE_TYPE, i);
        edit.apply();
    }

    public static int getFps(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getInt(VIDEO_FPS, 30);
    }

    public static int getVideoSize(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getInt(VIDEO_RESOLUTION, 1080);
    }

    public static int getImageSizeType(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getInt(IMAGE_SIZE, 1);
    }

    public static boolean getSoundStatus(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getBoolean(SOUND, true);
    }

    // New method to get grid type
    public static int getGridType(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getInt(GRID_TYPE, GRID_OFF);
    }

    // Keep for backward compatibility
    public static boolean getGridLinesStatus(Context context) {
        int gridType = getGridType(context);
        return gridType != GRID_OFF;
    }

    public static boolean getImageMaxQuality(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getBoolean(IMAGE_MAX_QUALITY, true);
    }

    public static int getPreviewScaleType(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_SETTINGS, 0).getInt(PREVIEW_SCALE_TYPE, 1);
    }

    public static int[] getVideoSizes(Context context) {
        int[] iArr = new int[2];
        int videoSize = getVideoSize(context);

        Log.d("Rishi_Video", "Selected videoSize preference: " + videoSize);

        if (videoSize == 240) {
            iArr[0] = 240;
            iArr[1] = 426;
        } else if (videoSize == 360) {
            iArr[0] = 360;
            iArr[1] = 640;
        } else if (videoSize == 480) {
            iArr[0] = 480;
            iArr[1] = 854;
        } else if (videoSize == 540) {
            iArr[0] = 540;
            iArr[1] = 960;
        } else if (videoSize == 720) {
            iArr[0] = 720;
            iArr[1] = 1280;
        } else if (videoSize == 1080) {
            iArr[0] = 1080;
            iArr[1] = 1920;
        } else if (videoSize == 1440) {
            iArr[0] = 1440;
            iArr[1] = 2960;
        }

        Log.d("Rishi_Video", "Resolved video resolution: " + iArr[0] + "x" + iArr[1]);
        return iArr;
    }

    public static int[] getImageSize(Context context) {
        int[] iArr = new int[2];
        int imageSizeType = getImageSizeType(context);
        if (imageSizeType != 1) {
            if (imageSizeType == 2) {
                iArr[0] = 2268;
                iArr[1] = 4032;
                return iArr;
            } else if (imageSizeType == 3) {
                iArr[0] = 1440;
                iArr[1] = 2560;
                return iArr;
            } else if (imageSizeType == 4) {
                iArr[0] = 3024;
                iArr[1] = 4032;
                return iArr;
            } else if (imageSizeType != 5) {
                return iArr;
            } else {
                iArr[0] = 2160;
                iArr[1] = 2880;
                return iArr;
            }
        }
        return null;
    }
}