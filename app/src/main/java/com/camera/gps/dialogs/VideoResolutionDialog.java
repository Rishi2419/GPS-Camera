package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.camera.gps.R;
import com.camera.gps.listener.OnResolutionSelectedListener;

public class VideoResolutionDialog extends Dialog {

    private LinearLayout layout1440p, layout1080p, layout720p, layout540p, layout480p, layout360p, layout240p;
    private RadioButton radio1440p, radio1080p, radio720p, radio540p, radio480p, radio360p, radio240p;
    private TextView btnSaveTimer, btnCancelTimer;
    private ImageView btnCloseDialog;
    private int selectedResolution;
    private OnResolutionSelectedListener listener;

    // Resolution constants
    public static final int RESOLUTION_1440P = 1440;
    public static final int RESOLUTION_1080P = 1080;
    public static final int RESOLUTION_720P = 720;
    public static final int RESOLUTION_540P = 540;
    public static final int RESOLUTION_480P = 480;
    public static final int RESOLUTION_360P = 360;
    public static final int RESOLUTION_240P = 240;



    public VideoResolutionDialog(Context context, int currentResolution, OnResolutionSelectedListener listener) {
        super(context);
        this.selectedResolution = currentResolution;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_videoresolution);
        setCancelable(true);

        initViews();
        setCurrentSelection();
        setClickListeners();
    }

    private void initViews() {
        layout1440p = findViewById(R.id.layout1440p);
        layout1080p = findViewById(R.id.layout1080p);
        layout720p = findViewById(R.id.layout720p);
        layout540p = findViewById(R.id.layout540p);
        layout480p = findViewById(R.id.layout480p);
        layout360p = findViewById(R.id.layout360p);
        layout240p = findViewById(R.id.layout240p);

        radio1440p = findViewById(R.id.radio1440p);
        radio1080p = findViewById(R.id.radio1080p);
        radio720p = findViewById(R.id.radio720p);
        radio540p = findViewById(R.id.radio540p);
        radio480p = findViewById(R.id.radio480p);
        radio360p = findViewById(R.id.radio360p);
        radio240p = findViewById(R.id.radio240p);

        btnSaveTimer = findViewById(R.id.btnSaveTimer);
        btnCancelTimer = findViewById(R.id.btnCancelTimer);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setCurrentSelection() {
        clearAllSelections();

        switch (selectedResolution) {
            case RESOLUTION_1440P:
                radio1440p.setChecked(true);
                break;
            case RESOLUTION_1080P:
                radio1080p.setChecked(true);
                break;
            case RESOLUTION_720P:
                radio720p.setChecked(true);
                break;
            case RESOLUTION_540P:
                radio540p.setChecked(true);
                break;
            case RESOLUTION_480P:
                radio480p.setChecked(true);
                break;
            case RESOLUTION_360P:
                radio360p.setChecked(true);
                break;
            case RESOLUTION_240P:
                radio240p.setChecked(true);
                break;
        }
    }

    private void clearAllSelections() {
        radio1440p.setChecked(false);
        radio1080p.setChecked(false);
        radio720p.setChecked(false);
        radio540p.setChecked(false);
        radio480p.setChecked(false);
        radio360p.setChecked(false);
        radio240p.setChecked(false);
    }

    private void setClickListeners() {
        layout1440p.setOnClickListener(v -> selectResolution(RESOLUTION_1440P));
        layout1080p.setOnClickListener(v -> selectResolution(RESOLUTION_1080P));
        layout720p.setOnClickListener(v -> selectResolution(RESOLUTION_720P));
        layout540p.setOnClickListener(v -> selectResolution(RESOLUTION_540P));
        layout480p.setOnClickListener(v -> selectResolution(RESOLUTION_480P));
        layout360p.setOnClickListener(v -> selectResolution(RESOLUTION_360P));
        layout240p.setOnClickListener(v -> selectResolution(RESOLUTION_240P));

        btnSaveTimer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResolutionSelected(selectedResolution);
            }
            dismiss();
        });

        btnCancelTimer.setOnClickListener(v -> dismiss());
        btnCloseDialog.setOnClickListener(v -> dismiss());
    }

    private void selectResolution(int resolution) {
        selectedResolution = resolution;
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