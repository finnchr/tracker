package com.finnchristian.tracker.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.finnchristian.tracker.R;

import java.text.DateFormat;
import java.util.Date;

public class TracksAdapter extends CursorAdapter {
    public static interface OnRemoveTrackListener {
        void onClick(final int trackId, final String name);
    }

    protected OnRemoveTrackListener onRemoveTrackListener;

    public TracksAdapter(Context context, Cursor c, int flags, final OnRemoveTrackListener onRemoveTrackListener) {
        super(context, c, flags);
        this.onRemoveTrackListener = onRemoveTrackListener;
    }

    public void setOnRemoveTrackListener(final OnRemoveTrackListener onRemoveTrackListener) {
        this.onRemoveTrackListener = onRemoveTrackListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.tracks_list_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(Database.Tracks.Columns._ID));
        final String name = cursor.getString(cursor.getColumnIndex(Database.Tracks.Columns.NAME));
        final Date created = new Date(cursor.getLong(cursor.getColumnIndex(Database.Tracks.Columns.CREATED)));

        final String createdDateFormatted = context.getString(R.string.date_format_long, created);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.titleTextView.setText(name);
        //viewHolder.dateTextView.setText(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(created));
        viewHolder.dateTextView.setText(createdDateFormatted);
        //viewHolder.kmTextView.setText("2,34 km");
        //viewHolder.durationTextView.setText("1,5 time");
        viewHolder.removeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onRemoveTrackListener != null) {
                    onRemoveTrackListener.onClick(id, name);
                }
            }
        });
    }

    private static class ViewHolder {
        public final TextView titleTextView;
        public final TextView dateTextView;
        //public final TextView kmTextView;
        //public final TextView durationTextView;
        public final ImageView removeImageView;

        public ViewHolder(View view) {
            titleTextView = (TextView) view.findViewById(R.id.track_listitem_title_text);
            dateTextView = (TextView) view.findViewById(R.id.track_listitem_date_text);
            //kmTextView = (TextView) view.findViewById(R.id.track_listitem_km_text);
            //durationTextView = (TextView) view.findViewById(R.id.track_listitem_duration_text);
            removeImageView = (ImageView) view.findViewById(R.id.track_listitem_remove_imageview);
        }
    }
}
