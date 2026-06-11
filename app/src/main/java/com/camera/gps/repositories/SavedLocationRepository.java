package com.camera.gps.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.MyLocation;

import java.util.List;

public class SavedLocationRepository {

    private final GlobalViewModel globalViewModel;

    public SavedLocationRepository(Application application) {
        GlobalViewModelFactory factory = new GlobalViewModelFactory(application);
        globalViewModel = factory.create(GlobalViewModel.class);
    }

    public LiveData<List<MyLocation>> getAllLocation() {
        return globalViewModel.getAllLocation();
    }

    public LiveData<Integer> deleteLocations(List<MyLocation> locations) {
        return globalViewModel.deleteLocations(locations);
    }

    public LiveData<Long> insertLocation(MyLocation location) {
        return globalViewModel.insertLocation(location);
    }

    public LiveData<Integer> deleteLocation(MyLocation location) {
        return globalViewModel.deleteLocation(location);
    }

    public LiveData<Integer> updateLocation(MyLocation location) {
        return globalViewModel.updateLocation(location);
    }
}