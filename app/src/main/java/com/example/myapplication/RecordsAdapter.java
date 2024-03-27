package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private static final String TAG = "RecordsAdapter";
    List<Record> records;
    public RecordsAdapter(Context context) {
        try (PlayerRecordDbHelper dbHelper = new PlayerRecordDbHelper(context)) {
            records = dbHelper.getRecords();
        } catch (IllegalArgumentException e) {
            // Very useful error messages
            Log.e("DB", "ERROR: error occurred while retrieving records. You might want to look into PlayerRecordDbHelper#getRecords");
            throw new IllegalStateException("Error Occurred while retrieving records");
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_list_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        Record record = records.get(position);
        Log.d(TAG, "onBindViewHolder: " + record.getId());
        holder.getTextViewId().setText(String.format("%d", record.getId()));
        Log.d(TAG, "onBindViewHolder: " + record.getTiming());
        holder.getTextViewTiming().setText(String.format("%.2f", record.getTiming()));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewId;
        private final TextView textViewTiming;
        public ViewHolder(View view) {
            super(view);
            textViewId = view.findViewById(R.id.txt_record_id);
            textViewTiming = view.findViewById(R.id.txt_timing);
        }

        public TextView getTextViewId() {
            return textViewId;
        }

        public TextView getTextViewTiming() {
            return textViewTiming;
        }

    }
}
