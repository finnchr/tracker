package com.finnchristian.tracker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
//import android.app.Fragment;
//import android.app.LoaderManager;
//import android.content.CursorLoader;
//import android.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.data.TracksAdapter;
import com.finnchristian.tracker.data.TracksProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class TracksFragment extends Fragment {
    public interface Callback {
        void onTrackSelected(final int trackId);
    }

    private static final String TAG = TracksFragment.class.getSimpleName();
    private static final String SELECTED_POSITION = "SELECTED_POSITION";
    private static final int TRACKS_LOADER = 0;

    private Loader<Cursor> loader;

    protected View rootView;
    protected ListView tracksListView;
    protected TracksAdapter tracksAdapter;

    private int selectedPosition = -1;

    public static Fragment newInstance() {
        return new TracksFragment();
    }


    public TracksFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // let the os know that we have menu items
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        tracksAdapter = new TracksAdapter(getActivity(), null, 0, new TracksAdapter.OnRemoveTrackListener() {
            @Override
            public void onClick(final int trackId, final String name) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.delete_track_title, name))
                        .setCancelable(true)
                        .setPositiveButton(R.string.delete_track_delete_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ContentResolverHelper(getActivity().getContentResolver()).deleteTrack(trackId);
                            }
                        }).show();
            }
        });

        rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        tracksListView = (ListView)rootView.findViewById(R.id.tracks_list_view);

        tracksListView.setAdapter(tracksAdapter);
        tracksListView.setOnItemClickListener(onTrackItemClickListener);

        // read last know position
        selectedPosition = (savedInstanceState != null)
                ? savedInstanceState.getInt(SELECTED_POSITION, selectedPosition)
                : selectedPosition;

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(SELECTED_POSITION, selectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loader = getLoaderManager().initLoader(TRACKS_LOADER, null, new LoaderCallback(getActivity()));

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.tracksfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_track:
                createNewTrack();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Create new track.
     */
    protected void createNewTrack() {
        CreateTrackDialogFragment.newInstance().show(getFragmentManager(), null);
    }


    protected AdapterView.OnItemClickListener onTrackItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Activity activity = getActivity();

            if(activity instanceof Callback) {
                selectedPosition = position;

                final Cursor item = (Cursor) tracksAdapter.getItem(position);
                final int trackId = item.getInt(item.getColumnIndex("_id"));

                // See MainActivity.onTrackSelected
                ((Callback) activity).onTrackSelected(trackId);
            }
        }
    };

    /**
     * Implementation of LoaderCallbacks. Handle
     */
    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        Activity ownerActivity;

        public LoaderCallback(Activity ownerActivity) {
            this.ownerActivity = ownerActivity;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(ownerActivity,
                    TracksProvider.Uris.TRACKS_CONTENT_URI,
                    new String[] {"_id", "name", "created"}, // Projection
                    null, // Selection
                    null, // Selection args
                    null // Sort order
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            tracksAdapter.swapCursor(data);

            // Set selected item (if device has been rotated)
            if(selectedPosition >= 0) {
                tracksListView.setSelection(selectedPosition);
                tracksListView.setItemChecked(selectedPosition, true);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            tracksAdapter.swapCursor(null);
        }
    }
}
