package com.camera.gps.model;

public class DateHeaderItem implements CreationItem {
    private String dateText;

    public DateHeaderItem(String dateText) {
        this.dateText = dateText;
    }

    public String getDateText() {
        return dateText;
    }

    @Override
    public int getItemType() {
        return TYPE_DATE_HEADER;
    }
}