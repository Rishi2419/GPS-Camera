//    package com.camera.gps.dialogs;
//
//    import android.app.Dialog;
//    import android.content.Context;
//    import android.graphics.Color;
//    import android.graphics.drawable.ColorDrawable;
//    import android.os.Bundle;
//    import android.view.View;
//    import android.view.Window;
//    import android.view.WindowManager;
//    import android.widget.ImageView;
//    import android.widget.LinearLayout;
//    import android.widget.TextView;
//
//    import androidx.annotation.NonNull;
//    import androidx.recyclerview.widget.LinearLayoutManager;
//    import androidx.recyclerview.widget.RecyclerView;
//
//    import com.appizona.yehiahd.fastsave.FastSave;
//    import com.camera.gps.MyApplication;
//    import com.camera.gps.R;
//    import com.camera.gps.adapter.FontStyle_Adapter;
//    import com.camera.gps.listener.OnFontSelectedListener;
//    import com.camera.gps.util.SP;
//
//    public class FontStyleDialog extends Dialog implements FontStyle_Adapter.FontStyleListener {
//
//        private final Context context;
//        private RecyclerView rvFontStyleDialog;
//        private LinearLayout llActionBtns;
//        private FontStyle_Adapter adapter;
//        private TextView btnSaveFont, btnCancelFont;
//        private ImageView btnCloseDialog;
//        private SP msp;
//        private String[] fontList;
//        private int selectedPosition = -1;
//        private OnFontSelectedListener listener;
//
//
//        public FontStyleDialog(@NonNull Context context, String[] fontList, OnFontSelectedListener listener) {
//            super(context);
//            this.context = context;
//            this.fontList = fontList;
//            this.listener = listener;
//            this.msp = new SP(context);
//        }
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN
//            );
//
//            View decorView = getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(uiOptions);
//
//            // Set dialog properties
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            setContentView(R.layout.dialog_font_style);
//
//
//            // Make dialog background transparent
//            if (getWindow() != null) {
//                getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.WRAP_CONTENT);
//            }
//
//            initViews();
//            setupRecyclerView();
//            setupClickListeners();
//
//            // Load saved position
//            int savedPos = msp.getInteger(context, SP.LOCATION_FONT_POSITION, 0);
//            selectedPosition = savedPos;
//            if (adapter != null) {
//                adapter.setSelectedPosition(savedPos);
//            }
//        }
//
//        private void initViews() {
//            llActionBtns = findViewById(R.id.llActionBtns);
//            rvFontStyleDialog = findViewById(R.id.rvFontStyleDialog);
//            btnSaveFont = findViewById(R.id.btnSaveFont);
//            btnCancelFont = findViewById(R.id.btnCancelFont);
//            btnCloseDialog = findViewById(R.id.btnCloseDialog);
//        }
//
//        private void setupRecyclerView() {
//            rvFontStyleDialog.setLayoutManager(new LinearLayoutManager(context));
//            adapter = new FontStyle_Adapter(context, fontList, this);
//            rvFontStyleDialog.setAdapter(adapter);
//
//            // Scroll to saved position
//            int savedPos = msp.getInteger(context, SP.LOCATION_FONT_POSITION, 0);
//            rvFontStyleDialog.scrollToPosition(savedPos);
//        }
//
//        private void setupClickListeners() {
//            btnSaveFont.setOnClickListener(v -> {
//                if (selectedPosition >= 0 && selectedPosition < fontList.length) {
////                    // Save to preferences
////                    msp.setInteger(context, SP.LOCATION_FONT_POSITION, selectedPosition);
////                    FastSave.getInstance().saveString(MyApplication.FONT_STYLE, fontList[selectedPosition]);
////
//                    // Notify listener
//                    if (listener != null) {
//                        listener.onFontSelected(fontList[selectedPosition], selectedPosition);
//                    }
//                }
//                dismiss();
//            });
//
//            btnCancelFont.setOnClickListener(v -> {
//                dismiss();
//            });
//
//            btnCloseDialog.setOnClickListener(v -> {
//                dismiss();
//            });
//        }
//
//        @Override
//        public void onFontClick(int position) {
//            selectedPosition = position;
//            // Update adapter selection immediately
//            if (adapter != null) {
//                adapter.updateSelection(position);
//            }
//        }
//
//        @Override
//        public void dismiss() {
//            super.dismiss();
//            if (listener != null) {
//                listener.onDialogDismissed();
//            }
//        }
//
//        // Method to update font list if needed
//        public void updateFontList(String[] newFontList) {
//            this.fontList = newFontList;
//            if (adapter != null) {
//                adapter.notifyDataSetChanged();
//            }
//        }
//
//        // Method to set selected position externally
//        public void setSelectedPosition(int position) {
//            this.selectedPosition = position;
//            if (adapter != null) {
//                adapter.setSelectedPosition(position);
//                rvFontStyleDialog.scrollToPosition(position);
//            }
//        }
//    }

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.FontStyle_Adapter;
import com.camera.gps.listener.OnFontSelectedListener;
import com.camera.gps.util.SP;

public class FontStyleDialog extends Dialog implements FontStyle_Adapter.FontStyleListener {

    private final Context context;
    private RecyclerView rvFontStyleDialog;
    private LinearLayout llActionBtns;
    private FontStyle_Adapter adapter;
    private TextView btnSaveFont, btnCancelFont;
    private ImageView btnCloseDialog;
    private SP msp;
    private String[] fontList;
    private int selectedPosition = -1;
    private OnFontSelectedListener listener;
    private boolean isPositionSetExternally = false; // Track if position was set externally


    public FontStyleDialog(@NonNull Context context, String[] fontList, OnFontSelectedListener listener) {
        super(context);
        this.context = context;
        this.fontList = fontList;
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

        // Set dialog properties
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_font_style);


        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Only load from SharedPreferences if position wasn't set externally
        if (!isPositionSetExternally) {
            int savedPos = msp.getInteger(context, SP.LOCATION_FONT_POSITION, 0);
            selectedPosition = savedPos;
        }

        // Apply the selected position to adapter
        if (adapter != null) {
            adapter.setSelectedPosition(selectedPosition);
            rvFontStyleDialog.scrollToPosition(selectedPosition);
        }
    }

    private void initViews() {
        llActionBtns = findViewById(R.id.llActionBtns);
        rvFontStyleDialog = findViewById(R.id.rvFontStyleDialog);
        btnSaveFont = findViewById(R.id.btnSaveFont);
        btnCancelFont = findViewById(R.id.btnCancelFont);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
    }

    private void setupRecyclerView() {
        rvFontStyleDialog.setLayoutManager(new LinearLayoutManager(context));
        adapter = new FontStyle_Adapter(context, fontList, this);
        rvFontStyleDialog.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSaveFont.setOnClickListener(v -> {
            if (selectedPosition >= 0 && selectedPosition < fontList.length) {
                // Notify listener
                if (listener != null) {
                    listener.onFontSelected(fontList[selectedPosition], selectedPosition);
                }
            }
            dismiss();
        });

        btnCancelFont.setOnClickListener(v -> {
            dismiss();
        });

        btnCloseDialog.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    public void onFontClick(int position) {
        selectedPosition = position;
        // Update adapter selection immediately
        if (adapter != null) {
            adapter.updateSelection(position);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDialogDismissed();
        }
    }

    // Method to update font list if needed
    public void updateFontList(String[] newFontList) {
        this.fontList = newFontList;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Method to set selected position externally
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        this.isPositionSetExternally = true; // Mark that position was set externally
        if (adapter != null) {
            adapter.setSelectedPosition(position);
            rvFontStyleDialog.scrollToPosition(position);
        }
    }
}