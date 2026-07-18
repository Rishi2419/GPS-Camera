package com.camera.gps;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.adsmanager.AppOpenManager;

import kotlin.jvm.internal.DefaultConstructorMarker;

public final class MyApplication extends Application {
    public static final String STAMP_BG_ALPHA = "STAMP_BG_ALPHA";
    public static final String STAMP_DATE_COLOR = "STAMP_DATE_COLOR";
    public static final String STAMP_TIME_COLOR = "STAMP_TIME_COLOR";


    public static final String STAMP_TEXT_COLOR = "STAMP_TEXT_COLOR";
    public static final String STAMP_DATE_TIME_COLOR = "STAMP_DATE_TIME_COLOR";
    public static final String STAMP_BG_COLOR = "STAMP_BG_COLOR";

    public static final String STAMP_LAYOUT_ID = "STAMP_LAYOUT_ID";
    public static final String TEMP_STAMP_LAYOUT_ID = "TEMP_STAMP_LAYOUT_ID";
    public static final String FONT_STYLE = "FONT_STYLE";
    public static final String STAMP_MAP_TYPE = "STAMP_MAP_TYPE";

    public static final String TIMEZONE_FORMAT = "TIME_ZONE";
    public static final String TIME_FORMAT = "time_formate";

    public static final String FORMAT_DATE = "FORMAT_DATE";
    public static final String FORMAT_TIME = "FORMAT_TIME";

    public static final String SHOW_WATERMARK = "SHOW_WATERMARK";

    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";


    public static SharedPreferences preferences;
    public static SharedPreferences.Editor mEditor;
    public static Context context;
    public static final Companion Companion = new Companion(null);

    private static AppOpenManager appOpenManager;
    private static MyApplication mApp;

    public static Context context() {
        return Companion.context();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appOpenManager = new AppOpenManager(this);
        mApp = this;
        context = this;
        FastSave.init(getApplicationContext());
        preferences = getSharedPreferences("GPS_Map_Camera" + context().getPackageName(), MODE_PRIVATE);
        mEditor = preferences.edit();

    }

    public static final class Companion {
        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
        private Companion() {}
        public Context context() {
            return mApp.getApplicationContext();
        }
    }


    //LANGUGE
    //return lang code from sharedpref & if not present than return en
    public static String getLanguageCode() {
        return preferences.getString("languageCode", "en");
    }

    public static void setLanguageCode(String lan) {
        mEditor.putString("languageCode", lan).commit();
    }
    public static boolean getIsLanguage() {
        return preferences.getBoolean("isLanguage", false);
    }

    public static boolean set_to_current_location = false;
    public static void setIsLanguage(boolean language) {
        mEditor.putBoolean("isLanguage", language).commit();
    }



    //PERMISSIONS
    public static boolean isCameraPermissionGranted() {
        return preferences.getBoolean("isCameraPermissionGranted", false);
    }

    public static void setCameraPermissionGranted(boolean granted) {
        mEditor.putBoolean("isCameraPermissionGranted", granted).apply();
    }

    public static boolean isLocationPermissionGranted() {
        return preferences.getBoolean("isLocationPermissionGranted", false);
    }

    public static void setLocationPermissionGranted(boolean granted) {
        mEditor.putBoolean("isLocationPermissionGranted", granted).apply();
    }

    public static boolean isMicrophonePermissionGranted() {
        return preferences.getBoolean("isMicrophonePermissionGranted", false);
    }

    public static void setMicrophonePermissionGranted(boolean granted) {
        mEditor.putBoolean("isMicrophonePermissionGranted", granted).apply();
    }

    //ONBOARDING
    public static boolean getIsOnBoardingScreen() {
        return preferences.getBoolean("onBoardingScreenFirst", true);
    }
    public static void setIsOnBoardingScreen(boolean language) {
        mEditor.putBoolean("onBoardingScreenFirst", language).commit();
    }


    //FONT
    public static void setFontStyleNativeFrequency(int myLocationNativeFrequency) {
        mEditor.putInt("FontStyleNativeFrequency", myLocationNativeFrequency).commit();
    }

    public static int getFontStyleNativeFrequency() {
        return preferences.getInt("FontStyleNativeFrequency", 1);
    }

    //WATERMARK
    public static void setShowWatermark(boolean value) {
        mEditor.putBoolean("SHOW_WATERMARK", value).apply();
    }

    public static boolean getShowWatermark() {
        return preferences.getBoolean("SHOW_WATERMARK", true);
    }


    //MAP
    // MAP TYPE
    public static void setMapType(int mapType) {
        mEditor.putInt(STAMP_MAP_TYPE, mapType).commit();
    }

    public static int getMapType() {
        return preferences.getInt(STAMP_MAP_TYPE, 1);
    }

    //For checking the internet
    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }




}
