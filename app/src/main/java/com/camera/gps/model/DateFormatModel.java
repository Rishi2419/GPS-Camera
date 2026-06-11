package com.camera.gps.model;

public class DateFormatModel {
    private String Time_format; // UI display
    private String format_Combined;
    private String format_Date;
    private String format_Time;
    private int selected;

    public String getTime_format() {
        return this.Time_format;
    }

    public void setTime_format(String str) {
        this.Time_format = str;
    }

    public String getFormat_Combined() {
        return this.format_Combined;
    }

    public void setFormat_Combined(String format_Combined) {
        this.format_Combined = format_Combined;
    }

    public String getFormat_Date() {
        return this.format_Date;
    }

    public void setFormat_Date(String format_Date) {
        this.format_Date = format_Date;
    }

    public String getFormat_Time() {
        return this.format_Time;
    }

    public void setFormat_Time(String format_Time) {
        this.format_Time = format_Time;
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int i) {
        this.selected = i;
    }
}
