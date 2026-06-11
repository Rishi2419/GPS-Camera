package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.camera.gps.R;
import com.camera.gps.listener.OnFpsSelectedListener;

public class FpsDialog extends Dialog {

    private LinearLayout layout60fps, layout50fps, layout40fps, layout30fps, layout20fps;
    private RadioButton radio60fps, radio50fps, radio40fps, radio30fps, radio20fps;
    private TextView btnSaveTimer, btnCancelTimer;
    private ImageView btnCloseDialog;
    private int selectedFps;
    private OnFpsSelectedListener listener;

    // FPS constants
    public static final int FPS_60 = 60;
    public static final int FPS_50 = 50;
    public static final int FPS_40 = 40;
    public static final int FPS_30 = 30;
    public static final int FPS_20 = 20;



    public FpsDialog(Context context, int currentFps, OnFpsSelectedListener listener) {
        super(context);
        this.selectedFps = currentFps;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_fps);
        setCancelable(true);

        initViews();
        setCurrentSelection();
        setClickListeners();
    }

    private void initViews() {
        layout60fps = findViewById(R.id.layout60fps);
        layout50fps = findViewById(R.id.layout50fps);
        layout40fps = findViewById(R.id.layout40fps);
        layout30fps = findViewById(R.id.layout30fps);
        layout20fps = findViewById(R.id.layout20fps);

        radio60fps = findViewById(R.id.radio60fps);
        radio50fps = findViewById(R.id.radio50fps);
        radio40fps = findViewById(R.id.radio40fps);
        radio30fps = findViewById(R.id.radio30fps);
        radio20fps = findViewById(R.id.radio20fps);

        btnSaveTimer = findViewById(R.id.btnSaveTimer);
        btnCancelTimer = findViewById(R.id.btnCancelTimer);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setCurrentSelection() {
        clearAllSelections();

        switch (selectedFps) {
            case FPS_60:
                radio60fps.setChecked(true);
                break;
            case FPS_50:
                radio50fps.setChecked(true);
                break;
            case FPS_40:
                radio40fps.setChecked(true);
                break;
            case FPS_30:
                radio30fps.setChecked(true);
                break;
            case FPS_20:
                radio20fps.setChecked(true);
                break;
        }
    }

    private void clearAllSelections() {
        radio60fps.setChecked(false);
        radio50fps.setChecked(false);
        radio40fps.setChecked(false);
        radio30fps.setChecked(false);
        radio20fps.setChecked(false);
    }

    private void setClickListeners() {
        layout60fps.setOnClickListener(v -> selectFps(FPS_60));
        layout50fps.setOnClickListener(v -> selectFps(FPS_50));
        layout40fps.setOnClickListener(v -> selectFps(FPS_40));
        layout30fps.setOnClickListener(v -> selectFps(FPS_30));
        layout20fps.setOnClickListener(v -> selectFps(FPS_20));

        btnSaveTimer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFpsSelected(selectedFps);
            }
            dismiss();
        });

        btnCancelTimer.setOnClickListener(v -> dismiss());
        btnCloseDialog.setOnClickListener(v -> dismiss());
    }

    private void selectFps(int fps) {
        selectedFps = fps;
        setCurrentSelection();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDialogDismissed();
        }
    }
}