package com.finnchristian.tracker.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.service.TrackerService;


public class MainActivity extends ActionBarActivity implements TracksFragment.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TRACK_DETAILS_FRAGMENT_TAG = TrackDetailsInfoFragment.class.getSimpleName();
    public static final String ARG_TRACK_ID = "MainActivity.ARG_TRACK_ID";

    private boolean twoPaneLayout;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         // Do we have two pane layout?
        twoPaneLayout = findViewById(R.id.track_details_fragment_container) != null;

        // Select track (by id) if we're sent here by the notification
        final Intent intent = getIntent();
        if(intent != null) {
            final int trackId = intent.getIntExtra(MainActivity.ARG_TRACK_ID, -1);
            if(trackId >= 0) {
                onTrackSelected(trackId);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTrackSelected(final int trackId) {
        final TrackDetailsViewPagerFragment fragment = TrackDetailsViewPagerFragment.newInstance(trackId);

        if(twoPaneLayout) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_details_fragment_container, fragment, TRACK_DETAILS_FRAGMENT_TAG)
                    .commit();

        }
        else {
            Intent intent = new Intent(this, TrackDetailsActivity.class);
            intent.putExtra(TrackDetailsActivity.ARG_TRACK_ID, trackId);

            startActivity(intent);
        }
    }
}

