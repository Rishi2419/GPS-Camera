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

import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.databinding.ActivityPhotoPreviewBinding;
import com.camera.gps.util.Utils;
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
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.model.StampTemplateDefaults;
import com.camera.gps.util.Constant;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SharedMediaStore;
import com.camera.gps.util.StampBackgroundUtils;
import com.camera.gps.util.StampedPhotoShareHelper;
import com.camera.gps.util.VideoStampShareHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class PhotoPreview_Activity extends AppCompatActivity {

    private static final int STABLE_PREVIEW_SYSTEM_UI =
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    private static final float MAP_CORNER_RADIUS_DP = 4f;

    boolean isVideo = false;
    double currentLatitude;
    double currentLongitude;
    int current_bg_color;
    int current_text_color;
    int current_datetime_color;
    int current_map_type;
    int currentstamp_type;
    int currentRatioType;
    boolean show_watermark;

    String format_Combined;
    String format_Date;
    String format_Time;
    SupportMapFragment mapFragment;
    LinearLayout mapViewContainer;
    ImageView staticMapImage;
    RelativeLayout relBottomStamp;
    int height16_9;
    private SupportMapFragment supportMapFragment;
    CardView stampBg;
    ConstraintLayout appStamp;
    String date;
    String time;
    String title;
    String lat_dms;
    String long_dms;
    String mapImagePath;
    private Dialog dialog;
    private final Handler videoProgressHandler = new Handler(Looper.getMainLooper());
    private final Runnable videoProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateVideoPlaybackPill();
            if (isVideo && binding != null && binding.videoView.isPlaying()) {
                videoProgressHandler.postDelayed(this, 500);
            }
        }
    };


    private LinearLayout dateTimeContainer, latLongContainer;
    private TextView lbl_lat, lbl_long, lbl_date, lbl_gmt, lbl_type, lbl_degree, lbl_dms;
    private TextView txtLocation, txtDateTime, txtLatitude, txtLongitude, txtDate, txtTime, txtTitle, txt_lat_dms, txt_long_dms;
    String current_address = "Loading...";
    HelperClass mHelperClass = new HelperClass();
    private ActivityPhotoPreviewBinding binding;
    private GlobalViewModel viewModel;
    private Photo displayedPhoto;
    public String fontStyle;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = ActivityPhotoPreviewBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        Application application = getApplication();

        getWindow().setFlags(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        relBottomStamp = findViewById(R.id.gallery_rel_bottom_stamp);

        viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(application)).get(GlobalViewModel.class);
        Serializable serializableExtra = getIntent().getSerializableExtra("model");


        final Photo photo = (Photo) serializableExtra;
        displayedPhoto = photo;
        boolean fromCreation = getIntent().getBooleanExtra("fromCreation", false);
        if (SharedMediaStore.isVideo(photo)) {
            isVideo = true;
            binding.videoView.setVisibility(VISIBLE);
            isVideo = true;
            binding.imageview.setVisibility(GONE);
            setupVideoPreview(SharedMediaStore.getContentUri(
                    this, photo.getMediaUri(), photo.getImagePath()));
        } else {
            binding.videoView.setVisibility(GONE);
            binding.videoPlaybackPill.setVisibility(GONE);
            binding.imageview.setVisibility(VISIBLE);
            RequestBuilder<Drawable> load = Glide.with(this)
                    .load(SharedMediaStore.getLoadSource(photo));
            load.into(binding.imageview);
        }

        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });
        binding.btnDelete.setOnClickListener(view -> deleteAlert(photo, isVideo));
        binding.btnShare.setOnClickListener(view -> share(photo));
        initCardInfoVisibility(photo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (displayedPhoto == null) {
            return;
        }
        if (!SharedMediaStore.exists(this,
                displayedPhoto.getMediaUri(), displayedPhoto.getImagePath())) {
            SharedMediaStore.delete(this, displayedPhoto);
            List<Photo> missing = new ArrayList<>();
            missing.add(displayedPhoto);
            viewModel.deletePhotos(missing);
            setDeletedResult(displayedPhoto);
            finish();
        } else {
            displayedPhoto.setImagePath(SharedMediaStore.resolveCurrentPath(
                    this, displayedPhoto.getMediaUri(), displayedPhoto.getImagePath()));
        }
    }

    private void setupVideoPreview(Uri videoUri) {
        binding.videoPlaybackPill.setVisibility(VISIBLE);
        binding.videoCurrentTime.setText(formatVideoTime(0));
        binding.videoDuration.setText(formatVideoTime(0));
        binding.videoPlayPauseIcon.setImageResource(R.drawable.ic_video_play);
        binding.videoView.setVideoURI(videoUri);
        binding.videoView.setOnClickListener(v -> toggleUI());
        binding.videoPlaybackPill.setOnClickListener(v -> toggleVideoPlayback());
        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            binding.videoView.seekTo(1);
            binding.videoView.start();
            videoProgressHandler.removeCallbacks(videoProgressRunnable);
            videoProgressHandler.post(videoProgressRunnable);
            updateVideoPlaybackPill();
        });
        binding.videoView.setOnCompletionListener(mediaPlayer -> {
            videoProgressHandler.removeCallbacks(videoProgressRunnable);
            binding.videoView.seekTo(0);
            updateVideoPlaybackPill();
        });
    }

    private void toggleVideoPlayback() {
        if (!isVideo) {
            return;
        }

        if (binding.videoView.isPlaying()) {
            binding.videoView.pause();
            videoProgressHandler.removeCallbacks(videoProgressRunnable);
        } else {
            binding.videoView.start();
            videoProgressHandler.removeCallbacks(videoProgressRunnable);
            videoProgressHandler.post(videoProgressRunnable);
        }
        updateVideoPlaybackPill();
    }

    private void updateVideoPlaybackPill() {
        if (!isVideo || binding == null) {
            return;
        }

        int currentPosition = Math.max(binding.videoView.getCurrentPosition(), 0);
        int duration = Math.max(binding.videoView.getDuration(), 0);
        binding.videoCurrentTime.setText(formatVideoTime(currentPosition));
        binding.videoDuration.setText(formatVideoTime(duration));
        binding.videoPlayPauseIcon.setImageResource(
                binding.videoView.isPlaying() ? R.drawable.ic_video_pause : R.drawable.ic_video_play
        );
    }

    private String formatVideoTime(int milliseconds) {
        int totalSeconds = Math.max(milliseconds, 0) / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void deleteAlert(final Photo photo, boolean isVideo) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new Dialog(this);
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

        // Dynamically set text based on type
        if (isVideo) {
            title.setText(getString(R.string.delete_this_video));
            message.setText(getString(R.string.delete_video_message));
        } else {
            title.setText(getString(R.string.delete_this_image));
            message.setText(getString(R.string.delete_image_message));
        }

        btnDelete.setOnClickListener(v -> {
            List<Photo> photoList = new ArrayList<>();
            photoList.add(photo);
            delete(photoList);
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
            if (result != null && result > 0 && isVideo) {
                Toast.makeText(PhotoPreview_Activity.this, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                setDeletedResult(list.get(0));
                onBackPressed();
            } else if (result != null && result > 0) {
                Toast.makeText(PhotoPreview_Activity.this, "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                setDeletedResult(list.get(0));
                onBackPressed();
            } else {
                Toast.makeText(PhotoPreview_Activity.this, "Failed to delete photo/video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDeletedResult(Photo photo) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("previewDeleted", true);
        if (photo != null) {
            Integer photoId = photo.getId();
            resultIntent.putExtra("deletedPhotoId", photoId != null ? photoId : -1);
            resultIntent.putExtra("deletedPhotoPath", photo.getImagePath());
        }
        setResult(RESULT_OK, resultIntent);
    }

    private void share(Photo photo) {
        if (SharedMediaStore.isVideo(photo)) {
            shareVideoWithStamp(photo);
        } else {
            shareImageWithStamp(photo);
        }
    }


    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, float cornerRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);


        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        // Draw rounded rectangle
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        // Use SRC_IN mode to mask the bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void shareImageWithStamp(Photo photo) {
        if (!SharedMediaStore.share(this, photo)) {
            Toast.makeText(this, R.string.error_in_creating_file, Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap createStampOverlay(Bitmap mapSnapshot) {
        // Create a bitmap that contains just the stamp information
        View stampContainer = relBottomStamp;

        Bitmap stampBitmap = Bitmap.createBitmap(stampContainer.getWidth(), stampContainer.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(stampBitmap);

        // Draw the stamp container
        stampContainer.draw(canvas);

        // Overlay the map if available
        if (mapSnapshot != null && mapViewContainer != null) {
            int[] mapLocation = new int[2];
            int[] stampLocation = new int[2];

            mapViewContainer.getLocationInWindow(mapLocation);
            stampContainer.getLocationInWindow(stampLocation);

            int relativeX = mapLocation[0] - stampLocation[0];
            int relativeY = mapLocation[1] - stampLocation[1];

            Bitmap scaledMapSnapshot = Bitmap.createScaledBitmap(mapSnapshot, mapViewContainer.getWidth(), mapViewContainer.getHeight(), false);
            Bitmap roundedMap = getRoundedCornerBitmap(scaledMapSnapshot, getMapCornerRadiusPx());

            canvas.drawBitmap(roundedMap, relativeX, relativeY, null);
        }

        return stampBitmap;
    }


    private void shareFile(File file, String mimeType, String chooserTitle) {
        Uri uriForFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        // App link text is disabled until the app is published.
        // intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + "\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, chooserTitle));
    }

    private Bitmap viewToImage(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();
        }

        Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);

        Drawable background = view.getBackground();
        if (background != null) {
            background.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        view.draw(canvas);
        return returnedBitmap;
    }

    private float getMapCornerRadiusPx() {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                MAP_CORNER_RADIUS_DP,
                getResources().getDisplayMetrics()
        );
    }

    private Bitmap getPhoto() {
        try {
            return viewToImage(binding.previewFrame);
        } catch (Exception e) {
            return null;
        }
    }


    private void shareVideoWithStamp(Photo photo) {
        shareVideoOnly(photo);
    }

    @androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    private void shareVideoOnly(Photo photo) {
        VideoStampShareHelper.shareVideoOnly(this, photo);
    }

    @SuppressLint("SetTextI18n")
    private void initCardInfoVisibility(Photo photo) {
        // Use stored photo data, not current live data
        String address = photo.getAddress();
        if (address == null || address.isEmpty()) {
            address = "Unknown Location";
        }

        current_address = address;
        currentLatitude = Double.parseDouble(photo.getLatitude());
        currentLongitude = Double.parseDouble(photo.getLongitude());
        date = photo.getDate();
        time = photo.getTime();
        title = photo.getTitle();
        Integer storedStampType = photo.getType();
        currentstamp_type = storedStampType != null ? storedStampType : 1;
        StampTemplateDefaults.Settings templateDefaults =
                StampTemplateDefaults.forTemplate(this, currentstamp_type);

        String storedFontStyle = photo.getFontStyle();
        fontStyle = storedFontStyle == null || storedFontStyle.trim().isEmpty()
                ? templateDefaults.fontStyle
                : storedFontStyle;

        Integer storedMapType = photo.getMap_type();
        current_map_type = storedMapType != null
                ? storedMapType
                : templateDefaults.mapType;
        mapImagePath = photo.getMapImagePath();
        show_watermark = Boolean.TRUE.equals(photo.getShow_watermark());
        lat_dms = photo.getLat_dms();
        long_dms = photo.getLong_dms();
        current_bg_color = photo.getCurrent_bg_color();
        current_text_color = photo.getCurrent_text_color();
        current_datetime_color = photo.getCurrent_datetime_color();
        currentRatioType = photo.getRatio();

        updatePreviewDateTime(photo);
        Log.d("Rishi", "Details:" + current_address + currentLatitude + currentLongitude + date + time + fontStyle + title);
        setRatio(currentRatioType);
        // The shared Gallery file already contains its rendered stamp.
        relBottomStamp.removeAllViews();
        relBottomStamp.setVisibility(GONE);
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

    private boolean isUIVisible = true;

    private void setRatio(int ratio) {
        if (binding.previewFrame == null) return;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        ConstraintLayout.LayoutParams frameParams = (ConstraintLayout.LayoutParams) binding.previewFrame.getLayoutParams();

        switch (ratio) {
            case 0:
                if (isVideo) {
                    frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.verticalBias = 0.5f;

                    setupFullScreenMode();
                    updateMediaViewForFullScreen();
                    break;

                } else {
                    // Full screen  - if image
                    frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

                    // Clear any other constraints that might interfere
                    frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.verticalBias = 0.5f;

                    setupFullScreenMode();
                    updateMediaViewForFullScreen();
                    break;
                }


            case 1: // 16:9 aspect ratio
                height16_9 = (screenWidth * 16) / 9;
                frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                frameParams.height = height16_9;
                frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                frameParams.verticalBias = 0.5f;

                setupFullScreenMode();
                updateMediaViewFor16_9(height16_9);
                break;

            case 2: // 4:3 aspect ratio
            default:
                int height4_3 = (screenWidth * 4) / 3;
                frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                frameParams.height = height4_3;

                // Center the frame in available space
                frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                frameParams.verticalBias = 0.5f;

                setupFullScreenMode();
                updateMediaViewFor4_3(height4_3);
                break;
        }

        binding.previewFrame.setLayoutParams(frameParams);
    }

    private void setupFullScreenMode() {
        // Initially hide UI for full screen
        hideUI();
        // Set click listener to toggle UI
        binding.previewFrame.setOnClickListener(v -> toggleUI());
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
        binding.videoPlaybackPill.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        binding.view.setVisibility(View.VISIBLE);
        isUIVisible = true;

        getWindow().getDecorView().setSystemUiVisibility(STABLE_PREVIEW_SYSTEM_UI);
    }

    private void updateMediaViewForFullScreen() {
        // Update ImageView
        ViewGroup.LayoutParams imageParams = binding.imageview.getLayoutParams();
        imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.imageview.setLayoutParams(imageParams);
        binding.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Update VideoView
        ViewGroup.LayoutParams videoParams = binding.videoView.getLayoutParams();
        videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.videoView.setLayoutParams(videoParams);
    }

    private void updateMediaViewFor16_9(int height) {
        // Update ImageView
        ViewGroup.LayoutParams imageParams = binding.imageview.getLayoutParams();
        imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageParams.height = height;
        binding.imageview.setLayoutParams(imageParams);
        binding.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Update VideoView
        ViewGroup.LayoutParams videoParams = binding.videoView.getLayoutParams();
        videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoParams.height = height;
        binding.videoView.setLayoutParams(videoParams);
    }

    private void updateMediaViewFor4_3(int height) {
        // Update ImageView
        ViewGroup.LayoutParams imageParams = binding.imageview.getLayoutParams();
        imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageParams.height = height;
        binding.imageview.setLayoutParams(imageParams);
        binding.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Update VideoView
        ViewGroup.LayoutParams videoParams = binding.videoView.getLayoutParams();
        videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoParams.height = height;
        binding.videoView.setLayoutParams(videoParams);
    }


    public void setCurrentStampLayout(Integer type) {
        relBottomStamp.removeAllViews();
        int layoutResId;
        switch (type) {
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

        View stampView = getLayoutInflater().inflate(layoutResId, relBottomStamp, false);
        relBottomStamp.addView(stampView);
        initializeStampViews(stampView);
        if (mapViewContainer != null) {
            setupStaticMapImage();
        }
        updateStampContent();
    }

    private void setupStaticMapImage() {
        mapFragment = null;
        mapViewContainer.removeAllViews();
        mapViewContainer.setBackgroundColor(Color.TRANSPARENT);
        staticMapImage = new ImageView(this);
        staticMapImage.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        staticMapImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mapViewContainer.addView(staticMapImage);
    }

    public void updateStampContent() {

        if (stampBg != null) {

            if (currentstamp_type == 9) {
                StampBackgroundUtils.applyRoundedGlassColor(
                        this,
                        current_bg_color,
                        dateTimeContainer,
                        latLongContainer,
                        txtTitle,
                        txtLocation
                );
                StampBackgroundUtils.applyRoundedDefaultGlass(this, appStamp);
            }else {
                stampBg.setCardBackgroundColor(current_bg_color);
            }
        }


        updateStampLocation();
        updateStampCoordinates();
        updateStampDateTime();
        updateMapLocation();
        updateStampTitle();
        updateWaterMarkVisibility();
        if (currentstamp_type == 2 || currentstamp_type == 3 || currentstamp_type == 6 || currentstamp_type == 7) {
            updateStampDMS();
        }
    }

    private void updateStampDMS() {
        if (txt_lat_dms != null && txt_long_dms != null) {
            txt_lat_dms.setText(lat_dms);
            txt_lat_dms.setTextColor(current_text_color);
            txt_lat_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));

            txt_long_dms.setText(long_dms);
            txt_long_dms.setTextColor(current_text_color);
            txt_long_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));

            lbl_dms.setTextColor(current_text_color);
            lbl_dms.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }
    }

    private void updateStampTitle() {
        if (txtTitle != null) {
            if (title == null || title.isEmpty()) {
                txtTitle.setVisibility(GONE);
            } else {
                txtTitle.setVisibility(VISIBLE);
                txtTitle.setText(title);
                txtTitle.setTextColor(current_text_color);
                txtTitle.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            }
        }
    }

    private void updateStampDateTime() {
        if (txtDate != null) {
            txtDate.setText(date);
            txtDate.setTextColor(current_datetime_color);
            lbl_date.setTextColor(current_datetime_color);
            txtDate.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            lbl_date.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }


        if (time == null || time.isEmpty()) {
            lbl_gmt.setVisibility(View.GONE);
            txtTime.setVisibility(GONE);
        } else {
            txtTime.setText(time);
            txtTime.setTextColor(current_datetime_color);
            lbl_gmt.setTextColor(current_datetime_color);
            txtTime.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            lbl_gmt.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }


    }

    private void updateStampLocation() {
        if (txtLocation != null) {
            txtLocation.setText(current_address);
            txtLocation.setTextColor(current_text_color);
            txtLocation.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
        }
    }

    private void updateWaterMarkVisibility() {
        if (show_watermark) {
            appStamp.setVisibility(View.VISIBLE);
        } else {
            appStamp.setVisibility(View.INVISIBLE);
        }
    }

    private void updateMapLocation() {
        if (staticMapImage != null) {
            if (mapImagePath != null && !mapImagePath.isEmpty() && new File(mapImagePath).exists()) {
                Glide.with(this)
                        .load(mapImagePath)
                        .apply(mapCornerOptions())
                        .placeholder(R.drawable.default_map)
                        .error(R.drawable.default_map)
                        .into(staticMapImage);
                return;
            }

            Glide.with(this)
                    .load(R.drawable.default_map)
                    .apply(mapCornerOptions())
                    .placeholder(R.drawable.default_map)
                    .error(R.drawable.default_map)
                    .into(staticMapImage);
        }
    }

    private RequestOptions mapCornerOptions() {
        return RequestOptions.bitmapTransform(
                new com.bumptech.glide.load.resource.bitmap.RoundedCorners((int) getMapCornerRadiusPx())
        );
    }

    private void updateStampCoordinates() {
        if (txtLatitude != null && txtLongitude != null) {

            // Determine N/S for latitude
            String latDirection = (currentLatitude >= 0) ? "N" : "S";

            // Determine E/W for longitude
            String lonDirection = (currentLongitude >= 0) ? "E" : "W";

            // Use absolute value to avoid negative sign with letters
            txtLatitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLatitude), latDirection));
            lbl_lat.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txtLatitude.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txtLongitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLongitude), lonDirection));
            lbl_long.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
            txtLongitude.setTypeface(mHelperClass.getFontStyle(this, fontStyle));

            txtLatitude.setTextColor(current_text_color);
            txtLongitude.setTextColor(current_text_color);
            lbl_lat.setTextColor(current_text_color);
            lbl_long.setTextColor(current_text_color);

            if (currentstamp_type == 2 || currentstamp_type == 3 || currentstamp_type == 6 || currentstamp_type == 7) {
                lbl_type.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
                lbl_degree.setTypeface(mHelperClass.getFontStyle(this, fontStyle));
                lbl_type.setTextColor(current_text_color);
                lbl_degree.setTextColor(current_text_color);
            }
        }
    }

    private void initializeStampViews(View stampView) {
        mapViewContainer = stampView.findViewById(R.id.mapView);
        txtLocation = stampView.findViewById(R.id.txt_gps_stamp_location);
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
    }


    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        // Check if we're in full screen mode and UI is hidden
        if ((currentRatioType == 0 || isLargeRatio()) && !isUIVisible) {
            // If UI is hidden in full screen mode, show it first instead of going back
            showUI();
            return;
        }

        // Original back button logic
        boolean fromMap = getIntent().getBooleanExtra("fromMap", false);
        if (fromMap) {
            startActivity(new Intent(this, Map_Activity.class));
            finish();
        } else if (getIntent().getBooleanExtra("isMain", false)) {
            finish();
        } else {
            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "previewgallery_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        myCreationNavigation();
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        Utils.LogUtils.logE("MyCreation", "Ad failed: " + errorMsg);
                    });
                } else {
                    Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                myCreationNavigation();
            }
        }
    }

    private void myCreationNavigation() {
        Intent intent = new Intent(this, MyCreation_Activity.class);
        // Pass the current tab information back
        if (isVideo) {
            intent.putExtra("selectedTab", 1); // Video tab
        } else {
            intent.putExtra("selectedTab", 0); // Photo tab
        }
        startActivity(intent);
        finish();
    }

    // Helper method to check if current ratio results in large display
    private boolean isLargeRatio() {
        if (currentRatioType == 1) { // 16:9
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            int height16_9 = (screenWidth * 16) / 9;
            return height16_9 >= screenHeight - 200;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideo && binding != null && binding.videoView.isPlaying()) {
            binding.videoView.pause();
            updateVideoPlaybackPill();
        }
        videoProgressHandler.removeCallbacks(videoProgressRunnable);
    }

    @Override
    protected void onDestroy() {
        videoProgressHandler.removeCallbacks(videoProgressRunnable);
        super.onDestroy();
    }
}
