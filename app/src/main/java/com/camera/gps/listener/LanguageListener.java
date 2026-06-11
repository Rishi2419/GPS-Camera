package com.camera.gps.listener;


//Used as a callback from adapter → activity
//when a language is tapped, activity gets notified
public interface LanguageListener {
    void language(String code);
}