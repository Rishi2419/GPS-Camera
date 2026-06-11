package com.camera.gps.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SP {
    public static final String LOCATION_FONT_POSITION = "location_font_pos";

    // Per-template font keys
    private static final String TEMPLATE_FONT_PREFIX = "template_font_pos_";
    private static final String TEMPLATE_BG_COLOR_PREFIX = "template_bg_color_";
    private static final String TEMPLATE_TEXT_COLOR_PREFIX = "template_text_color_";
    private static final String TEMPLATE_DATETIME_COLOR_PREFIX = "template_datetime_color_";

    private static final String TEMPLATE_DATETIME_COMBINED_FORMAT_PREFIX = "template_datetime_combined_";
    private static final String TEMPLATE_DATE_FORMAT_PREFIX = "template_date_format_";
    private static final String TEMPLATE_TIME_FORMAT_PREFIX = "template_time_format_";

    private static final String TEMPLATE_MAP_TYPE_PREFIX = "template_map_type_";

    private static final String KEY_REMOTE_JSON = "remote_config_json";

    private static final String TEMPLATE_EDITED_PREFIX = "template_edited_";

    private static final String PREF_NAME = "saved_colors";
    private static final String KEY_COLORS = "color_list";
    private static final int MAX_COLORS = 20;


    SharedPreferences myPreference;

    public SP(Context context) {
        StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        if (context != null) {
            try {
                this.myPreference = PreferenceManager.getDefaultSharedPreferences(context);
            } catch (Throwable th) {
                StrictMode.setThreadPolicy(allowThreadDiskReads);
                throw th;
            }
        }
        StrictMode.setThreadPolicy(allowThreadDiskReads);
    }


    public void setString(Context context, String str, String str2) {
        if (this.myPreference == null) {
            this.myPreference = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor edit = this.myPreference.edit();
        edit.putString(str, str2);
        edit.apply();
    }

    public String getString(Context context, String str, String str2) {
        if (this.myPreference == null) {
            this.myPreference = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return this.myPreference.getString(str, str2);
    }

    public void setInteger(Context context, String str, Integer num) {
        if (this.myPreference == null) {
            this.myPreference = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor edit = this.myPreference.edit();
        edit.putInt(str, num);
        edit.apply();
    }


    public Integer getInteger(Context context, String str, int i) {
        if (this.myPreference == null) {
            this.myPreference = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return this.myPreference.getInt(str, i);
    }




    public void setTemplateFontStyle(Context context, int templateId, String fontName) {
        String key = TEMPLATE_FONT_PREFIX + templateId;
        setString(context, key, fontName);
    }

    public String getTemplateFontStyle(Context context, int templateId, String defaultFontName) {
        String key = TEMPLATE_FONT_PREFIX + templateId;
        return getString(context, key, defaultFontName);
    }


    // Set background color for a specific template
    public void setTemplateBgColor(Context context, int templateId, int color) {
        String key = TEMPLATE_BG_COLOR_PREFIX + templateId;
        setInteger(context, key, color);
    }

    // Get background color for a specific template, with a default fallback
    public int getTemplateBgColor(Context context, int templateId, int defaultColor) {
        String key = TEMPLATE_BG_COLOR_PREFIX + templateId;
        return getInteger(context, key, defaultColor);
    }



    public void setTemplateTextColor(Context context, int templateId, int color) {
        String key = TEMPLATE_TEXT_COLOR_PREFIX + templateId;
        setInteger(context, key, color);
    }

    public int getTemplateTextColor(Context context, int templateId, int defaultColor) {
        String key = TEMPLATE_TEXT_COLOR_PREFIX + templateId;
        return getInteger(context, key, defaultColor);
    }

    public void setTemplateDateTimeColor(Context context, int templateId, int color) {
        String key = TEMPLATE_DATETIME_COLOR_PREFIX + templateId;
        setInteger(context, key, color);
    }

    public int getTemplateDateTimeColor(Context context, int templateId, int defaultColor) {
        String key = TEMPLATE_DATETIME_COLOR_PREFIX + templateId;
        return getInteger(context, key, defaultColor);
    }

    public void setTemplateMapType(Context context, int templateId, int mapType) {
        String key = TEMPLATE_MAP_TYPE_PREFIX + templateId;
        setInteger(context, key, mapType);
    }

    public int getTemplateMapType(Context context, int templateId, int defaultMapType) {
        String key = TEMPLATE_MAP_TYPE_PREFIX + templateId;
        return getInteger(context, key, defaultMapType);
    }


    // Save combined format
    public void setTemplateDateTimeCombinedFormat(Context context, int templateId, String format) {
        String key = TEMPLATE_DATETIME_COMBINED_FORMAT_PREFIX + templateId;
        setString(context, key, format);
    }

    // Get combined format with default
    public String getTemplateDateTimeCombinedFormat(Context context, int templateId, String defaultFormat) {
        String key = TEMPLATE_DATETIME_COMBINED_FORMAT_PREFIX + templateId;
        return getString(context, key, defaultFormat);
    }

    // Save date format
    public void setTemplateDateFormat(Context context, int templateId, String format) {
        String key = TEMPLATE_DATE_FORMAT_PREFIX + templateId;
        setString(context, key, format);
    }

    // Get date format
    public String getTemplateDateFormat(Context context, int templateId, String defaultFormat) {
        String key = TEMPLATE_DATE_FORMAT_PREFIX + templateId;
        return getString(context, key, defaultFormat);
    }

    // Save time format
    public void setTemplateTimeFormat(Context context, int templateId, String format) {
        String key = TEMPLATE_TIME_FORMAT_PREFIX + templateId;
        setString(context, key, format);
    }

    // Get time format
    public String getTemplateTimeFormat(Context context, int templateId, String defaultFormat) {
        String key = TEMPLATE_TIME_FORMAT_PREFIX + templateId;
        return getString(context, key, defaultFormat);
    }







    public void setTemplateEdited(Context context, int templateId, boolean isEdited) {
        String key = TEMPLATE_EDITED_PREFIX + templateId;
        setInteger(context, key, isEdited ? 1 : 0);
    }
    public boolean isTemplateEdited(Context context, int templateId) {
        String key = TEMPLATE_EDITED_PREFIX + templateId;
        return getInteger(context, key, 0) == 1;
    }

    public void resetTemplate(Context context, int templateId) {
        //setTemplateEdited(context, templateId, false);
        // You can also clear the saved font style if needed
        // setTemplateFontStyle(context, templateId, "SF Pro Display.otf"); // or your default font
    }




    public List<Integer> getSavedColors() {
        Set<String> colorStrings = myPreference.getStringSet(KEY_COLORS, getDefaultColors());
        List<Integer> colors = new ArrayList<>();

        for (String colorString : colorStrings) {
            try {
                colors.add(Integer.parseInt(colorString));
            } catch (NumberFormatException e) {
                // Skip invalid colors
            }
        }

        // If no colors or less than expected, add default colors
        if (colors.isEmpty()) {
            return getDefaultColorList();
        }

        return colors;
    }

    public void saveColors(List<Integer> colors) {
        Set<String> colorStrings = new HashSet<>();

        // Limit to MAX_COLORS
        int limit = Math.min(colors.size(), MAX_COLORS);
        for (int i = 0; i < limit; i++) {
            colorStrings.add(String.valueOf(colors.get(i)));
        }

        myPreference.edit()
                .putStringSet(KEY_COLORS, colorStrings)
                .apply();
    }

    public void addColor(int color) {
        List<Integer> currentColors = getSavedColors();

        // Insert the new color at the first position
        currentColors.add(0, color);

        // Remove the last color if list exceeds MAX_COLORS
        if (currentColors.size() > MAX_COLORS) {
            currentColors.remove(currentColors.size() - 1);
        }

        // Save updated list
        saveColors(currentColors);
    }


    private Set<String> getDefaultColors() {
        Set<String> defaultColors = new HashSet<>();
        List<Integer> colors = getDefaultColorList();

        for (Integer color : colors) {
            defaultColors.add(String.valueOf(color));
        }

        return defaultColors;
    }

    private List<Integer> getDefaultColorList() {
        List<Integer> colors = new ArrayList<>();

        // Row 1 - Basic colors
        colors.add(Color.parseColor("#EF4444")); // Red
        colors.add(Color.parseColor("#F97316")); // Orange
        colors.add(Color.parseColor("#EAB308")); // Yellow
        colors.add(Color.parseColor("#84CC16")); // Lime
        colors.add(Color.parseColor("#22C55E")); // Green
        colors.add(Color.parseColor("#06B6D4")); // Cyan
        colors.add(Color.parseColor("#3B82F6")); // Blue
        colors.add(Color.parseColor("#8B5CF6")); // Violet
        colors.add(Color.parseColor("#EC4899")); // Pink
        colors.add(Color.parseColor("#000000")); // Black

        // Row 2 - Darker variants
        colors.add(Color.parseColor("#DC2626")); // Dark Red
        colors.add(Color.parseColor("#EA580C")); // Dark Orange
        colors.add(Color.parseColor("#CA8A04")); // Dark Yellow
        colors.add(Color.parseColor("#65A30D")); // Dark Lime
        colors.add(Color.parseColor("#16A34A")); // Dark Green
        colors.add(Color.parseColor("#0891B2")); // Dark Cyan
        colors.add(Color.parseColor("#2563EB")); // Dark Blue
        colors.add(Color.parseColor("#7C3AED")); // Dark Violet
        colors.add(Color.parseColor("#DB2777")); // Dark Pink
        colors.add(Color.parseColor("#FFFFFF")); // White

        return colors;
    }



    /** Save remote config JSON string */
    public static void saveString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    public static String getString2(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defaultValue);
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        prefs.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        return prefs.getInt(key, defaultValue);
    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        prefs.edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_REMOTE_JSON, Context.MODE_PRIVATE);
        return prefs.getLong(key, defaultValue);
    }
}