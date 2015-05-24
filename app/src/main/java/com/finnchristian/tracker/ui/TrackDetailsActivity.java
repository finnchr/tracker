package com.finnchristian.tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.finnchristian.tracker.R;


public class TrackDetailsActivity extends ActionBarActivity {
    private static final String TAG = TrackDetailsActivity.class.getSimpleName();

    /*
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackerService.TrackerServiceBinder binder = (TrackerService.TrackerServiceBinder) service;
            trackerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackerService = null;
        }
    };

    protected TrackerService trackerService = null;
    private Intent trackerServiceIntent = null;
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int trackId = intent.getIntExtra("track_id", 0);
        //_TrackDetailFragment trackDetailFragment = _TrackDetailFragment.newInstance(trackId);
        TrackDetailsViewPagerFragment trackDetailsViewPagerFragment = TrackDetailsViewPagerFragment.newInstance(trackId);

        setContentView(R.layout.activity_track_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_details_fragment_container, trackDetailsViewPagerFragment)
                    .commit();
        }

        /*
        // Start tracker service
        if(trackerServiceIntent == null) {
            trackerServiceIntent = new Intent(this, TrackerService.class);
            final ComponentName componentName = startService(trackerServiceIntent);

            if(componentName != null) {
                Log.d(TAG, "Tracker service successfully started");
                Log.d(TAG, componentName.getPackageName());
                Log.d(TAG, componentName.getClassName());
                Log.d(TAG, componentName.getShortClassName());
            }
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        final Intent trackerServiceIntent = new Intent(this, TrackerService.class);
        final boolean success = bindService(trackerServiceIntent, serviceConnection, 0);

        if(!success) {
            Log.e(TAG, "Failed to bind with tracker service");
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();

        /*
        unbindService(serviceConnection);

        // Stop tracker service if it isn't tracking
        if(trackerService != null && !trackerService.isTracking()) {
            final boolean successfullyStopped = stopService(trackerServiceIntent);

            if(successfullyStopped) {
                Log.d(TAG, "Tracker service successfully stopped");
            }
        }
        */
    }
}
