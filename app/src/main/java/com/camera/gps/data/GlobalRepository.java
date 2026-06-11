package com.camera.gps.data;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import com.camera.gps.database.GpsMapPhotoDatabase;
import com.camera.gps.database.GpsMapPhotoDatabaseDao;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.database.entity.Photo;

public class GlobalRepository {
    private GpsMapPhotoDatabaseDao dataSource;

    public GlobalRepository(Application application) {
        this.dataSource = GpsMapPhotoDatabase.getInstance(application).getRedenesDatabaseDao();
    }

    public GpsMapPhotoDatabaseDao getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(GpsMapPhotoDatabaseDao gpsMapPhotoDatabaseDao) {
        this.dataSource = gpsMapPhotoDatabaseDao;
    }

    public long insertPhoto(Photo photo) {
        return this.dataSource.insertPhoto(photo);
    }

    public int deletePhoto(Photo photo) {
        return this.dataSource.deletePhoto(String.valueOf(photo.getId()));
    }

    public int deletePhotos(List<Photo> list) {
        ArrayList<Integer> IDList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            IDList.add(list.get(i).getId());
        }
        return dataSource.deletePhotos(IDList);
    }

    public int updatePhoto(Photo photo) {
        return dataSource.updatePhoto(photo);
    }

    public List<Photo> getAllPhoto() {
        return this.dataSource.getAllPhoto();
    }

    public long insertLocation(MyLocation location) {
        return dataSource.insertLocation(location);
    }

    public MyLocation getLocationByTitle(String title) {
        return dataSource.getLocationByTitle(title);
    }

    public int deleteLocation(MyLocation location) {
        return dataSource.deleteLocation(location.getId().toString());
    }

    public int updateLocation(MyLocation location) {
        return dataSource.updateLocation(location);
    }

    public int deleteLocations(List<MyLocation> list) {
        ArrayList<Integer> IDList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            IDList.add(list.get(i).getId());
        }
        return dataSource.deleteLocations(IDList);
    }

    public List<MyLocation> getAllLocation() {
        return this.dataSource.getAllLocation();
    }
}