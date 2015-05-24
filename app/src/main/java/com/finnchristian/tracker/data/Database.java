package com.finnchristian.tracker.data;

import android.provider.BaseColumns;

public class Database {
    public interface Tracks {
        String NAME = "tracks";

        interface Columns extends BaseColumns {
            String NAME = "name";
            String CREATED = "created";
            String TYPE = "type";
            String LAST_UPLOADED_TO_RUNKEEPER = "last_uploaded_to_runkeeper";
        }
    }

    public interface GeoLocations {
        String NAME = "geolocations";

        interface Columns extends BaseColumns {
            String TRACK_ID = "track_id";
            String LATITUDE = "latitude";
            String LONGITUDE = "longitude";
            String CREATED = "created";
            String ALTITUDE = "altitude";
            String SPEED = "speed";
            String BEARING = "bearing";
            String ACCURACY = "accuracy";
            String TIME = "time";
        }
    }
}
