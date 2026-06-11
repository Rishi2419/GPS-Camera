package com.camera.gps.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.camera.gps.R;

public final class ItemDateHeaderBinding {
    @NonNull
    private final View rootView;
    @NonNull
    public final TextView tvDateHeader;

    private ItemDateHeaderBinding(@NonNull View rootView, @NonNull TextView tvDateHeader) {
        this.rootView = rootView;
        this.tvDateHeader = tvDateHeader;
    }

    @NonNull
    public View getRoot() {
        return rootView;
    }

    @NonNull
    public static ItemDateHeaderBinding inflate(@NonNull LayoutInflater inflater) {
        return inflate(inflater, null, false);
    }

    @NonNull
    public static ItemDateHeaderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
        View root = inflater.inflate(R.layout.item_date_header, parent, false);
        if (attachToParent) {
            parent.addView(root);
        }
        return bind(root);
    }

    @NonNull
    public static ItemDateHeaderBinding bind(@NonNull View rootView) {
        TextView tvDateHeader = rootView.findViewById(R.id.tvDateHeader);
        if (tvDateHeader == null) {
            throw new NullPointerException("Missing required view with ID: tvDateHeader");
        }
        return new ItemDateHeaderBinding(rootView, tvDateHeader);
    }
}