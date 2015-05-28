package com.finnchristian.tracker.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.SettingsUtil;
import com.finnchristian.tracker.api.RunKeeper;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.model.runkeeper.FitnessActivity;
import com.finnchristian.tracker.model.runkeeper.Path;
import com.finnchristian.tracker.model.runkeeper.Token;
import com.finnchristian.tracker.model.runkeeper.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit.client.Response;

public class RunKeeperService extends IntentService {
    public interface Action {
        String TRACK_UPLOADED_SUCCESSFULLY = "TRACK_UPLOADED_SUCCESSFULLY";
        String TRACK_UPLOAD_FAILED = "TRACK_UPLOAD_FAILED";
    }

    public static final String PARAM_TRACK_ID = "PARAM_TRACK_ID";

    private static final String ACTION_POST_FITNESS_ACTIVITY = "ACTION_POST_FITNESS_ACTIVITY";
    private static final String ARG_TRACK_ID = "ARG_TRACK_ID";

    public static void startActionPostFitnessActivity(final Context context, final int trackId) {
        Intent intent = new Intent(context, RunKeeperService.class);
        intent.setAction(ACTION_POST_FITNESS_ACTIVITY);
        intent.putExtra(ARG_TRACK_ID, trackId);
        context.startService(intent);
    }

    public RunKeeperService() {
        super("RunKeeperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_POST_FITNESS_ACTIVITY.equals(action)) {
                final int trackId = intent.getIntExtra(ARG_TRACK_ID, -1);
                if(trackId > -1) {
                    handleActionSendToRunKeeper(trackId);
                }
            }

            // TODO Explore the option to download routes from RunKeeper
        }
    }

    private void handleActionSendToRunKeeper(final int trackId) {
        final FitnessActivity fitnessActivity = createFitnessActivity(trackId);

        final String runKeeperApiUrl = getString(R.string.uri_runkeeper_api_base);
        final Token runKeeperToken = new SettingsUtil(getApplicationContext()).getRunKeeperToken();

        final RunKeeper.Service runKeeperApi = RunKeeper.createService(runKeeperApiUrl, runKeeperToken);
        final User user = runKeeperApi.getUser();

        try {
            final Response response = runKeeperApi.postFitnessActivity(user.getFitnessActivities(), fitnessActivity);

            if (response.getStatus() == 201) {
                new ContentResolverHelper(getApplicationContext().getContentResolver()).trackUploadedToRunKeeper(trackId, new Date().getTime());
                broadcastSuccess(trackId);
            } else {
                broadcastFailure(trackId);
            }
        }
        catch (Exception ex) {
            broadcastFailure(trackId);
        }

        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private void broadcastSuccess(final int trackId) {
        final Intent intent = new Intent(Action.TRACK_UPLOADED_SUCCESSFULLY).putExtra(PARAM_TRACK_ID, trackId);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void broadcastFailure(final int trackId) {
        final Intent intent = new Intent(Action.TRACK_UPLOAD_FAILED).putExtra(PARAM_TRACK_ID, trackId);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private FitnessActivity createFitnessActivity(final int trackId) {
        final ContentResolverHelper contentResolverHelper = new ContentResolverHelper(getContentResolver());
        final Track track = contentResolverHelper.getTrackById(trackId);
        final List<GeoLocation> geoLocationList = contentResolverHelper.getTrackLocations(trackId);

        final List<Path> pathList = new ArrayList<>(geoLocationList.size());

        for(final GeoLocation geoLocation : geoLocationList) {
            pathList.add(new Path()
                    .setType("gps")
                    .setTimestamp((geoLocation.getCreated() - track.getCreated()) / 1000)
                    .setAltitude(geoLocation.getAltitude())
                    .setLatitude(geoLocation.getLatitude())
                    .setLongitude(geoLocation.getLongitude()));
        }

        return new FitnessActivity()
                .setSource(getApplicationContext().getString(R.string.app_name))
                .setNotes(track.getName())
                .setStartTime(track.getCreated())
                .setUtcOffset(getOffsetInHours(track.getCreated()))
                .setType(track.getType())
                .setPath(pathList);

    }

    private int getOffsetInHours(final long timestamp) {
        return TimeZone.getDefault().getOffset(timestamp) / 1000 / 60 / 60;
    }
}
