package com.camera.gps.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

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
import android.os.Handler;
import android.os.Looper;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
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
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.admob.AdMobBannerAdHelper;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.util.Constant;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SharedMediaStore;
import com.camera.gps.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class PhotoGallery_Activity extends AppCompatActivity {

    private static final int STABLE_PREVIEW_SYSTEM_UI =
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    private ActivityPhotoGalleryBinding binding;
    private GlobalViewModel viewModel;
    private PhotoGalleryAdapter galleryAdapter;
    private List<Photo> photoList = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isUIVisible = true;
    private int previewsSinceLastInterstitial = 0;
    private boolean ignoreInitialPageSelection = true;
    private static final int PREVIEWS_BEFORE_INTERSTITIAL = 7;
    private final Handler videoProgressHandler = new Handler(Looper.getMainLooper());
    private final Runnable videoProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateVideoPlaybackPill();
            PhotoGalleryAdapter.PhotoFragment currentFragment = getCurrentGalleryFragment();
            if (currentFragment != null && currentFragment.isVideoPlaying()) {
                videoProgressHandler.postDelayed(this, 500);
            }
        }
    };

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = ActivityPhotoGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadBottomBannerAd();

        Application application = getApplication();
        getWindow().setFlags(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(STABLE_PREVIEW_SYSTEM_UI);

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

    private void loadBottomBannerAd() {
        if (!Utils.getIsPremium(this)) {
            binding.flPhotoGalleryBanner.post(() -> AdMobBannerAdHelper.loadBannerAd(
                    this, binding.flPhotoGalleryBanner, "photo_gallery_banner"));
        } else {
            binding.flPhotoGalleryBanner.setVisibility(GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (galleryAdapter == null || photoList.isEmpty()) {
            return;
        }

        List<Photo> missing = new ArrayList<>();
        for (Photo photo : new ArrayList<>(photoList)) {
            if (!SharedMediaStore.exists(this, photo.getMediaUri(), photo.getImagePath())) {
                SharedMediaStore.delete(this, photo);
                missing.add(photo);
                photoList.remove(photo);
            } else {
                photo.setImagePath(SharedMediaStore.resolveCurrentPath(
                        this, photo.getMediaUri(), photo.getImagePath()));
            }
        }

        if (missing.isEmpty()) {
            return;
        }
        viewModel.deletePhotos(missing);
        if (photoList.isEmpty()) {
            finish();
            return;
        }

        currentPosition = Math.min(currentPosition, photoList.size() - 1);
        galleryAdapter.updatePhotoList(photoList);
        binding.viewPager.setCurrentItem(currentPosition, false);
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
                updateVideoPlaybackPill();
                if (isPlaying) {
                    startVideoProgressUpdates();
                } else {
                    videoProgressHandler.removeCallbacks(videoProgressRunnable);
                }
            }
        });

        binding.viewPager.setAdapter(galleryAdapter);
        int offscreenLimit = Math.min(3, Math.max(1, photoList.size() - 1));
        binding.viewPager.setOffscreenPageLimit(offscreenLimit);
        RecyclerView pagerRecyclerView = (RecyclerView) binding.viewPager.getChildAt(0);
        if (pagerRecyclerView != null) {
            pagerRecyclerView.setItemViewCacheSize(offscreenLimit * 2 + 1);
            pagerRecyclerView.setHasFixedSize(true);
        }
        binding.viewPager.setCurrentItem(currentPosition, false);
        preloadAround(currentPosition);
        binding.viewPager.postDelayed(() -> preloadAround(currentPosition), 150);
        binding.viewPager.post(() -> {
            galleryAdapter.playOnly(currentPosition);
            updateVideoPlaybackPill();
            startVideoProgressUpdates();
        });


        // Listen for page changes to update counter and buttons
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateCounter();
                preloadAround(position);
                galleryAdapter.playOnly(position);
                updateVideoPlaybackPill();
                startVideoProgressUpdates();
                handlePreviewSwipeAd();
            }
        });
    }

    private void preloadAround(int position) {
        for (int i = Math.max(0, position - 2); i <= Math.min(photoList.size() - 1, position + 2); i++) {
            Photo item = photoList.get(i);
            preloadPath(item.getImagePath());
            preloadPath(item.getMapImagePath());
        }
    }

    private void preloadPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        Glide.with(this).load(path).preload();
    }

    private void handlePreviewSwipeAd() {
        if (ignoreInitialPageSelection) {
            ignoreInitialPageSelection = false;
            return;
        }

        previewsSinceLastInterstitial++;
        if (previewsSinceLastInterstitial < PREVIEWS_BEFORE_INTERSTITIAL) {
            return;
        }

        if (!MyApplication.isNetworkAvailable(this)
                || Utils.getIsPremium(this)
                || InterstitialAdManager.isInterstitialShowing()) {
            return;
        }

        previewsSinceLastInterstitial = 0;
        setInterstitialShowing(true);
        InterstitialAdManager.getInstance().loadAndShowInterstitialAd(
                this,
                "previewgallery_open_interstitial",
                () -> setInterstitialShowing(false),
                errorMsg -> {
                    setInterstitialShowing(false);
                    Utils.LogUtils.logE("PhotoGallerySwipe", "Ad failed: " + errorMsg);
                }
        );
    }

    private void setupClickListeners() {
        binding.btBack.setOnClickListener(view -> onBackPressed());

        binding.btnDelete.setOnClickListener(view -> {
            if (currentPosition < photoList.size()) {
                Photo currentPhoto = photoList.get(currentPosition);
                boolean isVideo = SharedMediaStore.isVideo(currentPhoto);
                deleteAlert(currentPhoto, isVideo);
            }
        });

        binding.btnShare.setOnClickListener(view -> {
            if (currentPosition < photoList.size()) {
                Photo currentPhoto = photoList.get(currentPosition);
                share(currentPhoto);
            }
        });

        binding.videoPlaybackPill.setOnClickListener(view -> toggleCurrentVideoPlayback());
    }

    private void updateCounter() {
        if (photoList.size() > 0) {
            String counter = (currentPosition + 1) + " of " + photoList.size();
            binding.tvCounter.setText(counter);
            updatePreviewDateTime(photoList.get(currentPosition));
        }
    }

    private void updatePreviewDateTime(Photo photo) {
        if (photo == null) {
            return;
        }

        Date capturedAt = parseCaptureDate(photo.getDateTimeTaken());
        if (capturedAt != null) {
            binding.tvPreviewDate.setText(new SimpleDateFormat("d MMMM, yyyy", Locale.getDefault()).format(capturedAt));
            binding.tvPreviewTime.setText(new SimpleDateFormat("hh.mm a", Locale.getDefault()).format(capturedAt));
        } else {
            binding.tvPreviewDate.setText(photo.getDate() != null ? photo.getDate() : "");
            binding.tvPreviewTime.setText(photo.getTime() != null ? photo.getTime() : "");
        }
    }

    private Date parseCaptureDate(String dateTimeTaken) {
        if (dateTimeTaken == null || dateTimeTaken.trim().isEmpty()) {
            return null;
        }

        try {
            return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).parse(dateTimeTaken);
        } catch (Exception e) {
            return null;
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
        Photo mediaToDelete = list.get(0);
        if (!SharedMediaStore.delete(this, mediaToDelete)) {
            Toast.makeText(this, "Unable to delete photo/video from Gallery",
                    Toast.LENGTH_SHORT).show();
            return;
        }

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

                boolean isVideo = SharedMediaStore.isVideo(deletedPhoto);
                String message = isVideo ? "Video deleted successfully" : "Photo deleted successfully";
                Toast.makeText(PhotoGallery_Activity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PhotoGallery_Activity.this, "Failed to delete photo/video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void share(Photo photo) {
        if (SharedMediaStore.isVideo(photo)) {
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
        binding.videoPlaybackPill.setVisibility(View.GONE);
        binding.view.setVisibility(View.GONE);
        isUIVisible = false;

        getWindow().getDecorView().setSystemUiVisibility(STABLE_PREVIEW_SYSTEM_UI);
    }

    private void showUI() {
        binding.header.setVisibility(View.VISIBLE);
        binding.buttonContainer.setVisibility(View.VISIBLE);
        binding.videoPlaybackPill.setVisibility(isCurrentPhotoVideo() ? View.VISIBLE : View.GONE);
        binding.view.setVisibility(View.VISIBLE);
        isUIVisible = true;

        getWindow().getDecorView().setSystemUiVisibility(STABLE_PREVIEW_SYSTEM_UI);
    }

    private void toggleCurrentVideoPlayback() {
        PhotoGalleryAdapter.PhotoFragment currentFragment = getCurrentGalleryFragment();
        if (currentFragment == null || !currentFragment.isVideo()) {
            return;
        }

        currentFragment.toggleVideoPlayback();
        updateVideoPlaybackPill();
        if (currentFragment.isVideoPlaying()) {
            startVideoProgressUpdates();
        } else {
            videoProgressHandler.removeCallbacks(videoProgressRunnable);
        }
    }

    private void updateVideoPlaybackPill() {
        if (binding == null) {
            return;
        }

        PhotoGalleryAdapter.PhotoFragment currentFragment = getCurrentGalleryFragment();
        boolean shouldShow = isUIVisible && currentFragment != null && currentFragment.isVideo();
        binding.videoPlaybackPill.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        if (!shouldShow) {
            return;
        }

        binding.videoCurrentTime.setText(formatVideoTime(currentFragment.getVideoCurrentPosition()));
        binding.videoDuration.setText(formatVideoTime(currentFragment.getVideoDuration()));
        binding.videoPlayPauseIcon.setImageResource(
                currentFragment.isVideoPlaying() ? R.drawable.ic_video_pause : R.drawable.ic_video_play
        );
    }

    private void startVideoProgressUpdates() {
        videoProgressHandler.removeCallbacks(videoProgressRunnable);
        PhotoGalleryAdapter.PhotoFragment currentFragment = getCurrentGalleryFragment();
        if (currentFragment != null && currentFragment.isVideoPlaying()) {
            videoProgressHandler.post(videoProgressRunnable);
        }
    }

    private PhotoGalleryAdapter.PhotoFragment getCurrentGalleryFragment() {
        if (galleryAdapter == null || currentPosition < 0 || currentPosition >= photoList.size()) {
            return null;
        }
        return galleryAdapter.getCurrentFragment(currentPosition);
    }

    private boolean isCurrentPhotoVideo() {
        return currentPosition >= 0
                && currentPosition < photoList.size()
                && SharedMediaStore.isVideo(photoList.get(currentPosition));
    }

    private String formatVideoTime(int milliseconds) {
        int totalSeconds = Math.max(milliseconds, 0) / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PhotoGalleryAdapter.PhotoFragment currentFragment = getCurrentGalleryFragment();
        if (currentFragment != null) {
            currentFragment.pauseVideoIfNeeded();
        }
        videoProgressHandler.removeCallbacks(videoProgressRunnable);
    }

    @Override
    protected void onDestroy() {
        videoProgressHandler.removeCallbacks(videoProgressRunnable);
        super.onDestroy();
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
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
                boolean isVideo = SharedMediaStore.isVideo(currentPhoto);
                intent.putExtra("selectedTab", isVideo ? 1 : 0);
            }
            startActivity(intent);
        }
        finish();
    }
}
