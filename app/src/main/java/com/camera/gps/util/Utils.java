package com.camera.gps.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Utils {

    public static final String weeklykey = "journeystamp_weekly_subscription";
    public static final String yearlykey = "timestamp_yearly_subscription";

    public static void setRated(Context context, boolean z) {
        SharedPreferences.Editor edit = context.getSharedPreferences("GPSCamera", 0).edit();
        edit.putBoolean("is_rated", z);
        edit.apply();
    }

    public static boolean isRated(Context context) {
        return context.getSharedPreferences("GPSCamera", 0).getBoolean("is_rated", false);
    }

    public static void setIsPremium(Context context, boolean isPremium) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("premium_preference____", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_premium___", isPremium);
        editor.apply();
    }

    public static boolean getIsPremium(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("premium_preference____", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_premium___", false);
    }

    public static class LogUtils {

        private static final boolean DEBUG = true; // Set to false for release builds

        public static void logD(String tag, String message) {
            if (DEBUG) {
                Log.d(tag, message);
            }
        }

        public static void logI(String tag, String message) {
            if (DEBUG) {
                Log.i(tag, message);
            }
        }

        public static void logW(String tag, String message) {
            if (DEBUG) {
                Log.w(tag, message);
            }
        }

        public static void logE(String tag, String message) {
            if (DEBUG) {
                Log.e(tag, message);
            }
        }

        public static void logE(String tag, String message, Throwable throwable) {
            if (DEBUG) {
                Log.e(tag, message, throwable);
            }
        }
    }
}