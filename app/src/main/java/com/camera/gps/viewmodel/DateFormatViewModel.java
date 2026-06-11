package com.camera.gps.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import com.camera.gps.model.DateFormatModel;
import com.camera.gps.repositories.DateFormatRepository;

public class DateFormatViewModel extends AndroidViewModel {
    private DateFormatRepository repository;
    private MutableLiveData<ArrayList<DateFormatModel>> dateFormatsLiveData;
    private MutableLiveData<Integer> selectedItemLiveData;
    private MutableLiveData<Boolean> navigateToMainLiveData;

    public DateFormatViewModel(@NonNull Application application) {
        super(application);
        repository = new DateFormatRepository(application);
        dateFormatsLiveData = new MutableLiveData<>();
        selectedItemLiveData = new MutableLiveData<>();
        navigateToMainLiveData = new MutableLiveData<>();
        loadDateFormats();
    }

    public LiveData<ArrayList<DateFormatModel>> getDateFormats() {
        return dateFormatsLiveData;
    }

    public LiveData<Integer> getSelectedItem() {
        return selectedItemLiveData;
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMainLiveData;
    }

    private void loadDateFormats() {
        ArrayList<DateFormatModel> formats = repository.getDateFormats();
        dateFormatsLiveData.setValue(formats);
    }

    public void selectDateFormat(int position) {
        ArrayList<DateFormatModel> currentList = dateFormatsLiveData.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (i == position) {
                    currentList.get(i).setSelected(1);
                } else {
                    currentList.get(i).setSelected(0);
                }
            }

            DateFormatModel selectedFormat = currentList.get(position);
            repository.saveSelectedFormat(
                    selectedFormat.getFormat_Combined(),
                    selectedFormat.getFormat_Date(),
                    selectedFormat.getFormat_Time()
            );

            dateFormatsLiveData.setValue(currentList);
            selectedItemLiveData.setValue(position);
        }
    }


    public void reloadFormats() {
        ArrayList<DateFormatModel> formats = repository.getDateFormats();
        dateFormatsLiveData.setValue(formats);
    }
    public void onDoneClicked(int selectedPosition) {
        ArrayList<DateFormatModel> currentList = dateFormatsLiveData.getValue();
        if (currentList != null && selectedPosition >= 0 && selectedPosition < currentList.size()) {
            DateFormatModel selectedFormat = currentList.get(selectedPosition);
            repository.saveSelectedFormat(
                    selectedFormat.getFormat_Combined(),
                    selectedFormat.getFormat_Date(),
                    selectedFormat.getFormat_Time()
            );
            navigateToMainLiveData.setValue(true);
        }
    }
}