package com.camera.gps.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.camera.gps.MyApplication;
import com.camera.gps.R;

public class Permissions_Activity extends InsetAwareActivity {

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


//    Request codes used to identify which permission result was received in onRequestPermissionsResult
    private static final int REQ_CAMERA = 100;
    private static final int REQ_LOCATION = 101;
    private static final int REQ_MICROPHONE = 102;

    private Switch switchCamera, switchLocation, switchMicrophone;


//    Flags used to temporarily disable switch listeners during programmatic updates
    private boolean suppressCameraListener = false;
    private boolean suppressLocationListener = false;
    private boolean suppressMicrophoneListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        switchCamera = findViewById(R.id.switch_camera);
        switchLocation = findViewById(R.id.switch_location);
        switchMicrophone = findViewById(R.id.switch_microphone);

        updateSwitchStates();
        updateContinueVisibility();
        // CAMERA
        switchCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressCameraListener) return;
            if (isChecked) {
                requestCameraPermission();
            } else {
                MyApplication.setCameraPermissionGranted(false);
                openAppSettingsIfPermissionStillGranted(Manifest.permission.CAMERA);
            }
        });

        // LOCATION
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressLocationListener) return;
            if (isChecked) {
                requestLocationPermission();
            } else {
                MyApplication.setLocationPermissionGranted(false);
                openAppSettingsIfPermissionStillGranted(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        switchMicrophone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressMicrophoneListener) return;
            if (isChecked) {
                requestMicrophonePermission();
            } else {
                MyApplication.setMicrophonePermissionGranted(false);
                openAppSettingsIfPermissionStillGranted(Manifest.permission.RECORD_AUDIO);
            }
        });


        TextView btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(Permissions_Activity.this, Onboarding_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        } else {
            MyApplication.setCameraPermissionGranted(true);
            switchCamera.setChecked(true);
        }
        updateContinueVisibility();
    }

    private void requestLocationPermission() {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(this,
                    LOCATION_PERMISSIONS, REQ_LOCATION);
        } else {
            MyApplication.setLocationPermissionGranted(true);
            switchLocation.setChecked(true);
        }
        updateContinueVisibility();
    }

    private boolean isLocationPermissionGranted() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }
    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MICROPHONE);
        } else {
            MyApplication.setMicrophonePermissionGranted(true);
            switchMicrophone.setChecked(true);
        }
        updateContinueVisibility();
    }



    //    Opening settings because Android doesn't allow revoking permissions programmatically
    private void openAppSettingsIfPermissionStillGranted(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_permission_denied_title))
                .setMessage(getString(R.string.dialog_permission_denied_message))
                .setPositiveButton(getString(R.string.settings), (dialogInterface, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(this, R.color.blue_primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(this, R.color.blue_primary));
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        switch (requestCode) {
            case REQ_CAMERA:
                MyApplication.setCameraPermissionGranted(granted);
                suppressCameraListener = true;
                switchCamera.setChecked(granted);
                suppressCameraListener = false;

                if (!granted) showPermissionDeniedDialog();
                break;

            case REQ_LOCATION:
                boolean locGranted = isLocationPermissionGranted();
                MyApplication.setLocationPermissionGranted(locGranted);
                suppressLocationListener = true;
                switchLocation.setChecked(locGranted);
                suppressLocationListener = false;

                if (!locGranted) showPermissionDeniedDialog();
                break;

            case REQ_MICROPHONE:
                MyApplication.setMicrophonePermissionGranted(granted);
                suppressMicrophoneListener = true;
                switchMicrophone.setChecked(granted);
                suppressMicrophoneListener = false;

                if (!granted) showPermissionDeniedDialog();
                break;

        }

        updateContinueVisibility();
    }

    private void updateContinueVisibility() {
        boolean show = MyApplication.isCameraPermissionGranted() &&
                MyApplication.isLocationPermissionGranted() &&
                MyApplication.isMicrophonePermissionGranted();

        findViewById(R.id.btnContinue).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateSwitchStates() {
        // Read real system state
        boolean camGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        // Consider either fine or coarse as "location granted"
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean locGranted = fine || coarse;

        // Update your app-level flags to match reality
        MyApplication.setCameraPermissionGranted(camGranted);
        MyApplication.setMicrophonePermissionGranted(micGranted);
        MyApplication.setLocationPermissionGranted(locGranted);

        // Update switches without triggering listeners
        suppressCameraListener = true;
        suppressMicrophoneListener = true;
        suppressLocationListener = true;

        switchCamera.setChecked(camGranted);
        switchMicrophone.setChecked(micGranted);
        switchLocation.setChecked(locGranted);

        suppressCameraListener = false;
        suppressMicrophoneListener = false;
        suppressLocationListener = false;
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        startActivity(new Intent(Permissions_Activity.this, Language_Activity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchStates();       // Re-check system and refresh UI on every return
        updateContinueVisibility();
    }


}
