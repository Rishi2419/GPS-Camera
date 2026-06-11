//package com.camera.gps.adapter;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentActivity;
//import androidx.viewpager2.adapter.FragmentStateAdapter;
//
//import com.camera.gps.database.entity.Photo;
//import com.camera.gps.fragment.CreationFragment;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CreationViewPagerAdapter extends FragmentStateAdapter {
//
//    private List<Photo> allPhotos = new ArrayList<>();
//    private CreationFragment photosFragment;
//    private CreationFragment videosFragment;
//    private OnSelectionChangeListener selectionChangeListener;
//
//    public interface OnSelectionChangeListener {
//        void onSelectionChanged(boolean hasSelection);
//    }
//
//    public CreationViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
//        super(fragmentActivity);
//        photosFragment = CreationFragment.newInstance(CreationFragment.TYPE_PHOTOS);
//        videosFragment = CreationFragment.newInstance(CreationFragment.TYPE_VIDEOS);
//
//        setupSelectionListeners();
//    }
//
//    private void setupSelectionListeners() {
//        CreationFragment.OnSelectionChangeListener listener = hasSelection -> {
//            if (selectionChangeListener != null) {
//                selectionChangeListener.onSelectionChanged(hasSelection);
//            }
//        };
//
//        photosFragment.setSelectionChangeListener(listener);
//        videosFragment.setSelectionChangeListener(listener);
//    }
//
//    @NonNull
//    @Override
//    public Fragment createFragment(int position) {
//        switch (position) {
//            case 0:
//                return photosFragment;
//            case 1:
//                return videosFragment;
//            default:
//                return photosFragment;
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 2; // Photos and Videos tabs
//    }
//
//    public void updateData(List<Photo> photos) {
//        this.allPhotos = photos != null ? new ArrayList<>(photos) : new ArrayList<>();
//
//        // Filter and update fragments
//        List<Photo> photosList = filterPhotos(true);
//        List<Photo> videosList = filterPhotos(false);
//
//        if (photosFragment != null) {
//            photosFragment.updateData(photosList);
//        }
//        if (videosFragment != null) {
//            videosFragment.updateData(videosList);
//        }
//    }
//
//    private List<Photo> filterPhotos(boolean isPhoto) {
//        List<Photo> filteredList = new ArrayList<>();
//        for (Photo photo : allPhotos) {
//            if (photo.getImagePath() != null) {
//                boolean isPhotoFile = photo.getImagePath().endsWith(".jpg") ||
//                        photo.getImagePath().endsWith(".jpeg") ||
//                        photo.getImagePath().endsWith(".png");
//                boolean isVideoFile = photo.getImagePath().endsWith(".mp4");
//
//                if (isPhoto && isPhotoFile) {
//                    filteredList.add(photo);
//                } else if (!isPhoto && isVideoFile) {
//                    filteredList.add(photo);
//                }
//            }
//        }
//        return filteredList;
//    }
//
//    public List<Photo> getSelectedItems() {
//        List<Photo> selectedItems = new ArrayList<>();
//        if (photosFragment != null) {
//            selectedItems.addAll(photosFragment.getSelectedItems());
//        }
//        if (videosFragment != null) {
//            selectedItems.addAll(videosFragment.getSelectedItems());
//        }
//        return selectedItems;
//    }
//
//    public void clearSelection() {
//        if (photosFragment != null) {
//            photosFragment.clearSelection();
//        }
//        if (videosFragment != null) {
//            videosFragment.clearSelection();
//        }
//    }
////
////    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
////        this.selectionChangeListener = listener;
////    }
//}

package com.camera.gps.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.camera.gps.database.entity.Photo;
import com.camera.gps.fragment.CreationFragment;

import java.util.ArrayList;
import java.util.List;

public class CreationViewPagerAdapter extends FragmentStateAdapter {

    private List<Photo> allPhotos = new ArrayList<>();
    private CreationFragment photosFragment;
    private CreationFragment videosFragment;
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public CreationViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        photosFragment = CreationFragment.newInstance(CreationFragment.TYPE_PHOTOS);
        videosFragment = CreationFragment.newInstance(CreationFragment.TYPE_VIDEOS);

        setupSelectionListeners();
    }

    private void setupSelectionListeners() {
        CreationFragment.OnSelectionChangeListener listener = hasSelection -> {
            if (selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(hasSelection);
            }
        };

        photosFragment.setSelectionChangeListener(listener);
        videosFragment.setSelectionChangeListener(listener);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return photosFragment;
            case 1:
                return videosFragment;
            default:
                return photosFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Photos and Videos tabs
    }

    public void updateData(List<Photo> photos) {
        this.allPhotos = photos != null ? new ArrayList<>(photos) : new ArrayList<>();

        // Filter and update fragments
        List<Photo> photosList = filterPhotos(true);
        List<Photo> videosList = filterPhotos(false);

        if (photosFragment != null) {
            photosFragment.updateData(photosList);
        }
        if (videosFragment != null) {
            videosFragment.updateData(videosList);
        }
    }

    private List<Photo> filterPhotos(boolean isPhoto) {
        List<Photo> filteredList = new ArrayList<>();
        for (Photo photo : allPhotos) {
            if (photo.getImagePath() != null) {
                boolean isPhotoFile = photo.getImagePath().endsWith(".jpg") ||
                        photo.getImagePath().endsWith(".jpeg") ||
                        photo.getImagePath().endsWith(".png");
                boolean isVideoFile = photo.getImagePath().endsWith(".mp4");

                if (isPhoto && isPhotoFile) {
                    filteredList.add(photo);
                } else if (!isPhoto && isVideoFile) {
                    filteredList.add(photo);
                }
            }
        }
        return filteredList;
    }

    public List<Photo> getSelectedItems() {
        List<Photo> selectedItems = new ArrayList<>();
        if (photosFragment != null) {
            selectedItems.addAll(photosFragment.getSelectedItems());
        }
        if (videosFragment != null) {
            selectedItems.addAll(videosFragment.getSelectedItems());
        }
        return selectedItems;
    }

    public void clearSelection() {
        if (photosFragment != null) {
            photosFragment.clearSelection();
        }
        if (videosFragment != null) {
            videosFragment.clearSelection();
        }
    }

    public void enterSelectionMode() {
        if (photosFragment != null) {
            photosFragment.enterSelectionMode();
        }
        if (videosFragment != null) {
            videosFragment.enterSelectionMode();
        }
    }

    public void exitSelectionMode() {
        if (photosFragment != null) {
            photosFragment.exitSelectionMode();
        }
        if (videosFragment != null) {
            videosFragment.exitSelectionMode();
        }
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }
}