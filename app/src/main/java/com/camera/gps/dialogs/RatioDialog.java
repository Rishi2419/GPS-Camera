    package com.camera.gps.dialogs;

    import android.app.Dialog;
    import android.content.Context;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.RadioButton;
    import android.widget.TextView;

    import com.camera.gps.R;
    import com.camera.gps.listener.OnRatioSelectedListener;

    public class RatioDialog extends Dialog {

        private LinearLayout layoutfull, layout916, layout34;
        private RadioButton radiofull, radio916, radio34;
        private TextView btnSaveRatio, btnCancelRatio;
        private ImageView btnCloseDialog;
        private int selectedRatio;
        private OnRatioSelectedListener listener;

        // Ratio constants
        public static final int RATIO_FULL = 0;
        public static final int RATIO_16_9 = 1;
        public static final int RATIO_4_3 = 2;



        public RatioDialog(Context context, int currentRatio, OnRatioSelectedListener listener) {
            super(context);
            this.selectedRatio = currentRatio;
            this.listener = listener;
            init();
        }

        private void init() {
            setContentView(R.layout.dialog_ratio);
            setCancelable(true);

            initViews();
            setCurrentSelection();
            setClickListeners();
        }

        private void initViews() {
            layoutfull = findViewById(R.id.layoutfull);
            layout916 = findViewById(R.id.layout916);
            layout34 = findViewById(R.id.layout34);

            radiofull = findViewById(R.id.radiofull);
            radio916 = findViewById(R.id.radio916);
            radio34 = findViewById(R.id.radio34);

            btnSaveRatio = findViewById(R.id.btnSaveRatio);
            btnCancelRatio = findViewById(R.id.btnCancelRatio);
            btnCloseDialog = findViewById(R.id.btnCloseDialog);
        }

        private void setCurrentSelection() {
            clearAllSelections();

            switch (selectedRatio) {
                case RATIO_FULL:
                    radiofull.setChecked(true);
                    break;
                case RATIO_16_9:
                    radio916.setChecked(true);
                    break;
                case RATIO_4_3:
                    radio34.setChecked(true);
                    break;
            }
        }

        private void clearAllSelections() {
            radiofull.setChecked(false);
            radio916.setChecked(false);
            radio34.setChecked(false);
        }

        private void setClickListeners() {
            layoutfull.setOnClickListener(v -> selectRatio(RATIO_FULL));
            layout916.setOnClickListener(v -> selectRatio(RATIO_16_9));
            layout34.setOnClickListener(v -> selectRatio(RATIO_4_3));

            btnSaveRatio.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRatioSelected(selectedRatio);
                }
                dismiss();
            });

            btnCancelRatio.setOnClickListener(v -> dismiss());
            btnCloseDialog.setOnClickListener(v -> dismiss());
        }

        private void selectRatio(int ratio) {
            selectedRatio = ratio;
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