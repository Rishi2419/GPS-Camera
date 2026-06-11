package com.camera.gps.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.camera.gps.repositories.SavedLocationRepository;
import com.camera.gps.database.entity.MyLocation;

import java.util.List;

public class SavedLocationViewModel extends AndroidViewModel {

    private final SavedLocationRepository repository;
    private final MutableLiveData<List<MyLocation>> locationsLiveData;
    private final MutableLiveData<List<MyLocation>> deleteResultLiveData;
    private final MutableLiveData<Boolean> deleteButtonVisibility;

    public SavedLocationViewModel(@NonNull Application application) {
        super(application);
        repository = new SavedLocationRepository(application);
        locationsLiveData = new MutableLiveData<>();
        deleteResultLiveData = new MutableLiveData<>();
        deleteButtonVisibility = new MutableLiveData<>();
        deleteButtonVisibility.setValue(false);
    }

    public LiveData<List<MyLocation>> getLocationsLiveData() {
        return locationsLiveData;
    }

    public LiveData<List<MyLocation>> getDeleteResultLiveData() {
        return deleteResultLiveData;
    }

    public LiveData<Boolean> getDeleteButtonVisibility() {
        return deleteButtonVisibility;
    }

    public void getAllLocation() {
        repository.getAllLocation().observeForever(new Observer<List<MyLocation>>() {
            @Override
            public void onChanged(List<MyLocation> locations) {
                locationsLiveData.setValue(locations);
                repository.getAllLocation().removeObserver(this);
            }
        });
    }

    public void deleteLocations(List<MyLocation> locations) {
        repository.deleteLocations(locations).observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer result) {
                deleteResultLiveData.setValue(locations);
                repository.deleteLocations(locations).removeObserver(this);
            }
        });
    }

    public void toggleLocationSelection(MyLocation location) {
        Boolean currentSelection = location.isSelected();
        boolean newSelection = (currentSelection == null) ? true : !currentSelection;
        location.setSelected(newSelection);

        // Check if any item is still selected
        List<MyLocation> currentLocations = locationsLiveData.getValue();
        boolean hasSelection = false;
        if (currentLocations != null) {
            for (MyLocation loc : currentLocations) {
                if (loc.isSelected() != null && loc.isSelected()) {
                    hasSelection = true;
                    break;
                }
            }
        }

        setDeleteButtonVisibility(hasSelection);
    }

    public void setDeleteButtonVisibility(boolean isVisible) {
        deleteButtonVisibility.setValue(isVisible);
    }

    public void clearAllSelections() {
        List<MyLocation> currentLocations = locationsLiveData.getValue();
        if (currentLocations != null) {
            for (MyLocation location : currentLocations) {
                location.setSelected(false);
            }
            locationsLiveData.setValue(currentLocations);
            setDeleteButtonVisibility(false);
        }
    }
}