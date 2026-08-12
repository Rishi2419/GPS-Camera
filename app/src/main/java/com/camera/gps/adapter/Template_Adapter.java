package com.camera.gps.adapter;

import static com.camera.gps.activity.Template_Activity.current_stamp_id;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.adsmanager.NativeAdManager;
import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.activity.Template_Activity;
import com.camera.gps.premium.PremiumManager;
import com.camera.gps.util.Utils;

import java.util.ArrayList;

public class Template_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEMPLATE = 1;
    private static final int VIEW_TYPE_NATIVE_AD = 2;
    private static final int FIRST_NATIVE_AD_POSITION = 2;
    private static final int SECOND_NATIVE_AD_POSITION = 6;

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_NATIVE_AD) {
            return new NativeAdViewHolder(inflater.inflate(R.layout.item_native_ad_container, parent, false));
        }
        return new ViewHolder(inflater.inflate(R.layout.item_template, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return isNativeAdPosition(position) ? VIEW_TYPE_NATIVE_AD : VIEW_TYPE_TEMPLATE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHolder, @SuppressLint("RecyclerView") int position) {
        if (recyclerHolder instanceof NativeAdViewHolder) {
            ((NativeAdViewHolder) recyclerHolder).bind();
            return;
        }

        ViewHolder holder = (ViewHolder) recyclerHolder;
        int templatePosition = getTemplatePosition(position);
        holder.imageView.setImageResource(arrayList.get(templatePosition));

        boolean shouldShowPremiumBadge = PremiumManager.isTemplatePremium(templatePosition + 1)
                && !PremiumManager.isPremium(context);
        holder.premium_img.setVisibility(shouldShowPremiumBadge ? View.VISIBLE : View.GONE);

        // Get currently saved stamp ID
        int savedStampId = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);

//        // Remove white tick - not needed anymore
//        holder.img_select.setVisibility(View.GONE);

        // Determine overlay visibility based on both saved selection and click
        boolean shouldShowOverlay = false;
        boolean showOnlyEdit = false;

        if (savedStampId == templatePosition + 1) {
            // This is the currently saved/selected template
            if (clickedPosition == templatePosition || clickedPosition == -1) {
                shouldShowOverlay = true;
                showOnlyEdit = true; // Only show Edit button for already selected
            }
        } else if (clickedPosition == templatePosition) {
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
            activity.onUseClicked(templatePosition);
            clickedPosition = -1; // Reset after use
        });

        holder.btnEdit.setOnClickListener(v -> {
            activity.onEditClicked(templatePosition);
            clickedPosition = -1; // Reset after edit
        });

        holder.itemView.setOnClickListener(v -> {
            // Free users may select premium templates for an exact camera preview. Capture is
            // centrally gated in MainActivity.
            handleItemClick(templatePosition);
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
        return arrayList.size() + getNativeAdCount();
    }

    private boolean isNativeAdPosition(int position) {
        return position == FIRST_NATIVE_AD_POSITION
                || position == SECOND_NATIVE_AD_POSITION
                || position == getItemCount() - 1;
    }

    private int getNativeAdCount() {
        return arrayList.isEmpty() ? 0 : 3;
    }

    private int getTemplatePosition(int adapterPosition) {
        int nativeAdsBeforePosition = 0;
        if (adapterPosition > FIRST_NATIVE_AD_POSITION) {
            nativeAdsBeforePosition++;
        }
        if (adapterPosition > SECOND_NATIVE_AD_POSITION) {
            nativeAdsBeforePosition++;
        }
        if (adapterPosition > getItemCount() - 1) {
            nativeAdsBeforePosition++;
        }
        return adapterPosition - nativeAdsBeforePosition;
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

    public class NativeAdViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout container;
        private boolean requested;

        public NativeAdViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.flNativeAd);
        }

        public void bind() {
            if (!(context instanceof android.app.Activity)
                    || !MyApplication.isNetworkAvailable(context)
                    || Utils.getIsPremium(context)) {
                container.setVisibility(View.GONE);
                return;
            }

            container.setVisibility(View.VISIBLE);
            if (!requested) {
                requested = true;
                NativeAdManager.getInstance().loadAndShowNativeAd(
                        (android.app.Activity) context,
                        "template_list_native",
                        container,
                        true,
                        null,
                        () -> requested = false
                );
            }
        }
    }
}
