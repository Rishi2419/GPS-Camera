package com.camera.gps.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.listener.LanguageListener;
import com.camera.gps.model.LanguageSelectModel;

import java.util.ArrayList;


public class Language_Adapter extends RecyclerView.Adapter<Language_Adapter.LanguageHolder> {
    public Context context;
    ArrayList<LanguageSelectModel> itemList;
    public LanguageListener listener;
    private String selectedLanguageCode;

    public void setLanguageListener(LanguageListener listener) {
        this.listener = listener;
    }

    public void setSelectedLanguageCode(String selectedLanguageCode) {
        this.selectedLanguageCode = selectedLanguageCode;
    }

    public Language_Adapter(Context context, ArrayList<LanguageSelectModel> itemList, String selectedLanguageCode) {
        this.context = context;
        this.itemList = itemList;
        this.selectedLanguageCode = selectedLanguageCode;
    }

    @NonNull
    @Override
    public LanguageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LanguageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull LanguageHolder holder, int position) {

        holder.txtLanguage.setText(itemList.get(position).langName);
        holder.txtLangDesc.setText(itemList.get(position).langSubname);
        holder.imgFlag.setImageResource(itemList.get(position).flagIcon);


        if (itemList.get(position).langCode.equals(selectedLanguageCode)) {
            holder.cardView.setBackgroundResource(R.drawable.bg_card_selected);
        } else {
            holder.cardView.setBackgroundResource(R.drawable.bg_card_unselected);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                selectedLanguageCode = itemList.get(position).langCode;
                notifyDataSetChanged();
                listener.language(itemList.get(position).langCode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class LanguageHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView txtLanguage, txtLangDesc;
        public AppCompatImageView imgFlag;
        public View cardView;

        @SuppressLint("WrongViewCast")
        public LanguageHolder(@NonNull View itemView) {
            super(itemView);
            txtLanguage = itemView.findViewById(R.id.tv_title);
            txtLangDesc = itemView.findViewById(R.id.tv_body);
            imgFlag = itemView.findViewById(R.id.imgflag);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}