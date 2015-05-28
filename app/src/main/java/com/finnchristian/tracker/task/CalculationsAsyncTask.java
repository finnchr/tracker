package com.finnchristian.tracker.task;

import android.location.Location;
import android.os.AsyncTask;

import com.finnchristian.tracker.model.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class CalculationsAsyncTask extends AsyncTask<Void, Void, CalculationsAsyncTask.Result> {
    public static class Result {
        private float distance;
        private long duration;
        private long durationHours;
        private long durationMinutes;
        private double maxAltitude;
        private double minAltitude;
        private int locationsCount;
        private final List<LatLng> points = new ArrayList<>();
        private LatLng firstPoint;
        private LatLng lastPoint;

        public float getDistance() {
            return distance;
        }

        public long getDuration() {
            return duration;
        }

        public long getDurationHours() {
            return durationHours;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public double getMaxAltitude() {
            return maxAltitude;
        }

        public double getMinAltitude() {
            return minAltitude;
        }

        public int getLocationsCount() {
            return locationsCount;
        }

        public List<LatLng> getPoints() {
            return points;
        }

        public LatLng getFirstPoint() {
            return firstPoint;
        }

        public LatLng getLastPoint() {
            return lastPoint;
        }
    }

    public static interface Callback {
        void onResultReady(final Result result);
    }

    private final Callback callback;
    private final List<GeoLocation> geoLocations;

    public CalculationsAsyncTask(final List<GeoLocation> geoLocations, final Callback callback) {
        this.geoLocations = geoLocations;
        this.callback = callback;
    }

    @Override
    protected Result doInBackground(Void... params) {
        if(geoLocations != null && geoLocations.size() > 0) {
            final Result result = new Result();

            result.locationsCount = geoLocations.size();

            final GeoLocation firstGeoLocation = geoLocations.get(0);
            final GeoLocation lastGeoLocation = geoLocations.get(geoLocations.size() - 1);

            result.firstPoint = firstGeoLocation.getLatLng();
            result.lastPoint = lastGeoLocation.getLatLng();
            result.duration = lastGeoLocation.getCreated() - firstGeoLocation.getCreated();

            // calculate duration hours and minutes
            final double durationInMinutes = result.getDuration() / 1000f / 60f;
            result.durationHours = Math.round(Math.floor(durationInMinutes / 60f));
            result.durationMinutes = Math.round(durationInMinutes % 60f);

            final int size = geoLocations.size();
            for(int i = 1; i <= size; i++) {
                final GeoLocation current = geoLocations.get(i - 1);

                // add lat lng point
                result.points.add(current.getLatLng());

                final Location currentLocation = current.toLocation();

                // we haven't reached the last location yet
                if(i < size) {
                    final Location nextLocation = geoLocations.get(i).toLocation();

                    // add distance
                    result.distance += currentLocation.distanceTo(nextLocation);
                }

                // find max and min altitude (altitude == 0.0f -> location doesn't have an altitude)
                if(currentLocation.getAltitude() > 0f) {
                    result.minAltitude = result.minAltitude == 0.0f
                            ? currentLocation.getAltitude()
                            : Math.min(result.minAltitude, currentLocation.getAltitude());

                    result.maxAltitude = result.maxAltitude == 0.0f
                            ? currentLocation.getAltitude()
                            : Math.max(result.maxAltitude, currentLocation.getAltitude());

                }
            }

            return result;
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Result result) {
        if(callback != null && result != null) {
            callback.onResultReady(result);
        }
    }
}
