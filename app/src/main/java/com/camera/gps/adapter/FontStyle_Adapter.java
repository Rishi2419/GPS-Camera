package com.camera.gps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.premium.PremiumManager;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.Utils;

public class FontStyle_Adapter extends RecyclerView.Adapter<FontStyle_Adapter.SingleListItemHolder> {

    private final Context context;
    private final String[] fontList;
    private int selectedPos = -1;
    private final FontStyleListener listener;
    private final HelperClass helperClass = new HelperClass();

    public interface FontStyleListener {
        void onFontClick(int i);
    }

    public FontStyle_Adapter(Context context, String[] fontList, FontStyleListener listener) {
        this.context = context;
        this.fontList = fontList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SingleListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SingleListItemHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fontstyle, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SingleListItemHolder holder, int position) {
        holder.mItemDate.setTypeface(helperClass.getFontStyle(context, fontList[position]));

        // Set radio button state
        holder.radioSelect.setChecked(selectedPos == position);

        boolean shouldShowPremiumBadge = !PremiumManager.isPremium(context)
                && PremiumManager.isFontPremium(fontList[position]);
        holder.premium_img.setVisibility(shouldShowPremiumBadge ? View.VISIBLE : View.GONE);
        holder.radioSelect.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            selectedPos = adapterPosition;
            notifyDataSetChanged();
            listener.onFontClick(adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return fontList.length;
    }

    public String getFontName(int position) {
        return fontList[position];
    }

    public int getSelectedPosition() {
        return selectedPos;
    }

    public void setSelectedPosition(int position) {
        this.selectedPos = position;
    }

    // Optional: Method to get the currently selected font name
    public String getSelectedFontName() {
        if (selectedPos >= 0 && selectedPos < fontList.length) {
            return fontList[selectedPos];
        }
        return null;
    }

    public void updateSelection(int position) {
        this.selectedPos = position;
        notifyDataSetChanged();
    }

    static class SingleListItemHolder extends RecyclerView.ViewHolder {
        TextView mItemDate;
        RadioButton radioSelect;
        ImageView premium_img;

        SingleListItemHolder(View view) {
            super(view);
            mItemDate = view.findViewById(R.id.img_font);
            radioSelect = view.findViewById(R.id.radio_select);
            premium_img = view.findViewById(R.id.premium_img);
        }
    }
}
