package com.finnchristian.tracker.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;

import java.util.ArrayList;
import java.util.List;

public class ContentResolverHelper {
    final ContentResolver contentResolver;

    public ContentResolverHelper(final ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    public Track getTrackById(final int trackId) {
        Cursor cursor = contentResolver.query(TracksProvider.buildTrackByIdUri(String.valueOf(trackId)),
                new String[] {
                        Database.Tracks.Columns._ID,
                        Database.Tracks.Columns.CREATED,
                        Database.Tracks.Columns.NAME,
                        Database.Tracks.Columns.TYPE,
                        Database.Tracks.Columns.LAST_UPLOADED_TO_RUNKEEPER
                },
                "_id=?",
                new String[]{ String.valueOf(trackId) },
                null);

        if(cursor != null && cursor.moveToFirst()) {
            final String name = cursor.getString(cursor.getColumnIndex(Database.Tracks.Columns.NAME));
            final String type = cursor.getString(cursor.getColumnIndex(Database.Tracks.Columns.TYPE));
            final long created = cursor.getLong(cursor.getColumnIndex(Database.Tracks.Columns.CREATED));
            final long lastUploadedToRunKeeper = cursor.getLong(cursor.getColumnIndex(Database.Tracks.Columns.LAST_UPLOADED_TO_RUNKEEPER));

            final Track track = new Track();
            track.setId(trackId);
            track.setName(name);
            track.setType(type);
            track.setCreated(created);
            track.setLastUploadedToRunKeeper(lastUploadedToRunKeeper);

            return track;
        }
        else {
            return null;
        }
    }

    public List<GeoLocation> getTrackLocations(final int trackId) {
        Cursor cursor = contentResolver.query(TracksProvider.buildGeoLocationsUri(trackId),
                new String[] {
                        Database.GeoLocations.Columns._ID,
                        Database.GeoLocations.Columns.CREATED,
                        Database.GeoLocations.Columns.LATITUDE,
                        Database.GeoLocations.Columns.LONGITUDE,
                        Database.GeoLocations.Columns.ALTITUDE,
                        Database.GeoLocations.Columns.BEARING,
                        Database.GeoLocations.Columns.SPEED,
                        Database.GeoLocations.Columns.ACCURACY
                },
                null, null, null);

        final List<GeoLocation> geoLocationList = new ArrayList<>();

        while (cursor.moveToNext()) {
            final int id = cursor.getInt(cursor.getColumnIndex(Database.GeoLocations.Columns._ID));
            final long created = cursor.getLong(cursor.getColumnIndex(Database.GeoLocations.Columns.CREATED));
            final double latitude = cursor.getDouble(cursor.getColumnIndex(Database.GeoLocations.Columns.LATITUDE));
            final double longitude = cursor.getDouble(cursor.getColumnIndex(Database.GeoLocations.Columns.LONGITUDE));
            final double altitude = cursor.getDouble(cursor.getColumnIndex(Database.GeoLocations.Columns.ALTITUDE));
            final float bearing = cursor.getFloat(cursor.getColumnIndex(Database.GeoLocations.Columns.BEARING));
            final float speed = cursor.getFloat(cursor.getColumnIndex(Database.GeoLocations.Columns.SPEED));
            final float accuracy = cursor.getFloat(cursor.getColumnIndex(Database.GeoLocations.Columns.ACCURACY));


            final GeoLocation geoLocation = new GeoLocation();
            geoLocation.setTrackId(trackId);
            geoLocation.setId(id);
            geoLocation.setCreated(created);
            geoLocation.setLatitude(latitude);
            geoLocation.setLongitude(longitude);
            geoLocation.setAltitude(altitude);
            geoLocation.setBearing(bearing);
            geoLocation.setSpeed(speed);
            geoLocation.setAccuracy(accuracy);

            geoLocationList.add(geoLocation);
        }

        return geoLocationList;
    }

    public void createTrack(final String name, final long timestamp, final String type) {
        ContentValues values = new ContentValues();
        values.put(Database.Tracks.Columns.NAME, name);
        values.put(Database.Tracks.Columns.CREATED, timestamp);
        values.put(Database.Tracks.Columns.TYPE, type);

        contentResolver.insert(TracksProvider.Uris.TRACKS_CONTENT_URI, values);
    }

    public boolean deleteTrack(final int trackId) {
        final Uri uri = TracksProvider.buildTrackByIdUri(trackId);
        return 1 == contentResolver.delete(uri, null, null);
    }

    public boolean updateTrack(final int trackId, final String name, final String type) {
        final ContentValues values = new ContentValues();
        values.put(Database.Tracks.Columns.NAME, name);
        values.put(Database.Tracks.Columns.TYPE, type);

        final int n = contentResolver.update(TracksProvider.buildTrackByIdUri(trackId), values, null, null);
        return n == 1;
    }

    public boolean trackUploadedToRunKeeper(final int trackId, final long timestamp) {
        final ContentValues values = new ContentValues();
        values.put(Database.Tracks.Columns.LAST_UPLOADED_TO_RUNKEEPER, timestamp);

        final int n = contentResolver.update(TracksProvider.buildTrackByIdUri(trackId), values, null, null);
        return n == 1;
    }
}
