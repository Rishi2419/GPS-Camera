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
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.Utils;

public class FontStyle_Adapter extends RecyclerView.Adapter<FontStyle_Adapter.SingleListItemHolder> {

    private static final boolean HIDE_PREMIUM_BADGES_FOR_RELEASE = true;

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

        boolean shouldShowPremiumBadge = !(Utils.getIsPremium(context) || position == 0 || position ==1 || position == 4 || position == 5 || position == 6 || position ==8);
        holder.premium_img.setVisibility(shouldShowPremiumBadge && !HIDE_PREMIUM_BADGES_FOR_RELEASE ? View.VISIBLE : View.GONE);
        holder.radioSelect.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (Utils.getIsPremium(context) || position == 0 || position ==1 || position == 4 || position == 5 || position == 6 || position ==8) {
                selectedPos = position;
                notifyDataSetChanged();
                listener.onFontClick(position);
            } else {
                selectedPos = position;
                notifyDataSetChanged();
                listener.onFontClick(position);
                //Toast.makeText(context, "Please subscribe to access this feature", Toast.LENGTH_SHORT).show();
            }
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
