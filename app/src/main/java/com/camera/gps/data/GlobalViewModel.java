package com.camera.gps.data;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.camera.gps.database.entity.MyLocation;

import java.io.IOException;
import java.util.List;

import com.camera.gps.database.entity.Photo;

public final class GlobalViewModel extends ViewModel {
    private final GlobalRepository repository;

    public GlobalViewModel(GlobalRepository repository) {
        this.repository = repository;
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
