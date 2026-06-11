package com.camera.gps.dialogs;
import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.camera.gps.R;
import com.camera.gps.listener.OnSoundSelectedListener;

public class VideoVolumeDialog extends Dialog {
    private Context context;
    private boolean currentSoundStatus;
    private OnSoundSelectedListener listener;
    private RadioButton radioOn, radioOff;
    private LinearLayout layoutOn, layoutOff;


    public VideoVolumeDialog(Context context, boolean currentSoundStatus, OnSoundSelectedListener listener) {
        super(context);
        this.context = context;
        this.currentSoundStatus = currentSoundStatus;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_videovolume);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        // Initialize views
        radioOn = findViewById(R.id.radioOn);
        radioOff = findViewById(R.id.radioOff);
        layoutOn = findViewById(R.id.layoutOn);
        layoutOff = findViewById(R.id.layoutOff);

        ImageView btnClose = findViewById(R.id.btnCloseDialog);
        TextView btnSave = findViewById(R.id.btnSaveGrid);
        TextView btnCancel = findViewById(R.id.btnCancelGrid);

        // Set current selection
        updateSelection(currentSoundStatus);

        // Set click listeners
        layoutOn.setOnClickListener(v -> updateSelection(true));
        layoutOff.setOnClickListener(v -> updateSelection(false));

        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSoundSelected(radioOn.isChecked());
            }
            dismiss();
        });

        setOnDismissListener(dialog -> {
            if (listener != null) {
                listener.onDialogDismissed();
            }
        });
    }

    private void updateSelection(boolean soundOn) {
        radioOn.setChecked(soundOn);
        radioOff.setChecked(!soundOn);
        currentSoundStatus = soundOn;
    }
}