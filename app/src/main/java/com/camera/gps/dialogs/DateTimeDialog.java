//package com.camera.gps.dialogs;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.lifecycle.ViewModelStoreOwner;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.camera.gps.R;
//import com.camera.gps.adapter.DateTimeFormat_Adapter;
//import com.camera.gps.listener.OnDateTimeSelectedListener;
//import com.camera.gps.model.DateFormatModel;
//import com.camera.gps.util.SP;
//import com.camera.gps.viewmodel.DateFormatViewModel;
//
//import java.util.ArrayList;
//
//public class DateTimeDialog extends Dialog implements DateTimeFormat_Adapter.DateClicksListener {
//
//    private final Context context;
//    private RecyclerView rvDateTime;
//    private DateTimeFormat_Adapter adapter;
//    private TextView btnSaveDateTime, btnCanceDateTime;
//    private ImageView btnCloseDialog;
//    private SP msp;
//    private int selectedPosition = -1;
//    private int originalSelectedPosition = -1; // Store original selection
//    private OnDateTimeSelectedListener listener;
//    private DateFormatViewModel dateFormatViewModel;
//    private ArrayList<DateFormatModel> dateFormats;
//
//    public DateTimeDialog(@NonNull Context context, OnDateTimeSelectedListener listener) {
//        super(context);
//        this.context = context;
//        this.listener = listener;
//        this.msp = new SP(context);
//
//        // Initialize ViewModel
//        if (context instanceof ViewModelStoreOwner) {
//            dateFormatViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(DateFormatViewModel.class);
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );
//
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//        decorView.setSystemUiVisibility(uiOptions);
//
//        // Set dialog properties
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.dialog_date_format);
//
//        // Make dialog background transparent
//        if (getWindow() != null) {
//            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
//                    WindowManager.LayoutParams.WRAP_CONTENT);
//        }
//
//        initViews();
//        setupViewModel();
//        setupRecyclerView();
//        setupClickListeners();
//    }
//
//
//    private void initViews() {
//        rvDateTime = findViewById(R.id.rv_time);
//        btnSaveDateTime = findViewById(R.id.btnSaveDateTime);
//        btnCanceDateTime = findViewById(R.id.btnCancelDateTime);
//        btnCloseDialog = findViewById(R.id.btnCloseDialog);
//    }
//
//
//
//    private void setupViewModel() {
//        if (dateFormatViewModel != null) {
//            dateFormatViewModel.getDateFormats().observe((LifecycleOwner) context, formats -> {
//                if (formats != null) {
//                    this.dateFormats = new ArrayList<>(formats); // Create a copy to avoid modifying original
//
//                    // Find currently selected position and store as original
//                    for (int i = 0; i < formats.size(); i++) {
//                        if (formats.get(i).getSelected() == 1) {
//                            selectedPosition = i;
//                            originalSelectedPosition = i; // Store original selection
//                            break;
//                        }
//                    }
//
//                    if (adapter != null) {
//                        adapter.updateData(dateFormats);
//                    }
//                }
//            });
//        }
//    }
//
//    private void setupRecyclerView() {
//        rvDateTime.setLayoutManager(new LinearLayoutManager(context));
//
//        adapter = new DateTimeFormat_Adapter(context, dateFormats, this);
//        rvDateTime.setAdapter(adapter);
//
//        // Scroll to selected position if available
//        if (selectedPosition >= 0) {
//            rvDateTime.scrollToPosition(selectedPosition);
//        }
//    }
//
//    private void setupClickListeners() {
//        btnSaveDateTime.setOnClickListener(v -> {
//            if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
//                DateFormatModel selectedFormat = dateFormats.get(selectedPosition);
//
//                // NOW save using ViewModel (only when Save is clicked)
//                if (dateFormatViewModel != null) {
//                    dateFormatViewModel.onDoneClicked(selectedPosition);
//                }
//
//                // Notify listener to update the actual UI
//                if (listener != null) {
//                    listener.onDateTimeSelected(selectedFormat, selectedPosition);
//                }
//            }
//            dismiss();
//        });
//
//        btnCanceDateTime.setOnClickListener(v -> {
//            // Reset to original selection when cancelled
//            resetToOriginalSelection();
//            dismiss();
//        });
//
//        btnCloseDialog.setOnClickListener(v -> {
//            // Reset to original selection when closed without saving
//            resetToOriginalSelection();
//            dismiss();
//        });
//    }
//
//    @Override
//    public void onDateClick(int position) {
//        // Update selectedPosition for UI feedback only
//        selectedPosition = position;
//
//        // Update adapter selection for visual feedback ONLY
//        // Do NOT save to ViewModel or call listener here
//        if (adapter != null && dateFormats != null) {
//            // Update local list for immediate UI response (visual selection only)
//            for (int i = 0; i < dateFormats.size(); i++) {
//                dateFormats.get(i).setSelected(i == position ? 1 : 0);
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }
//
//    private void resetToOriginalSelection() {
//        // Reset selection back to original when dialog is cancelled or closed
//        if (dateFormats != null && originalSelectedPosition >= 0) {
//            selectedPosition = originalSelectedPosition;
//            for (int i = 0; i < dateFormats.size(); i++) {
//                dateFormats.get(i).setSelected(i == originalSelectedPosition ? 1 : 0);
//            }
//            if (adapter != null) {
//                adapter.notifyDataSetChanged();
//            }
//        }
//    }
//
//    @Override
//    public void dismiss() {
//        super.dismiss();
//        if (listener != null) {
//            listener.onDialogDismissed();
//        }
//    }
//
//    // Method to set selected position externally
//    public void setSelectedPosition(int position) {
//        this.selectedPosition = position;
//        this.originalSelectedPosition = position; // Update original as well
//        if (adapter != null && dateFormats != null) {
//            // Update selection
//            for (int i = 0; i < dateFormats.size(); i++) {
//                dateFormats.get(i).setSelected(i == position ? 1 : 0);
//            }
//            adapter.notifyDataSetChanged();
//            rvDateTime.scrollToPosition(position);
//        }
//    }
//
//    // Method to get currently selected format
//    public DateFormatModel getSelectedFormat() {
//        if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
//            return dateFormats.get(selectedPosition);
//        }
//        return null;
//    }
//}

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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.adapter.DateTimeFormat_Adapter;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.util.SP;
import com.camera.gps.viewmodel.DateFormatViewModel;

import java.util.ArrayList;

public class DateTimeDialog extends Dialog implements DateTimeFormat_Adapter.DateClicksListener {

    private final Context context;
    private RecyclerView rvDateTime;
    private DateTimeFormat_Adapter adapter;
    private TextView btnSaveDateTime, btnCanceDateTime;
    private ImageView btnCloseDialog;
    private SP msp;
    private int selectedPosition = -1;
    private int originalSelectedPosition = -1; // Store original selection
    private OnDateTimeSelectedListener listener;
    private DateFormatViewModel dateFormatViewModel;
    private ArrayList<DateFormatModel> dateFormats;
    private String currentActiveFormat; // Add this to track current active format
    private boolean persistSelection = true;

    public DateTimeDialog(@NonNull Context context, OnDateTimeSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.msp = new SP(context);

        // Initialize ViewModel
        if (context instanceof ViewModelStoreOwner) {
            dateFormatViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(DateFormatViewModel.class);
        }
    }

    // Add method to set current active format
    public void setCurrentActiveFormat(String currentActiveFormat) {
        this.currentActiveFormat = currentActiveFormat;
    }

    public void setPersistSelection(boolean persistSelection) {
        this.persistSelection = persistSelection;
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

        // Set dialog properties
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_date_format);

        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        rvDateTime = findViewById(R.id.rv_time);
        btnSaveDateTime = findViewById(R.id.btnSaveDateTime);
        btnCanceDateTime = findViewById(R.id.btnCancelDateTime);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setupViewModel() {
        if (dateFormatViewModel != null) {
            dateFormatViewModel.getDateFormats().observe((LifecycleOwner) context, formats -> {
                if (formats != null) {
                    this.dateFormats = new ArrayList<>(formats); // Create a copy to avoid modifying original

                    // Find currently selected position based on current active format
                    // Use currentActiveFormat if provided, otherwise fall back to saved preference
                    String formatToMatch = currentActiveFormat != null ? currentActiveFormat :
                            msp.getString(context, com.camera.gps.MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");

//                    for (int i = 0; i < formats.size(); i++) {
//                        if (formats.get(i).getTime_format().equalsIgnoreCase(formatToMatch)) {
//                            selectedPosition = i;
//                            originalSelectedPosition = i; // Store original selection
//                            // Update the selection in the model
//                            this.dateFormats.get(i).setSelected(1);
//                            break;
//                        }
//                    }

                    for (int i = 0; i < formats.size(); i++) {
                        formats.get(i).setSelected(0); // reset all
                        if (formats.get(i).getTime_format().equalsIgnoreCase(formatToMatch)) {
                            selectedPosition = i;
                            originalSelectedPosition = i;
                            formats.get(i).setSelected(1); // mark only one as active
                        }
                    }


                    if (adapter != null) {
                        adapter.updateData(dateFormats);
                    }
                }
            });
        }
    }

    private void setupRecyclerView() {
        rvDateTime.setLayoutManager(new LinearLayoutManager(context));

        adapter = new DateTimeFormat_Adapter(context, dateFormats, this);
        rvDateTime.setAdapter(adapter);

        // Scroll to selected position if available
        if (selectedPosition >= 0) {
            rvDateTime.scrollToPosition(selectedPosition);
        }
    }

    private void setupClickListeners() {
        btnSaveDateTime.setOnClickListener(v -> {
            if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
                DateFormatModel selectedFormat = dateFormats.get(selectedPosition);

                // NOW save using ViewModel (only when Save is clicked)
                if (persistSelection && dateFormatViewModel != null) {
                    dateFormatViewModel.onDoneClicked(selectedPosition);
                }

                // Notify listener to update the actual UI
                if (listener != null) {
                    listener.onDateTimeSelected(selectedFormat, selectedPosition);
                }
            }
            dismiss();
        });

        btnCanceDateTime.setOnClickListener(v -> {
            // Reset to original selection when cancelled
            resetToOriginalSelection();
            dismiss();
        });

        btnCloseDialog.setOnClickListener(v -> {
            // Reset to original selection when closed without saving
            resetToOriginalSelection();
            dismiss();
        });
    }

    @Override
    public void onDateClick(int position) {
        // Update selectedPosition for UI feedback only
        selectedPosition = position;

        // Update adapter selection for visual feedback ONLY
        // Do NOT save to ViewModel or call listener here
        if (adapter != null && dateFormats != null) {
            // Update local list for immediate UI response (visual selection only)
            for (int i = 0; i < dateFormats.size(); i++) {
                dateFormats.get(i).setSelected(i == position ? 1 : 0);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void resetToOriginalSelection() {
        // Reset selection back to original when dialog is cancelled or closed
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
        this.originalSelectedPosition = position; // Update original as well
        if (adapter != null && dateFormats != null) {
            // Update selection
            for (int i = 0; i < dateFormats.size(); i++) {
                dateFormats.get(i).setSelected(i == position ? 1 : 0);
            }
            adapter.notifyDataSetChanged();
            rvDateTime.scrollToPosition(position);
        }
    }

    // Method to get currently selected format
    public DateFormatModel getSelectedFormat() {
        if (selectedPosition >= 0 && dateFormats != null && selectedPosition < dateFormats.size()) {
            return dateFormats.get(selectedPosition);
        }
        return null;
    }
}
