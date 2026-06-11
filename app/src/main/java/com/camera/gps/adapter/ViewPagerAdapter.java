package com.camera.gps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camera.gps.R;

import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Integer> imageList;
    private final String[] title_list;
    private final String[] desc_list;

    public ViewPagerAdapter(Context context, ArrayList<Integer> imageList, String[] title_list, String[] desc_list) {
        this.context = context;
        this.imageList = imageList;
        this.title_list = title_list;
        this.desc_list = desc_list;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboard_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(imageList.get(position)).into(holder.sliderImage);
        holder.sliderTitle.setText(title_list[position]);
        holder.sliderDesc.setText(desc_list[position]);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sliderImage;
        TextView sliderTitle;
        TextView sliderDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sliderImage = itemView.findViewById(R.id.sliderImage);
            sliderTitle = itemView.findViewById(R.id.sliderTitle);
            sliderDesc = itemView.findViewById(R.id.sliderDesc);
        }
    }
}
