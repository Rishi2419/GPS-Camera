package com.camera.gps.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.camera.gps.R;
import com.camera.gps.listener.OnGridSelectedListener;

public class GridDialog extends Dialog {

    private LinearLayout layoutGridOff, layoutGrid3x3, layoutGrid4x4;
    private RadioButton radioGridOff, radioGrid3x3, radioGrid4x4;
    private TextView btnSaveGrid, btnCancelGrid;
    private ImageView btnCloseDialog;
    private int selectedGrid;
    private OnGridSelectedListener listener;

    // Grid constants
    public static final int GRID_OFF = 0;
    public static final int GRID_3x3 = 1;
    public static final int GRID_4x4 = 2;



    public GridDialog(Context context, int currentGrid, OnGridSelectedListener listener) {
        super(context);
        this.selectedGrid = currentGrid;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_grid);
        setCancelable(true);

        initViews();
        setCurrentSelection();
        setClickListeners();
    }

    private void initViews() {
        layoutGridOff = findViewById(R.id.layoutGridOff);
        layoutGrid3x3 = findViewById(R.id.layoutGrid3x3);
        layoutGrid4x4 = findViewById(R.id.layoutGrid4X4);

        radioGridOff = findViewById(R.id.radioGridOff);
        radioGrid3x3 = findViewById(R.id.radioGrid3x3);
        radioGrid4x4 = findViewById(R.id.radioGrid4x4);

        btnSaveGrid = findViewById(R.id.btnSaveGrid);
        btnCancelGrid = findViewById(R.id.btnCancelGrid);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setCurrentSelection() {
        clearAllSelections();

        switch (selectedGrid) {
            case GRID_3x3:
                radioGrid3x3.setChecked(true);
                break;
            case GRID_4x4:
                radioGrid4x4.setChecked(true);
                break;
            case GRID_OFF:
            default:
                radioGridOff.setChecked(true);
                break;
        }
    }

    private void clearAllSelections() {
        radioGridOff.setChecked(false);
        radioGrid3x3.setChecked(false);
        radioGrid4x4.setChecked(false);
    }

    private void setClickListeners() {
        layoutGridOff.setOnClickListener(v -> selectGrid(GRID_OFF));
        layoutGrid3x3.setOnClickListener(v -> selectGrid(GRID_3x3));
        layoutGrid4x4.setOnClickListener(v -> selectGrid(GRID_4x4));

        btnSaveGrid.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGridSelected(selectedGrid);
            }
            dismiss();
        });

        btnCancelGrid.setOnClickListener(v -> dismiss());
        btnCloseDialog.setOnClickListener(v -> dismiss());
    }

    private void selectGrid(int gridValue) {
        selectedGrid = gridValue;
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
