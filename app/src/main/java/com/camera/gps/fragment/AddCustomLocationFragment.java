package com.camera.gps.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.camera.gps.MyApplication;
import com.camera.gps.activity.MyLocation_Activity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Ref;

import com.camera.gps.R;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.databinding.FragmentAddCustomLocationBinding;
import com.camera.gps.premium.PremiumManager;
import com.camera.gps.util.Constant;
import com.camera.gps.util.LocationAddressFormatter;
import com.camera.gps.util.LocationSettingsPrompt;
import com.camera.gps.database.entity.MyLocation;

public class AddCustomLocationFragment extends Fragment implements OnMapReadyCallback {

    private FragmentAddCustomLocationBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private GlobalViewModel viewModel;
    private String defaultTitle = "";

    private final ActivityResultLauncher<IntentSenderRequest> enableLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (!isAdded()) {
                    return;
                }
                if (isLocationEnabled()) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        checkLocationPermission();
                    } else {
                        getLocation();
                    }
                    startLocationUpdates();
                }
            });

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean result) {
                            if (result) {
                                getLocation();
                            } else {
                                Toast.makeText(getContext(),
                                        getResources().getString(R.string.please_allow_the_location_permissions),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddCustomLocationBinding.inflate(inflater, container, false);
        binding.premiumBadge.setVisibility(PremiumManager.isPremium(requireContext())
                ? View.GONE : View.VISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupClickListeners();
        initMap();
    }


    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity(),
                new GlobalViewModelFactory(requireActivity().getApplication())).get(GlobalViewModel.class);
    }

    private void setupClickListeners() {
        binding.lldate.setOnClickListener(view -> showDatePicker());
        binding.llTime.setOnClickListener(view -> showTimePicker());
        binding.btnSave.setOnClickListener(view -> saveLocation());
    }

    private void saveLocation() {
        String title = binding.etHome.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String latitude = binding.etLatitide.getText().toString();
        String longitude = binding.etLongitude.getText().toString();
        String date = binding.tvDate.getText().toString();
        String time = binding.tvTime.getText().toString();



        if (isValidData(address, latitude, longitude)) {
            MyLocation location = new MyLocation(
                    null, title, date, time, address, latitude, longitude, false,
                    defaultTitle);

            // An optional blank title must not collide with other untitled locations.
            if (title.isEmpty()) {
                binding.etHome.setError(null);
                saveLocation(location);
                return;
            }

            viewModel.getLocationByTitle(title).observe(getViewLifecycleOwner(), existingLocation -> {
                if (existingLocation != null) {
                    binding.etHome.setError("Title already exists");
                } else {
                    saveLocation(location);
                }
            });
        }
    }

    private boolean isValidData(String address, String latitude, String longitude) {
        boolean valid = true;
        binding.etHome.setError(null);
        if (address.length() == 0) {
            binding.etAddress.setError(getResources().getString(R.string.please_enter_address));
            valid = false;
        } else {
            binding.etAddress.setError(null);
        }
        if (latitude.length() == 0) {
            binding.etLatitide.setError("Latitude is required!");
            valid = false;
        } else {
            binding.etLatitide.setError(null);
        }
        if (longitude.length() == 0) {
            binding.etLongitude.setError("Longitude is required!");
            return false;
        }
        binding.etLongitude.setError(null);
        return valid;
    }


    private void saveLocation(final MyLocation location) {
        viewModel.insertLocation(location).observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long result) {
                if (result != null) {
                    Constant.Companion.showToast(getContext(),
                            getResources().getString(R.string.location_saved));
                    location.setId(result.intValue());
                    viewModel.getAllLocation();


                    if (requireActivity() instanceof MyLocation_Activity) {
                        MyLocation_Activity activity = (MyLocation_Activity) requireActivity();

                        if ("SETTINGS".equals(activity.getSource())) {
                            activity.showSavedLocations();
                        } else {
                            // Return result to MainActivity and finish the activity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(MyApplication.EXTRA_LOCATION, location);
                            requireActivity().setResult(Activity.RESULT_OK, resultIntent);
                            requireActivity().finish();
                        }
                    }
                }
            }
        });
    }


    private void clearFields() {
        binding.etHome.setText("");
        binding.etAddress.setText("");
        binding.etLatitide.setText("");
        binding.etLongitude.setText("");
        defaultTitle = "";
        setCurrentTimeAndDate();
        if (mMap != null) {
            mMap.clear();
        }
    }

    private void initData() {
        Serializable serializableExtra = requireActivity().getIntent().getSerializableExtra(MyApplication.EXTRA_LOCATION);
        if (serializableExtra != null) {
            MyLocation location = (MyLocation) serializableExtra;
            binding.tvDate.setText(location.getDate());
            binding.tvTime.setText(location.getTime());
            binding.etLatitide.setText(location.getLatitude());
            binding.etLongitude.setText(location.getLongitude());
            binding.etAddress.setText(location.getAddress());
            defaultTitle = location.getDefaultTitle() == null ? "" : location.getDefaultTitle();
            if (location.getLatitude() != null && location.getLongitude() != null) {
                String latitude = location.getLatitude();
                double parseDouble = Double.parseDouble(latitude);
                String longitude = location.getLongitude();
                LatLng latLng = new LatLng(parseDouble, Double.parseDouble(longitude));
                this.mMap.addMarker(new MarkerOptions().position(latLng).title("Current position"));
                CameraPosition build = new CameraPosition.Builder().zoom(15.0f).target(latLng).build();
                CameraUpdate newCameraPosition = CameraUpdateFactory.newCameraPosition(build);
                this.mMap.animateCamera(newCameraPosition);
            }
        } else {
            setCurrentTimeAndDate();
            createLocationCallback();
            buildGoogleApiClient();
        }

        // FIXED ISSUE 1: Disable map gestures when needed to prevent tab switching interference
        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng = this.mMap.getCameraPosition().target;
            double doubleValue = new BigDecimal(latLng.latitude).setScale(6, RoundingMode.HALF_EVEN).doubleValue();
            double doubleValue2 = new BigDecimal(latLng.longitude).setScale(6, RoundingMode.HALF_EVEN).doubleValue();
            this.binding.etLatitide.setText(String.valueOf(doubleValue));
            this.binding.etLongitude.setText(String.valueOf(doubleValue2));
            this.getAddress(latLng.latitude, latLng.longitude);
        });
    }

    private void setCurrentTimeAndDate() {
        binding.tvDate.setText(new SimpleDateFormat("dd MMM-yy,EEEE").format(new Date()));
        binding.tvTime.setText(new SimpleDateFormat("hh:mm aaa").format(new Date()));
    }

    private void getAddress(double d, double d2) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(d, d2, 1);
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (binding != null) {
                            setAddress(addresses);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (binding != null) {
                            binding.etAddress.setText("Unknown");
                            defaultTitle = "";
                        }
                    });
                }
            }
        }).start();
    }

    public void setAddress(List<Address> list) {
        if (list != null && list.size() > 0) {
            Address address = list.get(0);
            binding.etAddress.setText(address.getAddressLine(0) + ",");
            defaultTitle = LocationAddressFormatter.buildDefaultTitle(address);
            return;
        }
        binding.etAddress.setText("Unknown");
        defaultTitle = "";
    }

    private void initMap() {
        Fragment findFragmentById = getChildFragmentManager().findFragmentById(R.id.map);
        ((SupportMapFragment) findFragmentById).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        configureMapTouchHandling();

        googleMap.setMapType(MyApplication.getMapType());

        googleMap.getUiSettings().setZoomControlsEnabled(true);


        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        initData();
    }

    /**
     * Keep vertical gestures that start on the map with the map instead of
     * allowing the containing ScrollView to intercept them.
     */
    private void configureMapTouchHandling() {
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null || mapFragment.getView() == null) {
            return;
        }

        setMapTouchListener(mapFragment.getView());
    }

    private void setMapTouchListener(View view) {
        view.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
            // Let the Google Maps view handle the gesture itself.
            return false;
        });

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setMapTouchListener(group.getChildAt(i));
            }
        }
    }

    private void showDatePicker() {
        long j = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(j);
        MaterialDatePicker<Long> build = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(calendar.getTimeInMillis())
                .setTheme(R.style.MyDatePickerTheme)
                .build();

        final Function1<Long, Unit> function1 = new Function1<Long, Unit>() {
            @Override
            public Unit invoke(Long l) {
                invoke2(l);
                return Unit.INSTANCE;
            }

            public void invoke2(Long l) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String format = simpleDateFormat.format(l);
                binding.tvDate.setText(format);
            }
        };
        build.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener) obj -> function1.invoke((Long) obj));
        build.show(getParentFragmentManager(), "tag");
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        final MaterialTimePicker build = new MaterialTimePicker.Builder()
                .setTitleText("Select Time")
                .setHour(calendar.get(11))
                .setMinute(calendar.get(12))
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTheme(R.style.MyTimePickerTheme)
                .build();

        build.addOnPositiveButtonClickListener(view -> {
            int hour = build.getHour();
            int minute = build.getMinute();
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(11, hour);
            calendar1.set(12, minute);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss a");
            binding.tvTime.setText(simpleDateFormat.format(calendar1.getTime()));
        });
        build.show(getParentFragmentManager(), "tag");
    }

    @Override
    public void onPause() {
        super.onPause();
        GoogleApiClient googleApiClient = this.mGoogleApiClient;
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                GoogleApiClient googleApiClient2 = this.mGoogleApiClient;
                googleApiClient2.disconnect();
                stopLocationUpdates();
            }
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
                    getLocation();
                }
            }
        };
    }

    private synchronized void buildGoogleApiClient() {
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        GoogleApiClient build = new GoogleApiClient.Builder(requireContext())
                .addApi(LocationServices.API)
                .build();
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
        GoogleApiClient googleApiClient = this.mGoogleApiClient;
        googleApiClient.connect();
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
                .setFastestInterval(2000);
        LocationSettingsPrompt.show(requireContext(), promptLocationRequest, enableLocationLauncher, () -> {});
    }

    private void showAlertFallback() {
        if (!isAdded()) {
            return;
        }
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        materialAlertDialogBuilder.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to Off.\nPlease Enable Location in settings then refresh the page")
                .setPositiveButton("Settings", (dialogInterface, i) -> {
                    startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        materialAlertDialogBuilder.show();
    }

    private boolean isLocationEnabled() {
        Object systemService = requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationManager locationManager = (LocationManager) systemService;
        return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
    }

    private void checkLocationPermission() {
        this.requestPermission.launch("android.permission.ACCESS_FINE_LOCATION");
    }

    public void startLocationUpdates() {
        this.mLocationRequest = LocationRequest.create()
                .setPriority(100)
                .setInterval(2000)
                .setFastestInterval(2000);

        if (ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_FINE_LOCATION") == 0 ||
                ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.mFusedLocationClient.requestLocationUpdates(this.mLocationRequest, this.mLocationCallback, Looper.myLooper());
        }
    }

    private void stopLocationUpdates() {
        FusedLocationProviderClient fusedLocationProviderClient = this.mFusedLocationClient;
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(this.mLocationCallback)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                        }
                    });
        }
    }

    public void getLocation() {
        if (this.mLocation == null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_FINE_LOCATION") != 0 &&
                    ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_COARSE_LOCATION") != 0) {
                return;
            }
            FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
            GoogleApiClient googleApiClient = this.mGoogleApiClient;
            this.mLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        }
        if (this.mLocation == null) {
            return;
        }

        binding.etLatitide.setText(String.valueOf(this.mLocation.getLatitude()));
        binding.etLongitude.setText(String.valueOf(this.mLocation.getLongitude()));
        double latitude = this.mLocation.getLatitude();
        LatLng latLng = new LatLng(latitude, this.mLocation.getLongitude());
        this.mMap.addMarker(new MarkerOptions().position(latLng).title("Current position"));
        CameraPosition build = new CameraPosition.Builder().zoom(18.0f).target(latLng).build();
        CameraUpdate newCameraPosition = CameraUpdateFactory.newCameraPosition(build);
        this.mMap.animateCamera(newCameraPosition);

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            double latitude2 = this.mLocation.getLatitude();
            List<Address> fromLocation = geocoder.getFromLocation(latitude2, this.mLocation.getLongitude(), 1);
            if (fromLocation != null) {
                setAddress(fromLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
