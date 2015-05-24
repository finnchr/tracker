package com.finnchristian.tracker.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.model.gpx.Author;
import com.finnchristian.tracker.model.gpx.Gpx;
import com.finnchristian.tracker.model.gpx.Metadata;
import com.finnchristian.tracker.model.gpx.TrackPoint;
import com.finnchristian.tracker.model.gpx.TrackSegment;
import com.google.common.base.Strings;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TracksProvider extends ContentProvider {
    private static final String TAG = TracksProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private interface UriType {
        int TRACKS = 0;
        int TRACK_BY_ID = 1;
        int GEO_LOCATIONS = 2;
        int GPX_FILE_BY_ID = 3;
    }

    public interface Uris {
        String AUTHORITY = "com.finnchristian.tracker.provider";
        String TRACKS = "tracks";
        String GEO_LOCATIONS = "geolocations";
        String GPX = "gpx";
        Uri BASE = Uri.parse("content://" + Uris.AUTHORITY);
        Uri TRACKS_CONTENT_URI = BASE.buildUpon().appendPath(Uris.TRACKS).build();
    }

    private interface ContentType {
        String TRACK = String.format("%s/%s/%s", ContentResolver.CURSOR_ITEM_BASE_TYPE, Uris.AUTHORITY, Uris.TRACKS);
        String TRACKS = String.format("%s/%s/%s", ContentResolver.CURSOR_DIR_BASE_TYPE, Uris.AUTHORITY, Uris.TRACKS);
        String GEO_LOCATIONS = String.format("%s/%s/%s", ContentResolver.CURSOR_DIR_BASE_TYPE, Uris.AUTHORITY, Uris.GEO_LOCATIONS);
        String GPX = String.format("%s/%s/%s/%s", ContentResolver.CURSOR_ITEM_BASE_TYPE, Uris.AUTHORITY, Uris.TRACKS, Uris.GPX);
    }


    static {
        URI_MATCHER.addURI(Uris.AUTHORITY, Uris.TRACKS, UriType.TRACKS);
        URI_MATCHER.addURI(Uris.AUTHORITY, String.format("%s/#", Uris.TRACKS), UriType.TRACK_BY_ID);
        URI_MATCHER.addURI(Uris.AUTHORITY, String.format("%s/#/%s", Uris.TRACKS, Uris.GEO_LOCATIONS), UriType.GEO_LOCATIONS);
        URI_MATCHER.addURI(Uris.AUTHORITY, String.format("%s/#/*", Uris.TRACKS), UriType.GPX_FILE_BY_ID);
    }

    /**
     * Build uri to get a track by it id.
     * @param trackId
     * @return
     */
    public static Uri buildTrackByIdUri(final String trackId) {
        return Uris.TRACKS_CONTENT_URI.buildUpon().appendPath(trackId).build();
    }
    public static Uri buildTrackByIdUri(final int trackId) {
        return buildTrackByIdUri(String.valueOf(trackId));
    }

    /**
     * Build uri to get geo locations for a track.
     * @param trackId
     * @return
     */
    public static Uri buildGeoLocationsUri(final String trackId) {
        return buildTrackByIdUri(trackId).buildUpon().appendPath(Uris.GEO_LOCATIONS).build();
    }

    public static Uri buildGeoLocationsUri(final int trackId) {
        return buildGeoLocationsUri(String.valueOf(trackId));
    }

    public static Uri buildGeoLocationUri(final String trackId, final String geoLocationId) {
        return buildGeoLocationsUri(trackId).buildUpon().appendPath(geoLocationId).build();
    }

    /**
     * Build uri to get track as gpx
     */
    public static Uri buildGpxUri(final int trackId, final String name) {
        return buildTrackByIdUri(trackId).buildUpon().appendPath(String.format("%s.gpx", name)).build();
    }


    protected TracksDbHelper dbHelper;


    private static String getTrackId(Uri uri) {
        return uri.getPathSegments().get(1);
    }

    private static String getGpxName(Uri uri) {
        return uri.getPathSegments().get(2);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new TracksDbHelper(getContext());
        final int oldVersion = dbHelper.getReadableDatabase().getVersion();
        return true;
    }

    @Override
    public String getType(Uri uri) {

        switch (URI_MATCHER.match(uri)) {
            case UriType.TRACKS:
                return ContentType.TRACKS;

            case UriType.TRACK_BY_ID:
                return ContentType.TRACK;

            case UriType.GEO_LOCATIONS:
                return ContentType.GEO_LOCATIONS;

            case UriType.GPX_FILE_BY_ID:
                return ContentType.GPX;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            // Query tracks by whatever
            case UriType.TRACKS: {
                cursor = dbHelper.getReadableDatabase().query(Database.Tracks.NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }

            // Query tracks by id
            case UriType.GPX_FILE_BY_ID: {
                //final String trackId = getTrackId(uri);
                final String name = getGpxName(uri);

                final MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_display_name", "_size"}, 1);
                matrixCursor.addRow(new Object[]{name, 0});
                cursor = matrixCursor;
                break;
            }

            case UriType.TRACK_BY_ID: {
                final String trackId = getTrackId(uri);

                cursor = dbHelper.getReadableDatabase().query(
                        Database.Tracks.NAME,
                        projection,
                        String.format("%s = ?", Database.Tracks.Columns._ID),
                        new String[]{ trackId },
                        null,
                        null,
                        sortOrder);

                break;
            }

            // Query locations by track id
            case UriType.GEO_LOCATIONS: {
                final String trackId = getTrackId(uri);

                cursor = dbHelper.getReadableDatabase().query(
                        Database.GeoLocations.NAME,
                        projection,
                        String.format("%s = ?", Database.GeoLocations.Columns.TRACK_ID),
                        new String[]{trackId},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                cursor = null;
                break;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri = null;

        switch (URI_MATCHER.match(uri)) {
            // Insert track
            case UriType.TRACKS: {

                long _id = dbHelper.getWritableDatabase().insert(Database.Tracks.NAME, null, values);

                if(_id > 0) {
                    returnUri = uri.buildUpon().appendPath(String.valueOf(_id)).build();
                }
                else {
                    throw new RuntimeException(String.format("Failed to insert track [uri=%s]", uri));
                }

                break;
            }

            // Insert location
            case UriType.GEO_LOCATIONS: {
                final String trackId = getTrackId(uri);

                values.put("track_id", trackId);

                long _id = dbHelper.getWritableDatabase().insert(Database.GeoLocations.NAME, null, values);

                if(_id > 0) {
                    returnUri = buildGeoLocationUri(trackId, String.valueOf(_id));
                }
                else {
                    throw new RuntimeException(String.format("Failed to insert location [uri=%s]", uri));
                }

                break;
            }

            default: {
                final String msg = String.format("Uri not recognised [uri=%s]", uri.toString());
                Log.d(TAG, msg);
                throw new UnsupportedOperationException(msg);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int affectedRows;

        switch (URI_MATCHER.match(uri)) {
            // Remove track by filter
            case UriType.TRACKS:
                Log.d(TAG, "Removal of tracks based on parameters is not implemented");
                throw new RuntimeException("Removal of tracks based on parameters is not implemented");

            // Remove track by id
            case UriType.TRACK_BY_ID: {
                final String trackId = getTrackId(uri);
                final int affectedTracks = dbHelper.getWritableDatabase().delete(Database.Tracks.NAME, "_id = ?", new String[] { trackId });
                final int affectedLocations = delete(buildGeoLocationsUri(trackId), null, null);
                affectedRows = affectedTracks + affectedLocations;

                if(affectedTracks > 0) {
                    Log.d(TAG, String.format("Removed track by id [id=%s, affectedTracks=%s]", trackId, affectedTracks));
                }
                else {
                    Log.d(TAG, String.format("Unable to removed track by id [track_id=%s]", trackId));
                }
                break;
            }

            // Remove all locations owned by a track
            case UriType.GEO_LOCATIONS: {
                final String trackId = getTrackId(uri);
                final String filter = String.format("%s = ?", Database.GeoLocations.Columns.TRACK_ID);
                affectedRows = dbHelper.getWritableDatabase().delete(Database.GeoLocations.NAME, filter, new String[]{trackId});

                if(affectedRows > 0) {
                    Log.d(TAG, String.format("Removed locations owned by track [track_id=%s]", trackId));
                }
                else {
                    Log.d(TAG, String.format("Unable to removed locations owned by track [id=%s]", trackId));
                }

                break;
            }

            default: {
                final String msg = String.format("Uri not recognised [uri=%s]", uri.toString());
                Log.d(TAG, msg);
                throw new UnsupportedOperationException(msg);
            }
        }

        if(affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int affectedRows;

        switch (URI_MATCHER.match(uri)) {
            // Update track by filter
            case UriType.TRACKS:
                Log.d(TAG, "Updating track based on parameters is not implemented");
                throw new RuntimeException("Updating track based on parameters is not implemented");

            // Update track by id
            case UriType.TRACK_BY_ID: {
                final String trackId = getTrackId(uri);

                // Only allow name changes
                values.remove(Database.Tracks.Columns._ID);
                values.remove(Database.Tracks.Columns.CREATED);

                affectedRows = dbHelper.getWritableDatabase().update(Database.Tracks.NAME, values, "_id = ?", new String[]{trackId});

                if(affectedRows > 0) {
                    Log.d(TAG, String.format("Updated track by id [id=%s]", trackId));
                }
                else {
                    Log.d(TAG, String.format("Unable to update track by id [track_id=%s]", trackId));
                }
                break;
            }

            default: {
                final String msg = String.format("Uri not recognised [uri=%s]", uri.toString());
                Log.d(TAG, msg);
                throw new UnsupportedOperationException(msg);
            }
        }

        if(affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        switch (URI_MATCHER.match(uri)){
            case UriType.GPX_FILE_BY_ID:
                if("r".equalsIgnoreCase(mode)) {
                    final String trackId = getTrackId(uri);
                    final String name = getGpxName(uri);
                    final Cursor trackCursor = query(buildTrackByIdUri(trackId), null, null, null, null);
                    final Track track = cursorToTrack(trackCursor);

                    if (track != null) {
                        final Cursor locationsCursor = query(buildGeoLocationsUri(trackId), null, null, null, null);
                        if (locationsCursor != null) {
                            final List<GeoLocation> geoLocations = cursorToGeoLocations(track.getId(), locationsCursor);
                            final File file = buildGpxFile(track, geoLocations, name);

                            try {
                                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                            }
                            catch (FileNotFoundException e) {
                                Log.e(TAG, "File doesn't exists", e);
                            }
                        }
                    }
                }
                else {
                    throw new UnsupportedOperationException(mode + " is not supported");
                }
                break;

            default:
                return null;
        }

        return null;
    }

    /**
     * Map track to model.
     * @param cursor
     * @return
     */
    private Track cursorToTrack(final Cursor cursor) {
        if(cursor != null && cursor.moveToFirst()) {
            final int trackId = cursor.getInt(cursor.getColumnIndex(Database.Tracks.Columns._ID));
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

    /**
     * Map geo locations to model.
     * @param trackId
     * @param cursor
     * @return
     */
    private List<GeoLocation> cursorToGeoLocations(final int trackId, final Cursor cursor) {
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

    /**
     * Serialize track and geo locations and write to file.
     * @param track
     * @param geoLocations
     * @param name
     * @return
     */
    private File buildGpxFile(final Track track, final List<GeoLocation> geoLocations, final String name) {
        final Context context = getContext();
        final Gpx gpx = new Gpx()
                .setCreator(context.getString(R.string.gpx_creator))
                        .setVersion(context.getString(R.string.gpx_version))
                        .setMetadata(new Metadata()
                                .setAuthor(new Author(context.getString(R.string.gpx_author_name), context.getString(R.string.gpx_author_email)))
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
        //final File file = new File(root, String.format("track-%s.gpx", !Strings.isNullOrEmpty(name) ? name : track.getName()));
        final File file = new File(root, name);

        try {
            final Serializer serializer = new Persister();
            serializer.write(gpx, file);

            return file;
        }
        catch (Exception e) {
            return null;
        }

    }
}
