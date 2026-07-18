package com.camera.gps.util;

import android.content.Context;

import com.camera.gps.dialogs.DateTimeDialog;
import com.camera.gps.dialogs.FontStyleDialog;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.listener.OnFontSelectedListener;

public final class StampSettingsBottomSheets {

    private StampSettingsBottomSheets() {
    }

    public static FontStyleDialog showFontStyle(
            Context context,
            String[] fontList,
            String selectedFont,
            OnFontSelectedListener listener
    ) {
        FontStyleDialog dialog = new FontStyleDialog(context, fontList, listener);
        int selectedPosition = findFontPosition(fontList, selectedFont);
        if (selectedPosition >= 0) {
            dialog.setSelectedPosition(selectedPosition);
        }
        new HelperClass().setBottomDialog(dialog);
        dialog.show();
        TemporarySettingsHint.bind(dialog);
        return dialog;
    }

    public static DateTimeDialog showDateTime(
            Context context,
            String currentFormat,
            OnDateTimeSelectedListener listener
    ) {
        DateTimeDialog dialog = new DateTimeDialog(context, listener);
        dialog.setCurrentActiveFormat(currentFormat);
        dialog.setPersistSelection(false);
        new HelperClass().setBottomDialog(dialog);
        dialog.show();
        TemporarySettingsHint.bind(dialog);
        return dialog;
    }

    private static int findFontPosition(String[] fontList, String selectedFont) {
        if (fontList == null || selectedFont == null) {
            return -1;
        }
        for (int i = 0; i < fontList.length; i++) {
            if (selectedFont.equalsIgnoreCase(fontList[i])) {
                return i;
            }
        }
        return -1;
    }
}
