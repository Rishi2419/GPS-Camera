package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.camera.gps.R;
import com.camera.gps.listener.OnTimerSelectedListener;

public class TimerDialog extends Dialog {

    private LinearLayout layoutTimerOff, layoutTimer3, layoutTimer5, layoutTimer10;
    private RadioButton radioTimerOff, radioTimer3, radioTimer5, radioTimer10;
    private TextView btnSaveTimer, btnCancelTimer;
    private ImageView btnCloseDialog;
    private int selectedTimer;
    private OnTimerSelectedListener listener;



    public TimerDialog(Context context, int currentTimer, OnTimerSelectedListener listener) {
        super(context);
        this.selectedTimer = currentTimer;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_timer);
        setCancelable(true);

        initViews();
        setCurrentSelection();
        setClickListeners();
    }

    private void initViews() {
        layoutTimerOff = findViewById(R.id.layoutTimerOff);
        layoutTimer3 = findViewById(R.id.layoutTimer3);
        layoutTimer5 = findViewById(R.id.layoutTimer5);
        layoutTimer10 = findViewById(R.id.layoutTimer10);

        radioTimerOff = findViewById(R.id.radioTimerOff);
        radioTimer3 = findViewById(R.id.radioTimer3);
        radioTimer5 = findViewById(R.id.radioTimer5);
        radioTimer10 = findViewById(R.id.radioTimer10);

        btnSaveTimer = findViewById(R.id.btnSaveTimer);
        btnCancelTimer = findViewById(R.id.btnCancelTimer);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setCurrentSelection() {
        clearAllSelections();

        switch (selectedTimer) {
            case 0:
                radioTimerOff.setChecked(true);
                break;
            case 3:
                radioTimer3.setChecked(true);
                break;
            case 5:
                radioTimer5.setChecked(true);
                break;
            case 10:
                radioTimer10.setChecked(true);
                break;
        }
    }

    private void clearAllSelections() {
        radioTimerOff.setChecked(false);
        radioTimer3.setChecked(false);
        radioTimer5.setChecked(false);
        radioTimer10.setChecked(false);
    }

    private void setClickListeners() {
        layoutTimerOff.setOnClickListener(v -> selectTimer(0));
        layoutTimer3.setOnClickListener(v -> selectTimer(3));
        layoutTimer5.setOnClickListener(v -> selectTimer(5));
        layoutTimer10.setOnClickListener(v -> selectTimer(10));

        btnSaveTimer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTimerSelected(selectedTimer);
            }
            dismiss();
        });

        btnCancelTimer.setOnClickListener(v -> dismiss());
        btnCloseDialog.setOnClickListener(v -> dismiss());
    }

    private void selectTimer(int timer) {
        selectedTimer = timer;
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
