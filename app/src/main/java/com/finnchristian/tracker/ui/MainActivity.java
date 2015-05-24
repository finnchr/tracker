package com.finnchristian.tracker.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.finnchristian.tracker.R;


public class MainActivity extends ActionBarActivity implements TracksFragment.Callback {
    private static final String TRACK_DETAILS_FRAGMENT_TAG = TrackDetailsInfoFragment.class.getSimpleName();

    private boolean twoPaneLayout;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        twoPaneLayout = findViewById(R.id.track_details_fragment_container) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if(twoPaneLayout) {
            //final _TrackDetailFragment fragment = _TrackDetailFragment.newInstance(trackId);
            final TrackDetailsViewPagerFragment fragment = TrackDetailsViewPagerFragment.newInstance(trackId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_details_fragment_container, fragment, TRACK_DETAILS_FRAGMENT_TAG)
                    .commit();

        }
        else {
            Intent intent = new Intent(this, TrackDetailsActivity.class);
            intent.putExtra("track_id", trackId);

            startActivity(intent);
        }
    }
}

