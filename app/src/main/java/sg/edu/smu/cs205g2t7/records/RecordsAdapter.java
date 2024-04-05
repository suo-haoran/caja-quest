package sg.edu.smu.cs205g2t7.records;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.edu.smu.cs205g2t7.db.PlayerRecordDbHelper;
import sg.edu.smu.cs205g2t7.R;
/**
 * An adapter class that acts as a bridge between the database
 * and the RecyclerView and ListView component
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    /** Used for logging */
    private static final String TAG = "RecordsAdapter";
    /** package private variable that stores records */
    List<Record> records;
    /**
     * Populate the database with the past game records
     * @param context - App context
     */
    public RecordsAdapter(Context context) {
        try (PlayerRecordDbHelper dbHelper = new PlayerRecordDbHelper(context)) {
            records = dbHelper.getRecords();
        } catch (IllegalArgumentException e) {
            // Very useful error messages
            Log.e("DB", "ERROR: error occurred while retrieving records. You might want to look into PlayerRecordDbHelper#getRecords");
            throw new IllegalStateException("Error Occurred while retrieving records");
        }
    }
    /**
     * Create a new viewholder when there are no existing view holders, or reuse an existing view
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return viewHolder Wrapper around the current View
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_list_item, parent, false);

        return new ViewHolder(view);
    }
    /**
     * Bind data from the database to the view
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        Record record = records.get(position);
        Log.d(TAG, "onBindViewHolder: " + record.getId());
        holder.getTextViewId().setText(String.format("%d", record.getId()));
        Log.d(TAG, "onBindViewHolder: " + record.getTiming());
        holder.getTextViewTiming().setText(String.format("%.02f", record.getTiming()));
    }
    /**
     * @return count - the number of game records
     */
    @Override
    public int getItemCount() {
        return records.size();
    }

    /**
     * ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** Text field for id */
        private final TextView textViewId;
        /** Text field for timing */
        private final TextView textViewTiming;
        /**
         * Constructor for instantiating a ViewHolder instance
         * @param view the current view object
         */
        public ViewHolder(View view) {
            super(view);
            textViewId = view.findViewById(R.id.txt_record_id);
            textViewTiming = view.findViewById(R.id.txt_timing);
        }
        /**
         * Getter for view holder
         * @return id of textview
         */
        public TextView getTextViewId() {
            return textViewId;
        }
        /**
         * Getter for view timing
         * @return id of view timing
         */
        public TextView getTextViewTiming() {
            return textViewTiming;
        }

    }
}
