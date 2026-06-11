package com.camera.gps.repositories;

import android.app.Application;
import android.content.res.Resources;

import com.appizona.yehiahd.fastsave.FastSave;

import com.camera.gps.MyApplication;
import com.camera.gps.util.SP;
import com.camera.gps.R;

public class FontStyleRepository {

    private final Application application;
    private final SP sp;

    public FontStyleRepository(Application application) {
        this.application = application;
        this.sp = new SP(application);
    }

    public String[] getFontArray() {
        Resources res = application.getResources();
        return res.getStringArray(R.array.font_name_array);
    }

    public int getSavedFontPosition() {
        return sp.getInteger(application, SP.LOCATION_FONT_POSITION, 0);
    }

    public void saveFontPosition(int position) {
        sp.setInteger(application, SP.LOCATION_FONT_POSITION, position);
    }

    public void saveFontStyle(String fontName) {
        FastSave.getInstance().saveString(MyApplication.FONT_STYLE, fontName);
    }
}
