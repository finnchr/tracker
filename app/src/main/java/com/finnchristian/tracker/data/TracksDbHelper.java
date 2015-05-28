package com.finnchristian.tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TracksDbHelper extends SQLiteOpenHelper {
    private static final String TAG = TracksDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 5;
    static final String DATABASE_NAME = "tracks.db";


    public TracksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createTracksTableSql = MessageFormat.format(
                "create table {0}(" +
                "{1} integer primary key autoincrement" +
                ",{2} text not null" +
                ",{3} text not null" +
                ",{4} integer not null" +
                ",{5} integer not null default(-1)" +
                ");",
                Database.Tracks.NAME,
                Database.Tracks.Columns._ID,
                Database.Tracks.Columns.NAME,
                Database.Tracks.Columns.TYPE,
                Database.Tracks.Columns.CREATED,
                Database.Tracks.Columns.LAST_UPLOADED_TO_RUNKEEPER
        );

        final String createLocationsTableSql = MessageFormat.format(
                "create table {0}(" +
                "{1} integer primary key autoincrement" +
                ",{2} integer not null" +
                ",{3} integer not null" +
                ",{4} real not null" +
                ",{5} real not null" +
                ",{6} real not null default(0)" +
                ",{7} real not null default(0)" +
                ",{8} real not null default(0)" +
                ",{9} real not null default(0)" +
                ",{10} integer not null default(0)" +
                ", foreign key({2}) references {11}({12})" +
                ");",
                Database.GeoLocations.NAME,
                Database.GeoLocations.Columns._ID,
                Database.GeoLocations.Columns.TRACK_ID,
                Database.GeoLocations.Columns.CREATED,
                Database.GeoLocations.Columns.LATITUDE,
                Database.GeoLocations.Columns.LONGITUDE,
                Database.GeoLocations.Columns.ALTITUDE,
                Database.GeoLocations.Columns.SPEED,
                Database.GeoLocations.Columns.BEARING,
                Database.GeoLocations.Columns.ACCURACY,
                Database.GeoLocations.Columns.TIME,
                Database.Tracks.NAME,
                Database.Tracks.Columns._ID
        );

        db.execSQL(createTracksTableSql);
        db.execSQL(createLocationsTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Rename existing tables
        renameTable(db, Database.Tracks.NAME);
        renameTable(db, Database.GeoLocations.NAME);

        // Create tables
        onCreate(db);

        List<String> tracksColumnsToCopy = Arrays.asList(
                Database.Tracks.Columns._ID,
                Database.Tracks.Columns.NAME,
                Database.Tracks.Columns.CREATED,
                Database.Tracks.Columns.LAST_UPLOADED_TO_RUNKEEPER);

        // Copy data from old table to new table
        final Map<String, String> additionalValues = new HashMap<>();
        additionalValues.put(Database.Tracks.Columns.TYPE, "Hiking");
        copyValues(db, Database.Tracks.NAME, tracksColumnsToCopy, additionalValues);

        List<String> geoLocationsColumnsToCopy = Arrays.asList(
                Database.GeoLocations.Columns.TRACK_ID,
                Database.GeoLocations.Columns.LATITUDE,
                Database.GeoLocations.Columns.LONGITUDE,
                Database.GeoLocations.Columns.CREATED,
                Database.GeoLocations.Columns.ACCURACY,
                Database.GeoLocations.Columns.ALTITUDE,
                Database.GeoLocations.Columns.BEARING,
                Database.GeoLocations.Columns.SPEED);

        // Copy data from old table to new table
        copyValues(db, Database.GeoLocations.NAME, geoLocationsColumnsToCopy, null);

        // Drop temporary tables
        dropOldTable(db, Database.Tracks.NAME);
        dropOldTable(db, Database.GeoLocations.NAME);
    }

    protected void renameTable(final SQLiteDatabase db, final String tableName) {
        final String createTableSqlTemplate = "alter table {0} rename to _old_{0}";
        db.execSQL(MessageFormat.format(createTableSqlTemplate, tableName));
    }

    protected void copyValues(final SQLiteDatabase db, final String tableName, final List<String> columns, final Map<String, String> additionalColumnsAndValues) {
        final Joiner joiner = Joiner.on(",");
        final String sql;

        if(additionalColumnsAndValues == null) {
            final String insertSqlTemplate = "insert into {0} ({1}) select {1} from _old_{0}";
            sql = MessageFormat.format(insertSqlTemplate, tableName, joiner.join(columns));
        }
        else {
            final List<String> values = new ArrayList<>();
            for(final Map.Entry<String, String> entry : additionalColumnsAndValues.entrySet()) {
                values.add(MessageFormat.format("{0} as [{1}]", "'"+entry.getValue()+"'", entry.getKey()));
            }
            final String additionalColumns = joiner.join(additionalColumnsAndValues.keySet());
            final String additionalValues = joiner.join(values);
            final String insertSqlTemplate = "insert into {0} ({1},{2}) select {1},{3} from _old_{0}";
            sql = MessageFormat.format(insertSqlTemplate, tableName, joiner.join(columns), additionalColumns, additionalValues);

        }

        db.execSQL(sql);
    }

    protected void dropOldTable(final SQLiteDatabase db, final String tableName) {
        final String dropTableSqlTemplate = "drop table if exists _old_{0}";
        db.execSQL(MessageFormat.format(dropTableSqlTemplate, tableName));
    }

}
