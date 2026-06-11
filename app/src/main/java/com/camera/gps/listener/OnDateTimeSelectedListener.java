package com.camera.gps.listener;

import com.camera.gps.model.DateFormatModel;

public interface OnDateTimeSelectedListener {
    void onDateTimeSelected(DateFormatModel selectedFormat, int position);
    void onDialogDismissed();
}