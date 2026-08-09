package com.camera.gps.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import kotlin.jvm.internal.Intrinsics;
import com.camera.gps.MyApplication;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.database.entity.Photo;

@Database(entities = {MyLocation.class, Photo.class}, version = 13, exportSchema = false)

public abstract class GpsMapPhotoDatabase extends RoomDatabase {
    private static GpsMapPhotoDatabase INSTANCE;

    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE MyLocation ADD COLUMN defaultTitle TEXT");
        }
    };

    public abstract GpsMapPhotoDatabaseDao getRedenesDatabaseDao();

    public static GpsMapPhotoDatabase getInstance(Context context) {
        GpsMapPhotoDatabase gpsMapPhotoDatabase;
        Intrinsics.checkNotNullParameter(context, "context");
        synchronized (MyApplication.context()) {
            gpsMapPhotoDatabase = GpsMapPhotoDatabase.INSTANCE;
            if (gpsMapPhotoDatabase == null) {
                gpsMapPhotoDatabase = Room.databaseBuilder(
                                context.getApplicationContext(),
                                GpsMapPhotoDatabase.class,
                                "redenes_database")
                        .addMigrations(MIGRATION_12_13)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
                GpsMapPhotoDatabase.INSTANCE = gpsMapPhotoDatabase;
            }
        }
        return gpsMapPhotoDatabase;
    }
}
