package com.finnchristian.tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.service.TrackerService;


public class TrackDetailsActivity extends ActionBarActivity {
    private static final String TAG = TrackDetailsActivity.class.getSimpleName();
    public static final String ARG_TRACK_ID = "TrackDetailsActivity.ARG_TRACK_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final int trackId = intent.getIntExtra(TrackDetailsActivity.ARG_TRACK_ID, -1);
        TrackDetailsViewPagerFragment trackDetailsViewPagerFragment = TrackDetailsViewPagerFragment.newInstance(trackId);

        setContentView(R.layout.activity_track_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_details_fragment_container, trackDetailsViewPagerFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
