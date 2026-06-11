//package com.example.gps.model;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.maps.android.clustering.ClusterItem;
//
//import java.io.Serializable;
//
//public final class MarkerModel implements ClusterItem, Serializable {
//    private Marker marker;
//    private final String path;
//    private final LatLng position;
//    private final String snippet;
//    private String title;
//
//    public Float getZIndex() {
//        return 0.0f;
//    }
//
//    public MarkerModel(double d, double d2, String path, String title, String snippet) {
//        this.position = new LatLng(d, d2);
//        this.title = title;
//        this.snippet = snippet;
//        this.path = path;
//    }
//
//    public String getPath() {
//        return this.path;
//    }
//
//    public void setTitle(String ttl) {
//        this.title = ttl;
//    }
//
//    public void setMarker(Marker mk) {
//        this.marker = mk;
//    }
//
//    public Marker getMarker() {
//        return this.marker;
//    }
//
//    @NonNull
//    @Override
//    public LatLng getPosition() {
//        return this.position;
//    }
//
//    @Override
//    public String getTitle() {
//        return this.title;
//    }
//
//    @Override
//    public String getSnippet() {
//        return this.snippet;
//    }
//}

package com.camera.gps.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public final class MarkerModel implements ClusterItem, Serializable {
    private Marker marker;
    private final String path;
    private final LatLng position;
    private final String snippet;
    private String title;
    private final boolean isVideo;
    private final long timestamp; // For consistent ordering

    public Float getZIndex() {
        return 0.0f;
    }

    public MarkerModel(double d, double d2, String path, String title, String snippet) {
        this.position = new LatLng(d, d2);
        this.title = title;
        this.snippet = snippet;
        this.path = path;
        this.isVideo = snippet != null && snippet.equals("video");
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor with explicit video flag and timestamp
    public MarkerModel(double d, double d2, String path, String title, String snippet, boolean isVideo, long timestamp) {
        this.position = new LatLng(d, d2);
        this.title = title;
        this.snippet = snippet;
        this.path = path;
        this.isVideo = isVideo;
        this.timestamp = timestamp;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isVideo() {
        return this.isVideo;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTitle(String ttl) {
        this.title = ttl;
    }

    public void setMarker(Marker mk) {
        this.marker = mk;
    }

    public Marker getMarker() {
        return this.marker;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return this.position;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getSnippet() {
        return this.snippet;
    }

    // Generate a unique key for location-based caching
    public String getLocationKey() {
        return String.format("%.6f,%.6f", position.latitude, position.longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MarkerModel that = (MarkerModel) obj;
        return getLocationKey().equals(that.getLocationKey());
    }

    @Override
    public int hashCode() {
        return getLocationKey().hashCode();
    }
}