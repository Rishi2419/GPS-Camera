package com.camera.gps.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public final class GlobalViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public GlobalViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GlobalViewModel.class)) {
            return (T) new GlobalViewModel(new GlobalRepository(this.application));
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}