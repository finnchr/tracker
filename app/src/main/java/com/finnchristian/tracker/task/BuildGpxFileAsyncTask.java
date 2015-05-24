package com.finnchristian.tracker.task;

import android.os.AsyncTask;
import android.os.Environment;

import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.model.gpx.Author;
import com.finnchristian.tracker.model.gpx.Gpx;
import com.finnchristian.tracker.model.gpx.Metadata;
import com.finnchristian.tracker.model.gpx.TrackPoint;
import com.finnchristian.tracker.model.gpx.TrackSegment;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildGpxFileAsyncTask extends AsyncTask<Void, Void, File>{
    public static interface Callback {
        void onResultReady(final File file);
    }

    private final List<GeoLocation> geoLocations;
    private final Track track;
    private final Callback callback;

    public BuildGpxFileAsyncTask(final Track track, final List<GeoLocation> geoLocations, final Callback callback) {
        this.track = track;
        this.geoLocations = geoLocations;
        this.callback = callback;
    }


    @Override
    protected File doInBackground(Void... params) {
        final Gpx gpx = new Gpx()
                .setCreator("Tracker")
                .setVersion("1.0")
                .setMetadata(new Metadata()
                    .setAuthor(new Author("Finn Chr. Reusch", "fcreusch@gmail.com"))
                    .setName(track.getName()));

        final com.finnchristian.tracker.model.gpx.Track gpxTrack = new com.finnchristian.tracker.model.gpx.Track()
                .setName(track.getName())
                .setNumber(track.getId());

        final TrackSegment trackSegment = new TrackSegment();

        for(final GeoLocation geoLocation : geoLocations) {
            final TrackPoint trackPoint = new TrackPoint();
            trackPoint.setTime(geoLocation.getCreated());
            trackPoint.setBearing(geoLocation.getBearing());
            trackPoint.setElevation(geoLocation.getAltitude());
            trackPoint.setLatitude(geoLocation.getLatitude());
            trackPoint.setLongitude(geoLocation.getLongitude());

            trackSegment.addTrackPoint(trackPoint);
        }

        gpxTrack.addSegment(trackSegment);
        gpx.setTrack(gpxTrack);

        final File root = Environment.getExternalStorageDirectory();
        final File file = new File(root, String.format("track-%s.gpx", track.getName()));

        try {
            final Serializer serializer = new Persister();
            serializer.write(gpx, file);

            return file;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(final File file) {
        if(callback != null && file != null) {
            callback.onResultReady(file);
        }
    }
}
