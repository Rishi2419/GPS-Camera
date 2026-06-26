package com.camera.gps.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.camera.gps.R;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.util.Constant;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.VideoStampShareHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoGalleryAdapter extends FragmentStateAdapter {

    private List<Photo> photoList;
    private OnPhotoInteractionListener listener;
    private SparseArray<PhotoFragment> fragmentList = new SparseArray<>();

    public interface OnPhotoInteractionListener {
        void onPhotoClick();

        void onVideoStatusChanged(boolean isPlaying);
    }

    public PhotoGalleryAdapter(@NonNull FragmentActivity fragmentActivity, List<Photo> photoList, OnPhotoInteractionListener listener) {
        super(fragmentActivity);
        this.photoList = new ArrayList<>(photoList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PhotoFragment fragment = PhotoFragment.newInstance(photoList.get(position), listener);
        fragmentList.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void updatePhotoList(List<Photo> newPhotoList) {
        this.photoList = new ArrayList<>(newPhotoList);
        fragmentList.clear();
        notifyDataSetChanged();
    }

    public PhotoFragment getCurrentFragment(int position) {
        return fragmentList.get(position);
    }

    public void playOnly(int position) {
        for (int i = 0; i < fragmentList.size(); i++) {
            int key = fragmentList.keyAt(i);
            PhotoFragment fragment = fragmentList.valueAt(i);
            if (fragment == null) {
                continue;
            }
            if (key == position) {
                fragment.playVideoIfNeeded();
            } else {
                fragment.pauseVideoIfNeeded();
            }
        }
    }

    public static class PhotoFragment extends Fragment {
        private static final String ARG_PHOTO = "photo";

        private Photo photo;
        private OnPhotoInteractionListener listener;

        // UI Components
        private FrameLayout previewFrame;
        private ShapeableImageView imageView;
        private VideoView videoView;
        private RelativeLayout stampContainer;

        // Stamp components
        private SupportMapFragment mapFragment;
        private LinearLayout mapViewContainer;
        private ImageView staticMapImage;
        private CardView stampBg;
        private ConstraintLayout appStamp;
        private TextView txtLocation, txtDateTime, txtLatitude, txtLongitude, txtDate, txtTime, txtTitle, txt_lat_dms, txt_long_dms;
        private TextView lbl_lat, lbl_long, lbl_date, lbl_gmt, lbl_type, lbl_degree, lbl_dms;

        // Photo data
        private boolean isVideo = false;
        private double currentLatitude;
        private double currentLongitude;
        private int current_bg_color;
        private int current_text_color;
        private int current_datetime_color;
        private int current_map_type;
        private int currentstamp_type;
        private int currentRatioType;
        private boolean show_watermark;
        private LinearLayout dateTimeContainer, latLongContainer;
        private String date, time, title, lat_dms, long_dms, mapImagePath, current_address = "Loading...", fontStyle;
        private HelperClass mHelperClass = new HelperClass();

        public static PhotoFragment newInstance(Photo photo, OnPhotoInteractionListener listener) {
            PhotoFragment fragment = new PhotoFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_PHOTO, photo);
            fragment.setArguments(args);
            fragment.listener = listener;
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                photo = (Photo) getArguments().getSerializable(ARG_PHOTO);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

            initViews(view);
            setupPhoto();
            setupClickListeners();

            return view;
        }

        private void initViews(View view) {
            previewFrame = view.findViewById(R.id.previewFrame);
            imageView = view.findViewById(R.id.imageview);
            videoView = view.findViewById(R.id.videoView);
            stampContainer = view.findViewById(R.id.gallery_rel_bottom_stamp);
        }

        private void setupPhoto() {
            if (photo == null) return;

            isVideo = photo.getImagePath().endsWith(".mp4");

            if (isVideo) {
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setMediaController(new MediaController(getContext()));
                videoView.setVideoURI(Uri.parse(photo.getImagePath()));
            } else {
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                if (getContext() != null) {
                    Glide.with(this).load(photo.getImagePath()).into(imageView);
                }
            }

            initPhotoData();
            setRatio(currentRatioType);
            setCurrentStampLayout(currentstamp_type);
        }

        private void setupClickListeners() {
            if (previewFrame != null && listener != null) {
                previewFrame.setOnClickListener(v -> listener.onPhotoClick());
            }
        }

        public void playVideoIfNeeded() {
            if (isVideo && videoView != null && !videoView.isPlaying()) {
                videoView.start();
            }
        }

        public void pauseVideoIfNeeded() {
            if (isVideo && videoView != null && videoView.isPlaying()) {
                videoView.pause();
            }
        }

        private void initPhotoData() {
            if (photo == null) return;

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
            mapImagePath = photo.getMapImagePath();
            show_watermark = photo.getShow_watermark();
            currentstamp_type = photo.getType();
            lat_dms = photo.getLat_dms();
            long_dms = photo.getLong_dms();
            current_bg_color = photo.getCurrent_bg_color();
            current_text_color = photo.getCurrent_text_color();
            current_datetime_color = photo.getCurrent_datetime_color();
            currentRatioType = photo.getRatio();
        }

        private void setRatio(int ratio) {
            if (previewFrame == null || getContext() == null) return;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            ConstraintLayout.LayoutParams frameParams = (ConstraintLayout.LayoutParams) previewFrame.getLayoutParams();

            switch (ratio) {
                case 0: // Full screen or 16:9 for video
                    if (isVideo) {
                        int height16_9 = (screenWidth * 16) / 9;
                        frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                        frameParams.height = height16_9;
                        updateMediaViewFor16_9(height16_9);
                    } else {
                        frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                        frameParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                        updateMediaViewForFullScreen();
                    }
                    break;

                case 1: // 16:9 aspect ratio
                    int height16_9 = (screenWidth * 16) / 9;
                    frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.height = height16_9;
                    updateMediaViewFor16_9(height16_9);
                    break;

                case 2: // 4:3 aspect ratio
                default:
                    int height4_3 = (screenWidth * 4) / 3;
                    frameParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                    frameParams.height = height4_3;
                    updateMediaViewFor4_3(height4_3);
                    break;
            }

            frameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            frameParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            frameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            frameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            frameParams.verticalBias = 0.5f;

            previewFrame.setLayoutParams(frameParams);
        }

        private void updateMediaViewForFullScreen() {
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imageParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ViewGroup.LayoutParams videoParams = videoView.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoView.setLayoutParams(videoParams);
        }

        private void updateMediaViewFor16_9(int height) {
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imageParams.height = height;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ViewGroup.LayoutParams videoParams = videoView.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.height = height;
            videoView.setLayoutParams(videoParams);
        }

        private void updateMediaViewFor4_3(int height) {
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imageParams.height = height;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ViewGroup.LayoutParams videoParams = videoView.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.height = height;
            videoView.setLayoutParams(videoParams);
        }

        public void setCurrentStampLayout(Integer type) {
            if (stampContainer == null) return;

            stampContainer.removeAllViews();
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

            View stampView = getLayoutInflater().inflate(layoutResId, stampContainer, false);
            stampContainer.addView(stampView);
            initializeStampViews(stampView);

            if (mapViewContainer != null && isAdded()) {
                setupStaticMapImage();
            }
            updateStampContent();
        }

        private void setupStaticMapImage() {
            mapFragment = null;
            mapViewContainer.removeAllViews();
            staticMapImage = new ImageView(requireContext());
            staticMapImage.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            staticMapImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mapViewContainer.addView(staticMapImage);
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

        private void updateStampLocation() {
            if (txtLocation != null) {
                txtLocation.setText(current_address);
                txtLocation.setTextColor(current_text_color);
                txtLocation.setTypeface(mHelperClass.getFontStyle(requireContext(), fontStyle));
            }
        }

        private void updateStampCoordinates() {
            if (txtLatitude != null && txtLongitude != null) {
                String latDirection = (currentLatitude >= 0) ? "N" : "S";
                String lonDirection = (currentLongitude >= 0) ? "E" : "W";

                txtLatitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLatitude), latDirection));
                txtLongitude.setText(String.format(Locale.getDefault(), "%.5f°%s", Math.abs(currentLongitude), lonDirection));

                if (getContext() != null) {
                    lbl_lat.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    txtLatitude.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    lbl_long.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    txtLongitude.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                }

                txtLatitude.setTextColor(current_text_color);
                txtLongitude.setTextColor(current_text_color);
                lbl_lat.setTextColor(current_text_color);
                lbl_long.setTextColor(current_text_color);

                if (currentstamp_type == 2 || currentstamp_type == 3 || currentstamp_type == 6 || currentstamp_type == 7) {
                    if (lbl_type != null && lbl_degree != null && getContext() != null) {
                        lbl_type.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                        lbl_degree.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                        lbl_type.setTextColor(current_text_color);
                        lbl_degree.setTextColor(current_text_color);
                    }
                }
            }
        }

        private void updateStampDateTime() {
            if (txtDate != null) {
                txtDate.setText(date);
                txtDate.setTextColor(current_datetime_color);
                lbl_date.setTextColor(current_datetime_color);
                if (getContext() != null) {
                    txtDate.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    lbl_date.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                }
            }

            if (time == null || time.isEmpty()) {
                if (lbl_gmt != null) lbl_gmt.setVisibility(View.GONE);
                if (txtTime != null) txtTime.setVisibility(View.GONE);
            } else if (txtTime != null) {
                txtTime.setText(time);
                txtTime.setTextColor(current_datetime_color);
                if (lbl_gmt != null) lbl_gmt.setTextColor(current_datetime_color);
                if (getContext() != null) {
                    txtTime.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    if (lbl_gmt != null)
                        lbl_gmt.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                }
            }
        }

        private void updateStampTitle() {
            if (txtTitle != null) {
                if (title == null || title.isEmpty()) {
                    txtTitle.setVisibility(View.GONE);
                } else {
                    txtTitle.setVisibility(View.VISIBLE);
                    txtTitle.setText(title);
                    txtTitle.setTextColor(current_text_color);
                    if (getContext() != null) {
                        txtTitle.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    }
                }
            }
        }

        private void updateStampDMS() {
            if (txt_lat_dms != null && txt_long_dms != null) {
                txt_lat_dms.setText(lat_dms);
                txt_lat_dms.setTextColor(current_text_color);
                txt_long_dms.setText(long_dms);
                txt_long_dms.setTextColor(current_text_color);

                if (getContext() != null) {
                    txt_lat_dms.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    txt_long_dms.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                }

                if (lbl_dms != null) {
                    lbl_dms.setTextColor(current_text_color);
                    if (getContext() != null) {
                        lbl_dms.setTypeface(mHelperClass.getFontStyle(getContext(), fontStyle));
                    }
                }
            }
        }

        private void updateWaterMarkVisibility() {
            if (appStamp != null) {
                if (show_watermark) {
                    appStamp.setVisibility(View.VISIBLE);
                } else {
                    appStamp.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void updateMapLocation() {
            if (staticMapImage != null && getContext() != null) {
                if (mapImagePath != null && !mapImagePath.isEmpty() && new File(mapImagePath).exists()) {
                    Glide.with(this)
                            .load(mapImagePath)
                            .placeholder(R.drawable.default_map)
                            .error(R.drawable.default_map)
                            .into(staticMapImage);
                    return;
                }

                Glide.with(this)
                        .load(R.drawable.default_map)
                        .placeholder(R.drawable.default_map)
                        .error(R.drawable.default_map)
                        .into(staticMapImage);
            }
        }

        // Share methods
        public void shareImageWithStamp() {
            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    googleMap.snapshot(mapSnapshot -> {
                        try {
                            View containerView = previewFrame;
                            Bitmap containerBitmap = Bitmap.createBitmap(containerView.getWidth(), containerView.getHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(containerBitmap);
                            containerView.draw(canvas);

                            if (mapSnapshot != null && mapViewContainer != null) {
                                int[] mapLocation = new int[2];
                                int[] containerLocation = new int[2];

                                mapViewContainer.getLocationInWindow(mapLocation);
                                containerView.getLocationInWindow(containerLocation);

                                int relativeX = mapLocation[0] - containerLocation[0];
                                int relativeY = mapLocation[1] - containerLocation[1];

                                Bitmap scaledMapSnapshot = Bitmap.createScaledBitmap(mapSnapshot, mapViewContainer.getWidth(), mapViewContainer.getHeight(), false);
                                Bitmap roundedMap = getRoundedCornerBitmap(scaledMapSnapshot, 0f);
                                canvas.drawBitmap(roundedMap, relativeX, relativeY, null);
                            }

                            File sharedFile = new File(requireContext().getCacheDir(), "shared_image_" + System.currentTimeMillis() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(sharedFile);
                            containerBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            fos.close();

                            shareFile(sharedFile, "image/jpeg", "Share Image!");

                        } catch (Exception e) {
                            e.printStackTrace();
                            Constant.Companion.showToast(requireContext(), getString(R.string.error_in_creating_file));
                        }
                    });
                });
            } else {
                try {
                    Bitmap containerBitmap = viewToImage(previewFrame);
                    File sharedFile = new File(requireContext().getCacheDir(), "shared_image_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(sharedFile);
                    containerBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                    shareFile(sharedFile, "image/jpeg", "Share Image!");
                } catch (Exception e) {
                    e.printStackTrace();
                    Constant.Companion.showToast(requireContext(), getString(R.string.error_in_creating_file));
                }
            }
        }

        public void shareVideoWithStamp() {
            try {
                if (mapFragment != null) {
                    mapFragment.getMapAsync(googleMap -> {
                        googleMap.snapshot(mapSnapshot -> {
                            try {
                                Bitmap stampBitmap = createStampOverlay(mapSnapshot);
                                VideoStampShareHelper.shareVideoWithStamp(requireContext(), photo, stampBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                                shareVideoOnly(photo);
                            }
                        });
                    });
                } else {
                    Bitmap stampBitmap = createStampOverlay(null);
                    VideoStampShareHelper.shareVideoWithStamp(requireContext(), photo, stampBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                shareVideoOnly(photo);
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
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }

        private Bitmap createStampOverlay(Bitmap mapSnapshot) {
            View stampContainerView = stampContainer;
            Bitmap stampBitmap = Bitmap.createBitmap(stampContainerView.getWidth(), stampContainerView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(stampBitmap);
            stampContainerView.draw(canvas);

            if (mapSnapshot != null && mapViewContainer != null) {
                int[] mapLocation = new int[2];
                int[] stampLocation = new int[2];

                mapViewContainer.getLocationInWindow(mapLocation);
                stampContainerView.getLocationInWindow(stampLocation);

                int relativeX = mapLocation[0] - stampLocation[0];
                int relativeY = mapLocation[1] - stampLocation[1];

                Bitmap scaledMapSnapshot = Bitmap.createScaledBitmap(mapSnapshot, mapViewContainer.getWidth(), mapViewContainer.getHeight(), false);
                canvas.drawBitmap(scaledMapSnapshot, relativeX, relativeY, null);
            }

            return stampBitmap;
        }

        private void shareFile(File file, String mimeType, String chooserTitle) {
            Uri uriForFile = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + "\n\nhttps://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
            intent.setType(mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, chooserTitle));
        }

        private void shareVideoOnly(Photo photo) {
            VideoStampShareHelper.shareVideoOnly(requireContext(), photo);
        }

        private Bitmap viewToImage(View view) {
            view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
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
    }
}
