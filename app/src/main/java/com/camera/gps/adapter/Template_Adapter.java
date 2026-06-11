package com.camera.gps.adapter;

import static com.camera.gps.activity.Template_Activity.current_stamp_id;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.activity.Template_Activity;
import com.camera.gps.util.Utils;

import java.util.ArrayList;

public class Template_Adapter extends RecyclerView.Adapter<Template_Adapter.ViewHolder> {

    ArrayList<Integer> arrayList;
    final TemplateClicksListener themeClicksListener;
    Context context;
    Template_Activity activity;
    private int clickedPosition = -1; // Track which item was clicked

    public interface TemplateClicksListener {
        void onThemeClick(int i);
    }

    public Template_Adapter(ArrayList<Integer> arrayList, TemplateClicksListener themeClicksListener, Context context) {
        this.arrayList = arrayList;
        this.themeClicksListener = themeClicksListener;
        this.context = context;
        this.activity = (Template_Activity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.imageView.setImageResource(arrayList.get(position));

        if (position == 0 || position == 3 || position == 7 || position == 4) {
            holder.premium_img.setVisibility(View.GONE);
        } else if (Utils.getIsPremium(context)) {
            holder.premium_img.setVisibility(View.GONE);
        } else {
            holder.premium_img.setVisibility(View.VISIBLE);
        }

        // Get currently saved stamp ID
        int savedStampId = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);

//        // Remove white tick - not needed anymore
//        holder.img_select.setVisibility(View.GONE);

        // Determine overlay visibility based on both saved selection and click
        boolean shouldShowOverlay = false;
        boolean showOnlyEdit = false;

        if (savedStampId == position + 1) {
            // This is the currently saved/selected template
            if (clickedPosition == position || clickedPosition == -1) {
                shouldShowOverlay = true;
                showOnlyEdit = true; // Only show Edit button for already selected
            }
        } else if (clickedPosition == position) {
            // This item was clicked but not yet selected
            shouldShowOverlay = true;
            showOnlyEdit = false; // Show both Edit and Use buttons
        }

        if (shouldShowOverlay) {
            holder.overlayLayout.setVisibility(View.VISIBLE);
            if (showOnlyEdit) {
                holder.btnUse.setVisibility(View.GONE);
                holder.btnEdit.setVisibility(View.VISIBLE);
            } else {
                holder.btnUse.setVisibility(View.VISIBLE);
                holder.btnEdit.setVisibility(View.VISIBLE);
            }
        } else {
            holder.overlayLayout.setVisibility(View.GONE);
        }

        // Setup overlay button clicks
        holder.btnUse.setOnClickListener(v -> {
            activity.onUseClicked(position);
            clickedPosition = -1; // Reset after use
        });

        holder.btnEdit.setOnClickListener(v -> {
            activity.onEditClicked(position);
            clickedPosition = -1; // Reset after edit
        });

        holder.itemView.setOnClickListener(v -> {
            if (position == 0 || position == 3 || position == 7 || position == 4) {
                handleItemClick(position);
            } else if (Utils.getIsPremium(context)) {
                handleItemClick(position);
            } else {
                handleItemClick(position);
                //Toast.makeText(context, "Please subscribe to access this feature", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleItemClick(int position) {
        // Set the clicked position and refresh the adapter
        clickedPosition = position;
        notifyDataSetChanged();
    }

    public void hideAllOverlays() {
        clickedPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
//        ImageView img_select;
        ImageView premium_img;
        LinearLayout overlayLayout;
        TextView btnUse;
        TextView btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewMain);
            premium_img = itemView.findViewById(R.id.premium_img);
            overlayLayout = itemView.findViewById(R.id.overlay_layout);
            btnUse = itemView.findViewById(R.id.btn_use);
            btnEdit = itemView.findViewById(R.id.btn_edit);

            // Initialize img_select even though we're not using it
            // to avoid null pointer exceptions
//            img_select = itemView.findViewById(R.id.img_select);
//            if (img_select == null) {
//                // Create a dummy ImageView if it doesn't exist in layout
//                img_select = new ImageView(itemView.getContext());
//            }
        }
    }
}