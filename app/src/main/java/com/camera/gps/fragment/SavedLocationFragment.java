
package com.camera.gps.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.activity.MyLocation_Activity;
import com.camera.gps.adapter.SavedLocationAdapter;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.databinding.FragmentSavedLocationBinding;
import com.camera.gps.viewmodel.SavedLocationViewModel;

import java.util.ArrayList;
import java.util.List;

public class SavedLocationFragment extends Fragment implements SavedLocationAdapter.LocationAdapterInterface {

    private FragmentSavedLocationBinding binding;
    private SavedLocationAdapter adapter;
    private SavedLocationViewModel viewModel;
    private boolean isSelectionMode = false;
    private boolean isSearchMode = false;
    private Dialog dialog;

    private List<MyLocation> originalList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        setupSearchBar();

        // Setup delete button click listener in activity
        if (getActivity() instanceof MyLocation_Activity) {
            MyLocation_Activity activity = (MyLocation_Activity) getActivity();
            activity.setDeleteClickListener(() -> handleDeleteButtonClick());
            activity.setSearchClickListener(() -> handleSearchButtonClick());
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SavedLocationViewModel.class);
    }

    private void setupRecyclerView() {
        binding.recycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recycler.setLayoutManager(linearLayoutManager);
        int activeLocationId = requireActivity().getIntent().getIntExtra(
                MyLocation_Activity.EXTRA_ACTIVE_SAVED_LOCATION_ID, -1);
        adapter = new SavedLocationAdapter(
                new ArrayList<>(),
                this,
                activeLocationId >= 0 ? activeLocationId : null);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setOnTouchListener((v, event) -> {
            if (isSelectionMode && event.getAction() == MotionEvent.ACTION_DOWN) {
                View clickedView = binding.recycler.findChildViewUnder(event.getX(), event.getY());
                if (clickedView == null) { // Clicked empty space
                    exitSelectionMode();
                    return true; // Consume event
                }
            }
            return false;
        });
    }

    private void setupSearchBar() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterLocations(String query) {
        if (query.isEmpty()) {
            adapter.addList(originalList);
        } else {
            List<MyLocation> filteredList = new ArrayList<>();
            for (MyLocation location : originalList) {
                if ((location.getTitle() != null && location.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                        (location.getAddress() != null && location.getAddress().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(location);
                }
            }
            adapter.addList(filteredList);
        }
    }

    public void handleDeleteButtonClick() {
        if (!isSelectionMode) {
            // Enter selection mode
            enterSelectionMode();
        } else {
            // Check if any items are selected
            List<MyLocation> selectedItems = adapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                // No items selected, exit selection mode
                exitSelectionMode();
            } else {
                // Show delete dialog
                showDeleteDialog();
            }
        }
    }

    public void handleSearchButtonClick() {
        if (!isSearchMode) {
            // Enter search mode
            enterSearchMode();
        } else {
            // Exit search mode
            exitSearchMode();
        }
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        adapter.setSelectionMode(true);
        adapter.notifyDataSetChanged();
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        adapter.notifyDataSetChanged();
    }

    private void enterSearchMode() {
        isSearchMode = true;
        binding.searchLayout.setVisibility(View.VISIBLE);
        binding.searchEditText.requestFocus();
    }

    private void exitSearchMode() {
        isSearchMode = false;
        binding.searchLayout.setVisibility(View.GONE);
        binding.searchEditText.setText("");
        binding.searchEditText.clearFocus();
        // Reset to original list
        adapter.addList(originalList);
    }

    private void setupClickListeners() {
        // Delete button click is now handled by the activity
        // Search functionality is also handled by the activity
    }

    private void showDeleteDialog() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new Dialog(requireContext());
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

        title.setText(getString(R.string.delete_this_location));
        message.setText(getString(R.string.delete_location_message));

        btnDelete.setOnClickListener(v -> {
            List<MyLocation> selectedItems = adapter.getSelectedItems();
            if (!selectedItems.isEmpty()) {
                viewModel.deleteLocations(selectedItems);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupObservers() {
        viewModel.getLocationsLiveData().observe(getViewLifecycleOwner(), new Observer<List<MyLocation>>() {
            @Override
            public void onChanged(List<MyLocation> locations) {
                originalList.clear();
                if (locations != null) {
                    originalList.addAll(locations);
                }
                if (!isSearchMode) {
                    addList(locations);
                } else {
                    // If in search mode, apply current filter
                    filterLocations(binding.searchEditText.getText().toString());
                }
            }
        });

        viewModel.getDeleteResultLiveData().observe(getViewLifecycleOwner(), new Observer<List<MyLocation>>() {
            @Override
            public void onChanged(List<MyLocation> deletedLocations) {
                exitSelectionMode();
                if (adapter != null && deletedLocations != null) {
                    adapter.removeList(deletedLocations);
                    // Also remove from original list
                    originalList.removeAll(deletedLocations);
                }
            }
        });
    }

    private void getAllLocation() {
        exitSelectionMode();
        viewModel.getAllLocation();
    }

    private void addList(List<MyLocation> list) {
        if (adapter != null) {
            adapter.addList(list);
        }
    }

    // LocationAdapterInterface implementations
    @Override
    public void onLocationClicked(MyLocation location, boolean isSelectionMode) {
        if (isSelectionMode) {
            viewModel.toggleLocationSelection(location);
            adapter.notifyDataSetChanged();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(MyApplication.EXTRA_LOCATION, location);
            requireActivity().setResult(Activity.RESULT_OK, resultIntent);
            requireActivity().finish();
        }
    }

    @Override
    public void onLocationLongClicked(MyLocation location) {
        // Long press functionality removed as per requirement
    }

    @Override
    public void onSelectionChanged(boolean hasSelection) {
        // This method can be used for additional selection change handling if needed
    }

    @Override
    public void onNavigationRequested(MyLocation location) {
        // Handle navigation in fragment if needed
    }

    @Override
    public void onGoogleMapsRequested(MyLocation location) {
        // Handle Google Maps in fragment if needed
    }

    @Override
    public void onShareRequested(MyLocation location) {
        // Handle share in fragment if needed
    }

    @Override
    public void checkValidation() {
        List<MyLocation> list = adapter.getList();
        if (list == null || list.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.GONE);
            return;
        }
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
