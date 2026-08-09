package com.camera.gps.util;

import android.location.Address;

import java.util.Locale;

public final class LocationAddressFormatter {

    private LocationAddressFormatter() {
    }

    public static String buildDefaultTitle(Address address) {
        if (address == null) {
            return "";
        }

        StringBuilder title = new StringBuilder();
        appendAddressPart(title, address.getLocality());
        appendAddressPart(title, address.getAdminArea());
        appendAddressPart(title, address.getCountryName());

        String countryFlag = countryCodeToFlag(address.getCountryCode());
        if (!countryFlag.isEmpty()) {
            if (title.length() > 0) {
                title.append(' ');
            }
            title.append(countryFlag);
        }
        return title.toString();
    }

    private static void appendAddressPart(StringBuilder title, String part) {
        if (part == null || part.trim().isEmpty()) {
            return;
        }
        if (title.length() > 0) {
            title.append(", ");
        }
        title.append(part.trim());
    }

    private static String countryCodeToFlag(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            return "";
        }
        String normalizedCode = countryCode.toUpperCase(Locale.US);
        int firstLetter = Character.codePointAt(normalizedCode, 0) - 'A' + 0x1F1E6;
        int secondLetter = Character.codePointAt(normalizedCode, 1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(firstLetter))
                + new String(Character.toChars(secondLetter));
    }
}
