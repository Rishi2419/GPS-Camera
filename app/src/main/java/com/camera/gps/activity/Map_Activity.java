package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.admob.AdMobBannerAdHelper;
import com.camera.gps.databinding.ActivityMapBinding;
import com.camera.gps.util.LocationSettingsPrompt;
import com.camera.gps.util.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.maps.android.clustering.ClusterManager;
import com.camera.gps.dialogs.MapTypeDialog;
import com.camera.gps.listener.OnMapTypeSelectedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.model.MarkerModel;
import com.camera.gps.model.PersonRenderer;

public final class Map_Activity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_SELECTED_MAP_TYPE = "com.camera.gps.activity.EXTRA_SELECTED_MAP_TYPE";
    private static final int FALLBACK_MAP_TYPE = Integer.MIN_VALUE;

    private Geocoder geocoder;
    private ActivityMapBinding binding;
    private ClusterManager<MarkerModel> mClusterManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    int current_map_type;
    private MapTypeDialog mapTypeDialog;

    private final ActivityResultLauncher<String> requestPermission;
    private final ActivityResultLauncher<IntentSenderRequest> enableLocationLauncher;
    private GlobalViewModel viewModel;
    private final List<MarkerModel> allMarkerModels = new ArrayList<>();
    private boolean isMapReady = false;
    private MarkerModel clickedMarker = null;
    private boolean isLocationObtained = false;
    private boolean hasZoomedToUserLocation = false;
    private boolean storageObserversRegistered = false;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private final List<Photo> currentPhotoList = new ArrayList<>();

    private Runnable timeoutRunnable;

    public Map_Activity() {
        this.enableLocationLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (isLocationEnabled()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkLocationPermission();
                } else {
                    getLocation();
                }
                startLocationUpdates();
            }
        });
        this.requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback() {
            @Override
            public void onActivityResult(Object obj) {
                if ((Boolean) obj) {
                    getLocation();
                } else {
                    Toast.makeText(Map_Activity.this, getResources().getString(R.string.please_allow_the_location_permissions), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int requestedMapType = getIntent().getIntExtra(EXTRA_SELECTED_MAP_TYPE, FALLBACK_MAP_TYPE);
        current_map_type = requestedMapType != FALLBACK_MAP_TYPE ? requestedMapType : MyApplication.getMapType();

        Application application = getApplication();
        viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(application)).get(GlobalViewModel.class);
        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });
        binding.ivMapType.setOnClickListener(this::showMapType);
        geocoder = new Geocoder(this, Locale.getDefault());

        initMap();
        loadBottomBannerAd();
    }

    private void loadBottomBannerAd() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            binding.flMapBanner.post(() -> AdMobBannerAdHelper.loadBannerAd(this, binding.flMapBanner, "map_banner"));
        } else {
            binding.flMapBanner.setVisibility(View.GONE);
        }
    }


    private void initMap() {
        Fragment findFragmentById = getSupportFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment supportMapFragment = (SupportMapFragment) findFragmentById;
        supportMapFragment.getMapAsync(this);
        @SuppressLint("ResourceType") View findViewById = supportMapFragment.requireView().findViewById(2);
        if (findViewById == null || !(findViewById.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = findViewById.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) layoutParams;
        layoutParams2.addRule(10, 0);
        layoutParams2.addRule(12, -1);
        int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30.0f, getResources().getDisplayMetrics());
        layoutParams2.setMargins(applyDimension, applyDimension, applyDimension, applyDimension * 4);
        findViewById.setLayoutParams(layoutParams2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        isMapReady = true;

        googleMap.setMapType(current_map_type);

        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 && ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        this.mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.getAlgorithm().setMaxDistanceBetweenClusteredItems(300);

        mMap.setOnMarkerClickListener(this.mClusterManager);
        mMap.setOnInfoWindowClickListener(this.mClusterManager);
        PersonRenderer personRenderer = new PersonRenderer(this, googleMap, mClusterManager);
        mClusterManager.setRenderer(personRenderer);

        mClusterManager.setOnClusterItemClickListener(markerModel -> {
            if (markerModel.equals(clickedMarker)) {
                // This is the second click, open the preview
                openMarkerPreview(markerModel);
                clickedMarker = null; // Reset for the next interaction
                return true; // Consume the event
            } else {
                // This is the first click
                clickedMarker = markerModel;
                String address = markerModel.getTitle();
                if (address == null || address.trim().isEmpty()) {
                    address = "Unknown";
                }
                markerModel.setTitle(address);

                // The renderer will handle showing the info window,
                // but we can give it a nudge if needed.
                // This logic might need adjustment based on your PersonRenderer implementation.
                new Handler(Looper.getMainLooper()).post(() -> {
                    Marker marker = personRenderer.getMarker(markerModel);
                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                });

                // Return false to allow the default behavior (centering on the marker and showing info window)
                // to also occur. If you want full manual control, you might need to adjust this.
                return false;
            }
        });

        mClusterManager.setOnClusterItemInfoWindowClickListener(markerModel -> {
            openMarkerPreview(markerModel);
        });

        createLocationCallback();
        buildGoogleApiClient();
        getPhotos();
        getLocation();
    }

    private void openMarkerPreview(MarkerModel markerModel) {
        try {
            Photo photo = findMatchingPhoto(markerModel);
            if (photo != null) {
                Intent intent = new Intent(this, PhotoPreview_Activity.class);
                intent.putExtra("model", photo);
                intent.putExtra("fromMap", true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Unable to preview this media", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MapActivity", "Error opening marker preview", e);
        }
    }

    private Photo findMatchingPhoto(MarkerModel markerModel) {
        for (Photo p : currentPhotoList) {
            if (p.getImagePath().equals(markerModel.getPath())) {
                return p;
            }
        }
        return null;
    }

    public String getAddress(LatLng latLng) {
        Address address = null;
        try {
            Geocoder geocoder = this.geocoder;
            List<Address> fromLocation = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (fromLocation == null || fromLocation.size() == 0) {
                return "Unknown";
            }
            address = fromLocation.get(0);
            return address.getAddressLine(0) + ",";
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private List<MarkerModel> getVisibleItems(LatLngBounds latLngBounds) {
        ArrayList arrayList = new ArrayList();
        for (MarkerModel markerModel : this.allMarkerModels) {
            if (latLngBounds.contains(markerModel.getPosition())) {
                arrayList.add(markerModel);
            }
        }
        return arrayList;
    }

    public void getPhotos() {
        viewModel.getAllPhoto().observe(this, new Observer() {
            @Override
            public void onChanged(Object obj) {
                if (obj != null && !((List) obj).isEmpty()) {
                    mMap.clear();

                    // ADDED: Store the current photo list for marker-to-photo mapping
                    currentPhotoList.clear();
                    allMarkerModels.clear();
                    List<Photo> photos = (List<Photo>) obj;
                    currentPhotoList.addAll(photos);

                    for (Object o : (List) obj) {
                        Photo photo = (Photo) o;
                        if (photo.getLatitude() != null && photo.getLongitude() != null) {
                            double parseDouble = Double.parseDouble(photo.getLatitude());
                            double parseDouble2 = Double.parseDouble(photo.getLongitude());

                            // UPDATED: Pass additional parameters for better marker creation
                            boolean isVideo = photo.getImagePath() != null &&
                                    (photo.getImagePath().endsWith(".mp4") ||
                                            photo.getImagePath().endsWith(".avi") ||
                                            photo.getImagePath().endsWith(".mov"));

                            long timestamp = photo.getId();

                            MarkerModel markerModel = new MarkerModel(parseDouble, parseDouble2,
                                    photo.getImagePath(),
                                    photo.getAddress(),
                                    isVideo ? "video" : "image",
                                    isVideo,
                                    timestamp);
                            allMarkerModels.add(markerModel);
                        }
                    }
                } else {
                    mMap.clear();
                    currentPhotoList.clear();
                    allMarkerModels.clear();
                    if (mClusterManager != null) {
                        mClusterManager.clearItems();
                        mClusterManager.cluster();
                    }
                }
                searchStorage();
            }
        });
    }

    private void searchStorage() {
        if (storageObserversRegistered) {
            return;
        }
        storageObserversRegistered = true;

        viewModel.getMarkerOpMutableLiveData().observe(this, new Observer() {
            @Override
            public void onChanged(Object obj) {
                if (obj != null) {
                    mClusterManager.addItem((MarkerModel) obj);
                }
            }
        });
        viewModel.getImagesFromGalleyOnlyLocation(this).observe(this, new Observer() {
            @Override
            public void onChanged(Object obj) {
                if (obj != null) {
                    mMap.setOnCameraIdleListener(() -> {
                        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        mClusterManager.clearItems();
                        mClusterManager.addItems(getVisibleItems(latLngBounds));
                        mClusterManager.cluster();
                    });
                }
            }
        });
    }

    @SuppressLint("ResourceType")
    private void showMapType(View view) {
        if (mapTypeDialog != null && mapTypeDialog.isShowing()) {
            return;
        }
        mapTypeDialog = new MapTypeDialog(this, current_map_type, new OnMapTypeSelectedListener() {
            @Override
            public void onMapTypeSelected(int mapType) {
                current_map_type = mapType;
                if (mMap != null) {
                    mMap.setMapType(current_map_type);
                }
            }

            @Override
            public void onDialogDismissed() {

            }
        });

        Window window = mapTypeDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.getAttributes().windowAnimations = R.style.BottomDialogSlideAnimation;
        }
        mapTypeDialog.show();
    }

    @Override
    public void onBackPressed() {
        setResultForMapType();
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "map_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("MapActivity", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }

    private void setResultForMapType() {
        Intent result = new Intent();
        result.putExtra(EXTRA_SELECTED_MAP_TYPE, current_map_type);
        setResult(RESULT_OK, result);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mGoogleApiClient != null) {
            if (this.mGoogleApiClient.isConnected()) {
                this.mGoogleApiClient.disconnect();
                stopLocationUpdates();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    private void createLocationCallback() {
        this.mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (mLocation == null) {
                    List<Location> locations = locationResult.getLocations();
                    if (locations.isEmpty()) {
                        mLocation = locationResult.getLastLocation();
                    } else {
                        mLocation = locationResult.getLocations().get(0);
                    }
                    if (mLocation != null) {
                        isLocationObtained = true;
                        zoomToUserLocation();
                    }
                }
            }
        };
    }

    private void zoomToUserLocation() {
        if (mLocation != null && mMap != null && !hasZoomedToUserLocation) {
            hasZoomedToUserLocation = true;
            CameraPosition build = new CameraPosition.Builder()
                    .zoom(17.0f)
                    .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                    .build();
            CameraUpdate newCameraPosition = CameraUpdateFactory.newCameraPosition(build);
            mMap.animateCamera(newCameraPosition);
        }
    }

    private synchronized void buildGoogleApiClient() {
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        GoogleApiClient build = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        this.mGoogleApiClient = build;
        build.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int i) {
            }

            @Override
            public void onConnected(Bundle bundle) {
                checkGPSEnabled();
                startLocationUpdates();
            }
        });
        mGoogleApiClient.connect();
    }

    public void checkGPSEnabled() {
        if (!isLocationEnabled()) {
            showAlert();
        } else if (Build.VERSION.SDK_INT >= 23) {
            checkLocationPermission();
        } else {
            getLocation();
        }
    }

    private void showAlert() {
        LocationRequest promptLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);
        LocationSettingsPrompt.show(this, promptLocationRequest, enableLocationLauncher, () -> {});
    }

    private void showAlertFallback() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.dialog_enable_location_title))
                .setMessage(getString(R.string.dialog_enable_location_message))
                .setPositiveButton(getString(R.string.settings), (dialog, which) -> {
                    startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(this, R.color.blue_primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(this, R.color.blue_primary));
    }


    private boolean isLocationEnabled() {
        Object systemService = getSystemService(Context.LOCATION_SERVICE);
        LocationManager locationManager = (LocationManager) systemService;
        return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
    }

    private void checkLocationPermission() {
        this.requestPermission.launch("android.permission.ACCESS_FINE_LOCATION");
    }

    public void startLocationUpdates() {
        this.mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 ||
                ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.mFusedLocationClient.requestLocationUpdates(this.mLocationRequest, this.mLocationCallback, Looper.myLooper());
        }
    }

    private void stopLocationUpdates() {
        this.mFusedLocationClient.removeLocationUpdates(this.mLocationCallback).addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
            }
        });
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0 &&
                ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            return;
        }

        // Try to get last known location first for faster response
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                mLocation = task.getResult();
                isLocationObtained = true;
                zoomToUserLocation();
            } else {
                // Fallback to LocationManager if FusedLocationProvider fails
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(@NonNull Location location) {
                        if (mLocation != null) {
                            return;
                        }
                        mLocation = location;
                        isLocationObtained = true;
                        zoomToUserLocation();
                        // Remove this listener after getting the first location
                        locationManager.removeUpdates(this);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(@NonNull String provider) {
                    }

                    public void onProviderDisabled(@NonNull String provider) {
                    }
                };

                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } catch (SecurityException e) {
                    Log.e("MapActivity", "Location permission not granted", e);
                }
            }
        });
    }
}
