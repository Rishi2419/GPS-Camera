package com.camera.gps.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.camera.gps.repositories.FontStyleRepository;

public class FontStyleViewModel extends AndroidViewModel {

    private final FontStyleRepository repository;
    private final MutableLiveData<String[]> fontList = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedFontPosition = new MutableLiveData<>();

    public FontStyleViewModel(@NonNull Application application) {
        super(application);
        repository = new FontStyleRepository(application);
        loadFonts();
    }

    private void loadFonts() {
        fontList.setValue(repository.getFontArray());
        selectedFontPosition.setValue(repository.getSavedFontPosition());
    }

    public LiveData<String[]> getFontList() {
        return fontList;
    }

    public LiveData<Integer> getSelectedFontPosition() {
        return selectedFontPosition;
    }

    public void selectFont(int position) {
        selectedFontPosition.setValue(position);
        repository.saveFontPosition(position);
    }

    public void saveFontStyle(String fontName) {
        repository.saveFontStyle(fontName);
    }
}


