package com.camera.gps.data;

import static android.location.Location.convert;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.camera.gps.database.entity.MyLocation;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.camera.gps.database.entity.Photo;
import com.camera.gps.model.MarkerModel;
import com.camera.gps.util.Constant;

public final class GlobalViewModel extends ViewModel {
    private MutableLiveData<MarkerModel> markerOpMutableLiveData;
    private final GlobalRepository repository;

    // Cache for location-based media to ensure consistency
    private Map<String, MediaInfo> locationMediaCache = new HashMap<>();

    public GlobalViewModel(GlobalRepository repository) {
        this.repository = repository;
        this.markerOpMutableLiveData = new MutableLiveData<>();
    }

    // Inner class to store media information
    private static class MediaInfo {
        String path;
        boolean isVideo;
        long dateAdded;

        MediaInfo(String path, boolean isVideo, long dateAdded) {
            this.path = path;
            this.isVideo = isVideo;
            this.dateAdded = dateAdded;
        }
    }

    public MutableLiveData<Long> insertPhoto(Photo photo) {

        MutableLiveData<Long> insertLiveData = new MutableLiveData<>();

        class InsertData extends AsyncTask<Void, Void, Long> {

            @Override
            protected Long doInBackground(Void... voids) {
                return repository.insertPhoto(photo);
            }

            @Override
            protected void onPostExecute(Long id) {
                super.onPostExecute(id);
                insertLiveData.setValue(id);
            }
        }

        new InsertData().execute();
        return insertLiveData;
    }

    public LiveData<Integer> deletePhoto(Photo photo) {

        MutableLiveData<Integer> deleteLiveData = new MutableLiveData<>();

        class DeleteData extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return repository.deletePhoto(photo);
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                deleteLiveData.setValue(id);
            }
        }

        new DeleteData().execute();
        return deleteLiveData;
    }

    public LiveData<Integer> deletePhotos(List<Photo> photos) {

        MutableLiveData<Integer> deleteLiveData = new MutableLiveData<>();

        class DeleteData extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return repository.deletePhotos(photos);
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                deleteLiveData.setValue(id);
            }
        }

        new DeleteData().execute();
        return deleteLiveData;
    }

    public LiveData<MyLocation> getLocationByTitle(String title) {
        MutableLiveData<MyLocation> data = new MutableLiveData<>();

        class CheckLocationTask extends AsyncTask<Void, Void, MyLocation> {
            @Override
            protected MyLocation doInBackground(Void... voids) {
                return repository.getLocationByTitle(title);
            }

            @Override
            protected void onPostExecute(MyLocation location) {
                super.onPostExecute(location);
                data.setValue(location);
            }
        }

        new CheckLocationTask().execute();
        return data;
    }


    public LiveData<Integer> deleteLocations(List<MyLocation> locations) {

        MutableLiveData<Integer> deleteLiveData = new MutableLiveData<>();

        class DeleteData extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return repository.deleteLocations(locations);
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                deleteLiveData.setValue(id);
            }
        }

        new DeleteData().execute();
        return deleteLiveData;
    }

    public LiveData<Integer> updatePhoto(Photo photo) {

        MutableLiveData<Integer> updatePhotoLiveData = new MutableLiveData<>();

        class UpdatePhotoTask extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return repository.updatePhoto(photo);
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                updatePhotoLiveData.setValue(id);
            }
        }

        new UpdatePhotoTask().execute();
        return updatePhotoLiveData;

    }

    public LiveData<List<Photo>> getAllPhoto() {

        MutableLiveData<List<Photo>> updatePhotoLiveData = new MutableLiveData<>();

        class PhotoListTask extends AsyncTask<Void, Void, List<Photo>> {

            @Override
            protected List<Photo> doInBackground(Void... voids) {
                return repository.getAllPhoto();
            }

            @Override
            protected void onPostExecute(List<Photo> list) {
                super.onPostExecute(list);
                updatePhotoLiveData.setValue(list);
            }
        }

        new PhotoListTask().execute();
        return updatePhotoLiveData;
    }

    public LiveData<Long> insertLocation(MyLocation location) {

        MutableLiveData<Long> updatePhotoLiveData = new MutableLiveData<>();

        class InsertLocationTask extends AsyncTask<Void, Void, Long> {

            @Override
            protected Long doInBackground(Void... voids) {
                return repository.insertLocation(location);
            }

            @Override
            protected void onPostExecute(Long id) {
                super.onPostExecute(id);
                updatePhotoLiveData.setValue(id);
            }
        }

        new InsertLocationTask().execute();
        return updatePhotoLiveData;
    }

    public LiveData<Integer> deleteLocation(MyLocation location) {
        MutableLiveData<Integer> deleteLocationLiveData = new MutableLiveData<>();

        class DeleteLocationTask extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return Integer.parseInt(String.valueOf(repository.deleteLocation(location)));
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                deleteLocationLiveData.setValue(id);
            }
        }

        new DeleteLocationTask().execute();
        return deleteLocationLiveData;
    }

    public LiveData<Integer> updateLocation(MyLocation location) {
        MutableLiveData<Integer> updateLocationLiveData = new MutableLiveData<>();

        class UpdateLocationTask extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return Integer.parseInt(String.valueOf(repository.updateLocation(location)));
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);
                updateLocationLiveData.setValue(id);
            }
        }

        new UpdateLocationTask().execute();
        return updateLocationLiveData;
    }



    public MutableLiveData<List<MyLocation>> getAllLocation() {
        MutableLiveData<List<MyLocation>> getAllLocation = new MutableLiveData<>();

        class UpdateLocationTask extends AsyncTask<Void, Void, List<MyLocation>> {

            @Override
            protected List<MyLocation> doInBackground(Void... voids) {
                return repository.getAllLocation();
            }

            @Override
            protected void onPostExecute(List<MyLocation> locations) {
                super.onPostExecute(locations);
                getAllLocation.setValue(locations);
            }
        }

        new UpdateLocationTask().execute();
        return getAllLocation;
    }


//    public MutableLiveData<List> getAllLocation() {
//
//        MutableLiveData<List> getAllLocation = new MutableLiveData<>();
//
//        class UpdateLocationTask extends AsyncTask<Void, Void, List> {
//
//            @Override
//            protected List doInBackground(Void... voids) {
//                return repository.getAllLocation();
//            }
//
//            @Override
//            protected void onPostExecute(List id) {
//                super.onPostExecute(id);
//                getAllLocation.setValue(id);
//            }
//        }
//
//        new UpdateLocationTask().execute();
//        return getAllLocation;
//    }

    public MutableLiveData<MarkerModel> getMarkerOpMutableLiveData() {
        return this.markerOpMutableLiveData;
    }

    public void setMarkerOpMutableLiveData(MutableLiveData<MarkerModel> mutableLiveData) {
        this.markerOpMutableLiveData = mutableLiveData;
    }

    private void addMarker(Context context, String str, double d, double d2, String str2) {
        try {
//            Bitmap createMarkerBitmap = Constant.Companion.createMarkerBitmap(context, str, false, "");
            Bitmap createMarkerBitmap = Constant.Companion.createUserBitmap(context, str);
            MarkerOptions icon = new MarkerOptions().position(new LatLng(d, d2)).icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap));
            if (str2 != null) {
                icon.title(str2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<MarkerModel>> getImagesFromGalleyOnlyLocation(Context context) {
        MutableLiveData<List<MarkerModel>> mutableLiveData = new MutableLiveData<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            ArrayList<MarkerModel> list = new ArrayList<>();
            ArrayList<Object> arrayList = new ArrayList<>();

            // Clear previous cache
            locationMediaCache.clear();

            // Query both images and videos
            queryMediaFiles(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false);
            queryMediaFiles(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true);

            // Convert cached media to markers
            for (MediaInfo mediaInfo : locationMediaCache.values()) {
                MarkerModel markerModel = new MarkerModel(0, 0, mediaInfo.path, "Loading..", mediaInfo.isVideo ? "video" : "image");
                arrayList.add(markerModel);
                GlobalViewModel.this.getMarkerOpMutableLiveData().postValue(markerModel);
            }

            handler.post(() -> mutableLiveData.postValue(list));
        });
        return mutableLiveData;
    }

    private void queryMediaFiles(Context context, android.net.Uri uri, boolean isVideo) {
        String[] projection;
        String selection;

        if (isVideo) {
            projection = new String[]{"_id", "_data", "date_added"};
            selection = null; // Get all videos
        } else {
            projection = new String[]{"_id", "_data", "date_added"};
            selection = "_data LIKE '%/DCIM/Camera/%'"; // Only camera images
        }

        Cursor query = context.getContentResolver().query(uri, projection, selection, null, "date_added DESC");

        if (query != null && query.moveToFirst()) {
            do {
                query.getLong(query.getColumnIndexOrThrow("_id"));
                String filePath = query.getString(query.getColumnIndexOrThrow("_data"));
                long dateAdded = query.getLong(query.getColumnIndexOrThrow("date_added"));

                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        double[] coordinates = getGPSCoordinates(file);
                        if (coordinates != null) {
                            String locationKey = String.format("%.6f,%.6f", coordinates[0], coordinates[1]);

                            // Only add if this location doesn't have media yet, or if this media is newer
                            if (!locationMediaCache.containsKey(locationKey) ||
                                    locationMediaCache.get(locationKey).dateAdded < dateAdded) {
                                locationMediaCache.put(locationKey, new MediaInfo(filePath, isVideo, dateAdded));
                            }
                        }
                    }
                }
            } while (query.moveToNext());
            query.close();
        }
    }

    private double[] getGPSCoordinates(File file) {
        try {
            ExifInterface exifInterface = new ExifInterface(file);
            String latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (latitude != null && longitude != null) {
                double lat = convert(latitude);
                double lng = convert(longitude);

                // Apply the reference (N/S for latitude, E/W for longitude)
                if ("S".equals(latitudeRef)) lat = -lat;
                if ("W".equals(longitudeRef)) lng = -lng;

                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isImageFile(File file) {
        String filename = file.getName().toLowerCase();
        return filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg");
    }

    public static boolean isVideoFile(File file) {
        String filename = file.getName().toLowerCase();
        return filename.endsWith(".mp4") || filename.endsWith(".avi") || filename.endsWith(".mov") ||
                filename.endsWith(".mkv") || filename.endsWith(".3gp");
    }

    private String getAddress(Geocoder geocoder, double d, double d2) {
        Address address = null;
        try {
            List<Address> fromLocation = geocoder.getFromLocation(d, d2, 1);
            if (fromLocation == null || fromLocation.size() <= 0) {
                return "Unknown";
            }
            address = fromLocation.get(0);
            return address.getAddressLine(0) + ",";
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}