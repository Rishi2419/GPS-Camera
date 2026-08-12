package com.camera.gps.premium;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.model.StampTemplateDefaults;
import com.camera.gps.util.SP;
import com.camera.gps.util.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for the temporary premium flow. The local premium boolean can later be
 * replaced by the verified Google Play entitlement without changing feature checks throughout the
 * app.
 */
public final class PremiumManager {

    private static final Set<Integer> PREMIUM_TEMPLATE_IDS = new HashSet<>(
            Arrays.asList(3, 4, 7, 8, 9));

    // These are the existing free positions from FontStyle_Adapter, expressed as stable names.
    private static final Set<String> FREE_FONTS = new HashSet<>(Arrays.asList(
            "sf pro display.otf",
            "roboto regular.ttf",
            "antipasto.otf",
            "arbutus slab.ttf",
            "balthazar regular.ttf",
            "cabo rounded regular.otf"
    ));

    // The formats belonging to free templates 1, 2, 5 and 6 remain free.
    private static final Set<String> FREE_DATE_TIME_FORMATS = new HashSet<>(Arrays.asList(
            normalizeFormat("dd-MM-yyyy HH:mm:ss a"),
            normalizeFormat("MM/dd/yyyy hh:mm a"),
            normalizeFormat("MMM dd, yyyy hh:mm:ss a"),
            normalizeFormat("EEEE, MMMM dd, yyyy HH:mm a")
    ));

    private PremiumManager() {
    }

    public static boolean isPremium(Context context) {
        return Utils.getIsPremium(context);
    }

    public static void setPremium(Context context, boolean premium) {
        Utils.setIsPremium(context, premium);
        if (!premium && !MyApplication.getShowWatermark()) {
            MyApplication.setShowWatermark(true);
        }
    }

    public static boolean isTemplatePremium(int templateId) {
        return PREMIUM_TEMPLATE_IDS.contains(templateId);
    }

    public static boolean isFontPremium(String fontName) {
        return fontName != null && !FREE_FONTS.contains(fontName.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean isDateTimePremium(String combinedFormat) {
        return combinedFormat != null && !FREE_DATE_TIME_FORMATS.contains(normalizeFormat(combinedFormat));
    }

    public static boolean hasPremiumCaptureConfiguration(
            int templateId,
            String fontName,
            String combinedDateTimeFormat,
            boolean customLocationActive
    ) {
        return isTemplatePremium(templateId)
                || isFontPremium(fontName)
                || isDateTimePremium(combinedDateTimeFormat)
                || customLocationActive;
    }

    /** Reset a blocked preview to the complete built-in Template 1 configuration. */
    public static void resetToDefaultTemplate(Context context) {
        StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(context, 1);
        SP sp = new SP(context);

        FastSave.getInstance().saveInt(MyApplication.STAMP_LAYOUT_ID, 1);
        FastSave.getInstance().saveString(MyApplication.FONT_STYLE, defaults.fontStyle);
        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, defaults.dateFormat);
        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, defaults.timeFormat);
        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, defaults.combinedFormat);
        FastSave.getInstance().saveInt(MyApplication.STAMP_BG_COLOR,
                ContextCompat.getColor(context, R.color.transparent_30));
        FastSave.getInstance().saveInt(MyApplication.STAMP_TEXT_COLOR,
                ContextCompat.getColor(context, R.color.white));
        FastSave.getInstance().saveInt(MyApplication.STAMP_DATE_TIME_COLOR,
                ContextCompat.getColor(context, R.color.white));

        sp.setTemplateFontStyle(context, 1, defaults.fontStyle);
        sp.setTemplateDateFormat(context, 1, defaults.dateFormat);
        sp.setTemplateTimeFormat(context, 1, defaults.timeFormat);
        sp.setTemplateDateTimeCombinedFormat(context, 1, defaults.combinedFormat);
        sp.setTemplateMapType(context, 1, defaults.mapType);
        sp.setTemplateEdited(context, 1, false);

        MyApplication.setMapType(defaults.mapType);
        MyApplication.setShowWatermark(true);
    }

    /** Called once per process start so a free user never reopens into a blocked preview. */
    public static void resetPremiumPreviewOnProcessStart(Context context) {
        if (isPremium(context)) {
            return;
        }

        int templateId = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);
        StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(context, templateId);
        SP sp = new SP(context);
        boolean edited = sp.isTemplateEdited(context, templateId);
        String font = edited
                ? sp.getTemplateFontStyle(context, templateId, defaults.fontStyle)
                : defaults.fontStyle;
        String dateTime = edited
                ? sp.getTemplateDateTimeCombinedFormat(context, templateId, defaults.combinedFormat)
                : defaults.combinedFormat;

        if (isTemplatePremium(templateId)
                || isFontPremium(font)
                || isDateTimePremium(dateTime)
                || !MyApplication.getShowWatermark()) {
            resetToDefaultTemplate(context);
        }
    }

    private static String normalizeFormat(String format) {
        return format == null
                ? ""
                : format.trim().replaceAll("\\s+", " ").replace("aa", "a");
    }
}
