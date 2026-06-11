package com.camera.gps.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SP;

public class DateTimeFormat_Adapter extends RecyclerView.Adapter<DateTimeFormat_Adapter.MyViewHolder> {
    private HelperClass helperClass = new HelperClass();
    private Context mContext;
    private SP msp;
    private ArrayList<DateFormatModel> time_list;
    private final DateClicksListener dateClicksListener;

    public interface DateClicksListener {
        void onDateClick(int i);
    }


    public DateTimeFormat_Adapter(Context context, ArrayList<DateFormatModel> arrayList, DateClicksListener dateClicksListener) {
        this.mContext = context;
        this.time_list = arrayList;
        this.dateClicksListener = dateClicksListener;
        this.msp = new SP(context);
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_date_format, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") final int i) {
        DateFormatModel dateFormatModel = this.time_list.get(i);

        // Set radio button state based on selection
        myViewHolder.radio_select.setChecked(dateFormatModel.getSelected() == 1);

        // Set the formatted date text
        myViewHolder.txt_note.setText(this.helperClass.setDateTime(
                dateFormatModel.getTime_format(),
                this.msp.getString(this.mContext, MyApplication.TIMEZONE_FORMAT,
                        this.time_list.size() > 5 ? this.time_list.get(5).getTime_format() : "GMT"),
                this.mContext));

        // Set click listener
        myViewHolder.btn_click.setOnClickListener(view -> {
            if (dateClicksListener != null) {
                dateClicksListener.onDateClick(i);
            }
        });
    }

    public void updateData(ArrayList<DateFormatModel> newList) {
        this.time_list = newList;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return this.time_list.size();
    }

    // Method to get selected position
    public int getSelectedPosition() {
        for (int i = 0; i < time_list.size(); i++) {
            if (time_list.get(i).getSelected() == 1) {
                return i;
            }
        }
        return -1;
    }

    // Method to update selection
    public void updateSelection(int position) {
        for (int i = 0; i < time_list.size(); i++) {
            time_list.get(i).setSelected(i == position ? 1 : 0);
        }
        notifyDataSetChanged();
    }

    // Method to get selected format
    public DateFormatModel getSelectedFormat() {
        int selectedPos = getSelectedPosition();
        if (selectedPos >= 0 && selectedPos < time_list.size()) {
            return time_list.get(selectedPos);
        }
        return null;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout btn_click;
        RadioButton radio_select;
        TextView txt_note;

        public MyViewHolder(View view2) {
            super(view2);
            this.txt_note = view2.findViewById(R.id.txt_notes);
            this.radio_select = view2.findViewById(R.id.radio_select);
            this.btn_click = view2.findViewById(R.id.btn_click);
        }
    }
}