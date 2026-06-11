package com.camera.gps.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.camera.gps.activity.MainActivity;
import com.camera.gps.activity.Permissions_Activity;
import com.camera.gps.model.LanguageSelectModel;
import java.util.ArrayList;
import com.camera.gps.repositories.LanguageRepository;
import com.camera.gps.MyApplication;

public class LanguageSelectViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<LanguageSelectModel>> languageListLiveData;
    private MutableLiveData<String> selectedLanguageCodeLiveData;
    private MutableLiveData<Boolean> isSettingLiveData;
    private MutableLiveData<Boolean> shouldShowBackButtonLiveData;
    private MutableLiveData<Intent> navigationLiveData;
    private MutableLiveData<Boolean> finishActivityLiveData;
    private final LanguageRepository languageRepository;
    private ArrayList<LanguageSelectModel> itemList;
    private String selectedLanguageCode;
    private boolean isSetting = false;

    public LanguageSelectViewModel(@NonNull Application application) {
        super(application);
        this.languageRepository = new LanguageRepository();
        initializeLiveData();
        initializeLanguageList();
    }

    private void initializeLiveData() {
        languageListLiveData = new MutableLiveData<>();
        selectedLanguageCodeLiveData = new MutableLiveData<>();
        isSettingLiveData = new MutableLiveData<>();
        shouldShowBackButtonLiveData = new MutableLiveData<>();
        navigationLiveData = new MutableLiveData<>();
        finishActivityLiveData = new MutableLiveData<>();
    }

    private void initializeLanguageList() {
        itemList = languageRepository.getSupportedLanguages();
        selectedLanguageCode = languageRepository.getSavedLanguageCode();
        languageListLiveData.setValue(itemList);
        selectedLanguageCodeLiveData.setValue(selectedLanguageCode);
    }

    public void setIsSetting(boolean isSetting) {
        this.isSetting = isSetting;
        isSettingLiveData.setValue(isSetting);
        shouldShowBackButtonLiveData.setValue(isSetting);
    }

    public void onLanguageSelected(String languageCode) {
        selectedLanguageCode = languageCode;
        selectedLanguageCodeLiveData.setValue(languageCode);
    }

    public void onSaveButtonClicked(Context context) {
        languageRepository.saveLanguage(context, selectedLanguageCode);

        if (MyApplication.getIsOnBoardingScreen()) {
            navigationLiveData.setValue(new Intent(context, Permissions_Activity.class));
        } else {
            navigationLiveData.setValue(new Intent(context, MainActivity.class));
        }
        finishActivityLiveData.setValue(true);
    }


    // Getters for LiveData
    public MutableLiveData<ArrayList<LanguageSelectModel>> getLanguageListLiveData() {
        return languageListLiveData;
    }

    public MutableLiveData<String> getSelectedLanguageCodeLiveData() {
        return selectedLanguageCodeLiveData;
    }



    public MutableLiveData<Boolean> getIsSettingLiveData() {
        return isSettingLiveData;
    }

    public MutableLiveData<Boolean> getShouldShowBackButtonLiveData() {
        return shouldShowBackButtonLiveData;
    }

    public MutableLiveData<Intent> getNavigationLiveData() {
        return navigationLiveData;
    }

    public MutableLiveData<Boolean> getFinishActivityLiveData() {
        return finishActivityLiveData;
    }
}