//package com.camera.gps.dialogs;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.camera.gps.R;
//import com.camera.gps.adapter.StampColorsAdapter;
//import com.camera.gps.util.SP;
//
//import java.util.List;
//import java.util.Locale;
//
//import ir.kotlin.kavehcolorpicker.KavehColorAlphaSlider;
//import ir.kotlin.kavehcolorpicker.KavehColorPicker;
//import ir.kotlin.kavehcolorpicker.KavehHueSlider;
//
//public class ColorDialog extends Dialog {
//
//    private KavehColorPicker colorPickerView;
//    private KavehHueSlider hueSlider;
//    private KavehColorAlphaSlider colorAlphaSlider;
//    private EditText etHexColor;
//    private ImageView previewColor;
//    private TextView btnSaveColor;
//    private TextView btnCancelColor;
//    private TextView tvAddColor;
//    private RecyclerView recyclerViewColors;
//
//    private StampColorsAdapter colorAdapter;
//    private SP colorStorageManager;
//    private List<Integer> savedColors;
//    private int currentColor = Color.parseColor("#2563EB"); // Default blue color
//    private boolean isUpdatingFromCode = false;
//
//    // Interface for color selection callback
//    public interface OnColorSelectedListener {
//        void onColorSelected(int color);
//    }
//
//    private OnColorSelectedListener colorSelectedListener;
//    private Context context;
//
//    public ColorDialog(@NonNull Context context) {
//        super(context);
//        colorStorageManager = new SP(context);
//        this.context = context;
//
//        loadSavedColors();
//    }
//
//    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
//        this.colorSelectedListener = listener;
//    }
//
//    public void setInitialColor(int color) {
//        this.currentColor = color;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_color);
//
//        initViews();
//        setupRecyclerView();
//        setupColorPicker();
//        setupListeners();
//
//        // Set initial color
//        updateColorPreview(currentColor);
//    }
//
//    private void initViews() {
//        colorPickerView = findViewById(R.id.colorPickerView);
//        hueSlider = findViewById(R.id.hueSlider);
//        colorAlphaSlider = findViewById(R.id.colorAlphaSlider);
//        etHexColor = findViewById(R.id.etHexColor);
//        previewColor = findViewById(R.id.PreviewColor);
//        btnSaveColor = findViewById(R.id.btnSaveColor);
//        btnCancelColor = findViewById(R.id.btnCancelColor);
//        tvAddColor = findViewById(R.id.tvAddColor);
//        recyclerViewColors = findViewById(R.id.recyclerViewColors);
//    }
//
//    private void setupColorPicker() {
//        // Connect sliders to color picker
//        colorPickerView.setAlphaSliderView(colorAlphaSlider);
//        colorPickerView.setHueSliderView(hueSlider);
//
//        // Set initial color
//        colorPickerView.setColor(currentColor);
//
//        // Color picker listener
//        colorPickerView.setOnColorChangedListener(color -> {
//            if (!isUpdatingFromCode) {
//                currentColor = color;
//                updateColorPreview(color);
//                updateHexCode(color);
//                updateSelectedColorInAdapter(color);
//            }
//        });
//    }
//
//    private void setupRecyclerView() {
//        colorAdapter = new StampColorsAdapter(context, savedColors);
//        recyclerViewColors.setLayoutManager(new GridLayoutManager(context, 10)); // 10 colors per row
//        recyclerViewColors.setAdapter(colorAdapter);
//
//        // Set initial selected color
//        colorAdapter.setSelectedColor(currentColor);
//
//        colorAdapter.setOnColorClickListener(color -> {
//            isUpdatingFromCode = true;
//            currentColor = color;
//            colorPickerView.setColor(color);
//            updateColorPreview(color);
//            updateHexCode(color);
//            updateSelectedColorInAdapter(color);
//            isUpdatingFromCode = false;
//        });
//    }
//
//    private void setupListeners() {
//        // Hex code input listener
//        etHexColor.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!isUpdatingFromCode && s.length() >= 7) {
//                    try {
//                        String hexColor = s.toString();
//                        if (!hexColor.startsWith("#")) {
//                            hexColor = "#" + hexColor;
//                        }
//
//                        int color = Color.parseColor(hexColor);
//                        isUpdatingFromCode = true;
//                        currentColor = color;
//                        colorPickerView.setColor(color);
//                        updateColorPreview(color);
//                        updateSelectedColorInAdapter(color);
//                        isUpdatingFromCode = false;
//                    } catch (IllegalArgumentException e) {
//                        // Invalid hex color, ignore
//                    }
//                }
//            }
//        });
//
//        // Add color button
//        tvAddColor.setOnClickListener(v -> {
//            addColorToSavedList(currentColor);
//        });
//
//        // Save button
//        btnSaveColor.setOnClickListener(v -> {
//            if (colorSelectedListener != null) {
//                colorSelectedListener.onColorSelected(currentColor);
//            }
//            dismiss();
//        });
//
//        // Cancel button
//        btnCancelColor.setOnClickListener(v -> {
//            dismiss();
//        });
//
//        // Close button
//        findViewById(R.id.btnCloseDialog).setOnClickListener(v -> {
//            dismiss();
//        });
//    }
//
//    private void loadSavedColors() {
//        savedColors = colorStorageManager.getSavedColors();
//    }
//
//    private void updateColorPreview(int color) {
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setShape(GradientDrawable.RECTANGLE);
//        drawable.setColor(color);
//        drawable.setCornerRadius(8f);
//        previewColor.setBackground(drawable);
//    }
//
//    private void updateHexCode(int color) {
//        isUpdatingFromCode = true;
//        String hexColor = String.format(Locale.getDefault(), "#%08X", color);
//        etHexColor.setText(hexColor);
//        isUpdatingFromCode = false;
//    }
//
//    private void updateSelectedColorInAdapter(int color) {
//        colorAdapter.setSelectedColor(color);
//    }
//
//    private void addColorToSavedList(int color) {
//        // Add to storage manager (it handles duplicates and limits)
//        colorStorageManager.addColor(color);
//
//        // Reload colors from storage
//        savedColors = colorStorageManager.getSavedColors();
//
//        // Update adapter
//        colorAdapter.updateColors(savedColors);
//        colorAdapter.setSelectedColor(currentColor);
//
//        Toast.makeText(context, "Color saved", Toast.LENGTH_SHORT).show();
//    }
//}

package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.adapter.StampColorsAdapter;
import com.camera.gps.util.SP;

import java.util.List;
import java.util.Locale;

import ir.kotlin.kavehcolorpicker.KavehColorAlphaSlider;
import ir.kotlin.kavehcolorpicker.KavehColorPicker;
import ir.kotlin.kavehcolorpicker.KavehHueSlider;

public class ColorDialog extends Dialog {

    private KavehColorPicker colorPickerView;
    private KavehHueSlider hueSlider;
    private KavehColorAlphaSlider colorAlphaSlider;
    private EditText etHexColor;
    private ImageView previewColor;
    private TextView btnSaveColor;
    private TextView btnCancelColor;
    private TextView tvAddColor;
    private TextView tvDialogTitle;
    private RecyclerView recyclerViewColors;

    private StampColorsAdapter colorAdapter;
    private SP colorStorageManager;
    private List<Integer> savedColors;
    private int currentColor = Color.parseColor("#2563EB"); // Default blue color
    private boolean isUpdatingFromCode = false;
    private String dialogTitle = "Background Color"; // Default title

    // Interface for color selection callback
    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private OnColorSelectedListener colorSelectedListener;
    private Context context;

    public ColorDialog(@NonNull Context context) {
        super(context);
        colorStorageManager = new SP(context);
        this.context = context;

        loadSavedColors();
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorSelectedListener = listener;
    }

    public void setInitialColor(int color) {
        this.currentColor = color;
    }

    public void setDialogTitle(String title) {
        this.dialogTitle = title;
        if (tvDialogTitle != null) {
            tvDialogTitle.setText(title);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color);

        initViews();
        setupRecyclerView();
        setupColorPicker();
        setupListeners();

        // Set dialog title
        tvDialogTitle.setText(dialogTitle);

        // Set initial color
        updateColorPreview(currentColor);
    }

    private void initViews() {
        colorPickerView = findViewById(R.id.colorPickerView);
        hueSlider = findViewById(R.id.hueSlider);
        colorAlphaSlider = findViewById(R.id.colorAlphaSlider);
        etHexColor = findViewById(R.id.etHexColor);
        previewColor = findViewById(R.id.PreviewColor);
        btnSaveColor = findViewById(R.id.btnSaveColor);
        btnCancelColor = findViewById(R.id.btnCancelColor);
        tvAddColor = findViewById(R.id.tvAddColor);
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        recyclerViewColors = findViewById(R.id.recyclerViewColors);
    }

    private void setupColorPicker() {
        // Connect sliders to color picker
        colorPickerView.setAlphaSliderView(colorAlphaSlider);
        colorPickerView.setHueSliderView(hueSlider);

        // Set initial color
        colorPickerView.setColor(currentColor);

        // Color picker listener
        colorPickerView.setOnColorChangedListener(color -> {
            if (!isUpdatingFromCode) {
                currentColor = color;
                updateColorPreview(color);
                updateHexCode(color);
                updateSelectedColorInAdapter(color);
            }
        });
    }

    private void setupRecyclerView() {
        colorAdapter = new StampColorsAdapter(context, savedColors);
        recyclerViewColors.setLayoutManager(new GridLayoutManager(context, 10)); // 10 colors per row
        recyclerViewColors.setAdapter(colorAdapter);

        // Set initial selected color
        colorAdapter.setSelectedColor(currentColor);

        colorAdapter.setOnColorClickListener(color -> {
            isUpdatingFromCode = true;
            currentColor = color;
            colorPickerView.setColor(color);
            updateColorPreview(color);
            updateHexCode(color);
            updateSelectedColorInAdapter(color);
            isUpdatingFromCode = false;
        });
    }

    private void setupListeners() {
        // Hex code input listener
        etHexColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingFromCode && s.length() >= 7) {
                    try {
                        String hexColor = s.toString();
                        if (!hexColor.startsWith("#")) {
                            hexColor = "#" + hexColor;
                        }

                        int color = Color.parseColor(hexColor);
                        isUpdatingFromCode = true;
                        currentColor = color;
                        colorPickerView.setColor(color);
                        updateColorPreview(color);
                        updateSelectedColorInAdapter(color);
                        isUpdatingFromCode = false;
                    } catch (IllegalArgumentException e) {
                        // Invalid hex color, ignore
                    }
                }
            }
        });

        // Add color button
        tvAddColor.setOnClickListener(v -> {
            addColorToSavedList(currentColor);
        });

        // Save button
        btnSaveColor.setOnClickListener(v -> {
            if (colorSelectedListener != null) {
                colorSelectedListener.onColorSelected(currentColor);
            }
            dismiss();
        });

        // Cancel button
        btnCancelColor.setOnClickListener(v -> {
            dismiss();
        });

        // Close button
        findViewById(R.id.btnCloseDialog).setOnClickListener(v -> {
            dismiss();
        });
    }

    private void loadSavedColors() {
        savedColors = colorStorageManager.getSavedColors();
    }

    private void updateColorPreview(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(8f);
        previewColor.setBackground(drawable);
    }

    private void updateHexCode(int color) {
        isUpdatingFromCode = true;
        String hexColor = String.format(Locale.getDefault(), "#%08X", color);
        etHexColor.setText(hexColor);
        isUpdatingFromCode = false;
    }

    private void updateSelectedColorInAdapter(int color) {
        colorAdapter.setSelectedColor(color);
    }

    private void addColorToSavedList(int color) {
        // Add to storage manager (it handles duplicates and limits)
        colorStorageManager.addColor(color);

        // Reload colors from storage
        savedColors = colorStorageManager.getSavedColors();

        // Update adapter
        colorAdapter.updateColors(savedColors);
        colorAdapter.setSelectedColor(currentColor);

        Toast.makeText(context, "Color saved", Toast.LENGTH_SHORT).show();
    }
}