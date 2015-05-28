package com.finnchristian.tracker.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.SettingsUtil;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.data.TracksProvider;
import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.service.RunKeeperService;
import com.finnchristian.tracker.service.TrackerService;
import com.finnchristian.tracker.task.CalculationsAsyncTask;
import com.google.common.base.Strings;

import java.util.Date;
import java.util.List;


public class TrackDetailsInfoFragment extends Fragment {
    private static final String TAG = TrackDetailsInfoFragment.class.getSimpleName();
    public static final String ARG_TRACK_ID = "track_id";

    protected int trackId;
    protected TrackerService trackerService = null;
    protected ViewHolder viewHolder = null;
    protected ContentResolverHelper contentResolverHelper = null;

    private Intent trackerServiceIntent = null;

    /**
     * Calculations callback
     */
    protected final CalculationsAsyncTask.Callback calculationTaskCallback = new CalculationsAsyncTask.Callback() {
        @Override
        public void onResultReady(final CalculationsAsyncTask.Result result) {
            if(viewHolder == null || getActivity() == null) {
                return;
            }

            viewHolder.setPoints(String.valueOf(result.getLocationsCount()));

            final String durationFormattedValue = getResources().getString(R.string.detail_duration_value, result.getDurationHours(), result.getDurationMinutes());
            viewHolder.setDuration(durationFormattedValue);

            final String distanceFormattedValue;
            // show as km when distance exceeds 2000 meters
            if(result.getDistance() > 2000f) {
                distanceFormattedValue = getResources().getString(R.string.detail_distance_km_value, result.getDistance() / 1000f);
            }
            else {
                distanceFormattedValue = getResources().getString(R.string.detail_distance_meters_value, result.getDistance());
            }
            viewHolder.setDistance(distanceFormattedValue);

            // set max and min altitude
            final String maxAltitude = getResources().getString(R.string.detail_altitude_value, result.getMaxAltitude());
            final String minAltitude = getResources().getString(R.string.detail_altitude_value, result.getMinAltitude());
            viewHolder.setMaxAltitude(maxAltitude);
            viewHolder.setMinAltitude(minAltitude);
        }
    };

    /**
     * Rename track
     */
    protected final View.OnClickListener editTitleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditTrackDialogFragment.newInstance(trackId).show(getFragmentManager(), null);
        }
    };

    /**
     * Toggle tracking
     */
    protected final View.OnClickListener toggleTrackingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (trackerService != null) {
                if (trackerService.isTracking(trackId)) {
                    trackerService.stopTracking();
                    viewHolder.setToggleTrackingText(getText(R.string.detail_start_tracking_button));
                } else {
                    trackerService.startTracking(trackId);
                    viewHolder.setToggleTrackingText(getText(R.string.detail_stop_tracking_button));
                }
            }
        }
    };

    /**
     * Post fitness activity to RunKeeper
     */
    protected final View.OnClickListener postFitnessActivityToRunKeeperClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_no_network_connection_title)
                        .setMessage(R.string.dialog_no_network_connection_message)
                        .setPositiveButton(R.string.dialog_no_network_connection_turn_on_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        })
                        .setNegativeButton(R.string.dialog_no_network_connection_close_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();

                return;
            }

            final SettingsUtil settingsUtil = new SettingsUtil(getActivity());

//            settingsUtil.setRunKeeperToken(null);
            if(settingsUtil.getRunKeeperToken() == null) {
                final Fragment fragment = RunKeeperAuthorizationFragment.newInstance(trackId, true);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.view_pager_container, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
            else {
                RunKeeperService.startActionPostFitnessActivity(getActivity(), trackId);
            }
        }
    };

    /**
     * Service connection callback
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackerService.TrackerServiceBinder binder = (TrackerService.TrackerServiceBinder) service;
            trackerService = binder.getService();
            enableAndUpdateStartStopTrackingButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackerService = null;
        }
    };

    /**
     * Observes changes to geo locations.
     */
    private final ContentObserver geoLocationsContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange");

            if(contentResolverHelper != null) {
                final List<GeoLocation> geoLocations = contentResolverHelper.getTrackLocations(trackId);
                new CalculationsAsyncTask(geoLocations, calculationTaskCallback).execute();
            }
        }
    };

    /**
     * Observes changes to the track
     */
    private final ContentObserver trackContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange");

            if(contentResolverHelper != null && viewHolder != null) {
                final Track track = contentResolverHelper.getTrackById(trackId);
                viewHolder.setTitle(track.getName());

                if(track.isUploadedToRunKeeper()) {
                    viewHolder.setUploadedToRunKeeper(track.getLastUploadedToRunKeeperAsDate());
                }
            }
        }
    };

    /**
     * Receives broadcasts from RunKeeperService
     */
    private final BroadcastReceiver runKeeperServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = Strings.nullToEmpty(intent.getAction());
            final int trackid = intent.getIntExtra(RunKeeperService.PARAM_TRACK_ID, -1);

            if(trackId == TrackDetailsInfoFragment.this.trackId) {
                if (action.equals(RunKeeperService.Action.TRACK_UPLOADED_SUCCESSFULLY)) {
                    Toast.makeText(getActivity(), R.string.toast_successfully_uploaded_track_to_runkeeper, Toast.LENGTH_LONG).show();
                }
                else if (action.equals(RunKeeperService.Action.TRACK_UPLOAD_FAILED)) {
                    Toast.makeText(getActivity(), R.string.toast_failed_to_upload_track_to_runkeeper, Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TrackDetailFragment.
     */
    public static TrackDetailsInfoFragment newInstance(int trackId) {
        TrackDetailsInfoFragment fragment = new TrackDetailsInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Default constructor
     */
    public TrackDetailsInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, 0);
        }

        contentResolverHelper = new ContentResolverHelper(getActivity().getContentResolver());
        trackerServiceIntent = new Intent(getActivity(), TrackerService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_track_details_info, container, false);

        final Track track = contentResolverHelper.getTrackById(trackId);

        viewHolder = new ViewHolder(view);
        viewHolder.setTitle(track.getName());
        viewHolder.setCreated(track.getCreatedAsDate());
        viewHolder.setToggleTrackingOnClickListener(toggleTrackingClickListener);
        viewHolder.setEditTitleOnClickListener(editTitleClickListener);
        viewHolder.setPostFitnessActivityOnClickListener(postFitnessActivityToRunKeeperClickListener);

        // make button unable until we know whether we're tracking current track or not
        viewHolder.disableToggleTrackingButton();

        if(track.isUploadedToRunKeeper()) {
            viewHolder.setUploadedToRunKeeper(track.getLastUploadedToRunKeeperAsDate());
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        startAndBindWithTrackerService();

        // register observers (observes changes to geo locations and the track)
        getActivity().getContentResolver().registerContentObserver(TracksProvider.buildGeoLocationsUri(trackId), false, geoLocationsContentObserver);
        getActivity().getContentResolver().registerContentObserver(TracksProvider.buildTrackByIdUri(trackId), false, trackContentObserver);

        // register broadcast receiver
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RunKeeperService.Action.TRACK_UPLOADED_SUCCESSFULLY);
        intentFilter.addAction(RunKeeperService.Action.TRACK_UPLOAD_FAILED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(runKeeperServiceBroadcastReceiver, intentFilter);

        // do necessary calculations and update ui
        new CalculationsAsyncTask(contentResolverHelper.getTrackLocations(trackId), calculationTaskCallback).execute();
    }

    @Override
    public void onPause() {
        super.onPause();

        unbindAndStopTrackerService();

        // unregister observers
        getActivity().getContentResolver().unregisterContentObserver(geoLocationsContentObserver);
        getActivity().getContentResolver().unregisterContentObserver(trackContentObserver);

        // unregister broadcast receiver
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(runKeeperServiceBroadcastReceiver);
    }

    /**
     * Start tracker service and bind with it.
     */
    private void startAndBindWithTrackerService() {
        // Start tracker service
        final ComponentName componentName = getActivity().startService(trackerServiceIntent);

        if(componentName != null) {
            Log.d(TAG, "Tracker service successfully started");
        }
        else {
            Log.d(TAG, "Failed to start tracker service");
            return; // no point in continuing when the service didn't start
        }

        // Bind with tracker service
        final boolean bindingSucceeded = getActivity().bindService(trackerServiceIntent, serviceConnection, 0);

        if(bindingSucceeded) {
            Log.d(TAG, "Successfully bound with tracker service");
        }
        else {
            Log.e(TAG, "Failed to bind with tracker service");
        }
    }

    /**
     * Unbind with tracker service and stop the service if it isn't currently tracking.
     */
    private void unbindAndStopTrackerService() {
        final boolean isTracking = trackerService != null && trackerService.isTracking();

        // Unbind with tracker service
        getActivity().unbindService(serviceConnection);

        // Stop tracker service (if it isn't tracking)
        if(!isTracking) {
            final boolean successfullyStopped = getActivity().stopService(trackerServiceIntent);
            if(successfullyStopped) {
                Log.d(TAG, "Tracker service successfully stopped");
            }
            else {
                Log.e(TAG, "Failed to stop tracker service");
            }
        }
    }

    /**
     * Enable and update toggle button text. If the tracker service is tracking current track
     * the text will be "Stop tracking", otherwise it'll be "Start tracking".
     */
    private void enableAndUpdateStartStopTrackingButton() {
        viewHolder.enableToggleTrackingButton();

        final CharSequence buttonText = trackerService != null && trackerService.isTracking(trackId)
                ? getText(R.string.detail_stop_tracking_button)
                : getText(R.string.detail_start_tracking_button);

        viewHolder.setToggleTrackingText(buttonText);
    }

    /**
     * View holder class
     */
    private static class ViewHolder {
        private final View view;
        final TextView detailTitle;
        final TextView detailCreated;
        final TextView detailUploadedToRunKeeper;
        final TextView detailPoints;
        final TextView detailDistance;
        final TextView detailDuration;
        final TextView detailMaxAltitude;
        final TextView detailMinAltitude;
        final ImageView detailEditTitleImageView;
        final Button toggleTrackingButton;
        final Button postFitnessActivityToRunKeeperButton;

        public ViewHolder(final View view) {
            this.view = view;
            detailTitle = (TextView) view.findViewById(R.id.detail_title_text_view);
            detailCreated = (TextView) view.findViewById(R.id.detail_created_text_view);
            detailUploadedToRunKeeper = (TextView) view.findViewById(R.id.detail_uploaded_to_runkeeper_text_view);
            detailPoints = (TextView) view.findViewById(R.id.detail_points_text_view);
            detailDistance = (TextView) view.findViewById(R.id.detail_distance_text_view);
            detailDuration = (TextView) view.findViewById(R.id.detail_duration_text_view);
            detailMaxAltitude = (TextView) view.findViewById(R.id.detail_max_altitude_text_view);
            detailMinAltitude = (TextView) view.findViewById(R.id.detail_min_altitude_text_view);
            detailEditTitleImageView = (ImageView) view.findViewById(R.id.detail_title_edit_imageview);
            toggleTrackingButton = (Button)view.findViewById(R.id.toggle_tracking_button);
            postFitnessActivityToRunKeeperButton = (Button)view.findViewById(R.id.post_fitness_activity_to_runkeeper_button);
        }

        public void setTitle(final String value) {
            detailTitle.setText(value);
        }

        public void setCreated(final Date value) {
            final String text = view.getResources().getString(R.string.date_and_time_format, value);
            detailCreated.setText(text);
        }

        public void setUploadedToRunKeeper(final Date value) {
            final String text = view.getResources().getString(R.string.date_and_time_format, value);
            detailUploadedToRunKeeper.setText(text);
        }

        public void setPoints(final String value) {
            detailPoints.setText(value);
        }

        public void setDuration(final String value) {
            detailDuration.setText(value);
        }

        public void setDistance(final String value) {
            detailDistance.setText(value);
        }

        public void setMaxAltitude(final String value) {
            detailMaxAltitude.setText(value);
        }

        public void setMinAltitude(final String value) {
            detailMinAltitude.setText(value);
        }

        public void setToggleTrackingText(final CharSequence value) {
            toggleTrackingButton.setText(value);
        }

        public void enableToggleTrackingButton() {
            toggleTrackingButton.setEnabled(true);
        }

        public void disableToggleTrackingButton() {
            toggleTrackingButton.setEnabled(false);
        }

        public void setToggleTrackingOnClickListener(final View.OnClickListener onClickListener) {
            toggleTrackingButton.setOnClickListener(onClickListener);
        }

        public void setEditTitleOnClickListener(final View.OnClickListener onClickListener) {
            detailEditTitleImageView.setOnClickListener(onClickListener);
        }

        public void setPostFitnessActivityOnClickListener(final View.OnClickListener onClickListener) {
            postFitnessActivityToRunKeeperButton.setOnClickListener(onClickListener);
        }

    }
}
