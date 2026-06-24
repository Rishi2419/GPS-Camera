package com.camera.gps.dialogs;

import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adsmanager.NativeAdManager;
import com.camera.gps.listener.OnMapTypeSelectedListener;
import com.camera.gps.util.Utils;

public class MapTypeDialog extends Dialog {

    private ImageView imgNormalMap, imgHybridMap, imgSatelliteMap, imgTerrainMap;
    private ImageView ivNormalCheck, ivHybridCheck, ivSatelliteCheck, ivTerrainCheck;
    private View vNormalOverlay, vHybridOverlay, vSatelliteOverlay, vTerrainOverlay;
    private TextView btnSaveMap, btnCancelMap;
    private ImageView btnCloseDialog;
    private FrameLayout flMapTypeNative;
    private Activity activity;
    private boolean nativeLoadRequested = false;
    private int selectedMapType;
    private OnMapTypeSelectedListener listener;

    // Map type constants
    public static final int MAP_TYPE_NORMAL = 1;
    public static final int MAP_TYPE_SATELLITE = 2;
    public static final int MAP_TYPE_TERRAIN = 3;
    public static final int MAP_TYPE_HYBRID = 4;

    public MapTypeDialog(Context context, int currentMapType, OnMapTypeSelectedListener listener) {
        super(context);
        this.activity = resolveActivity(context);
        this.selectedMapType = currentMapType;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_maptypeselection);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(true);

        initViews();
        setCurrentSelection();
        setClickListeners();
    }

    private void initViews() {
        imgNormalMap = findViewById(R.id.imgNormalMap);
        imgHybridMap = findViewById(R.id.imgHybridMap);
        imgSatelliteMap = findViewById(R.id.imgSatelliteMap);
        imgTerrainMap = findViewById(R.id.imgTerrainMap);

        ivNormalCheck = findViewById(R.id.ivNormalCheck);
        ivHybridCheck = findViewById(R.id.ivHybridCheck);
        ivSatelliteCheck = findViewById(R.id.ivSatelliteCheck);
        ivTerrainCheck = findViewById(R.id.ivTerrainCheck);

        vNormalOverlay = findViewById(R.id.vNormalOverlay);
        vHybridOverlay = findViewById(R.id.vHybridOverlay);
        vSatelliteOverlay = findViewById(R.id.vSatelliteOverlay);
        vTerrainOverlay = findViewById(R.id.vTerrainOverlay);

        btnSaveMap = findViewById(R.id.btnSaveMap);
        btnCancelMap = findViewById(R.id.btnCancelMap);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
        flMapTypeNative = findViewById(R.id.flMapTypeNative);
    }

    private void setCurrentSelection() {
        clearAllSelections();

        switch (selectedMapType) {
            case MAP_TYPE_NORMAL:
                ivNormalCheck.setVisibility(View.VISIBLE);
                vNormalOverlay.setVisibility(View.VISIBLE);
                break;
            case MAP_TYPE_SATELLITE:
                ivSatelliteCheck.setVisibility(View.VISIBLE);
                vSatelliteOverlay.setVisibility(View.VISIBLE);
                break;
            case MAP_TYPE_TERRAIN:
                ivTerrainCheck.setVisibility(View.VISIBLE);
                vTerrainOverlay.setVisibility(View.VISIBLE);
                break;
            case MAP_TYPE_HYBRID:
                ivHybridCheck.setVisibility(View.VISIBLE);
                vHybridOverlay.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void clearAllSelections() {
        // Hide all check icons
        ivNormalCheck.setVisibility(View.GONE);
        ivHybridCheck.setVisibility(View.GONE);
        ivSatelliteCheck.setVisibility(View.GONE);
        ivTerrainCheck.setVisibility(View.GONE);

        // Hide all overlays
        vNormalOverlay.setVisibility(View.GONE);
        vHybridOverlay.setVisibility(View.GONE);
        vSatelliteOverlay.setVisibility(View.GONE);
        vTerrainOverlay.setVisibility(View.GONE);
    }

    private void setClickListeners() {
        imgNormalMap.setOnClickListener(v -> selectMapType(MAP_TYPE_NORMAL));
        imgHybridMap.setOnClickListener(v -> selectMapType(MAP_TYPE_HYBRID));
        imgSatelliteMap.setOnClickListener(v -> selectMapType(MAP_TYPE_SATELLITE));
        imgTerrainMap.setOnClickListener(v -> selectMapType(MAP_TYPE_TERRAIN));

        btnSaveMap.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMapTypeSelected(selectedMapType);
            }
            dismiss();
        });

        btnCancelMap.setOnClickListener(v -> dismiss());

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dismiss());
        }

    }

    private void loadNativeAd() {
        if (nativeLoadRequested) {
            return;
        }
        nativeLoadRequested = true;

        if (activity == null
                || !MyApplication.isNetworkAvailable(activity)
                || Utils.getIsPremium(activity)) {
            if (flMapTypeNative != null) {
                flMapTypeNative.setVisibility(View.GONE);
            }
            return;
        }

        NativeAdManager.getInstance().loadAndShowNativeAd(
                activity,
                "map_type_native",
                flMapTypeNative,
                false,
                null,
                () -> nativeLoadRequested = false
        );
    }

    private Activity resolveActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void selectMapType(int mapType) {
        selectedMapType = mapType;
        setCurrentSelection();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDialogDismissed();
        }
    }

    @Override
    public void show() {
        super.show();
        if (flMapTypeNative != null) {
            flMapTypeNative.post(this::loadNativeAd);
        }
    }
}
