package com.camera.gps.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.camera.gps.databinding.ActivityPhotoGalleryBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.PhotoGalleryAdapter;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.util.Constant;
import com.camera.gps.util.HelperClass;

public final class PhotoGallery_Activity extends AppCompatActivity {

    private ActivityPhotoGalleryBinding binding;
    private GlobalViewModel viewModel;
    private PhotoGalleryAdapter galleryAdapter;
    private List<Photo> photoList = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isUIVisible = true;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = ActivityPhotoGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Application application = getApplication();
        getWindow().setFlags(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(application)).get(GlobalViewModel.class);

        // Get the photo list and current position from intent
        ArrayList<Photo> photos = (ArrayList<Photo>) getIntent().getSerializableExtra("photoList");
        currentPosition = getIntent().getIntExtra("currentPosition", 0);
        boolean fromCreation = getIntent().getBooleanExtra("fromCreation", false);

        if (photos != null) {
            photoList = new ArrayList<>(photos);
            setupViewPager();
        }

        setupClickListeners();
        updateCounter();
    }

    private void setupViewPager() {
        galleryAdapter = new PhotoGalleryAdapter(this, photoList, new PhotoGalleryAdapter.OnPhotoInteractionListener() {
            @Override
            public void onPhotoClick() {
                toggleUI();
            }

            @Override
            public void onVideoStatusChanged(boolean isPlaying) {
                // Handle video play/pause if needed
            }
        });

        binding.viewPager.setAdapter(galleryAdapter);
        binding.viewPager.setCurrentItem(currentPosition, false);


        // Listen for page changes to update counter and buttons
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateCounter();
            }
        });
    }

    private void setupClickListeners() {
        binding.btBack.setOnClickListener(view -> onBackPressed());

        binding.btnDelete.setOnClickListener(view -> {
            if (currentPosition < photoList.size()) {
                Photo currentPhoto = photoList.get(currentPosition);
                boolean isVideo = currentPhoto.getImagePath().endsWith(".mp4");
                deleteAlert(currentPhoto, isVideo);
            }
        });

        binding.btnShare.setOnClickListener(view -> {
            if (currentPosition < photoList.size()) {
                Photo currentPhoto = photoList.get(currentPosition);
                share(currentPhoto);
            }
        });
    }

    private void updateCounter() {
        if (photoList.size() > 0) {
            String counter = (currentPosition + 1) + " of " + photoList.size();
            binding.tvCounter.setText(counter);
        }
    }

    private void deleteAlert(final Photo photo, boolean isVideo) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_item);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
        }

        TextView title = dialog.findViewById(R.id.tvDeleteTitle);
        TextView message = dialog.findViewById(R.id.tvDeleteMessage);
        TextView btnDelete = dialog.findViewById(R.id.btnDelete);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        if (isVideo) {
            title.setText(getString(R.string.delete_this_video));
            message.setText(getString(R.string.delete_video_message));
        } else {
            title.setText(getString(R.string.delete_this_image));
            message.setText(getString(R.string.delete_image_message));
        }

        btnDelete.setOnClickListener(v -> {
            List<Photo> photoListToDelete = new ArrayList<>();
            photoListToDelete.add(photo);
            delete(photoListToDelete);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void delete(List<Photo> list) {
        viewModel.deletePhotos(list).observe(this, result -> {
            if (result != null && result > 0) {
                // Remove from current list
                Photo deletedPhoto = list.get(0);
                int deletedPosition = photoList.indexOf(deletedPhoto);

                if (deletedPosition != -1) {
                    photoList.remove(deletedPosition);
                    galleryAdapter.updatePhotoList(photoList);

                    if (photoList.isEmpty()) {
                        // No more photos, go back
                        onBackPressed();
                        return;
                    }

                    // Adjust current position if necessary
                    if (currentPosition >= photoList.size()) {
                        currentPosition = photoList.size() - 1;
                    }

                    binding.viewPager.setCurrentItem(currentPosition, false);
                    updateCounter();
                }

                boolean isVideo = deletedPhoto.getImagePath().endsWith(".mp4");
                String message = isVideo ? "Video deleted successfully" : "Photo deleted successfully";
                Toast.makeText(PhotoGallery_Activity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PhotoGallery_Activity.this, "Failed to delete photo/video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void share(Photo photo) {
        if (photo.getImagePath().endsWith(".mp4")) {
            shareVideoWithStamp(photo);
        } else {
            shareImageWithStamp(photo);
        }
    }

    private void shareImageWithStamp(Photo photo) {
        // Get current fragment and share its content
        PhotoGalleryAdapter.PhotoFragment currentFragment = galleryAdapter.getCurrentFragment(currentPosition);
        if (currentFragment != null) {
            currentFragment.shareImageWithStamp();
        }
    }

    private void shareVideoWithStamp(Photo photo) {
        // Similar to shareImageWithStamp but for videos
        PhotoGalleryAdapter.PhotoFragment currentFragment = galleryAdapter.getCurrentFragment(currentPosition);
        if (currentFragment != null) {
            currentFragment.shareVideoWithStamp();
        }
    }

    private void toggleUI() {
        if (isUIVisible) {
            hideUI();
        } else {
            showUI();
        }
    }

    private void hideUI() {
        binding.header.setVisibility(View.GONE);
        binding.buttonContainer.setVisibility(View.GONE);
        binding.view.setVisibility(View.GONE);
        isUIVisible = false;

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void showUI() {
        binding.header.setVisibility(View.VISIBLE);
        binding.buttonContainer.setVisibility(View.VISIBLE);
        binding.view.setVisibility(View.VISIBLE);
        isUIVisible = true;

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onBackPressed() {
        if (!isUIVisible) {
            showUI();
            return;
        }

        boolean fromMap = getIntent().getBooleanExtra("fromMap", false);
        if (fromMap) {
            startActivity(new Intent(this, Map_Activity.class));
        } else {
            Intent intent = new Intent(this, MyCreation_Activity.class);
            // Determine if current photo is video to set correct tab
            if (currentPosition < photoList.size()) {
                Photo currentPhoto = photoList.get(currentPosition);
                boolean isVideo = currentPhoto.getImagePath().endsWith(".mp4");
                intent.putExtra("selectedTab", isVideo ? 1 : 0);
            }
            startActivity(intent);
        }
        finish();
    }
}