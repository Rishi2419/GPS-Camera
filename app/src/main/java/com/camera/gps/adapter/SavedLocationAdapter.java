package com.camera.gps.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.databinding.ItemSavedLocationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.ViewHolder> {
    private final List<MyLocation> list;
    private final LocationAdapterInterface listener;
    private boolean isSelectionMode = false;

    public interface LocationAdapterInterface {
        void onLocationClicked(MyLocation location, boolean isSelectionMode);
        void onLocationLongClicked(MyLocation location);
        void onSelectionChanged(boolean hasSelection);
        void onNavigationRequested(MyLocation location);
        void onGoogleMapsRequested(MyLocation location);
        void onShareRequested(MyLocation location);
        void checkValidation();
    }

    public SavedLocationAdapter(List<MyLocation> list, LocationAdapterInterface listener) {
        this.list = list;
        this.listener = listener;
    }

    public List<MyLocation> getList() {
        return this.list;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
    }

    public boolean isSelectionMode() {
        return this.isSelectionMode;
    }

    public List<MyLocation> getSelectedItems() {
        ArrayList<MyLocation> arrayList = new ArrayList<>();
        for (MyLocation obj : this.list) {
            Boolean isSelected = obj.isSelected();
            if (isSelected != null && isSelected) {
                arrayList.add(obj);
            }
        }
        return arrayList;
    }

    public void removeList(List<MyLocation> locations) {
        this.list.removeAll(locations);
        if (listener != null) {
            listener.checkValidation();
        }
        notifyDataSetChanged();
    }

    public void addList(List<MyLocation> list) {
        this.list.clear();
        if (!(list == null || list.isEmpty())) {
            Collections.reverse(list);
            this.list.addAll(list);
        }
        if (listener != null) {
            listener.checkValidation();
        }
        notifyDataSetChanged();
    }

    public void addItem(MyLocation item) {
        this.list.add(0, item);
        if (listener != null) {
            listener.checkValidation();
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        for (MyLocation location : list) {
            location.setSelected(false);
        }
        if (listener != null) {
            listener.onSelectionChanged(false);
        }
        notifyDataSetChanged();
    }

    private boolean hasSelectedItems() {
        for (MyLocation location : list) {
            if (location.isSelected() != null && location.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSavedLocationBinding bin;
        GoogleMap mMap;

        public ViewHolder(ItemSavedLocationBinding bin) {
            super(bin.getRoot());
            this.bin = bin;

            bin.mapView.onCreate(null);
            bin.mapView.getMapAsync(map -> {
                this.mMap = map;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setAllGesturesEnabled(false);
                map.getUiSettings().setMapToolbarEnabled(false);
                map.getUiSettings().setZoomControlsEnabled(false);
            });
        }

        public GoogleMap getMMap() {
            return this.mMap;
        }

        public void bind(final MyLocation model) {
            ItemSavedLocationBinding itemMyLocationBinding = this.bin;

            // Show selection indicator based on selection mode and selection state
            if (isSelectionMode) {
                itemMyLocationBinding.ivSelectionIndicator.setVisibility(View.VISIBLE);
                if (model.isSelected() != null && model.isSelected()) {
                    // Show blue tick when selected
                    itemMyLocationBinding.ivSelectionIndicator.setImageResource(R.drawable.ic_blue_tick);
                } else {
                    // Show empty circle when not selected
                    itemMyLocationBinding.ivSelectionIndicator.setImageResource(R.drawable.unselect_bg_dot);
                }
            } else {
                itemMyLocationBinding.ivSelectionIndicator.setVisibility(View.GONE);
            }

            // Keep the old chose indicator for backward compatibility if needed
            itemMyLocationBinding.ivChose.setVisibility(View.GONE);

            String latitude = model.getLatitude();
            String longitude = model.getLongitude();

            String latFormatted = String.format("%.6f", Double.parseDouble(latitude));
            String lonFormatted = String.format("%.6f", Double.parseDouble(longitude));

            itemMyLocationBinding.tvLatLong.setText("Lat: " + latFormatted + ", Long: " + lonFormatted);
            itemMyLocationBinding.tvAddress.setText(model.getAddress());
            itemMyLocationBinding.tvDate.setText(model.getDate());
            itemMyLocationBinding.tvTime.setText(model.getTime());
            itemMyLocationBinding.tvTitle.setText(model.getTitle());

            if (mMap != null) {
                mMap.clear();
                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                mMap.addMarker(new MarkerOptions().position(latLng).title("Current position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            }

            itemMyLocationBinding.btnCompass.setOnClickListener(view -> {
                if (!isSelectionMode) { // Only allow navigation when not in selection mode
                    if (listener != null) {
                        listener.onNavigationRequested(model);
                    }
                    openNavigationApp(model);
                }
            });

            itemMyLocationBinding.btnGoogleMaps.setOnClickListener(view -> {
                if (!isSelectionMode) { // Only allow maps when not in selection mode
                    if (listener != null) {
                        listener.onGoogleMapsRequested(model);
                    }
                    openGoogleMaps(model);
                }
            });

            itemMyLocationBinding.btnShare.setOnClickListener(view -> {
                if (!isSelectionMode) { // Only allow share when not in selection mode
                    if (listener != null) {
                        listener.onShareRequested(model);
                    }
                    shareLocation(model);
                }
            });

            this.itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onLocationClicked(model, isSelectionMode);

                    if (isSelectionMode) {
                        listener.onSelectionChanged(hasSelectedItems());
                    }
                }
            });

            // Remove long click listener as per requirement
            this.itemView.setOnLongClickListener(null);
        }

        private void openNavigationApp(MyLocation location) {
            String uri = String.format(
                    "https://www.google.com/maps/dir/?api=1&destination=%s,%s",
                    location.getLatitude(),
                    location.getLongitude()
            );
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                itemView.getContext().startActivity(intent);
            } else {
                itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        }

        private void openGoogleMaps(MyLocation location) {
            String uri = String.format("http://maps.google.com/maps?q=%s,%s",
                    location.getLatitude(),
                    location.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                itemView.getContext().startActivity(intent);
            } else {
                intent.setPackage(null);
                itemView.getContext().startActivity(intent);
            }
        }

        private void shareLocation(MyLocation location) {
            String shareText = String.format(
                    "📸 Captured with GeoCamera\n\n" +
                            "📍 %s\n" +
                            "🏠 Address: %s\n\n" +
                            "🗺 Open in Google Maps:\n" +
                            "https://maps.google.com/?q=%s,%s\n\n" +
                            "📲 Get the app: https://play.google.com/store/apps/details?id=com.yourpackage.name",
                    location.getTitle() != null ? location.getTitle() : "My Location",
                    location.getAddress() != null ? location.getAddress() : "Unknown Address",
                    location.getLatitude(),
                    location.getLongitude()
            );
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Share location via"));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getMMap() != null) {
            holder.getMMap().clear();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemSavedLocationBinding inflate = ItemSavedLocationBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                viewGroup,
                false
        );
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bind(this.list.get(viewHolder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }
}