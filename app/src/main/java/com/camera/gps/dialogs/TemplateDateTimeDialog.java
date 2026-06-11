package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.adapter.TemplateDateTimeFormat_Adapter;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SP;

import java.util.ArrayList;

public class TemplateDateTimeDialog extends Dialog implements TemplateDateTimeFormat_Adapter.DateClicksListener {

    private final Context context;
    private RecyclerView rvDateTime;
    private TemplateDateTimeFormat_Adapter adapter;
    private TextView btnSaveDateTime, btnCanceDateTime;
    private ImageView btnCloseDialog;
    private SP msp;
    private int selectedPosition = -1;
    private int originalSelectedPosition = -1;
    private OnDateTimeSelectedListener listener;
    private ArrayList<DateFormatModel> dateFormats;
    private int templateId;

    public TemplateDateTimeDialog(@NonNull Context context, int templateId, OnDateTimeSelectedListener listener) {
        super(context);
        this.context = context;
        this.templateId = templateId;
        this.listener = listener;
        this.msp = new SP(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_date_format);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        loadDateFormats();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        rvDateTime = findViewById(R.id.rv_time);
        btnSaveDateTime = findViewById(R.id.btnSaveDateTime);
        btnCanceDateTime = findViewById(R.id.btnCancelDateTime);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void loadDateFormats() {
        // Create date format models from the string array
        String[] timeFormatArray = context.getResources().getStringArray(R.array.time_formate_array);
        dateFormats = new ArrayList<>();

        for (int i = 0; i < timeFormatArray.length; i++) {
            DateFormatModel model = new DateFormatModel();
            String fullFormat = timeFormatArray[i];

            model.setTime_format(fullFormat); // This is for UI display
            model.setFormat_Combined(fullFormat); // Combined format

            // Parse the combined format to separate date and time
            String dateFormat = extractDateFormat(fullFormat);
            String timeFormat = extractTimeFormat(fullFormat);

            model.setFormat_Date(dateFormat);
            model.setFormat_Time(timeFormat);
            model.setSelected(0); // Initially none selected
            dateFormats.add(model);
        }

        // Apply pending selection if available
        if (selectedPosition >= 0 && selectedPosition < dateFormats.size()) {
            dateFormats.get(selectedPosition).setSelected(1);
            originalSelectedPosition = selectedPosition;
        }
    }

    private String extractDateFormat(String combinedFormat) {
        // Extract date part from combined format
        // Look for common date patterns
        if (combinedFormat.contains("dd-MM-yyyy")) {
            return "dd-MM-yyyy";
        } else if (combinedFormat.contains("MM/dd/yyyy")) {
            return "MM/dd/yyyy";
        } else if (combinedFormat.contains("dd/MM/yyyy")) {
            return "dd/MM/yyyy";
        } else if (combinedFormat.contains("MM/dd/yy")) {
            return "MM/dd/yy";
        } else if (combinedFormat.contains("dd/MM/yy")) {
            return "dd/MM/yy";
        }

        // Fallback: try to extract first part before space
        String[] parts = combinedFormat.split(" ");
        return parts.length > 0 ? parts[0] : combinedFormat;
    }

    private String extractTimeFormat(String combinedFormat) {
        // Extract time part from combined format
        // Look for common time patterns
        if (combinedFormat.contains("HH:mm:ss a")) {
            return "HH:mm:ss a";
        } else if (combinedFormat.contains("hh:mm:ss aa")) {
            return "hh:mm:ss aa";
        } else if (combinedFormat.contains("HH:mm:ss")) {
            return "HH:mm:ss";
        } else if (combinedFormat.contains("hh:mm aa")) {
            return "hh:mm aa";
        } else if (combinedFormat.contains("HH:mm")) {
            return "HH:mm";
        }

        // If no time found, return empty or the full format
        if (combinedFormat.contains(" ")) {
            String[] parts = combinedFormat.split(" ");
            if (parts.length > 1) {
                StringBuilder timeBuilder = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) timeBuilder.append(" ");
                    timeBuilder.append(parts[i]);
                }
                return timeBuilder.toString();
            }
        }

        return ""; // No time part found
    }

    private void setupRecyclerView() {
        rvDateTime.setLayoutManager(new LinearLayoutManager(context));
        adapter = new TemplateDateTimeFormat_Adapter(context, dateFormats, this);
        rvDateTime.setAdapter(adapter);

        // Scroll to selected position if available
        if (selectedPosition >= 0 && selectedPosition < dateFormats.size()) {
            rvDateTime.post(() -> rvDateTime.scrollToPosition(selectedPosition));
        }
    }

    private void setupClickListeners() {
        btnSaveDateTime.setOnClickListener(v -> {
            if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
                DateFormatModel selectedFormat = dateFormats.get(selectedPosition);

                // Notify listener - Edit Activity will handle the saving
                if (listener != null) {
                    listener.onDateTimeSelected(selectedFormat, selectedPosition);
                }
            }
            dismiss();
        });

        btnCanceDateTime.setOnClickListener(v -> {
            resetToOriginalSelection();
            dismiss();
        });

        btnCloseDialog.setOnClickListener(v -> {
            resetToOriginalSelection();
            dismiss();
        });
    }

    @Override
    public void onDateClick(int position) {
        selectedPosition = position;

        if (adapter != null && dateFormats != null) {
            for (int i = 0; i < dateFormats.size(); i++) {
                dateFormats.get(i).setSelected(i == position ? 1 : 0);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void resetToOriginalSelection() {
        if (dateFormats != null && originalSelectedPosition >= 0) {
            selectedPosition = originalSelectedPosition;
            for (int i = 0; i < dateFormats.size(); i++) {
                dateFormats.get(i).setSelected(i == originalSelectedPosition ? 1 : 0);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDialogDismissed();
        }
    }

    // Method to set selected position externally
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        this.originalSelectedPosition = position;

        // If data is already loaded, apply the selection immediately
        if (dateFormats != null && position >= 0 && position < dateFormats.size()) {
            // Clear all selections
            for (int i = 0; i < dateFormats.size(); i++) {
                dateFormats.get(i).setSelected(0);
            }
            // Set the selected position
            dateFormats.get(position).setSelected(1);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
                rvDateTime.scrollToPosition(position);
            }
        }
        // If data is not loaded yet, the selection will be applied in loadDateFormats()
    }

    // Method to get currently selected format
    public DateFormatModel getSelectedFormat() {
        if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
            return dateFormats.get(selectedPosition);
        }
        return null;
    }
}