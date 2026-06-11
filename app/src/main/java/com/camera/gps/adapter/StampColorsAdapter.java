package com.camera.gps.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;

import java.util.ArrayList;
import java.util.List;

public class StampColorsAdapter extends RecyclerView.Adapter<StampColorsAdapter.ColorViewHolder> {
    private List<Integer> colors;
    private OnColorClickListener listener;
    private int selectedColor = -1;
    private Context context;

    public interface OnColorClickListener {
        void onColorClick(int color);
    }

    public StampColorsAdapter(Context context, List<Integer> colors) {
        this.context = context;
        this.colors = new ArrayList<>(colors);
    }

    public void setOnColorClickListener(OnColorClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    public void updateColors(List<Integer> newColors) {
        this.colors = new ArrayList<>(newColors);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_circle, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int color = colors.get(position);
        holder.bind(color, color == selectedColor);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    class ColorViewHolder extends RecyclerView.ViewHolder {
        private ImageView colorCircle;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorCircle = itemView.findViewById(R.id.colorCircle);
        }

        void bind(int color, boolean isSelected) {
            // Create the main color circle
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setShape(GradientDrawable.OVAL);
            colorDrawable.setColor(color);

            if (isSelected) {
                // Create selection ring
                GradientDrawable selectionRing = new GradientDrawable();
                selectionRing.setShape(GradientDrawable.OVAL);
                selectionRing.setStroke(dpToPx(2), Color.parseColor("#00A1F2")); // Blue selection ring
                selectionRing.setColor(Color.TRANSPARENT);

                // Create inner color circle (slightly smaller to show the ring)
                GradientDrawable innerColorDrawable = new GradientDrawable();
                innerColorDrawable.setShape(GradientDrawable.OVAL);
                innerColorDrawable.setColor(color);

                // Layer them together
                LayerDrawable layerDrawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{
                        selectionRing, innerColorDrawable
                });

                // Add padding to inner circle to show the selection ring
                int padding = dpToPx(4);
                layerDrawable.setLayerInset(1, padding, padding, padding, padding);

                colorCircle.setBackground(layerDrawable);
            } else {
                // Just the color circle with a subtle border
                colorDrawable.setStroke(dpToPx(1), Color.parseColor("#E5E7EB"));
                colorCircle.setBackground(colorDrawable);
            }

            colorCircle.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onColorClick(color);
                }
            });
        }

        private int dpToPx(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}