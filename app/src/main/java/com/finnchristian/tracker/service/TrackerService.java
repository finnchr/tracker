package com.finnchristian.tracker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.SettingsUtil;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.data.TracksProvider;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.ui.TrackDetailsInfoFragment;
import com.finnchristian.tracker.ui.TrackDetailsActivity;

import java.util.Date;

public class TrackerService extends Service {
    private static final int NOTIFICATION_ID = 0;

    public static class TrackerServiceBinder extends Binder {
        private final TrackerService service;

        public TrackerServiceBinder(TrackerService service) {
            this.service = service;
        }

        public TrackerService getService() {
            return service;
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            long created = new Date().getTime();

            Uri uri = TracksProvider.buildGeoLocationsUri(trackId);

            ContentValues values = new ContentValues();
            //values.put("track_id", trackId);
            values.put("created", created);
            values.put("latitude", location.getLatitude() );
            values.put("longitude", location.getLongitude());
            values.put("altitude", location.getAltitude());
            values.put("speed", location.getSpeed());
            values.put("bearing", location.getBearing());
            values.put("accuracy", location.getAccuracy());
            values.put("time", location.getTime());

            getApplicationContext().getContentResolver().insert(uri, values);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private final IBinder binder = new TrackerServiceBinder(this);
    private LocationManager locationManager = null;
    private int trackId = -1;
    private NotificationManager notificationManager = null;

    public static final int DEFAULT_TIME_INTERVAL = 1000 * 10; // 10 sec
    public static final int DEFAULT_DISTANCE_INTERVAL = 5; // 5 meters


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Service.LOCATION_SERVICE);
        }
        else {
            locationManager.removeUpdates(locationListener);
        }

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        removeNotification();

        locationManager.removeUpdates(locationListener);
        locationManager = null;
    }


    protected Notification createNotification(final int trackId) {
        final Track track = new ContentResolverHelper(getContentResolver()).getTrackById(trackId);

        final Intent intent = new Intent(getApplicationContext(), TrackDetailsActivity.class);
        intent.putExtra(TrackDetailsInfoFragment.ARG_TRACK_ID, track.getId());

        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final String notificationTitle = getString(R.string.service_title_text);
        final String notificationText = getString(R.string.service_context_text, track.getName());

        return new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_walk_notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true) // make notification persistent
                .build();
    }

    protected void removeNotification() {
        if(notificationManager != null) {
            notificationManager.cancel(0);
            notificationManager = null;
        }
    }

    public boolean isTracking(int trackId) {
        return trackId > 0 && this.trackId == trackId;
    }

    public boolean isTracking() {
        return trackId > 0;
    }

    // TODO Return value that tells whether tracking started or not (maybe the gps isn't enabled)
    public void startTracking(int trackId) {
        if(trackId > 0) {
            this.trackId = trackId;

            final SettingsUtil settingsUtil = new SettingsUtil(getApplicationContext());

            int distanceInterval = settingsUtil.getGpsDistanceInterval();
            if(distanceInterval <= 0) {
                distanceInterval = DEFAULT_DISTANCE_INTERVAL;
            }

            int timeInterval = settingsUtil.getGpsTimeInterval();
            timeInterval *= 1000; // convert to milliseconds
            if(timeInterval <= 0) {
                timeInterval = DEFAULT_TIME_INTERVAL;
            }

            // request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterval, distanceInterval, locationListener);

            notificationManager.notify(NOTIFICATION_ID, createNotification(trackId));
        }
    }

    public void stopTracking() {
        // remove notification
        notificationManager.cancel(NOTIFICATION_ID);

        // remove location listener
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        this.trackId = -1;

        //notificationManager.notify(NOTIFICATION_ID, createNotification(getString(R.string.service_context_text_default)));
    }
}
