package com.camera.gps.model;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.camera.gps.R;

public final class StampTemplateDefaults {

    private StampTemplateDefaults() {
    }

    public static Settings forTemplate(Context context, int templateId) {
        int transparentBg = ContextCompat.getColor(context, R.color.transparent_30);

        switch (templateId) {
            case 1:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        1,
                        "dd-MM-yyyy",
                        "HH:mm:ss a",
                        "dd-MM-yyyy HH:mm:ss a"
                );
            case 2:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        2,
                        "MM/dd/yyyy",
                        "hh:mm a",
                        "MM/dd/yyyy hh:mm a"
                );
            case 3:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        3,
                        "yyyy-MM-dd",
                        "HH:mm",
                        "yyyy-MM-dd HH:mm"
                );
            case 4:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        4,
                        "dd/MM/yyyy",
                        "HH:mm:ss",
                        "dd/MM/yyyy HH:mm:ss"
                );
            case 5:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        1,
                        "MMM dd, yyyy",
                        "hh:mm:ss a",
                        "MMM dd, yyyy hh:mm:ss a"
                );
            case 6:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        2,
                        "EEEE, MMMM dd, yyyy",
                        "HH:mm a",
                        "EEEE, MMMM dd, yyyy HH:mm a"
                );
            case 7:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        3,
                        "dd.MM.yyyy",
                        "HH:mm:ss",
                        "dd.MM.yyyy HH:mm:ss"
                );
            case 8:
                return new Settings(
                        "SF Pro Display.otf",
                        transparentBg,
                        Color.WHITE,
                        Color.WHITE,
                        4,
                        "yyyy/MM/dd",
                        "hh:mm a",
                        "yyyy/MM/dd hh:mm a"
                );
            case 9:
                return new Settings(
                        "SF Pro Display.otf",
                        ContextCompat.getColor(context, R.color.bg_glass),
                        Color.WHITE,
                        Color.WHITE,
                        1,
                        "dd-MMM-yyyy",
                        "HH:mm:ss a",
                        "dd-MMM-yyyy HH:mm:ss a"
                );
            default:
                return forTemplate(context, 1);
        }
    }

    public static final class Settings {
        public final String fontStyle;
        public final int bgColor;
        public final int textColor;
        public final int dateTimeColor;
        public final int mapType;
        public final String dateFormat;
        public final String timeFormat;
        public final String combinedFormat;

        private Settings(String fontStyle,
                         int bgColor,
                         int textColor,
                         int dateTimeColor,
                         int mapType,
                         String dateFormat,
                         String timeFormat,
                         String combinedFormat) {
            this.fontStyle = fontStyle;
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.dateTimeColor = dateTimeColor;
            this.mapType = mapType;
            this.dateFormat = dateFormat;
            this.timeFormat = timeFormat;
            this.combinedFormat = combinedFormat;
        }
    }
}
