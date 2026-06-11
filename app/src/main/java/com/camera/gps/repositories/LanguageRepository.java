package com.camera.gps.repositories;

import android.content.Context;
import android.content.res.Configuration;
import com.camera.gps.R;
import com.camera.gps.model.LanguageSelectModel;
import java.util.ArrayList;
import java.util.Locale;
import com.camera.gps.MyApplication;

public class LanguageRepository {

    public ArrayList<LanguageSelectModel> getSupportedLanguages() {
        ArrayList<LanguageSelectModel> languages = new ArrayList<>();
        languages.add(new LanguageSelectModel("English", "(English)","en", R.drawable.ic_english));
        languages.add(new LanguageSelectModel("Indonesia","(Indonesian)", "in", R.drawable.ic_indonesian));
        languages.add(new LanguageSelectModel("Española", "(Spanish)", "es", R.drawable.ic_spanish));
        languages.add(new LanguageSelectModel("Tiếng Việt", "(Vietnamese)", "vi", R.drawable.ic_vietnamese));
        languages.add(new LanguageSelectModel("แบบไทย", "(Thai)", "th", R.drawable.ic_thai));
        languages.add(new LanguageSelectModel("Português", "(Portuguese)", "pt", R.drawable.ic_portuguese));
        languages.add(new LanguageSelectModel("Русский", "(Russian)", "ru", R.drawable.ic_russian));
        languages.add(new LanguageSelectModel("Français", "(French)", "fr", R.drawable.ic_french));
        languages.add(new LanguageSelectModel("Coréenne", "(Korean)", "ko", R.drawable.ic_korean));
        languages.add(new LanguageSelectModel("हिन्दी", "(Hindi)", "hi", R.drawable.ic_hindi));
        return languages;
    }

    public String getSavedLanguageCode() {
        try {
            return MyApplication.getLanguageCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "en";
        }
    }

    public void saveLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        MyApplication.setLanguageCode(languageCode);
    }
}
