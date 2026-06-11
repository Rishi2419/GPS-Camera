package com.camera.gps.model;

public class LanguageSelectModel {
    public String langName;
    public String langSubname;
    public String langCode;
    public Integer flagIcon;

    public LanguageSelectModel(String langName, String langSubname, String langCode, Integer flagIcon) {
        this.langName = langName;
        this.langSubname = langSubname;
        this.langCode = langCode;
        this.flagIcon = flagIcon;
    }
}