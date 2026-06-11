//package com.example.gps.model;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.BitmapDescriptor;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.maps.android.clustering.Cluster;
//import com.google.maps.android.clustering.ClusterManager;
//import com.google.maps.android.clustering.view.DefaultClusterRenderer;
//import com.example.gps.util.Constant;
//
//import java.util.Collection;
//
//public final class PersonRenderer extends DefaultClusterRenderer<MarkerModel> implements GoogleMap.OnCameraMoveListener {
//    private final Context context;
//    private float currentZoomLevel;
//    private final GoogleMap mMap;
//    private final float maxZoomLevel;
//
//    public PersonRenderer(Context context, GoogleMap mMap, ClusterManager<MarkerModel> manager) {
//        super(context, mMap, manager);
//        this.context = context;
//        this.mMap = mMap;
//        this.maxZoomLevel = 18.0f;
//        mMap.setOnCameraMoveListener(this);
//    }
//
//    @Override
//    public void onClusterItemRendered(MarkerModel person, @NonNull Marker marker) {
//        person.setMarker(marker);
//        super.onClusterItemRendered(person, marker);
//    }
//
//    @Override
//    public void onBeforeClusterItemRendered(@NonNull MarkerModel person, MarkerOptions markerOptions) {
//        markerOptions.icon(getItemIcon(person, "")).title(person.getTitle());
//    }
//
//    @Override
//    public void onClusterItemUpdated(@NonNull MarkerModel person, Marker marker) {
//        marker.setIcon(getItemIcon(person, ""));
//        marker.setTitle(person.getTitle());
//        person.setMarker(marker);
//    }
//
//    private BitmapDescriptor getItemIcon(MarkerModel markerModel, String str) {
//        Bitmap bitmapFromCache = Constant.Companion.getBitmapFromCache(markerModel.getPath());
//        if (bitmapFromCache == null) {
//            bitmapFromCache = Constant.Companion.createUserBitmap(this.context, markerModel.getPath());
//            Constant.Companion companion = Constant.Companion;
//            companion.saveBitmapToCache(markerModel.getPath() + str, bitmapFromCache);
//        }
//        return BitmapDescriptorFactory.fromBitmap(bitmapFromCache);
//    }
//
//    @Override
//    protected void onBeforeClusterRendered(@NonNull Cluster<MarkerModel> cluster, @NonNull MarkerOptions markerOptions) {
//        Collection<MarkerModel> items = cluster.getItems();
//        MarkerModel markerModel = ((MarkerModel[]) items.toArray(new MarkerModel[0]))[0];
//        markerOptions.icon(getItemIcon(markerModel, String.valueOf(cluster.getSize())));
//        markerOptions.title(markerModel.getTitle());
//    }
//
//    @Override
//    protected void onClusterUpdated(@NonNull Cluster<MarkerModel> cluster, @NonNull Marker marker) {
//        Collection<MarkerModel> items = cluster.getItems();
//        MarkerModel markerModel = ((MarkerModel[]) items.toArray(new MarkerModel[0]))[0];
//        marker.setIcon(getItemIcon(markerModel, String.valueOf(cluster.getSize())));
//        marker.setTitle(markerModel.getTitle());
//    }
//
//    @Override
//    protected boolean shouldRenderAsCluster(@NonNull Cluster<MarkerModel> cluster) {
//        return this.currentZoomLevel < this.maxZoomLevel && cluster.getSize() > 1;
//    }
//
//    @Override
//    public void onCameraMove() {
//        this.currentZoomLevel = this.mMap.getCameraPosition().zoom;
//    }
//}

package com.camera.gps.model;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.camera.gps.util.Constant;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class PersonRenderer extends DefaultClusterRenderer<MarkerModel> implements GoogleMap.OnCameraMoveListener {
    private final Context context;
    private float currentZoomLevel;
    private final GoogleMap mMap;
    private final float maxZoomLevel;

    // Cache for consistent marker icons per location
    private final Map<String, BitmapDescriptor> iconCache = new HashMap<>();

    public PersonRenderer(Context context, GoogleMap mMap, ClusterManager<MarkerModel> manager) {
        super(context, mMap, manager);
        this.context = context;
        this.mMap = mMap;
        this.maxZoomLevel = 18.0f;
        mMap.setOnCameraMoveListener(this);
    }

    @Override
    public void onClusterItemRendered(MarkerModel person, @NonNull Marker marker) {
        person.setMarker(marker);
        super.onClusterItemRendered(person, marker);
    }

    @Override
    public void onBeforeClusterItemRendered(@NonNull MarkerModel person, MarkerOptions markerOptions) {
        BitmapDescriptor icon = getConsistentItemIcon(person);
        markerOptions.icon(icon).title(person.getTitle());
    }

    @Override
    public void onClusterItemUpdated(@NonNull MarkerModel person, Marker marker) {
        BitmapDescriptor icon = getConsistentItemIcon(person);
        marker.setIcon(icon);
        marker.setTitle(person.getTitle());
        person.setMarker(marker);
    }

    private BitmapDescriptor getConsistentItemIcon(MarkerModel markerModel) {
        String locationKey = markerModel.getLocationKey();

        // Check if we already have a cached icon for this location
        if (iconCache.containsKey(locationKey)) {
            return iconCache.get(locationKey);
        }

        // Create new icon and cache it
        BitmapDescriptor icon = createItemIcon(markerModel, "");
        iconCache.put(locationKey, icon);
        return icon;
    }

    private BitmapDescriptor createItemIcon(MarkerModel markerModel, String clusterSize) {
        // Create cache key that includes cluster size for different representations
        String cacheKey = markerModel.getPath() + "_" + clusterSize + "_" + markerModel.isVideo();

        Bitmap bitmapFromCache = Constant.Companion.getBitmapFromCache(cacheKey);
        if (bitmapFromCache == null) {
            try {
                bitmapFromCache = Constant.Companion.createUserBitmap(this.context, markerModel.getPath());
                if (bitmapFromCache != null) {
                    Constant.Companion.saveBitmapToCache(cacheKey, bitmapFromCache);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Create a default bitmap if creation fails
                bitmapFromCache = createDefaultBitmap();
            }
        }

        if (bitmapFromCache == null) {
            bitmapFromCache = createDefaultBitmap();
        }

        return BitmapDescriptorFactory.fromBitmap(bitmapFromCache);
    }

    private Bitmap createDefaultBitmap() {
        // Create a simple default bitmap
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(android.graphics.Color.BLUE);
        paint.setAntiAlias(true);
        canvas.drawCircle(50, 50, 50, paint);
        return bitmap;
    }

    @Override
    protected void onBeforeClusterRendered(@NonNull Cluster<MarkerModel> cluster, @NonNull MarkerOptions markerOptions) {
        Collection<MarkerModel> items = cluster.getItems();
        if (items.isEmpty()) return;

        // Get the most recent item from the cluster for consistent representation
        MarkerModel representativeItem = null;
        long latestTimestamp = 0;

        for (MarkerModel item : items) {
            if (item.getTimestamp() > latestTimestamp) {
                latestTimestamp = item.getTimestamp();
                representativeItem = item;
            }
        }

        if (representativeItem != null) {
            BitmapDescriptor icon = createItemIcon(representativeItem, String.valueOf(cluster.getSize()));
            markerOptions.icon(icon);
            markerOptions.title(representativeItem.getTitle() + " (+" + (cluster.getSize() - 1) + " more)");
        }
    }

    @Override
    protected void onClusterUpdated(@NonNull Cluster<MarkerModel> cluster, @NonNull Marker marker) {
        Collection<MarkerModel> items = cluster.getItems();
        if (items.isEmpty()) return;

        // Get the most recent item from the cluster for consistent representation
        MarkerModel representativeItem = null;
        long latestTimestamp = 0;

        for (MarkerModel item : items) {
            if (item.getTimestamp() > latestTimestamp) {
                latestTimestamp = item.getTimestamp();
                representativeItem = item;
            }
        }

        if (representativeItem != null) {
            BitmapDescriptor icon = createItemIcon(representativeItem, String.valueOf(cluster.getSize()));
            marker.setIcon(icon);
            marker.setTitle(representativeItem.getTitle() + " (+" + (cluster.getSize() - 1) + " more)");
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<MarkerModel> cluster) {
        return this.currentZoomLevel < this.maxZoomLevel && cluster.getSize() > 1;
    }

    @Override
    public void onCameraMove() {
        this.currentZoomLevel = this.mMap.getCameraPosition().zoom;
    }

    // Method to clear icon cache when needed (e.g., when refreshing data)
    public void clearIconCache() {
        iconCache.clear();
    }
}