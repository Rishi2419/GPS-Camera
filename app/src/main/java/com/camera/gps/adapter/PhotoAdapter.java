//package com.camera.gps.adapter;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.camera.gps.R;
//import com.camera.gps.activity.MyCreation_Activity;
//import com.camera.gps.activity.PhotoGallery_Activity;
//import com.camera.gps.activity.PhotoPreview_Activity;
//import com.camera.gps.database.entity.Photo;
//import com.camera.gps.databinding.ItemMyCreationBinding;
//import com.camera.gps.databinding.ItemDateHeaderBinding;
//import com.camera.gps.model.CreationItem;
//import com.camera.gps.model.DateHeaderItem;
//import com.camera.gps.model.PhotoItem;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import static android.view.View.GONE;
//import static android.view.View.VISIBLE;
//
//public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private List<CreationItem> itemList;
//    private List<Photo> originalPhotoList;
//    private boolean isSelectionMode = false;
//    private OnSelectionChangeListener selectionChangeListener;
//
//    public interface OnSelectionChangeListener {
//        void onSelectionChanged(boolean hasSelection);
//    }
//
//    public PhotoAdapter(List<Photo> photoList) {
//        this.originalPhotoList = new ArrayList<>();
//        this.itemList = new ArrayList<>();
//        if (photoList != null) {
//            updateData(photoList);
//        }
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return itemList.get(position).getItemType();
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//
//        if (viewType == CreationItem.TYPE_DATE_HEADER) {
//            ItemDateHeaderBinding binding = ItemDateHeaderBinding.inflate(inflater, parent, false);
//            return new DateHeaderViewHolder(binding);
//        } else {
//            ItemMyCreationBinding binding = ItemMyCreationBinding.inflate(inflater, parent, false);
//            return new PhotoViewHolder(binding);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        CreationItem item = itemList.get(position);
//
//        if (holder instanceof DateHeaderViewHolder && item instanceof DateHeaderItem) {
//            ((DateHeaderViewHolder) holder).bind((DateHeaderItem) item);
//        } else if (holder instanceof PhotoViewHolder && item instanceof PhotoItem) {
//            ((PhotoViewHolder) holder).bind(((PhotoItem) item).getPhoto());
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return itemList.size();
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    public void updateData(List<Photo> newPhotos) {
//        this.originalPhotoList.clear();
//        this.itemList.clear();
//
//        if (newPhotos != null && !newPhotos.isEmpty()) {
//            this.originalPhotoList.addAll(newPhotos);
//
//            // Group photos by date
//            Map<String, List<Photo>> groupedPhotos = groupPhotosByDate(newPhotos);
//
//            // Create items list with headers and photos
//            for (Map.Entry<String, List<Photo>> entry : groupedPhotos.entrySet()) {
//                // Add date header
//                itemList.add(new DateHeaderItem(entry.getKey()));
//
//                // Add photos for this date
//                for (Photo photo : entry.getValue()) {
//                    itemList.add(new PhotoItem(photo));
//                }
//            }
//        }
//
//        notifyDataSetChanged();
//    }
//
////    private Map<String, List<Photo>> groupPhotosByDate(List<Photo> photos) {
////        Map<String, List<Photo>> groupedPhotos = new LinkedHashMap<>();
////        // Use correct pattern with dash
////        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
////        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
////
////        // Sort photos by date (newest first)
////        List<Photo> sortedPhotos = new ArrayList<>(photos);
////        Collections.sort(sortedPhotos, (p1, p2) -> {
////            try {
////                Date date1 = inputFormat.parse(p1.getDate() != null ? p1.getDate() : "01-01-1970");
////                Date date2 = inputFormat.parse(p2.getDate() != null ? p2.getDate() : "01-01-1970");
////                return date2.compareTo(date1); // Newest first
////            } catch (ParseException e) {
////                return 0;
////            }
////        });
////
////        for (Photo photo : sortedPhotos) {
////            String dateKey;
////            try {
////                String rawDate = photo.getDateTimeTaken();
////                Log.d("RishiDate", "📌 Raw Date from Photo: " + rawDate);
////
////                if (rawDate != null && !rawDate.isEmpty()) {
////                    String cleanDate = rawDate.trim();
////                    Log.d("RishiDate", "✅ Cleaned Date: " + cleanDate);
////
////                    Date date = inputFormat.parse(cleanDate);
////                    Log.d("RishiDate", "🛠 Parsed Date Object: " + date);
////
////                    dateKey = outputFormat.format(date).toUpperCase();
////                    Log.d("RishiDate", "🎯 Final Formatted Date Key: " + dateKey);
////                } else {
////                    Log.d("RishiDate", "⚠️ Date is NULL or EMPTY for this photo.");
////                    dateKey = "UNKNOWN DATE";
////                }
////            } catch (ParseException e) {
////                Log.d("RishiDate", "❌ ParseException: " + e.getMessage());
////                dateKey = "UNKNOWN DATE";
////            }
////
////            groupedPhotos.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(photo);
////        }
////
////        return groupedPhotos;
////    }
//
//
//    private Map<String, List<Photo>> groupPhotosByDate(List<Photo> photos) {
//        Map<String, List<Photo>> groupedPhotos = new LinkedHashMap<>();
//        // Use correct pattern with dash
//        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
//        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
//
//        // Sort photos by FULL datetime (newest first) - this ensures within same date, newer photos come first
//        List<Photo> sortedPhotos = new ArrayList<>(photos);
//        Collections.sort(sortedPhotos, (p1, p2) -> {
//            try {
//                String date1 = p1.getDateTimeTaken() != null ? p1.getDateTimeTaken() : "19700101_000000";
//                String date2 = p2.getDateTimeTaken() != null ? p2.getDateTimeTaken() : "19700101_000000";
//
//                // Parse as full datetime to maintain order within same date
//                Date dateTime1 = inputFormat.parse(date1);
//                Date dateTime2 = inputFormat.parse(date2);
//
//                return dateTime2.compareTo(dateTime1); // Newest first (including time)
//            } catch (ParseException e) {
//                return 0;
//            }
//        });
//
//        // Group the already sorted photos by date
//        for (Photo photo : sortedPhotos) {
//            String dateKey;
//            try {
//                String rawDate = photo.getDateTimeTaken();
//                Log.d("RishiDate", "📌 Raw Date from Photo: " + rawDate);
//
//                if (rawDate != null && !rawDate.isEmpty()) {
//                    String cleanDate = rawDate.trim();
//                    Log.d("RishiDate", "✅ Cleaned Date: " + cleanDate);
//
//                    Date date = inputFormat.parse(cleanDate);
//                    Log.d("RishiDate", "🛠 Parsed Date Object: " + date);
//
//                    dateKey = outputFormat.format(date).toUpperCase();
//                    Log.d("RishiDate", "🎯 Final Formatted Date Key: " + dateKey);
//                } else {
//                    Log.d("RishiDate", "⚠️ Date is NULL or EMPTY for this photo.");
//                    dateKey = "UNKNOWN DATE";
//                }
//            } catch (ParseException e) {
//                Log.d("RishiDate", "❌ ParseException: " + e.getMessage());
//                dateKey = "UNKNOWN DATE";
//            }
//
//            // Add to the date group - since photos are already sorted by full datetime,
//            // newer photos within the same date will be added first
//            groupedPhotos.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(photo);
//        }
//
//        return groupedPhotos;
//    }
//
//    public List<Photo> getSelectedItems() {
//        List<Photo> selectedItems = new ArrayList<>();
//        for (Photo photo : originalPhotoList) {
//            if (photo.isSelected() != null && photo.isSelected()) {
//                selectedItems.add(photo);
//            }
//        }
//        return selectedItems;
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    public void clearSelection() {
//        isSelectionMode = false;
//        for (Photo photo : originalPhotoList) {
//            photo.setSelected(false);
//        }
//        notifyDataSetChanged();
//        notifySelectionChange();
//    }
//
//    private void notifySelectionChange() {
//        if (selectionChangeListener != null) {
//            boolean hasSelection = !getSelectedItems().isEmpty();
//            selectionChangeListener.onSelectionChanged(hasSelection);
//        }
//    }
//
//    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
//        this.selectionChangeListener = listener;
//    }
//
//    // Date Header ViewHolder
//    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
//        private final ItemDateHeaderBinding binding;
//
//        public DateHeaderViewHolder(ItemDateHeaderBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        public void bind(DateHeaderItem dateHeader) {
//            binding.tvDateHeader.setText(dateHeader.getDateText());
//        }
//    }
//
//    // Photo ViewHolder
//    public class PhotoViewHolder extends RecyclerView.ViewHolder {
//        private final ItemMyCreationBinding binding;
//
//        public PhotoViewHolder(ItemMyCreationBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        @SuppressLint("NotifyDataSetChanged")
//        public void bind(Photo photo) {
//            Context context = binding.getRoot().getContext();
//
//            // Load image/video thumbnail
//            if (photo.getImagePath() != null) {
//                if (photo.getImagePath().endsWith(".mp4")) {
//                    binding.playVideo.setVisibility(VISIBLE);
//                    Glide.with(context).load(photo.getImagePath()).into(binding.image);
//                } else if (photo.getImagePath().endsWith(".jpeg") || photo.getImagePath().endsWith(".jpg") || photo.getImagePath().endsWith(".png")) {
//                    binding.playVideo.setVisibility(GONE);
//                    Glide.with(context).load(photo.getImagePath()).into(binding.image);
//                }
//            }
//
//            // Handle selection state
//            boolean isSelected = photo.isSelected() != null && photo.isSelected();
//
//            // Click listeners
//            itemView.setOnClickListener(v -> {
//                openPhotoPreview(context, photo);
//            });
//        }
////
////        private void openPhotoPreview(Context context, Photo photo) {
////            Intent intent = new Intent(context, PhotoPreview_Activity.class);
////            intent.putExtra("model", photo);
////            intent.putExtra("fromCreation", true);
////            context.startActivity(intent);
////
////            if (context instanceof MyCreation_Activity) {
////                ((MyCreation_Activity) context).finish();
////            }
////        }
//
//        // Update the openPhotoPreview method in your PhotoAdapter class:
//
//        private void openPhotoPreview(Context context, Photo photo) {
//            // Get all photos from the adapter for the sliding gallery
//            ArrayList<Photo> allPhotos = new ArrayList<>(originalPhotoList);
//
//            // Find the position of the clicked photo
//            int currentPosition = -1;
//            for (int i = 0; i < allPhotos.size(); i++) {
//                if (allPhotos.get(i).getId() != null && allPhotos.get(i).getId().equals(photo.getId())) {
//                    currentPosition = i;
//                    break;
//                }
//            }
//
//            // Launch the gallery activity
//            Intent intent = new Intent(context, PhotoGallery_Activity.class);
//            intent.putExtra("photoList", allPhotos);
//            intent.putExtra("currentPosition", currentPosition != -1 ? currentPosition : 0);
//            intent.putExtra("fromCreation", true);
//            context.startActivity(intent);
//
//            if (context instanceof MyCreation_Activity) {
//                ((MyCreation_Activity) context).finish();
//            }
//        }
//    }
//}

package com.camera.gps.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.activity.MyCreation_Activity;
import com.camera.gps.activity.PhotoGallery_Activity;
import com.camera.gps.adsmanager.NativeAdManager;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.databinding.ItemMyCreationBinding;
import com.camera.gps.databinding.ItemDateHeaderBinding;
import com.camera.gps.model.CreationItem;
import com.camera.gps.model.DateHeaderItem;
import com.camera.gps.model.NativeAdItem;
import com.camera.gps.model.PhotoItem;
import com.camera.gps.util.SharedMediaStore;
import com.camera.gps.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CreationItem> itemList;
    private List<Photo> originalPhotoList;
    private boolean isSelectionMode = false;
    private OnSelectionChangeListener selectionChangeListener;
    private static final int NATIVE_AD_INTERVAL = 6;
    private boolean nativeAdsEnabled = true;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public PhotoAdapter(List<Photo> photoList) {
        this.originalPhotoList = new ArrayList<>();
        this.itemList = new ArrayList<>();
        if (photoList != null) {
            updateData(photoList);
        }
    }

    public void setNativeAdsEnabled(boolean nativeAdsEnabled) {
        this.nativeAdsEnabled = nativeAdsEnabled;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == CreationItem.TYPE_DATE_HEADER) {
            ItemDateHeaderBinding binding = ItemDateHeaderBinding.inflate(inflater, parent, false);
            return new DateHeaderViewHolder(binding);
        } else if (viewType == CreationItem.TYPE_NATIVE_AD) {
            return new NativeAdViewHolder(inflater.inflate(R.layout.item_native_ad_container, parent, false));
        } else {
            ItemMyCreationBinding binding = ItemMyCreationBinding.inflate(inflater, parent, false);
            return new PhotoViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CreationItem item = itemList.get(position);

        if (holder instanceof DateHeaderViewHolder && item instanceof DateHeaderItem) {
            ((DateHeaderViewHolder) holder).bind((DateHeaderItem) item);
        } else if (holder instanceof NativeAdViewHolder) {
            ((NativeAdViewHolder) holder).bind();
        } else if (holder instanceof PhotoViewHolder && item instanceof PhotoItem) {
            ((PhotoViewHolder) holder).bind(((PhotoItem) item).getPhoto());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Photo> newPhotos) {
        this.originalPhotoList.clear();
        this.itemList.clear();

        if (newPhotos != null && !newPhotos.isEmpty()) {
            this.originalPhotoList.addAll(newPhotos);

            // Group photos by date
            Map<String, List<Photo>> groupedPhotos = groupPhotosByDate(newPhotos);

            // Create items list with headers and photos
            int mediaCount = 0;
            for (Map.Entry<String, List<Photo>> entry : groupedPhotos.entrySet()) {
                // Add date header
                itemList.add(new DateHeaderItem(entry.getKey()));

                // Add photos for this date
                for (Photo photo : entry.getValue()) {
                    itemList.add(new PhotoItem(photo));
                    mediaCount++;
                    if (nativeAdsEnabled && mediaCount % NATIVE_AD_INTERVAL == 0) {
                        itemList.add(new NativeAdItem());
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    private Map<String, List<Photo>> groupPhotosByDate(List<Photo> photos) {
        Map<String, List<Photo>> groupedPhotos = new LinkedHashMap<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

        // Sort photos by FULL datetime (newest first)
        List<Photo> sortedPhotos = new ArrayList<>(photos);
        Collections.sort(sortedPhotos, (p1, p2) -> {
            try {
                String date1 = p1.getDateTimeTaken() != null ? p1.getDateTimeTaken() : "19700101_000000";
                String date2 = p2.getDateTimeTaken() != null ? p2.getDateTimeTaken() : "19700101_000000";

                Date dateTime1 = inputFormat.parse(date1);
                Date dateTime2 = inputFormat.parse(date2);

                return dateTime2.compareTo(dateTime1); // Newest first
            } catch (ParseException e) {
                return 0;
            }
        });

        // Group the already sorted photos by date
        for (Photo photo : sortedPhotos) {
            String dateKey;
            try {
                String rawDate = photo.getDateTimeTaken();

                if (rawDate != null && !rawDate.isEmpty()) {
                    String cleanDate = rawDate.trim();

                    Date date = inputFormat.parse(cleanDate);

                    dateKey = outputFormat.format(date).toUpperCase();
                } else {
                    dateKey = "UNKNOWN DATE";
                }
            } catch (ParseException e) {
                dateKey = "UNKNOWN DATE";
            }

            groupedPhotos.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(photo);
        }

        return groupedPhotos;
    }

    public List<Photo> getSelectedItems() {
        List<Photo> selectedItems = new ArrayList<>();
        for (Photo photo : originalPhotoList) {
            if (photo.isSelected() != null && photo.isSelected()) {
                selectedItems.add(photo);
            }
        }
        return selectedItems;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelection() {
        for (Photo photo : originalPhotoList) {
            photo.setSelected(false);
        }
        notifyDataSetChanged();
        notifySelectionChange();
    }

    public void enterSelectionMode() {
        isSelectionMode = true;
        notifyDataSetChanged();
    }

    public void exitSelectionMode() {
        isSelectionMode = false;
        clearSelection();
    }

    private void notifySelectionChange() {
        if (selectionChangeListener != null) {
            boolean hasSelection = !getSelectedItems().isEmpty();
            selectionChangeListener.onSelectionChanged(hasSelection);
        }
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    // Date Header ViewHolder
    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemDateHeaderBinding binding;

        public DateHeaderViewHolder(ItemDateHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DateHeaderItem dateHeader) {
            binding.tvDateHeader.setText(dateHeader.getDateText());
        }
    }

    public static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout container;
        private boolean requested;

        public NativeAdViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.flNativeAd);
        }

        public void bind() {
            Context context = itemView.getContext();
            if (!(context instanceof android.app.Activity)
                    || !MyApplication.isNetworkAvailable(context)
                    || Utils.getIsPremium(context)) {
                container.setVisibility(android.view.View.GONE);
                return;
            }

            container.setVisibility(android.view.View.VISIBLE);
            if (!requested) {
                requested = true;
                NativeAdManager.getInstance().loadAndShowNativeAd(
                        (android.app.Activity) context,
                        "my_creation_native",
                        container,
                        false,
                        null,
                        () -> requested = false
                );
            }
        }
    }

    // Photo ViewHolder
    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyCreationBinding binding;

        public PhotoViewHolder(ItemMyCreationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void bind(Photo photo) {
            Context context = binding.getRoot().getContext();

            // Load image/video thumbnail
            if (photo.getImagePath() != null) {
                if (SharedMediaStore.isVideo(photo)) {
                    binding.playVideo.setVisibility(VISIBLE);
                    Glide.with(context).load(SharedMediaStore.getLoadSource(photo)).into(binding.image);
                } else {
                    binding.playVideo.setVisibility(GONE);
                    Glide.with(context).load(SharedMediaStore.getLoadSource(photo)).into(binding.image);
                }
            }

            // Handle selection state
            boolean isSelected = photo.isSelected() != null && photo.isSelected();

            // Show/hide selection UI based on mode
            if (isSelectionMode) {
                binding.selectionOverlay.setVisibility(isSelected ? VISIBLE : GONE);
                binding.checkBox.setVisibility(VISIBLE);
                binding.checkBox.setChecked(isSelected);
            } else {
                binding.selectionOverlay.setVisibility(GONE);
                binding.checkBox.setVisibility(GONE);
                binding.checkBox.setChecked(false);
            }

            itemView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    boolean currentlySelected = photo.isSelected() != null && photo.isSelected();
                    boolean newSelectionState = !currentlySelected;
                    photo.setSelected(newSelectionState);
                    binding.checkBox.setChecked(newSelectionState);
                    binding.selectionOverlay.setVisibility(newSelectionState ? VISIBLE : GONE);
                    notifySelectionChange();
                } else {
                    // Normal click - open preview
                    openPhotoPreview(context, photo);
                }
            });

            binding.checkBox.setOnClickListener(v -> itemView.performClick());

            // Remove long press functionality since delete button handles selection mode
        }

        private void openPhotoPreview(Context context, Photo photo) {
            // Get all photos from the adapter for the sliding gallery
            ArrayList<Photo> allPhotos = new ArrayList<>(originalPhotoList);

            // Find the position of the clicked photo
            int currentPosition = -1;
            for (int i = 0; i < allPhotos.size(); i++) {
                if (allPhotos.get(i).getId() != null && allPhotos.get(i).getId().equals(photo.getId())) {
                    currentPosition = i;
                    break;
                }
            }

            // Launch the gallery activity
            Intent intent = new Intent(context, PhotoGallery_Activity.class);
            intent.putExtra("photoList", allPhotos);
            intent.putExtra("currentPosition", currentPosition != -1 ? currentPosition : 0);
            intent.putExtra("fromCreation", true);
            context.startActivity(intent);

            if (context instanceof MyCreation_Activity) {
                ((MyCreation_Activity) context).finish();
            }
        }
    }
}
