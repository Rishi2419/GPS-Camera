package com.camera.gps.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import kotlin.jvm.internal.Intrinsics;
import com.camera.gps.MyApplication;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.database.entity.Photo;

@Database(entities = {MyLocation.class, Photo.class}, version = 10, exportSchema = false)

public abstract class GpsMapPhotoDatabase extends RoomDatabase {
    private static GpsMapPhotoDatabase INSTANCE;

    public abstract GpsMapPhotoDatabaseDao getRedenesDatabaseDao();

    public static GpsMapPhotoDatabase getInstance(Context context) {
        GpsMapPhotoDatabase gpsMapPhotoDatabase;
        Intrinsics.checkNotNullParameter(context, "context");
        synchronized (MyApplication.context()) {
            gpsMapPhotoDatabase = GpsMapPhotoDatabase.INSTANCE;
            if (gpsMapPhotoDatabase == null) {
                gpsMapPhotoDatabase = Room.databaseBuilder(context.getApplicationContext(), GpsMapPhotoDatabase.class, "redenes_database").allowMainThreadQueries().fallbackToDestructiveMigration().build();
                GpsMapPhotoDatabase.INSTANCE = gpsMapPhotoDatabase;
            }
        }
        return gpsMapPhotoDatabase;
    }
}