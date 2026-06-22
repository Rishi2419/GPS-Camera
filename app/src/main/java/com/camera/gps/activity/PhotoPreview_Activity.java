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

import com.appizona.yehiahd.fastsave.FastSave;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
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
import com.camera.gps.util.Constant;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.VideoStampShareHelper;

public final class PhotoPreview_Activity extends AppCompatActivity {

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
    private Dialog dialog;


    private LinearLayout dateTimeContainer, latLongContainer;
    private TextView lbl_lat, lbl_long, lbl_date, lbl_gmt, lbl_type, lbl_degree, lbl_dms;
    private TextView txtLocation, txtDateTime, txtLatitude, txtLongitude, txtDate, txtTime, txtTitle, txt_lat_dms, txt_long_dms;
    String current_address = "Loading...";
    HelperClass mHelperClass = new HelperClass();
    private ActivityPhotoPreviewBinding binding;
    private GlobalViewModel viewModel;
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
        boolean fromCreation = getIntent().getBooleanExtra("fromCreation", false);
        if (photo.getImagePath().endsWith(".mp4")) {
            isVideo = true;
            binding.videoView.setVisibility(VISIBLE);
            isVideo = true;
            binding.imageview.setVisibility(GONE);
            binding.videoView.setMediaController(new MediaController(this));
            binding.videoView.setVideoURI(Uri.parse(photo.getImagePath()));
            binding.videoView.start();
        } else if (photo.getImagePath().endsWith(".jpeg")) {
            binding.videoView.setVisibility(GONE);
            binding.imageview.setVisibility(VISIBLE);
            RequestBuilder<Drawable> load = Glide.with(this).load(photo.getImagePath());
            load.into(binding.imageview);
        }

        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });
        binding.btnDelete.setOnClickListener(view -> deleteAlert(photo, isVideo));
        binding.btnShare.setOnClickListener(view -> share(photo));
        initCardInfoVisibility(photo);
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
        viewModel.deletePhotos(list).observe(this, result -> {
            if (result != null && result > 0 && isVideo) {
                Toast.makeText(PhotoPreview_Activity.this, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else if (result != null && result > 0) {
                Toast.makeText(PhotoPreview_Activity.this, "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                Toast.makeText(PhotoPreview_Activity.this, "Failed to delete photo/video", Toast.LENGTH_SHORT).show();
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
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                googleMap.snapshot(mapSnapshot -> {
                    try {
                        // Get the main container view that includes the image and stamp
                        View containerView = binding.previewFrame;

                        // Create bitmap from the container
                        Bitmap containerBitmap = Bitmap.createBitmap(containerView.getWidth(), containerView.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(containerBitmap);

                        // Draw the container (this includes the image and stamp layout)
                        containerView.draw(canvas);

                        // Now overlay the map snapshot at the correct position
                        if (mapSnapshot != null && mapViewContainer != null) {
                            // Get map view position relative to the container
                            int[] mapLocation = new int[2];
                            int[] containerLocation = new int[2];

                            mapViewContainer.getLocationInWindow(mapLocation);
                            containerView.getLocationInWindow(containerLocation);

                            // Calculate relative position
                            int relativeX = mapLocation[0] - containerLocation[0];
                            int relativeY = mapLocation[1] - containerLocation[1];

                            // Scale the map snapshot to match the mapView size
                            Bitmap scaledMapSnapshot = Bitmap.createScaledBitmap(mapSnapshot, mapViewContainer.getWidth(), mapViewContainer.getHeight(), false);

                            // Draw the map at the correct position
                            float cornerRadius = 0f; // e.g. 30px, or convert dp -> px
                            Bitmap roundedMap = getRoundedCornerBitmap(scaledMapSnapshot, cornerRadius);

                            canvas.drawBitmap(roundedMap, relativeX, relativeY, null);
                        }

                        // Save to temporary file
                        File sharedFile = new File(getCacheDir(), "shared_image_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(sharedFile);
                        containerBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.close();

                        // Share the image
                        shareFile(sharedFile, "image/jpeg", "Share Image!");

                    } catch (Exception e) {
                        e.printStackTrace();
                        Constant.Companion.showToast(this, getResources().getString(R.string.error_in_creating_file));
                    }
                });
            });
        } else {
            // Fallback if no map - just share the container view
            try {
                Bitmap containerBitmap = viewToImage(binding.previewFrame);
                File sharedFile = new File(getCacheDir(), "shared_image_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = new FileOutputStream(sharedFile);
                containerBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                shareFile(sharedFile, "image/jpeg", "Share Image!");
            } catch (Exception e) {
                e.printStackTrace();
                Constant.Companion.showToast(this, getResources().getString(R.string.error_in_creating_file));
            }
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

            canvas.drawBitmap(scaledMapSnapshot, relativeX, relativeY, null);
        }

        return stampBitmap;
    }


    private void shareFile(File file, String mimeType, String chooserTitle) {
        Uri uriForFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + "\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, chooserTitle));
    }

    private Bitmap viewToImage(View view) {
        // Ensure view is properly measured and laid out
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
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

    private Bitmap getPhoto() {
        try {
            return viewToImage(binding.previewFrame);
        } catch (Exception e) {
            return null;
        }
    }


    private void shareVideoWithStamp(Photo photo) {
        try {
            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    googleMap.snapshot(mapSnapshot -> {
                        try {
                            Bitmap stampBitmap = createStampOverlay(mapSnapshot);
                            VideoStampShareHelper.shareVideoWithStamp(this, photo, stampBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            shareVideoOnly(photo);
                        }
                    });
                });
            } else {
                Bitmap stampBitmap = viewToImage(relBottomStamp);
                VideoStampShareHelper.shareVideoWithStamp(this, photo, stampBitmap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            shareVideoOnly(photo);
        }
    }

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
        fontStyle = photo.getFontStyle();
        title = photo.getTitle();
        current_map_type = photo.getMap_type();
        show_watermark = photo.getShow_watermark();
        currentstamp_type = photo.getType();
        lat_dms = photo.getLat_dms();
        long_dms = photo.getLong_dms();
        current_bg_color = photo.getCurrent_bg_color();
        current_text_color = photo.getCurrent_text_color();
        current_datetime_color = photo.getCurrent_datetime_color();
        currentRatioType = photo.getRatio();

        Log.d("Rishi", "Details:" + current_address + currentLatitude + currentLongitude + date + time + fontStyle + title);
        setRatio(currentRatioType);
        setCurrentStampLayout(currentstamp_type);
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
                //16:9 - if video
                if (isVideo) {
                    height16_9 = (screenWidth * 16) / 9;
                    frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.height = height16_9;

                    if (height16_9 >= screenHeight - 200) {
                        // Large 16:9 - treat as full screen
                        frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                        frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                        frameParams.verticalBias = 0.5f;
                        setupFullScreenMode();
                    } else {
                        // Normal 16:9 - center between available space
                        frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                        frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                        frameParams.verticalBias = 0.5f;
                        showUI();
                        // Remove click listener for normal mode
                        binding.previewFrame.setOnClickListener(null);
                    }
                    updateMediaViewFor16_9(height16_9);
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

                if (height16_9 >= screenHeight - 200) {
                    // Large 16:9 - treat as full screen
                    frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.verticalBias = 0.5f;
                    setupFullScreenMode();
                } else {
                    // Normal 16:9 - center between available space
                    frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    frameParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    frameParams.verticalBias = 0.5f;
                    showUI();
                    // Remove click listener for normal mode
                    binding.previewFrame.setOnClickListener(null);
                }
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

                showUI();
                // Remove click listener for normal mode
                binding.previewFrame.setOnClickListener(null);
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
        binding.view.setVisibility(View.GONE);
        isUIVisible = false;

        // Make status bar and navigation bar transparent/hidden for immersive experience
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showUI() {
        binding.header.setVisibility(View.VISIBLE);
        binding.buttonContainer.setVisibility(View.VISIBLE);
        binding.view.setVisibility(View.VISIBLE);
        isUIVisible = true;

        // Restore system UI
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
            FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            beginTransaction.add(mapViewContainer.getId(), mapFragment);
            beginTransaction.commit();

            try {
                MapsInitializer.initialize(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateStampContent();
    }

    public void updateStampContent() {

        if (stampBg != null) {

            if (currentstamp_type == 9) {
                dateTimeContainer.setBackgroundColor(current_bg_color);
                latLongContainer.setBackgroundColor(current_bg_color);
                txtTitle.setBackgroundColor(current_bg_color);
                txtLocation.setBackgroundColor(current_bg_color);
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
        if (mapFragment != null && currentLatitude != 0.0 && currentLongitude != 0.0) {
            mapFragment.getMapAsync(googleMap -> {
                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Photo Location"));
                Log.d("Rishi_map_type", "Map" + current_map_type);
                googleMap.setMapType(current_map_type);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            });
        }
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
}
