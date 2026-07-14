package com.camera.gps.util;

import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

public final class LocationSettingsPrompt {

    private LocationSettingsPrompt() {
    }

    public static void show(
            Context context,
            LocationRequest locationRequest,
            ActivityResultLauncher<IntentSenderRequest> launcher,
            Runnable fallback
    ) {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build();

        LocationServices.getSettingsClient(context)
                .checkLocationSettings(request)
                .addOnFailureListener(exception -> {
                    if (exception instanceof ResolvableApiException) {
                        try {
                            launcher.launch(new IntentSenderRequest.Builder(
                                    ((ResolvableApiException) exception).getResolution()
                            ).build());
                            return;
                        } catch (Exception ignored) {
                            // Fall through to the existing manual settings dialog.
                        }
                    }
                    fallback.run();
                });
    }
}
