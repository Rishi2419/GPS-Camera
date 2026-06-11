package com.camera.gps.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.database.entity.Photo;

@Dao
public interface GpsMapPhotoDatabaseDao {

    @Query("DELETE FROM MyLocation")
    void deleteAllLocation();

    @Query("DELETE FROM Photo")
    void deleteAllPhoto();

    @Query("DELETE FROM MyLocation WHERE id = :id")
    int deleteLocation(String id);

    @Query("DELETE FROM MyLocation WHERE id IN (:idList)")
    int deleteLocations(List<Integer> idList);

    @Query("DELETE FROM Photo WHERE id = :id")
    int deletePhoto(String id);

    @Query("DELETE FROM Photo WHERE id IN (:list)")
    int deletePhotos(List<Integer> list);

//    @Query("DELETE FROM MyLocation WHERE id LIKE :id")
//    int deleteLocation(String id);
//
//    @Query("DELETE FROM MyLocation WHERE id LIKE :idList")
//    int deleteLocations(List<Integer> idList);
//
//    @Query("DELETE FROM Photo WHERE id LIKE :id")
//    int deletePhoto(String id);
//
//    @Query("DELETE FROM Photo WHERE id LIKE :list")
//    int deletePhotos(List<Integer> list);

    @Query("SELECT * FROM MyLocation")
    List<MyLocation> getAllLocation();
    @Query("SELECT * FROM MyLocation WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    MyLocation getLocationByTitle(String title);

    @Query("SELECT * FROM Photo")
    List<Photo> getAllPhoto();

    @Insert
    long insertLocation(MyLocation location);

    @Insert
    long insertPhoto(Photo photo);

    @Update
    int updateLocation(MyLocation location);

    @Update
    int updatePhoto(Photo photo);
}