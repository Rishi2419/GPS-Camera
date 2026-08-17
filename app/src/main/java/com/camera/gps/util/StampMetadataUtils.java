package com.camera.gps.util;

import androidx.annotation.Nullable;

/** Null-safe normalization for metadata read from saved photo database rows. */
public final class StampMetadataUtils {

    private StampMetadataUtils() {
    }

    @Nullable
    public static String optionalText(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static double latitudeOrNaN(@Nullable String value) {
        return coordinateOrNaN(value, -90d, 90d);
    }

    public static double longitudeOrNaN(@Nullable String value) {
        return coordinateOrNaN(value, -180d, 180d);
    }

    public static boolean hasCoordinates(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude);
    }

    public static int templateIdOrDefault(@Nullable Integer value) {
        return value != null && value >= 1 && value <= 9 ? value : 1;
    }

    public static int mapTypeOrDefault(@Nullable Integer value, int fallback) {
        return value != null && value >= 0 && value <= 4 ? value : fallback;
    }

    public static int ratioOrDefault(int value) {
        return value >= 0 && value <= 2 ? value : 2;
    }

    public static int colorOrDefault(int value, int fallback) {
        return value != 0 ? value : fallback;
    }

    private static double coordinateOrNaN(@Nullable String value, double min, double max) {
        String normalized = optionalText(value);
        if (normalized == null) {
            return Double.NaN;
        }
        try {
            double coordinate = Double.parseDouble(normalized);
            return Double.isFinite(coordinate) && coordinate >= min && coordinate <= max
                    ? coordinate
                    : Double.NaN;
        } catch (NumberFormatException ignored) {
            return Double.NaN;
        }
    }
}
