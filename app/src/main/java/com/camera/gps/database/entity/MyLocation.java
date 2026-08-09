package com.camera.gps.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "MyLocation")
public class MyLocation implements Serializable {
    private String address;
    private String date;
    private String defaultTitle;
    @PrimaryKey
    private Integer id;
    private Boolean isSelected;
    private String latitude;
    private String longitude;
    private String time;
    private String title;

    public MyLocation() {
        this(null, null, null, null, null, null, null, null, null);
    }

    @Ignore
    public MyLocation(Integer num, String str, String str2, String str3, String str4, String str5, String str6, Boolean bool) {
        this(num, str, str2, str3, str4, str5, str6, bool, null);
    }

    @Ignore
    public MyLocation(Integer num, String str, String str2, String str3, String str4,
                      String str5, String str6, Boolean bool, String defaultTitle) {
        this.id = num;
        this.title = str;
        this.date = str2;
        this.time = str3;
        this.address = str4;
        this.latitude = str5;
        this.longitude = str6;
        this.isSelected = bool;
        this.defaultTitle = defaultTitle;
    }

    public final Integer getId() {
        return this.id;
    }

    public final void setId(Integer num) {
        this.id = num;
    }

    public final String getTitle() {
        return this.title;
    }

    public final void setTitle(String str) {
        this.title = str;
    }

    public final String getDate() {
        return this.date;
    }

    public final void setDate(String str) {
        this.date = str;
    }

    public final String getTime() {
        return this.time;
    }

    public final void setTime(String str) {
        this.time = str;
    }

    public final String getAddress() {
        return this.address;
    }

    public final void setAddress(String str) {
        this.address = str;
    }

    public final String getDefaultTitle() {
        return this.defaultTitle;
    }

    public final void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public final String getLatitude() {
        return this.latitude;
    }

    public final void setLatitude(String str) {
        this.latitude = str;
    }

    public final String getLongitude() {
        return this.longitude;
    }

    public final void setLongitude(String str) {
        this.longitude = str;
    }

    public final Boolean isSelected() {
        return this.isSelected;
    }

    public final void setSelected(Boolean bool) {
        this.isSelected = bool;
    }
}
