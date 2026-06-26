//package com.camera.gps.fragment;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.GridLayoutManager;
//
//import com.camera.gps.R;
//import com.camera.gps.adapter.PhotoAdapter;
//import com.camera.gps.database.entity.Photo;
//import com.camera.gps.databinding.FragmentCreationBinding;
//import com.camera.gps.model.CreationItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CreationFragment extends Fragment {
//
//    public static final int TYPE_PHOTOS = 0;
//    public static final int TYPE_VIDEOS = 1;
//
//    private static final String ARG_TYPE = "type";
//
//    private FragmentCreationBinding binding;
//    private PhotoAdapter adapter;
//    private int fragmentType;
//    private OnSelectionChangeListener selectionChangeListener;
//    private List<Photo> pendingData; // Store data if view not ready
//
//    public interface OnSelectionChangeListener {
//        void onSelectionChanged(boolean hasSelection);
//    }
//
//    public static CreationFragment newInstance(int type) {
//        CreationFragment fragment = new CreationFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_TYPE, type);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            fragmentType = getArguments().getInt(ARG_TYPE, TYPE_PHOTOS);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        binding = FragmentCreationBinding.inflate(inflater, container, false);
//        setupRecyclerView();
//
//        // Apply pending data if any
//        if (pendingData != null) {
//            updateData(pendingData);
//            pendingData = null;
//        }
//
//        return binding.getRoot();
//    }
//
//    private void setupRecyclerView() {
//        binding.recyclerView.setHasFixedSize(true);
//
//        // Create GridLayoutManager with span size lookup for headers
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
//        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                // Date headers should span all 3 columns, photos span 1 column
//                if (adapter != null) {
//                    return adapter.getItemViewType(position) == CreationItem.TYPE_DATE_HEADER ? 3 : 1;
//                }
//                return 1;
//            }
//        });
//
//        binding.recyclerView.setLayoutManager(gridLayoutManager);
//
//        adapter = new PhotoAdapter(new ArrayList<>());
//        adapter.setOnSelectionChangeListener(hasSelection -> {
//            if (selectionChangeListener != null) {
//                selectionChangeListener.onSelectionChanged(hasSelection);
//            }
//        });
//
//        binding.recyclerView.setAdapter(adapter);
//    }
//
//    public void updateData(List<Photo> photos) {
//        // Store data for later if view is not created yet
//        if (binding == null) {
//            pendingData = photos;
//            return;
//        }
//
//        if (photos == null || photos.isEmpty()) {
//            showEmptyState();
//            return;
//        }
//
//        hideEmptyState();
//        if (adapter != null) {
//            adapter.updateData(photos);
//        }
//    }
//
//    private void showEmptyState() {
//        if (binding == null) return;
//
//        binding.recyclerView.setVisibility(View.GONE);
//        binding.emptyStateLayout.setVisibility(View.VISIBLE);
//
//        if (fragmentType == TYPE_PHOTOS) {
//            binding.emptyStateText.setText(getString(R.string.photos_not_found));
//            binding.emptyStateImage.setImageResource(R.drawable.ic_photo_empty_state);
//        } else {
//            binding.emptyStateText.setText(getString(R.string.videos_not_found));
//            binding.emptyStateImage.setImageResource(R.drawable.ic_video_empty_state);
//        }
//    }
//
//    private void hideEmptyState() {
//        if (binding == null) return;
//
//        binding.recyclerView.setVisibility(View.VISIBLE);
//        binding.emptyStateLayout.setVisibility(View.GONE);
//    }
//
//    public List<Photo> getSelectedItems() {
//        return adapter != null ? adapter.getSelectedItems() : new ArrayList<>();
//    }
//
//    public void clearSelection() {
//        if (adapter != null) {
//            adapter.clearSelection();
//        }
//    }
//
//    public void setSelectionChangeListener(OnSelectionChangeListener listener) {
//        this.selectionChangeListener = listener;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//        // Don't clear pendingData here in case fragment is recreated
//    }
//}

package com.camera.gps.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.PhotoAdapter;
import com.camera.gps.adsmanager.NativeAdManager;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.databinding.FragmentCreationBinding;
import com.camera.gps.model.CreationItem;
import com.camera.gps.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class CreationFragment extends Fragment {

    public static final int TYPE_PHOTOS = 0;
    public static final int TYPE_VIDEOS = 1;

    private static final String ARG_TYPE = "type";

    private FragmentCreationBinding binding;
    private PhotoAdapter adapter;
    private int fragmentType;
    private OnSelectionChangeListener selectionChangeListener;
    private List<Photo> pendingData; // Store data if view not ready
    private boolean isSelectionMode = false;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public static CreationFragment newInstance(int type) {
        CreationFragment fragment = new CreationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentType = getArguments().getInt(ARG_TYPE, TYPE_PHOTOS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreationBinding.inflate(inflater, container, false);
        setupRecyclerView();

        // Apply pending data if any
        if (pendingData != null) {
            updateData(pendingData);
            pendingData = null;
        }

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);

        // Create GridLayoutManager with span size lookup for headers
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Date headers should span all 3 columns, photos span 1 column
                if (adapter != null) {
                    int viewType = adapter.getItemViewType(position);
                    return viewType == CreationItem.TYPE_DATE_HEADER || viewType == CreationItem.TYPE_NATIVE_AD ? 3 : 1;
                }
                return 1;
            }
        });

        binding.recyclerView.setLayoutManager(gridLayoutManager);

        adapter = new PhotoAdapter(new ArrayList<>());
        adapter.setOnSelectionChangeListener(hasSelection -> {
            if (selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(hasSelection);
            }
        });

        binding.recyclerView.setAdapter(adapter);
    }

    public void updateData(List<Photo> photos) {
        // Store data for later if view is not created yet
        if (binding == null) {
            pendingData = photos;
            return;
        }

        if (photos == null || photos.isEmpty()) {
            showEmptyState();
            return;
        }

        hideEmptyState();
        if (adapter != null) {
            adapter.setNativeAdsEnabled(shouldShowNativeAds());
            adapter.updateData(photos);
        }
    }

    private boolean shouldShowNativeAds() {
        return getActivity() != null
                && getContext() != null
                && MyApplication.isNetworkAvailable(requireContext())
                && !Utils.getIsPremium(requireContext())
                && NativeAdManager.getInstance().isAdsEnabled(requireActivity(), "my_creation_native");
    }

    private void showEmptyState() {
        if (binding == null) return;

        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);

        if (fragmentType == TYPE_PHOTOS) {
            binding.emptyStateText.setText(getString(R.string.photos_not_found));
            binding.emptyStateImage.setImageResource(R.drawable.ic_photo_empty_state);
        } else {
            binding.emptyStateText.setText(getString(R.string.videos_not_found));
            binding.emptyStateImage.setImageResource(R.drawable.ic_video_empty_state);
        }
    }

    private void hideEmptyState() {
        if (binding == null) return;

        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
    }

    public List<Photo> getSelectedItems() {
        return adapter != null ? adapter.getSelectedItems() : new ArrayList<>();
    }

    public void clearSelection() {
        if (adapter != null) {
            adapter.clearSelection();
        }
    }

    public void enterSelectionMode() {
        isSelectionMode = true;
        if (adapter != null) {
            adapter.enterSelectionMode();
        }
    }

    public void exitSelectionMode() {
        isSelectionMode = false;
        if (adapter != null) {
            adapter.exitSelectionMode();
        }
    }

    public void setSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // Don't clear pendingData here in case fragment is recreated
    }
}
