//package com.camera.gps.repositories;
//
//
//import android.content.Context;
//
//import com.appizona.yehiahd.fastsave.FastSave;
//
//import java.util.ArrayList;
//
//import com.camera.gps.MyApplication;
//import com.camera.gps.R;
//import com.camera.gps.model.DateFormatModel;
//import com.camera.gps.util.SP;
//
//public class DateFormatRepository {
//    private Context context;
//    private SP msp;
//
//    public DateFormatRepository(Context context) {
//        this.context = context;
//        this.msp = new SP(context);
//    }
//
//    public ArrayList<DateFormatModel> getDateFormats() {
//        String[] time_format = context.getResources().getStringArray(R.array.time_formate_array);
//        ArrayList<DateFormatModel> time_list = new ArrayList<>();
//
//        for (String s : time_format) {
//            DateFormatModel dateFormatModel = new DateFormatModel();
//            dateFormatModel.setTime_format(s); // For UI display
//            dateFormatModel.setFormat_Combined(s); // Store the combined format
//
//            // Split the format into date and time parts
//            String[] splitFormats = splitDateTimeFormat(s);
//            dateFormatModel.setFormat_Date(splitFormats[0]);
//            dateFormatModel.setFormat_Time(splitFormats[1]);
//
//            if (s.equalsIgnoreCase(msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a"))) {
//                dateFormatModel.setSelected(1);
//            } else {
//                dateFormatModel.setSelected(0);
//            }
//            time_list.add(dateFormatModel);
//        }
//        return time_list;
//    }
//
//    private String[] splitDateTimeFormat(String combinedFormat) {
//        String dateFormat = "";
//        String timeFormat = "";
//
//        // Split based on common patterns
//        if (combinedFormat.contains(" ")) {
//            String[] parts = combinedFormat.split(" ", 2);
//            dateFormat = parts[0];
//            if (parts.length > 1) {
//                timeFormat = parts[1];
//            }
//        } else {
//            // If no space, it's likely date only
//            dateFormat = combinedFormat;
//            timeFormat = "";
//        }
//
//        return new String[]{dateFormat, timeFormat};
//    }
//
//    public void saveSelectedFormat(String format_Combined, String format_Date, String format_Time) {
//        // Save all three formats
//        msp.setString(context, MyApplication.TIME_FORMAT, format_Combined);
//        msp.setString(context, MyApplication.FORMAT_DATE, format_Date);
//        msp.setString(context, MyApplication.FORMAT_TIME, format_Time);
//
//        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, format_Combined);
//        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, format_Date);
//        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, format_Time);
//    }
//
//    public String getCurrentFormat() {
//        return msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
//    }
//
//    public String getCurrentDateFormat() {
//        return msp.getString(context, MyApplication.FORMAT_DATE, "dd-MM-yyyy");
//    }
//
//    public String getCurrentTimeFormat() {
//        return msp.getString(context, MyApplication.FORMAT_TIME, "HH:mm:ss a");
//    }
//}


package com.camera.gps.repositories;

import android.content.Context;

import com.appizona.yehiahd.fastsave.FastSave;

import java.util.ArrayList;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.util.SP;

public class DateFormatRepository {
    private Context context;
    private SP msp;

    public DateFormatRepository(Context context) {
        this.context = context;
        this.msp = new SP(context);
    }

    public ArrayList<DateFormatModel> getDateFormats() {
        // Get the default format from preferences
        String currentFormat = msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
        return getDateFormats(currentFormat);
    }

    // Overloaded method that accepts current format parameter
//    public ArrayList<DateFormatModel> getDateFormats(String currentActiveFormat) {
//        String[] time_format = context.getResources().getStringArray(R.array.time_formate_array);
//        ArrayList<DateFormatModel> time_list = new ArrayList<>();
//
//        // Use provided current format, or fall back to saved preference
//        String formatToMatch = currentActiveFormat != null ? currentActiveFormat :
//                msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
//
//        for (String s : time_format) {
//            DateFormatModel dateFormatModel = new DateFormatModel();
//            dateFormatModel.setTime_format(s); // For UI display
//            dateFormatModel.setFormat_Combined(s); // Store the combined format
//
//            // Split the format into date and time parts
//            String[] splitFormats = splitDateTimeFormat(s);
//            dateFormatModel.setFormat_Date(splitFormats[0]);
//            dateFormatModel.setFormat_Time(splitFormats[1]);
//
//            // Check against the current active format instead of just saved preference
//            if (s.equalsIgnoreCase(formatToMatch)) {
//                dateFormatModel.setSelected(1);
//            } else {
//                dateFormatModel.setSelected(0);
//            }
//            time_list.add(dateFormatModel);
//        }
//        return time_list;
//    }


    public ArrayList<DateFormatModel> getDateFormats(String currentActiveFormat) {
        String[] time_format = context.getResources().getStringArray(R.array.time_formate_array);
        ArrayList<DateFormatModel> time_list = new ArrayList<>();

        // Ensure fallback
        String formatToMatch = currentActiveFormat != null ? currentActiveFormat :
                msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");

        for (String s : time_format) {
            DateFormatModel dateFormatModel = new DateFormatModel();
            dateFormatModel.setTime_format(s);
            dateFormatModel.setFormat_Combined(s);

            // Split into date + time parts
            String[] splitFormats = splitDateTimeFormat(s);
            dateFormatModel.setFormat_Date(splitFormats[0]);
            dateFormatModel.setFormat_Time(splitFormats[1]);

            // 🔑 Reset and then select only the active one
            if (s.equalsIgnoreCase(formatToMatch)) {
                dateFormatModel.setSelected(1);
            } else {
                dateFormatModel.setSelected(0);
            }

            time_list.add(dateFormatModel);
        }

        return time_list;
    }

    private String[] splitDateTimeFormat(String combinedFormat) {
        int twentyFourHourStart = combinedFormat.indexOf(" HH");
        int twelveHourStart = combinedFormat.indexOf(" hh");
        int timeStart;
        if (twentyFourHourStart < 0) {
            timeStart = twelveHourStart;
        } else if (twelveHourStart < 0) {
            timeStart = twentyFourHourStart;
        } else {
            timeStart = Math.min(twentyFourHourStart, twelveHourStart);
        }

        if (timeStart < 0) {
            return new String[]{combinedFormat, ""};
        }
        return new String[]{
                combinedFormat.substring(0, timeStart),
                combinedFormat.substring(timeStart + 1)
        };
    }

    public void saveSelectedFormat(String format_Combined, String format_Date, String format_Time) {
        // Save all three formats
        msp.setString(context, MyApplication.TIME_FORMAT, format_Combined);
        msp.setString(context, MyApplication.FORMAT_DATE, format_Date);
        msp.setString(context, MyApplication.FORMAT_TIME, format_Time);

        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, format_Combined);
        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, format_Date);
        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, format_Time);
    }

    public String getCurrentFormat() {
        return msp.getString(context, MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
    }

    public String getCurrentDateFormat() {
        return msp.getString(context, MyApplication.FORMAT_DATE, "dd-MM-yyyy");
    }

    public String getCurrentTimeFormat() {
        return msp.getString(context, MyApplication.FORMAT_TIME, "HH:mm:ss a");
    }
}
