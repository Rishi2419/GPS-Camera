package com.camera.gps.activity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.camera.gps.MyApplication.set_to_current_location;
import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExposureState;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.Observer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.camera.gps.MyApplication;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.OpenAdManager;
import com.camera.gps.camerax.adapter.ViewPagerTitleAdapter;
import com.camera.gps.camerax.controller.MainController;
import com.camera.gps.camerax.util.SharedPrefsSettings;
import com.camera.gps.camerax.util.UtilsX;
import com.camera.gps.camerax.view.CameraGridLinesView;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.dialogs.DateTimeDialog;
import com.camera.gps.dialogs.FontStyleDialog;
import com.camera.gps.dialogs.FpsDialog;
import com.camera.gps.dialogs.GridDialog;
import com.camera.gps.dialogs.RatioDialog;
import com.camera.gps.dialogs.TimerDialog;
import com.camera.gps.dialogs.VideoResolutionDialog;
import com.camera.gps.dialogs.VideoVolumeDialog;
import com.camera.gps.listener.CameraReadyListener;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.listener.OnFontSelectedListener;
import com.camera.gps.listener.OnFpsSelectedListener;
import com.camera.gps.listener.OnGridSelectedListener;
import com.camera.gps.listener.OnRatioSelectedListener;
import com.camera.gps.listener.OnResolutionSelectedListener;
import com.camera.gps.listener.OnSoundSelectedListener;
import com.camera.gps.listener.OnTimerSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.model.StampTemplateDefaults;
import com.camera.gps.premium.PremiumManager;
import com.camera.gps.premium.PremiumTestBottomSheet;
import com.camera.gps.repositories.DateFormatRepository;
import com.camera.gps.util.DirManager;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.LocationAddressFormatter;
import com.camera.gps.util.LocationSettingsPrompt;
import com.camera.gps.util.SharedMediaStore;
import com.camera.gps.util.SafeMapSnapshot;
import com.camera.gps.util.SP;
import com.camera.gps.util.StampBackgroundUtils;
import com.camera.gps.util.StampMetadataUtils;
import com.camera.gps.util.StampSettingsBottomSheets;
import com.camera.gps.util.StampedPhotoComposer;
import com.camera.gps.util.StampedVideoComposer;
import com.camera.gps.viewmodel.DateFormatViewModel;
import com.camera.gps.viewmodel.FontStyleViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.ListenableFuture;
import com.camera.gps.R;
import com.camera.gps.database.entity.Photo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import android.widget.Toast;

import androidx.camera.core.*;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;

import com.camera.gps.util.Utils.LogUtils;
import com.camera.gps.util.Utils;


import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    //Permissions
    private static final int REQ_ALL_PERMISSIONS = 110;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // Android 10 and below
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else { // Android 11+
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                    // no storage permissions needed, handled by MediaStore
            };
        }
    }

    // Camera related variables
    private PreviewView cameraPreview;
    private FrameLayout cameraContainer;
    private Camera camera;
    @SuppressLint("RestrictedApi")
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private ImageCapture imageCapture;
    private CameraControl cameraControl;
    private CameraInfo cameraInfo;
    private CameraReadyListener cameraReadyListener;
    private int lensFacingType = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private float currentZoomRatio = 1.0f;
    private float backCameraZoomRatio = 1.0f;
    private boolean isSwitching = false;

    //Horizontal menu
    //Font selection
    HelperClass mHelperClass = new HelperClass();
    private LinearLayout btnFontStyle;
    public String fontStyle;
    private FontStyleDialog fontStyleDialog;
    private FontStyleViewModel fontViewModel;
    private DateFormatViewModel dateFormatViewModel;

    //Time & Date selection
    private LinearLayout btnDateTime;
    private DateTimeDialog dateTimeDialog;
    String format_Combined;
    String format_Date;
    String format_Time;

    //Timer
    private LinearLayout btnTimer;
    private TextView txtCountDownTakePhoto;
    private TimerDialog timerDialog;
    private CountDownTimer countDownTimer;


    //Exposure
    private View layoutExposure;
    private LinearLayout btnExposure;
    private TextView btnExposure0;
    private TextView btnExposure1;
    private TextView btnExposure2;
    private TextView btnExposure3;
    private TextView btnExposure4;
    private TextView btnExposure_1;
    private TextView btnExposure_2;
    private TextView btnExposure_3;
    private TextView btnExposure_4;
    private TextView arrowExposure_4, arrowExposure_3, arrowExposure_2, arrowExposure_1, arrowExposure0, arrowExposure1, arrowExposure2, arrowExposure3, arrowExposure4;

    //RATIO
    private LinearLayout btnRatio;
    private RatioDialog ratioDialog;
    private int currentRatioType = RatioDialog.RATIO_4_3;

    //GRIDLINES
    private LinearLayout btnGrid;
    private GridDialog gridDialog;
    private int currentGridType = GridDialog.GRID_OFF;
    private CameraGridLinesView gridLinesView;

    //WATERMARK
    private LinearLayout btnWatermark;
    private boolean showWatermark;
    private ConstraintLayout appStamp;

    //VIDEO RESOLUTION
    private LinearLayout btnVideoresolution;
    private VideoResolutionDialog videoResolutionDialog;

    //FPS
    private LinearLayout btnFps;
    private FpsDialog fpsDialog;

    //IMAGE QUALITY
    private LinearLayout btnImgQuality;

    //VIDEO VOLUME
    private LinearLayout btnVideoVolume;
    private VideoVolumeDialog videoVolumeDialog;


    // UI Components
    int current_map_type;
    private Integer sessionMapType;
    private ImageButton btnMap;
    private ImageButton btnCollection;
    private ImageButton btnTemplate;
    private ImageButton btnAddLocation;
    private ActivityResultLauncher resultLauncher;
    private ActivityResultLauncher<IntentSenderRequest> enableLocationLauncher;
    private ActivityResultLauncher<Intent> internetSettingsLauncher;
    private ActivityResultLauncher<Intent> mapTypeResultLauncher;
    private ActivityResultLauncher<Intent> templateResultLauncher;
    private ActivityResultLauncher<Intent> previewResultLauncher;
    private ImageView imgFocus, imgCenterTakeAction, imgVideoRec;
    private TabLayout tabLayout;
    private ViewPager viewPagerSwitchAction;
    private View layoutBottom;
    private ImageButton btnFlash, btnSwitchCamera, btnTakeAction;
    private LinearLayout zoomLayout;
    private TextView btnZoom1x, btnZoom2x, btnZoom3x;
    private boolean isMenuExpanded = false;
    private LinearLayout layoutHorizontalMenu;
    private ImageButton btnExpandMenu;
    private ImageButton btnSettings;

    // Flash overlay for front camera
    private View flashOverlay;

    // Location related variables
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "Loading location...";
    private String currentDefaultTitle = "";
    private String currentTitle;
    private String savedDate;
    private String savedTime;
    private String lonDMS;
    private String latDMS;
    private boolean isCameraReady = false;
    private boolean isLocationFetched = false;
    private boolean isLiveLocationMode = true;
    private Integer activeSavedLocationId;
    private Location lastGeocodedLocation;
    private long lastAddressLookupTime = 0L;
    private int addressLookupGeneration = 0;
    private static final long ADDRESS_LOOKUP_INTERVAL_MS = 15000L;
    private static final float ADDRESS_LOOKUP_DISTANCE_METERS = 30f;
    private boolean isLocationPromptActive = false;
    private AlertDialog internetRequiredDialog;
    private AlertDialog locationFallbackDialog;
    private ConnectivityManager.NetworkCallback internetNetworkCallback;
    private boolean internetNetworkCallbackRegistered = false;

    // Stamp UI components
    private RelativeLayout relBottomStamp;
    private LinearLayout mapViewContainer;
    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private int currentstamp_type, current_DateTimeColor, current_TextColor, current_StampBgColor;
    private boolean hasSessionStampOverride = false;
    private boolean hasSessionDateTimeOverride = false;
    private TextView txtLocation, txtDefaultTitle, txtDateTime, txtLatitude, txtLongitude, txtDate, txtTime, txtTitle, txt_lat_dms, txt_long_dms;
    private LinearLayout dateTimeContainer, latLongContainer, defaultTitleAddressContainer;
    private CardView stampBg;
    private TextView lbl_lat, lbl_long, lbl_date, lbl_gmt, lbl_type, lbl_degree, lbl_dms;
    private SP msp;

    // Date/Time updater
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable dateTimeUpdater;

    //Take Action
    private int timernew = 0;
    private boolean isRecording = false;
    private boolean isVideoRecordingPreparing = false;
    private Animation animationRecVideo;
    private Photo photo;
    private String mediaFilePath;
    private Chronometer chronometerVideo;
    private File videoFile;
    private ImageView ivMyCapture;
    private View previewLoadingIndicator;
    private boolean isPreviewLoading = false;


    private GlobalViewModel viewModel;
    private Photo photoOld;
    private boolean isCapture = false;
    private boolean isMapSetup = false;

//
//    private void updateLocationData(MyLocation location) {
//        // Update current data with the latest from database

//        currentAddress = location.getAddress();
//        currentTitle = location.getTitle();
//        currentLatitude = Double.parseDouble(location.getLatitude());
//        currentLongitude = Double.parseDouble(location.getLongitude());
//        savedDate = location.getDate();
//        savedTime = location.getTime();
//
//        // Update UI
//        updateStampContent();
//    }

    public MainActivity() {
        Log.d("Rishi_MainActivity", "Inside mainacti");
        enableLocationLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            isLocationPromptActive = false;
            if (isLocationEnabled()) {
                isLocationFetched = false;
                setupLocation();
            }
            showInternetDialogIfNeeded();
        });

        internetSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            reconcileInternetDialogState();
            handler.postDelayed(this::reconcileInternetDialogState, 1200L);
        });

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResultCallback<ActivityResult>) result -> {
            Log.d("Rishi_MainActivity", "ResultLauncher callback triggered");
            Log.d("Rishi_MainActivity", "Result code: " + result.getResultCode());

            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Log.d("Rishi_MainActivity", "Data intent is not null");
                    applySettingsSessionOverrides(data);

                    // Check if location data is present
                    if (data.hasExtra(MyApplication.EXTRA_LOCATION)) {
                        Log.d("Rishi_MainActivity", "Location extra found");

                        Serializable locationExtra = data.getSerializableExtra(MyApplication.EXTRA_LOCATION);
                        if (locationExtra instanceof MyLocation) {
                            MyLocation receivedLocation = (MyLocation) locationExtra;
                            isLiveLocationMode = false;
                            activeSavedLocationId = receivedLocation.getId();
                            applyPremiumPreviewScreenshotProtection();
                            Log.d("Rishi_MainActivity", "Received Location Details:");
                            Log.d("Rishi_MainActivity", "ID: " + receivedLocation.getId());
                            Log.d("Rishi_MainActivity", "Title: " + receivedLocation.getTitle());
                            Log.d("Rishi_MainActivity", "Address: " + receivedLocation.getAddress());
                            Log.d("Rishi_MainActivity", "Latitude: " + receivedLocation.getLatitude());
                            Log.d("Rishi_MainActivity", "Longitude: " + receivedLocation.getLongitude());
                            Log.d("Rishi_MainActivity", "Date: " + receivedLocation.getDate());
                            Log.d("Rishi_MainActivity", "Time: " + receivedLocation.getTime());
                            Log.d("Rishi_MainActivity", "Is Selected: " + receivedLocation.isSelected());

                            currentAddress = receivedLocation.getAddress();
                            currentTitle = receivedLocation.getTitle();
                            applySavedDefaultTitle(receivedLocation);
                            currentLatitude = Double.parseDouble(receivedLocation.getLatitude());
                            currentLongitude = Double.parseDouble(receivedLocation.getLongitude());
                            savedDate = receivedLocation.getDate();
                            savedTime = receivedLocation.getTime();
                            updateStampContent();
                            // Handle the received location data here
                            // You can update your UI or perform other operations with the location

                        } else {
                            Log.d("Rishi_MainActivity", "Location extra is not MyLocation instance");
                        }
                    } else {
                        Log.d("Rishi_MainActivity", "No location extra found in data");
                    }
                } else {
                    Log.d("Rishi_MainActivity", "Data intent is null");
                }
            }
            else {
                Log.d("Rishi_MainActivity", "Result code is not RESULT_OK");
                if (set_to_current_location) {
                    // A saved/custom location disables live updates while it is selected.
                    // Re-enable live mode before restarting the location request.
                    isLiveLocationMode = true;
                    activeSavedLocationId = null;
                    currentAddress = null;
                    currentTitle = null;
                    currentDefaultTitle = "";
                    currentLatitude = 0.0;
                    currentLongitude = 0.0;
                    savedDate = null;
                    savedTime = null;
                    applyPremiumPreviewScreenshotProtection();
                    isLocationFetched = false;
                    lastGeocodedLocation = null;
                    lastAddressLookupTime = 0L;
                    setupLocation();
                    set_to_current_location = false;
                    Log.d("Rishi_MainActivity", "Setting current location");
                }

                if (currentTitle != null && !currentTitle.trim().isEmpty()) {
                    Log.d("Rishi_MainActivity", "Checking if location with title '" + currentTitle + "' still exists?");

                    viewModel.getLocationByTitle(currentTitle).observe(this, location -> {
                        if (location == null) {
                            isLiveLocationMode = true;
                            activeSavedLocationId = null;
                            currentAddress = null;
                            currentTitle = null;
                            currentDefaultTitle = "";
                            currentLatitude = 0.0;
                            currentLongitude = 0.0;
                            savedDate = null;
                            savedTime = null;
                            isLiveLocationMode = true;
                            applyPremiumPreviewScreenshotProtection();
                            isLocationFetched = false;
                            setupLocation();
                            //initAfterPermissionsGranted();
                            Log.d("Rishi_MainActivity", "Location with title '" + currentTitle + "' not exists");
                        } else {
                            Log.d("Rishi_MainActivity", "Location with title '" + currentTitle + "' still exists");
                        }
                    });
                }
            }
        });

        mapTypeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                if (data.hasExtra(Map_Activity.EXTRA_SELECTED_MAP_TYPE)) {
                    applyTemporaryMapType(data.getIntExtra(
                            Map_Activity.EXTRA_SELECTED_MAP_TYPE,
                            current_map_type
                    ));
                }
            }
        });

        templateResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                clearSessionStampOverrides();
                getStampType();
                updateMaps();
                renderStamp();
            }
        });

        previewResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (result.getResultCode() == Activity.RESULT_OK && data != null && data.getBooleanExtra("previewDeleted", false)) {
                clearDeletedPreview(data.getIntExtra("deletedPhotoId", -1), data.getStringExtra("deletedPhotoPath"));
            }
        });
    }

    private int getActiveMapTypeForSession() {
        if (sessionMapType != null) {
            return sessionMapType;
        }
        Integer applicationSessionMapType = MyApplication.getSessionMapType();
        if (applicationSessionMapType != null) {
            return applicationSessionMapType;
        }
        return resolveTemplateMapType();
    }

    private void applyTemporaryMapType(int mapType) {
        hasSessionStampOverride = true;
        sessionMapType = mapType;
        MyApplication.setSessionMapType(mapType);
        current_map_type = mapType;
        updateMaps();
    }

    private int resolveTemplateMapType() {
        StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(this, currentstamp_type);
        int fallbackMapType = defaults.mapType;
        if (msp != null) {
            return msp.getTemplateMapType(this, currentstamp_type, fallbackMapType);
        }
        return fallbackMapType;
    }

    private void clearSessionStampOverrides() {
        hasSessionStampOverride = false;
        hasSessionDateTimeOverride = false;
        sessionMapType = null;
        MyApplication.clearSessionMapType();
        MyApplication.clearSessionStampFormatting();
    }

    private void applyApplicationSessionFormatting() {
        String sessionFont = MyApplication.getSessionFontStyle();
        if (sessionFont != null && !sessionFont.trim().isEmpty()) {
            fontStyle = sessionFont;
            hasSessionStampOverride = true;
        }

        String sessionDate = MyApplication.getSessionDateFormat();
        String sessionTime = MyApplication.getSessionTimeFormat();
        String sessionCombined = MyApplication.getSessionCombinedFormat();
        if (sessionDate != null && sessionTime != null && sessionCombined != null) {
            format_Date = sessionDate;
            format_Time = sessionTime;
            format_Combined = sessionCombined;
            hasSessionStampOverride = true;
            hasSessionDateTimeOverride = true;
        }
    }

    private void applySettingsSessionOverrides(Intent data) {
        boolean fontUpdated = false;
        boolean dateTimeUpdated = false;

        if (data.hasExtra(Settings_Activity.EXTRA_SESSION_FONT_STYLE)) {
            fontStyle = data.getStringExtra(Settings_Activity.EXTRA_SESSION_FONT_STYLE);
            MyApplication.setSessionFontStyle(fontStyle);
            hasSessionStampOverride = true;
            fontUpdated = true;
        }
        if (data.hasExtra(Settings_Activity.EXTRA_SESSION_DATE_FORMAT)) {
            format_Date = data.getStringExtra(Settings_Activity.EXTRA_SESSION_DATE_FORMAT);
            format_Time = data.getStringExtra(Settings_Activity.EXTRA_SESSION_TIME_FORMAT);
            format_Combined = data.getStringExtra(Settings_Activity.EXTRA_SESSION_COMBINED_FORMAT);
            MyApplication.setSessionDateTimeFormats(format_Date, format_Time, format_Combined);
            hasSessionStampOverride = true;
            hasSessionDateTimeOverride = true;
            dateTimeUpdated = true;
        }
        if (data.hasExtra(Settings_Activity.EXTRA_SESSION_MAP_TYPE)) {
            applyTemporaryMapType(data.getIntExtra(
                    Settings_Activity.EXTRA_SESSION_MAP_TYPE,
                    current_map_type
            ));
        }

        if (fontUpdated) {
            updateStampContent();
        }
        if (dateTimeUpdated) {
            updateStampDateTime();
        }
        applyPremiumPreviewScreenshotProtection();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable immersive mode - hide navigation buttons
        enableImmersiveMode();
        setContentView(R.layout.activity_main);
        initializeViews();
        refreshPremiumUi();
        initializePhotoObject();
        viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(getApplication())).get(GlobalViewModel.class);
        loadBottomBannerAd();


        // Check permissions first
        if (hasAllPermissions()) {
            initAfterPermissionsGranted();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQ_ALL_PERMISSIONS);
        }

//        if (!hasAllPermissions()) {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQ_ALL_PERMISSIONS);
//        } else {
//            initAfterPermissionsGranted();
//        }

//        if (!hasAllPermissions()) {
//            Log.d("Rishi_permission", "Dont have permission");
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQ_ALL_PERMISSIONS);
//        } else {
//            Log.d("Rishi_permission", "Have permission");
//
//            // Check Internet connectivity
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
//
//            if (isConnected) {
//                Log.d("Rishi_chk", "Internet is ON");
//            } else {
//                Log.d("Rishi_chk", "Internet is OFF");
//                new AlertDialog.Builder(this)
//                        .setTitle("No Internet")
//                        .setMessage("Oops! You're not connected to the internet. GPS Map Camera needs internet to show your location.")
//                        .setCancelable(false)
//                        .setPositiveButton("USE MOBILE DATA", (dialog, which) -> {
//                            // Open Mobile Data settings
//                            Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
//                            startActivity(intent);
//                        })
//                        .setNegativeButton("CONNECT TO WI-FI", (dialog, which) -> {
//                            // Open Wi-Fi settings
//                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                            startActivity(intent);
//                        })
//                        .show();
//            }
//
//            // Check if Location (GPS) is enabled
//            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (isGpsEnabled || isNetworkEnabled) {
//                Log.d("Rishi_chk", "Location is ON");
//            } else {
//                Log.d("Rishi_chk", "Location is OFF");
//                new AlertDialog.Builder(this)
//                        .setTitle("Location Disabled")
//                        .setMessage("Please turn on Location (GPS) to continue.")
//                        .setPositiveButton("Turn On", (dialog, which) -> {
//                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            startActivity(intent);
//                        })
//                        .setCancelable(false)
//                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                        .show();
//            }
//
//            // If everything is fine
//            initAfterPermissionsGranted();
//        }

//        if (!hasAllPermissions()) {
//            Log.d("Rishi_permission", "Dont have permission");
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQ_ALL_PERMISSIONS);
//        } else {
//            Log.d("Rishi_permission", "Have permission");
//            initAfterPermissionsGranted();
//        }

        //     initAfterPermissionsGranted();
    }


    // ========================== PERMISSIONS ==========================


    private void initializePhotoObject() {
        photo = new Photo();
        photo.setSelected(false);
    }

    private void loadBottomBannerAd() {
        FrameLayout bannerContainer = findViewById(R.id.flMainBanner);
        bannerContainer.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                   oldLeft, oldTop, oldRight, oldBottom) ->
                updateBottomOptionsBannerOffset(bannerContainer));
        updateBottomOptionsBannerOffset(bannerContainer);
        if (!Utils.getIsPremium(this)) {
            bannerContainer.post(() -> com.camera.gps.adsmanager.admob.AdMobBannerAdHelper
                    .loadBannerAd(this, bannerContainer, "main_banner"));
        } else {
            bannerContainer.setVisibility(GONE);
            updateBottomOptionsBannerOffset(bannerContainer);
        }
    }

    private void updateBottomOptionsBannerOffset(FrameLayout bannerContainer) {
        if (layoutBottom == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = layoutBottom.getLayoutParams();
        if (!(rawParams instanceof RelativeLayout.LayoutParams)) {
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rawParams;
        int bannerOffset = 0;
        if (bannerContainer.getVisibility() == VISIBLE && bannerContainer.getHeight() > 0) {
            bannerOffset = bannerContainer.getHeight();
            ViewGroup.LayoutParams bannerParams = bannerContainer.getLayoutParams();
            if (bannerParams instanceof ViewGroup.MarginLayoutParams) {
                bannerOffset += ((ViewGroup.MarginLayoutParams) bannerParams).bottomMargin;
            }
        }
        if (params.bottomMargin != bannerOffset) {
            params.bottomMargin = bannerOffset;
            layoutBottom.setLayoutParams(params);
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_ALL_PERMISSIONS) {
            boolean allGranted = true;
            boolean someDeniedForever = false;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;

                    // Check if user ticked "Dont allow"
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        someDeniedForever = true;
                    }
                }
            }

            if (allGranted) {
                initAfterPermissionsGranted();
            } else {
                // User selected "Never ask again" → guide to settings
                Toast.makeText(this, "Please enable permissions from Settings.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
                finish();
            }
        }
    }


//    private boolean hasAllPermissions() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }

//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQ_ALL_PERMISSIONS) {
//            boolean allGranted = true;
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    allGranted = false;
//                    break;
//                }
//            }
//
//            if (allGranted) {
//                initAfterPermissionsGranted();
//            } else {
//                Toast.makeText(this, "All permissions are required to continue.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    private void initAfterPermissionsGranted() {
//        initializeViews();
        startCameraPreview();
        chk_Location_Internet();

        setCameraReadyListener(() -> {
            getStampType();
            setupLocation();
            setupDateTimeUpdater();
            setupEventListeners();
            getStampFont();
            getStampDateTime();
            getStampBgColor();
            getTextColor();
            getDateTimeColor();
        });
    }


    private void chk_Location_Internet() {
        // Location is always resolved first. The internet dialog is checked only after the
        // Android location prompt has closed, so the two dialogs can never overlap.
        if (!isLocationEnabled()) {
            Log.d("Rishi_chk", "Location is OFF");
            showLocationDialog();
            return;
        }

        showInternetDialogIfNeeded();
    }

    private boolean hasValidatedInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void showInternetDialogIfNeeded() {
        if (isLocationPromptActive
                || (locationFallbackDialog != null && locationFallbackDialog.isShowing())) {
            return;
        }

        if (!hasValidatedInternetConnection()) {
            Log.d("Rishi_chk", "Validated internet is OFF");
            showInternetDialog();
        }
    }

    private void reconcileInternetDialogState() {
        if (hasValidatedInternetConnection()) {
            if (internetRequiredDialog != null && internetRequiredDialog.isShowing()) {
                internetRequiredDialog.dismiss();
            }
            retryAddressResolutionIfPossible();
        } else {
            showInternetDialogIfNeeded();
        }
    }

    private void registerInternetNetworkCallback() {
        if (internetNetworkCallbackRegistered) {
            return;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        internetNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                handler.post(MainActivity.this::reconcileInternetDialogState);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities networkCapabilities) {
                handler.post(MainActivity.this::reconcileInternetDialogState);
            }

            @Override
            public void onLost(@NonNull Network network) {
                handler.post(MainActivity.this::reconcileInternetDialogState);
            }
        };
        try {
            connectivityManager.registerDefaultNetworkCallback(internetNetworkCallback);
            internetNetworkCallbackRegistered = true;
        } catch (RuntimeException exception) {
            Log.e("Internet", "Unable to register network callback", exception);
        }
    }

    private void unregisterInternetNetworkCallback() {
        if (!internetNetworkCallbackRegistered || internetNetworkCallback == null) {
            return;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            connectivityManager.unregisterNetworkCallback(internetNetworkCallback);
        } catch (RuntimeException exception) {
            Log.e("Internet", "Unable to unregister network callback", exception);
        }
        internetNetworkCallbackRegistered = false;
        internetNetworkCallback = null;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void enableImmersiveMode() {
        // Remove fullscreen flag so status bar is visible
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Make the status bar transparent
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }

    // ========================== INITIALIZATION METHODS ==========================

    private void initializeViews() {
        // Camera views
        cameraPreview = findViewById(R.id.cameraPreview);
        cameraContainer = findViewById(R.id.cameraContainer);
        viewPagerSwitchAction = findViewById(R.id.viewPagerSwitchAction);
        viewPagerSwitchAction.setAdapter(new ViewPagerTitleAdapter(getSupportFragmentManager(), MainController.getFragments(), MainController.getTitles()));

        //Horizontal Menu
        btnFontStyle = findViewById(R.id.btnFontStyle);
        btnDateTime = findViewById(R.id.btnDateTime);
        btnTimer = findViewById(R.id.btnTimer);
        txtCountDownTakePhoto = findViewById(R.id.txtCountDownTakePhoto);
        btnExposure = findViewById(R.id.btnExposure);
        layoutExposure = findViewById(R.id.layoutExposure);
        btnExposure_4 = findViewById(R.id.btnExposure_4);
        btnExposure_3 = findViewById(R.id.btnExposure_3);
        btnExposure_2 = findViewById(R.id.btnExposure_2);
        btnExposure_1 = findViewById(R.id.btnExposure_1);
        btnExposure0 = findViewById(R.id.btnExposure0);
        btnExposure1 = findViewById(R.id.btnExposure1);
        btnExposure2 = findViewById(R.id.btnExposure2);
        btnExposure3 = findViewById(R.id.btnExposure3);
        btnExposure4 = findViewById(R.id.btnExposure4);
        arrowExposure_4 = findViewById(R.id.arrowExposure_4);
        arrowExposure_3 = findViewById(R.id.arrowExposure_3);
        arrowExposure_2 = findViewById(R.id.arrowExposure_2);
        arrowExposure_1 = findViewById(R.id.arrowExposure_1);
        arrowExposure0 = findViewById(R.id.arrowExposure0);
        arrowExposure1 = findViewById(R.id.arrowExposure1);
        arrowExposure2 = findViewById(R.id.arrowExposure2);
        arrowExposure3 = findViewById(R.id.arrowExposure3);
        arrowExposure4 = findViewById(R.id.arrowExposure4);
        btnRatio = findViewById(R.id.btnRatio);
        btnGrid = findViewById(R.id.btnGrid);
        gridLinesView = findViewById(R.id.gridLinesView);
        currentGridType = SharedPrefsSettings.getGridType(this);
        updateGridDisplay();
        btnWatermark = findViewById(R.id.btnWatermark);
        btnVideoresolution = findViewById(R.id.btnResolution);
        btnFps = findViewById(R.id.btnFps);
        btnImgQuality = findViewById(R.id.btnImageQuality);
        btnVideoVolume = findViewById(R.id.btnVideoVolume);


        // UI controls
        imgFocus = findViewById(R.id.imgFocus);
        tabLayout = findViewById(R.id.tabLayout);
        imgCenterTakeAction = findViewById(R.id.imgCenterTakeAction);
        imgVideoRec = findViewById(R.id.imgVideoRec);
        layoutBottom = findViewById(R.id.layoutBottom);
        btnFlash = findViewById(R.id.btnFlash);
        btnSettings = findViewById(R.id.btnSettings);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnTakeAction = findViewById(R.id.btnTakeAction);
        btnMap = findViewById(R.id.btnMap);
        btnCollection = findViewById(R.id.btnCollection);
        btnAddLocation = findViewById(R.id.btnAddLocation);
        btnTemplate = findViewById(R.id.btnTemplate);

        // Zoom controls
        zoomLayout = findViewById(R.id.zoomLayout);
        btnZoom1x = findViewById(R.id.btnZoom1x);
        btnZoom2x = findViewById(R.id.btnZoom2x);
        btnZoom3x = findViewById(R.id.btnZoom3x);
        zoomLayout.setVisibility(GONE);

        // Menu controls
        layoutHorizontalMenu = findViewById(R.id.layoutHorizontalMenu);
        btnExpandMenu = findViewById(R.id.btnExpandMenu);

        // Stamp container
        relBottomStamp = findViewById(R.id.gallery_rel_bottom_stamp);

        //take action
        animationRecVideo = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_recording);
        chronometerVideo = findViewById(R.id.chronometerVideo);
        ivMyCapture = findViewById(R.id.ivMyCapture);
        previewLoadingIndicator = findViewById(R.id.previewLoadingIndicator);

        // Create flash overlay for front camera flash
        createFlashOverlay();

        fontViewModel = new ViewModelProvider(this).get(FontStyleViewModel.class);
        msp = new SP(this);
        // Initialize Photo object
        photo = new Photo();
    }

    private void createFlashOverlay() {
        flashOverlay = new View(this);
        flashOverlay.setBackgroundColor(Color.WHITE);
        flashOverlay.setVisibility(GONE);

        // Add to root layout
        RelativeLayout rootLayout = findViewById(R.id.mainll);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.addView(flashOverlay, params);
    }

    @SuppressLint("MissingPermission")
    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                5000L)
                .setMinUpdateIntervalMillis(2000L)
                .setMinUpdateDistanceMeters(5f)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (!isLiveLocationMode) return;

                Location location = result.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };

        // setupLocation can finish after onResume (for example, after permission or
        // location-settings flows), so start immediately instead of waiting for the
        // next lifecycle transition.
        requestLocationUpdates();
    }

    private void setupDateTimeUpdater() {
        dateTimeUpdater = new Runnable() {
            @Override
            public void run() {
                updateStampDateTime();
                handler.postDelayed(this, 1000L);
            }
        };
        handler.post(dateTimeUpdater);
    }

    private void setupEventListeners() {


        setUpHorizontalMenuListeners();
        setupZoomListeners();
        setupCameraListeners();
        setupTabListeners();
        setupMenuListeners();
        setUpTakeActionListeners();
        setUpBottomTabListeners();
    }

    // ========================== TAKE ACTIONS ==========================

    private void setUpTakeActionListeners() {
        this.btnTakeAction.setOnClickListener(view -> {

            if (!isCameraReady) {
                Toast.makeText(this, "Camera is initializing...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isLocationFetched) {
                Toast.makeText(this, "Loading location...", Toast.LENGTH_SHORT).show();
                return;
            }

            takeAction();
        });
    }

    @SuppressLint("RestrictedApi")
    private void takeAction() {
        if (viewPagerSwitchAction.getCurrentItem() == 0) {
            if (timernew == 0) {
                takePhoto();
                return;
            }
            btnTakeAction.setVisibility(GONE);
            imgCenterTakeAction.setVisibility(GONE);
            startCountDown(viewPagerSwitchAction.getCurrentItem());
        } else if (isRecording) {
            stopVideoRecording();
        } else {
            if (!isVideoRecordingPreparing) {
                if (timernew == 0) {
                    recordVideo();
                    return;
                }
                btnTakeAction.setVisibility(GONE);
                imgVideoRec.setVisibility(GONE);
                startCountDown(viewPagerSwitchAction.getCurrentItem());
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void stopVideoRecording() {
        if (activeRecording != null && isRecording) {
            activeRecording.stop();
            activeRecording = null;
            UtilsX.playSound(R.raw.stop_recording_sound, getBaseContext());
            MainController.stopRecAnimation(animationRecVideo);
            MainController.stopChronometer(chronometerVideo);
        }
    }

    // Update the takePhoto method
    @SuppressLint("WrongConstant")
    public void takePhoto() {
        if (blockCaptureForPremiumPreview()) {
            return;
        }
        File mediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (mediaFile == null) {
            return;
        }

        // Handle front camera flash
        if (lensFacingType == CameraSelector.LENS_FACING_FRONT && flashMode == ImageCapture.FLASH_MODE_ON) {
            showFrontCameraFlash();
        }

        UtilsX.playSound(R.raw.take_photo_sound, this);
        this.btnTakeAction.setEnabled(false);
        setPreviewLoading(true);
        UtilsX.animateBtnTakePhoto(this.btnTakeAction);
        ImageCapture.OutputFileOptions build = new ImageCapture.OutputFileOptions.Builder(mediaFile).build();

        this.imageCapture.setTargetRotation(UtilsX.getDisplayRotation(this));
        this.imageCapture.setFlashMode(this.flashMode);
        this.imageCapture.takePicture(build, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                StampedPhotoComposer.writeStampedPhoto(
                        MainActivity.this,
                        mediaFile,
                        relBottomStamp,
                        googleMap,
                        mapViewContainer,
                        success -> {
                            if (success) {
                                publishCapturedMedia(mediaFile, false);
                            } else {
                                // If the activity/map surface disappeared during capture,
                                // preserve and publish the original photo without a stamp.
                                publishCapturedMedia(mediaFile, false);
                            }
                        });

                // Hide front camera flash overlay
                if (flashOverlay.getVisibility() == VISIBLE) {
                    hideFrontCameraFlash();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException imageCaptureException) {
                if (mediaFile.exists()) {
                    mediaFile.delete();
                }
                btnTakeAction.setEnabled(true);
                setPreviewLoading(false);
                Toast.makeText(getBaseContext(), "Error taking photo: " + imageCaptureException.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                // Hide front camera flash overlay
                if (flashOverlay.getVisibility() == VISIBLE) {
                    hideFrontCameraFlash();
                }
            }
        });
    }

    @SuppressLint({"RestrictedApi", "MissingPermission"})
    private void recordVideo() {
        if (blockCaptureForPremiumPreview()) {
            return;
        }
        if (isVideoRecordingPreparing || isRecording) {
            return; // Prevent multiple calls
        }

        // Disable flash for front camera video recording
        if (lensFacingType == CameraSelector.LENS_FACING_FRONT) {
            disableFlashForFrontCamera();
        }

        videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (videoFile == null) {
            Toast.makeText(this, "Failed to create video file", Toast.LENGTH_SHORT).show();
            return;
        }

        isVideoRecordingPreparing = true;

        // Get sound setting
        boolean soundEnabled = SharedPrefsSettings.getSoundStatus(getBaseContext());

        FileOutputOptions outputOptions = new FileOutputOptions.Builder(videoFile).build();
        this.videoCapture.setTargetRotation(UtilsX.getDisplayRotation(this));

        // Check permissions and start recording based on sound setting
        if (soundEnabled) {
            // Sound enabled - require audio permission
            if (ActivityCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") == 0) {
                startVideoRecording(outputOptions, soundEnabled);
            } else {
                isVideoRecordingPreparing = false;
                Toast.makeText(this, "Audio permission required for video recording with sound", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Sound disabled - record without audio permission check
            startVideoRecording(outputOptions, soundEnabled);
        }
    }

    @SuppressLint({"RestrictedApi", "MissingPermission"})
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startVideoRecording(FileOutputOptions outputOptions, boolean soundEnabled) {
        PendingRecording pendingRecording = this.videoCapture.getOutput().prepareRecording(this, outputOptions);
        if (soundEnabled) {
            pendingRecording = pendingRecording.withAudioEnabled();
        }

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                isVideoRecordingPreparing = false;
                isRecording = false;
                activeRecording = null;
                btnTakeAction.setEnabled(false);
                MainController.stopChronometer(chronometerVideo);
                MainController.stopRecAnimation(animationRecVideo);

                if (finalizeEvent.hasError()) {
                    if (videoFile != null && videoFile.exists()) {
                        videoFile.delete();
                    }
                    btnTakeAction.setEnabled(true);
                    String message = "Video recording error: " + finalizeEvent.getError();
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("Rishi_Video", message, finalizeEvent.getCause());
                    return;
                }

                setPreviewLoading(true);

                // Update UI with captured video
                Uri outputUri = finalizeEvent.getOutputResults().getOutputUri();
                if (outputUri != null && outputUri != Uri.EMPTY) {
                    Glide.with(getBaseContext()).load(outputUri).into(ivMyCapture);
                    mediaFilePath = outputUri.getPath();
                } else if (videoFile.exists()) {
                    // Fallback to file path
                    Glide.with(getBaseContext()).load(videoFile).into(ivMyCapture);
                    mediaFilePath = videoFile.getAbsolutePath();
                }

                finalizeSavedVideo(videoFile);

                // Log recording completion with settings used
                Log.d("Rishi_Video", "Video recorded successfully with sound: " + soundEnabled);
            }
        });

        // Start recording indicators
        this.isRecording = true;
        isVideoRecordingPreparing = false;
        MainController.startRecAnimation(imgVideoRec, animationRecVideo);
        MainController.startChronometer(chronometerVideo);
    }

    @androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    private void finalizeSavedVideo(File recordedFile) {
        if (recordedFile == null || photo == null || photo.getImagePath() == null) {
            setPreviewLoading(false);
            btnTakeAction.setEnabled(true);
            return;
        }

        StampedVideoComposer.writeStampedVideo(this, photo, relBottomStamp, googleMap, mapViewContainer, () -> {
            publishCapturedMedia(recordedFile, true);
        });
    }

    private void publishCapturedMedia(File capturedFile, boolean video) {
        final Photo capturedPhoto = photo;
        SharedMediaStore.publishAsync(this, capturedFile, video,
                new SharedMediaStore.PublishCallback() {
                    @Override
                    public void onSuccess(SharedMediaStore.PublishedMedia media) {
                        if (capturedPhoto == null) {
                            SharedMediaStore.delete(MainActivity.this,
                                    media.getUri().toString(), media.getPath());
                            if (canUpdateCaptureUi()) {
                                btnTakeAction.setEnabled(true);
                                setPreviewLoading(false);
                            }
                            return;
                        }

                        capturedPhoto.setImagePath(media.getPath());
                        capturedPhoto.setMediaUri(media.getUri().toString());
                        capturedPhoto.setMediaType(video ? "video" : "image");
                        mediaFilePath = media.getPath();
                        isCapture = true;

                        if (canUpdateCaptureUi()) {
                            Glide.with(MainActivity.this)
                                    .load(media.getUri())
                                    .into(ivMyCapture);
                        }
                        saveMapSnapshotAndInsertPhoto(
                                capturedPhoto, capturedPhoto.getDateTimeTaken());
                    }

                    @Override
                    public void onError(String error) {
                        if (capturedFile.exists()) {
                            capturedFile.delete();
                        }
                        if (canUpdateCaptureUi()) {
                            btnTakeAction.setEnabled(true);
                            setPreviewLoading(false);
                            Toast.makeText(MainActivity.this,
                                    "Unable to save to Gallery: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @SuppressLint("WrongConstant")
    private void muteVideoAudio(String videoPath) {
        try {
            File originalFile = new File(videoPath);
            File mutedFile = new File(originalFile.getParent(), "muted_" + originalFile.getName());

            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(videoPath);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);

            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            int rotationDegrees = (rotation != null) ? Integer.parseInt(rotation) : 0;

            MediaMuxer muxer = new MediaMuxer(mutedFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            muxer.setOrientationHint(rotationDegrees);

            // Copy only video track
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    int trackIndex = muxer.addTrack(format);
                    muxer.start();

                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                    while (true) {
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) break;

                        info.offset = 0;
                        info.size = sampleSize;
                        info.presentationTimeUs = extractor.getSampleTime();
                        info.flags = extractor.getSampleFlags();

                        muxer.writeSampleData(trackIndex, buffer, info);
                        extractor.advance();
                    }
                    break;
                }
            }

            muxer.stop();
            muxer.release();
            extractor.release();
            retriever.release();

            // Replace original file with muted version
            if (mutedFile.exists() && originalFile.delete()) {
                mutedFile.renameTo(originalFile);
                Log.d("Rishi_Video", "Audio successfully removed and rotation preserved");
            }

        } catch (Exception e) {
            Log.e("Rishi_Video", "Error removing audio from video", e);
        }
    }

    private void showFrontCameraFlash() {
        flashOverlay.setVisibility(VISIBLE);
        flashOverlay.setAlpha(1.0f);

        // Auto-hide after a short duration
        new Handler().postDelayed(this::hideFrontCameraFlash, 200);
    }

    private void hideFrontCameraFlash() {
        flashOverlay.animate().alpha(0.0f).setDuration(100).withEndAction(() -> flashOverlay.setVisibility(GONE)).start();
    }

    private File getOutputMediaFile(int type) {
        File generateFile = DirManager.Companion.generateFile();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        Log.d("RishiDate", timeStamp);
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE)
            mediaFile = new File(generateFile.getPath() + File.separator + "IMG_" + timeStamp + ".jpeg");
        else if (type == MEDIA_TYPE_VIDEO)
            mediaFile = new File(generateFile.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        else return null;

        try {
            if (photo != null) {
                photo.setImagePath(mediaFile.getAbsolutePath());
                photo.setDateTimeTaken(timeStamp);
                // IMPORTANT: Use the location data that was captured when the photo was taken
                // Make sure we have valid location data
                if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                    photo.setLatitude(String.valueOf(currentLatitude));
                    photo.setLongitude(String.valueOf(currentLongitude));
                    photo.setAddress(getSafeCaptureAddress());
                } else {
                    // Fallback values if location is not available
                    photo.setLatitude("0.0");
                    photo.setLongitude("0.0");
                    photo.setAddress("Location not available");
                }

                if (savedDate != null && !savedDate.trim().isEmpty() && savedTime != null && !savedTime.trim().isEmpty()) {
                    Log.d("Location_date", "If block");
                    // Custom locations already contain the user-selected display values. Their
                    // formats are not guaranteed to be dd-MM-yyyy / hh:mm:ss a, so parsing them
                    // with fixed patterns can abort the rest of the photo metadata initialization.
                    photo.setDate(savedDate);
                    photo.setTime(savedTime);
                } else {
                    Log.d("Location_date", "Else block");
                    SimpleDateFormat stampFormat = new SimpleDateFormat(format_Date, Locale.getDefault());
                    photo.setDate(stampFormat.format(new Date()));
                    SimpleDateFormat timeFormat = new SimpleDateFormat(format_Time, Locale.getDefault());
                    photo.setTime(timeFormat.format(new Date()));
                }

                photo.setType(currentstamp_type);
                photo.setTitle(currentTitle);
                photo.setFontStyle(fontStyle);
                photo.setMap_type(current_map_type);
                photo.setShow_watermark(showWatermark);
                photo.setLat_dms(latDMS);
                photo.setLong_dms(lonDMS);
                photo.setCurrent_bg_color(current_StampBgColor);
                photo.setCurrent_text_color(current_TextColor);
                photo.setCurrent_datetime_color(current_DateTimeColor);
                photo.setRatio(currentRatioType);


                // Log the data being saved for debugging
                Log.d("PhotoCapture", "Saving photo with:");
                Log.d("PhotoCapture", "Lat: " + photo.getLatitude());
                Log.d("PhotoCapture", "Lng: " + photo.getLongitude());
                Log.d("PhotoCapture", "Address: " + photo.getAddress());
                Log.d("PhotoCapture", "Date: " + photo.getDate());
                Log.d("PhotoCapture", "Time: " + photo.getTime());
                Log.d("PhotoCapture", "FontStyle: " + photo.getFontStyle());
                Log.d("PhotoCapture", "StampType: " + photo.getType());
                Log.d("PhotoCapture", "MapType: " + photo.getMap_type());
                Log.d("PhotoCapture", "DMS Lat: " + photo.getLat_dms());
                Log.d("PhotoCapture", "DMS Long: " + photo.getLong_dms());
                Log.d("PhotoCapture", "WaterMark: " + photo.getShow_watermark());
                Log.d("PhotoCapture", "bg col: " + photo.getCurrent_bg_color());
                Log.d("PhotoCapture", "txt col: " + photo.getCurrent_text_color());
                Log.d("PhotoCapture", "datetime col: " + photo.getCurrent_datetime_color());
                Log.d("PhotoCapture", "ratio " + photo.getRatio());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    private void saveMapSnapshotAndInsertPhoto(Photo capturedPhoto, String timeStamp) {
        if (capturedPhoto == null) {
            return;
        }

        double capturedLatitude = StampMetadataUtils.latitudeOrNaN(capturedPhoto.getLatitude());
        double capturedLongitude = StampMetadataUtils.longitudeOrNaN(capturedPhoto.getLongitude());
        if (!canUpdateCaptureUi()
                || googleMap == null
                || !StampMetadataUtils.hasCoordinates(capturedLatitude, capturedLongitude)
                || capturedLatitude == 0.0
                || capturedLongitude == 0.0) {
            insertPhoto(capturedPhoto);
            return;
        }

        SafeMapSnapshot.capture(this, googleMap, mapViewContainer, bitmap -> {
            if (bitmap != null) {
                String savedMapPath = saveMapBitmap(bitmap, timeStamp);
                capturedPhoto.setMapImagePath(savedMapPath);
            }
            insertPhoto(capturedPhoto);
        });
    }

    private String saveMapBitmap(Bitmap bitmap, String timeStamp) {
        try {
            File outputFolder = DirManager.Companion.generateFile();
            File mapFile = new File(outputFolder, "MAP_" + timeStamp + ".png");
            FileOutputStream fos = new FileOutputStream(mapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return mapFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("PhotoCapture", "Failed to save map snapshot", e);
            return null;
        }
    }


    private void insertPhoto(Photo capturedPhoto) {
        if (capturedPhoto != null && capturedPhoto.getImagePath() != null) {
            // Persistence must complete even if this activity was destroyed while
            // the asynchronous map snapshot or MediaStore publish was running.
            if (canUpdateCaptureUi()) {
                RequestManager with = Glide.with(this);
                RequestBuilder<Drawable> load = with.load(capturedPhoto.getImagePath());
                load.into(ivMyCapture);
            }

            // Insert into database
            this.viewModel.insertPhoto(capturedPhoto).observe(this, new Observer() {
                @Override
                public void onChanged(Object obj) {
                    if (obj != null) {

                        long insertedId = (long) obj;
                        Log.d("PhotoCapture", "Photo inserted with ID: " + insertedId);

                        capturedPhoto.setId((int) insertedId);

                        if (!canUpdateCaptureUi() || photo != capturedPhoto) {
                            return;
                        }

                        btnTakeAction.setEnabled(true);
                        setPreviewLoading(false);

                        // Copy for photoOld
                        photoOld = new Photo();
                        photoOld.setId((int) insertedId);
                        photoOld.setImagePath(capturedPhoto.getImagePath());
                        photoOld.setMediaUri(capturedPhoto.getMediaUri());
                        photoOld.setMediaType(capturedPhoto.getMediaType());
                        photoOld.setLatitude(capturedPhoto.getLatitude());
                        photoOld.setLongitude(capturedPhoto.getLongitude());
                        photoOld.setAddress(capturedPhoto.getAddress());
                        photoOld.setDate(capturedPhoto.getDate());
                        photoOld.setTime(capturedPhoto.getTime());
                        photoOld.setType(capturedPhoto.getType());
                        photoOld.setTitle(capturedPhoto.getTitle());
                        photoOld.setFontStyle(capturedPhoto.getFontStyle());
                        photoOld.setDateTimeTaken(capturedPhoto.getDateTimeTaken());
                        photoOld.setMap_type(capturedPhoto.getMap_type());
                        photoOld.setMapImagePath(capturedPhoto.getMapImagePath());
                        photoOld.setShow_watermark(capturedPhoto.getShow_watermark());
                        photoOld.setLong_dms(capturedPhoto.getLong_dms());
                        photoOld.setLat_dms(capturedPhoto.getLat_dms());
                        photoOld.setCurrent_datetime_color(capturedPhoto.getCurrent_datetime_color());
                        photoOld.setCurrent_bg_color(capturedPhoto.getCurrent_bg_color());
                        photoOld.setCurrent_text_color(capturedPhoto.getCurrent_text_color());
                        photoOld.setRatio(capturedPhoto.getRatio());
                        // Create a new photo object for next capture
                        initializePhotoObject();
                    } else {
                        Log.e("PhotoCapture", "Failed to insert photo");
                        SharedMediaStore.delete(MainActivity.this, capturedPhoto);
                        if (canUpdateCaptureUi() && photo == capturedPhoto) {
                            btnTakeAction.setEnabled(true);
                            setPreviewLoading(false);
                            isCapture = false;
                            ivMyCapture.setImageResource(R.drawable.my_capture_icon);
                            initializePhotoObject();
                        }
                    }
                }
            });
        } else if (canUpdateCaptureUi()) {
            setPreviewLoading(false);
            btnTakeAction.setEnabled(true);
        }
    }

    private boolean canUpdateCaptureUi() {
        return !isFinishing() && !isDestroyed();
    }

    private void setPreviewLoading(boolean loading) {
        if (isPreviewLoading == loading) {
            return;
        }
        isPreviewLoading = loading;
        if (previewLoadingIndicator != null) {
            previewLoadingIndicator.setVisibility(loading ? VISIBLE : GONE);
        }
        if (ivMyCapture != null) {
            ivMyCapture.setEnabled(!loading);
        }
    }

    // ========================== CAMERA METHODS ==========================

    public void setCameraReadyListener(CameraReadyListener listener) {
        this.cameraReadyListener = listener;
    }

    private void startCameraPreview() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // initialize photo & video capture before binding
                initImageCapture();
                initVideoCapture();

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
               // cameraProvider.unbindAll();
                bindCameraUseCases(cameraProvider);

                boolean firstCameraReady = !isCameraReady;
                if (cameraReadyListener != null && firstCameraReady) {
                    runOnUiThread(() -> {
                        isCameraReady = true;
                        cameraReadyListener.onCameraReady();
                    });
                } else {
                    isCameraReady = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    @SuppressLint("RestrictedApi")
    private void initVideoCapture() {
        int[] videoSizes = SharedPrefsSettings.getVideoSizes(getBaseContext());
        int fps = SharedPrefsSettings.getFps(getBaseContext());
        boolean soundEnabled = SharedPrefsSettings.getSoundStatus(getBaseContext());
        Log.d("Rishi_Video", "Initializing VideoCapture with resolution: " + videoSizes[0] + "x" + videoSizes[1] + " at " + fps + " FPS" + soundEnabled + "sound");

        this.videoCapture = createVideoCapture(chooseVideoQuality(videoSizes), AspectRatio.RATIO_16_9);

//
//        int width = 1280;   // 720p width
//        int height = 720;   // 720p height
//        int fps = 30;       // Standard frame rate
//
//        Log.d("Rishi_Video", "Initializing VideoCapture with resolution: "
//                + width + "x" + height + " at " + fps + " FPS");
//
//        this.videoCapture = new VideoCapture.Builder()
//                .setVideoFrameRate(fps)
//                .setTargetResolution(new Size(1280, 720))
//                .build();
    }

//    @SuppressLint("RestrictedApi")
//    private void initVideoCapture() {
//        int[] videoSizes = SharedPrefsSettings.getVideoSizes(getBaseContext());
//        int fps = SharedPrefsSettings.getFps(getBaseContext());
//        boolean soundEnabled = SharedPrefsSettings.getSoundStatus(getBaseContext());
//
//        Log.d("Rishi_Video", "Initializing VideoCapture with resolution: " + videoSizes[0] + "x" + videoSizes[1] + " at " + fps + " FPS");
//
//        // Add fallback logic for Android 10
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//            // Use more conservative settings for Android 10
//            videoSizes = getSafeVideoSize(videoSizes);
//            fps = 30; // Cap at 30 FPS for older devices
//        }
//
//        this.videoCapture = new VideoCapture.Builder()
//                .setVideoFrameRate(fps)
//                .setMaxResolution(new Size(videoSizes[0], videoSizes[1]))
//                .build();
//    }

    private int[] getSafeVideoSize(int[] requestedSize) {
        // Common safe resolutions for Android 10
        int[][] safeSizes = {
                {1920, 1080}, // 1080p
                {1280, 720},  // 720p
                {640, 480}    // 480p fallback
        };

        for (int[] safeSize : safeSizes) {
            if (requestedSize[0] <= safeSize[0] && requestedSize[1] <= safeSize[1]) {
                return requestedSize;
            }
        }

        // Return 720p as fallback
        return new int[]{1280, 720};
    }

    private void initImageCapture() {
        boolean isMaxQuality = SharedPrefsSettings.getImageMaxQuality(this);
        int captureMode = isMaxQuality ? ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY : ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;

        Log.d("Rishi_Image", "Initializing ImageCapture | MaxQuality: " + isMaxQuality + " | CaptureMode: " + captureMode);

        imageCapture = new ImageCapture.Builder().setCaptureMode(captureMode).build();
    }


    // ========================== RATIO AND STAMP HEIGHT SETTINGS ==========================

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview;

        // Handle different aspect ratios based on currentRatioType
        switch (currentRatioType) {
            case RatioDialog.RATIO_FULL:
                // Full screen setup
                initImageCapture();
                initVideoCapture();
                preview = new Preview.Builder().build();
                updateCameraPreviewSizeFull();
                break;

            case RatioDialog.RATIO_16_9:
                // 16:9 aspect ratio setup
                initializeImageCaptureWithRatio(AspectRatio.RATIO_16_9);
                initializeVideoCaptureWithRatio(AspectRatio.RATIO_16_9);
                preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
                updateCameraPreviewFor16_9();
                break;

            case RatioDialog.RATIO_4_3:
            default:
                // 4:3 aspect ratio setup (default)
                initializeImageCaptureWithRatio(AspectRatio.RATIO_4_3);
                initializeVideoCaptureWithRatio(AspectRatio.RATIO_4_3);
                preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                updateCameraPreviewFor4_3();
                break;
        }

        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        UseCase captureUseCase = viewPagerSwitchAction.getCurrentItem() == 0 ? imageCapture : videoCapture;

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacingType).build();

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, captureUseCase, preview);

        cameraControl = camera.getCameraControl();
        cameraInfo = camera.getCameraInfo();
        configureExposureControls();
        setZoomRatio(getZoomRatioForCurrentLens());
        updateZoomVisibility();
    }

    private void configureExposureControls() {
        if (camera == null) {
            return;
        }

        ExposureState exposureState = camera.getCameraInfo().getExposureState();
        boolean isSupported = exposureState.isExposureCompensationSupported();
        btnExposure.setVisibility(isSupported ? VISIBLE : GONE);

        if (!isSupported) {
            layoutExposure.setVisibility(GONE);
            return;
        }

        Range<Integer> supportedRange = exposureState.getExposureCompensationRange();
        setExposureOptionAvailability(btnExposure_4, arrowExposure_4, -4, supportedRange);
        setExposureOptionAvailability(btnExposure_3, arrowExposure_3, -3, supportedRange);
        setExposureOptionAvailability(btnExposure_2, arrowExposure_2, -2, supportedRange);
        setExposureOptionAvailability(btnExposure_1, arrowExposure_1, -1, supportedRange);
        setExposureOptionAvailability(btnExposure0, arrowExposure0, 0, supportedRange);
        setExposureOptionAvailability(btnExposure1, arrowExposure1, 1, supportedRange);
        setExposureOptionAvailability(btnExposure2, arrowExposure2, 2, supportedRange);
        setExposureOptionAvailability(btnExposure3, arrowExposure3, 3, supportedRange);
        setExposureOptionAvailability(btnExposure4, arrowExposure4, 4, supportedRange);
    }

    private void setExposureOptionAvailability(TextView optionView, TextView arrowView,
                                               int compensationIndex,
                                               Range<Integer> supportedRange) {
        boolean isAvailable = supportedRange.contains(compensationIndex);
        optionView.setVisibility(isAvailable ? VISIBLE : GONE);
        if (!isAvailable) {
            arrowView.setVisibility(GONE);
        }
    }

    private void initializeImageCaptureWithRatio(int aspectRatio) {
        imageCapture = new ImageCapture.Builder().setTargetAspectRatio(aspectRatio).setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
    }

    @SuppressLint("RestrictedApi")
    private void initializeVideoCaptureWithRatio(int aspectRatio) {
        int[] videoSizes = SharedPrefsSettings.getVideoSizes(getBaseContext());
        videoCapture = createVideoCapture(chooseVideoQuality(videoSizes), aspectRatio);
    }

    private VideoCapture<Recorder> createVideoCapture(Quality quality, int aspectRatio) {
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality, FallbackStrategy.lowerQualityOrHigherThan(quality)))
                .setAspectRatio(aspectRatio)
                .build();
        return VideoCapture.withOutput(recorder);
    }

    private Quality chooseVideoQuality(int[] videoSizes) {
        int width = Math.max(videoSizes[0], videoSizes[1]);
        if (width >= 3840) {
            return Quality.UHD;
        } else if (width >= 1920) {
            return Quality.FHD;
        } else if (width >= 1280) {
            return Quality.HD;
        } else {
            return Quality.SD;
        }
    }

    private void updateCameraPreviewSizeFull() {
        if (cameraContainer != null && cameraPreview != null) {
            positionCameraContainer(false);
            // Make the container full height
            ViewGroup.LayoutParams containerParams = cameraContainer.getLayoutParams();
            containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            cameraContainer.setLayoutParams(containerParams);

            // Make preview fill container
            ViewGroup.LayoutParams previewParams = cameraPreview.getLayoutParams();
            previewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            previewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            cameraPreview.setLayoutParams(previewParams);
        }
    }


    private void updateCameraPreviewFor16_9() {
        if (cameraContainer != null && cameraPreview != null) {
            positionCameraContainer(false);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int height16_9 = (screenWidth * 16) / 9;

            // Update container height
            ViewGroup.LayoutParams containerParams = cameraContainer.getLayoutParams();
            containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParams.height = height16_9;
            cameraContainer.setLayoutParams(containerParams);

            // Update preview height to match container
            ViewGroup.LayoutParams previewParams = cameraPreview.getLayoutParams();
            previewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            previewParams.height = height16_9;
            cameraPreview.setLayoutParams(previewParams);

            centerPreviewInContainer(height16_9);
        }
    }


    private void updateCameraPreviewFor4_3() {
        if (cameraContainer != null && cameraPreview != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int height4_3 = (screenWidth * 4) / 3;

            // Update container height
            ViewGroup.LayoutParams containerParams = cameraContainer.getLayoutParams();
            containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParams.height = height4_3;
            cameraContainer.setLayoutParams(containerParams);
            positionCameraContainer(true);

            // Update preview height to match container
            ViewGroup.LayoutParams previewParams = cameraPreview.getLayoutParams();
            previewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            previewParams.height = height4_3;
            cameraPreview.setLayoutParams(previewParams);

            centerPreviewInContainer(height4_3);
        }
    }

    private void positionCameraContainer(boolean useFourThreeBias) {
        ViewGroup.LayoutParams rawParams = cameraContainer.getLayoutParams();
        if (!(rawParams instanceof RelativeLayout.LayoutParams)) {
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rawParams;
        params.removeRule(RelativeLayout.CENTER_IN_PARENT);
        params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
        params.removeRule(RelativeLayout.ABOVE);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraContainer.setLayoutParams(params);

        cameraContainer.setTranslationY(0f);
        if (useFourThreeBias) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int frameHeight = cameraContainer.getLayoutParams().height;
            int unusedVerticalSpace = Math.max(0, displayMetrics.heightPixels - frameHeight);
            cameraContainer.setTranslationY(-unusedVerticalSpace * 0.20f);
        }
    }

    private void centerPreviewInContainer(int previewHeight) {
        if (cameraPreview != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;

            // If preview is smaller than screen, center it
            if (previewHeight < screenHeight) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cameraPreview.getLayoutParams();
                params.gravity = Gravity.CENTER;
                cameraPreview.setLayoutParams(params);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ========================== LOCATION METHODS ==========================

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocationUpdate(Location location) {
        // Do not replace a usable fix with a poor update while driving.
        if (isLocationFetched && location.hasAccuracy() && location.getAccuracy() > 100f) {
            return;
        }

        isLocationFetched = true;

        // Keep the stamp in sync with the latest location while the camera is active.
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Log.d("Location", "Location captured: " + currentLatitude + ", " + currentLongitude);

        LoadDMS();
        renderStamp();

        long now = System.currentTimeMillis();
        boolean movedEnough = lastGeocodedLocation == null
                || location.distanceTo(lastGeocodedLocation) >= ADDRESS_LOOKUP_DISTANCE_METERS;
        boolean waitedEnough = now - lastAddressLookupTime >= ADDRESS_LOOKUP_INTERVAL_MS;
        if (movedEnough || waitedEnough) {
            lastGeocodedLocation = new Location(location);
            lastAddressLookupTime = now;
            resolveAddressFromLocation(location);
        }
    }

    private void LoadDMS() {
        // Latitude
        String latDirection = (currentLatitude >= 0) ? "N" : "S";
        latDMS = convertToDMS(Math.abs(currentLatitude)) + " " + latDirection;

        // Longitude
        String lonDirection = (currentLongitude >= 0) ? "E" : "W";
        lonDMS = convertToDMS(Math.abs(currentLongitude)) + " " + lonDirection;

        Log.d("PhotoCapture", "DMS " + lonDMS + " " + latDMS);
    }

    private void resolveAddressFromLocation(Location location) {
        final int requestGeneration = ++addressLookupGeneration;
        new Thread(() -> {
            String resolvedAddress;
            String resolvedDefaultTitle;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    resolvedAddress = address.getAddressLine(0);
                    resolvedDefaultTitle = LocationAddressFormatter.buildDefaultTitle(address);
                    if (resolvedAddress == null || resolvedAddress.isEmpty()) {
                        resolvedAddress = "Address not available";
                    }
                } else {
                    resolvedAddress = "Address not available";
                    resolvedDefaultTitle = "";
                }
            } catch (IOException e) {
                e.printStackTrace();
                resolvedAddress = "Address resolution failed";
                resolvedDefaultTitle = "";
            }

            final String addressResult = resolvedAddress;
            final String defaultTitleResult = resolvedDefaultTitle;
            runOnUiThread(() -> {
                if (requestGeneration != addressLookupGeneration || !isLiveLocationMode) {
                    return;
                }
                currentAddress = addressResult;
                currentDefaultTitle = defaultTitleResult;
                Log.d("Location", "Address resolved: " + currentAddress);
                updateStampLocation();
            });
        }).start();
    }

    private void applySavedDefaultTitle(MyLocation location) {
        String storedDefaultTitle = location.getDefaultTitle();
        if (storedDefaultTitle != null && !storedDefaultTitle.trim().isEmpty()) {
            currentDefaultTitle = storedDefaultTitle;
            return;
        }

        currentDefaultTitle = "";
        String selectedTitle = location.getTitle();
        double selectedLatitude;
        double selectedLongitude;
        try {
            selectedLatitude = Double.parseDouble(location.getLatitude());
            selectedLongitude = Double.parseDouble(location.getLongitude());
        } catch (NumberFormatException exception) {
            return;
        }

        new Thread(() -> {
            String resolvedDefaultTitle = "";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        selectedLatitude, selectedLongitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    resolvedDefaultTitle = LocationAddressFormatter.buildDefaultTitle(addresses.get(0));
                }
            } catch (IOException exception) {
                Log.e("Location", "Failed to resolve saved location title", exception);
            }

            String titleResult = resolvedDefaultTitle;
            runOnUiThread(() -> {
                if (isLiveLocationMode || selectedTitle == null
                        || !selectedTitle.equals(currentTitle)) {
                    return;
                }
                currentDefaultTitle = titleResult;
                updateStampLocation();

                if (!titleResult.isEmpty() && location.getId() != null) {
                    location.setDefaultTitle(titleResult);
                    viewModel.updateLocation(location);
                }
            });
        }).start();
    }

    private void retryAddressResolutionIfPossible() {
        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            return;
        }

        if (currentAddress != null
                && !currentAddress.equals("Loading location...")
                && !currentAddress.equals("Address not available")
                && !currentAddress.equals("Address resolution failed")) {
            return;
        }

        currentAddress = "Loading location...";
        updateStampLocation();
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(currentLatitude);
        location.setLongitude(currentLongitude);
        resolveAddressFromLocation(location);
    }

    private String convertToDMS(double decimalCoord) {
        int degrees = (int) decimalCoord;

        double minutesFull = (decimalCoord - degrees) * 60;
        int minutes = (int) minutesFull;

        double seconds = (minutesFull - minutes) * 60;

        return String.format(Locale.getDefault(), "%d°%d'%s\"", degrees, minutes, formatSeconds(seconds));
    }

    private String formatSeconds(double seconds) {
        // Remove unnecessary trailing zeros
        if (seconds == (long) seconds)
            return String.format(Locale.getDefault(), "%d", (long) seconds);
        else return String.format(Locale.getDefault(), "%.2f", seconds);
    }
    // ========================== STAMP RENDERING METHODS ==========================


    private void getStampFont() {
        if (FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf") != null) {
            fontStyle = FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf");
        } else {
            fontStyle = "SF Pro Display.otf";
        }
    }

    private void getStampBgColor() {
        current_StampBgColor = FastSave.getInstance().getInt(MyApplication.STAMP_BG_COLOR, ContextCompat.getColor(this, R.color.transparent_30) // default
        );
    }

    private void getTextColor() {
        current_TextColor = FastSave.getInstance().getInt(MyApplication.STAMP_TEXT_COLOR, ContextCompat.getColor(this, R.color.white) // default
        );
    }

    private void getDateTimeColor() {
        current_DateTimeColor = FastSave.getInstance().getInt(MyApplication.STAMP_DATE_TIME_COLOR, ContextCompat.getColor(this, R.color.white) // default
        );

        // Debug log (both int & hex)
        Log.d("Rishi_Color", "Loaded DateTimeColor int: " + current_DateTimeColor + " | hex: #" + Integer.toHexString(current_DateTimeColor));
    }


    private void getStampType() {
        int previousStampType = currentstamp_type;
        currentstamp_type = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);
        if (previousStampType != 0 && previousStampType != currentstamp_type) {
            clearSessionStampOverrides();
        }
    }

    private void getTemplateStampDateTime() {

        // Get global defaults first
        String globalDateFormat = FastSave.getInstance().getString(MyApplication.FORMAT_DATE, "dd-MM-yyyy");
        String globalTimeFormat = FastSave.getInstance().getString(MyApplication.FORMAT_TIME, "HH:mm:ss a");
        String globalCombinedFormat = FastSave.getInstance().getString(MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");

        // Get template-specific formats (fallback to global if not set)
        format_Date = msp.getTemplateDateFormat(this, currentstamp_type, globalDateFormat);
        format_Time = msp.getTemplateTimeFormat(this, currentstamp_type, globalTimeFormat);
        format_Combined = msp.getTemplateDateTimeCombinedFormat(this, currentstamp_type, globalCombinedFormat);

        // Have a repository instance here (or pass one in)
        DateFormatRepository repo = new DateFormatRepository(this);
        repo.saveSelectedFormat(format_Combined, format_Date, format_Time);


        Log.d("Rishi_datetime", "=== Template DateTime Format Debug ===");
        Log.d("Rishi_datetime", "Template ID: " + currentstamp_type);
        Log.d("Rishi_datetime", "format_Combined: " + format_Combined);
        Log.d("Rishi_datetime", "format_Date: " + format_Date);
        Log.d("Rishi_datetime", "format_Time: " + format_Time);
    }

    private void getStampDateTime() {
        // Check if we should use template-specific formats
        boolean useTemplateFormats = msp.isTemplateEdited(this, currentstamp_type);
        Log.d("Rishi_datetime", "T/F = " + useTemplateFormats);
        if (useTemplateFormats) {
            // Use template-specific formats
            getTemplateStampDateTime();
        } else {
            StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(this, currentstamp_type);
            format_Combined = defaults.combinedFormat;
            format_Date = defaults.dateFormat;
            format_Time = defaults.timeFormat;

            Log.d("Rishi_datetime", "=== Global DateTime Format Debug ===");
            Log.d("Rishi_datetime", "format_Combined: " + format_Combined);
            Log.d("Rishi_datetime", "format_Date: " + format_Date);
            Log.d("Rishi_datetime", "format_Time: " + format_Time);
        }
    }


//    private void renderStamp() {
//        try {
//
//
//            // Only recreate stamp layout if stamp type actually changed
//            if (isMapSetup && mapViewContainer != null && supportMapFragment != null) {
//                // Just update the existing map and content
//                updateStampContent();
//                updateMapLocation(); // This will update map with current location
//                return;
//            }
//
//            relBottomStamp.removeAllViews();
//
//            int layoutResId;
//            switch (currentstamp_type) {
//                case 1:
//                    layoutResId = R.layout.stamp_layout_1;
//                    break;
//                case 2:
//                    layoutResId = R.layout.stamp_layout_2;
//                    break;
//                case 3:
//                    layoutResId = R.layout.stamp_layout_3;
//                    break;
//                case 4:
//                    layoutResId = R.layout.stamp_layout_4;
//                    break;
//                case 5:
//                    layoutResId = R.layout.stamp_layout_5;
//                    break;
//                case 6:
//                    layoutResId = R.layout.stamp_layout_6;
//                    break;
//                case 7:
//                    layoutResId = R.layout.stamp_layout_7;
//                    break;
//                default:
//                    layoutResId = R.layout.stamp_layout_1;
//                    break;
//            }
//
//
//            Log.d("Rishi_Savedstamptemplate", "STAMP_LAYOUT_ID - RENDER" + currentstamp_type);
//
//            View stampView = getLayoutInflater().inflate(layoutResId, relBottomStamp, false);
//
//
//            // Add the view to parent first
//            relBottomStamp.addView(stampView);
//
//            // Initialize views
//            initializeStampViews(stampView);
//
//            if (mapViewContainer != null) {
//                if (mapViewContainer.getId() == View.NO_ID) {
//                    mapViewContainer.setId(View.generateViewId());
//                }
//                setupMapFragment();
//            }
//
//            updateStampContent();
//            isMapSetup = true;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error setting up stamp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    private void renderStamp() {
        try {

            relBottomStamp.removeAllViews();
            int layoutResId;
            switch (currentstamp_type) {
                case 1:
                    layoutResId = R.layout.stamp_layout_1;
                    break;
                case 2:
                    layoutResId = R.layout.stamp_layout_2;
                    break;
                case 3:
                    layoutResId = R.layout.stamp_layout_3;
                    break;
                case 4:
                    layoutResId = R.layout.stamp_layout_4;
                    break;
                case 5:
                    layoutResId = R.layout.stamp_layout_5;
                    break;
                case 6:
                    layoutResId = R.layout.stamp_layout_6;
                    break;
                case 7:
                    layoutResId = R.layout.stamp_layout_7;
                    break;
                case 8:
                    layoutResId = R.layout.stamp_layout_8;
                    break;
                case 9:
                    layoutResId = R.layout.stamp_layout_9;
                    break;
                default:
                    layoutResId = R.layout.stamp_layout_1;
                    break;
            }


            Log.d("Rishi_Savedstamptemplate", "STAMP_LAYOUT_ID - RENDER" + currentstamp_type);

            View stampView = getLayoutInflater().inflate(layoutResId, relBottomStamp, false);


            // Add the view to parent first
            relBottomStamp.addView(stampView);

            // Initialize views
            initializeStampViews(stampView);

            // Setup map fragment with proper timing
            if (mapViewContainer != null) {
                final LinearLayout targetMapContainer = mapViewContainer;

                // Ensure the container has a stable ID
                if (targetMapContainer.getId() == View.NO_ID) {
                    targetMapContainer.setId(View.generateViewId());
                }

                // Use ViewTreeObserver to ensure layout is complete
                targetMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver observer = targetMapContainer.getViewTreeObserver();
                        if (observer.isAlive()) {
                            observer.removeOnGlobalLayoutListener(this);
                        }

                        // Returning from TemplateActivity can render a second template before this
                        // callback runs. Never operate on an old, detached map container.
                        if (targetMapContainer != mapViewContainer
                                || !targetMapContainer.isAttachedToWindow()) {
                            return;
                        }
                        setupMapFragment(targetMapContainer);
                    }
                });
            }

            // Update other stamp content
            updateStampContent();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up stamp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeStampViews(View stampView) {
        compactLiveStampText(stampView);
        mapViewContainer = stampView.findViewById(R.id.mapView);
        txtLocation = stampView.findViewById(R.id.txt_gps_stamp_location);
        txtDefaultTitle = stampView.findViewById(R.id.txt_gps_stamp_default_title);
        txtDateTime = stampView.findViewById(R.id.txt_gps_stamp_datetime);
        txtLatitude = stampView.findViewById(R.id.txt_latitude);
        txtLongitude = stampView.findViewById(R.id.txt_longitude);
        txtTitle = stampView.findViewById(R.id.txt_gps_stamp_title);
        txtDate = stampView.findViewById(R.id.txt_date);
        txtTime = stampView.findViewById(R.id.txt_time);
        lbl_lat = stampView.findViewById(R.id.lbl_lat);
        lbl_long = stampView.findViewById(R.id.lbl_long);
        lbl_date = stampView.findViewById(R.id.lbl_date);
        lbl_gmt = stampView.findViewById(R.id.lbl_gmt);
        lbl_type = stampView.findViewById(R.id.lbl_type);
        lbl_degree = stampView.findViewById(R.id.lbl_degree);
        lbl_dms = stampView.findViewById(R.id.lbl_dms);
        txt_lat_dms = stampView.findViewById(R.id.txt_lat_dms);
        txt_long_dms = stampView.findViewById(R.id.txt_long_dms);
        appStamp = stampView.findViewById(R.id.appStamp);
        stampBg = stampView.findViewById(R.id.rel_gps_stamp);

        latLongContainer = stampView.findViewById(R.id.latLongContainer);
        dateTimeContainer = stampView.findViewById(R.id.dateTimeContainer);
        defaultTitleAddressContainer = stampView.findViewById(R.id.defaultTitleAddressContainer);
    }

    /**
     * The camera overlay is measured against the tall camera screen, which makes the default
     * top/bottom font metrics accumulate across every row. The captured-photo renderer is
     * measured against the media frame and is visibly tighter. Videos reuse this live view, so
     * normalize every text line here before the view is measured and exported.
     */
    private void compactLiveStampText(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setIncludeFontPadding(false);
            textView.setMinHeight(0);
            textView.setMinimumHeight(0);
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                compactLiveStampText(group.getChildAt(index));
            }
        }
    }

    private void setupMapFragment(LinearLayout targetMapContainer) {
        // Add safety checks
        if (targetMapContainer == null || targetMapContainer != mapViewContainer) {
            return;
        }

        // Ensure the view has a stable ID
        if (targetMapContainer.getId() == View.NO_ID) {
            targetMapContainer.setId(View.generateViewId());
        }

        // Wait for the view to be laid out properly
        targetMapContainer.post(() -> {
            if (isFinishing() || isDestroyed()
                    || targetMapContainer != mapViewContainer
                    || !targetMapContainer.isAttachedToWindow()) {
                return; // Don't proceed if activity is finishing
            }

            try {
                // Remove existing fragment if any
                if (supportMapFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(supportMapFragment).commitNowAllowingStateLoss();
                    supportMapFragment = null;
                }

                // Create new fragment and add it
                supportMapFragment = SupportMapFragment.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(targetMapContainer.getId(), supportMapFragment);
                transaction.commitAllowingStateLoss();

                // Setup map callback
                supportMapFragment.getMapAsync(this);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error setting up map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        current_map_type = getActiveMapTypeForSession();
        googleMap.setMapType(current_map_type);
        updateMapLocation();
    }

    private void updateStampContent() {

        if (!hasSessionStampOverride && msp.isTemplateEdited(this, currentstamp_type)) {
            //Log.d("Edit_Activity_Rishi","?"+ msp.isTemplateEdited(this, currentstamp_type));
            // Template has been customized - use saved template font
            String defaultFont = FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf");
            fontStyle = msp.getTemplateFontStyle(this, currentstamp_type, defaultFont);
//            msp.setInteger(getApplicationContext(), SP.LOCATION_FONT_POSITION, fontStyle);
            FastSave.getInstance().saveString(MyApplication.FONT_STYLE, fontStyle);

//           // Log.d("Edit_Activity_Rishi", "Using template " + currentstamp_type +
//                    " saved font: " + fontStyle);

            // Take color from template
            int whiteColor = ContextCompat.getColor(this, R.color.white);
            int transparent30 = ContextCompat.getColor(this, R.color.transparent_30);

            current_StampBgColor = msp.getTemplateBgColor(this, currentstamp_type, transparent30);
            current_TextColor = msp.getTemplateTextColor(this, currentstamp_type, whiteColor);
            current_DateTimeColor = msp.getTemplateDateTimeColor(this, currentstamp_type, whiteColor);
            current_map_type = resolveTemplateMapType();


            Log.d("Rishi_Color", "Saving BgColor: #" + Integer.toHexString(current_StampBgColor));
            Log.d("Rishi_Color", "Saving TextColor: #" + Integer.toHexString(current_TextColor));
            Log.d("Rishi_Color", "Saving DateTimeColor: #" + Integer.toHexString(current_DateTimeColor));

            //Saving color from template
            FastSave.getInstance().saveInt(MyApplication.STAMP_BG_COLOR, current_StampBgColor);
            FastSave.getInstance().saveInt(MyApplication.STAMP_TEXT_COLOR, current_TextColor);
            FastSave.getInstance().saveInt(MyApplication.STAMP_DATE_TIME_COLOR, current_DateTimeColor);
        } else if (!hasSessionStampOverride) {
            StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(this, currentstamp_type);
            fontStyle = defaults.fontStyle;
            current_StampBgColor = defaults.bgColor;
            current_TextColor = defaults.textColor;
            current_DateTimeColor = defaults.dateTimeColor;
            current_map_type = defaults.mapType;
        }

        // Watermark is an app-wide preference and must remain independent of
        // temporary font, date/time, and map overrides for the current session.
        showWatermark = MyApplication.getShowWatermark();

        // A temporary map choice is owned by the current app session, not by
        // the selected template. Always resolve it after a stamp refresh.
        current_map_type = getActiveMapTypeForSession();


        //msp.setTemplateEdited(this, currentstamp_type, false);
        //Log.d("Edit_Activity_Rishi","?"+ msp.isTemplateEdited(this, currentstamp_type));
        //fontStyle = FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf");

        updateStampLocation();
        updateStampBgColor();
        updateStampCoordinates();
        updateStampDateTime();
        updateMapLocation();
        updateStampTitle();
        updateWaterMarkVisibility();
        applyPremiumPreviewScreenshotProtection();
        if (currentstamp_type == 2 || currentstamp_type == 3 || currentstamp_type == 6 || currentstamp_type == 7) {
            updateStampDMS();
        }
    }

    private void updateMapLocation() {
        if (googleMap != null && currentLatitude != 0.0 && currentLongitude != 0.0) {
            LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

            // Disable map interactions to prevent conflicts
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }


    private void updateStampBgColor() {
        if (currentstamp_type == 9 ) {
            if (current_StampBgColor == ContextCompat.getColor(this, R.color.transparent_30)) {
                current_StampBgColor = ContextCompat.getColor(this, R.color.bg_glass);
            }

            StampBackgroundUtils.applyRoundedGlassColor(
                    this,
                    current_StampBgColor,
                    dateTimeContainer,
                    latLongContainer,
                    defaultTitleAddressContainer
            );
            StampBackgroundUtils.applyRoundedDefaultGlass(this, appStamp);
        }else {
            stampBg.setCardBackgroundColor(current_StampBgColor);
        }
    }

    private void updateWaterMarkVisibility() {
        if (showWatermark) {
            appStamp.setVisibility(View.VISIBLE);
        } else if (currentstamp_type == 9) {
            appStamp.setVisibility(GONE);
        } else {
            appStamp.setVisibility(View.INVISIBLE);
        }
    }

    private void updateStampTitle() {
        if (currentTitle != null) {
            txtTitle.setVisibility(VISIBLE);
            txtTitle.setText(currentTitle);
            txtTitle.setTextColor(current_TextColor);
            txtTitle.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }
    }

    private void updateStampLocation() {
        if (txtDefaultTitle != null) {
            txtDefaultTitle.setText(currentDefaultTitle);
            txtDefaultTitle.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txtDefaultTitle.setTextColor(current_TextColor);
            txtDefaultTitle.setVisibility(VISIBLE);
        }
        if (txtLocation != null) {
            txtLocation.setText(currentAddress);
            txtLocation.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txtLocation.setTextColor(current_TextColor);
        }
    }

    private void updateStampCoordinates() {
        if (txtLatitude != null && txtLongitude != null) {

            // Determine N/S for latitude
            String latDirection = (currentLatitude >= 0) ? "N" : "S";

            // Determine E/W for longitude
            String lonDirection = (currentLongitude >= 0) ? "E" : "W";

            // Use absolute value to avoid negative sign with letters
            //txtLatitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLatitude), latDirection));
            txtLatitude.setText(String.format(Locale.getDefault(), "%.5f°%s", currentLatitude, latDirection));
            txtLatitude.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            lbl_lat.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            //txtLongitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLongitude), lonDirection));
            txtLongitude.setText(String.format(Locale.getDefault(), "%.5f°%s", currentLongitude, lonDirection));
            txtLongitude.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            lbl_long.setTypeface(mHelperClass.getFontStyle(this, fontStyle));

            txtLatitude.setTextColor(current_TextColor);
            txtLongitude.setTextColor(current_TextColor);
            lbl_lat.setTextColor(current_TextColor);
            lbl_long.setTextColor(current_TextColor);

            //For stamp 2,3,6:
            if (currentstamp_type == 2 || currentstamp_type == 3 || currentstamp_type == 6 || currentstamp_type == 7) {
                lbl_type.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
                lbl_degree.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
                lbl_type.setTextColor(current_TextColor);
                lbl_degree.setTextColor(current_TextColor);
            }
        }
    }

    private void updateStampDMS() {
        if (txt_lat_dms != null && txt_long_dms != null) {
            // Set text
            txt_lat_dms.setText(latDMS);
            txt_lat_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txt_long_dms.setText(lonDMS);
            txt_long_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            lbl_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));

            //Set Color
            txt_lat_dms.setTextColor(current_TextColor);
            txt_long_dms.setTextColor(current_TextColor);
            lbl_dms.setTextColor(current_TextColor);
        }
    }


//    private void updateStampDateTime() {
//        if (txtDate != null && txtTime != null) {
//            Date now = new Date();
//
//            SimpleDateFormat dateFormatter = new SimpleDateFormat(format_Date, Locale.getDefault());
//            SimpleDateFormat timeFormatter = new SimpleDateFormat(format_Time, Locale.getDefault());
//
//            txtDate.setText(dateFormatter.format(now));
//            txtDate.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
//            lbl_date.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
//
//            if (format_Time == null || format_Time.isEmpty()) {
//                lbl_gmt.setVisibility(View.GONE);
//                txtTime.setVisibility(GONE);
//            } else {
//                lbl_gmt.setVisibility(VISIBLE);
//                txtTime.setVisibility(VISIBLE);
//                txtTime.setText(timeFormatter.format(now));
//                txtTime.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
//                lbl_gmt.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
//            }
//        }
//
//        // Legacy datetime field for backward compatibility
//        if (txtDateTime != null) {
//            SimpleDateFormat fullFormat = new SimpleDateFormat("dd MMM YY, EEEE HH:mm:ss", Locale.getDefault());
//            txtDateTime.setText(fullFormat.format(new Date()));
//        }
//    }


    private void updateStampDateTime() {
        if (txtDate == null && txtTime == null && lbl_date == null && lbl_gmt == null) {
            return;
        }

        String displayDate;
        String displayTime;

        // ----- DATE -----
        if (savedDate != null && !savedDate.trim().isEmpty()) {
            try {
                SimpleDateFormat originalDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date parsedDate = originalDateFormat.parse(savedDate);
                SimpleDateFormat displayDateFormat = new SimpleDateFormat(format_Date, Locale.getDefault());
                displayDate = parsedDate != null ? displayDateFormat.format(parsedDate) : savedDate;
            } catch (Exception e) {
                displayDate = savedDate; // fallback if parsing fails
            }
        } else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format_Date, Locale.getDefault());
            displayDate = dateFormatter.format(new Date());
        }

        // ----- TIME -----
        if (savedTime != null && !savedTime.trim().isEmpty()) {
            try {
                SimpleDateFormat originalTimeFormat = new SimpleDateFormat("HH:mm:ss aa", Locale.getDefault());
                Date parsedTime = originalTimeFormat.parse(savedTime);
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat(format_Time, Locale.getDefault());
                displayTime = parsedTime != null ? displayTimeFormat.format(parsedTime) : savedTime;
            } catch (Exception e) {
                displayTime = savedTime; // fallback if parsing fails
            }
        } else {
            SimpleDateFormat timeFormatter = new SimpleDateFormat(format_Time, Locale.getDefault());
            displayTime = timeFormatter.format(new Date());
        }

        // Set date
        if (txtDate != null) {
            txtDate.setText(displayDate);
            txtDate.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }
        if (lbl_date != null) {
            lbl_date.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }

        // Set time
        if (txtTime != null) {
            if (format_Time == null || format_Time.isEmpty()) {
                txtTime.setVisibility(GONE);
            } else {
                txtTime.setVisibility(VISIBLE);
                txtTime.setText(displayTime);
                txtTime.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            }
        }

        if (lbl_gmt != null) {
            if (format_Time == null || format_Time.isEmpty()) {
                lbl_gmt.setVisibility(View.GONE);
            } else {
                lbl_gmt.setVisibility(VISIBLE);
                lbl_gmt.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            }
        }

        // Apply colors
        if (txtTime != null) {
            txtTime.setTextColor(current_DateTimeColor);
        }
        if (txtDate != null) {
            txtDate.setTextColor(current_DateTimeColor);
        }
        if (lbl_date != null) {
            lbl_date.setTextColor(current_DateTimeColor);
        }
        if (lbl_gmt != null) {
            lbl_gmt.setTextColor(current_DateTimeColor);
        }
        Log.d("Rishi_Color", "DateTimeColor (int): " + current_DateTimeColor);
        Log.d("Rishi_Color", "DateTimeColor (hex): #" + Integer.toHexString(current_DateTimeColor));


        // Legacy combined field
        if (txtDateTime != null) {
            if (savedDate != null && savedTime != null) {
                txtDateTime.setText(savedDate + " " + savedTime);
            } else {
                SimpleDateFormat fullFormat = new SimpleDateFormat("dd MMM YY, EEEE HH:mm:ss", Locale.getDefault());
                txtDateTime.setText(fullFormat.format(new Date()));
            }
        }
    }

    // ========================== EVENT LISTENERS ==========================

    private void setupExposureListeners() {
        btnExposure_4.setOnClickListener(view -> {
            setExposureCompensation(-4);
        });
        btnExposure_3.setOnClickListener(view -> setExposureCompensation(-3));
        btnExposure_2.setOnClickListener(view -> setExposureCompensation(-2));
        btnExposure_1.setOnClickListener(view -> setExposureCompensation(-1));
        btnExposure0.setOnClickListener(view -> setExposureCompensation(0));
        btnExposure1.setOnClickListener(view -> setExposureCompensation(1));
        btnExposure2.setOnClickListener(view -> setExposureCompensation(2));
        btnExposure3.setOnClickListener(view -> setExposureCompensation(3));
        btnExposure4.setOnClickListener(view -> setExposureCompensation(4));
    }

    ;


    private void setupZoomListeners() {
        btnZoom1x.setOnClickListener(v -> {
            UtilsX.animateZoomButton(btnZoom1x);
            setZoomRatio(1f);
        });
        relBottomStamp.setOnClickListener(view -> {
            Log.d("bottom_stamp_rishi", "StampClicked");
            myLocationNavigation();
        });

        btnZoom2x.setOnClickListener(v -> {
            UtilsX.animateZoomButton(btnZoom2x);
            Log.d("bottom_stamp_rishi", "2x clicked");
            setZoomRatio(2f);
        });

        btnZoom3x.setOnClickListener(v -> {
            UtilsX.animateZoomButton(btnZoom3x);
            setZoomRatio(3f);
        });
    }

    private void setupCameraListeners() {

        btnFlash.setOnClickListener(v -> toggleFlash());
        btnSwitchCamera.setOnClickListener(v -> {
            switchCamera();
            UtilsX.rotateSwitchCameraButton(btnSwitchCamera);
        });
    }

    private void setupTabListeners() {
        viewPagerSwitchAction.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabSelection(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupMenuListeners() {
        btnExpandMenu.setOnClickListener(view -> toggleHorizontalMenu());

        btnSettings.setOnClickListener(view -> {
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "settings_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        settingsNavigation();
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        LogUtils.logE("Settings", "Ad failed: " + errorMsg);
                    });
                } else {
                    LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                settingsNavigation();
            }
        });
    }

    private void settingsNavigation() {
        UtilsX.rotateSettingsButton(btnSettings);
        String currentDate = "";
        String currentTime = "";

        if (format_Date != null && format_Time != null) {
            Date now = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format_Date, Locale.getDefault());
            SimpleDateFormat timeFormatter = new SimpleDateFormat(format_Time, Locale.getDefault());
            currentDate = dateFormatter.format(now);
            currentTime = timeFormatter.format(now);
        }

        MyLocation location = new MyLocation(null, currentTitle, currentDate, currentTime,
                currentAddress, String.valueOf(currentLatitude), String.valueOf(currentLongitude),
                false, currentDefaultTitle);
        Intent intent = new Intent(MainActivity.this, Settings_Activity.class);
        intent.putExtra(MyApplication.EXTRA_LOCATION, location);
        if (activeSavedLocationId != null) {
            intent.putExtra(MyLocation_Activity.EXTRA_ACTIVE_SAVED_LOCATION_ID,
                    activeSavedLocationId);
        }
        intent.putExtra(Settings_Activity.EXTRA_CURRENT_FONT_STYLE, fontStyle);
        intent.putExtra(Settings_Activity.EXTRA_CURRENT_DATE_FORMAT, format_Date);
        intent.putExtra(Settings_Activity.EXTRA_CURRENT_TIME_FORMAT, format_Time);
        intent.putExtra(Settings_Activity.EXTRA_CURRENT_COMBINED_FORMAT, format_Combined);
        intent.putExtra(Settings_Activity.EXTRA_CURRENT_MAP_TYPE, getActiveMapTypeForSession());
       // startActivity(intent);
        resultLauncher.launch(intent);
    }

    private void setUpBottomTabListeners() {
        btnMap.setOnClickListener(view -> {
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "map_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        Intent intent = new Intent(MainActivity.this, Map_Activity.class);
                        intent.putExtra(Map_Activity.EXTRA_SELECTED_MAP_TYPE, getActiveMapTypeForSession());
                        mapTypeResultLauncher.launch(intent);

                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        LogUtils.logD("MapActivity", "Ad failed: " + errorMsg);
                    });
                } else {
                    LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                Intent intent = new Intent(MainActivity.this, Map_Activity.class);
                intent.putExtra(Map_Activity.EXTRA_SELECTED_MAP_TYPE, getActiveMapTypeForSession());
                mapTypeResultLauncher.launch(intent);
            }
        });

        btnCollection.setOnClickListener(view -> {
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "collection_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        Intent intent = new Intent(MainActivity.this, MyCreation_Activity.class);
                        startActivity(intent);
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        LogUtils.logE("MyCreation", "Ad failed: " + errorMsg);
                    });
                } else {
                    LogUtils.logD("MyCreation", "Interstitial already showing, ignoring click");
                }
            } else {
                Intent intent = new Intent(MainActivity.this, MyCreation_Activity.class);
                startActivity(intent);
            }
        });

        btnTemplate.setOnClickListener(view -> {
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "template_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        Intent intent = new Intent(MainActivity.this, Template_Activity.class);
                        templateResultLauncher.launch(intent);
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        LogUtils.logE("TemplateActivity", "Ad failed: " + errorMsg);
                    });
                } else {
                    LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                Intent intent = new Intent(MainActivity.this, Template_Activity.class);
                templateResultLauncher.launch(intent);
            }
        });


        btnAddLocation.setOnClickListener(view -> {
            // Use current location data instead of photo data
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "location_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        myLocationNavigation();
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        LogUtils.logE("MyLocation", "Ad failed: " + errorMsg);
                    });
                } else {
                    LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                myLocationNavigation();
            }


        });


        ivMyCapture.setOnClickListener(view -> {
            if (isPreviewLoading) {
                return;
            }
            // Check if location is loaded


            if ("Loading location...".equals(currentAddress) || "Loading...".equals(currentAddress)) {
                Toast.makeText(this, getResources().getString(R.string.please_wait_data_is_loading), Toast.LENGTH_SHORT).show();
                return;
            }


            // Use the most recent photo (either just captured or last one from database)
            Photo photoToPreview = null;

            if (isCapture && photo != null && photo.getImagePath() != null) {

                photoToPreview = photo;
            } else if (photoOld != null && photoOld.getImagePath() != null) {

                photoToPreview = photoOld;
            }

            if (photoToPreview != null) {
                Intent intent = new Intent(MainActivity.this, PhotoPreview_Activity.class);
                intent.putExtra("model", photoToPreview);
                intent.putExtra("isMain", true);
                previewResultLauncher.launch(intent);
                // Reset capture flag after preview
                isCapture = false;
            } else {
                Intent intent = new Intent(MainActivity.this, No_Preview_Available.class);
                startActivity(intent);
            }
        });
    }

    private void clearDeletedPreview(int deletedPhotoId, String deletedPhotoPath) {
        if (isSamePhoto(photoOld, deletedPhotoId, deletedPhotoPath)) {
            photoOld = null;
        }

        if (isSamePhoto(photo, deletedPhotoId, deletedPhotoPath)) {
            isCapture = false;
            initializePhotoObject();
        }

        ivMyCapture.setImageResource(R.drawable.my_capture_icon);
    }

    private boolean isSamePhoto(Photo target, int photoId, String photoPath) {
        if (target == null) {
            return false;
        }

        Integer targetId = target.getId();
        if (photoId != -1 && targetId != null && targetId.equals(photoId)) {
            return true;
        }

        String targetPath = target.getImagePath();
        return photoPath != null && targetPath != null && photoPath.equals(targetPath);
    }

    private void myLocationNavigation() {
        String currentDate = "";
        String currentTime = "";

        if (format_Date != null && format_Time != null) {
            Date now = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format_Date, Locale.getDefault());
            SimpleDateFormat timeFormatter = new SimpleDateFormat(format_Time, Locale.getDefault());
            currentDate = dateFormatter.format(now);
            currentTime = timeFormatter.format(now);
        }

        MyLocation location = new MyLocation(null, currentTitle, currentDate, currentTime,
                currentAddress, String.valueOf(currentLatitude), String.valueOf(currentLongitude),
                false, currentDefaultTitle);
        Intent intent = new Intent(MainActivity.this, MyLocation_Activity.class);
        intent.putExtra(MyApplication.EXTRA_LOCATION, location);
        if (activeSavedLocationId != null) {
            intent.putExtra(MyLocation_Activity.EXTRA_ACTIVE_SAVED_LOCATION_ID,
                    activeSavedLocationId);
        }
        resultLauncher.launch(intent);
    }


    private void setUpHorizontalMenuListeners() {


        setupExposureListeners();

        btnFontStyle.setOnClickListener(view -> {
            showFontStyleDialog();
            manageExposurelayout();
        });

        btnDateTime.setOnClickListener(view -> {
            showDateTimeDialog();
            manageExposurelayout();
        });

        btnTimer.setOnClickListener(view -> {
            showTimerDialog();
            manageExposurelayout();
        });

        btnExposure.setOnClickListener(view -> {
            if (isMenuExpanded) {
                MainController.toggleVisibilityView(layoutExposure, btnExposure, getResources());
            }
        });

        btnRatio.setOnClickListener(view -> {
            showRatioDialog();
            manageExposurelayout();
        });

        btnGrid.setOnClickListener(view -> {
            showGridDialog();
            manageExposurelayout();
        });

        btnWatermark.setOnClickListener(view -> {
            showWatermarkDialog();
            manageExposurelayout();
        });

        btnVideoresolution.setOnClickListener(view -> {
            if (viewPagerSwitchAction.getCurrentItem() == 0) {
                Toast.makeText(this, R.string.camera_no_support_resolution, Toast.LENGTH_SHORT).show();
            } else {
                showVideoResolutionDialog();
                manageExposurelayout();
            }
        });

        btnFps.setOnClickListener(view -> {
            if (viewPagerSwitchAction.getCurrentItem() == 0) {
                Toast.makeText(this, R.string.camera_no_support_fps, Toast.LENGTH_SHORT).show();
            } else {
                showFpsDialog();
                manageExposurelayout();
            }
        });

        btnImgQuality.setOnClickListener(view -> {
            showImageQualityDialog();
            manageExposurelayout();
        });

        btnVideoVolume.setOnClickListener(view -> {
            showVideoVolumeDialog();
            manageExposurelayout();
        });
    }

    ;

    private void showFontStyleDialog() {
        if (fontStyleDialog != null && fontStyleDialog.isShowing()) {
            return;
        }

        String[] fontList = fontViewModel.getFontList().getValue();
        if (fontList == null) {
            // Fallback
            fontList = getResources().getStringArray(R.array.font_name_array);
        }
        fontStyleDialog = StampSettingsBottomSheets.showFontStyle(this, fontList, fontStyle, new OnFontSelectedListener() {
            @Override
            public void onFontSelected(String fontName, int position) {
                hasSessionStampOverride = true;
                fontStyle = fontName;
                MyApplication.setSessionFontStyle(fontName);
                updateStampContent();
            }

            @Override
            public void onDialogDismissed() {
                fontStyleDialog = null;
            }
        });
    }


    private void showDateTimeDialog() {

        if (dateTimeDialog != null && dateTimeDialog.isShowing()) {
            return;
        }

        dateTimeDialog = StampSettingsBottomSheets.showDateTime(this, format_Combined, new OnDateTimeSelectedListener() {
            @Override
            public void onDateTimeSelected(DateFormatModel selectedFormat, int position) {
                hasSessionStampOverride = true;
                hasSessionDateTimeOverride = true;
                format_Date = selectedFormat.getFormat_Date();
                format_Time = selectedFormat.getFormat_Time();
                format_Combined = selectedFormat.getFormat_Combined();
                MyApplication.setSessionDateTimeFormats(
                        format_Date, format_Time, format_Combined);
                updateStampDateTime();
                applyPremiumPreviewScreenshotProtection();
            }

            @Override
            public void onDialogDismissed() {
                dateTimeDialog = null;
            }
        });
    }

    private void showTimerDialog() {

        if (timerDialog != null && timerDialog.isShowing()) {
            return;
        }

        timerDialog = new TimerDialog(this, this.timernew, new OnTimerSelectedListener() {
            @Override
            public void onTimerSelected(int timerValue) {
                timernew = timerValue;
            }

            @Override
            public void onDialogDismissed() {
                timerDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(timerDialog);
        timerDialog.show();
    }

    private void showRatioDialog() {
        if (ratioDialog != null && ratioDialog.isShowing()) {
            return;
        }
        boolean isVideoMode = viewPagerSwitchAction != null && viewPagerSwitchAction.getCurrentItem() == 1;
        ratioDialog = new RatioDialog(this, currentRatioType, !isVideoMode, new OnRatioSelectedListener() {
            @Override
            public void onRatioSelected(int ratioValue) {
                if (currentRatioType != ratioValue) {
                    currentRatioType = ratioValue;
                    startCameraPreview();
                    toggleHorizontalMenu();
                }
            }

            @Override
            public void onDialogDismissed() {
                ratioDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(ratioDialog);
        ratioDialog.show();
    }

    private void showGridDialog() {

        if (gridDialog != null && gridDialog.isShowing()) {
            return;
        }

        gridDialog = new GridDialog(this, currentGridType, new OnGridSelectedListener() {
            @Override
            public void onGridSelected(int gridValue) {
                if (currentGridType != gridValue) {
                    currentGridType = gridValue;
                    SharedPrefsSettings.setGridType(gridValue, MainActivity.this);
                    updateGridDisplay();
                    startCameraPreview();
                    toggleHorizontalMenu();
                }
            }

            @Override
            public void onDialogDismissed() {
                gridDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(gridDialog);
        gridDialog.show();
    }

    private void showWatermarkDialog() {
        Dialog watermarkdialog = new Dialog(this);
        watermarkdialog.setContentView(R.layout.dialog_watermark);
        watermarkdialog.setCancelable(true);

        Switch switchCamera = watermarkdialog.findViewById(R.id.switch_watermark);
        ImageView premiumBadge = watermarkdialog.findViewById(R.id.premiumWatermarkBadgeMain);
        boolean premium = PremiumManager.isPremium(this);
        premiumBadge.setVisibility(premium ? GONE : VISIBLE);

        // Load saved value instead of constant
        showWatermark = premium ? MyApplication.getShowWatermark() : true;
        switchCamera.setChecked(showWatermark);

        switchCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!PremiumManager.isPremium(this) && !isChecked) {
                buttonView.setChecked(true);
                watermarkdialog.dismiss();
                showPremiumSheet();
                return;
            }
            hasSessionStampOverride = true;
            showWatermark = isChecked;
            MyApplication.setShowWatermark(isChecked);
            updateStampContent();
        });

        watermarkdialog.setOnDismissListener(dialogInterface -> {
        });


        View anchorView = findViewById(R.id.layoutHorizontalMenu);
        Window window = watermarkdialog.getWindow();
        if (window != null) {
            // Calculate position of the anchor view
            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);
            int anchorBottom = location[1] + anchorView.getHeight();

            // Set watermarkdialog position to TOP with margin below anchor
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.x = 0;
            params.y = anchorBottom; // 20px margin below the anchor
            window.setAttributes(params);

            // Calculate width with some gap (e.g., 32dp from both sides)
            int sideMargin = (int) (12 * getResources().getDisplayMetrics().density);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (sideMargin * 2);

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        watermarkdialog.show();
    }

    private boolean blockCaptureForPremiumPreview() {
        if (PremiumManager.isPremium(this)) {
            return false;
        }

        if (PremiumManager.hasPremiumCaptureConfiguration(
                currentstamp_type,
                fontStyle,
                format_Combined,
                !isLiveLocationMode)) {
            showPremiumSheet();
            return true;
        }
        return false;
    }

    private void showPremiumSheet() {
        PremiumTestBottomSheet.show(this, new PremiumTestBottomSheet.Listener() {
            @Override
            public void onPremiumChanged(boolean premium) {
                if (!premium) {
                    showWatermark = true;
                    MyApplication.setShowWatermark(true);
                }
                refreshPremiumUi();
                refreshMainAdForPremiumState();
                updateWaterMarkVisibility();
            }

            @Override
            public void onContinueFree() {
                restoreDefaultFreeRuntime();
            }
        });
    }

    private void refreshPremiumUi() {
        applyPremiumPreviewScreenshotProtection();
    }

    private void refreshMainAdForPremiumState() {
        FrameLayout banner = findViewById(R.id.flMainBanner);
        if (PremiumManager.isPremium(this)) {
            banner.removeAllViews();
            banner.setVisibility(GONE);
            updateBottomOptionsBannerOffset(banner);
        } else if (banner.getChildCount() == 0) {
            loadBottomBannerAd();
        }
    }

    private void restoreDefaultFreeRuntime() {
        PremiumManager.resetToDefaultTemplate(this);
        clearSessionStampOverrides();
        isLiveLocationMode = true;
        activeSavedLocationId = null;
        addressLookupGeneration++;
        currentTitle = null;
        currentDefaultTitle = "";
        savedDate = null;
        savedTime = null;
        currentAddress = "Loading location...";
        currentLatitude = 0.0;
        currentLongitude = 0.0;
        isLocationFetched = false;
        lastGeocodedLocation = null;
        lastAddressLookupTime = 0L;

        getStampType();
        getStampFont();
        getStampDateTime();
        getStampBgColor();
        getTextColor();
        getDateTimeColor();
        showWatermark = true;
        applyPremiumPreviewScreenshotProtection();
        setupLocation();
        updateMaps();
        renderStamp();
        refreshPremiumUi();
    }

    private String getSafeCaptureAddress() {
        if (currentAddress == null || currentAddress.trim().isEmpty()
                || "Loading location...".equals(currentAddress)
                || "Loading...".equals(currentAddress)) {
            return "Location not available";
        }
        return currentAddress;
    }

    private void applyPremiumPreviewScreenshotProtection() {
        boolean shouldProtect = !PremiumManager.isPremium(this)
                && PremiumManager.hasPremiumCaptureConfiguration(
                currentstamp_type,
                fontStyle,
                format_Combined,
                !isLiveLocationMode);
        if (shouldProtect) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    private void showVideoResolutionDialog() {

        if (videoResolutionDialog != null && videoResolutionDialog.isShowing()) {
            return;
        }

        int currentVideoResolution = SharedPrefsSettings.getVideoSize(this);

        videoResolutionDialog = new VideoResolutionDialog(this, currentVideoResolution, new OnResolutionSelectedListener() {
            @Override
            public void onResolutionSelected(int resolutionValue) {
                SharedPrefsSettings.setVideoSize(resolutionValue, getBaseContext());
                // Reinitialize video capture if recording is not in progress
                if (!isRecording) {
                    startCameraPreview();
                    toggleHorizontalMenu();
                }
            }

            @Override
            public void onDialogDismissed() {
                videoResolutionDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(videoResolutionDialog);
        videoResolutionDialog.show();
    }

    private void showFpsDialog() {

        if (fpsDialog != null && fpsDialog.isShowing()) {
            return;
        }

        int currentFps = SharedPrefsSettings.getFps(this);

        fpsDialog = new FpsDialog(this, currentFps, new OnFpsSelectedListener() {
            @Override
            public void onFpsSelected(int fpsValue) {
                SharedPrefsSettings.setFps(fpsValue, getBaseContext());

                // Reinitialize video capture if recording is not in progress
                if (!isRecording) {
                    initVideoCapture();
                    startCameraPreview();
                    toggleHorizontalMenu();
                }
            }

            @Override
            public void onDialogDismissed() {
                fpsDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(fpsDialog);
        fpsDialog.show();
    }

    private void showImageQualityDialog() {
        Dialog imageQualityDialog = new Dialog(this);
        imageQualityDialog.setContentView(R.layout.dialog_imagequality);
        imageQualityDialog.setCancelable(true);

        Switch switchMaximumImageQuality = imageQualityDialog.findViewById(R.id.switch_maximumimagequality);

        boolean currentQualityState = SharedPrefsSettings.getImageMaxQuality(this);
        switchMaximumImageQuality.setChecked(currentQualityState);

        switchMaximumImageQuality.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefsSettings.setImageMaxQuality(isChecked, this);
            initImageCapture();
            startCameraPreview();
            Log.d("Rishi_Image", "Image quality changed to: " + (isChecked ? "Maximum Quality" : "Minimize Latency"));
        });

        imageQualityDialog.setOnDismissListener(dialogInterface -> {
        });

        View anchorView = findViewById(R.id.layoutHorizontalMenu);
        Window window = imageQualityDialog.getWindow();
        if (window != null) {
            // Calculate position of the anchor view
            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);
            int anchorBottom = location[1] + anchorView.getHeight();

            // Set watermarkdialog position to TOP with margin below anchor
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.x = 0;
            params.y = anchorBottom; // 20px margin below the anchor
            window.setAttributes(params);

            // Calculate width with some gap (e.g., 32dp from both sides)
            int sideMargin = (int) (12 * getResources().getDisplayMetrics().density);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (sideMargin * 2);

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        imageQualityDialog.show();
    }

    private void showVideoVolumeDialog() {
        if (videoVolumeDialog != null && videoVolumeDialog.isShowing()) {
            return;
        }
        boolean currentSoundStatus = SharedPrefsSettings.getSoundStatus(this);

        videoVolumeDialog = new VideoVolumeDialog(this, currentSoundStatus, new OnSoundSelectedListener() {
            @Override
            public void onSoundSelected(boolean soundEnabled) {
                // Save the sound setting to SharedPreferences
                SharedPrefsSettings.setSoundStatus(soundEnabled, getBaseContext());

                if (!isRecording) {
                    startCameraPreview();
                    toggleHorizontalMenu();
                }
            }

            @Override
            public void onDialogDismissed() {
                videoVolumeDialog = null;
            }
        });

        new HelperClass().setFullscreenBottomDialog(videoVolumeDialog);
        videoVolumeDialog.show();
    }

    private void showInternetDialog() {
        if (isFinishing()
                || isDestroyed()
                || isLocationPromptActive
                || (locationFallbackDialog != null && locationFallbackDialog.isShowing())
                || (internetRequiredDialog != null && internetRequiredDialog.isShowing())) {
            return;
        }

        internetRequiredDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet")
                .setMessage("Please turn on Wi-Fi or mobile data to retrieve the address for your location.")
                .setCancelable(false)
                .setPositiveButton("Turn On", (dialog, which) -> openInternetSettings())
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setOnDismissListener(d -> {
                    internetRequiredDialog = null;
                    renderStamp();
                })
                .show();

        int actionColor = ContextCompat.getColor(this, R.color.blue_primary);
        internetRequiredDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actionColor);
        internetRequiredDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actionColor);
    }

    private void openInternetSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
        } else {
            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        }

        try {
            internetSettingsLauncher.launch(intent);
        } catch (Exception exception) {
            internetSettingsLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    private void showInternetLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Location Disabled and no internet")
                .setMessage("Please turn on Location (GPS) and internet to continue.")
                .setCancelable(false)
//                .setPositiveButton("USE MOBILE DATA", (dialog, which) -> {
//                    Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
//                    startActivity(intent);
//                })
//                .setNegativeButton("CONNECT TO WI-FI", (dialog, which) -> {
//                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                    startActivity(intent);
//                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setOnDismissListener(d -> {
                    renderStamp();
                })
                .show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
    }

    private void showLocationDialog() {
        if (isLocationPromptActive
                || (locationFallbackDialog != null && locationFallbackDialog.isShowing())) {
            return;
        }

        isLocationPromptActive = true;
        LocationRequest promptLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000L)
                .setFastestInterval(2000L);
        LocationSettingsPrompt.show(this, promptLocationRequest, enableLocationLauncher, () -> {
            isLocationPromptActive = false;
            showLocationDialogFallback();
        });
    }

    private void showLocationDialogFallback() {
        if (isFinishing()
                || isDestroyed()
                || (locationFallbackDialog != null && locationFallbackDialog.isShowing())) {
            return;
        }

        locationFallbackDialog = new AlertDialog.Builder(this)
                .setTitle("Location Disabled")
                .setMessage("Please turn on Location (GPS) to continue.")
                .setCancelable(false)
//                .setPositiveButton("Turn On", (dialog, which) -> {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(intent);
//                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setOnDismissListener(d -> {
                    locationFallbackDialog = null;
                    renderStamp();
                    showInternetDialogIfNeeded();
                })
                .show();
        locationFallbackDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
    }


    private void updateGridDisplay() {
        if (gridLinesView != null) {
            gridLinesView.setGridType(currentGridType);
        }
    }

    private void startCountDown(int position) {
        txtCountDownTakePhoto.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(timernew * 1000L, 1000L) {
            @Override
            public void onTick(long j) {
                UtilsX.playSound(R.raw.beep_timer_sound, getBaseContext());
                txtCountDownTakePhoto.setText(String.valueOf((j / 1000) + 1));
            }

            @Override
            public void onFinish() {
                if (position == 0) {
                    takePhoto();
                    txtCountDownTakePhoto.setVisibility(View.INVISIBLE);
                    btnTakeAction.setVisibility(View.VISIBLE);
                    imgCenterTakeAction.setVisibility(View.VISIBLE);
                } else {
                    recordVideo();
                    txtCountDownTakePhoto.setVisibility(View.INVISIBLE);
                    btnTakeAction.setVisibility(View.VISIBLE);
                    imgVideoRec.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    private void setExposureCompensation(int i) {
        MainController.setExposureCompensation(i, this.camera, this.btnExposure, this.btnExposure_4, this.btnExposure_3, this.btnExposure_2, this.btnExposure_1, this.btnExposure0, this.btnExposure1, this.btnExposure2, this.btnExposure3, this.btnExposure4, this.arrowExposure_4, this.arrowExposure_3, this.arrowExposure_2, this.arrowExposure_1, this.arrowExposure0, this.arrowExposure1, this.arrowExposure2, this.arrowExposure3, this.arrowExposure4, this);
    }

    // ========================== UI CONTROL METHODS ==========================

    private void toggleFlash() {
        // Cycle through all three flash modes: OFF -> ON -> AUTO -> OFF
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashMode = ImageCapture.FLASH_MODE_ON;
                UtilsX.changeButtonIcon(btnFlash, R.drawable.ic_flash_on, this);
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                UtilsX.changeButtonIcon(btnFlash, R.drawable.ic_flash_auto, this); // You'll need this icon
                break;
            case ImageCapture.FLASH_MODE_AUTO:
                flashMode = ImageCapture.FLASH_MODE_OFF;
                UtilsX.changeButtonIcon(btnFlash, R.drawable.ic_flash_off, this);
                break;
        }

        // Update torch for video mode if currently in video mode
        updateVideoFlash();
    }

    private void updateVideoFlash() {
        if (viewPagerSwitchAction.getCurrentItem() == 1 && camera != null && cameraControl != null) {
            // For video mode, only use torch on/off (no auto mode)
            boolean enableTorch = flashMode == ImageCapture.FLASH_MODE_ON && lensFacingType == CameraSelector.LENS_FACING_BACK;
            cameraControl.enableTorch(enableTorch);
        }
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
//        int keyCode = keyEvent.getKeyCode();
//
//        if (keyEvent.getAction() == 0) {
//            if (keyCode == 24 || keyCode == 25) { // 24 = VOLUME_UP, 25 = VOLUME_DOWN
//                int currentItem = this.viewPagerSwitchAction.getCurrentItem();
//                if (currentItem == 0 || currentItem == 1) {
//                    takeAction();
//                }
//                return true;
//            }
//            return super.dispatchKeyEvent(keyEvent);
//        }
//        return super.dispatchKeyEvent(keyEvent);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();

        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            // Prevent multiple triggers when long-pressed
            if (keyEvent.getRepeatCount() == 0) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    int currentItem = viewPagerSwitchAction.getCurrentItem();
                    if (currentItem == 0 || currentItem == 1) {
                        takeAction();
                    }
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }


    private void disableFlashForFrontCamera() {
        if (lensFacingType == CameraSelector.LENS_FACING_FRONT) {
            // Turn off torch for front camera
            if (camera != null && cameraControl != null) {
                cameraControl.enableTorch(false);
            }
        }
    }

    private void switchCamera() {
        // Stop any ongoing recording before switching
        if (isRecording) {
            stopVideoRecording();
        }

        lensFacingType = lensFacingType == CameraSelector.LENS_FACING_BACK ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        currentZoomRatio = getZoomRatioForCurrentLens();

        // Auto-disable flash for front camera in video mode
        if (lensFacingType == CameraSelector.LENS_FACING_FRONT && viewPagerSwitchAction.getCurrentItem() == 1) {
            disableFlashForFrontCamera();
        }

        startCameraPreview();
    }

    private void toggleHorizontalMenu() {
        if (isMenuExpanded) {
            UtilsX.slideMenuUp(layoutHorizontalMenu);
            UtilsX.rotateExpandButton(btnExpandMenu, false);
            manageExposurelayout();
            isMenuExpanded = false;
        } else {
            UtilsX.slideMenuDown(layoutHorizontalMenu);
            UtilsX.rotateExpandButton(btnExpandMenu, true);
            isMenuExpanded = true;
        }
    }

    private void manageExposurelayout() {
        if (layoutExposure.getVisibility() == View.VISIBLE) {
            MainController.toggleVisibilityView(layoutExposure, btnExposure, getResources());
        }
    }


    private void setZoomRatio(float ratio) {
        if (cameraControl != null && cameraInfo != null) {
            float requestedRatio = lensFacingType == CameraSelector.LENS_FACING_FRONT
                    ? 1.0f
                    : ratio;
            float minZoom = cameraInfo.getZoomState().getValue().getMinZoomRatio();
            float maxZoom = cameraInfo.getZoomState().getValue().getMaxZoomRatio();
            float clampedRatio = Math.max(minZoom, Math.min(maxZoom, requestedRatio));

            cameraControl.setZoomRatio(clampedRatio);
            currentZoomRatio = clampedRatio;
            if (lensFacingType == CameraSelector.LENS_FACING_BACK) {
                backCameraZoomRatio = clampedRatio;
            }
            UtilsX.updateZoomButtons(btnZoom1x, btnZoom2x, btnZoom3x, currentZoomRatio);
        }
    }

    private float getZoomRatioForCurrentLens() {
        return lensFacingType == CameraSelector.LENS_FACING_FRONT
                ? 1.0f
                : backCameraZoomRatio;
    }

    private void updateZoomVisibility() {
        int selectedTab = tabLayout.getSelectedTabPosition();
        boolean isBackCamera = lensFacingType == CameraSelector.LENS_FACING_BACK;
        boolean shouldShowZoom = (selectedTab == 0 || selectedTab == 1) && isBackCamera;

        zoomLayout.setVisibility(shouldShowZoom ? VISIBLE : GONE);
    }

//    private void handleTabSelection(int position) {
//        // Stop any ongoing recording when switching tabs
//        if (isRecording && position != 1) {
//            stopVideoRecording();
//        }
//
//        viewPagerSwitchAction.setCurrentItem(position);
//        UtilsX.toggleRecordingIndicator(imgCenterTakeAction, imgVideoRec, position);
//
//        new Handler().postDelayed(() -> {
//            startCameraPreview();
//            updateZoomVisibility();
//
//            // Update flash for video mode
//            if (position == 1) {
//                updateVideoFlash();
//                // Auto-disable flash for front camera in video mode
//                if (lensFacingType == CameraSelector.LENS_FACING_FRONT) {
//                    disableFlashForFrontCamera();
//                }
//            }
//        }, 200);
//
//        float alpha = (position == 0 || position == 1) ? 1f : 0.5f;
//        UtilsX.setAlpha(layoutBottom, alpha);
//
//        boolean isEnabled = position == 0 || position == 1;
//        btnTakeAction.setEnabled(isEnabled);
//        btnSwitchCamera.setEnabled(isEnabled);
//    }

    private void handleTabSelection(int position) {
        if (isSwitching) return; // ignore rapid switches
        isSwitching = true;

        if (isRecording && position != 1) {
            stopVideoRecording();
        }

        if (position == 1 && currentRatioType == RatioDialog.RATIO_FULL) {
            currentRatioType = RatioDialog.RATIO_16_9;
        }

        viewPagerSwitchAction.setCurrentItem(position);
        UtilsX.toggleRecordingIndicator(imgCenterTakeAction, imgVideoRec, position);

        new Handler().postDelayed(() -> {
            startCameraPreview();
            updateZoomVisibility();

            if (position == 1) {
                updateVideoFlash();
                if (lensFacingType == CameraSelector.LENS_FACING_FRONT) {
                    disableFlashForFrontCamera();
                }
            }

            isSwitching = false; // allow next switch only after preview binds
        }, 200);
    }

    // ========================== LIFECYCLE METHODS ==========================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupResources();
    }

    private void cleanupResources() {
        // Stop any ongoing recording
        if (isRecording) {
            stopVideoRecording();
        }

        if (handler != null && dateTimeUpdater != null) {
            handler.removeCallbacks(dateTimeUpdater);
        }

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        if (supportMapFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(supportMapFragment).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        txtCountDownTakePhoto.setVisibility(View.INVISIBLE);
        btnTakeAction.setVisibility(View.VISIBLE);
        imgCenterTakeAction.setVisibility(View.VISIBLE);
        imgVideoRec.setVisibility(View.VISIBLE);

        // Stop recording when app goes to background
        if (isRecording) {
            stopVideoRecording();
        }

        if (handler != null && dateTimeUpdater != null) {
            handler.removeCallbacks(dateTimeUpdater);
        }
    }

    private void updateMaps() {
        if (!hasSessionDateTimeOverride) {
            getStampDateTime();
        }
        updateStampDateTime();
        current_map_type = getActiveMapTypeForSession();
        if (googleMap != null) {
            googleMap.setMapType(current_map_type);
        }
    }

    private void refreshLatestPhoto() {
        LiveData<List<Photo>> source = viewModel.getAllPhoto();
        source.observe(this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                source.removeObserver(this);
                List<Photo> validPhotos = new ArrayList<>();
                List<Photo> missingPhotos = new ArrayList<>();

                if (photos != null) {
                    for (Photo candidate : photos) {
                        if (SharedMediaStore.exists(MainActivity.this,
                                candidate.getMediaUri(), candidate.getImagePath())) {
                            candidate.setImagePath(SharedMediaStore.resolveCurrentPath(
                                    MainActivity.this,
                                    candidate.getMediaUri(),
                                    candidate.getImagePath()));
                            validPhotos.add(candidate);
                        } else {
                            missingPhotos.add(candidate);
                        }
                    }
                }

                if (!missingPhotos.isEmpty()) {
                    for (Photo missingPhoto : missingPhotos) {
                        SharedMediaStore.delete(MainActivity.this, missingPhoto);
                    }
                    viewModel.deletePhotos(missingPhotos);
                }

                if (!validPhotos.isEmpty()) {
                    photoOld = validPhotos.get(0);
                    Glide.with(MainActivity.this)
                            .load(SharedMediaStore.getLoadSource(photoOld))
                            .into(ivMyCapture);
                } else {
                    photoOld = null;
                    isCapture = false;
                    ivMyCapture.setImageResource(R.drawable.my_capture_icon);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerInternetNetworkCallback();
        reconcileInternetDialogState();
    }

    @Override
    protected void onResume() {

        super.onResume();
        refreshPremiumUi();
        refreshMainAdForPremiumState();
        reconcileInternetDialogState();
        handler.postDelayed(this::reconcileInternetDialogState, 1200L);
        if (fusedLocationClient != null && locationCallback != null && locationRequest != null
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestLocationUpdates();
        }
        refreshLatestPhoto();
        setZoomRatio(getZoomRatioForCurrentLens());
        updateZoomVisibility();
        // Re-enable immersive mode
        enableImmersiveMode();

        if (handler != null && dateTimeUpdater != null) {
            handler.post(dateTimeUpdater);
        }
        getStampType();
        applyApplicationSessionFormatting();
        updateMaps();
        renderStamp();
    }



    @Override
    protected void onStop() {
        super.onStop();
        unregisterInternetNetworkCallback();

        // Ensure recording is stopped when activity stops
        if (isRecording) {
            stopVideoRecording();
        }
    }
}
