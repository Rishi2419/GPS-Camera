package com.camera.gps.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.camera.gps.databinding.ActivityNoPreviewAvailableBinding;

public class No_Preview_Available extends AppCompatActivity {

    private ActivityNoPreviewAvailableBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize view binding
        binding = ActivityNoPreviewAvailableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Back button click
        binding.btBack.setOnClickListener(view -> onBackPressed());

        // Go to Collection button click
        binding.goToCollection.setOnClickListener(view -> {
            Intent intent = new Intent(No_Preview_Available.this, MyCreation_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
