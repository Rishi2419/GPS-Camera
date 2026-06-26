
package com.camera.gps.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Photo")
public final class Photo implements Serializable {
    private String address;
    private String title;
    private Boolean bgMode;
    private String date;
    @PrimaryKey
    private Integer id;
    private String imagePath;
    private Boolean isSelected;
    private String latitude;
    private String longitude;
    private String time;
    private Integer type;
    private String fontStyle;
    private String dateTimeTaken;
    private Integer map_type;
    private String mapImagePath;
    private Boolean show_watermark;

    // ✅ New fields
    private String lat_dms;
    private String long_dms;
    private int current_bg_color;
    private int current_text_color;
    private int current_datetime_color;
    private int ratio; // ✅ newly added field

    public Photo() {
        this(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, 0, 0, 0, 0);
    }

    public Photo(String title, Integer id, String address, String latitude, String longitude,
                 String date, String time, String imagePath,
                 Integer type, Boolean bgMode, Boolean isSelected,
                 String fontStyle, String dateTimeTaken,
                 Integer map_type, String mapImagePath, Boolean show_watermark,
                 String lat_dms, String long_dms,
                 int current_bg_color, int current_text_color, int current_datetime_color,
                 int ratio) {
        this.title = title;
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.time = time;
        this.imagePath = imagePath;
        this.type = type;
        this.bgMode = bgMode;
        this.isSelected = isSelected;
        this.fontStyle = fontStyle;
        this.dateTimeTaken = dateTimeTaken;
        this.map_type = map_type;
        this.mapImagePath = mapImagePath;
        this.show_watermark = show_watermark;
        this.lat_dms = lat_dms;
        this.long_dms = long_dms;
        this.current_bg_color = current_bg_color;
        this.current_text_color = current_text_color;
        this.current_datetime_color = current_datetime_color;
        this.ratio = ratio;
    }

    // ---------------- Getters & Setters ----------------
    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getLat_dms() {
        return lat_dms;
    }

    public void setLat_dms(String lat_dms) {
        this.lat_dms = lat_dms;
    }

    public String getLong_dms() {
        return long_dms;
    }

    public void setLong_dms(String long_dms) {
        this.long_dms = long_dms;
    }

    public int getCurrent_bg_color() {
        return current_bg_color;
    }

    public void setCurrent_bg_color(int current_bg_color) {
        this.current_bg_color = current_bg_color;
    }

    public int getCurrent_text_color() {
        return current_text_color;
    }

    public void setCurrent_text_color(int current_text_color) {
        this.current_text_color = current_text_color;
    }

    public int getCurrent_datetime_color() {
        return current_datetime_color;
    }

    public void setCurrent_datetime_color(int current_datetime_color) {
        this.current_datetime_color = current_datetime_color;
    }

    public Integer getMap_type() {
        return map_type;
    }

    public void setMap_type(Integer map_type) {
        this.map_type = map_type;
    }

    public String getMapImagePath() {
        return mapImagePath;
    }

    public void setMapImagePath(String mapImagePath) {
        this.mapImagePath = mapImagePath;
    }

    public Boolean getShow_watermark() {
        return show_watermark;
    }

    public void setShow_watermark(Boolean show_watermark) {
        this.show_watermark = show_watermark;
    }

    public String getDateTimeTaken() {
        return dateTimeTaken;
    }

    public void setDateTimeTaken(String dateTimeTaken) {
        this.dateTimeTaken = dateTimeTaken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String str) {
        this.address = str;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String str) {
        this.latitude = str;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String str) {
        this.longitude = str;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String str) {
        this.date = str;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String str) {
        this.time = str;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String str) {
        this.imagePath = str;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
    }

    public Boolean getBgMode() {
        return this.bgMode;
    }

    public void setBgMode(Boolean bool) {
        this.bgMode = bool;
    }

    public Boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(Boolean bool) {
        this.isSelected = bool;
    }
}
